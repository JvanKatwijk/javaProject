
#include "rtlsdr-handler.h"
#include "devices_rtlsdrDevice.h"


/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdrInit
 * Signature: (IIZ)J
 */
JNIEXPORT jlong JNICALL Java_devices_rtlsdrDevice_rtlsdrInit
  (JNIEnv * env, jobject obj, jint frequency, jint gain, jboolean ag) {
	rtlsdrHandler *handle =  NULL;
	try {
	   handle = new rtlsdrHandler (frequency, 0, gain, ag, 0);
	} catch (int e) {}
	fprintf (stderr, "in de C wereld is handle %ld\n", handle);
	return reinterpret_cast <jlong>(handle);
}

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_getSamples
 * Signature: (J[FI)I
 */
JNIEXPORT jint JNICALL Java_devices_rtlsdrDevice_rtlsdr_1getSamples
  (JNIEnv *env, jobject obj, jlong handle, jfloatArray samples, jint amount) {
	if (handle == 0)
	   return 0;
std::complex<float> temp [(int) amount];
jfloat *body = env -> GetFloatArrayElements (samples, 0);
rtlsdrHandler * h = reinterpret_cast <rtlsdrHandler *>(handle);
int realAmount = h -> getSamples (temp, amount);
int	i;
	for (i = 0; i < realAmount; i ++) {
	   std::complex<float> t = temp [i];
	   body [2 * i    ] = real (t);
	   body [2 * i + 1] = imag (t);
	}
        env -> ReleaseFloatArrayElements (samples, body, 0);
        return realAmount;
}

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_samples
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_devices_rtlsdrDevice_rtlsdr_1samples
  (JNIEnv * env, jobject obj, jlong handle) {
	if (handle == 0)
	   return 0;
	
	rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
	return h -> Samples ();
}

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_resetBuffer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1resetBuffer
  (JNIEnv * env, jobject obj, jlong handle) {
	if (handle == 0)
	   return;

	rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
	h -> resetBuffer ();
}

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_restartReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1restartReader
  (JNIEnv * env, jobject obj, jlong handle) {
	if (handle == 0)
	   return;
	rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
	h -> restartReader ();
}

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_stopReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1stopReader
  (JNIEnv *env, jobject obj, jlong handle) {
	if (handle == 0)
	   return;
	rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
        h -> stopReader ();
}


/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_setVFOFrequency
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1setVFOFrequency
  (JNIEnv *env, jobject obj , jlong handle, jint freq) {
	if (handle == 0)
           return;
        rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
	h -> setVFOFrequency (freq);
}

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_getVFOFrequency
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_devices_rtlsdrDevice_rtlsdr_1getVFOFrequency
  (JNIEnv *env , jobject obj , jlong handle) {
       if (handle == 0)
           return 0;
        rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
	return h -> getVFOFrequency ();
}

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_setGain
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1setGain
  (JNIEnv *env , jobject obj , jlong handle , jint gain) {
       if (handle == 0)
           return;
        rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
	h -> setGain (gain);
}

JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1autogain
  (JNIEnv *env , jobject obj , jlong handle , jboolean gain) {
       if (handle == 0)
           return;
        rtlsdrHandler *h = reinterpret_cast <rtlsdrHandler *> (handle);
	h -> autogain (gain);
}

