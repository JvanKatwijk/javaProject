
#include "airspy-handler.h"
#include "devices_airspyDevice.h"

/*
 * Class:     devices_airspyDevice
 * Method:    airspyInit
 * Signature: (IIZ)J
 */
JNIEXPORT jlong JNICALL Java_devices_airspyDevice_airspyInit
  (JNIEnv * env, jobject obj, jint frequency, jint gain, jboolean ag) {
	airspyHandler *handle = NULL;
	try {
	   handle =  new airspyHandler (frequency, 0, gain);
	} catch (int e) {}
	return reinterpret_cast <jlong>(handle);
}

/*
 * Class:     devices_airspyDevice
 * Method:    airspy_getSamples
 * Signature: (J[FI)I
 */
JNIEXPORT jint JNICALL Java_devices_airspyDevice_airspy_1getSamples
  (JNIEnv *env, jobject obj, jlong handle, jfloatArray samples, jint amount) {
	if (handle == 0)
	   return 0;
std::complex<float> temp [(int) amount];
jfloat *body = env -> GetFloatArrayElements (samples, 0);
airspyHandler * h = reinterpret_cast <airspyHandler *>(handle);
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
 * Class:     devices_airspyDevice
 * Method:    airspy_samples
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_devices_airspyDevice_airspy_1samples
  (JNIEnv * env, jobject obj, jlong handle) {
	if (handle == 0)
	   return 0;
	
	airspyHandler *h = reinterpret_cast <airspyHandler *> (handle);
	return h -> Samples ();
}

/*
 * Class:     devices_airspyDevice
 * Method:    airspy_resetBuffer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_airspyDevice_airspy_1resetBuffer
  (JNIEnv * env, jobject obj, jlong handle) {
	if (handle == 0)
	   return;

	airspyHandler *h = reinterpret_cast <airspyHandler *> (handle);
	h -> resetBuffer ();
}

/*
 * Class:     devices_airspyDevice
 * Method:    airspy_restartReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_airspyDevice_airspy_1restartReader
  (JNIEnv * env, jobject obj, jlong handle, jint freq) {
	if (handle == 0)
	   return;
	airspyHandler *h = reinterpret_cast <airspyHandler *> (handle);
	h -> restartReader (freq);
}

/*
 * Class:     devices_airspyDevice
 * Method:    airspy_stopReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_airspyDevice_airspy_1stopReader
  (JNIEnv *env, jobject obj, jlong handle) {
	if (handle == 0)
	   return;
	airspyHandler *h = reinterpret_cast <airspyHandler *> (handle);
        h -> stopReader ();
}


/*
 * Class:     devices_airspyDevice
 * Method:    airspy_setGain
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_devices_airspyDevice_airspy_1setGain
  (JNIEnv *env , jobject obj , jlong handle , jint gain) {
       if (handle == 0)
           return;
        airspyHandler *h = reinterpret_cast <airspyHandler *> (handle);
	h -> setGain (gain);
}


