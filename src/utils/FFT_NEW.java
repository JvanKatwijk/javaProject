/*
 * fft.c
 * Douglas L. Jones 
 * University of Illinois at Urbana-Champaign 
 * January 19, 1992 
 * http://cnx.rice.edu/content/m12016/latest/
 * 
 *   fft: in-place radix-2 DIT DFT of a complex input 
 * 
 *   input: 
 * n: length of FFT: must be a power of two 
 * m: n = 2**m 
 *   input/output 
 * x: double array of length n with real part of data 
 * y: double array of length n with imag part of data 
 * 
 *   Permission to copy and use this program is granted 
 *   as long as this header is included. 
 */

package utils;
public class FFT_NEW {

int n, m;
// Lookup tables.  Only need to recompute when size of FFT changes.
	float [] cosTable;
	float [] sinTable;

	public FFT_NEW (int n) {
	   this. n = n;
	   this. m = (int)(Math.log(n) / Math.log(2));

	// precompute tables
	   cosTable = new float [n / 2];
	   sinTable = new float [n / 2];

	   for (int i = 0; i < n / 2; i++) {
	      cosTable [i] = (float)(Math.cos(-2*Math.PI*i/n));
	      sinTable [i] = (float)(Math.sin(-2*Math.PI*i/n));
	   }
	}

	public void fft (float [] x) {
int i,j,k,n1,n2,a;
float c,s,e,t1,t2;

//	Bit-reverse
	   j = 0;
	   n2 = n / 2;
	   for (i=1; i < n - 1; i++) {
	      n1 = n2;
	      while (j >= n1) {
	         j = j - n1;
	         n1 = n1/2;
	      }

	      j = j + n1;
     
	      if (i < j) {
	         t1 = x [2 * i + 0];
	         x [2 * i + 0] = x [2 * j + 0];
	         x [2 * j + 0] = t1;
	         t1 = x [2 * i + 1];
	         x [2 * i + 1] =  x[2 * j + 1];
	         x [2 * j + 1] = t1;
	      }
	   }
//	FFT
	   n1 = 0;
	   n2 = 1;
   
	   for (i = 0; i < m; i++) {
	      n1 = n2;
	      n2 = n2 + n2;
	      a = 0;
    
	      for (j = 0; j < n1; j++) {
	         c = cosTable [a];
	         s = sinTable [a];
	         a +=  1 << (m-i-1);
 
	         for (k = j; k < n; k = k + n2) {
	            t1 = c * x [2 * (k + n1)] - s * x [2 * (k + n1) + 1];
	            t2 = s * x [2 * (k + n1)] + c * x [2 * (k + n1) + 1];
	            x [2 * (k + n1)] = x [2 * k] - t1;
	            x [2 * (k + n1) + 1] = x [2 * k + 1] - t2;
	            x [2 * k] = x [2 * k] + t1;
	            x [2 * k + 1] = x [2 * k + 1] + t2;
	         }
	      }
	   }
	}
//      we use IFFT only in the phasereference module, we do not
//      need the scaling there, so we keep it simple
           public void ifft (float [] vector) {
              for (int i = 0; i < n; i ++)
                 vector [2 * i + 1] = - vector [2 * i + 1];
              fft (vector);
              for (int i = 0; i < n; i ++)
                 vector [2 * i + 1] = - vector [2 * i + 1];
           }

}                          
