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
import	devices. Device;

public class Reader {
	final   private	Device	my_Device;
	private int     inputRate 	= 2048000;
	private	int	currentPhase	= 0;
	private	float	sLevel		= 0.0f;
	private	int	bufferContent	= 0;
	private	Boolean	running		= true;
	final   private	float []	oscillatorTable;
        int sampleCount                 = 0;
	final   private	RadioModel	 theGUI;

	public Reader (Device theDevice, int rate, RadioModel view ) {
	   my_Device	= theDevice;
           inputRate    = rate;
	   theGUI	= view;
           oscillatorTable      = new float [2 * rate];
	   for (int i = 0; i < rate; i ++) {
              oscillatorTable [2 * i] =
	                        (float) (Math. cos (2.0 * Math.PI * (float)i / (float)rate));
              oscillatorTable [2 * i + 1] =
	                        (float) (Math. sin (2.0 * Math.PI * (float)i / (float)rate));
	   }
	}

	public	void	setRunning (Boolean b) {
	   running = b;
	}

	public	Float	get_sLevel	() {
	   return sLevel;
	}

//
//	Note that "samples" are here complex values, stored in
//	two subsequent floats in the array
	public	int	getSamples (float [] v,
                                    float freqOffset) throws Exception {
	   int n	= v. length / 2;

//	bufferContent is an indicator for the value of ... -> Samples ()
	   if (bufferContent < n) {
	      bufferContent = my_Device. samples ();
	      while ((bufferContent < n) && running) {
                 try {
                    Thread. sleep (10);
                 } catch (InterruptedException e) {
                 }
	         bufferContent = my_Device. samples ();
	      }
	   }
	   n = my_Device. getSamples (v, n);
	   if (!running) {
	      throw new Exception ();
	   }
	   bufferContent -= n;

	   for (int i = 0; i < n; i ++) {
	      currentPhase -= (int)(freqOffset);
//	Note that "phase" itself might be negative;
	      currentPhase = (currentPhase + inputRate) % inputRate;

	      Float	re_s	= v [2 * i    ];
	      Float	im_s	= v [2 * i + 1];
	      Float	re_o	= oscillatorTable [2 * currentPhase    ];
	      Float	im_o	= oscillatorTable [2 * currentPhase + 1];
	      Float	re_new	= re_s * re_o - im_s * im_o;
	      Float	im_new	= re_s * im_o + re_o * im_s;
	      v [2 * i    ]	= re_new;
	      v [2 * i + 1]	= im_new;

	      Float	absValue = (re_new < 0 ? - re_new : re_new) +
	                           (im_new < 0 ? - im_new : im_new);
	      sLevel       = (float)(0.00001 * absValue + (1.0 - 0.00001) * sLevel);
           }

	   sampleCount += n;
	   if (sampleCount > 2048000 / 4) {
//	      System. out. println (sampleCount);
//	      final int offset = (int)freqOffset;
//	      try {
//	         javax. swing. SwingUtilities. invokeLater (new Runnable () {
//	            @Override
//	            public void run () {theGUI. setFreqOffset (offset);}});
//	      }
//	      catch (Exception e) {}
	      sampleCount = 0;
	   }

	   return n;
        }
}
