#
/*
 *    Copyright (C) 2013 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Programming
 *
 *    This file is part of DAB java
 *    DAB java is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    DAB java is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with DAB java; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
#include	<stdio.h>
#include	<unistd.h>
#include	<stdlib.h>
#include	<fcntl.h>
#include	<sys/time.h>
#include	<time.h>
#include	<cstring>
#include	"wavfiles.h"

static inline
int64_t		getMyTime	(void) {
struct timeval	tv;

	gettimeofday (&tv, NULL);
	return ((int64_t)tv. tv_sec * 1000000 + (int64_t)tv. tv_usec);
}

#define	__BUFFERSIZE	32 * 32768

	wavFiles::wavFiles (std::string f) {
SF_INFO *sf_info;

	fileName	= f;
	_I_Buffer	= new RingBuffer <float> (__BUFFERSIZE);

	sf_info		= (SF_INFO *)alloca (sizeof (SF_INFO));
	sf_info	-> format	= 0;
	filePointer	= sf_open (f. c_str (), SFM_READ, sf_info);
	if (filePointer == NULL) {
	   fprintf (stderr, "file %s no legitimate sound file\n", 
	                                f. c_str ());
	   throw (24);
	}
	
	if ((sf_info -> samplerate != 2048000) ||
	    (sf_info -> channels != 2)) {
	   fprintf (stderr, "This is not a recorded DAB file, sorry\n");
	   sf_close (filePointer);
	   throw (25);
	}
	currPos		= 0;
	running. store (false);
	fprintf (stderr, "file %s successfully opened\n", f. c_str ());
}

	wavFiles::~wavFiles (void) {
	if (running. load ()) {
	   running. store (false);
	   workerHandle. join ();
	}
	sf_close (filePointer);
	delete _I_Buffer;
}

bool	wavFiles::restartReader	(void) {
	fprintf (stderr, "restartReader entered met load = %d\n", running. load ());
	if (running. load ()) {
	   running. store (false);
	   workerHandle. join ();
	}
        sf_seek (filePointer, 0, SEEK_SET);
	workerHandle = std::thread (wavFiles::run, this);
	fprintf (stderr, "restartReader: thread created\n");
	running. store (true);
	return true;
}

void	wavFiles::stopReader	(void) {
	if (running. load ()) {
	   running. store (false);
           workerHandle. join ();
	}
}
//
//	size is in I/Q pairs
int32_t	wavFiles::getSamples	(float *V, int32_t requested) {
int32_t	amount;

	if (filePointer == NULL)
	   return 0;
	if (!running. load ())
	   return 0;

	while (_I_Buffer -> GetRingBufferReadAvailable () < 2 * requested)
	   usleep (100);

	amount = _I_Buffer	-> getDataFromBuffer (V, 2 * requested);
	return amount / 2;
}

int32_t	wavFiles::Samples (void) {
	return _I_Buffer -> GetRingBufferReadAvailable () / 2;
}
//
//	The actual interface to the filereader is in a separate thread

void	wavFiles::run	(wavFiles *p) {
int32_t	t, i;
int32_t	bufferSize	= 2 * 2048;
float	*bi;
int64_t	period;
int64_t	nextStop;
//
//	we (try to) read in segments accounting to 2 msec,
//	i.e. 2 * 2048 IQ samples
	fflush (stderr);
	p -> running. store (true);	
	period		= bufferSize * 1000 / 2048;	// usec
	fprintf (stderr, "Period = %ld\n", period);
	bi		= new float [2 * bufferSize];
	nextStop	= getMyTime ();
	while (p -> running. load ()) {
	   while (p -> _I_Buffer -> WriteSpace () < 2 * bufferSize) {
	      if (!p -> running. load ())
	         break;
	      usleep (100);
	   }
	   nextStop += period;
	   t = sf_readf_float (p -> filePointer, bi, bufferSize);
           if (t < bufferSize) {
              sf_seek (p -> filePointer, 0, SEEK_SET);
              fprintf (stderr, "End of file, restarting\n");
	   }

	   if (t < bufferSize) {
	      for (i = 2 * t; i < 2 * bufferSize; i ++)
	          bi [i] = 0;
	      t = bufferSize;
	   }
	   p -> _I_Buffer -> putDataIntoBuffer (bi, 2 * bufferSize);
	   if (nextStop - getMyTime () > 0)
	      usleep (nextStop - getMyTime ());
	}
	fprintf (stderr, "taak voor replay eindigt hier\n");
}

