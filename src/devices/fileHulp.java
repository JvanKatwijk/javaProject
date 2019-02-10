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
package devices;
import java.io.*;
import utils.*;

public class fileHulp extends Thread {

	private FileBuffer	theBuffer;
	private	boolean		running;
	InputStream		inStream;
	
	public		fileHulp (String fileName, FileBuffer theBuffer) {
	   this. theBuffer	= theBuffer;

	   try {
	      inStream	= new FileInputStream (fileName);
	   } catch (IOException e) {
	      e.printStackTrace();
	   }
	   start ();
	}

	public void	run 	() {
	   int segSize		= 4 * 2048;	// 4 msec of data
	   float [] buffer	= new float [2 * segSize]; // 2 msec of data
	   long nextTime	= System. nanoTime ();
	   running		= true;
	   System. out. println ("filereader starts");
	   byte [] lbuf  = new byte [2];
	   while (running) {
	      try {
	          for (int i = 0; i < segSize; i ++) {
	              inStream. read (lbuf);
	              int re = ((int)lbuf [0]) & 0xFF;
	              int im = ((int)lbuf [1]) & 0xFF;
	              buffer [2 * i]     = (float) (re - 128) / 128.0f;
	              buffer [2 * i + 1] = (float) (im - 128) / 128.0f;
	          }
	          theBuffer. putDataintoBuffer (buffer, 2 * segSize);
	      } catch (Exception e) {
	         System. out. println ("Help");
	      }
	      nextTime		= nextTime + 5000;	// is 2 msec
	      long waitingTime	= nextTime - System. nanoTime ();
	      if (waitingTime > 0) {
	         try {
//	            Thread. sleep (waitingTime / 1000,
//	                          (int)(waitingTime % 1000));
	            Thread. sleep (5);
	         } catch (Exception e) {}
	      }
	   }
	   try {
	    inStream. close ();
	   } catch (Exception e) {}
	}

	public void stopHulp	() {
	   running	= false;
	}
};
