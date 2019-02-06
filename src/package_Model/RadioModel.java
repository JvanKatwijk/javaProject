
package package_Model;
//
//	The Model contains a basic form of the "real" software
//	It is fully unaware of the GUI. It sends signals to and
//	receives commands from the Controller.

import	devices.*;
import	java. util.*;

public class RadioModel {
	private static final String RADIO = "javaDab";
	private final	Device		my_device;
	private final	int		Mode;
	private	final	DabParams	my_params;
	private	final	ficHandler	my_ficHandler;
	private	final	PhaseReference	my_phaseReference;
	private	final	Reader		my_Reader;
	private final	DabBackend	my_Backend;

	private	DabProcessor		dabProcessor;
	private final List<modelSignals> listener = new ArrayList<>();
	public	void	addServiceListener (modelSignals service) {
	   listener. add (service);
	}

	public RadioModel (int Mode, Device my_device) {
	   this. Mode		= Mode;
           this. my_device	= my_device;

	   my_params		= new DabParams (Mode);
	   my_Backend		= new DabBackend (my_params, this);
	   my_ficHandler	= new ficHandler (my_params, this);
	   my_phaseReference	= new PhaseReference (Mode, 3, 35);
	   my_Reader		= new Reader    (my_device,
	                                         2048000, this);
	}

	private	boolean channel_active = false;
	public void selectChannel (int frequency, boolean scanning) {
	   my_device. stopReader ();
	   my_ficHandler. reset ();
	   dabProcessor	= new DabProcessor (my_params,
	                                    scanning,
	                                    my_device, 
	                                    my_ficHandler,
	                                    my_phaseReference,
	                                    my_Reader,
	                                    this);
//	restart the dabprocessor
	   dabProcessor. start ();
	   
	   my_device. restartReader (frequency * 1000);
	   channel_active	= true;
	}

	public	void	stopChannel	() {
	   if (!channel_active)
	      return;
	   dabProcessor. stopProcessor ();
	   my_device.    stopReader ();
	}
//
	public	void	setDeviceGain (int gainValue) {
	   my_device. setGain (gainValue);
	}

	private boolean autoGain = false;
	public	void	set_autoGain	() {
	   autoGain = !autoGain;
	   my_device. autoGain (autoGain);
	}
	  

//	This one is called from within the dabProcessor
//	in case no DAB signal is found
	public	void	no_signal_found	() {
	   listener. forEach ((hl) -> {
	      hl. no_signal_found ();
	   });
	}
        
        public  void    show_SNR (int snr) {
            listener. forEach ((hl) -> {
               hl. show_SNR (snr);
            });
        }

	public	void	setService (String s) {
	   dabProcessor. selectService (s);
	}

	public void newService (String s, String ch) {
	   if (dabProcessor. is_audioService (s)) {
	      AudioData as = new AudioData ();
	      dabProcessor. audioservice_Data (s, as);
	      if (!as. defined)
	         return;
              listener.forEach ((hl) -> {
                 hl. newService (s, as);
              });
	   }
	   else
	   if (dabProcessor. is_packetService (s)) {
	      PacketData ps = new PacketData ();
	      dabProcessor. packetservice_Data (s, ps);
	      if (!ps. defined)
	         return;
              listener.forEach ((hl) -> {
                 hl. newService (s, ps);
              });
	   }
	}

	public	void updateEnsembleLabel (String Name, int Sid) {
           listener. forEach((hl) -> {
                hl. ensembleName (Name, Sid);
            });
	}

	public	void	show_isStereo (byte b) {
	   listener. forEach ((hl) -> {
	        hl. show_isStereo (b != 0);
	   });
	}

	public	void	show_ficSuccess (int successRate) {
	   listener. forEach ((hl) -> {
	        hl. show_ficSuccess (successRate);
	   });
	}

	public	void	setFreqOffset	(int offset) {
	}

	public	void	show_Sync (boolean flag) {
            listener.forEach((hl) -> {
                hl. show_Sync (flag);
            });
	}

	public	void	show_picture (byte [] data, int subtype, String name) {
           listener.forEach((hl) -> {
                hl. show_picture (data, subtype, name);
            });
	}

	public	void	show_dynamicLabel (String s) {
           listener.forEach((hl) -> {
                hl. show_dynamicLabel (s);
            });
	}
	 
	public	void	show_motHandling (boolean flag) {
           listener.forEach((hl) -> {
                hl. show_motHandling (flag);
            });
	}

	public	void	show_frameErrors (int errors) {
	   listener.forEach((hl) -> {
                hl. show_frameErrors (errors);
            });
	}
	
}

