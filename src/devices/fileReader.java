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
public class fileReader implements Device {
        private boolean         running	= false;
	private	String		fileName;
        InputStream             inStream;
	final private byte [] dataBuffer = new byte [8 * 2048];
	private	int	bufferP	= 0;
	private	long	nextTime;

	public fileReader (String name) {
	   fileName		= name;
	   running		= false;
	}

	@Override
	public	void    restartReader   (int frequency) {
	   if (running)
	      return;
	   try {
              inStream  = new FileInputStream (fileName);
	      inStream. read (dataBuffer);
	      bufferP	= 0;
	      running	= true;
	      nextTime	= System. nanoTime () + 8000;
           } catch (IOException e) {
              e.printStackTrace();
	   }
	}

        @Override
	public	void    stopReader      () {
	   if (!running)
	      return;
	   try {
            inStream. close ();
           } catch (Exception e) {}
	   running	= false;
	}
        
        @Override
        public void resetBuffer		() {
	}

	@Override
	public void setGain		(int g) {
	}

	@Override
	public	void	autoGain	(boolean b) {}

        @Override
	public	int	samples		() {
	   return 32768;
	}

        @Override
	public	int	getSamples	(float [] buffer, int amount) {
	   byte lbuf [] = new byte [2 * amount];
	   if (!running)
	      return 0;
	   try {
	      for (int i = 0; i < amount; i ++) {
	         if (bufferP >= 4 * 2048) {
	            long waitingTime = nextTime - System. nanoTime ();
	            if (waitingTime > 0) {
	               try {
	                  Thread. sleep (waitingTime / 1000,
	                                 (int)(waitingTime % 1000));
                       } catch (Exception e) {}
	            }
	            nextTime	= System. nanoTime () + 8000;
	            inStream. read (dataBuffer);
	            bufferP = 0;
	         }
	         int re = ((int)dataBuffer [2 * bufferP    ]) & 0xFF;
	         int im = ((int)dataBuffer [2 * bufferP + 1]) & 0xFF;
	         buffer [2 * i]     = ((float) (re - 128)) / 128.0f;
	         buffer [2 * i + 1] = ((float) (im - 128)) / 128.0f;
	         bufferP ++;
	      }
	   } catch (Exception e) {
	      System. out. println ("help");
	   }
	   return amount;
	}

        @Override
        public  boolean is_nullDevice   () {
            return false;
        }

        @Override
        public  boolean is_fileInput   () {
            return true;
        }
}
