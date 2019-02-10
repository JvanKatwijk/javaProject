
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

public class nullDevice implements Device {
        @Override
	public	void    restartReader   (int f)	{}
        @Override
	public	void    stopReader      ()	{}
        @Override
	public	void    resetBuffer     ()	{}
        @Override
	public	int	samples		()	{return 0;}
        @Override
	public	int	getSamples	(float [] v, int amount) {
	   return 0;
	}
        @Override
        public  void    setGain         (int g) {}
	@Override
	public	void	autoGain	(boolean b) {}
	@Override
	public	boolean	is_nullDevice	()	{ return true; }
	public	boolean	is_fileInput	()	{ return false; }
}
