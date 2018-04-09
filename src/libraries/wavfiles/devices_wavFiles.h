/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class devices_wavFiles */

#ifndef _Included_devices_wavFiles
#define _Included_devices_wavFiles
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     devices_wavFiles
 * Method:    fileOpen
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_devices_wavFiles_fileOpen
  (JNIEnv *, jobject, jstring);

/*
 * Class:     devices_wavFiles
 * Method:    wav_getSamples
 * Signature: (J[FI)I
 */
JNIEXPORT jint JNICALL Java_devices_wavFiles_wav_1getSamples
  (JNIEnv *, jobject, jlong, jfloatArray, jint);

/*
 * Class:     devices_wavFiles
 * Method:    wav_samples
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_devices_wavFiles_wav_1samples
  (JNIEnv *, jobject, jlong);

/*
 * Class:     devices_wavFiles
 * Method:    wav_restartReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_wavFiles_wav_1restartReader
  (JNIEnv *, jobject, jlong);

/*
 * Class:     devices_wavFiles
 * Method:    wav_stopReader
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_devices_wavFiles_wav_1stopReader
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
