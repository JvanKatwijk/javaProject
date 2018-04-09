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

public class RingBuffer {
	private final byte [] data;
	private final int bufferSize;
	private       int capacity;
	private       int readPointer;
	private       int writePointer;

	public	RingBuffer (int size) {
        // Create a list shared by producer and consumer
	   data 	= new byte [size];
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

//	Note. Faad delivers (pairs of short int), the java audio wants bytes, 
//	so this buffer is magic, it accepts pairs of short and output bytes
//	We ensure that we never enter more data than the buffer can handle
	public void putDataintoBuffer (short [] buf, int size)
	                                    throws InterruptedException {
	   synchronized (this) {
	      int realAmount = size / 2;	// amount of pairs
	      if (capacity / 4 < size / 2)
	         realAmount = capacity / 4;
	      for (int i = 0; i < realAmount; i ++) {
	         data [writePointer ++] =
	                         (byte)((buf [2 * i] >> 8) & 0xFF);
	         if (writePointer >= bufferSize)
	            writePointer = 0;
	         data [writePointer ++] =
	                         (byte)(buf [2 * i] & 0xFF);
	         if (writePointer >= bufferSize)
	            writePointer = 0;
	         capacity -= 2;
	         data [writePointer ++] =
	                        (byte)((buf [2 * i + 1] >> 8) & 0xFF);
	         if (writePointer >= bufferSize)
	            writePointer = 0;
	         data [writePointer ++] =
	                        (byte)(buf [2 * i + 1] & 0xFF);
	         if (writePointer >= bufferSize)
	            writePointer = 0;
	         capacity -= 2;
	      }
	   }
	}

	public void putDataintoBuffer (short [] buf)
	                                    throws InterruptedException {
	   synchronized (this) {
	      for (int i = 0; i < buf. length; i ++) {
	         data [writePointer ++] = (byte)((buf [i] >> 8) & 0xFF);
	         if (writePointer >= bufferSize)
	            writePointer = 0;
	         data [writePointer ++] = (byte)(buf [i] & 0xFF);
	         if (writePointer >= bufferSize)
	            writePointer = 0;
	         capacity -= 2;
	      }
	   }
	}

	public void getDatafromBuffer (byte [] buf)
	                                throws InterruptedException {
           int realAmount = buf. length;
	   synchronized (this) {
	      if (bufferSize - capacity < buf. length)
	         realAmount = bufferSize - capacity;

	      for (int i = 0; i < realAmount; i ++) {
	         buf [i] = data [readPointer ++];
	         if (readPointer >= bufferSize)
	            readPointer = 0;
	      }
	      capacity += realAmount;
	   }
	   for (int i = realAmount; i < buf. length; i ++) 
	      buf [i] = 0;
	}
}
