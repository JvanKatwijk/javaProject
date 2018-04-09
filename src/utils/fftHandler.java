
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
package utils;
import package_Model.*;
//	Do not forget that complexes are are virtual, and implemented
//	as pairs (2 * i, 2 * i + 1) of floats
public class	fftHandler {
	long handle;
	private	native	long fftWrapper	(int mode);
	public		void do_FFT	(float [] v, int direction) {
           do_FFT (handle, v, direction);
        }
	private	native	void do_FFT	(long handle,
	                                 float [] v, int direction);
        
	public fftHandler (int mode) {
	   DabParams my_dabParams	= new DabParams (mode);
           try {
              System. load  ("/usr/local/lib/libfft-wrapper.so");
           } catch (Exception e) {}
           handle	= fftWrapper (my_dabParams. get_T_u ());
        }
}

