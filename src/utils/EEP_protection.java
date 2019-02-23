/*
 *    Copyright (C) 2013 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Programming
 *
 *    This file is part of java DAB
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
 * 	The eep handling
 */

package utils;
import java.util.Arrays;
/**
  *	\brief eep_protection
  *	equal error protection, bitRate and protLevel
  *	define the puncturing table
  */
	public class EEP_protection extends Protection {
	private final	int	bitRate;
	private	final	int	protLevel;
	private	final	int	outSize;
	private final	int []	viterbiBlock;
	private final	boolean []	indexTable;

	private	final	ViterbiHandler	myViterbi;
	
	
	public	EEP_protection (int rate, int level) {
	final	int	L1;
	final	int	L2;
	final	int	tableIndex_1;
	final	int	tableIndex_2;
		int	inputCounter	= 0;
		int	viterbiCounter	= 0;

	   bitRate	= rate;
	   protLevel	= level;
	   outSize	= 24 * bitRate;
	   viterbiBlock	= new int [outSize * 4 + 24];
	   indexTable	= new boolean [outSize * 4 + 24];
	   Arrays. fill (indexTable, false);	
	   myViterbi	= new ViterbiHandler (outSize, false);
	   if ((protLevel & (1 << 2)) == 0) {	// set A profiles
	      switch (protLevel & 03) {
	      default:
	         case 0:			// actually level 1
	            L1			= 6 * bitRate / 8 - 3;
	            L2			= 3;
	            tableIndex_1	= 24;	// index in protection table
	            tableIndex_2	= 23;
	            break;

	         case 1:			// actually level 2
	            if (bitRate == 8) {
	               L1		= 5;
	               L2		= 1;
	               tableIndex_1	= 13;
	               tableIndex_2	= 12;
	            } else {
	               L1		= 2 * bitRate / 8 - 3;
	               L2		= 4 * bitRate / 8 + 3;
	               tableIndex_1	= 14;
	               tableIndex_2	= 13;
	            }
	            break;

	         case 2:			// actually level 3
	            L1			= 6 * bitRate / 8 - 3;
	            L2			= 3;
	            tableIndex_1	= 8;
	            tableIndex_2	= 7;
	            break;

	         case 3:			//actually level 4
	            L1			= 4 * bitRate / 8 - 3;
	            L2			= 2 * bitRate / 8 + 3;
	            tableIndex_1	= 3;
	            tableIndex_2	= 2;
	            break;
	      }
	   }
	   else {  // ((protLevel & (1 << 2)) != 0) 		// B series
	      switch ((protLevel & 03)) {
	         case 3:					// actually level 4
	            L1			= 24 * bitRate / 32 - 3;
	            L2			= 3;
	            tableIndex_1	= 2;
	            tableIndex_2	= 1;
	            break;

	         case 2:					// actually level 3
	            L1			= 24 * bitRate / 32 - 3;
	            L2			= 3;
	            tableIndex_1	= 4;
	            tableIndex_2	= 3;
	            break;

	         case 1:					// actually level 2
	            L1			= 24 * bitRate / 32 - 3;
	            L2			= 3;
	            tableIndex_1	= 6;
	            tableIndex_2	= 5;
	            break;
	         
	         default:
	         case 0:					// actually level 1
	            L1			= 24 * bitRate / 32 - 3;
	            L2			= 3;
	            tableIndex_1	= 10;
	            tableIndex_2	=  9 ;
	            break;
	      }
	   }

//	according to the standard we process the logical frame
//	with a pair of tuples
//	(L1, PI1), (L2, PI2), (L3, PI3), (L4, PI4)
//
	   for (int i = 0; i < L1; i ++) {
	      for (int j = 0; j < 128; j ++) {
	         if (myTableHandler. get_PCode (tableIndex_1, j % 32) != 0) 
	            indexTable [viterbiCounter] = true;
	         viterbiCounter ++;	
	      }
	   }

	   for (int i = 0; i < L2; i ++) {
	      for (int j = 0; j < 128; j ++) {
	         if (myTableHandler. get_PCode (tableIndex_2, j % 32) != 0) 
	            indexTable [viterbiCounter] = true;
	         viterbiCounter ++;	
	      }
	   }
//	we had a final block of 24 bits  with puncturing according to PI_X
//	This block constitues the 6 * 4 bits of the register itself.
	   for (int i = 0; i < 24; i ++) {
	      if (myTableHandler. get_PCode (8,  i) != 0) 
	         indexTable [viterbiCounter] = true;
	      viterbiCounter ++;
	   }

	}

	@Override
	public	boolean deconvolve (int [] inVec, int size, byte [] outVec) {
	   int inputCounter  = 0;
	   Arrays. fill (viterbiBlock, 0);
	   for (int i = 0; i < viterbiBlock. length; i ++)
	      if (indexTable [i])
	         viterbiBlock [i] = inVec [inputCounter ++];

	   myViterbi. deconvolve (viterbiBlock, outVec);
	   return true;
	}
}

