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

#include	"utils_ViterbiHandler.h"
#include	"viterbi-768.h"

/*
 * Class:     utils_ViterbiHandler
 * Method:    deconvolve
 * Signature: (J[I[B)V
 */

JNIEXPORT void
	JNICALL Java_utils_ViterbiHandler_deconvolve (JNIEnv *env,
	                                                jobject obj,
	                                                jlong	handler,
	                                                jintArray v_in,
	                                                jbyteArray v_out) {
viterbi_768 *my_viterbiHandler =
	                  reinterpret_cast <viterbi_768 *> (handler);
jint	*in_vector	= env -> GetIntArrayElements (v_in, 0);
jbyte	*out_vector	= env -> GetByteArrayElements (v_out, 0);

	my_viterbiHandler	-> deconvolve ((int *)in_vector,
	                                       (char *)out_vector);
	env	-> ReleaseIntArrayElements  (v_in,  in_vector, 0);
	env	-> ReleaseByteArrayElements (v_out, out_vector, 0);
}

/*
 * Class:     viterbi_ViterbiHandler
 * Method:    initViterbi
 * Signature: (IZ)J
 */
JNIEXPORT jlong JNICALL Java_utils_ViterbiHandler_initViterbi
	          (JNIEnv * env, jobject obj, jint size, jboolean flag) {
viterbi_768 *my_viterbiHandler	= new viterbi_768 ((int)size, (bool)false);
	return reinterpret_cast <jlong> (my_viterbiHandler);
}


