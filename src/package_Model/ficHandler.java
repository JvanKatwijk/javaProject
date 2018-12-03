/*
 *    Copyright (C) 2014 .. 2017
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
 */

package package_Model;
import  utils.*;
import java.util.Arrays;

public	class	ficHandler extends fibHandler {
	final private	byte [] bitBuffer_out	= new byte [768];
        final private   byte [] pRBS		= new byte [768];
	final private	int  [] ofdmInput	= new int  [2304];
	final private	int  []	viterbiBlock	= new int  [3072 + 24];
	final private	boolean  []	indexTable	= new boolean  [3072 + 24];
	final private	byte [] fibBlock	= new byte [256];

	final private   TableHandler my_tableHandler    = new TableHandler ();
	private	int	index		= 0;
	private	int	ficno		= 0;
	private	int	ficSuccess;           
	private	int	ficCount;
        final private	int		bitsperBlock;
	final private	DabParams	params;
	final private	ViterbiHandler	myViterbi;
        final private   RadioModel	theGUI;

    /**
     *
     * @param params
     * @param theScreen
     */
    public ficHandler (DabParams params, RadioModel theScreen) {
	   super (theScreen);
           theGUI       = theScreen;
	   final byte [] shiftRegister	= new byte [9];
          
	   this. params	= params;
	   myViterbi	= new ViterbiHandler (768, true);
	   bitsperBlock	= 2 * params. get_carriers ();
	   index	= 0;
	   Arrays. fill (shiftRegister, (byte)1);        

	   for (int i = 0; i < 768; i ++) {
              pRBS [i] = (byte) (shiftRegister [8] ^ shiftRegister [4]);
              for (int j = 8; j > 0; j --)
                 shiftRegister [j] = shiftRegister [j - 1];

              shiftRegister [0] = pRBS [i];
           }

	   int	local           = 0;
//	a block of 2304 bits is considered to be a codeword
//	In the first step we have 21 blocks with puncturing according to PI_16
//	each 128 bit block contains 4 subblocks of 32 bits
//	on which the given puncturing is applied
	   Arrays. fill (indexTable, false);
	   for (int i = 0; i < 21; i ++) {
              for (int k = 0; k < 32 * 4; k ++) {
                 if (my_tableHandler. get_PCode (16,  k % 32) != 0)
                    indexTable [local] = true;
                 local ++;
              }
           }

//	In the second step we have 3 blocks with puncturing according to PI_15
//	each 128 bit block contains 4 sub-blocks of 32 bits
//	on which the given puncturing is applied

           for (int i = 0; i < 3; i ++) {
              for (int k = 0; k < 32 * 4; k ++) {
                 if (my_tableHandler. get_PCode (15, k % 32) != 0)
                    indexTable [local] = true;
                 local ++;
              }
           }

//	we have a final block of 24 bits  with puncturing according to PI_X
//	This block constitues the 6 * 4 bits of the register itself.
           for (int k = 0; k < 24; k ++) {
	      if (my_tableHandler.get_PCode (8,  k % 32) != 0)
                 indexTable [local] = true;
              local ++;
           }

	}

	public	void	process_ficBlock (int [] data, int blkno) {
           if (blkno == 1) {
              index = 0;
              ficno = 0;
           }

           if ((1 <= blkno) && (blkno <= 3)) {
              for (int i = 0; i < bitsperBlock; i ++) {
                 ofdmInput [index ++] = data [i];
                 if (index >= 2304) {
                    process_ficInput (ficno);
                    index = 0;
                    ficno ++;
                 }
              }
           }
	}

	private	void	process_ficInput (int	ficno) {
//	Now we have the full word ready for deconvolution
//	deconvolution is according to DAB standard section 11.2
           int inputCounter = 0;
	   Arrays. fill (viterbiBlock, 0);
	   for (int i = 0; i < 4 * 768 + 24; i ++)
	      if (indexTable [i])
	         viterbiBlock [i] = ofdmInput [inputCounter++];

           myViterbi. deconvolve (viterbiBlock, bitBuffer_out);
//	if everything worked as planned, we now have a
//	768 bit vector containing three FIB's
//
//	first step: energy dispersal according to the DAB standard
//	We use a predefined vector PRBS

           for (int i = 0; i < 768; i ++)
              bitBuffer_out [i] ^= pRBS [i];

//	each of the fib blocks is protected by a crc
//	(we know that there are three fib blocks each time we are here)

	   for (int i = ficno * 3; i < ficno * 3 + 3; i ++) {
	      int j = (i % 3) * 256; 
	      System. arraycopy (bitBuffer_out, (i % 3) * 256,
	                         fibBlock, 0, 256);
	      if (!check_CRC_bits (fibBlock)) {
	         show_ficSuccess (false);
	         continue;
	      }
//	      System. out. println ("x");
	      show_ficSuccess (true);
	      process_FIB (fibBlock, ficno);
           }
	}

final byte crcPolynome [] =
	{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0};	// MSB .. LSB

	boolean	check_CRC_bits (byte [] in) {
	int	size	= in. length;
	byte []	b	= new byte [16];
	int	Sum	= 0;

	   Arrays. fill (b, 0, 16, (byte)1);

	   for (int i = size - 16; i < size; i ++)
	      in [i] ^= 1;

	   for (int i = 0; i < size; i++) {
	      if ((b [0] ^ in [i]) == 1) {
	         for (int f = 0; f < 15; f++) 
	            b [f] = (byte)(crcPolynome [f] ^ b[f + 1]);
	         b [15] = 1;
	      }
	      else {
                 System. arraycopy (b, 1, b, 0, 15);		// shift
	         b [15] = 0;
	      }
	   }

	   for (int i = 0; i < 16; i++)
	      Sum += b [i];

	   return Sum == 0;
	}

	public	boolean syncReached () {
	   return ensembleIdentified;
	}

	private	void	show_ficSuccess (boolean b) {
	   if (b)
	      ficSuccess ++;
	   ficCount ++;
	   if (ficCount >= 100) {
	      final int successRate = ficSuccess;
	      try {
	         javax. swing. SwingUtilities. invokeLater (new Runnable () {
                    @Override
                    public void run () {
	                    theGUI. show_ficSuccess (successRate);}}
	         );
              }
              catch (Exception e) {}
	      ficSuccess	= 0;
	      ficCount		= 0;
	   }
	}
}

