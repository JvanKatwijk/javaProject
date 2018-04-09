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
package utils;

public class PCMwrapper {
	private	native	long	pcmwrapperInit  ();
	private native	void	pcmwrapperStop  (long handle);
        private native  void    pcmwrapperStart (long handle);
	private native	void	pcmwrapperBuffer (long handle,
	                                          short [] buffer, int size);
        private long    handle;
        
	public	PCMwrapper () {
	   System. out. println ("going to load pcm-wrapper library");
	   handle = 0;
	   try {
	      System. load  ("/usr/local/lib/lib-pcmwrapper.so");
	      handle = pcmwrapperInit ();
	   } catch (Exception e) {
              System. out. println ("loading pcmwrapper failed");
           }
	}

	public void stopCard () {
	   if (handle != 0)
	      pcmwrapperStop (handle);
	}

        public void start () {
            if (handle != 0)
               pcmwrapperStart (handle);
        }
        
	public void addData (short [] buffer, int size) {
	   if (handle != 0)
	      pcmwrapperBuffer (handle, buffer, size);
	}
}


