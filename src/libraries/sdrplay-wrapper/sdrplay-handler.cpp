#
/*
 *    Copyright (C) 2014 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Programming
 *
 *    This file is part of the DAB library
 *
 *    DAB library is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    DAB library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with DAB library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include	"sdrplay-handler.h"

#define	DEFAULT_GAIN	40

	sdrplayHandler::sdrplayHandler  (int32_t	frequency,
	                                 int16_t	ppm,
	                                 int16_t	gain) {
int	err;
float	ver;
mir_sdr_DeviceT devDesc [4];

	this	-> inputRate		= 2048000;
        this    -> frequency            = frequency;
        this    -> ppmCorrection        = ppmCorrection;
        this    -> theGain              = 20 + gain * 39 / 100;
        this    -> deviceIndex          = 0;
        this    -> agcMode		= false;

	_I_Buffer	= NULL;
	err		= mir_sdr_ApiVersion (&ver);
	if (ver < 2.13) {
	   fprintf (stderr, "sorry, library too old\n");
	   throw (24);
	}

	mir_sdr_GetDevices (devDesc, &numofDevs, uint32_t (4));
	if (numofDevs == 0) {
	   fprintf (stderr, "Sorry, no device found\n");
	   throw (25);
	}

	if (deviceIndex >= numofDevs)
	   this -> deviceIndex = 0;
	hwVersion = devDesc [deviceIndex]. hwVer;
	fprintf (stderr, "sdrdevice found = %s, hw Version = %d\n",
	                              devDesc [deviceIndex]. SerNo, hwVersion);
	mir_sdr_SetDeviceIdx (deviceIndex);

	if (hwVersion == 2) 
	   err = mir_sdr_RSPII_AntennaControl (mir_sdr_RSPII_ANTENNA_A);

	if (hwVersion == 255) {
	   nrBits	= 14;
	   denominator	= 8192.0;
	}
        else {
           nrBits	= 12;
	   denominator	= 1024.0;
	}

	_I_Buffer	= new RingBuffer<std::complex<float>>(8 * 1024 * 1024);
//
	mir_sdr_RSP_SetGr (theGain, 3, 1, 0);
	running. store (false);
}

	sdrplayHandler::~sdrplayHandler	(void) {
	stopReader ();
	if (numofDevs > 0)
	   mir_sdr_ReleaseDeviceIdx ();
	if (_I_Buffer != NULL)
	   delete _I_Buffer;
}
//
#define	kHz(x)	(x * 1000)
#define	MHz(x)	(kHz (x) * 1000)
//
//	For the setting of gain, not using a widget, we map the
//	gain value upon an attenation value and set setexternal Gain
void	sdrplayHandler::setGain		(int32_t g) {
	theGain		= 20 + g * 39 / 100;
	fprintf (stderr, "gain reduction set to %d\n", theGain);
	mir_sdr_RSP_SetGr (theGain, 3, 1, 0);
}

void	sdrplayHandler::set_autogain	(bool agcMode) {
	this	-> agcMode = agcMode;
	fprintf (stderr, "autogain set %s\n", agcMode ? "on" : "off");
	mir_sdr_AgcControl (agcMode ? mir_sdr_AGC_100HZ :
                                      mir_sdr_AGC_DISABLE,
                            -30, 0, 0, 0, 0, 3);
}

static
void myStreamCallback (int16_t		*xi,
	               int16_t		*xq,
	               uint32_t		firstSampleNum, 
	               int32_t		grChanged,
	               int32_t		rfChanged,
	               int32_t		fsChanged,
	               uint32_t		numSamples,
	               uint32_t		reset,
                       uint32_t		hwRemoved,
	               void		*cbContext) {
int16_t	i;
sdrplayHandler	*p	= static_cast<sdrplayHandler *> (cbContext);
std::complex<float> localBuf [numSamples];
float	denominator	= (float)(p -> denominator);

	for (i = 0; i <  (int)numSamples; i ++) {
	   localBuf [i] = std::complex<float> (float (xi [i]) / denominator,
	                                       float (xq [i]) / denominator);
	}
	p -> _I_Buffer -> putDataIntoBuffer (localBuf, numSamples);
	(void)	firstSampleNum;
	(void)	grChanged;
	(void)	rfChanged;
	(void)	fsChanged;
	(void)	reset;
}

void	myGainChangeCallback (uint32_t	gRdB,
	                      uint32_t	lnaGRdB,
	                      void	*cbContext) {
	(void)gRdB;
	(void)lnaGRdB;	
	(void)cbContext;
}

bool	sdrplayHandler::restartReader	(int frequency) {
int	gRdBSystem;
int	samplesPerPacket;
mir_sdr_ErrT	err;
int	localGRed	= theGain;

	if (running. load ())
	   return true;

	_I_Buffer	->  FlushRingBuffer ();
	fprintf (stderr, "frequency = %d localGred = %d\n", frequency, localGRed);
	err	= mir_sdr_StreamInit (&localGRed,
	                              double (inputRate) / MHz (1),
	                              double (frequency) / MHz (1),
	                              mir_sdr_BW_1_536,
	                              mir_sdr_IF_Zero,
	                              3,	// lnaEnable do not know yet
	                              &gRdBSystem,
	                              mir_sdr_USE_RSP_SET_GR,
	                              &samplesPerPacket,
	                              (mir_sdr_StreamCallback_t)myStreamCallback,
	                              (mir_sdr_GainChangeCallback_t)myGainChangeCallback,
	                              this);
	if (err != mir_sdr_Success) {
	   fprintf (stderr, "Error %d on streamInit\n", err);
	   return false;
	}

//	mir_sdr_SetPpm       ((float)ppmCorrection);
        err             = mir_sdr_SetDcMode (4, 1);
        err             = mir_sdr_SetDcTrackTime (63);
	if (agcMode) 
	   mir_sdr_AgcControl (mir_sdr_AGC_100HZ,
                               -30,
                               0, 0, 0, 0, 4);

        running. store (true);
        return true;
}

void	sdrplayHandler::stopReader	(void) {
	if (!running. load ())
	   return;

	mir_sdr_StreamUninit	();
	running. store (false);
}

//	Note that the sdrPlay returns 12/14 bit values
int32_t	sdrplayHandler::getSamples (std::complex<float> *V, int32_t size) { 
int	i;
int	amount;

	amount = _I_Buffer -> getDataFromBuffer (V, size);
	if (size != amount)
	   fprintf (stderr, "gevraagd %d, gekregen %d\n", size, amount);
	return amount;
}

int32_t	sdrplayHandler::Samples	(void) {
	return _I_Buffer	-> GetRingBufferReadAvailable ();
}

void	sdrplayHandler::resetBuffer	(void) {
	_I_Buffer	-> FlushRingBuffer ();
}

