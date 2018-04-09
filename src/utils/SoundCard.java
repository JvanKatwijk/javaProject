package utils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class SoundCard extends Thread {
	SourceDataLine dataline;
	private final RingBuffer audioBuffer;
	private	boolean		soundcardOK;
	private	boolean		running;
	final AudioFormat	format;

	public SoundCard () {
	   audioBuffer	= new RingBuffer (48000);
	   soundcardOK	= true;		// default
	   format = new AudioFormat ((float)48000,
	                             16,
	                             2,
	                             true,
	                             true);

	   DataLine.Info sourceInfo =
	                   new DataLine.Info (SourceDataLine.class, format);
	   for (Mixer. Info MixerInfo: AudioSystem.getMixerInfo()) {
	      try {
	         Mixer mixer = AudioSystem. getMixer (MixerInfo);
	         dataline = (SourceDataLine)mixer. getLine (sourceInfo);
	         if (dataline == null) {
	            continue; //Doesn't support this format
	         }
	         else
	            break;
	      } catch (Exception ex) {
	         continue;
	      }
	   }

	   try {
	      dataline. open (format, 512);
	      dataline .start();
	   } catch (Exception e) {
	      System. out. println (e.getMessage());
	      System. out. println ("Failure in getting soundcard");
	      soundcardOK = false;
	   }
	   running = false;
	}

	public void addData (short [] buffer, int size) {
	   if (running)
	      try {
                  audioBuffer. putDataintoBuffer (buffer, size);
           } catch (InterruptedException ex) {
               Logger.getLogger(SoundCard.class.getName()).log(Level.SEVERE, null, ex);
           }
	}
	 
	public void stopCard () {
	   running = false;
	   try {
	      this. join ();
	   } catch (Exception e) {}
	}

	@Override
        public void run () {
	   System. out. println ("soundcard is running");
	   if (!soundcardOK) 
	      return;
	   int bufSize = dataline. getBufferSize ();
	   System. out. println ("soundcard has a buffersize " + bufSize);
	   running	= true;
	   byte[] buf = new byte [bufSize];
//
	   while (running) {
              try {
	         audioBuffer. getDatafromBuffer (buf);	
	         dataline. write (buf, 0, bufSize);		// synchronous
              } catch (Exception ex) {
	             System. out. println (ex. getMessage ());
	             System. out. println ("Failure in buffer");
	      }        
	   }
	}
}

