/*
 *    Copyright (C) 2014 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of java DAB
 *
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
//
//	Pretty straightforward package for galois computing,
//	up to 8 bits symsize

package package_Model;

public class Galois {
	private final	int	mm;            /* Bits per symbol */
        private final	int	gfpoly;
        private final	int	codeLength;    /* Symbols per block (= (1<<mm)-1) */
        private final	int	d_q;
        private final	int []	alpha_to;     /* log lookup table */
        private final	int []	index_of;     /* Antilog lookup table */

	public	Galois (int symsize, int gfpoly) {
	   int sr;
	   mm		= symsize;
	   this. gfpoly	= gfpoly;
	   codeLength	= (1 << mm) - 1;
	   d_q		=  1 << mm;
	   alpha_to	= new int [codeLength + 1];
	   index_of	= new int [codeLength + 1];
/*	generate Galois field lookup tables */
	   index_of [0]		= codeLength;	/* log (zero) = -inf */
	   alpha_to [codeLength] = 0;	/* alpha**-inf = 0 */

	   sr = 1;
	   for (int i = 0; i < codeLength; i++){
	      index_of [sr] = i;
	      alpha_to [i] = sr;
	      sr <<= 1;
	      if ((sr & (1 << symsize)) != 0)
	         sr ^= gfpoly;
	      sr &= codeLength;
	   }
	}

	private int modnn (int x) {
	   while (x >= codeLength) {
	      x -= codeLength;
	      x = (x >> mm) + (x & codeLength);
	   }
	   return x;
	}

	private int	round_mod (int a, int n) {
	   return (a % n < 0) ? (a % n + n) : (a % n);
	}

	public int	add_poly (int a, int b) {
	   return a ^ b;
	}

	public int	poly2power (int a) {
	   return index_of [a];
	}

	public int	power2poly (int a) {
	   return alpha_to [a];
	}

	public int	add_power	(int a, int b) {
	   return index_of [alpha_to [a] ^ alpha_to [b]];
	}

	public int	multiply_power	(int a, int b) {
	   return modnn (a + b);
	}

	public int	multiply_poly	(int a, int b) {
	   if ((a == 0) || (b == 0))
	      return 0;
	   return alpha_to [multiply_power (index_of [a], index_of [b])];
	}

	public int	divide_power	(int a, int b) {
	   return modnn (d_q - 1 + a - b);
	}

	public int	divide_poly	(int a, int b) {
	   if (a == 0)
	      return 0;
	   return alpha_to [divide_power (index_of [a], index_of [b])];
	}

	public int	inverse_poly	(int a) {
	   return alpha_to [inverse_power (index_of [a])];
	}

	public int	inverse_power	(int a) {
	   return d_q - 1 - a;
	}

	public int	pow_poly	(int a, int n) {
	   return alpha_to [pow_power (index_of [a], n)];
	}

	public int	pow_power	(int a, int n) {
	   return (a == 0) ? 0 : (a * n) % (d_q - 1);
	}
}
