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
package package_Model;

public	class DabBackend {
	final	int	CU_SIZE		= 4 * 16;
		int []	cifVector	= new int [55296];
		int	cifCount	= 0;    // msc blocks in CIF
		int	blkCount	= 0;
		DabVirtual dabHandler   = new DabVirtual ();
		boolean	work_to_be_done = false;
	final	DabParams	params;
	final	int		mode;
        final	int		bitsperBlock;
	 	int		numberofblocksperCIF;
	final	RadioModel	theGUI;
	final	ProgramData	pd	= new ProgramData ();

        public	DabBackend (DabParams p, RadioModel theScreen) {
	   params	= p;
	   theGUI	= theScreen;
	   mode		= params. get_dabMode ();
           bitsperBlock = 2 * params. get_carriers ();
	   switch (mode) {
	      case 4:	// 2 CIFS per 76 blocks
	         numberofblocksperCIF	= 36;
	         break;

	      case 1:	// 4 CIFS per 76 blocks
	         numberofblocksperCIF	= 18;
	         break;

	      case 2:	// 1 CIF per 76 blocks
	         numberofblocksperCIF   = 72;
	         break;

	      default:	// should not happen
	         numberofblocksperCIF   = 18;
	      break;
	   }
	System. out. println ("en tot hier");
	}

//      Any change in the selected service will only be active
//      in the next call to process_mscBlock.
	public	void	process_mscBlock (int  [] fbits, int blkno) {
	int	currentblk;
        int     myBegin;

	    if (!work_to_be_done)
	       return;

	    currentblk	= (blkno - 4) % numberofblocksperCIF;
	    System. arraycopy (fbits, 0, 
	                         cifVector, currentblk * bitsperBlock,
	                         bitsperBlock);
	    if (currentblk < numberofblocksperCIF - 1)
	       return;

//	OK, now we have a full CIF
	    blkCount	= 0;
	    cifCount	= (cifCount + 1) & 03;
//
//	This might be a dummy handler
	    try {
                synchronized (this) {
	           myBegin	= pd.startAddr * CU_SIZE;
	           dabHandler. process (cifVector,
	                                myBegin,
	                                pd. length * CU_SIZE);
                }
	    } catch (Exception e) {
	       System. out. println (e. getMessage ());
	       System. out. println ("Dabhandler fails");
	    }
	}

	public void	setAudioChannel (ProgramData d) {
//	In the (may be far) future we might want the processing
//	of the data to be done in a separate thread, so we first
//	copy the program data
	   synchronized (this) {
	      dabHandler. stopRunning ();
	      work_to_be_done	= false;
	      pd. shortForm	= d. shortForm;
	      pd. startAddr	= d. startAddr;
	      pd. length	= d. length;
	      pd. protLevel	= d. protLevel;
	      pd. bitRate	= d. bitRate;
	      pd. ASCTy		= d. ASCTy;
	      dabHandler	= new DabAudio (pd, theGUI);
	      work_to_be_done	= true;
	   }
	   
	   System. out. print ("setting channel startaddress " + d. startAddr);
	   System. out. print (" length = " + d. length + " bitRate " + d. bitRate);
	   System. out. println (" + protLevel " + d. protLevel);
	}

	public void reset      () {
	   work_to_be_done = false;
	}
}

