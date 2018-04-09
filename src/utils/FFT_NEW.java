/*
 *    Copyright (C) 2009 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of javaDab
 *
 *    javaDab is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    javaDab is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with java-DAB; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 ***************************************************************
  * fft.c
  * Douglas L. Jones 
  * University of Illinois at Urbana-Champaign 
  * January 19, 1992 
  * http://cnx.rice.edu/content/m12016/latest/
  * 
  *   fft: in-place radix-2 DIT DFT of a complex input 
  * 
  *  input:
  * n: length of FFT: must be a power of two
  * m: n = 2**m
  *   input/output
  * x: float array of length n with real part of data
  * y: float array of length n with imag part of data
  *
  *   Permission to copy and use this program is granted
  *   as long as this header is included.

  ****************************************************************
  *
  *	A minor modification was made for use in javaDab.
  *	the complex numbers  are stored interleaved in a
  *	single input/ output vector.
  *	So, we use x [2 * i] to address a "real" element of the
  *	complex number and x [2 * i + 1] to address an "imag" element
  */
package utils;

public class FFT_NEW {
	  private final int n, m;
//	Lookup tables.  Only need to recompute when size of FFT changes.
	  private final float [] cos;
	  private final float [] sin;

	  public FFT_NEW (int n) {

	    this. n = n;
	    this. m = (int)(Math.log(n) / Math.log(2));
	
	    // Make sure n is a power of 2
	    if (n != (1 << m))
	      throw new RuntimeException("FFT length must be power of 2");
	
	    // precompute tables
	    cos = new float [n / 2];
	    sin = new float [n / 2];
	
	    for (int i = 0; i < n / 2; i++) {
	      cos [i] = (float)(Math. cos (-2 * Math. PI * i / n));
	      sin [i] = (float)(Math. sin (-2 * Math. PI * i / n));
	    }
	  }
	
	   public void fft (float [] x) {
	      int i,j,k,n1,n2,a;
	      float c,s,e,t1,t2;
	  
//	Bit-reverse
	      j = 0;
	      n2 = n / 2;
	      for (i = 1; i < n - 1; i++) {
	         n1 = n2;
	         while (j >= n1 ) {
	            j = j - n1;
	            n1 = n1/2;
	         }
	         j = j + n1;
	    
	         if (i < j) {
	            t1		= x[2 * i];
	            x [2 * i]	= x[2 * j];
	            x [2 * j]	= t1;

	            t1		= x[2 * i + 1];
	            x [2 * i + 1]	= x[2 * j + 1];
	            x [2 * j + 1]	= t1;
	         }
	      }
	    // FFT
	      n1 = 0;
	      n2 = 1;
	  
	      for (i = 0; i < m; i++) {
	         n1 = n2;
	         n2 = n2 + n2;
	         a = 0;
	    
	         for (j = 0; j < n1; j++) {
	            c = cos [a];
	            s = sin [a];
	            a +=  1 << (m-i-1);
	
	            for (k = j; k < n; k = k + n2) {
	               int k2_x	= 2 * k;
	               int k2_y	= 2 * k + 1;
	               int kn1_x = 2 * (k + n1);
	               int kn1_y = 2 * (k + n1) + 1;

	               t1 = c * x[kn1_x] - s * x[kn1_y];
	               t2 = s * x[kn1_x] + c * x[kn1_y];
	               x [kn1_x]		= x [k2_x] - t1;
	               x [kn1_y]		= x [k2_y] - t2;
	               x [k2_x]			= x [k2_x] + t1;
	               x [k2_y]			= x [k2_y] + t2;
//	               t1 = c * x[2 * (k + n1)] - s * x[2 * (k + n1) + 1];
//	               t2 = s * x[2 * (k + n1)] + c * x[2 * (k + n1) + 1];
//	               x [2 * (k + n1)]      = x [2 * k] - t1;
//	               x [2 * (k + n1) + 1]  = x [2 * k + 1] - t2;
//	               x [2 * k]             = x [2 * k] + t1;
//	               x [2 * k + 1]         = x [2 * k + 1] + t2;
	            }
	         }
	      }
	   }
//
//	we use IFFT only in the phasereference module, we do not
//	need the scaling there, so we keep it simple
	   public void ifft (float [] vector) {
	      for (int i = 0; i < n; i ++)
	         vector [2 * i + 1] = - vector [2 * i + 1];
	      fft (vector);
	      for (int i = 0; i < n; i ++)
	         vector [2 * i + 1] = - vector [2 * i + 1];
	   }
}
