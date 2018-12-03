/*
 *    Copyright (C) 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of the DAB-java program
 *    DAB-java is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    DAB-java is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with DAB-java; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package package_Model;
import utils.*;
/**
 *
 * @author jan
 */
public class PhaseReference {
	private		int	threshold	= 3;		// default
	private		int	diff_length	= 35;		// default
	private final	DabParams	my_params;
	private final	float []	phaseDifferences;
	private	final	FFT_NEW		my_fftHandler;	
//	private	final	fftHandler	my_fftHandler;	
	private final	float []	refTable;
	private final	PhaseTable      my_phaseTable;
	private final	float []	workVector;

	public PhaseReference (int mode, int t, int d) { 
	   Double Phi_k;
           my_params            = new DabParams (mode);
	   my_fftHandler	= new FFT_NEW (my_params. get_T_u ());
//	   my_fftHandler	= new fftHandler (mode);
           int t_u              = my_params. get_T_u ();
	   threshold		= t;
	   diff_length		= d;
	   phaseDifferences 	= new float [diff_length];
	   workVector		= new float [2 * t_u];
           refTable		= new float [2 * t_u];

           my_phaseTable        = new PhaseTable ();
	   refTable [0] = 0;
	   refTable [1] = 0;
           for (int i = 1; i <= my_params. get_carriers () / 2; i ++) {
              Phi_k =  my_phaseTable. get_Phi (mode, i);
              refTable [2 * i    ]		= (float)(Math. cos (Phi_k));
              refTable [2 * i + 1]		= (float)(Math. sin (Phi_k));
              Phi_k = my_phaseTable. get_Phi (mode, -i);
              refTable [2 * t_u - 2 * i]	= (float)(Math. cos (Phi_k));
	      refTable [2 * t_u - 2 * i + 1]	= (float)(Math. sin (Phi_k));
           }

//      prepare a table for the coarse frequency synchronization
//      could be a static one
//	phaseDifferences [i] = arg (refTable [i] * conj (refTable [i + 1]))
	   for (int i = 1; i <= diff_length; i ++) {
	      int base		=   t_u + i;
	      float re_1	=   refTable [(2 * (base)    ) % (2 * t_u)];
	      float im_1	=   refTable [(2 * (base) + 1) % (2 * t_u)];
	      float re_2	=   refTable [(2 * (base + 1)    ) % (2 * t_u)];
	      float im_2	= - refTable [(2 * (base + 1) + 1) % (2 * t_u)];
	      float re_new	=   re_1 * re_2 - im_1 * im_2;
	      float im_new	=   re_1 * im_2 + re_2 * im_1;
              phaseDifferences [i - 1] =
	                     Math. abs ((float)(Math. atan2 (im_new, re_new)));	
	   }
	}

	public int findIndex (float [] v) {
final	   int  t_u	= my_params. get_T_u ();
final	   int  t_g	= my_params. get_T_g ();
	   int		maxIndex	= -1;
	   float	sum	= 0.0f;
	   float	Max	= 0.0f;
	
           System. arraycopy (v, 0, workVector, 0, 2 * t_u);
	   my_fftHandler. fft (workVector);
//	   my_fftHandler. do_FFT (workVector, 1);
//	   workVector [i] *= conj (refTable [i]);
           for (int i = 0; i < t_u; i ++) {
	      float re_1		=   workVector [2 * i    ];
	      float im_1		=   workVector [2 * i + 1];
	      float re_2		=   refTable   [2 * i    ];
	      float im_2		= - refTable   [2 * i + 1];
	      workVector [2 * i    ]	=   re_1 * re_2 - im_1 * im_2;
	      workVector [2 * i + 1]	=   re_1 * im_2 + re_2 * im_1;
	   }
           my_fftHandler. ifft (workVector);
//	   my_fftHandler. do_FFT (workVector, -1);

//
//	compute the "average" signal value
	   for (int i = 0; i < t_u; i ++) {
	      float re	= workVector [2 * i    ];
	      float im	= workVector [2 * i + 1];
	      float absValue  = (re < 0 ? -re : re) +
	                        (im < 0 ? -im : im);
	      sum += absValue;
	      if ((i < 2 * t_g) && (absValue > Max)) {
	         maxIndex = i;
	         Max      = absValue;
	      }
	   }
	   if (Max < threshold * sum / t_u)
	      return - (int)(Math. abs (Max * t_u / sum) - 1);
	   else
	      return maxIndex;
        }
//
//	We heave to compute and recompute a sum of diff_length
//	values. We therefore recompute the terms of the sum
//	in a vector computedDiffs
//
	private final int	SEARCH_RANGE	 = 2 * 40;

	int	estimate_CarrierOffset (float [] v) {
	int	index	= 100;
//	int	index_1	= 100;
	int	t_u	= my_params. get_T_u ();

           System. arraycopy (v, 0, workVector, 0, v. length);
           my_fftHandler. fft (workVector);
//           my_fftHandler. do_FFT (workVector, 1);
	   float	diff;
	   float	Mmin = 10000.0f;
//
//	believe it or not, we are computing
//	workVector [(t_u + i + j + 1) % t_u] *
//                  conj (workVector [(t_u + i + j + 2) % t_u])
//	in order to get the minimum difference or the args with the
//	predefined phaseDifferences
	   float [] computedDiffs = new float [SEARCH_RANGE + diff_length];
	   for (int i = t_u - SEARCH_RANGE / 2;
	                    i < t_u + SEARCH_RANGE / 2 + diff_length; i ++) {
	      float re_1 =   workVector [(2 * i    ) % (2 * t_u)];
	      float im_1 =   workVector [(2 * i + 1) % (2 * t_u)];
	      float re_2 =   workVector [(2 * i + 2) % (2 * t_u)];
	      float im_2 = - workVector [(2 * i + 3) % (2 * t_u)];
	      float re_new = re_1 * re_2 - im_1 * im_2;
	      float im_new = re_1 * im_2 + re_2 * im_1;
	      computedDiffs [i - (t_u - SEARCH_RANGE / 2)] =
	                            Math. abs ((float)(Math. atan2 (im_new, re_new)));
	   }

	   for (int i = t_u - SEARCH_RANGE / 2;
	            i < t_u + SEARCH_RANGE / 2; i ++) {
	      float sum = 0;
	      for (int j = 1; j <= diff_length; j ++)
	         if (Math. abs (phaseDifferences [j - 1]) < 0.05)
	            sum += computedDiffs [i - (t_u - SEARCH_RANGE / 2) + j];
	      if (sum < Mmin) {
	         Mmin = sum;
	         index = i;
	      }
	   }

//	   Mmin	= 10000.0f;
//	   for (int i = t_u - SEARCH_RANGE / 2;
//	                    i < t_u  + SEARCH_RANGE / 2; i ++) {
//	      float sum	= 0;
//	      for (int j = 0; j < diff_length; j ++) {
//	         if (Math. abs (phaseDifferences [j]) < 0.01) {
//	            int	ind1	= (2 * (i + j + 1)    ) % (2 * t_u);
//	            int	ind2	= (2 * (i + j + 1) + 1) % (2 * t_u);
//	            int	ind3	= (2 * (i + j + 2)    ) % (2 * t_u);
//	            int	ind4	= (2 * (i + j + 2) + 1) % (2 * t_u);
//	            float	re_1	=   workVector [ind1];
//	            float	im_1	=   workVector [ind2];
//	            float	re_2	=   workVector [ind3];
//	            float	im_2	= - workVector [ind4];
//	            float	re_new	= re_1 * re_2 - im_1 * im_2;
//	            float	im_new	= re_1 * im_2 + re_2 * im_1; 
//	            sum += Math. abs (Math. atan2 (im_new, re_new));
//	         }
//	      }
//
//	      if (sum < Mmin) {
//	         Mmin = sum;
//	         index_1 = i;
//	      }
//	   }
//	   if (index != index_1)
//	      System. out. println ("Differences: index = " + index + " index_1 = " + index_1);
	   return index == 100 ? 0 : index - t_u; 
	}
}
