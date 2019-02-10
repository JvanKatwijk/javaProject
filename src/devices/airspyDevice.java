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

public class airspyDevice implements Device {

	private long handle	= 0;
	private	native	long	airspyInit	(int freq,
	                                         int gain, boolean ag);
        @Override
	public	void    restartReader   (int f) {
	   airspy_restartReader (handle, f);
	}
        @Override
	public	void    stopReader      () {
	   airspy_stopReader (handle);
	}
        @Override
	public	void    resetBuffer     () {
	   airspy_resetBuffer (handle);
	}
        @Override
	public	int	samples		() {
	   return airspy_samples (handle);
	}
        @Override
	public	int	getSamples	(float [] v, int amount) {
	   return airspy_getSamples (handle, v, amount);
	}
	@Override
	public	void	setGain		(int g) {
	   airspy_setGain (handle, g);
	}

	@Override
	public	void	autoGain	(boolean b) {
	}

	public	airspyDevice (int frequency,
	                      int gain,
	                      boolean autogain) throws Exception {
	   handle = 0;
	   try {
              System. load  ("/usr/local/lib/libairspy-wrapper.so");
	      handle = airspyInit (frequency, gain, autogain);
           } catch (Exception | Error e) {
	      System. out. println ("loading airpsy-wrapper failed");
	      System. out. println (e. getMessage ());
	   }
           if (handle == 0)
              throw (new Exception ());
	}
        
        @Override
	public	boolean	is_nullDevice () { return false; }

	@Override
	public	boolean	is_fileInput () { return false; }

	private native	int	airspy_getSamples (long handle, float [] v,
	                                                      int amount);
	private	native	int	airspy_samples	        (long handle);
	private	native	void	airspy_resetBuffer	(long handle);
	private	native	void	airspy_restartReader	(long handle, int freq);
	private	native	void	airspy_stopReader	(long handle);
	private native	void	airspy_setGain		(long handle, int g);
}
