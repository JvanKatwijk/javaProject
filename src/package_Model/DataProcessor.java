
/*
 *    Copyright (C) 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Programming
 *
 *    This file is part of java DAB
 *    java DAB is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    java DAB is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with java DAB; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package package_Model;
	public class DataProcessor {
	   private int	bitRate;
	   private int	DSCTy;
	   private int	appType;
	   private int	packetAddress;
	   private int	DGflag;
	   private int	FEC_scheme;
	   public	DataProcessor	(PacketData pd) {
	      bitRate		= pd. bitRate;
              DSCTy		= pd. DSCTy;
              appType		= pd. appType;
              packetAddress	= pd. packetAddress;
              DGflag		= pd. DGflag;
              FEC_scheme	= pd. FEC_scheme;

	      System. out. println (bitRate + " " + DSCTy + " " + appType + " " + packetAddress + " " + DGflag + " " + FEC_scheme);
	   }

	   public void	addtoFrame	(byte [] v) {
	   }

	   public void	stop		() {};
}

