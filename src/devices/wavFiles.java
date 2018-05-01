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

public class wavFiles implements Device {
	private long    handle;
	private	int	frequency	= 220000000;

	private	native	long	fileOpen (String fileName);
        @Override
	public	void    restartReader   () {
	   wav_restartReader (handle);
	}
        @Override
	public	void    stopReader      () {
	   wav_stopReader (handle);
	}
        
        @Override
        public  int    bitDepth		() {
            return 12;
        }

        @Override
        public void resetBuffer		() {}
        
        @Override
        public void setVFOFrequency	(int newFrequency) {
	   frequency	= newFrequency;
	}

        @Override
        public int getVFOFrequency	() {
	   return frequency;
	}

	@Override
	public void setGain		(int g) {
	}

	@Override
	public	void	autoGain	(boolean b) {}
        @Override
	public	int	samples		() {
	   return wav_samples (handle);
	}

        @Override
	public	int	getSamples	(float [] v, int amount) {
	   return wav_getSamples (handle, v, amount);
	}
        @Override
        public  boolean is_nullDevice   () {
            return false;
        }
        
	public	wavFiles (String fileName) {
	   System. out. println ("going to load wavfiles library");
	   handle = 0;
	   try {
	      System. load  ("/usr/local/lib/libwav-wrapper.so");
	      handle = fileOpen (fileName);
           } catch (Exception | Error e) {
	      System. out. println ("loading wav wrapper failed");
	      System. out. println (e. getMessage ());
	   }

	}
	private native	int	wav_getSamples		(long handle,
	                                                 float [] v,
	                                                 int amount);
	private	native	int	wav_samples		(long handle);
	private	native	void	wav_restartReader	(long handle);
	private	native	void	wav_stopReader		(long handle);
}
