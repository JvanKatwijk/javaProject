/*
 *    Copyright (C) 2014 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of javaDab
 *
 *    DAB java is free software; you can redistribute it and/or modify
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
package utils;

public	class ViterbiHandler {
	final private int	K	=       7;
//#define               Poly1   91
	final private int	Poly1	=	0133;
//#define               Poly2   121
	final private int	Poly2	=	0171;
//#define               Poly3   101
	final private int	Poly3   =       0145;
//#define               Poly4   91
	final private int	Poly4	= 	0133;
	final private int	numofStates =	(1 << (K - 1));

	final private int []	predecessor_for_0	= new int [numofStates];
	final private int []	predecessor_for_1	= new int [numofStates];

	final private int [][]	transCosts;
	final private int [][]	history;
	final private int []	stateSequence;

	final private int	blockLength;

	final private short []	indexTable;
	final private int []	costTable = new int [16];

	public		ViterbiHandler	(int blockLength, boolean flag) {
	   this. blockLength  = blockLength;
//
           transCosts	= new int [blockLength + 6 + 1][];
	   history	= new int [blockLength + 6 + 1][];
	   stateSequence	= new int [blockLength + 6 + 1];
//
	   for (int i = 0; i <= blockLength + 6; i++) {
	      transCosts [i]	= new int [numofStates];
	      history    [i]	= new int [numofStates];
	      stateSequence [i]	= 0;
	      for (int j = 0; j < numofStates; j ++) {
	         transCosts [i][j] = 0;
	         history    [i][j] = 0;
	      }
	   }

//  These tables give a mapping from (state * bit * Poly -> outputbit)
	   byte [] poly1_table     = new byte [2 * numofStates];
	   for (int i = 0; i < 2; i ++)
              for (int j = 0; j < numofStates; j ++)
                 poly1_table [i * numofStates + j] = bitFor (j, Poly1, i);

           byte [] poly2_table     = new byte [2 * numofStates];
	   for (int i = 0; i < 2; i ++)
	      for (int j = 0; j < numofStates; j ++)
	         poly2_table [i * numofStates + j] = bitFor (j, Poly2, i);

	   byte [] poly3_table     = new byte [2 * numofStates];
	   for (int i = 0; i < 2; i ++)
              for (int j = 0; j < numofStates; j ++)
                 poly3_table [i * numofStates + j] = bitFor (j, Poly3, i);

	   byte [] poly4_table     = new byte [2 * numofStates];
	   for (int i = 0; i < 2; i ++)
	      for (int j = 0; j < numofStates; j ++)
	         poly4_table [i * numofStates + j] = bitFor (j, Poly4, i);

//      The indextable maps the four bits we get from the polynomes
//      to an index, used in computing the costs
        indexTable      = new short [2 * numofStates];
           for (int i = 0; i < 2 * numofStates; i ++)
              indexTable [i] = (short) (
                    ((poly1_table [i] != 0) ? 8 : 0) +
                    ((poly2_table [i] != 0) ? 4 : 0) +
                    ((poly3_table [i] != 0) ? 2 : 0) +
                    ((poly4_table [i] != 0) ? 1 : 0));

	   for (int i = 0; i < numofStates; i ++) {
	      predecessor_for_0 [i] = ((i << 1) + 00) & (numofStates - 1);
	      predecessor_for_1 [i] = ((i << 1) + 01) & (numofStates - 1);
           }
        }

//	Note that the soft bits are such that
//	they are int16_t -255 -> (bit)1, +255 -> (bit)0
	void    computeCostTable (short sym_0,
                                      short sym_1, short sym_2, short sym_3) {
        costTable [0]  = - sym_0 - sym_1 - sym_2 - sym_3;
        costTable [1]  = - sym_0 - sym_1 - sym_2 + sym_3;
        costTable [2]  = - sym_0 - sym_1 + sym_2 - sym_3;
        costTable [3]  = - sym_0 - sym_1 + sym_2 + sym_3;
        costTable [4]  = - sym_0 + sym_1 - sym_2 - sym_3;
        costTable [5]  = - sym_0 + sym_1 - sym_2 + sym_3;
        costTable [6]  = - sym_0 + sym_1 + sym_2 - sym_3;
        costTable [7]  = - sym_0 + sym_1 + sym_2 + sym_3;
        costTable [8]  = + sym_0 - sym_1 - sym_2 - sym_3;
        costTable [9]  = + sym_0 - sym_1 - sym_2 + sym_3;
        costTable [10] = + sym_0 - sym_1 + sym_2 - sym_3;
        costTable [11] = + sym_0 - sym_1 + sym_2 + sym_3;
        costTable [12] = + sym_0 + sym_1 - sym_2 - sym_3;
        costTable [13] = + sym_0 + sym_1 - sym_2 + sym_3;
        costTable [14] = + sym_0 + sym_1 + sym_2 - sym_3;
        costTable [15] = + sym_0 + sym_1 + sym_2 + sym_3;
}

//      block is the sequence of soft bits
//      its length = 4 * blockLength + 4 * 6
	public		void	deconvolve	(int [] sym,
	                                         byte [] bitBuffer) {
	   int	prev_0, prev_1;
	   int	costs_0, costs_1;
//
//      first step is to "pump" the soft bits into the state machine
//      and compute the cost matrix.
//      we assume the overall costs for state 0 are zero
//      and remain zero

	   for (int i = 1; i <= blockLength + 6; i ++) {
              short	sym_0 = (short)(- sym [4 * (i - 1) + 0]);
              short	sym_1 = (short)(- sym [4 * (i - 1) + 1]);
              short	sym_2 = (short)(- sym [4 * (i - 1) + 2]);
              short	sym_3 = (short)(- sym [4 * (i - 1) + 3]);

	      computeCostTable (sym_0, sym_1, sym_2, sym_3);
              for (int cState = 0; cState < numofStates / 2; cState ++) {
                 byte entrybit =  0;
                 prev_0    = predecessor_for_0 [cState];
                 prev_1    = predecessor_for_1 [cState];
//      we compute the minimal costs, based on the costs of the
//      prev states, and the additional costs of arriving from
//      the previous state to the current state with the symbol "sym"
//
//      entrybit = 0, so the index for the cost function is prev_xx
	         costs_0 = transCosts [i - 1] [prev_0] +
	                   costTable [indexTable [prev_0]];
	         costs_1 = transCosts [i - 1] [prev_1] +
	                   costTable [indexTable [prev_1]];
	         if (costs_0 < costs_1) {
	            transCosts [i] [cState] = costs_0;
	            history [i][cState] = prev_0;
	         } else {
	            transCosts [i] [cState] = costs_1;
	            history [i][cState] = prev_1;
	         }
	      }

	      for (int cState = numofStates / 2;
	                                cState < numofStates; cState ++) {
                 byte entrybit = 1;
	         prev_0    = predecessor_for_0 [cState];
	         prev_1    = predecessor_for_1 [cState];

//      we compute the minimal costs, based on the costs of the
//      prev states, and the additional costs of arriving from
//      the previous state to the current state with the symbol row "sym"
//
//      entrybit is here "1", so the index is id cost function
//      is prev_xx + NumofStates
	         costs_0 = transCosts [i - 1] [prev_0] +
	                      costTable [indexTable [prev_0 + numofStates]];
	         costs_1 = transCosts [i - 1] [prev_1] +
                              costTable [indexTable [prev_1 + numofStates]];
	         if (costs_0 < costs_1) {
	            transCosts [i] [cState] = costs_0;
	            history [i] [cState] = prev_0;
	         } else {
	            transCosts [i] [cState] = costs_1;
	            history [i][cState] = prev_1;
                 }
              }
           }
//
//      Once all costs are computed, we can look for the minimal cost
//      Our "end state" is somewhere in column blockLength + 6
	   int minimalCosts	= 100000;
	   int bestState	= 0;

	   for (int i = 0; i < numofStates; i++) {
	      if (transCosts [blockLength + 4][i] < minimalCosts) {
	          minimalCosts = transCosts [blockLength][i];
	          bestState = i;
	      }
	   }

	   stateSequence [blockLength] = bestState;
/*
 *      Trace backgoes back to state 0, and builds up the
 *      sequence of decoded symbols
 */
	   for (int i = blockLength + 4; i > 0; i --)
	      stateSequence [i - 1] = history [i][stateSequence[i]];

	   for (int i = 1; i <= blockLength; i++)
	      bitBuffer [i - 1] =  (byte) ((stateSequence [i] >= numofStates / 2) ? 01 : 00);
    }


/*
 *      as an aid, we give a function "bitFor" that, given
 *      the register state, the polynome and the bit to be inserted
 *      returns the bit coming from the engine
 */
	private  byte	bitFor (int state, int poly, int bit) {
	   int  register;
	   byte resBit = 0;
//
//      the register after shifting "bit" in would be:
           register = bit == 0 ? state : (state + numofStates);
           register &= poly;
/*
 *      now for the individual bits
 */
           for (int i = 0; i <= K; i++) {
              resBit ^=  (byte)(register & 01);
              register >>= 1;
           }

	   return resBit;
	}

 };

