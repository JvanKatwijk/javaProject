/*
 *    Copyright (C) 2015 .. 2017
 *    Jan van Katwijk (J.vanKatwijk@gmail.com)
 *    Lazy Chair Computing
 *
 *    This file is part of javaDab
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
package package_Model;
import java.util.*;
/**
  *	\class padHandler
  *	Handles the pad segments passed on from mp2- and mp4Processor
  */
	public class Padhandler {
	   private final	RadioModel	theGUI;
	   private		int	index = 0;
//	when F_PAD type = 00 and X-PAD Ind = 02 we need this table
	   private final	int []	lengthTable =
	                             {4, 6, 8, 12, 16, 24, 32, 48};
//
	   private		ArrayList<Byte> data = new ArrayList <> ();
//	dataGroupLength is set when having processed an appType 1
	   private		int dataGroupLength	= 0;
	   private		int xpadLength	= -1;
           private              int charSet;
           private              int lastApptype;
//
//	and for the shortPad we maintain
	   private		int still_to_go	= 0;
	   private		boolean lastSegment	= false;
	   private		boolean firstSegment	= false;
	   private		int	segmentNumber	= -1;

	   private		String	dynamicLabelText;
//
//	and for the "pictures" we need
	   private		boolean mscGroupElement	= false;
	   private		int	msc_dataGroupLength = 0;
	   private		ArrayList<Byte>  msc_dataGroupBuffer;
           private              byte []     shortpadData;

	private class MotElement   {
	   public int		transportId;
	   public int		bodySize;
	   public int		contentType;
	   public int		contentsubType;
	   public int		numofSegments;
	   public int		lastSegmentHandled;
	   public byte []	data;
	   public int		fillPointer;
	   public String	name;
	   private MotElement () {
	      numofSegments = 0;
	      lastSegmentHandled = 0;
	   }
	   public MotElement (int transportId,
	                      int bodySize,
	                      int contentType,
	                      int contentsubType,
	                      String Name) {
	      this. transportId = transportId;
	      this. bodySize	= bodySize;
	      this. contentType	= contentType;
	      this. contentsubType	= contentsubType;
	      this. name	= Name;
	      this. data	= new byte [bodySize];
	      this. fillPointer	= 0;
	      lastSegmentHandled	= -1;
	   }
	                       
	};

	private	final MotElement [] motList;
	private	      int	    nextMOT	= 0;

	public Padhandler (RadioModel theGUI) {
        this. msc_dataGroupBuffer = new ArrayList <> ();
           this. theGUI    = theGUI;	
	   motList	   = new MotElement [15];
	}

//	Data is stored reverse, we pass the vector and the index of the
//	last element of the XPad data.
//	 L0 is the "top" byte of the L field, L1 the next to top one.
	public void	processPAD (byte [] buffer, int last,
	                            byte  L1, byte L0) {
	   byte fpadType	= (byte)((L1 >> 6) & 03);

	   if (fpadType != 00) 
	      return;
//
//	OK, we'll try
	   byte x_padInd = (byte)((L1 >> 4) & 03);
	   byte CI_flag  = (byte)(L0 & 02);

	   switch (x_padInd) {
	      default:
	         break;

	      case  01 :
	         handle_shortPAD    (buffer, last, CI_flag);
	         break;

	      case  02:
	         handle_variablePAD (buffer, last, CI_flag);
	         break;
	   }
        }


//	Since the data is stored in reversed order, we pass
//	on the vector address and the offset of the last element
//	in that vector
	private void	handle_shortPAD (byte [] b, int last, byte CIf) {
	   if (CIf != 0) {	// has a CI flag
	      byte CI		= b [last];
	      firstSegment	= (b [last - 1] & 0x40) != 0;
	      lastSegment	= (b [last - 1] & 0x20) != 0;
	      charSet		=  b [last - 2] & 0x0F;
	      byte AcTy		= (byte)(CI & 037);	// application type

	      switch (AcTy) {
	         default:
	            break;

	         case 0:	// end marker
	            break;

	         case 2:	// start of fragment, extract the length
	            segmentNumber = b [last - 2] >> 4;
	            if (firstSegment && !lastSegment) {
	               if (dynamicLabelText. length () > 1)
	                  showLabel (dynamicLabelText);
	               dynamicLabelText = " ";
	            }
	            still_to_go     =  b [last - 1] & 0x0F;
	            data. add (b [last - 3]);
	            break;

	         case 3:	// continuation of fragment
	            for (int i = 0; (i < 3) && (still_to_go > 0); i ++) {
	               still_to_go --;
	               data. add (b [last - 1 - i]);
	            }

	            if ((still_to_go <= 0) && (data. size () > 1)) {
	               for (int i = 0; i < data. size (); i ++)
	                  dynamicLabelText +=
	                        Character. toString ((char)((byte)(data. get (i))));
                    data. clear ();
                 }
                 break;

	      }
	   }
	   else {	// No CI flag
 //	X-PAD field is all data
	      for (int i = 0; (i < 4) && (still_to_go > 0); i ++) {
	         data. add (b [last - i]);
	         still_to_go --;
	      }
//	at the end of a frame
	      if ((still_to_go <= 0) && (data. size () > 1)) {
//	just to avoid doubling by unsollicited shortpads
                 for (int k = 0; k < data. size ();k ++)
                     dynamicLabelText += Character. toString ((char)((byte)(data. get (k))));
	         data. clear ();
	       	                                        
//	if we are at the end of the last segment (and the text is not empty)
//	then show it.
	         if (!firstSegment && lastSegment) {
	            if (dynamicLabelText. length () > 0)
	               showLabel (dynamicLabelText);
	            dynamicLabelText = " ";
	         }
	      }
	   }
	}

///////////////////////////////////////////////////////////////////////
//
//	Since the data is reversed, we pass on the vector address
//	and the offset of the last element in the vector,
//	i.e. we start (downwards)  beginning at b [last];
	private void	handle_variablePAD (byte [] b,
	                                    int last, byte CI_flag) {
	   int	CI_Index = 0;
	   byte []  CI_table  = new byte [4];
	   int	base	= last;	
	   byte [] p_data;

//	If an xpadfield shows with a CI_flag == 0, and if we are
//	dealing with an msc field, the size to be taken is
//	the size of the latest xpadfield that had a CI_flag != 0
	   if (CI_flag == 0) {
	      if (mscGroupElement && (xpadLength > 0)) {
                 p_data = new byte [xpadLength];
                 for (int j = 0; j < xpadLength; j ++)
	            p_data [j] =  b [base - j];
                 add_MSC_element (p_data);
              }
	      return;
	   }
//
//	The CI flag in the F_PAD data is set, so we have local CI's
//	7.4.2.2: Contents indicators are one byte long

	      while (((b [base] & 037) != 0) && (CI_Index < 4))
	         CI_table [CI_Index ++] = b [base --];


	   if (CI_Index < 4) 	// we have a "0" indicator, adjust base
	      base -= 1;

//	The space for the CI's does belong to the Cpadfield, so
//	but do not forget to take into account the '0'field if CI_Index < 4
//	Handle the contents
	   for (int i = 0; i < CI_Index; i ++) {
	      byte	appType	= (byte)(CI_table [i] & 037);
	      int	length	= lengthTable [(CI_table [i] >> 5) & 0x7];
	      if (appType == 1) {
	         dataGroupLength = ((b [base] & 077) << 8) |
	                                   (b [base - 1] & 0xFF);
	         base -= 4;
	         lastApptype = 1;
	         continue;
	      }
//	collect data, reverse the reversed bytes
	      p_data = new byte [length];
	      for (int j = 0; j < length; j ++)  {
	            p_data [j] = b [base - j];
	      }

	      switch (appType) {
	         default:
	            return;	// sorry, we do not handle this

	         case 2:
	         case 3:
	            dynamicLabel (p_data, CI_table [i]);
	            break;

	         case 12:
	            new_MSC_element (p_data, dataGroupLength);
	            break;

	         case 13:
	            add_MSC_element (p_data);
	            break;
	      }

	      lastApptype = appType;
	      base -= length;
	      if (base < 0 && i < CI_Index - 1) {
	         System. out. println ("Hier gaat het fout, base = " + base);
	         return;
	      }
	   }
	}
//
	private		int segmentno           = 0;
	private		int remainDataLength    = 0;
	private		boolean isLastSegment   = false;
	private		boolean moreXPad	= false;
	private		int  dataLength		=  0;
//	A dynamic label is created from a sequence of (dynamic) xpad
//	fields, starting with CI = 2, continuing with CI = 3

	private void	startofSegment (byte [] data, int length) {
	   int	prefix = ((data [0] << 8) & 0xFF00) | (data [1] & 0xFF);
	   byte field_1 =  (byte)((prefix >> 8) & 017);
	   byte Cflag   =  (byte)((prefix >> 12) & 01);
	   byte first   =  (byte)((prefix >> 14) & 01);
	   byte last    =  (byte)((prefix >> 13) & 01);
	   dataLength   = length - 2; // The length with header removed

	   if (first != 0) { 
	      segmentno = 1;
	      charSet = (byte)((prefix >> 4) & 017);
	      dynamicLabelText = " ";
	   }
	   else 
	      segmentno = ((prefix >> 4) & 07) + 1;
//	segment number computed
	   if (Cflag != 0) {		// special dynamic label command
	      // the only specified command is to clear the display
	      dynamicLabelText = " ";
	   }
	   else {		// Dynamic text length
	      int totalDataLength = field_1 + 1;
	      if (length - 2 < totalDataLength) {
	         dataLength = length - 2; // the length is shortened by header
	         moreXPad   = true;
	      }
	      else {
	         dataLength = totalDataLength;  // no more xpad app's 3
	         moreXPad   = false;
	      }

//	convert dynamic label
	      for (int k = 0; k < dataLength; k ++)
	         dynamicLabelText += Character. toString ((char)data [2 + k]);

//	if at the end, show the label
	      if (last != 0) {
	         if (!moreXPad) {
	            showLabel (dynamicLabelText);
	         }
	         else
	            isLastSegment = true;
	      }
	      else 
	         isLastSegment = false;
//	calculate remaining data length
	      remainDataLength = totalDataLength - dataLength;
	   }
	}

	private void	dynamicLabel (byte [] data, byte CI) {
	   if ((CI &  037) == 02) {	// start of segment
	      startofSegment (data, data. length);
	   }
	   else 
	   if (((CI & 037) == 03) && moreXPad) {
	      if (remainDataLength > data. length) {
	         dataLength = data. length;
	         remainDataLength -= data. length;
	      }
	      else {
	         dataLength = remainDataLength;
	         moreXPad   = false;
	      }

	      for (int k = 0; k < dataLength; k ++) 
	         dynamicLabelText += Character. toString ((char)(data [k]));

	      if (!moreXPad && isLastSegment) {
	         showLabel (dynamicLabelText);
	      }
	   }
        }

	private void showLabel (String s) {
	   final String ss = s;
	   try {
	      javax. swing. SwingUtilities. invokeLater (new Runnable () {
	         @Override
	         public void run () {
	            theGUI. show_dynamicLabel (ss);}}
	      );
	   } catch (Exception e){}
	}

//
//      Called at the start of the msc datagroupfield,
//      the msc_length was given by the preceding appType "1"
	private void	new_MSC_element (byte [] data, int msc_length) {
           mscGroupElement	= true;
	   msc_dataGroupBuffer  = new ArrayList<> ();
	   for (int i = 0; i < data. length; i ++)
	      msc_dataGroupBuffer. add (data [i]);
	   msc_dataGroupLength	= msc_length;
	   show_motHandling (true);
	}
//
	private void	add_MSC_element     (byte [] newData) {
	   final int currentLength = msc_dataGroupBuffer. size ();

	   if (!mscGroupElement)
	      return;
	   for (int i = 0; i < newData. length; i ++)
	      msc_dataGroupBuffer. add (newData [i]);
           if (msc_dataGroupBuffer. size () >= msc_dataGroupLength) {
	      build_MSC_segment (msc_dataGroupBuffer, msc_dataGroupLength);
	      msc_dataGroupBuffer. clear ();
	      mscGroupElement      = false;
	      xpadLength           = -1;
	      show_motHandling (false);
	   }
	}

	private void	build_MSC_segment (ArrayList<Byte> mscdataGroup,
                                           int msc_length) {
//      we have a MOT segment, let us look what is in it
//      according to DAB 300 401 (page 37) the header (MSC data group)
//      is
	   int size    = mscdataGroup. size ();

	   int	groupType       =  mscdataGroup. get (0) & 0xF;
           int	continuityIndex = (mscdataGroup. get (1) & 0xF0) >> 4;
	   int	repetitionIndex =  mscdataGroup. get (1) & 0xF;
	   int	msc_segmentNumber   = -1;           // default
	   int	transportId     = -1;           // default
	   int  l_index;
           boolean  lastFlag    = false;

	   if ((mscdataGroup. get (0) & 0x40) != 0) {	// crc check
	      boolean res	= check_crc_bytes (mscdataGroup,
	                                           msc_length - 2);
	      if (!res) 
	         return;
	   }

	   if ((groupType != 3) && (groupType != 4))
	      return;              // do not know yet

//      extensionflag
	   boolean	extensionFlag   = (mscdataGroup. get (0) & 0x80) != 0;
//      if the segmentflag is on, then a lastflag and segmentnumber are
//      available, i.e. 2 bytes more
	   l_index		= extensionFlag ? 4 : 2;
	   boolean segmentFlag	=  (mscdataGroup. get (0) & 0x20) != 0;
	   if (segmentFlag) {
	      lastFlag		=  (mscdataGroup. get (l_index) & 0x80) != 0;
	      msc_segmentNumber	= ((mscdataGroup. get (l_index) & 0x7F) << 8) |
	                             (mscdataGroup. get (l_index + 1) & 0xFF);
	      l_index += 2;
	   }
//      if the user access flag is on there is a user accessfield
	   if ((mscdataGroup. get (0) & 0x10) != 0) {
	      int lengthIndicator = mscdataGroup. get (l_index) & 0x0F;
	      if ((mscdataGroup. get (l_index) & 0x10) != 0) { //transportid flag
	         transportId = ((mscdataGroup. get (l_index + 1) << 8) & 0xFF00) |
	                                (mscdataGroup. get (l_index + 2) & 0xFF);
	         l_index += 3;
	      }
	      else  	// sorry no transportId
                 return;
	      l_index += (lengthIndicator - 2);
	   }

	   int msc_segmentSize = ((mscdataGroup . get (l_index + 0) & 0x1F) << 8) |
	                          (mscdataGroup . get (l_index + 1) & 0xFF);

	   if ((msc_segmentNumber == 0) && (groupType == 3))
	      processHeader (transportId,
	                     mscdataGroup, l_index + 2, msc_segmentSize, lastFlag);
	   else
	   if (groupType == 4) 
	      try {
	         processSegment (transportId, 
	                         mscdataGroup,
	                         l_index + 2,
	                         msc_segmentNumber,
	                         msc_segmentSize,
	                         lastFlag);
	      } catch (Exception e) {
	        System. out. println ("processSegment needs attention");
	      }
	//	else .. we do not handle that here
	}


//      Process a regular header, i.e. a type 3
	private void    processHeader (int transportId,
                                       ArrayList<Byte> segment,
	                               int	p_index,
                                       int	segmentSize,
                                       boolean	lastFlag) {
	   int headerSize     =
                ((segment. get (p_index + 3) & 0x0F) << 9) |
	         (segment. get (p_index + 4) & 0xFF) |
	        ((segment. get (p_index + 5) >> 7) & 01);
	   int bodySize       =
                ((segment. get (p_index + 0) << 20) & 0x0FF00000) | 
                ((segment. get (p_index + 1) << 12) & 0x000FF000) |
                ((segment. get (p_index + 2) << 4 ) & 0x00000FF0) |
	        ((segment. get (p_index + 3) & 0xF0) >> 4) & 0x0000000F;
	   int contentType	= ((segment. get (p_index + 5) >> 1) & 0x3F);
	   int contentsubType	= ((segment. get (p_index + 5) & 0x01) << 8) |
	                              (segment . get (p_index + 6) & 0xFF);
	   int pointer = 7;
	   String name	= " ";

//      If we had a header with that transportId, do not do anything
           if (getHandle (transportId) >= 0) {
              return;
           }

	   while (pointer < headerSize) {
	   int PLI 	= (segment. get (p_index + pointer) & 0300) >> 6;
	   int  paramId = (segment. get (p_index + pointer) & 077);
	   int  length;
           switch (PLI) {
	      case 00:
	         pointer += 1;
	         break;

	      case 01:
	         pointer += 2;
	         break;

	      case 02:
	         pointer += 5;
	         break;

	      case 03:
	         if ((segment. get (p_index + pointer + 1) & 0200) != 0) {
	            length = (segment. get (p_index + pointer + 1) & 0177) << 8 |
	                     (segment. get (p_index + pointer + 2) & 0xFF);
	            pointer += 3;
	         }
	         else {
	            length = segment. get (p_index + pointer + 1) & 0177;
	            pointer += 2;
	         }
	         if (paramId == 12) {
	            byte [] nameText = new byte [length];
	            for (int i = 0; i < length; i ++)
	               nameText [i] = segment. get (p_index + pointer + i);
	            name = new String (nameText);
	         }
	         pointer += length;
	      }
	   }
	   newEntry (transportId,
	             bodySize, contentType, contentsubType, name);
	}
//
//      type 4 is a segment. These segments are only useful is
//      the header for the transportId is known
	private void	processSegment (int		transportId,
                                        ArrayList<Byte> bodySegment,
	                                int		p_index,
                                        int		segmentNumber,
                                        int		segmentSize,
                                        boolean		lastFlag) {
           int handleIndex = getHandle (transportId);
	   if (handleIndex < 0)
	      return;		// skip,  not yet registered

           if ((segmentNumber != motList [handleIndex]. lastSegmentHandled + 1))
              return;

	   motList [handleIndex]. lastSegmentHandled = segmentNumber;
           for (int i = 0; i <  segmentSize; i ++)
              motList [handleIndex].
	                data [motList [handleIndex]. fillPointer ++] =
	                                      bodySegment. get (p_index + i);

	   if (lastFlag) {
	      if (motList [handleIndex]. contentType != 2) 
	         return;

	      show_picture (motList [handleIndex]. data,
	                    motList [handleIndex]. contentsubType, 
	                    motList [handleIndex]. name);
	   }
	}

	private	int getHandle (int transportId) {
	   for (int i = 0; i < nextMOT; i ++)
	      if (motList [i]. transportId == transportId)
	         return i;
           return -1;
	}

	private void	newEntry (int transportId,
	                          int bodySize,
	                          int contentType,
	                          int contentsubType,
	                          String name) {

	   MotElement m = new MotElement (transportId,
	                                  bodySize,
	                                  contentType,
	                                  contentsubType,
	                                  name);
	   motList [nextMOT] = m;
	   nextMOT = (nextMOT + 1) % 15;
	}

	public boolean check_crc_bytes (ArrayList<Byte> msg,
	                                int frameSize) {
	   int	accumulator     = 0xFFFF;
	   int	crc;
	   int	genpoly         = 0x1021;

	   for (int i = 0; i < frameSize; i ++) {
              int crc_data = (msg. get (i) & 0xFF) << 8;
	      for (int j = 8; j > 0; j--) {
	         if (((crc_data ^ accumulator) & 0x8000) != 0)
	            accumulator = ((accumulator << 1) ^ genpoly) & 0xFFFF;
	         else
	            accumulator = (accumulator << 1) & 0xFFFF;
	         crc_data = (crc_data << 1) & 0xFFFF;
	      }
	   }
//
//      ok, now check with the crc that is contained
//      in the au
	   crc     = ~(((msg. get (frameSize) & 0xFF) << 8) |
	                (msg. get (frameSize + 1)) & 0xFF) & 0xFFFF;
	   return (crc ^ accumulator) == 0;
	}

	private void	show_motHandling (boolean b) {
	   final boolean f = b;

	   try {
	      javax. swing. SwingUtilities. invokeLater (new Runnable () {
	         @Override
	         public void run () {
	            theGUI. show_motHandling (f);}}
	      );
	   } catch (Exception e){}
	}

	private void	show_picture (byte [] 	data,
	                              int	subtype,
	                              String	pictureName) {
	   final byte [] ldata	= new byte [data. length];
	   final int    st	= subtype;
	   final String name	= pictureName;
	   System. arraycopy (data, 0, ldata, 0, data. length);
	   try {
	      javax. swing. SwingUtilities. invokeLater (new Runnable () {
	         @Override
	         public void run () {
	            theGUI. show_picture  (ldata, st, name);}}
	      );
	   } catch (Exception e){}
           
        }
}


