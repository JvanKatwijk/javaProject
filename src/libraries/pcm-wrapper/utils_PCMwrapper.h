/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class utils_PCMwrapper */

#ifndef _Included_utils_PCMwrapper
#define _Included_utils_PCMwrapper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     utils_PCMwrapper
 * Method:    pcmwrapperInit
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_utils_PCMwrapper_pcmwrapperInit
  (JNIEnv *, jobject);

/*
 * Class:     utils_PCMwrapper
 * Method:    pcmwrapperStop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_utils_PCMwrapper_pcmwrapperStop
  (JNIEnv *, jobject, jlong);

/*
 * Class:     utils_PCMwrapper
 * Method:    pcmwrapperStart
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_utils_PCMwrapper_pcmwrapperStart
  (JNIEnv *, jobject, jlong);

/*
 * Class:     utils_PCMwrapper
 * Method:    pcmwrapperBuffer
 * Signature: (J[SI)V
 */
JNIEXPORT void JNICALL Java_utils_PCMwrapper_pcmwrapperBuffer
  (JNIEnv *, jobject, jlong, jshortArray, jint);

#ifdef __cplusplus
}
#endif
#endif
