**javaRadio**

-----------------------------------------------------------------------------

 W O R K  I N  P R O G R E S S
-----------------------------------------------------------------------------

------------------------------------------------------------------------------
Introduction
------------------------------------------------------------------------------

![javaRadio with SDRPlay as device ](/javaRadio.png?raw=true)

**javaRadio** is an implementation of the DAB software in java.
The advantage of Java
is its portability, its main disadvantage is that it runs on a VM and
needs some "native" code libraries for e.g. device handling.
The javaRadio software is modelled after, and derived from the C++ software 
in Qt-DAB and dab-cmdline. While it is work in progress, the implementation
does work pretty well.

----------------------------------------------------------------------------
Installation
----------------------------------------------------------------------------
One needs to have installed 
* a. jdk-8*  for running (compiling) the program
* b. libfaad, the faad library as well as the include files;
* c. portaudio, the library for handling the soundcard;
* d. fftw3, the library for the fft operation.
* e. libmirsdrapi-rsp, the library for the SDRplay, and/or librtlsdr, the
     library for the dabsticks, and/or libairspy, the library for the
     airspy.


-----------------------------------------------------------------------------
The use of native code
-----------------------------------------------------------------------------

In order to run, one does need some parts that are encoded as "native",
wrappers around the existing libraries mainly.

* a. wrappers for the device handlers;
* b. a wrapper around the faad library;
* c. a small library for handling MP2 frames;
* d. a wrapper around the fftw3f library (although a simple FFT class 
     written in java is part of the sources as well);
* e. a wrapper around a "spiral-based" convolutional decoder. (A simple
     deconvolutional decoder written in Java is part of the sources as well).
* f. a wrapper around the portaudio library.


The C(++) code for the wrappers is in the different subdirectories in the
directory "libraries".
These (sub)directories contain - obviously - the code, as well as a script
to generate the wrapper under Ubuntu and Stretch. However, one might have
to adapt the wrapper to point to the right include directories of the 
installed jdk.

Installation of the wrappers is in "/usr/local/lib".

-----------------------------------------------------------------------

Handling devices
------------------------------------------------------------------------

The main program will check the availability of the various devices.
Three devices are supported, one might want to exclude devices that
are not available from these checks. This is done by commenting out
the try {....}catch sections in the function "bindDevices" in the file
Javaradio.java.

-------------------------------------------------------------------------
------------------------------------------------------------------------

Compiling and running the program
-------------------------------------------------------------------------

The git repository is made from the Netbeans project directory. It contains
the sources of the program in "src".
If/when everything is in place, "cd" to the directory where the java sources
are stored.

Compilation is simply by "javac JavaRadio.java"

Running is simply by "java JavaRadio"

Running DAB then is simply by "java JavaRadio".

-----------------------------------------------------------------------------
Features
-----------------------------------------------------------------------------

The - pretty simple - GUI does not provide for a channel selector. In the
first run of the program, the program will scan all channels in the selected
band (default Band III). It will keep a list of channels where DAB signals
were found, so the subsequent times the program is run it will ONLY scan
the channels on this list.

Depending on the conditions, the interrogation of some channels may not detect
a DAB signal, where - at another time or location - the DAB channel could
have been detected. The "reset" button will cause a scan to be done
over all channels and build a new list of channels carrying detectable data.

The GUI is pretty self explanatory, basically the only things that can be
selected are the gain setting for the attached device (a slider),
and a selective. The GUI will show when scanning is complete and services
are ready to be selected.

The implementation currently supports DAB and DAB+ audio services, including
some PAD (Program Associated Data) handling (i.e. dynamic labels and images that are transmitted using PAD).
It does not support data services, labels of data services are not even shown on the GUI.

Devices that are supported are
* SDRplay,
* rtlsdr based DABsticks,
* AIRspy

------------------------------------------------------------------------------
Selecting a band
-------------------------------------------------------------------------------

While most transmissions are in Band III, in some countries the Band (and
the Mode) differ. This can be set in an "ini" file. Such an ini file
is stored in the home directory, and is named ".javaDab.ini"

Adding a line "dabBand=LBand" will cause the tuner to
be set to the L-Band, and setting "dabMode=2" will set the software to
recognize Mode 2.

-----------------------------------------------------------------------------
Todo
-------------------------------------------------------------------------------

* a. Finishing the MP2 decoding: it gives sound and the padhandling is OK, but at switching
between MP2 services, there are some issues with the sound;
* b. Use of a java encoded MP2 and Faad library;
* c. Looking into the efficiency of the FFT handling and convolutional decoder.
* d. Running the implementation on an RPI 2.

and of course, the GUI needs quite some work.

-------------------------------------------------------------------------------
Copyright
------------------------------------------------------------------------------


        Copyright (C)  2017
        Jan van Katwijk (J.vanKatwijk@gmail.com)
        Lazy Chair Computing

        The javaDab software is made available under the GPL-2.0.
        The SDR-J software, of which the javaDab software is a part,
        is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.


