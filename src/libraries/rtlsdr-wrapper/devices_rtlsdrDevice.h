/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class devices_rtlsdrDevice */

#ifndef _Included_devices_rtlsdrDevice
#define _Included_devices_rtlsdrDevice
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdrInit
 * Signature: (IIZ)J
 */
JNIEXPORT jlong JNICALL Java_devices_rtlsdrDevice_rtlsdrInit
  (JNIEnv *, jobject, jint, jint, jboolean);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_getSamples
 * Signature: (J[FI)I
 */
JNIEXPORT jint JNICALL Java_devices_rtlsdrDevice_rtlsdr_1getSamples
  (JNIEnv *, jobject, jlong, jfloatArray, jint);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_samples
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_devices_rtlsdrDevice_rtlsdr_1samples
  (JNIEnv *, jobject, jlong);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_resetBuffer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1resetBuffer
  (JNIEnv *, jobject, jlong);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_restartReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1restartReader
  (JNIEnv *, jobject, jlong);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_stopReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1stopReader
  (JNIEnv *, jobject, jlong);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_setVFOFrequency
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1setVFOFrequency
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_getVFOFrequency
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_devices_rtlsdrDevice_rtlsdr_1getVFOFrequency
  (JNIEnv *, jobject, jlong);

/*
 * Class:     devices_rtlsdrDevice
 * Method:    rtlsdr_setGain
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_devices_rtlsdrDevice_rtlsdr_1setGain
  (JNIEnv *, jobject, jlong, jint);

#ifdef __cplusplus
}
#endif
#endif
