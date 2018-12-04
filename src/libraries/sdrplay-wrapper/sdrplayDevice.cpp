#
/*
 *    Copyright (C) 2014 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of DAB java
 *    DAB java is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    DAB java is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with DAB java; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include	"devices_sdrplayDevice.h"
#include	"sdrplay-handler.h"
#include	"ringbuffer.h"
#include	"mirsdrapi-rsp.h"
#include	<complex>


/*
 * Class:     devices_sdrplayDevice
 * Method:    sdrplayInit
 * Signature: (IIZ)J
 */
JNIEXPORT jlong
	JNICALL Java_devices_sdrplayDevice_sdrplayInit (JNIEnv 	*env,
	                                                jobject	obj,
	                                                jint	freq,
	                                                jint	gain,
	                                                jboolean ag) {
	sdrplayHandler *handle = NULL;
        try {
           handle =  new sdrplayHandler (freq, 0, gain);
        } catch (int e) {}
        return reinterpret_cast <jlong>(handle);
}

/*
 * Class:     devices_sdrplayDevice
 * Method:    restartReader
 * Signature: (J)V
 */
JNIEXPORT void
	JNICALL Java_devices_sdrplayDevice_sdr_1restartReader (JNIEnv *env,
	                                                       jobject obj,
	                                                       jlong handle,
	                                                       jint freq) {
	if (handle == 0)
           return;
        sdrplayHandler *h = reinterpret_cast <sdrplayHandler *> (handle);
        h -> restartReader (freq);
}

/*
 * Class:     devices_sdrplayDevice
 * Method:    stopReader
 * Signature: (J)V
 */
JNIEXPORT void
	JNICALL Java_devices_sdrplayDevice_sdr_1stopReader (JNIEnv *env,
	                                                    jobject obj,
	                                                    jlong handle) {
	if (handle == 0)
	   return;
	sdrplayHandler *h = reinterpret_cast <sdrplayHandler *> (handle);
        h -> stopReader ();
}

/*
 * Class:     devices_sdrplayDevice
 * Method:    resetBuffer
 * Signature: (J)V
 */
JNIEXPORT void
	JNICALL Java_devices_sdrplayDevice_sdr_1resetBuffer
	       (JNIEnv * env, jobject obj, jlong handle) {
	if (handle == 0)
	   return;

	sdrplayHandler *h = reinterpret_cast <sdrplayHandler *> (handle);
        h -> resetBuffer ();
}

/*
 * Class:     devices_sdrplayDevice
 * Method:    samples
 * Signature: (J)I
 */
JNIEXPORT jint
	JNICALL Java_devices_sdrplayDevice_sdr_1samples
	            (JNIEnv *env, jobject obj, jlong handle) {
int amount;
	if (handle == 0)
	   return 0;
	sdrplayHandler *h = reinterpret_cast <sdrplayHandler *> (handle);
        return h -> Samples ();
}

/*
 * Class:     devices_sdrplayDevice
 * Method:    sdrplay_getSamples
 * Signature: (J[FI)I
 */

JNIEXPORT jint JNICALL Java_devices_sdrplayDevice_sdr_1getSamples
  (JNIEnv *env, jobject obj, jlong handle, jfloatArray samples, jint amount) {
        if (handle == 0)
           return 0;
	std::complex<float> temp [(int) amount];
	jfloat *body = env -> GetFloatArrayElements (samples, 0);
	sdrplayHandler * h = reinterpret_cast <sdrplayHandler *>(handle);
	int realAmount = h -> getSamples (temp, amount);
	int     i;
        for (i = 0; i < realAmount; i ++) {
           std::complex<float> t = temp [i];
           body [2 * i    ] = real (t);
           body [2 * i + 1] = imag (t);
        }
        env -> ReleaseFloatArrayElements (samples, body, 0);
        return realAmount;
}

/*
 * Class:     devices_sdrplayDevice
 * Method:    setGain
 * Signature: (JI)V
 */
JNIEXPORT void
	JNICALL Java_devices_sdrplayDevice_sdr_1setGain
	          (JNIEnv *env, jobject obj, jlong handle, jint gain){
	if (handle == 0)
	   return;
	sdrplayHandler *h = reinterpret_cast <sdrplayHandler *> (handle);
        h -> setGain (gain);
}

/*
 * Class:     devices_sdrplayDevice
 * Method:    autoGain
 * Signature: (JI)V
 */
JNIEXPORT void
	JNICALL Java_devices_sdrplayDevice_sdr_1autoGain
	          (JNIEnv *env, jobject obj, jlong handle, jboolean gain){
	if (handle == 0)
	   return;
	sdrplayHandler *h = reinterpret_cast <sdrplayHandler *> (handle);
        h -> set_autogain (gain);
}


