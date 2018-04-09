#
/*
 *    Copyright (C) 2009 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of the Qt-DAB
 *
 *    Qt-DAB is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    Qt-DAB is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Qt-DAB; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
#include	"utils_fftHandler.h"
#include	<stdint.h>
#include	<cstring>
#include	<fftw3.h>

class	fftHandler {
private:
	int	fftSize;
	fftwf_plan	plan;
	fftwf_complex	*vector;

public:
	void	do_FFT (float *v) {
int	i;
	for (i = 0; i < fftSize; i ++) {
	   vector [i] [0] = v [2 * i];
	   vector [i] [1] = v [2 * i + 1];
	}
	fftwf_execute (plan);
	for (i = 0; i < fftSize; i ++) {
	   v [2 * i    ] = vector [i] [0];
	   v [2 * i + 1] = vector [i] [1];
	}
}

	void	do_IFFT (float *v) {
int	i;
	for (i = 0; i < fftSize; i ++) {
	   vector [i] [0] = v [2 * i];
	   vector [i] [1] = - v [2 * i + 1];
	}
	fftwf_execute (plan);
	for (i = 0; i < fftSize; i ++) {
	   v [2 * i    ] = vector [i] [0];
	   v [2 * i + 1] = - vector [i] [1];
	}
}

private:
	void	test_fft	(void) {
	int i;
	float temp [2 * fftSize];
	memset (temp, 0, 2 * fftSize * sizeof (float));
	temp [2] = 1000; temp[3] = 1000;
	do_FFT (temp);
	do_IFFT (temp);
	for (i = 0; i < 20; i ++)
	   fprintf (stderr, "%f %f\n", temp [2 * i], temp [2 * i + 1]);
}

public:
	fftHandler (int size) {
	fftSize		= (int)size;
	vector		= (fftwf_complex *)
	                   fftwf_malloc (sizeof (fftwf_complex) * fftSize);
	plan		= fftwf_plan_dft_1d (fftSize,
	                             reinterpret_cast <fftwf_complex *>(vector),
	                             reinterpret_cast <fftwf_complex *>(vector),
	                             FFTW_FORWARD, FFTW_ESTIMATE);
//	test_fft ();
}

	~fftHandler	(void) {
	fftwf_destroy_plan	(plan);
	fftwf_free		(vector);
}


};

/*
 * Class:     utils_fftHandler
 * Method:    fftWrapper
 * Signature: (I)J
 */
JNIEXPORT jlong
	JNICALL
	Java_utils_fftHandler_fftWrapper (JNIEnv *env,
	                                        jobject ob, jint size) {
fftHandler *handler	= new fftHandler ((int)size);
	return reinterpret_cast<jlong>(handler);
}

/*
 * Class:     utils_fftHandler
 * Method:    do_FFT
 * Signature: ([Ljava/lang/Float;I)V
 */
JNIEXPORT void
JNICALL Java_utils_fftHandler_do_1FFT (JNIEnv	*env,
	                                     jobject	obj,
	                                     jlong	handle,
	                                     jfloatArray arr,
	                                     jint	direction) {
int	i;
jfloat *body	= env -> GetFloatArrayElements (arr, 0);
fftHandler	* handler = reinterpret_cast<fftHandler *>(handle);

	if (direction <= 0) 
	   handler	-> do_IFFT (body);
	else
	   handler	-> do_FFT  (body);
	env -> ReleaseFloatArrayElements (arr, body, 0);
}

