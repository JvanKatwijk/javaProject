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
import	java.util.concurrent.Semaphore;
import  java.util.concurrent.TimeUnit;

public class	DabAudio extends DabVirtual {
	private final	ProgramData my_programData;
	private final	int	CU_SIZE		= 4 * 16;
	private final	int	dabModus;
	private final	int	DAB_PLUS	= 0100;
	private final	int	DAB		= 0101;
	private final	byte []	outV;
	private final	int [][]	interleaveData;
        private final	int	fragmentSize;
	private final	Protection protectionHandler;
	private final	AudioProcessor audioProcessor;
	private final	PCMwrapper	soundHandler;
//	private final	SoundCard	soundHandler;
	      private	int countforInterleaver  = 0;
	      private	int interleaverIndex     = 0;
	private final	byte [] shiftRegister    = new byte [9];
	private final	int []	dataV;
	private final	int []  tempX;
	private final	int [][] theData;
	private	final	byte []	disperseVector;
	private		int	nextIn	= 0;
	private		int	nextOut	= 0;
	private	final	Semaphore	freeSlots;
	private	final	Semaphore	usedSlots;
        private         boolean running;

        public DabAudio (ProgramData pd, RadioModel theScreen) {
//	System. out. println ("we made it to here");
	   my_programData	= pd;
	   fragmentSize		= pd. length * CU_SIZE;
	   dataV 		= new int [fragmentSize];
	   tempX 		= new int [fragmentSize];
	   dabModus		= pd. ASCTy == 077 ? DAB_PLUS : DAB;
	   outV			= new byte [pd. bitRate * 24];
	   theData		= new int [20] [];
	   for (int i = 0; i < 20; i ++)
	      theData [i]	= new int [fragmentSize];
	   freeSlots		=  new Semaphore (20);
	   usedSlots		=  new Semaphore (0);
           nextIn               = 0;
           nextOut              = 0;

	   interleaveData  = new int[16][]; // max size
	   for (int i = 0; i < 16; i ++) {
	      interleaveData [i] = new int [fragmentSize];
	   }

	   if (pd. shortForm)
	      protectionHandler    = new UEP_protection (pd. bitRate,
	                                                 pd. protLevel);
	   else
	      protectionHandler    = new EEP_protection (pd. bitRate,
	                                                 pd. protLevel);
//
	   soundHandler		= new PCMwrapper ();
//	   soundHandler		= new SoundCard ();
	   if (dabModus == DAB_PLUS)
	      audioProcessor = new MP4_Processor (pd. bitRate,
	                                          theScreen,
	                                          soundHandler);
	   else
	      audioProcessor = new MP2_Processor (pd. bitRate,
	                                          theScreen,
	                                          soundHandler);

	   for (int i = 0; i < 9; i ++)
               shiftRegister [i] = 1;
	   disperseVector = new byte [pd. bitRate * 24]; 
           for (int i = 0; i < pd. bitRate * 24; i ++) {
              byte b = (byte)(shiftRegister [8] ^ shiftRegister [4]);
              for (int j = 8; j > 0; j--)
                 shiftRegister [j] = shiftRegister [j - 1];
              shiftRegister [0] = b;
	      disperseVector [i] = b;
	   }
	   start ();
	}

        @Override
        public void stopRunning   () {
	   running = false;
            try {
	       this. join ();
	       soundHandler. stopCard ();
            } catch (Exception e) {}
	}
        
	final private  int INTERLEAVE_MAP [] =
	                 {0,8,4,12,2,10,6,14,1,9,5,13,3,11,7,15};

	@Override
	public void   process	(int [] v, int index, int c) {
	   try {
              while (!(freeSlots.tryAcquire (1, 50,
	                                          TimeUnit. MILLISECONDS))) {
	         if (!running) {
	            return;
                 }
              }
              System. arraycopy (v, index, theData [nextIn], 0, fragmentSize);
	      nextIn = (nextIn + 1) % 20;
	      usedSlots. release (1);
	   } catch (Exception e) {}
	}

        @Override
	public void	run () {
           System. out. println ("Audiohandler is gestart");
	   soundHandler. start ();
           synchronized (this) {
              running = true;
           }
	   try {
	      while (running) {
	         while (!usedSlots.
	                   tryAcquire (1, 50, TimeUnit.MILLISECONDS)) {
	            if (!running)
                       return;
                 }
                 processSegment (theData [nextOut]);
	      }
	   } catch (Exception e) {
              System. out. println (e. getMessage ());
           }
 	}

	private void processSegment (int [] v) {
	   for (int i = 0; i < fragmentSize; i ++) {
	      tempX [i] = interleaveData [(interleaverIndex +
                                           INTERLEAVE_MAP [i & 017]) & 017][i];
//	      interleaveData [interleaverIndex][i] = v [i];
           }
	   System. arraycopy (v, 0,
	                      interleaveData [interleaverIndex], 0, fragmentSize);
           interleaverIndex = (interleaverIndex + 1) & 0x0F;
	   nextOut = (nextOut + 1) % 20;
	   freeSlots. release ();

//      only continue when de-interleaver is filled
           if (countforInterleaver <= 15) {
              countforInterleaver ++;
           }
//
	   protectionHandler. deconvolve (tempX, fragmentSize, outV);
	   freeSlots. release (1);

	   for (int i = 0; i < my_programData. bitRate * 24; i ++) 
	      outV [i] ^= disperseVector [i];

           audioProcessor. addtoFrame (outV);
        }
	
}

