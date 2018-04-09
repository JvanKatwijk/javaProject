
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
public class DabParams {
    private int dabMode;
    private int l;
    private int k;
    private int t_null;
    private int t_F;
    private int t_s;
    private int t_u;
    private int t_g;
    private int carrierDiff;
    
    public DabParams (int mode) {
        switch (mode) {
           case 2:
              dabMode   = 2;
              l        = 76;           // blocks per frame
              k         = 384;          // carriers
              t_null    = 664;          // null length
              t_F       = 49152;        // samples per frame
              t_s       = 638;          // block length
              t_u       = 512;          // useful part
              t_g       = 126;
              carrierDiff       = 4000;
              break;

            case 4:
              dabMode           = 4;
              l                 = 76;
              k                 = 768;
              t_F               = 98304;
              t_null            = 1328;
              t_s               = 1276;
              t_u               = 1024;
              t_g               = 252;
              carrierDiff       = 2000;
              break;

           case 3:
              dabMode           = 3;
              l                 = 153;
              k                 = 192;
              t_F               = 49152;
              t_null            = 345;
              t_s               = 319;
              t_u               = 256;
              t_g               = 63;
              carrierDiff       = 2000;
              break;

           case 1:
           default:
              dabMode          = 1;
               l                = 76;
               k                = 1536;
               t_F              = 196608;
               t_null           = 2656;
               t_s              = 2552;
               t_u              = 2048;
               t_g              = 504;
               carrierDiff      = 1000;
              break;
        }
    }
    public int  get_dabMode () { return dabMode; }
    public int  get_T_null ()   {return t_null;}
    public int  get_T_s ()  {return t_s;}
    public int  get_T_u ()  {return t_u;}
    public int  get_T_g ()  {return t_g;}
    public int get_T_F ()   {return t_F;}
    public int get_L ()     {return l;}
    public int get_carriers ()  {return k;}
    public int get_carrierDiff () {return carrierDiff;}  
}
