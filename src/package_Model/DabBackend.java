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

import  java.util.concurrent.Semaphore;
import  java.util.concurrent.TimeUnit;
import  utils.*;

public	class DabBackend extends Thread {
	private final	int		CU_SIZE		= 4 * 16;
	private final	int []		cifVector	= new int [55296];
	private 	int		cifCount	= 0;// msc blocks in CIF
	private 	int		blkCount	= 0;
	private		DabVirtual dabHandler		= new DabVirtual ();
	private final	FreqInterleaver my_Mapper;
	private final	FFT_NEW         javaFFT;
	private		boolean	work_to_be_done = false;
	private	final	DabParams	params;
	private	final	int		mode;
        private	final	int		bitsperBlock;
	private	 	int		numberofblocksperCIF;
	private final	RadioModel	theGUI;
	private final	int		t_s;
	private final	int		t_u;
	private final	int		t_g;
	private final	int		carriers;
	private final	int		nrBlocks;
	private final	int []		ibits;
	private final	float [] 	phaseReference;
	private		int		frameCount_1;
	private final	float [][]	theData;
	private		int		nextIn;
	private		int		nextOut;
	private final	Semaphore	freeSlots;
	private final	Semaphore	usedSlots;
	private		boolean		running;

        public	DabBackend (DabParams p, RadioModel theScreen) {
	   params	= p;
	   theGUI	= theScreen;
	   carriers	= params. get_carriers ();
	   mode		= params. get_dabMode ();
	   javaFFT	= new FFT_NEW (params. get_T_u ());
           bitsperBlock = 2 * carriers;
	   my_Mapper	= new FreqInterleaver   (params);
	   t_s		= params. get_T_s ();
	   t_u		= params. get_T_u ();
	   t_g		= params. get_T_g ();
	   nrBlocks	= params. get_L   ();
	   ibits	= new int [bitsperBlock];
	   phaseReference	= new float [2 * t_u];
	   frameCount_1	= 0;
	   theData	= new float [nrBlocks] [];
	   for (int i = 0; i < nrBlocks; i ++)
	      theData [i]       = new float [2 * t_u];
	   freeSlots	=  new Semaphore (nrBlocks);
	   usedSlots	=  new Semaphore (0);
	   nextIn               = 0;
	   nextOut              = 0;

	   switch (mode) {
	      case 4:	// 2 CIFS per 76 blocks
	         numberofblocksperCIF	= 36;
	         break;

	      case 1:	// 4 CIFS per 76 blocks
	         numberofblocksperCIF	= 18;
	         break;

	      case 2:	// 1 CIF per 76 blocks
	         numberofblocksperCIF   = 72;
	         break;

	      default:	// should not happen
	         numberofblocksperCIF   = 18;
	      break;
	   }
	   running = false;
	}
//
//	This is what the externals see
	public void	processBlock (float [] fft_buffer, int blkno) {
	   try {
	      while (!freeSlots.
                          tryAcquire (1, 50, TimeUnit.MILLISECONDS)) {
	         if (!running)
	            return;
	      }
	   } catch (Exception e) {}
//	we could/should assert that blkno == nextIn
//	   if (blkno != nextIn)
//	      System. out. println (blkno + "is NOT " + nextIn);
	   System. arraycopy (fft_buffer, 0,
	                      theData [nextIn], 0,fft_buffer. length);
	   usedSlots. release ();
           nextIn = (nextIn + 1) % nrBlocks;
	}

	@Override
	public void     run () {
	   running = true;

	   try {
	      while (running) {
	         while (!usedSlots.
	                  tryAcquire (1, 50, TimeUnit.MILLISECONDS)) {
	            if (!running)
	               return;
	         }
	         if (nextOut < 4) {
	            if (nextOut == 3)
	               System. arraycopy (theData [3], 0,
	                                  phaseReference, 0, t_u);
	         }
	         else {
	            decodeMscblock (theData [nextOut], nextOut);
	         }

	         freeSlots. release ();
	         nextOut = (nextOut + 1) % nrBlocks;
	            
	      }
           } catch (Exception e) {}
	}

	public void	stopRunning	() {
	   running = false;
	}
/**
  *	Msc block decoding is equal to FIC block decoding,
  *	further processing is different though
  */
	public void	decodeMscblock (float [] fft_buffer, int blkno) {
	   javaFFT.  fft (fft_buffer);
	   for (int i = 0; i < carriers; i ++) {
	      int	index	= my_Mapper.  mapIn (i);
	      if (index < 0) index += t_u;

//	decoding is computing the phase difference between
//	carriers with the same index in subsequent blocks.
	      float re_1	= fft_buffer     [2 * index];
	      float re_2	= phaseReference [2 * index];
	      float im_1	= fft_buffer     [2 * index + 1];
	      float im_2	= -phaseReference [2 * index + 1];

	      float re_new	= re_1 * re_2 - im_1 * im_2;
	      float im_new	= re_1 * im_2 + re_2 * im_1;
	      float jan_abs	= (re_new < 0 ? - re_new : re_new) +
	                          (im_new < 0 ? - im_new : im_new);

//	split the real and the imaginary part and scale it
//	we make the bits into softbits in the range -127 .. 127
	      ibits [i]		   =  -(int)(re_new / jan_abs * 127.0);
	      ibits [carriers + i]      =  -(int)(im_new / jan_abs * 127.0);
	   }
//	The carrier of a block is the reference for the carrier
//	on the same position in the next block
	   System. arraycopy (fft_buffer, 0,
	                             phaseReference, 0, fft_buffer. length);
	      process_mscBlock (ibits, blkno);
	}

//      Any change in the selected service will only be active
//      in the next call to process_mscBlock.
	public	void	process_mscBlock (int  [] fbits, int blkno) {
	int	currentblk;
        int     myBegin;

	    if (!work_to_be_done)
	       return;

	    currentblk	= (blkno - 4) % numberofblocksperCIF;
	    System. arraycopy (fbits, 0, 
	                       cifVector, currentblk * bitsperBlock,
	                       bitsperBlock);
	    if (currentblk < numberofblocksperCIF - 1)
	       return;

//	OK, now we have a full CIF
	    blkCount	= 0;
	    cifCount	= (cifCount + 1) & 03;
//
//	This might be a dummy handler
	    try {
                synchronized (this) {
	           myBegin	= dabHandler. startAddress () * CU_SIZE;
	           dabHandler. process (cifVector,
	                                myBegin,
	                                dabHandler. length () * CU_SIZE);
                }
	    } catch (Exception e) {
	       System. out. println (e. getMessage ());
	       System. out. println ("Dabhandler fails");
	    }
	}

//	In the (may be far) future we might want the processing
//	of the data to be done in a separate thread, so we first
//	copy the program data
	public void	setAudioChannel (AudioData d) {
	   AudioData ad = new AudioData ();
	   synchronized (this) {
	      dabHandler. stopRunning ();
	      work_to_be_done	= false;
	      ad. shortForm	= d. shortForm;
	      ad. startAddr	= d. startAddr;
	      ad. length	= d. length;
	      ad. protLevel	= d. protLevel;
	      ad. bitRate	= d. bitRate;
	      ad. ASCTy		= d. ASCTy;
	      dabHandler	= new DabAudio (ad, theGUI);
	      work_to_be_done	= true;
	   }
	   
	   System. out. print ("setting channel startaddress " + d. startAddr);
	   System. out. print (" length = " + d. length + " bitRate " + d. bitRate);
	   System. out. println (" + protLevel " + d. protLevel);
	}

	public void	setDataChannel (PacketData pd) {
	   synchronized (this) {
	      dabHandler. stopRunning ();
	      work_to_be_done	= false;
	      dabHandler	= new DabData (pd, theGUI);
	      work_to_be_done	= true;
	   }
	   
	   System. out. print ("setting channel startaddress " + pd. startAddr);
	   System. out. print (" length = " + pd. length + " bitRate " + pd. bitRate);
	   System. out. println (" + protLevel " + pd. protLevel);
	}


	public void reset      () {
	   work_to_be_done = false;
	}
}

