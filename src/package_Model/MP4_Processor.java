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
import	utils.*;

	public class	MP4_Processor extends AudioProcessor {
	private final int	superFramesize;
	private final int	bitRate;
	private	final int	rsDims;
	private final int  []	au_start;
	private final byte []	frameBytes;
	private final byte []	outVector;
	private final byte []	rsIn;
	private	final byte []	rsOut;
	private final SoundCard	soundHandler;
        
	private final Padhandler my_padHandler;
	private final firecodeChecker fc;
	private final ReedSolomon	my_rsDecoder;
        private final RadioModel   theGUI;
	private	      int	blockFillIndex  = 0;
        private	      int	blocksInBuffer  = 0;
        private       int	frameCount      = 0;
        private       int	frameErrors     = 0;
        private       int	aacErrors       = 0;
        private       int	aacFrames       = 0;
        private       int	rsFrames        = 0;
        private       int	rsErrors        = 0;
	private final AACDecoder  my_aacDecoder;

	   public	MP4_Processor (int bitRate,
	                               RadioModel theScreen,
	                               SoundCard rb) {
	      this. bitRate	= bitRate;
              this. theGUI      = theScreen;
	      soundHandler	= rb;
	      superFramesize 	= 110 * (bitRate / 8);
	      rsDims		= bitRate / 8;
	      fc		= new firecodeChecker ();
	      my_rsDecoder	= new ReedSolomon (8, 0435, 0, 1, 10);
	      au_start		= new int [10];
//	and for the rs decoding
	      frameBytes	= new byte [rsDims * 120];   // input
	      outVector		= new byte [rsDims * 110];
	      rsIn		= new byte [120];
	      rsOut		= new byte [110];
//
//	and we load the aac decoder
	      my_aacDecoder	= new AACDecoder (soundHandler);
//	... and finally, the padhandler
	      my_padHandler	= new Padhandler (theGUI);
	   }

	   @Override
	   public void	addtoFrame	(byte [] v) {
	      byte temp;
	      int   nBits	= 24 * bitRate;

	      for (int i = 0; i < nBits / 8; i ++) {
	         temp = 0;
	         for (int j = 0; j < 8; j ++)
	            temp = (byte)((temp << 1) | (v [i * 8 + j] & 01));
	         frameBytes [blockFillIndex * nBits / 8 + i] = temp;
	      }

	      blocksInBuffer ++;
	      blockFillIndex = (blockFillIndex + 1) % 5;
/**
  *     we take the last five blocks to look at
  */
	      if (blocksInBuffer < 5)
	         return;

	      if (++frameCount >= 100) {
	         show_frameErrors (100 - frameErrors);
	         frameCount = 0;
	         frameErrors = 0;
	      }

/**
  *     starting for real: check the fire code
  *     if the fire code is OK, we handle the frame
  *     and adjust the buffer here for the next round
  */
	      if (!fc. check (frameBytes, blockFillIndex * nBits / 8)) {
	         blocksInBuffer  = 4;
	         frameErrors ++;
	         return;
	      }
	
	      if (!(processSuperframe (frameBytes,
                                       blockFillIndex * nBits / 8))) {
	         blocksInBuffer	= 4;
	         frameErrors ++;
	         return;
	      }
//      since we processed a full cycle of 5 blocks, we just start a
//      new sequence, beginning with block blockFillIndex
	      blocksInBuffer    = 0;
	      if (++ rsFrames > 25) {
	         show_rsErrors (4 * (25 - rsErrors));
	         rsFrames	= 0;
	         rsErrors	= 0;
	      }
	   }

	   private boolean processSuperframe (byte [] frameBytes, int base) {
	      int	num_aus;
	      byte	dacRate;
	      byte	sbrFlag;
	      byte	aacChannelMode;
	      byte	psFlag;
	      byte	mpegSurround;
/**
  *     apply reed-solomon error repar
  *     OK, what we now have is a vector with RSDims * 120 uint8_t's
  *     the superframe, containing parity bytes for error repair
  *     take into account the interleaving that is applied.
  */
	   for (int j = 0; j < rsDims; j ++) {
	      for (int k = 0; k < 120; k ++)
	         rsIn [k] =
	            frameBytes [(base + j + k * rsDims) % (rsDims * 120)];
	      int ler = my_rsDecoder. dec (rsIn, rsOut, 135);
	      if (ler < 0) {	// no repair possible
	         rsErrors ++;
	         return false;
	      }
	      for (int k = 0; k < 110; k ++)
	         outVector [j + k * rsDims] = rsOut [k];
	      rsFrames	++;
	   }
//      bits 0 .. 15 is firecode
//      bit 16 is unused
//	bit manipulations are easier done in C++ than in Java!!
	   dacRate	= (byte)((outVector [2] >> 6) & 01);    // bit 17
	   sbrFlag	= (byte)((outVector [2] >> 5) & 01);    // bit 18
	   aacChannelMode	= (byte)((outVector [2] >> 4) & 01);    // bit 19
	   psFlag	= (byte)((outVector [2] >> 3) & 01);    // bit 20
	   mpegSurround	= (byte)((outVector [2] & 07));         // bits 21 .. 23

	   switch (2 * dacRate + sbrFlag) {
	      default:              // cannot happen
	      case 0:
	         num_aus = 4;
	         au_start [0] = 8;
	         au_start [1] = outVector [3] * 16 +
	                                      ((outVector [4] >> 4) & 0xF);
	         au_start [2] = (outVector [4] & 0xf) * 256 + outVector [5];
	         au_start [3] = outVector [6] * 16 +
	                                       ((outVector [7] >> 4) & 0xF);
	         au_start [4] = 110 *  (bitRate / 8);
	         break;

	      case 1:
	         num_aus = 2;
	         au_start [0] = 5;
	         au_start [1] = outVector [3] * 16 +
	                                    ((outVector [4] >> 4) & 0xF);
	         au_start [2] = 110 *  (bitRate / 8);
	         break;

	      case 2:
	         num_aus = 6;
	         au_start [0] = 11;
	         au_start [1] = outVector [3] * 16 +
	                                      ((outVector [4] >> 4) & 0xF);
	         au_start [2] = (outVector [4] & 0xf) * 256 + outVector [ 5];
	         au_start [3] = outVector [6] * 16 +
	                                      ((outVector [7] >> 4) & 0xF);
	         au_start [4] = (outVector [7] & 0xf) * 256 + outVector [8];
	         au_start [5] = outVector [9] * 16 +
	                                      ((outVector [10] >> 4) & 0xF);
	         au_start [6] = 110 *  (bitRate / 8);
	         break;

	      case 3:
	         num_aus = 3;
	         au_start [0] = 6;
	         au_start [1] = outVector [3] * 16 +
	                                 ((outVector [4] >> 4) & 0xF);
	         au_start [2] = (outVector [4] & 0xF) * 256 +
	                                         (outVector [5] & 0xFF);
	         au_start [3] = 110 * (bitRate / 8);
	         break;
	   }
/**
  *     OK, the result is N * 110 * 8 bits
  *     extract the AU's, and prepare a buffer,  with the sufficient
  *     lengthy for conversion to PCM samples
  */

	      for (int i = 0; i < num_aus; i ++) {
	         int aac_frame_length;

//     sanity check 1
	         if (au_start [i + 1] < au_start [i]) {
//      should not happen, all errors were corrected
	            return false;
	         }

	         aac_frame_length = au_start [i + 1] - au_start [i] - 2;
//      just a sanity check
	         if ((aac_frame_length >=  960) || (aac_frame_length < 0)) {
	            System. out. println ("framelength " + aac_frame_length);
		    return false;
	         }

	         if (!check_crc_bytes (outVector, au_start [i],
	                                             aac_frame_length)) {
	             System. out. println
	                        ("CRC failure frame " + i + " " + num_aus);
	             return false;
	         }

	         boolean err;
	         err = handle_aacFrame (outVector, au_start [i],
	                                aac_frame_length,
	                                dacRate,
	                                sbrFlag,
	                                mpegSurround,
	                                aacChannelMode);
	         isStereo (aacChannelMode);
	         if (err)
	            aacErrors ++;
	         if (++aacFrames > 25) {
	            show_aacErrors (4 * (25 - aacErrors));
	            aacErrors      = 0;
	            aacFrames      = 0;
	         }
	      }
	      return true;
	   }

	   private boolean handle_aacFrame (byte [] 	outVector,
	                                    int		startAddress,
                                            int		frame_length,
                                            byte	dacRate,
                                            byte	sbrFlag,
                                            byte	mpegSurround,
                                            byte	aacChannelMode) {
//
//	for later, when we implement the PAD handler
	      if (((outVector [startAddress + 0] >> 5) & 07) == 4) {
	         int 	count = (outVector [startAddress + 1] & 0xFF);
	         if (count > 0) {
	            byte []	buffer 	= new byte [count];
	            System. arraycopy (outVector, startAddress + 2,
	                                          buffer, 0, count);
                    byte L0	= buffer [count - 1];
	            byte L1	= buffer [count - 2];
	            my_padHandler. processPAD (buffer, count - 3, L1, L0);
	         }
	      }

	      int tmp = my_aacDecoder. MP42PCM (dacRate,
	                                        sbrFlag,
	                                        mpegSurround,
	                                        aacChannelMode,
	                                        outVector,
	                                        startAddress,
	                                        frame_length);
	      return tmp == 0;
	   }


           private void isStereo (byte b) {
	      final byte stereoByte = b;
	      try {
	         javax. swing. SwingUtilities. invokeLater (new Runnable () {
	              @Override
	              public void run () {
	                 theGUI. show_isStereo (b);}}
	            );
	      } catch (Exception e){}
	   }
           
           private void show_frameErrors (int er) {
              final int errorsFound = er;
	      try {
	         javax. swing. SwingUtilities. invokeLater (new Runnable () {
	            @Override
	            public void run () {
	               theGUI. show_frameErrors (errorsFound);}}
	            );
	      } catch (Exception e){}
	   }
           
           private void  show_rsErrors (int er) {
              final int errorsFound = er;
//              try {
//                 javax. swing. SwingUtilities. invokeLater (new Runnable () {
//                    @Override
//                    public void run () {
//                            theGUI. show_rsErrors (errorsFound);}}
//                 );
//              } catch (Exception e){}
           }
           
           private void show_aacErrors (int er) {
              final int the_aacErrors = er;
//              try {
//                 javax. swing. SwingUtilities. invokeLater (new Runnable () {
//                    @Override
//                    public void run () {
//                            theGUI. show_aacErrors (the_aacErrors);}}
//                 );
//              } catch (Exception e){}
           }

	   private boolean check_crc_bytes (byte [] msg,
	                                    int startAddress, int frameSize) {
	   int	accumulator     = 0xFFFF;
	   int	crc;
	   int	genpoly         = 0x1021;

	   for (int i = 0; i < frameSize; i ++) {
              int data = (msg [startAddress + i] & 0xFF) << 8;
	      for (int j = 8; j > 0; j--) {
	         if (((data ^ accumulator) & 0x8000) != 0)
	            accumulator = ((accumulator << 1) ^ genpoly) & 0xFFFF;
	         else
	            accumulator = (accumulator << 1) & 0xFFFF;
	         data = (data << 1) & 0xFFFF;
	      }
	   }
//
//      ok, now check with the crc that is contained
//      in the au
	   crc     = ~(((msg [startAddress + frameSize] & 0xFF) << 8) |
	                (msg [startAddress + frameSize + 1]) & 0xFF) & 0xFFFF;
	   return (crc ^ accumulator) == 0;
	}

}

