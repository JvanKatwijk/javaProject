/*
 *    Copyright (C) 2014 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of the DAB java
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
package package_Model;

public	class	fibHandler {
//
// Tabelle ETSI EN 300 401 Page 50
// Table is copied from the work of Michael Hoehn
static final int PROT_LEVEL [][]   = {{16, 5,32},	// Index 0
	                              {21, 4,32},
	                              {24, 3,32},
	                              {29, 2,32},
	                              {35, 1,32},	// Index 4
	                              {24, 5,48},
	                              {29, 4,48},
	                              {35, 3,48},
	                              {42, 2,48},
	                              {52, 1,48},	// Index 9
	                              {29, 5,56},
	                              {35, 4,56},
	                              {42, 3,56},
	                              {52, 2,56},
	                              {32, 5,64},	// Index 14
	                              {42, 4,64},
	                              {48, 3,64},
	                              {58, 2,64},
	                              {70, 1,64},
	                              {40, 5,80},	// Index 19
	                              {52, 4,80},
	                              {58, 3,80},
	                              {70, 2,80},
	                              {84, 1,80},
	                              {48, 5,96},	// Index 24
	                              {58, 4,96},
	                              {70, 3,96},
	                              {84, 2,96},
	                              {104,1,96},
	                              {58, 5,112},	// Index 29
	                              {70, 4,112},
	                              {84, 3,112},
	                              {104,2,112},
	                              {64, 5,128},
	                              {84, 4,128},	// Index 34
	                              {96, 3,128},
	                              {116,2,128},
	                              {140,1,128},
	                              {80, 5,160},
	                              {104,4,160},	// Index 39
	                              {116,3,160},
	                              {140,2,160},
	                              {168,1,160},
	                              {96, 5,192},
	                              {116,4,192},	// Index 44
	                              {140,3,192},
	                              {168,2,192},
	                              {208,1,192},
	                              {116,5,224},
	                              {140,4,224},	// Index 49
	                              {168,3,224},
	                              {208,2,224},
	                              {232,1,224},
	                              {128,5,256},
	                              {168,4,256},	// Index 54
	                              {192,3,256},
	                              {232,2,256},
	                              {280,1,256},
	                              {160,5,320},
	                              {208,4,320},	// index 59
	                              {280,2,320},
	                              {192,5,384},
	                              {280,3,384},
	                              {416,1,384}};


	private class channels {
	   public boolean	inUse;
	   public int		startAddr;
	   public int		bitRate;
	   public int		length;
	   public int		protLevel;
	   public boolean	shortForm;
	   public channels () {
              inUse = false;
           }
	};

	private final channels [] subChannels = new channels [64];

	private class serviceId {
	   public boolean	inUse;
	   public int		serviceId;
	   public boolean	hasName;
	   public String	serviceLabel;
	   public short		language;
	   public short		programType;
	   public serviceId () {
              inUse             = false;
	      programType	= -1;
	      language		= -1;
           }
	};

	private final serviceId [] listofServices =
	                              new serviceId [64];

	private class serviceComponent {
	   public boolean	inUse;
	   public byte		tMID;
           public int           subChannelId;
	   public int		service;
	   public int		aSCTy;
	   public int		componentnr;
	   public byte		ps_flag;
	   public int		SCId;
	   public int		CAflag;
           public int           packetAddress;
           public int           DSCTy;
           public int           DGFlag;
	   public int		appType;
	   public int		FEC_scheme;
	   public boolean	is_madePublic;
	 
	   public serviceComponent () {
	      inUse	= false;
	      componentnr = -1;
	   }
	}

	private final RadioModel theScreen;

	private final serviceComponent [] ServiceComps =
	                               new serviceComponent [64];

	public          fibHandler      (RadioModel theGui) {
	   theScreen	= theGui;
	   ensembleIdentified	= false;
	   for (int i = 0; i < 64; i ++) {
	      listofServices [i] = new serviceId ();
	      subChannels    [i] = new channels ();
	      ServiceComps   [i] = new serviceComponent ();
	   }
        }

    /**
     *
     */
    public	void reset	() {
	   ensembleIdentified   = false;
	   for (int i = 0; i < 64; i ++) {
	      listofServices [i]. inUse = false;
	      ServiceComps   [i]. inUse = false;
	      subChannels    [i]. inUse = false;
	   }
	}

//	FIB's are segments of 256 bits. When here, we already
//	passed the crc and we start unpacking into FIGs
//	This is merely a dispatcher
	public	void	process_FIB (byte [] p, int ficno) {
int	figType;
int	processedBytes	= 0;
int	d		= 0;	// the index

	while (processedBytes  < 30) {
	   figType 		= getBits (p, d, 0, 3);
	   switch (figType) {
	      case 0:
	         process_FIG0 (p, d);	
	         break;

	      case 1:
	         process_FIG1 (p, d);
	         break;

	      case 7:
	         break;

	      default:
	         break;
	   }
	   processedBytes += getBits (p, d, 3, 5) + 1;
	   d = processedBytes * 8;
	}
}

	private void	process_FIG0 (byte [] p, int d) {
	int	extension	= getBits (p, d, 8 + 3, 5);

	   switch (extension) {
	      case 0:
	         break;

	      case 1:
	         extension1_FIG0 (p, d);
	         break;

	      case 2:
	         extension2_FIG0 (p, d);
	         break;

	      case 3:
	         extension3_FIG0 (p, d);
	         break;

	      case 13:
	         extension13_FIG0 (p, d);
	         break;

	      case 17:
	         extension17_FIG0 (p, d);
	         break;

	      default:
	         break;
	   }
	}

//	FIG0 extension 1 creates a mapping between the
//	sub channel identifications and the positions in the
//	relevant CIF.
	private void	extension1_FIG0 (byte [] p, int d) {
	int	used	= 2;		// offset in bytes
	int	length	= getBits (p, d, 3, 5);
	byte	PD_bit	= (byte)getBits (p, d, 8 + 2, 1);

	   while (used < length - 1)
	      used = handleFIG0Extension1 (p, d, used, PD_bit);
	}
//
//	defining the subchannels
	private int	handleFIG0Extension1 (byte [] p, int d,
	                                      int offset,
	                                      byte pd) {
int	bitOffset	= offset * 8;
int	subChannelId	= getBits (p, d, bitOffset, 6);
int	startAddr	= getBits (p, d, bitOffset + 6, 10);
int	tableIndex;

//	System. out. println ("fig0-1 detected");
	subChannels [subChannelId]. inUse = true;
	subChannels [subChannelId]. startAddr	= startAddr;
	if (getBits (p, d, bitOffset + 16, 1) == 0) {	// short form
	   tableIndex = getBits (p, d, bitOffset + 18, 6);
           subChannels [subChannelId]. shortForm = true;
	   subChannels [subChannelId]. length    = PROT_LEVEL [tableIndex][0];
           subChannels [subChannelId]. protLevel = PROT_LEVEL [tableIndex][1];
           subChannels [subChannelId]. bitRate   = PROT_LEVEL [tableIndex][2];
	   bitOffset += 24;
	}
	else {			// EEP long form
	   int option		= getBits (p, d, bitOffset + 17, 3);
	   int protection	= getBits (p, d, bitOffset + 20, 2);
           int subChanSize	= getBits (p, d, bitOffset + 22, 10);
	   if (option == 0) {	// A level protection
	      subChannels [subChannelId]. protLevel = protection;
              subChannels [subChannelId]. length    = subChanSize;
	      switch (protection + 1) {
	         case 1:
                    subChannels [subChannelId]. bitRate	= subChanSize / 12 * 8;
	            break;

                 case 2:
                    subChannels [subChannelId]. bitRate	= subChanSize / 8 * 8;
	            break;
	
                 case 3:
                    subChannels [subChannelId]. bitRate     = subChanSize / 6 * 8;
	            break;

	         case 4:
                    subChannels [subChannelId]. bitRate	= subChanSize / 4 * 8;
	            break;

	         default:
	            break;
	      }
	   }
	   else
	   if (option == 001) {	// B level protection
	      subChannels [subChannelId]. protLevel = protection + (1 << 2);
	      subChannels [subChannelId]. length    = subChanSize;
	      if (protection == 0)	// actually prot level 1
	         subChannels [subChannelId]. bitRate	= subChanSize / 27 * 32;
	      if (protection == 1)
	         subChannels [subChannelId]. bitRate	= subChanSize / 21 * 32;
	      if (protection == 2)
	         subChannels [subChannelId]. bitRate	= subChanSize / 18 * 32;
	      if (protection == 3)
	         subChannels [subChannelId]. bitRate	= subChanSize / 15 * 32;
	   }
           
	   bitOffset += 32;
	}
	return bitOffset / 8;	// we return bytes used
}
//
//      Service organization, 6.3.1
//      bind channels to serviceIds

	private void	extension2_FIG0 (byte [] p, int d) {
int	used    = 2;            // offset in bytes
int	length  = getBits (p, d, 3, 5);
byte	PD_bit  = (byte)(getBits (p, d, 8 + 2, 1));
byte	CN      = (byte)(getBits (p, d, 8 + 0, 1));

        while (used < length) {
           used = handleFIG0Extension2 (p, d, used, CN, PD_bit);
        }
}

//	With FIG0/2 we bind the channels to Service Ids
//
	private	int handleFIG0Extension2 (byte [] p, int d,
	                                  int offset,
	                                  byte cn,
	                                  byte pd) {
int	lOffset	= 8 * offset;
byte	ecc;
byte	cId;
int	SId;
int	nrofComponents;

	if (pd == 1) {		// long Sid
	   SId	= getBits (p, d, lOffset, 32);
	   lOffset	+= 32;
	}
	else {
	   SId	= getBits (p, d, lOffset, 16);
	   lOffset	+= 16;
	}

	nrofComponents	= (byte)getBits (p, d, lOffset + 4, 4);
	lOffset	+= 8;

	for (int i = 0; i < nrofComponents; i ++) {
	   byte	tMid	= (byte)(getBits (p, d, lOffset, 2));
	   if (tMid == 00)  {	// Audio
	      byte	aSCTy	= (byte)(getBits (p, d, lOffset + 2, 6));
	      byte	subChannelId	= (byte)(getBits (p, d, lOffset + 8, 6));
	      byte	ps_flag	= (byte)(getBits (p, d, lOffset + 14, 1));
	      bind_audioService (tMid, SId, i, subChannelId, ps_flag, aSCTy);
	   }
	   else
	   if (tMid == 3) { // MSC packet data
	      int SCId		= getBits (p, d, lOffset + 2, 12);
	      byte PS_flag	= (byte)getBits (p, d, lOffset + 14, 1);
              byte CA_flag	= (byte)getBits (p, d, lOffset + 15, 1);
              bind_packetService (tMid, SId, i, SCId, PS_flag, CA_flag);
           }

	   lOffset += 16;
	}
	return lOffset / 8;		// in Bytes
}

	private void	extension3_FIG0 (byte [] p, int d) {
int	used    = 2;            // offset in bytes
int	length  = getBits (p, d, 3, 5);

        while (used < length) {
           used = handleFIG0Extension3 (p, d, used);
        }
}

	private int	handleFIG0Extension3 (byte [] p, int d, int offset) {
int SCId            = getBits (p, d, offset * 8, 12);
int CAOrgflag       = getBits (p, d, offset * 8 + 15, 1);
int DGflag          = getBits (p, d, offset * 8 + 16, 1);
int DSCTy           = getBits (p, d, offset * 8 + 18, 6);
int SubChId         = getBits (p, d, offset * 8 + 24, 6);
int packetAddress   = getBits (p, d, offset * 8 + 30, 10);
int CAOrg;

int compIndex = find_packetComponent (SCId);
int serviceIndex;

        if (CAOrgflag == 1) {
           CAOrg = getBits (p, d, offset * 8 + 40, 16);
           offset += 16 / 8;
        }
        offset += 40 / 8;

        if (compIndex == -1)         // no serviceComponent yet
           return offset;

//	if the  Data Service Component Type == 0, we do not deal
//	with it
        if (DSCTy == 0)
           return offset;

//      We want to have the subchannel OK
        if (!subChannels [SubChId]. inUse)
           return offset;

//      If the component exists, we first look whether is
//      was already handled
        if (ServiceComps [compIndex]. is_madePublic)
           return offset;
//
        serviceIndex = ServiceComps [compIndex]. service;
        if (ServiceComps [compIndex]. componentnr == 0) { // otherwise sub component
	   final String serviceName =
	                  listofServices [serviceIndex]. serviceLabel;
           final int SId = listofServices [serviceIndex]. serviceId;
	   System. out. println ("servicename " + serviceName + " " + SId);
	   javax. swing. SwingUtilities. invokeLater (() -> {
                                   theScreen.
                                       newService (serviceName,
	                               Integer.toHexString (SId));
                                                                  });
	}

        ServiceComps [compIndex]. is_madePublic = true;
        ServiceComps [compIndex]. subChannelId = SubChId;
        ServiceComps [compIndex]. DSCTy        = DSCTy;
        ServiceComps [compIndex]. DGFlag       = DGflag;
        ServiceComps [compIndex]. packetAddress        = packetAddress;
        return offset;
}

//
//	User Application Information 6.3.6
	private void	extension13_FIG0 (byte [] p, int d) {
int	used	= 2;		// offset in bytes
int	Length	= getBits (p, d, 3, 5);
int	PD_bit	= getBits (p, d, 8 + 2, 1);

	while (used < Length) 
	   used = HandleFIG0Extension13 (p, d, used, PD_bit);
}

int	HandleFIG0Extension13 (byte [] p,
	                       int  d,
	                       int used,
	                       int pdBit) {
int	lOffset		= used * 8;
int	SId	= getBits (p, d, lOffset, pdBit == 1 ? 32 : 16);
int	SCId;
int	NoApplications;
int	appType;
int	s	= findServiceIndex (SId);
int	i;

	lOffset		+= pdBit == 1 ? 32 : 16;
	SCId		= getBits (p, d, lOffset, 4);
	NoApplications	= getBits (p, d, lOffset + 4, 4);
	lOffset += 8;

	for (i = 0; i < NoApplications; i ++) {
	   appType		= getBits (p, d, lOffset, 11);
	   int	length		= getBits (p, d, lOffset + 11, 5);
	   lOffset 		+= (11 + 5 + 8 * length);

	   int packetCompIndex	= find_packetComponent (SId);
           if (packetCompIndex != -1) 
	      ServiceComps [i]. appType = appType;
	}

	return lOffset / 8;
}

//	FEC sub-channel organization 6.2.2
	private void	FIG0Extension14 (byte [] p, int d) {
int	Length	= getBits (p, d, 3, 5);	// in Bytes
int	used	= 2;			// in Bytes
int	i;

	while (used < Length) {
	   int	SubChId	= getBits (p, d, used * 8, 6);
	   int	FEC_scheme	= getBits (p, d, used * 8 + 6, 2);
	   used = used + 1;

	   for (i = 0; i < 64; i ++) {
              if (ServiceComps [i]. subChannelId == SubChId) {
                 ServiceComps [i]. FEC_scheme = FEC_scheme;
              }
           }
	}
}

	private	void	process_FIG1 (byte [] p, int d) {
//
//	from byte 1 we deduce:
int	charSet		= getBits (p, d, 8, 4);
int	oe		= getBits (p, d, 8 + 4, 1);
int	extension	= getBits (p, d, 8 + 5, 3); 
int	SId;
int	offset;
byte []	label		= new byte [16];
StringBuilder buffer;

	if (oe == 01)
	   return;
	switch (extension) {
	   default:
	      return;

	   case 0:	// ensemble label
	      SId	= getBits (p, d, 16, 16);
	      offset	= 32;
	      if (charSet <= 16) {
	          for (int i = 0; i < 16; i ++)
	             label [i] = (byte)(getBits (p, d, offset + 8 * i, 8));
	          String str = new String (label);
	          final String xx = str;              
                  javax. swing. SwingUtilities. invokeLater (new Runnable () {
                     @Override
                     public void run () {theScreen.
	                                  updateEnsembleLabel (str, SId);}});
	          ensembleIdentified	= true;
              }
	      break;

	   case 1: {	// 16 bit Identifier field for service label
	      SId	= getBits (p, d, 16, 16);
	      offset	= 32;
	      buffer	= new StringBuilder ();
	      for (int i = 0; i < 16; i ++)
	         label [i] = (byte)(getBits (p, d, offset + 8 * i, 8));
	      
	      String str	= new String (label);
	      int myIndex	= findServiceIndex (SId);
              if ((listofServices [myIndex]. hasName) || (charSet > 16)) 
	         break;

              listofServices [myIndex]. hasName		= true;
	      listofServices [myIndex]. serviceLabel	= str;
	      ensembleIdentified	= true;
           }
	      break;

	   case 3:
	      // region label
	      break;

	   case 4:	// service component
	      break;

	   case 5: {	// 32 bit Identifier field for service label
	      SId	= getBits (p, d, 16, 32);
	      offset	= 48;
	      buffer	= new StringBuilder ();
	      for (int i = 0; i < 16; i ++)
	         label [i] = (byte)(getBits (p, d, offset + 8 * i, 8));
	      
	      String str	= new String (label);
	      int myIndex	= findServiceIndex (SId);
              if ((listofServices [myIndex]. hasName) || (charSet > 16)) 
	         break;

              listofServices [myIndex]. hasName		= true;
	      listofServices [myIndex]. serviceLabel	= str;
	      ensembleIdentified	= true;
           }
	      break;

	   case 6:	// XPAD label
	      break;
	   }
	}

	private void	extension17_FIG0 (byte [] p, int d) {
	   int     length  = getBits (p, d, 3, 5);
	   int	offset  = 16;

	   while (offset < length * 8) {
              int	SId	= getBits (p, d, offset, 16);
              boolean L_flag	= getBits (p, d, offset + 18, 1) != 0;
              boolean CC_flag	= getBits (p, d, offset + 19, 1) != 0;
              short  Language	= 0x00;     // init with unknown language
              int serviceIndex	= findServiceIndex (SId);
              if (L_flag) {                // language field present
                 Language = (short)(getBits (p, d, offset + 24, 8));
                 listofServices [serviceIndex]. language = Language;
                 offset += 8;
              }

              short type	= (short)(getBits (p, d, offset + 27, 5));
              listofServices [serviceIndex]. programType	= type;
              if (CC_flag)              // cc flag
                 offset += 40;
              else
                 offset += 32;
           }
	}


	private int findServiceIndex (int SId) {
	   for (int i = 0; i < 64; i ++) {
	      if ((listofServices [i]. inUse) &&
	                         (listofServices [i]. serviceId == SId))
	         return i;
	   }
//
//	It is not there yet, add it
	   for (int i = 0; i < 64; i ++) {
	      if (!listofServices [i]. inUse) {
	         listofServices [i]. inUse = true;
	         listofServices [i]. hasName = false;
	         listofServices [i]. serviceId = SId;
	         return i;
	      }
	   }
           return 0;
	}

//	bind_audioService is the main processor for - what the name suggests -
//	connecting the description of audioservices to a SID
	private void	bind_audioService (byte	tMid,
	                                   int	SId,
	                                   int	compnr,
	                                   int	subChId,
	                                   byte	ps_flag,
	                                   int	aSCTy) {
	int service	= findServiceIndex	(SId);
	int	firstFree	= -1;

	   if (!listofServices [service]. hasName)
	      return;

	   if (!subChannels [subChId]. inUse)
	      return;

	   for (int i = 0; i < 64; i ++) {
	      if (!ServiceComps [i]. inUse) {
	         if (firstFree == -1)
	            firstFree = i;
	         continue;
	      }
	      if ((ServiceComps [i]. service == service) &&
                  (ServiceComps [i]. componentnr == compnr))
	         return;
	   }
     
	   final String serviceName =
	                  listofServices [service]. serviceLabel;

	   javax. swing. SwingUtilities. invokeLater (new Runnable () {
	              @Override
	              public void run () {
                                   theScreen.
                                       newService (serviceName,
	                                        Integer. toHexString(SId));
                                                                  }});

	   ServiceComps [firstFree]. inUse	= true;
	   ServiceComps [firstFree]. tMID		= tMid;
	   ServiceComps [firstFree]. componentnr	= compnr;
	   ServiceComps [firstFree]. service	= service;
	   ServiceComps [firstFree]. subChannelId = subChId;
	   ServiceComps [firstFree]. ps_flag	= ps_flag;
	   ServiceComps [firstFree]. aSCTy	= aSCTy;
	}


//	bind_packetService is the main processor for - what the name suggests -
//	connecting the service component defining the service to the SId,
//	Note that the subchannel is assigned through a FIG0/3
	private	void	bind_packetService (byte tMid,
	                                    int		SId,
	                                    int		compnr,
	                                    int		SCId,
	                                    byte	ps_flag,
	                                    int		CAflag) {
	int service	= findServiceIndex      (SId);
	int firstFree	= -1;

           if (!listofServices [service]. hasName)
              return;

           for (int i = 0; i < 64; i ++) {
              if (!ServiceComps [i]. inUse) {
                 if (firstFree == -1)
                    firstFree = i;
                 continue;
              }
              if ((ServiceComps [i]. service == service) &&
                  (ServiceComps [i]. componentnr == compnr))
                 return;
           }

	   ServiceComps [firstFree]. inUse  = true;
	   ServiceComps [firstFree]. tMID   = tMid;
	   ServiceComps [firstFree]. service = service;
	   ServiceComps [firstFree]. componentnr = compnr;
	   ServiceComps [firstFree]. SCId   = SCId;
	   ServiceComps [firstFree]. ps_flag = ps_flag;
	   ServiceComps [firstFree]. CAflag = CAflag;
	   ServiceComps [firstFree]. is_madePublic = false;
	}

	private int	getBits (byte [] p, int index, int offset, int size) {
	int	res     = 0;
	   try  {
              for (int i = 0; i < size; i ++) {
	         res <<= 1;
	         res |= (p [index + offset + i]) != 0 ? 1 : 0;
	      }
	   } catch (Exception e) {
	      System. out. println ("index " + index + " offset " + offset);
	   }
	   return res;
	}

	private boolean matches (String src, String tar) {
	   if (src.equals (tar))
	      return true;
	   return tar. startsWith (src);
	}

	private int	findServiceId (String s) {
	   int serviceIndex = findServiceIndex (s);
	   if (serviceIndex != -1)
	      return listofServices [serviceIndex]. serviceId;
	   return -1;
	}

	private int	findServiceIndex (String s) {
	   for (int i = 0; i < 64; i ++) {
	      if (!listofServices [i]. inUse)
	         continue;
	      if (!listofServices [i]. hasName) 
	         continue;
	      if (matches (s, listofServices [i]. serviceLabel))
	         return i;
           }
	   return -1;
	}

	private int	findServiceComponent (String s) {
	   for (int i = 0; i < 64; i ++) {
	      if (!ServiceComps [i]. inUse)
	         continue;
	      int key = ServiceComps [i]. service;
	      if ((listofServices [key]. inUse) &&
	                      (listofServices [key]. hasName))
	         if (matches (s, listofServices [key]. serviceLabel))
	            return i;
	   }
	   return -1;
	}

	private int	findSubChannel	(int serviceComp) {
	   return ServiceComps [serviceComp].subChannelId;
	}

	public	boolean is_audioService (String s) {
	   int serviceIndex	= findServiceIndex (s);
	   if (serviceIndex == -1)
	      return false;

	   int compIndex	= findServiceComponent (s);
	   if (compIndex == -1)
	      return false;

	   return ServiceComps [compIndex]. tMID == 0;
	}

	public	boolean is_packetService (String s) {
	   int serviceIndex	= findServiceIndex (s);
	   if (serviceIndex == -1)
	      return false;

	   int compIndex	= findServiceComponent (s);
	   if (compIndex == -1)
	      return false;

	   return ServiceComps [compIndex]. tMID == 3;
	}

	public	void	audioservice_Data (String s, AudioData p) {
	   int serviceIndex	= findServiceIndex (s);
	   if (serviceIndex == -1) {
	      p. defined = false;
	      return;
	   }

	   int serviceComp	= findServiceComponent (s);
	   if (serviceComp == -1) {
	      p. defined = false;
	      return;
	   }

	   if (ServiceComps [serviceComp]. tMID != 0) {
	      p. defined = false;
	      return;
	   }

	   int subChId		= findSubChannel (serviceComp);
	   if (subChId == -1) {
	      p. defined = false;
	      return;
	   }

	   p. audioService	= true;
	   p. serviceId		= listofServices [serviceIndex]. serviceId;
	   p. subchId		= subChId;
	   p. startAddr		= subChannels [subChId]. startAddr;
	   p. length		= subChannels [subChId]. length;
	   p. shortForm		= subChannels [subChId]. shortForm;
	   p. protLevel		= subChannels [subChId]. protLevel;
	   p. bitRate		= subChannels [subChId]. bitRate;
	   p. language		= listofServices [serviceIndex]. language;
	   p. programType	= listofServices [serviceIndex]. programType;
	   p. ASCTy		= ServiceComps [serviceComp]. aSCTy;
	   p. defined		= true;
	}

	public	void	packetservice_Data (String s, PacketData p) {
	   int serviceIndex	= findServiceIndex (s);
	   if (serviceIndex == -1) {
	      p. defined = false;
	      return;
	   }

	   int serviceComp	= findServiceComponent (s);
	   if (serviceComp == -1) {
	      p. defined = false;
	      return;
	   }

	   if (ServiceComps [serviceComp]. tMID != 3) {
	      p. defined = false;
	      return;
	   }

	   int subChId		= findSubChannel (serviceComp);
	   if (subChId == -1) {
	      p. defined = false;
	      return;
	   }

	   p. audioService	= false;
	   p. serviceId		= listofServices [serviceIndex]. serviceId;
	   p. subchId		= subChId;
	   p. startAddr		= subChannels [subChId]. startAddr;
	   p. shortForm		= subChannels [subChId]. shortForm;
	   p. protLevel		= subChannels [subChId]. protLevel;
	   p. length		= subChannels [subChId]. length;
	   p. bitRate		= subChannels [subChId]. bitRate;
	   p. FEC_scheme	= ServiceComps [serviceComp]. FEC_scheme;
	   p. DSCTy		= ServiceComps [serviceComp]. DSCTy;
           p. DGflag		= ServiceComps [serviceComp]. DGFlag;
           p. packetAddress	= ServiceComps [serviceComp]. packetAddress;
           p. componentNr	= ServiceComps [serviceComp]. componentnr;
           p. appType		= ServiceComps [serviceComp]. appType;
	   p. defined		= true;
	}

	protected
	boolean	ensembleIdentified	= false;


	private int find_packetComponent (int SCId) {
           for (int i = 0; i < 64; i ++) {
              if (!ServiceComps [i]. inUse)
                 continue;
              if (ServiceComps [i]. tMID != 03)
                 continue;
              if (ServiceComps [i]. SCId == SCId)
                 return i;
           }
           return -1;
	}
}

