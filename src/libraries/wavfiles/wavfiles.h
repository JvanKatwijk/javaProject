#
/*
 *    Copyright (C) 2013 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
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
#ifndef	__WAV_FILES__
#define	__WAV_FILES__

#include	<sndfile.h>
#include        "ringbuffer.h"
#include	<thread>
#include	<atomic>

class	wavFiles {
public:
			wavFiles	(std::string);
	       		~wavFiles	(void);
	int32_t		getSamples	(float *, int32_t);
	uint8_t		myIdentity	(void);
	int32_t		Samples		(void);
	bool		restartReader	(void);
	void		stopReader	(void);
	
private:
	std::string	fileName;
static	void		run		(wavFiles *);
	int32_t		readBuffer	(float *, int32_t);
	RingBuffer<float>	*_I_Buffer;
	std::thread     workerHandle;
	int32_t		bufferSize;
	SNDFILE		*filePointer;
	std::atomic<bool> running;
	int64_t		currPos;
};

#endif

