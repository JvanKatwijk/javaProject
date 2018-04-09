/*
 *    Copyright (C) 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of javaDab
 *
 *    javaDab is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation as version 2 of the License.
 *
 *    javaDab is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with javaDab; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place
 */

package utils;

	public class LFfilter {

	   private final float [] buffer;
	   private final float [] kernel;
	   private	 int	ip;
	   private final int	firSize;

	private final float M_PI	= 3.14159265358979323846f;  /* pi */

        public LFfilter (int firSize,
                         int cutoffFreq,
                         int sampleRate) {
	   this. firSize	= firSize;
	   float []   tmp	= new float [firSize];
	   final float lo	= (float)(cutoffFreq) / (float)sampleRate;
	   float   sum     = 0.0f;

	   buffer	= new float [firSize];
	   kernel	= new float [firSize];
	   ip		= 0;
//      create a low pass filter for lo;
           for (int i = 0; i < firSize; i ++) {
              if (i == firSize / 2)
                 tmp [i] = 2 * M_PI * lo;
              else
                 tmp [i] = (float)Math. sin (2 * M_PI * lo * (i - firSize /2)) / (i - firSize/2);
//
//      windowing with Blackman
              tmp [i]  *= (0.42 -
                    0.5 * Math. cos (2 * M_PI * (float)i / (float)firSize) +
                    0.08 * Math.cos (4 * M_PI * (float)i / (float)firSize));

              sum += tmp [i];
           }
//
//      and the kernel
           for (int i = 0; i < firSize; i ++)
              kernel [i] = tmp [i] / sum;
}

//      we process the samples backwards rather than reversing
//      the kernel
	public float Pass (float v) {
	   float   tmp	= 0;

	   buffer  [ip]     = v;
	   for (int i = 0; i < firSize; i ++) {
              int index = ip - i;
              if (index < 0)
                 index += firSize;
              tmp += buffer [index] * kernel [i];
	   }

	   ip = (ip + 1) % firSize;
	   return tmp;
	}
}
