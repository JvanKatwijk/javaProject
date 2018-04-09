/*
 *    Copyright (C) 2013 .. 2017
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

#include	"devices_wavFiles.h"
#include	"wavfiles.h"
/*
 * Class:     devices_wavFiles
 * Method:    fileOpen
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong
	JNICALL Java_devices_wavFiles_fileOpen (JNIEnv *env,
	                                        jobject	obj,
	                                        jstring	name) {
const char * _fileName = env -> GetStringUTFChars (name, 0);

	wavFiles *handle = NULL;
	try {
	   handle = new wavFiles (std::string (_fileName));
	} catch (int e) {}
        env -> ReleaseStringUTFChars (name, _fileName);
	return  reinterpret_cast<jlong>(handle);
}

/*
 * Class:     devices_wavFiles
 * Method:    wav_getSamples
 * Signature: (J[FI)I
 */
JNIEXPORT jint
	JNICALL Java_devices_wavFiles_wav_1getSamples (JNIEnv *env,
	                                               jobject obj,
	                                               jlong   handle,
	                                               jfloatArray arr,
	                                               jint amount) {
jfloat *body = env -> GetFloatArrayElements (arr, 0);
	if (handle == 0)
	   return 0;
	wavFiles *handler = reinterpret_cast<wavFiles *>(handle);

	int res = handler	-> getSamples ((float *)body, amount);
	env -> ReleaseFloatArrayElements (arr, body, 0);
        return res;
}

/*
 * Class:     devices_wavFiles
 * Method:    wav_samples
 * Signature: (J)I
 */
JNIEXPORT jint
	JNICALL Java_devices_wavFiles_wav_1samples (JNIEnv *env,
	                                            jobject obj,
	                                            jlong handle) {
wavFiles *handler	= reinterpret_cast<wavFiles *>(handle);
	return handler	-> Samples ();
}


/*
 * Class:     devices_wavFiles
 * Method:    wav_restartReader
 * Signature: (J)V
 */
JNIEXPORT void
	JNICALL Java_devices_wavFiles_wav_1restartReader (JNIEnv * env,
	                                                  jobject obj,
	                                                  jlong handle) {
wavFiles *handler	= reinterpret_cast<wavFiles *>(handle);
	fprintf (stderr, "pas op, handler = %X (%X)\n",
	                           reinterpret_cast<int64_t>(handler), 
	                                     (int64_t)handle);
	handler	-> restartReader ();
}

/*
 * Class:     devices_wavFiles
 * Method:    wav_stopReader
 * Signature: (J)V
 */
JNIEXPORT void
	JNICALL Java_devices_wavFiles_wav_1stopReader (JNIEnv * env,
	                                               jobject obj,
	                                               jlong handle) {
wavFiles *handler       = reinterpret_cast<wavFiles *>(handle);
        handler -> stopReader ();
}


