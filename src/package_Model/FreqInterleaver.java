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

public class FreqInterleaver {
	private final int [] permTable;

	public FreqInterleaver (DabParams p) {
        int carriers	= p. get_carriers ();
	permTable	= new int [p. get_T_u ()];

        switch (p. get_dabMode ()) {
           case 1:
           default:             // shouldn't happen
              createMapper (511, 256, 256 + carriers, permTable);
              break;

           case 2:
              createMapper (127, 64, 64 + carriers, permTable);
              break;

           case 3:
              createMapper (63, 32, 32 + carriers, permTable);
              break;

	   case 4:
              createMapper (255, 128, 128 + carriers, permTable);
              break;
	   }
	}

	public final  void createMapper (int v1, int lwb,
	                                 int upb, int [] v) {
	int []	tmp    = new int [v. length];
	int	index   = 0;

           tmp [0] = 0;
           for (int i = 1; i < v. length; i ++)
	      tmp [i] = (13 * tmp [i - 1] + v1) % v. length;
	   for (int i = 0; i < v. length; i ++) {
	      if (tmp [i] == v. length / 2)
	         continue;
	      if ((tmp [i] < lwb) || (tmp [i] > upb))
	         continue;
//	we now have a table with values from lwb .. upb
	      v [index ++] = tmp [i] - v. length / 2;
//	we now have a table with values from lwb - T_u / 2 .. lwb + T_u / 2
	   }
	}

//      according to the standard, the map is a function from
//      0 .. 1535 -> -768 .. 768 (with exclusion of {0})
	int	mapIn (int n) {
           return permTable [n];
	}
}
