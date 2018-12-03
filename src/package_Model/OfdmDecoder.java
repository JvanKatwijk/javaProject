/*
 *    Copyright (C) 2013 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of javaDab
 *    javaDab is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    javaDab is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with javaDab; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *	Once the samples are "in", interpretation and manipulation
 *	should reconstruct the data blocks.
 *	Ofdm_decoder is called once every Ts samples, and
 *	its invocation results in 2 * Tu bits
 */
package package_Model;
import  java.util.concurrent.Semaphore;
import  java.util.concurrent.TimeUnit;
import	utils.*;

	public class OfdmDecoder extends Thread {
	   private final	DabParams	my_dabParams;
	   private final	FFT_NEW		my_FFT;
//	   private final	fftHandler	my_FFT;
	   private final	ficHandler	my_ficHandler;
	   private final	DabBackend	my_dabBackend;
	   private final	FreqInterleaver	my_Mapper;
	   private final	RadioModel	theGUI;
	   private final	int		t_s;
	   private final	int		t_u;
	   private final	int		t_g;
	   private final	int		carriers;
	   private final	int		nrBlocks;
	   private final	int []		ibits;
	   private final	float []	phaseReference;
	   private final	float []	hulpVector;
	   private		float		current_snr;
	   private		int		frameCount_1;
	   private		int		frameCount_2;
	   private final	float [][]	theData;
	   private		int		nextIn;
	   private		int		nextOut;
	   private final	Semaphore	freeSlots;
	   private final	Semaphore	usedSlots;
           private              boolean         running;
	   private final	int []	spectrumBuffer;

	   public	OfdmDecoder	(DabParams	my_dabParams,
	                                 ficHandler	fc,
	                                 DabBackend	mb,
	                                 RadioModel	theGUI) {
	      this. my_dabParams	= my_dabParams;
	      my_FFT		= new FFT_NEW (my_dabParams. get_T_u ());
//	      my_FFT		= new fftHandler (my_dabParams. get_dabMode ());
	      my_ficHandler	= fc;
	      my_dabBackend	= mb;
	      this. theGUI	= theGUI;
	      my_Mapper		= new FreqInterleaver	(my_dabParams);
	      t_s		= my_dabParams. get_T_s ();
	      t_u		= my_dabParams. get_T_u ();
	      t_g		= my_dabParams. get_T_g ();
	      carriers		= my_dabParams. get_carriers ();
	      nrBlocks		= my_dabParams. get_L   ();
	      ibits		= new int [2 * carriers];
	      phaseReference	= new float [2 * t_u];
	      hulpVector	= new float [2 * t_u];
	      spectrumBuffer	= new int [t_u];
	      frameCount_1	= 0;
	      frameCount_2	= 0;
	      theData              = new float [nrBlocks] [];
	      for (int i = 0; i < nrBlocks; i ++)
	         theData [i]       = new float [2 * t_u];
	      freeSlots            =  new Semaphore (nrBlocks);
	      usedSlots            =  new Semaphore (0);
	      nextIn               = 0;
	      nextOut              = 0;
              running              = false;

	   }

	   public void stopRunning	() {
	      running	= false;
	   }

	   public void reset	() {
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
//	            System. arraycopy (theData [nextOut], 0,
//	                                   hulpVector, 0, 2 * t_u);
	            if (nextOut == 0)
	               processBlock_0 (theData [nextOut]);
	            else
	            if (nextOut < 4) {
	               decodeFICblock (theData [nextOut], nextOut);
                    }
                    else {
	               decodeMscblock (theData [nextOut], nextOut);
                    }
	            freeSlots. release ();
	            nextOut = (nextOut + 1) % nrBlocks;
	            
                 }
              } catch (Exception e) {}
	   }

	   public	void processBlock (float [] buffer, int blockno) {
	      try {
                  while (!freeSlots.
	                     tryAcquire (1, 50, TimeUnit.MILLISECONDS)) {
	              if (!running)
	                 return;
	          }
	          System. arraycopy (buffer, 0, theData [nextIn], 0, 2 * t_u);
	          nextIn = (nextIn + 1) % nrBlocks;
	          usedSlots. release ();
	      } catch (InterruptedException e) {}
           }

	   public	void	processBlock_0 (float [] buffer) {
//	we could add an assert here that buffer, length = 2 * t_u
	      System. arraycopy (buffer, 0, phaseReference, 0, buffer. length);
	      my_FFT. fft (phaseReference);
//	      my_FFT. do_FFT (phaseReference, 1);
	      current_snr	= 0.6f * current_snr +
	                                    0.4f * get_snr (phaseReference);
	      if (++frameCount_2 > 10) {
	         final int the_snr = (int)current_snr;
	         frameCount_2 = 0;
	         try {
	               javax. swing. SwingUtilities. invokeLater (new Runnable () {
	                  @Override
	                  public void run () {theGUI. show_SNR (the_snr);}});
	            }
	            catch (Exception e) {}
	         }
	      }
//
/**
  *	for the other blocks of data, the first step is to go from
  *	time to frequency domain, to get the carriers.
  *	we distinguish between FIC blocks and other blocks,
  *	only to spare a test. The mapping code is the same
     * @param fft_buffer
     * @param blkno
  */
	   public void	decodeFICblock (float [] fft_buffer, int blkno) {
	      my_FFT.  fft (fft_buffer);
//	      my_FFT.  do_FFT (fft_buffer, 1);

	      if (++frameCount_1 > 12) {
	         frameCount_1 = 0;
//	         try {
//	              for (int i = 0; i < t_u / 2; i ++) {
//	                 float re = phaseReference [2 * i];
//	                 float im = phaseReference [2 * i + 1];
//	                 float t = (re < 0 ? - re : re) +
//	                             (im < 0 ? - im : im);
//	                 spectrumBuffer [t_u / 2 + i] = (int)(128 * t);
//	               }
//	              for (int i = 0; i < t_u / 2; i ++) {
//	                 float re = phaseReference [t_u + 2 * i];
//	                 float im = phaseReference [t_u + 2 * i + 1];
//	                 float t = (re < 0 ? - re : re) +
//	                             (im < 0 ? - im : im);
//	                 spectrumBuffer [i] = (int)(128 * t);
//	               }
//	               javax. swing. SwingUtilities. invokeLater (new Runnable () {
//	                  @Override
//	                  public void run () {theGUI. addLine (spectrumBuffer);}});
//	            }
//	            catch (Exception e) {}
	      }
//	a little optimization: we do not interchange the
//	positive/negative frequencies to their right positions.
//	The de-interleaving understands this
	      for (int i = 0; i < carriers; i ++) {
	         int	index	= my_Mapper.  mapIn (i);
	         if (index < 0) index += t_u;

//	decoding is computing the phase difference between
//	carriers with the same index in subsequent blocks.
	         float re_1	= fft_buffer      [2 * index];
	         float re_2	= phaseReference  [2 * index];
	         float im_1	= fft_buffer      [2 * index + 1];
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
	   
	   my_ficHandler. process_ficBlock (ibits, blkno);
}
/**
  *	Msc block decoding is equal to FIC block decoding,
  *	further processing is different though
     * @param fft_buffer
     * @param blkno
  */
	   public void	decodeMscblock (float [] fft_buffer, int blkno) {
	      my_FFT.  fft (fft_buffer);
//	      my_FFT.  do_FFT (fft_buffer, 1);

//	a little optimization: we do not interchange the
//	positive/negative frequencies to their right positions.
//	The de-interleaving understands this
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
	      my_dabBackend. process_mscBlock (ibits, blkno);
	   }

/**
  *     for the snr we have a full T_u wide vector, with in the middle
  *     K carriers.
  *     Just get the strength from the selected carriers compared
  *     to the strength of the carriers outside that region
  */
	   private float get_snr (float [] v) {
	   float   noise   = 0;
	   float   signal  = 0;
	   int	low     = t_u - carriers / 2;
	   int	high    = t_u + carriers / 2;

	      for (int i = t_u / 2; i < low - 20; i ++) {
	         float n_re = v [(2 * i) % (2 * t_u)];
	         float n_im = v [(2 * i + 1) % (2 * t_u)];
	         noise += Math. sqrt (n_re * n_re + n_im * n_im);
	      }

	      for (int i = high + 20; i < t_u - 10; i ++) {
	         float n_re = v [(2 * i) % (2 * t_u)];
	         float n_im = v [(2 * i + 1) % (2 * t_u)];
	         noise += Math. sqrt (n_re * n_re + n_im * n_im);
	      }
	      noise   /= t_u - 60 - carriers;

	      for (int i = t_u - carriers / 4;  i < t_u + carriers / 4; i ++) {
	         float s_re = v [(2 * i) % (2 * t_u)];
	         float s_im = v [(2 * i + 1) % (2 * t_u)];
	         signal += Math. sqrt (s_re * s_re + s_im * s_im);
	      }
	      signal /= carriers / 2;
   
	      return (float)(20.0 * Math. log10 (signal + 1) -
	                     20.0 * Math. log10 (noise  + 1));
   
	   }
}


