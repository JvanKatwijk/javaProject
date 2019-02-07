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
package package_Model;
import utils.*;


	public class	MP2_Processor extends AudioProcessor {
// the maximum size of a frame
	private final static int KJMP2_MAX_FRAME_SIZE = 1440;
// the number of samples per frame
	private final static int KJMP2_SAMPLES_PER_FRAME  = 1152;

	private final	byte [] MP2frame;
	private	final	int	MP2framesize;
        private		int	MP2Header_OK;
        private		int	MP2headerCount;
        private		int	MP2bitCount;
	private		int	baudRate;
	private	final	int	bitRate;
	private final	RadioModel	theGUI;
	private	final	SoundCard soundHandler;
	private final   Padhandler my_padHandler;
	private final	short [] sampleBuf;
	private		boolean	mp2Loaded;
	private         int	numberofFrames;
	private		int	errorFrames;
	private final	static int [] SAMPLE_RATES =
	{ 44100, 48000, 32000, 0,  // MPEG-1
	  22050, 24000, 16000, 0   // MPEG-2
	};

	private	final	long	handle;

	   public	MP2_Processor  (int bitRate,
	                                RadioModel theScreen,
	                                SoundCard soundHandler) {
	     
              this. bitRate     = bitRate;
              theGUI            = theScreen;
              this. soundHandler    = soundHandler;
	      baudRate		= 48000;        // default for DAB
              MP2framesize	= 24 * bitRate; // may be changed
              MP2frame		= new byte [2 * MP2framesize];
              MP2Header_OK	= 0;
              MP2headerCount	= 0;
              MP2bitCount	= 0;
              numberofFrames	= 0;
              errorFrames	= 0;
	      my_padHandler	= new Padhandler (theGUI);
	      sampleBuf 	= new short [KJMP2_SAMPLES_PER_FRAME * 2];
              try {
                 System. load  ("/usr/local/lib/libmp2-wrapper.so");
              } catch (Exception e) {
	         mp2Loaded = false;
                 handle     = 0;
	         return;
	      }

	      mp2Loaded		= true;
	      handle	= init_mp2lib ();
	}

//	bits to MP2 frames, amount is amount of bits

    /**
     *
     * @param v
     */
        @Override
           public void  addtoFrame      (byte [] v) {
              try {
	      int lf	   = baudRate == 48000 ? MP2framesize :
	                                         2 * MP2framesize;
	      int amount    = MP2framesize;
	      int vLength   = 24 * bitRate / 8;
	      byte [] help  = new byte [vLength];
	      if (!mp2Loaded)
	         return;
	      for (int i = 0; i < 24 * bitRate / 8; i ++) {
	         help [i] = 0;
	         for (int j = 0; j < 8; j ++) {
	            help [i] <<= 1;
	            help [i] |= v [8 * i + j] & 01;
	         }
	      }
	      { byte L0	= help [vLength - 1];
	        byte L1	= help [vLength - 2];
	        int  down = bitRate * 1000 >= 56000 ? 4 : 2;
	        my_padHandler. processPAD (help,
	                                   vLength - 2 - down - 1, L1, L0);
	      }

	      for (int i = 0; i < amount; i ++) {
	         switch (MP2Header_OK) {
	            case 2:
	               addbittoMP2 (MP2frame, v [i], MP2bitCount ++);
	               if (MP2bitCount >= lf) {
	                  if (mp2decodeFrame (handle, MP2frame, sampleBuf)) 
                             try {
	                        soundHandler. addData (sampleBuf, sampleBuf. length);
                             } catch (Exception e) {
                                System. out. println ("in soundhandler gaat het mis");
                             }
	                  else
	                     errorFrames ++;
	                  numberofFrames ++;
	                  if (numberofFrames > 25) {
	                     show_frameErrors (4 *
	                               (numberofFrames - errorFrames));
	                     numberofFrames	= 0;
	                     errorFrames	= 0;
	                  }

	                  MP2Header_OK	= 0;
	                  MP2headerCount	= 0;
	                  MP2bitCount	= 0;
	               }
	               break;

	            case 0:
//	apparently , we are not in sync yet
	               if (v [i] == 01) {
	                  if (++ MP2headerCount == 12) {
	                     MP2bitCount = 0;
	                     for (int j = 0; j < 12; j ++)
	                        addbittoMP2 (MP2frame, (byte)1, MP2bitCount ++);
	                     MP2Header_OK = 1;
	                  }
	               }
	               else
	                  MP2headerCount = 0;
	               break;
	            case 1:
	               addbittoMP2 (MP2frame, v [i], MP2bitCount ++);
	               if (MP2bitCount == 24) {
	                  setSamplerate (mp2sampleRate (MP2frame));
	                  MP2Header_OK = 2;
	               }
	         }
	      }
              } catch (Exception e) {
                  System. out. println (e. getMessage ());
              }
	   }

	   private void addbittoMP2 (byte [] v, byte b, int nm) {
	      byte my_byte	= v [nm / 8];
	      int  bitnr	= 7 - (nm & 7);
	      byte newbyte	= (byte)(01 << bitnr);

	      if (b == 0)
                 my_byte &= ~newbyte;
              else
                 my_byte |= newbyte;
              v [nm / 8] = my_byte;
	   }

	   private int mp2sampleRate  (byte [] frame) {

	      if (( frame[0]         != 0xFF)   // no valid syncword?
	      ||  ((frame[1] & 0xF6) != 0xF4)   // no MPEG-1/2 Audio Layer II?
	      ||  ((frame[2] - 0x10) >= 0xE0))  // invalid bitrate?
	         return 0;

 //	MPEG-1/2 switch
	      return SAMPLE_RATES [(((frame[1] & 0x08) >> 1) ^ 4)
                      + ((frame[2] >> 2) & 3)];         // actual rate
	   }

	   private void	setSamplerate (int rate) {
	      if (baudRate == rate)
	         return;
	      if ((rate != 48000) && (rate != 24000))
                 return;
	      baudRate = rate;
	   }

	   private void show_frameErrors (int er) {
	      final int frameErrors = er;
//	      try {
//	         javax. swing. SwingUtilities. invokeLater (new Runnable () {
//	            @Override
//	            public void run () {
//	                    theGUI. show_frameErrors (frameErrors);}}
//	         );
//	      } catch (Exception e){}
	   }

	   private native boolean mp2decodeFrame (long handle,
	                                          byte [] v, short[] audio);
	   private native long    init_mp2lib ();
}
