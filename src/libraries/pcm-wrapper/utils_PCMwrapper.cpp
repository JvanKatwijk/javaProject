
#include	"utils_PCMwrapper.h"
#include	"pa-writer.h"

JNIEXPORT jlong JNICALL Java_utils_PCMwrapper_pcmwrapperInit
  (JNIEnv *, jobject) {
	paWriter *handle = new paWriter (0300);
	handle -> selectDefaultDevice ();
	return reinterpret_cast <jlong>(handle);
}

/*
 * Class:     utils_PCMwrapper
 * Method:    pcmwrapperStart
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_utils_PCMwrapper_pcmwrapperStart
  (JNIEnv *, jobject, jlong handle) {
	if (handle == 0)
	   return;
	paWriter *h = reinterpret_cast <paWriter *>(handle);
	h -> restartWriter ();
}

/*
 * Class:     utils_PCMwrapper
 * Method:    pcmwrapperStop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_utils_PCMwrapper_pcmwrapperStop
  (JNIEnv *, jobject, jlong handle) {
	if (handle == 0)
	   return;
	paWriter *h = reinterpret_cast<paWriter *>(handle);
	h -> stopWriter ();
	delete h;
}

/*
 * Class:     utils_PCMwrapper
 * Method:    pcmwrapperBuffer
 * Signature: ([SI)V
 */
JNIEXPORT void JNICALL Java_utils_PCMwrapper_pcmwrapperBuffer
  (JNIEnv *env, jobject obj, jlong handle, jshortArray arr, jint amount) {
int16_t *body = env -> GetShortArrayElements (arr, 0);

	if (handle == 0)
	   return;
	paWriter *h = reinterpret_cast<paWriter *>(handle);
	h -> putSamples (body, amount);
	env -> ReleaseShortArrayElements (arr, body, 0);
}

