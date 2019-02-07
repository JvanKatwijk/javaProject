
package package_Controller;
import	java. util. *;
import  java. util. Timer;
import  java. util. TimerTask;
import	utils.*;
import	package_Model.*;
import	package_View.*;

public class RadioController implements modelSignals, viewSignals {

//	The Controller needs to interact with both the Model and View.
//	It "listens" to signals from both and it issues commands to both
	private final	RadioModel	m_model;
	private final	RadioView	m_view;

	private final	List<ProgramData> services = new ArrayList<>();
	private	int			channelNumber;
	private final	Timer		timer;
        private		TimerTask	timerTask;
        private		TimerTask	selectorTask;
	private		String		currentChannel;
	private	final	BandHandler     my_bandHandler;
	private	final	Textmappers	textMapper;
	private		boolean		scanning;
	private		int		serviceCount;

	private		Properties	savedValues;
	private		int		theGain;
    //========================================================== constructor
    /** Constructor
     * @param model
     * @param view
     * @param savedValues */
	public RadioController (RadioModel	model,
	                        RadioView	view,	
	                        Properties	savedValues,
                                BandHandler     my_bandHandler) {
           m_model		= model;
           m_view		= view;
	   this. savedValues	= savedValues;
	   this. my_bandHandler	= my_bandHandler;
	   textMapper		= new Textmappers ();
	   channelNumber	= 0;
	   scanning		= true;
	   serviceCount		= 0;
	   timer		= new Timer ();
	}
//
	public	void	startRadio () {
	   m_model.	addServiceListener (this);
	   m_view. 	addServiceListener (this);
	   String gv;
           gv = savedValues. getProperty ("gainValue", "40");
	   theGain	= Integer.parseInt (gv);
	   m_view.	setDeviceGain (theGain);
	   m_model.	setDeviceGain (theGain);
	   startScanning ();
	}
//
//	startScanning can be called from the reset function to
//	generate a new list of channels with services

	@Override
	public	void	reset		() {
	   if (scanning)
	      return;
	   m_model. stopChannel		();
	   m_view. clearScreen	();

	   for (int i = 0; i < my_bandHandler. numberofItems (); i ++) 
	      savedValues. setProperty (my_bandHandler. channel (i), "true");
	   startScanning ();
	}
	
	private	void	startScanning () {
	   scanning		= true;
	   channelNumber	= 0;
	   serviceCount		= 0;
	   while (true) {
	      if (channelNumber >= my_bandHandler. numberofItems ()) {
	         readytoGo ();
	         return;
	      }
	      String h = savedValues.
	                  getProperty (my_bandHandler. channel (channelNumber),
	                              "true");
	      if (h. equals ("true"))
	         break;
	      channelNumber ++;
	   }

	   timerTask	= new TimerTask () {
	      @Override
	      public void run () {
	         handle_scannerTimeout ();
	      }
	   };
	   timer. schedule (timerTask, 10 * 1000);
	   int tunedFrequency	= my_bandHandler. Frequency (channelNumber);
	   m_model. selectChannel (tunedFrequency, true);
	   m_view.  showScanning  (my_bandHandler. channel (channelNumber));
	}

	@Override
	public	void	no_signal_found	() {
	   System. out. println ("channel number " + channelNumber);
	   savedValues.
	         setProperty (my_bandHandler. channel (channelNumber),
	                                                          "false");
	   handle_scannerTimeout ();
	}

	public	void	handle_scannerTimeout () {
	   timerTask. cancel ();
	   m_model. stopChannel ();
	   while (true) {
	      channelNumber ++;
	      if (channelNumber >= my_bandHandler. numberofItems ()) {
	         readytoGo ();
	         return;
	      }
	      String h = savedValues. getProperty (my_bandHandler.
	                               channel (channelNumber), "true");
	      if (h. equals ("true"))
	         break;
	   }

	   timerTask	= new TimerTask () {
	      @Override
	      public void run () {
	         handle_scannerTimeout ();
	      }
	   };
	   timer. schedule (timerTask, 10 * 1000);
	   int tunedFrequency	= my_bandHandler. Frequency (channelNumber);
	   m_model. selectChannel (tunedFrequency, true);
	   m_view.  showScanning  (my_bandHandler. channel (channelNumber));
	   m_view.  setEnsembleName (" ");
	} 

	@Override
	public void newService	(String s1, ProgramData p) {
	   if (!scanning)
	      return;
	   p. channel		= my_bandHandler. channel (channelNumber);
	   p. serviceName	= s1;
	   m_view. newService (s1);
	   
	   if (p instanceof PacketData) {
	      PacketData pd = (PacketData)p;
	      services. add (pd);
	   }
	   else {
	      AudioData ad = (AudioData)p;
	      services. add (ad);
	   }
	   serviceCount ++;
	   m_view. showServiceCount (serviceCount);
	}
//
//	As soon as scanning is complete, we allow selecting a service
//	Handling service selection is in two steps:
//	a. the receiver is tuned to the channel on which the service is transmitted,
//	b. within the ensemble carrier by the channel the service is selected.

	public void readytoGo () {
	   scanning	= false;
	   if (serviceCount == 0) 
	      m_view. show_noServices ();
	   else
	      m_view. showServiceEnabled (serviceCount);
	}

	@Override
	public	void	tableSelect_withLeft (String serviceName) {
	   if (scanning)
	      return;
	   int sdi	= serviceIndex (serviceName);
	   if (sdi < 0) {	// should not happen
	      System. out. println ("sorry, could not locate " + serviceName);
	      return;
	   }
	   startService (sdi);
	}

	@Override
	public void	tableSelect_withRight (String serviceName) {
	   int sdi	= serviceIndex (serviceName);
	   if (sdi < 0) {	// should not happen
	      System. out. println ("sorry, could not locate " + serviceName);
	      return;
	   }
	   show_serviceData (sdi);
	}

	@Override
	public	void	gainValue	(int val) {
	   m_model.	setDeviceGain (val);
	   savedValues. setProperty ("gainValue",
	                       Integer. toString (val));
	}

	@Override
	public	void	autogainButton	() {
	   m_model.	set_autoGain	();
	}

	private	void	show_serviceData (int index) {
	   if (services. get (index) instanceof AudioData) {
	      AudioData ad = (AudioData)(services. get (index));
	      String serviceName	= ad. serviceName;
	      String channel	= ad. channel;
	      int    startAddr	= ad. startAddr;
	      int    length	= ad. length;
	      boolean	shortForm	= ad. shortForm;
	      int    bitRate	= ad. bitRate;
	      int    protLevel	= ad. protLevel;
	      String programType	= textMapper. getProgramType (
	                            ad. programType);
	
	      m_view. show_audioData (serviceName,
	                              channel,
	                              startAddr,
	                              length,
	                              shortForm,
	                              bitRate,
	                              protLevel,
	                              programType);
	   }
	   else
	   if (services. get (index) instanceof PacketData) {
	      PacketData pd = (PacketData)(services. get (index));
	      String serviceName	= pd. serviceName;
	      String channel		= pd. channel;
	      int    startAddr		= pd. startAddr;
	      int    length		= pd. length;
	      boolean	shortForm	= pd. shortForm;
	      int    bitRate		= pd. bitRate;
	      int    protLevel		= pd. protLevel;
	      int    DSCTy		= pd. DSCTy;
	      int    FEC_scheme		= pd. FEC_scheme;
	      int    appType		= pd. appType;
	      m_view. show_packetData (serviceName,
	                               channel,
	                               startAddr,
	                               length,
	                               shortForm,
	                               bitRate,
	                               protLevel,
	                               DSCTy,
	                               FEC_scheme,
	                               appType);
	   }
	}
//
//	When starting a service, we first have to ensure that the
//	tuner is set to the right channel. If it is already, we are done,
//	if not then we tune and wait a few moments before starting the
//	service
	private	void	startService (int index) {
	   String channel	= services. get (index). channel;
	   int frequency	= my_bandHandler. Frequency (channel);
	   timerTask. cancel ();
	   if (channel. equals (currentChannel)) {
	      setService (index);
	   }
	   else {
	      currentChannel	= channel;
	      selectorTask	= new TimerTask () {
	         private final int ind = index;
	         @Override
	         public void run () {
	            setService (ind);
	         }
	      };
	      m_view. clear_dynamicLabel ();
	      m_view. showService   ("tuning to channel " + channel);
	      m_model. selectChannel (frequency, false);
	      timer. schedule (selectorTask, 10 * 1000);
	   }
	   m_view. showSelectedChannel (currentChannel);
	}

	@Override
	public void ensembleName (String s, int SId) {
	   m_view. setEnsembleName (s);
	}

	@Override
	public	void	show_Sync (boolean flag) {
	   m_view. set_Synced (flag);
	}

	public	void	show_SNR (int snr) {
	   m_view. show_SNR (snr);
	}

	@Override
	public	void	show_isStereo	(boolean b) {
	   m_view. show_isStereo (b);
	}

	@Override
	public	void	show_ficSuccess	(int successRate) {
	   m_view. show_ficSuccess (successRate);
	}

	@Override
	public	void	show_freqOffset	(int offset) {
	}

	@Override
	public	void	show_picture	(byte [] data,
	                                 int subtype, String Name) {
	   m_view. show_picture (data, subtype, Name);
	}

	@Override
	public	void	show_dynamicLabel	(String s) {
	   m_view. show_dynamicLabel (s);
	}

	@Override
	public	void	show_motHandling	(boolean flag) {
	   m_view. show_motHandling (flag);
	}

	@Override
	public	void	show_frameErrors	(int errors) {
	   m_view. show_frameErrors (errors);
	}

	public	void	setService (int index) {
	   String serviceName = services. get (index). serviceName;
	   m_view.  showService   (serviceName);
	   m_model. setService	  (serviceName);
	   show_serviceData (index);
	}

	public	int	serviceIndex (String service) {
	   for (int i = 0; i < services. size (); i ++)
	      if (services. get (i). serviceName. equals (service))
	         return i;
	   return -1;
	}
}

