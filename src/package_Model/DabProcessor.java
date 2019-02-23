/*
 *    Copyright (C) 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of the javaDAB program
 *    javaDAB is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    javaDAB is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with javaDAB; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package package_Model;
import  devices. Device;
import	utils.*;

public class DabProcessor extends Thread {
	final   private	PhaseReference  my_phaseReference;
	final   private	DabParams	my_params;
        final	private	ficHandler	my_ficHandler; 
	final	private DabBackend	my_dabBackend;
	final   private Reader		my_Reader;
	final   private	Device		my_Device;
        final   private	OfdmDecoder	my_ofdmDecoder;
	final   private	int		t_s;
	final   private	int		t_u;
	final   private	int		carriers;
	final   private	int		carrierDiff;
        	private boolean         correctionNeeded = true;
        	private float           coarseCorrector = 0.0f;
        final	private int		Khz   = 1000;
	final	private RadioModel	theGUI;
	final	private	boolean		scanning;
		private	boolean		inSync;

	public DabProcessor (DabParams	my_params,
	                     boolean	scanning,
	                     Device	theDevice,
	                     ficHandler	my_ficHandler,
	                     PhaseReference my_phaseReference,
	                     Reader	my_Reader,
	                     RadioModel theScreen) {
	   this. my_params	= my_params;
	   this. scanning	= scanning;
	   my_Device		= theDevice;
	   this. my_ficHandler	= my_ficHandler;
	   this. my_Reader	= my_Reader;
	   this. my_phaseReference	= my_phaseReference;
	   theGUI		= theScreen;

           my_ofdmDecoder       = new OfdmDecoder (my_params,
	                                           my_ficHandler,
                                                   theScreen);
	   my_dabBackend	= new DabBackend  (my_params, theScreen);
	   t_s			= my_params. get_T_s ();
	   t_u			= my_params. get_T_u ();
	   carriers		= my_params. get_carriers ();
	   carrierDiff		= my_params. get_carrierDiff ();
	}

        @Override
	public void run	() {
	final	int	nrBlocks	= my_params. get_L ();
	final	int	t_F		= my_params. get_T_F ();
        final	int	t_g		= my_params. get_T_g ();
	final	int	t_null		= my_params. get_T_null ();
	final	float []	ofdmBuffer	= new float [2 * t_u];
	final	float []	Tnull_Buffer	= new float [2 * t_null];
	final	float []	tg_Buffer	= new float [2 * t_g];
		int		counter;
		Double		cLevel;
		int		syncBufferIndex;
	final	int		syncBufferSize  = 32768;
	final	int		syncBufferMask  = syncBufferSize - 1;
	final	float [] 	envBuffer	= new float [syncBufferSize];
		float		fineCorrector	= 0.0f;
		int		attempts	= 0;

           my_Device. resetBuffer ();
           my_Reader. setRunning (true);
	   inSync	= false;
	   my_dabBackend. start ();
	   try {
	      float [] s_Sample = new float [2];
	      for (int i = 0; i < t_F / 2; i ++) {
                 my_Reader. getSamples (s_Sample, 0.0f);
	      }

//	The main loop. As long as we are not (time) synced,
//	we better start with attempting
//      to get that done. If a decent startindex is found we
//	assume to be timesynced
	      while (true) {
	         if (!inSync) {
	            syncBufferIndex        = 0;
	            cLevel                 = 0.0;

	            for (int i = 0; i < 100; i ++) {
	               my_Reader. getSamples (s_Sample, 0.0f);
	               envBuffer [syncBufferIndex] = 
	                    (s_Sample [0] < 0 ? -s_Sample [0] : s_Sample [0]) +
	                    (s_Sample [1] < 0 ? -s_Sample [1] : s_Sample [1]);
	               cLevel             += envBuffer [syncBufferIndex];
	               syncBufferIndex ++;
	            }
//	We now have initial values for currentStrength (i.e. the sum
//	over the last 50 samples) and sLevel, the long term average.
//	Here we start looking for the null level, i.e. a dip
	            counter        = 0;
	            while (cLevel / 100.0  > 0.5 * my_Reader. get_sLevel ()) {
	               my_Reader. getSamples (s_Sample, 
	                                     fineCorrector + coarseCorrector);
	              
	               envBuffer [syncBufferIndex] =
	                    (s_Sample [0] < 0 ? -s_Sample [0] : s_Sample [0]) +
	                    (s_Sample [1] < 0 ? -s_Sample [1] : s_Sample [1]);
//	update the current strength level
	              cLevel += envBuffer [syncBufferIndex] -
	                        envBuffer [(syncBufferIndex - 100) & syncBufferMask];
	              syncBufferIndex = (syncBufferIndex + 1) & syncBufferMask;
	              counter ++;
	              if (counter > t_F) {  // hopeless
	                 break;
	              }
	            }
	            if (counter > t_F) {
	               if (scanning) {
	                  attempts ++;
	                  if (attempts > 5) {
	                     attempts = 0;
	                     try {
	                        javax. swing. SwingUtilities.
	                                 invokeLater (new Runnable () {
	                           @Override
	                              public void run () {
	                                   theGUI. no_signal_found ();}}); 
	                        }
	                        catch (Exception e) {}
	                  }
	               }
	               continue;
		    }
	       
//      We  apparently found a dip that started app 65/100 * 50 samples earlier.
//      We now start looking for the end of the null period.

	            counter        = 0;
	            while (cLevel / 100.0 < 0.75 * my_Reader. get_sLevel ()) {
	               my_Reader. getSamples (s_Sample,
	                                      coarseCorrector + fineCorrector);
	       
	               envBuffer [syncBufferIndex] =
	                    (s_Sample [0] < 0 ? -s_Sample [0] : s_Sample [0]) +
	                    (s_Sample [1] < 0 ? -s_Sample [1] : s_Sample [1]);
//	update the cLevel
	               cLevel += envBuffer [syncBufferIndex] -
	                         envBuffer [(syncBufferIndex - 100) & syncBufferMask];
	               syncBufferIndex = (syncBufferIndex + 1) & syncBufferMask;
	               counter     ++;
//
	               if (counter > t_null + 100) { // hopeless
	                  break;
	               }
	            }
	            if (counter > t_null + 100)
	               continue;
	         }

//      We arrive here when time synchronized, either from a
//	(re)start of the program or after having processed a frame
//      We now have to find the exact first sample of the non-null period.
//      We use a correlation that will find the first sample after the
//      cyclic prefix.
//      Now read in Tu samples (which means here 2 * Tu values).
//	The precise number is not really important
//      as long as we can be sure that the first sample to be identified
//      is part of the samples read.
	         my_Reader. getSamples (ofdmBuffer,
	                                coarseCorrector + fineCorrector);
//      and then, call upon the phase synchronizer to verify/compute
//      the real "first" sample
	         int startIndex = my_phaseReference. findIndex (ofdmBuffer);
	         if (startIndex < 0) { // no sync, try again
	           if (inSync) {
	              inSync		= false;
                      setSync (false);
	           }
	           correctionNeeded	= true;
	           continue;
	         }
	         attempts	= 0;
	         if (!inSync) {
                    setSync (true);
                    inSync  = true;
	         }
/**
  *     Once here, we are synchronized, we need to copy the data we
  *     used for synchronization for block 0
  */
	         System. arraycopy (ofdmBuffer, 2 * startIndex,
	                            ofdmBuffer, 0, 2 * (t_u - startIndex));
//      Block 0 is special in that it is used for fine time synchronization,
//      for coarse frequency synchronization
//      and its content is used as a reference for decoding the
//      first datablock.
//      We read the missing samples in the ofdm buffer
//	I do miss the possibility of taking a slice of an array and passing
//	that as parameter
	         for (int i = t_u - startIndex; i < t_u; i ++) {
                    my_Reader. getSamples (s_Sample,
                                           coarseCorrector + fineCorrector);
	            ofdmBuffer [2 * i    ] = s_Sample [0];
	            ofdmBuffer [2 * i + 1] = s_Sample [1];
	         }
	  
//	and prepare the reference values for decoding
	         my_ofdmDecoder. processBlock (ofdmBuffer, 0);
//	         my_ofdmDecoder. processBlock_0 (ofdmBuffer);
	         my_dabBackend. processBlock (ofdmBuffer, 0);

//      Here we look only at the block_0 when we need a coarse
//      frequency synchronization.
	         correctionNeeded	= !my_ficHandler. syncReached ();
	         if (correctionNeeded) {
	            int correction =
	               my_phaseReference. estimate_CarrierOffset (ofdmBuffer);
                    coarseCorrector     += correction * carrierDiff;
                    if (Math. abs (coarseCorrector) > 35 * Khz)
                       coarseCorrector = 0.0f;
                 }
//      The first ones are the FIC blocks. We immediately
//      start with building up an average of the phase difference
//      between the samples in the cyclic prefix and the
//      corresponding samples in the datapart.
	         float freqCorr_re  = 0.0f;
	         float freqCorr_im  = 0.0f;
	         int   ofdmSymbolCount;
	         for (ofdmSymbolCount = 1;
	              ofdmSymbolCount < 4; ofdmSymbolCount ++) {
	            my_Reader. getSamples (tg_Buffer,
	                                   coarseCorrector + fineCorrector);
	            my_Reader. getSamples (ofdmBuffer,
	                                   coarseCorrector + fineCorrector);
	   
	            for (int i = 0; i < t_g; i ++) {
	              Float re_ofdm =   ofdmBuffer [2 * (t_u - t_g + i)];
	              Float im_ofdm = - ofdmBuffer [2 * (t_u - t_g + i) + 1];
	              Float re_tg   =   tg_Buffer [2 * i];
	              Float im_tg   =   tg_Buffer [2 * i + 1];
	              freqCorr_re  += re_ofdm * re_tg - im_ofdm * im_tg;
	              freqCorr_im  += re_ofdm * im_tg + im_ofdm * re_tg;
	            }
	            my_ofdmDecoder. processBlock (ofdmBuffer,
	                                            ofdmSymbolCount);
	            my_dabBackend. processBlock  (ofdmBuffer,
	                                            ofdmSymbolCount);
	         }

	         for (ofdmSymbolCount = 4;
	              ofdmSymbolCount <  nrBlocks; ofdmSymbolCount ++) {
	            my_Reader. getSamples (tg_Buffer,
	                                   coarseCorrector + fineCorrector);
	            my_Reader. getSamples (ofdmBuffer,
	                                   coarseCorrector + fineCorrector);
	   
	            for (int i = 0; i < t_g; i ++) {
	              Float re_ofdm =   ofdmBuffer [2 * (t_u - t_g + i)];
	              Float im_ofdm = - ofdmBuffer [2 * (t_u - t_g + i) + 1];
	              Float re_tg   =   tg_Buffer [2 * i];
	              Float im_tg   =   tg_Buffer [2 * i + 1];
	              freqCorr_re  += re_ofdm * re_tg - im_ofdm * im_tg;
	              freqCorr_im  += re_ofdm * im_tg + im_ofdm * re_tg;
	            }
	            my_dabBackend. processBlock (ofdmBuffer,
	                                                ofdmSymbolCount);
	         }

//	we integrate the newly found frequency offset with the
//	fine corrector
	         Float angle	= (float)(Math. atan2 (freqCorr_im, 
                                                               freqCorr_re));
	         if (!Float. isNaN (angle))
	            fineCorrector += 0.1f *
	                         angle / (float)(2.0 * Math. PI) * 
                                                     (float)(carrierDiff);
//	and go on for the next cycle by skipping Tnull samples
	         my_Reader. getSamples (Tnull_Buffer,
	                                fineCorrector + coarseCorrector);
	      }
	   } catch (Exception e) {
//	      System. out. println (e. getMessage ());
//	      System. out. println ("stopping the ofdmProcessor");
	   }
	}

	public void stopProcessor () {
           my_Reader. setRunning (false);
           try {
	       this. join ();
               Thread. sleep (1000);
           } catch (Exception e) {}
	   my_dabBackend. stopRunning ();
}

	public	void	reset () {
	   coarseCorrectorOn ();
	   my_ficHandler. reset ();
	}

	public	void	coarseCorrectorOn () {
	   correctionNeeded	= true;
	   coarseCorrector	= 0.0f;
	}

	public	void	coarseCorrectorOff () {
	   correctionNeeded	= false;
	}

	public	void	audioservice_Data   (String s, AudioData p) {
	   p. defined = false;
	   if (is_audioService (s))
	      my_ficHandler. audioservice_Data (s, p);
	}

	public void	packetservice_Data (String s, PacketData p) {
	   p. defined = false;
	   if (is_packetService (s))
	      my_ficHandler. packetservice_Data (s, p);
	}

	public	void	selectService (String s) {
	   if (correctionNeeded) {
	      return;
	   }
	   if (my_ficHandler. is_audioService (s)) {
	      AudioData as = new AudioData ();
	      my_ficHandler. audioservice_Data (s, as);
	      if (!as. defined)
	         return;
	      my_dabBackend. setAudioChannel (as);
	   }
	   else
	   if (my_ficHandler. is_packetService (s)) {
	      PacketData ps = new PacketData ();
	      my_ficHandler. packetservice_Data (s, ps);
	      if (!ps. defined)
	         return;
	      
	      my_dabBackend. setDataChannel (ps);
	   }
	}

	public	boolean	is_audioService (String s) {
	   return my_ficHandler. is_audioService (s);
	}

	public	boolean is_packetService (String s) {
	   return my_ficHandler. is_packetService (s);
	}
        
        void    setSync (boolean f) {
            final boolean flag = f;
	    try {
	       javax. swing. SwingUtilities. invokeLater (new Runnable () {
	       @Override
	          public void run () {theGUI. show_Sync (flag);}}); 
	    }
	    catch (Exception e) {}
        }
}
