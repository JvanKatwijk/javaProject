/*
 *    Copyright (C) 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of the javaDab program
 *    javaDab is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    javaDab is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with javaDab; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package devices;

public class sdrplayDevice implements Device {
	private long handle;

	private	native	long	sdrplayInit (int freq, int gain, boolean ag);
        @Override
	public	void    restartReader   (int f) {
	   sdr_restartReader (handle, f);
	}
        @Override
	public	void    stopReader      () {
	   sdr_stopReader (handle);
	}
        @Override
	public	void    resetBuffer     () {
	   sdr_resetBuffer (handle);
	}
        @Override
	public	int	samples		() {
	   return sdr_samples (handle);
	}

        @Override
	public	int	getSamples	(float [] v, int amount) {
	   return sdr_getSamples (handle, v, amount);
	}

	@Override
	public	void	setGain		(int g) {
	   sdr_setGain (handle, g);
	}

	@Override
	public	void	autoGain	(boolean b) {
	   sdr_autoGain (handle, b);
	}

	@Override
	public	boolean	is_nullDevice	() { return false; }

	@Override
	public	boolean	is_fileInput	() { return false; }

	public	sdrplayDevice (int frequency,
	                       int gain, boolean autogain) throws Exception {
	   System. out. println ("going to load sdrplay library");
	   handle	= 0;
	   try {
              System. load  ("/usr/local/lib/libsdrplay-wrapper.so");
	      handle = sdrplayInit (frequency, gain, autogain);
           } catch (Exception | Error e) {
	      System. out. println ("loading sdrplay wrapper failed");
	      System. out. println (e. getMessage ());
	   }
           if (handle == 0)
              throw (new Exception ());
        }

	private native int	sdr_getSamples		(long handle,
	                                                 float [] v,
	                                                 int amount);
	private	native	int	sdr_samples		(long handle);
	private	native	void	sdr_resetBuffer		(long handle);
	private	native	void	sdr_restartReader	(long handle, int f);
	private	native	void	sdr_stopReader		(long handle);
	private native	void	sdr_setGain		(long handle,
	                                                 int g);
	private	native	void	sdr_autoGain		(long handle, 
	                                                 boolean b);
}
