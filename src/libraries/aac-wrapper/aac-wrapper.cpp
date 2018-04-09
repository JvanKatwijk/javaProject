#include	"neaacdec.h"
#include	"utils_AACDecoder.h"
#include	<stdio.h>
#include	<stdint.h>
#include	"ringbuffer.h"
#include	<math.h>

static	bool			aacInitialized = false;
static	NeAACDecHandle		aacHandle;
static	long unsigned int	sample_rate;
static	     unsigned char	channels;
static	RingBuffer<int16_t>     *audioBuffer	= NULL;
/*
 * Class:     utils_AACDecoder
 * Method:    aacReset
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_utils_AACDecoder_aacReset
	                         (JNIEnv *, jobject) {
	aacInitialized	= false;
	if (audioBuffer == NULL)
	   audioBuffer	= new RingBuffer <int16_t> (2 * 32768);
	else
	   audioBuffer	-> FlushRingBuffer ();
}

/*
 * Class:     utils_AACDecoder
 * Method:    aacInitialize
 * Signature: (IIII)Z
 */
int get_aac_channel_configuration (int16_t m_mpeg_surround_config,
                                   uint8_t aacChannelMode) {

        switch(m_mpeg_surround_config) {
           case 0:     // no surround
              return aacChannelMode ? 2 : 1;
           case 1:     // 5.1
              return 6;
           case 2:     // 7.1
              return 7;
           default:
              return -1;
        }
}

JNIEXPORT jboolean JNICALL Java_utils_AACDecoder_aacInitialize
	(JNIEnv *env, jobject obj,
	   jint dacRate, jint sbrFlag, jint mpegSurround, jint aacChannelMode) {
	if (aacInitialized)
	   return true;
	aacHandle = NeAACDecOpen	();
/* AudioSpecificConfig structure (the only way to select 960 transform here!)
 *
 *  00010 = AudioObjectType 2 (AAC LC)
 *  xxxx  = (core) sample rate index
 *  xxxx  = (core) channel config
 *  100   = GASpecificConfig with 960 transform
 *
 * SBR: implicit signaling sufficient - libfaad2
 * automatically assumes SBR on sample rates <= 24 kHz
 * => explicit signaling works, too, but is not necessary here
 *
 * PS:  implicit signaling sufficient - libfaad2
 * therefore always uses stereo output (if PS support was enabled)
 * => explicit signaling not possible, as libfaad2 does not
 * support AudioObjectType 29 (PS)
 */
	int core_sr_index =
                     dacRate ? (sbrFlag ? 6 : 3) :
                               (sbrFlag ? 8 : 5);   // 24/48/16/32 kHz
        int core_ch_config = get_aac_channel_configuration (mpegSurround,
                                                            aacChannelMode);
        if (core_ch_config == -1) {
           printf ("Unrecognized mpeg surround config (ignored): %d\n",
                                               mpegSurround);
           return false;
        }

        uint8_t asc[2];
        asc[0] = 0b00010 << 3 | core_sr_index >> 1;
        asc[1] = ((core_sr_index & 0x01) << 7) | (core_ch_config << 3) | 0b100;
        long int init_result = NeAACDecInit2 (aacHandle,
                                              asc,
                                              sizeof (asc),
                                              &sample_rate,
                                              &channels);

	if (init_result != 0) {
/*      If some error initializing occured, skip the file */
	   printf ("Error initializing decoder library: %s\n",
	                        NeAACDecGetErrorMessage (-init_result));
	   NeAACDecClose (aacHandle);
	   return false;
	}
	aacInitialized = true;
	return true;
}

/*
 * Class:     utils_AACDecoder
 * Method:    aacDecode
 * Signature: ([B[I)I
 */
JNIEXPORT jint JNICALL Java_utils_AACDecoder_aacDecode
	(JNIEnv *env, jobject obj,
	            jbyteArray data, jint startAddress,
	                       jint frameLength, jintArray inf) {
int32_t 	samples;
long unsigned int       sampleRate;
NeAACDecFrameInfo       hInfo;
uint8_t channels;
jbyte *dataBody	= env -> GetByteArrayElements (data, 0);
jint  *infoBody = env -> GetIntArrayElements  (inf, 0);

uint8_t	temp [frameLength + 10];
int	i;

	for (i = 0; i < frameLength; i ++)
	   temp [i] = dataBody [startAddress + i];
	for (i = frameLength; i < frameLength + 10; i ++)
	   temp [i] = 0;
	
	int16_t *outBuffer = (int16_t *)NeAACDecDecode (aacHandle,
	                                                &hInfo,
	                                                temp,
	                                                frameLength);
	infoBody [0]	= hInfo. error;
	infoBody [1]	= hInfo. samples;
	infoBody [2]	= hInfo. samplerate;
        env     -> ReleaseIntArrayElements  (inf, infoBody, 0);
	env     -> ReleaseByteArrayElements  (data, dataBody, 0);
	samples		= hInfo. samples;
        sample_rate 	= hInfo. samplerate;
	channels	= hInfo. channels;
        if (hInfo. error != 0) {
           fprintf (stderr, "Warning: %s\n",
                       faacDecGetErrorMessage (hInfo. error));
           return 0;
        }

        if (hInfo. channels == 2) 
	   audioBuffer  -> putDataIntoBuffer (outBuffer, samples);
	else
        if (channels == 1) {
           int16_t *buffer = (int16_t *)alloca (2 * samples);
           int16_t i;
           for (i = 0; i < samples; i ++) {
              buffer [2 * i]    = ((int16_t *)outBuffer) [i];
              buffer [2 * i + 1] = buffer [2 * i];
           }
           audioBuffer  -> putDataIntoBuffer (buffer, 2 * samples);
        }

	return hInfo.channels == 2 ? samples : 2 * samples;
}

/*
 * Class:     utils_AACDecoder
 * Method:    aacFetchData
 * Signature: ([S)I
 */
JNIEXPORT jint JNICALL Java_utils_AACDecoder_aacFetchData
 	(JNIEnv *env, jobject obj, jshortArray data) {
	jsize len = env -> GetArrayLength (data);
	jshort *body = env -> GetShortArrayElements (data, 0);	
	int amount = audioBuffer -> GetRingBufferReadAvailable ();

	if (len < amount)
	   amount = len;

	len	= audioBuffer -> getDataFromBuffer (body, amount);

        env     -> ReleaseShortArrayElements  (data, body, 0);
	return len;
}

