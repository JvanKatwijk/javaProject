/*
 *    Copyright (C) 2013 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Programming
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
 * 	The uep handling
 *
 *     the table is based on chapter 11 of the DAB standard.
 *
 *     The bitRate and the protectionLevel determine the
 *     depuncturing scheme.
 */

package utils;
import java.util.Arrays;
/**
  *	\brief uep_protection
  *	equal error protection, bitRate and protLevel
  *	define the puncturing table
  */
	public class UEP_protection extends Protection {
	private final	int BITRATE	= 0;
	private	final	int PROTLEVEL	= 1;
	private	final	int L1		= 2;
	private	final	int L2		= 3;
	private	final	int L3		= 4;
	private	final	int L4		= 5;
	private	final	int PI_1	= 6;
	private	final	int PI_2	= 7;
	private	final	int PI_3	= 8;
	private	final	int PI_4	= 9;

	private final   int     outSize;
	private final   int []  viterbiBlock;
	private final   boolean [] punctureTable;
	
	private final	int [][] PROFILE_TABLE  = {
	{32,    5,      3, 4, 17, 0,    5, 3, 2, -1},
	{32,    4,      3, 3, 18, 0,    11, 6, 5, -1},
	{32,    3,      3, 4, 14, 3,    15, 9, 6, 8},
	{32,    2,      3, 4, 14, 3,    22, 13, 8, 13},
	{32,    1,      3, 5, 13, 3,    24, 17, 12, 17},

	{48,    5,      4, 3, 26, 3,    5, 4, 2, 3},
	{48,    4,      3, 4, 26, 3,    9, 6, 4, 6},
	{48,    3,      3, 4, 26, 3,    15, 10, 6, 9},
	{48,    2,      3, 4, 26, 3,    24, 14, 8, 15},
	{48,    1,      3, 5, 25, 3,    24, 18, 13, 18},

	{64,    5,      6, 9, 31, 2,    5, 3, 2, 3},
	{64,    4,      6, 9, 33, 0,    11, 6, 6, -1},
	{64,    3,      6, 12, 27, 3,   16, 8, 6, 9},
	{64,    2,      6, 10, 29, 3,   23, 13, 8, 13},
	{64,    1,      6, 11, 28, 3,   24, 18, 12, 18},

	{80,    5,      6, 10, 41, 3,   6, 3, 2, 3},
	{80,    4,      6, 10, 41, 3,   11, 6, 5, 6},
	{80,    3,      6, 11, 40, 3,   16, 8, 6, 7},
	{80,    2,      6, 10, 41, 3,   23, 13, 8, 13},
	{80,    1,      6, 10, 41, 3,   24, 7, 12, 18},

	{96,    5,      7, 9, 53, 3,    5, 4, 2, 4},
	{96,    4,      7, 10, 52, 3,   9, 6, 4, 6},
	{96,    3,      6, 12, 51, 3,   16, 9, 6, 10},
	{96,    2,      6, 10, 53, 3,   22, 12, 9, 12},

//      Thanks to Kalle Riis, who found that the "112" was missing
	{112,   5,      14, 17, 50, 3,  5, 4, 2, 5},
	{112,   4,      11, 21, 49, 3,  9, 6, 4, 8},
	{112,   3,      11, 23, 47, 3,  16, 8, 6, 9},
	{112,   2,      11, 21, 49, 3,  23, 12, 9, 14},

	{128,   5,      12, 19, 62, 3,  5, 3, 2, 4},
	{128,   4,      11, 21, 61, 3,  11, 6, 5, 7},
	{128,   3,      11, 22, 60, 3,  16, 9, 6, 10},
	{128,   2,      11, 21, 61, 3,  22, 12, 9, 14},
	{128,   1,      11, 20, 62, 3,  24, 17, 13, 19},

	{160,   5,      11, 19, 87, 3,  5, 4, 2, 4},
	{160,   4,      11, 23, 83, 3,  11, 6, 5, 9},
	{160,   3,      11, 24, 82, 3,  16, 8, 6, 11},
	{160,   2,      11, 21, 85, 3,  22, 11, 9, 13},
	{160,   1,      11, 22, 84, 3,  24, 18, 12, 19},

	{192,   5,      11, 20, 110, 3, 6, 4, 2, 5},
	{192,   4,      11, 22, 108, 3, 10, 6, 4, 9},
	{192,   3,      11, 24, 106, 3, 16, 10, 6, 11},
	{192,   2,      11, 20, 110, 3, 22, 13, 9, 13},
	{224,   5,      12, 22, 131, 3, 8,  6, 2, 6},
	{224,   4,      12, 26, 127, 3, 12, 8, 4, 11},
	{224,   3,      11, 20, 134, 3, 16, 10, 7, 9},
	{224,   2,      11, 22, 132, 3, 24, 16, 10, 15},
	{224,   1,      11, 24, 130, 3, 24, 20, 12, 20},

	{256,   5,      11, 24, 154, 3, 6, 5, 2, 5},
	{256,   4,      11, 24, 154, 3, 12, 9, 5, 10},
	{256,   3,      11, 27, 151, 3, 16, 10, 7, 10},
	{256,   2,      11, 22, 156, 3, 24, 14, 10, 13},
	{256,   1,      11, 26, 152, 3, 24, 19, 14, 18},

	{320,   5,      11, 26, 200, 3, 8, 5, 2, 6},
	{320,   4,      11, 25, 201, 3, 13, 9, 5, 10},
	{320,   2,      11, 26, 200, 3, 24, 17, 9, 17},
	{384,   5,      11, 27, 247, 3, 8, 6, 2, 7},
	{384,   3,      11, 24, 250, 3, 16, 9, 7, 10},
	{384,   1,      12, 28, 245, 3, 24, 20, 14, 23},
	{0,     -1,     -1, -1, -1, -1, -1, -1, -1, -1}
};

	private final   ViterbiHandler  myViterbi;

	private int findIndex (int bitRate, int protLevel) {
	   for (int i = 0; PROFILE_TABLE [i][BITRATE] != 0; i ++)
	      if ((PROFILE_TABLE [i] [BITRATE] == bitRate) &&
	          (PROFILE_TABLE [i][PROTLEVEL] == protLevel))
	         return i;

	   return -1;
	}

	public	UEP_protection (int bitRate, int protLevel) {
	final	int	length_1;
	final	int	length_2;
	final	int	length_3;
	final	int	length_4;

	final	int	index_1;
	final	int	index_2;
	final	int	index_3;
	final	int	index_4;

	int viterbiCounter  = 0;
	
	   outSize		= 24 * bitRate;
	   myViterbi		= new ViterbiHandler (outSize, false);
	   int index		= findIndex (bitRate, protLevel);
	   if (index == -1) {   // should not happen
	      System. out. println ("problem with bitrate " + bitRate + "and priotectionlevel " + protLevel + " call an expert");
	      index = 1;
	   }
	   viterbiBlock		= new int [outSize * 4 + 24];
	   punctureTable	= new boolean [outSize * 4 + 24];
	   Arrays. fill (punctureTable, false);
	   length_1		= PROFILE_TABLE [index] [L1];
	   length_2		= PROFILE_TABLE [index] [L2];
	   length_3		= PROFILE_TABLE [index] [L3];
	   length_4		= PROFILE_TABLE [index] [L4];

	   index_1		= PROFILE_TABLE [index] [PI_1];
	   index_2		= PROFILE_TABLE [index] [PI_2];
	   index_3		= PROFILE_TABLE [index] [PI_3];
	   if ((PROFILE_TABLE [index] [PI_4]) != -1)
	      index_4  = PROFILE_TABLE [index] [PI_4];
	   else
	      index_4	= 0;

//      according to the standard we process the logical frame
//      with a pair of tuples
//      (L1, PI1), (L2, PI2), (L3, PI3), (L4, PI4)

	   for (int i = 0; i < length_1; i ++) {
	      for (int j = 0; j < 128; j ++) {
	         if (myTableHandler. get_PCode (index_1,  j % 32) != 0)
	            punctureTable [viterbiCounter] = true;
	         viterbiCounter ++;
	      }
	   }

	   for (int i = 0; i < length_2; i ++) {
	      for (int j = 0; j < 128; j ++) {
	         if (myTableHandler. get_PCode (index_2, j % 32) != 0)
	            punctureTable [viterbiCounter] = true;
	         viterbiCounter ++;
	      }
	   }

	   for (int i = 0; i < length_3; i ++) {
	      for (int j = 0; j < 128; j ++) {
	         if (myTableHandler. get_PCode (index_3, j % 32) != 0)
	            punctureTable [viterbiCounter] = true;
	         viterbiCounter ++;
	      }
	   }

	   if (index_4 != 0) {
	      for (int i = 0; i < length_4; i ++) {
	         for (int j = 0; j < 128; j ++) {
	            if (myTableHandler. get_PCode (index_4, j % 32) != 0)
	               punctureTable [viterbiCounter] = true;
	            viterbiCounter ++;
	         }
	      }
	   }
/**
  *     we have a final block of 24 bits  with puncturing according to PI_X
  *     This block constitutes the 6 * 4 bits of the register itself.
  */
	   for (int i = 0; i < 24; i ++) {
	      if (myTableHandler. get_PCode (8 - 1, i) != 0)
	         punctureTable [viterbiCounter] = true;
	      viterbiCounter ++;
	   }
	}

	@Override
	public boolean deconvolve (int [] inVec, int size, byte [] outVec) {
	   int inputCounter = 0;
//     clear the bits in the viterbiBlock,
//     only the non-punctured ones are set
	   Arrays. fill (viterbiBlock, 0);

	   for (int i = 0; i < viterbiBlock. length; i ++)
	      if (punctureTable [i])
	         viterbiBlock [i] = inVec [inputCounter ++];

//     The actual deconvolution is done by the viterbi decoder
	   myViterbi. deconvolve (viterbiBlock, outVec);
	   return true; // always true
	}
}
