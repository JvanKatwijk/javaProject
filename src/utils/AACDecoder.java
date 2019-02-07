/*
 *    Copyright (C) 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Programming
 *
 *    This file is part of java DAB
 *    java DAB is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    java DAB is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with java DAB; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package utils;

public class AACDecoder {
	private	      boolean 		aacInitialized;
	private final int []		info;
	private final short []		localBuffer;

	private	final	LFfilter	f_32000_r;
	private	final	LFfilter	f_32000_i;
	private final	SoundCard	soundHandler;

	public AACDecoder (SoundCard b) {
	   soundHandler	= b;
	   info		= new int [10];
	   localBuffer	= new short [1024];
	   System. out. println ("going to load faad handler");
	   try {
	      System. load  ("/usr/local/lib/libfaad-wrapper.so");
	      System. out. println ("faad loaded");
	   } catch (Error | Exception e) {
	      System. out. println (e. getMessage ());
	      System. exit (1);
	   }
	   aacReset ();
	   f_32000_r	= new LFfilter (5, 24000, 96000);
	   f_32000_i	= new LFfilter (5, 24000, 96000);
	}

	private int phase	= 0;

	public int  MP42PCM (int	dacRate,
	                     int	sbrFlag,
	                     int	mpegSurround,
	                     int	aacChannelMode,
	                     byte []	theAudioUnit,
	                     int	startAddress,
	                     int	frame_length) {
//	hier roepen we de library aan
	      if (!aacInitialized) {
	          if (!aacInitialize (dacRate, sbrFlag,
	                                     mpegSurround, aacChannelMode))
	            return 0;
	         aacInitialized = true;
	      }
//  note that the aacDecoder (a native function) stores its output in a local
//  circular buffer
	      aacDecode (theAudioUnit, startAddress, frame_length, info);
	      int error		= info [0];
	      int amount	= info [1];	// in samples
	      int rate		= info [2];
//              System. out. println ("error " + error + " amount " + amount + " rate" + rate);
              
	      if ((amount <= 0) || (error != 0))	// may be an error
	         return 0;

//	the aacwrapper/decoder puts the values in a ringbuffer
//	the fetch function fetches at most localbuffersize values
	      int samplesRead = aacFetchData (localBuffer);
	      while (samplesRead > 0) {
	         convert_and_store (localBuffer, samplesRead, rate);
	         samplesRead = aacFetchData (localBuffer);
	      }
	      return info [1];           
	   }
//
//	These will be interfaces to C
//
	private native void	aacReset ();
	private native boolean aacInitialize (int dacRate,
	                                      int sbrFlag,
	                                      int mpegSurround,
	                                      int aacChannelMode);

	private native int aacDecode (byte [] theAudioUnit,
	                              int startAddress, int frameLength,
	                                           int [] info);

        private native int aacFetchData (short [] buffer);
//
//
//	Confusing, but recall that size gives the number of shorts
	private void	convert_and_store (short [] buffer,
	                                   int size, int rate) {
	   switch (rate) {
	      case 24000:
	         convert_and_store_24000 (buffer, size);
	         break;
	      case 32000:
	         convert_and_store_32000 (buffer, size);
	         break;
	      default:
	      case 48000:
                 try {
	            addData (buffer, size);
                 } catch (Exception e) {
                    System. out. println (e. getMessage ());
                 }
	         break;
	   }
	}
//
//	May be we need an additional filter here
	private void	convert_and_store_24000 (short [] buffer, int size) {
	   short [] lbuffer = new short [4 * size];
	   for (int i = 0; i < size / 2; i ++) {
	      lbuffer [4 * i    ] = buffer [2 * i];
	      lbuffer [4 * i + 1] = buffer [2 * i + 1];
	      lbuffer [4 * i + 2] = 0;
	      lbuffer [4 * i + 3] = 0;
	   }
	   try {
	      addData (lbuffer, 2 * size);
	   } catch (Exception e) {}
	}

	private	void	convert_and_store_32000 (short [] buffer, int size) {
	   short [] lbuffer = new short [6 * size];
	   for (int i = 0; i < size / 2; i ++) {
	      lbuffer [6 * i    ] = buffer [2 * i];
	      lbuffer [6 * i + 1] = buffer [2 * i + 1];
	      lbuffer [6 * i + 2] = 0;
	      lbuffer [6 * i + 3] = 0;
	      lbuffer [6 * i + 4] = 0;
	      lbuffer [6 * i + 5] = 0;
	   }
	   for (int i = 0; i < (int)(1.5f * size / 2); i ++) {
	      lbuffer [2 * i    ] = (short)f_32000_r. Pass (lbuffer [4 * i    ]);
	      lbuffer [2 * i + 1] = (short)f_32000_i. Pass (lbuffer [4 * i + 1]);
	   }
	  
	   try {
	      addData (lbuffer, (int)(1.5 * size));
	   } catch (Exception e) {
              System. out. println (e. getMessage ());
           }
	}

	public	void	addData (short [] buffer, int size) {
	   soundHandler. addData (buffer, size);
	}
}


