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

package utils;

public class FileBuffer {
	private final float [] data;
	private final int bufferSize;
	private       int capacity;
	private       int readPointer;
	private       int writePointer;

	public	FileBuffer (int size) {
        // Create a list shared by producer and consumer
	   data 	= new float [size];
	   bufferSize	= size;
	   capacity	= size;		//
	   readPointer	= 0;
	   writePointer = 0;
	}

	public void reset () {
	   synchronized (this) {
	      capacity		= bufferSize;		//
	      readPointer	= 0;
	      writePointer	= 0;
	   }
	}

	public int  Samples () {
	   return capacity;
	}

	public void putDataintoBuffer (float [] buf, int size)
	                                    throws InterruptedException {
	   synchronized (this) {
	      
	      for (int i = 0; i < size; i ++) {
	         data [writePointer ++] = (float)(buf [i]);
	         if (writePointer >= bufferSize)
	            writePointer = 0;
	         capacity --;
	      }
	   }
	}

	public int getDatafromBuffer (float [] buf, int size)
	                                throws InterruptedException {
           int realAmount = size;
	   synchronized (this) {
	      if (bufferSize - capacity < size)
	         realAmount = bufferSize - capacity;

	      for (int i = 0; i < realAmount; i ++) {
	         buf [i] = data [readPointer ++];
	         if (readPointer >= bufferSize)
	            readPointer = 0;
	      }
	      capacity += realAmount;
	   }
	   return realAmount;
	}
}
