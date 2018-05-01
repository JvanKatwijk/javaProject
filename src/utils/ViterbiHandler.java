/*
 *    Copyright (C) 2014 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of DAB-java
 *
 *    DAB java is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    DAB java is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with DAB java; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package utils;

public	class ViterbiHandler {
	private long handler = 0;
	public		ViterbiHandler	(int size, boolean flag) {
	
	   try {
	      System. load  ("/usr/local/lib/libviterbi-wrapper.so");
	      handler	= initViterbi (size, flag);
	   } catch (Exception e) {
	      System. out. println ("Loading viterbi-wrapper failed");
	      System. out. println (e. getMessage ());
	      System. exit (1);
	   }
	}

	public		void	deconvolve	(int [] block,
	                                         byte [] bitBuffer) {
	   if (handler!= 0)
	      deconvolve (handler, block, bitBuffer);
	}

	private native	void	deconvolve	(long handler,
	                                         int [] block,
	                                         byte [] bitBuffer);

	private native	long	initViterbi	(int size,
	                                         boolean flag);
}

