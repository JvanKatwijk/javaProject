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

public class rtlsdrDevice implements Device {

	private long handle	= 0;
	private	native	long	rtlsdrInit	(int freq,
	                                         int gain, boolean ag);
        @Override
	public	void    restartReader   (int f) {
	   rtlsdr_restartReader (handle, f);
	}
        @Override
	public	void    stopReader      () {
	   rtlsdr_stopReader (handle);
	}
        @Override
	public	void    resetBuffer     () {
	   rtlsdr_resetBuffer (handle);
	}
        @Override
	public	int	samples		() {
	   return rtlsdr_samples (handle);
	}
        @Override
	public	int	getSamples	(float [] v, int amount) {
	   return rtlsdr_getSamples (handle, v, amount);
	}
	@Override
	public	void	setGain		(int g) {
	   rtlsdr_setGain (handle, g);
	}

	@Override
	public	void	autoGain	(boolean b) {
	   rtlsdr_autogain (handle, b);
	}

	@Override
	public	boolean	is_nullDevice	() { return false; }

	@Override
	public	boolean	is_fileInput	() { return false; }

	public	rtlsdrDevice (int frequency,
	                      int gain,
	                      boolean autogain) throws Exception {
	   try {
	      handle = 0;
	      System. load  ("/usr/local/lib/librtlsdr-wrapper.so");
	      handle = rtlsdrInit (frequency, gain, autogain);
	   } catch (Error | Exception e) {
	      System. out. println ("loading rtlsdr wrapper failed");
	      System. out. println (e. getMessage ());
	   }
	   if (handle == 0)
	      throw (new Exception ());
	}

	private native	int	rtlsdr_getSamples (long handle, float [] v,
	                                                      int amount);
	private	native	int	rtlsdr_samples	  (long handle);
	private	native	void	rtlsdr_resetBuffer	(long handle);
	private	native	void	rtlsdr_restartReader	(long handle, int freq);
	private	native	void	rtlsdr_stopReader	(long handle);
	private native	void	rtlsdr_setGain		(long handle, int g);
	private	native	void	rtlsdr_autogain		(long handle, boolean b);
}
