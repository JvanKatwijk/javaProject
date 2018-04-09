/*
 * Copyright 2004,2010 Free Software Foundation, Inc.
 *
 * This file is part of GNU Radio
 *
 * GNU Radio is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 *
 * GNU Radio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GNU Radio; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street,
 * Boston, MA 02110-1301, USA.
 */
//
//      This is a (partial) rewrite of the GNU radio code, for use
//      within the java DAB software
//      all rights are acknowledged.
//	Java does not "know" unsigned integers, so we add an abundant
//	amount of filters.
//
package package_Model;
import java.util.Arrays;

class	firecodeChecker {
//      g(x)=(x^11+1)(x^5+x^3+x^2+x+1)=1+x+x^2+x^3+x^5+x^11+x^12+x^13+x^14+x^16
	private final byte [] g    =  {1,1,1,1,0,1,0,0,0,0,0,1,1,1,1,0};
        private final int  [] tab;
                
	public firecodeChecker () {
// prepare the table
	   int	[]  regs 	= new int  [16];
	   int	[]  itab 	= new int  [8];
           tab                  = new int  [256];

	   for (int i = 0; i < 8; i++) {
	      Arrays. fill (regs, 0);
              regs [8 + i]	= 1;
              itab [i]		= run8 (regs);
	   }

	   for (int i = 0; i < 256; i++) {
	      tab [i] = 0;
	      for (int j = 0; j < 8; j++) {
	         if ((i & (1 << j)) != 0)
	            tab [i] = tab[i] ^ itab[j];
	      }
	   }
        }

	private int run8 (int [] regs) {
	   int	z;
	   long	v = 0;

	   for (int i = 0; i < 8; i++) {
	      z = regs [15];
              for (int j = 15; j > 0; j--)
                 regs [j] = regs [j-1] ^ (z & g [j]);
              regs [0] = z;
	   }

	   for (int i = 15; i >= 0; i--)
	      v = (v << 1) | regs[i];

	   return (int)(v & 0xFFFF);
	}

	public boolean check (final byte [] x, int index ) { 
	   int state = (x [index + 2] << 8) | (x [index + 3] & 0xFF);
	   int istate = 0;
	   state	= state & 0xFFFF;

	   for (int i = 4; i < 11; i++) {
	      istate = tab [(state  >> 8) & 0xFF];
	      int oldState = state;
	      state = ((istate & 0x00ff) ^ (x [index + i] & 0xFF)) |
	              ((istate ^ ((oldState & 0xFF) << 8)) & 0xff00);
	      state = state & 0xFFFF;
	   }
          
    	   for (int i = 0; i < 2; i++) {
	      istate = tab [(state >> 8) & 0xFF];
	      state = ((istate & 0x00ff) ^ (x [index + i] & 0xFF)) |
	              ((istate ^ ((state & 0xFF) << 8)) & 0xff00);
	      state = state & 0xFFFF;
	      if (state < (long)0)
	        System. out. println ("rampen komen over ons voor i " + i);
	   }

	   return (state & 0xFFFF) == 0;
	}
}
