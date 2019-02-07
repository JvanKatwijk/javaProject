package package_View;
import	java.awt.*;
import	javax.swing.*;
import	javax.swing.JOptionPane;
import	javax.swing.JPanel;
import	java.awt.event.*;
import	java.io.ByteArrayInputStream;
import  java.util.*;
import  javax.imageio.ImageIO;
import  javax.imageio.ImageReadParam;
import  javax.imageio.ImageReader;
import  javax.imageio.stream.ImageInputStream;
import	javax.swing.event.*;
import	javax. swing. JSlider;
import	java. awt. Dimension;

public class RadioView extends JFrame {
    
    //... Components
	private final	JLabel	m_timesync	= new JLabel ("sync ");
	private final	JLabel	m_stereo	= new JLabel ("st");
	private final	JLabel	m_motLabel	= new JLabel ("  ");
	private final	JLabel	m_dynamicLabel	= new JLabel ("                     ");
	private	final	JSlider	m_gainSlider	= new JSlider ();
	private	final	JButton	m_resetButton	= new JButton ("reset");
	private	final	JButton	m_autogainButton	= new JButton ("auto");
	private	final	JLabel	m_gainLabel	= new JLabel ("50");

	private final	JLabel m_ficLevel_1	= new JLabel ("  ");
	private final	JLabel m_ficLevel_2	= new JLabel ("  ");
	private final	JLabel m_ficLevel_3	= new JLabel ("  ");
	private final	JLabel m_ficLevel_4	= new JLabel ("  ");
	private final	JLabel m_ficLevel_5	= new JLabel ("  ");

	private final	JLabel m_ensembleLabel	= new JLabel ("ensemble");
	private final	JLabel m_snrLabel	= new JLabel ("    ");
	private final	JLabel m_selectedService= new JLabel ("wait for services");
	private final	JLabel m_channelLabel	= new JLabel ("          ");
	private	final	JLabel m_serviceCount	= new JLabel ("  ");
	public serviceTable m_serviceTable	= new serviceTable ();
	private java.util.List<viewSignals> listeners = new ArrayList<>();
        public  void    addServiceListener (viewSignals service) {
           listeners. add (service);
        }
    
    //======================================================= constructor
    /** Constructor */
	public RadioView () {
        //... Set up the logic
	   m_selectedService. setToolTipText ("the name of the selected service");
	   m_timesync. setToolTipText ("Green indicates time sync OK");
	   m_stereo. setToolTipText ("Green indicates transmission is stereo");

	   m_resetButton. setBackground (Color. red);
	   m_resetButton. setOpaque (true);
	   m_autogainButton.  setBackground	(Color. green);
	   m_autogainButton.  setOpaque (true);
	   m_timesync.    setBackground (Color. red);
	   m_timesync.    setOpaque (true);
	   m_stereo.      setBackground (Color. red);
	   m_stereo.      setOpaque (true);
	   m_motLabel.    setBackground (Color. red);
	   m_motLabel.    setOpaque (true);
	   m_selectedService. setBackground (Color. red);
	   m_selectedService. setOpaque (true);
	   m_gainSlider.   setPreferredSize(new Dimension(30, 60));
	   m_serviceCount.  setToolTipText ("number of services detected");
	   m_ficLevel_1.   setToolTipText ("green shows that the FIC information can be decoded");
    //... Layout the components.      
           JPanel row1 = new JPanel();
	   row1. setLayout (new FlowLayout ());
	   row1. add (m_timesync);
	   row1. add (m_stereo);
	   row1. add (Box. createRigidArea (new Dimension (10, 0)));
	   row1. add (m_motLabel);
	   row1. add (Box. createRigidArea (new Dimension (10, 0)));
	   row1. add (m_serviceCount);
	   row1. add (Box. createRigidArea (new Dimension (10, 0)));
	   row1. add (m_resetButton);
	   m_ficLevel_1. setBackground (Color. red);
	   m_ficLevel_1. setOpaque (true);
	   m_ficLevel_2. setBackground (Color. red);
	   m_ficLevel_2. setOpaque (true);
	   m_ficLevel_3. setBackground (Color. red);
	   m_ficLevel_3. setOpaque (true);
	   m_ficLevel_4. setBackground (Color. red);
	   m_ficLevel_4. setOpaque (true);
	   m_ficLevel_5. setBackground (Color. red);
	   m_ficLevel_5. setOpaque (true);

	   JPanel row2 = new JPanel ();
	   row2.setLayout(new BoxLayout (row2, BoxLayout. X_AXIS));
	   row2. add (m_channelLabel);
	   row2. add (Box. createRigidArea (new Dimension (50, 0)));
	   row2. add (m_ficLevel_1);
	   row2. add (Box. createRigidArea (new Dimension (5, 0)));
	   row2. add (m_ficLevel_2);
	   row2. add (Box. createRigidArea (new Dimension (5, 0)));
	   row2. add (m_ficLevel_3);
	   row2. add (Box. createRigidArea (new Dimension (5, 0)));
	   row2. add (m_ficLevel_4);
	   row2. add (Box. createRigidArea (new Dimension (5, 0)));
	   row2. add (m_ficLevel_5);

	   JPanel row3	= new JPanel ();
	   row3. setLayout (new FlowLayout ());
	   row3. add (m_ensembleLabel);
	   row3. add (Box. createRigidArea (new Dimension (5, 0)));
	   row3. add (m_snrLabel);

	   JPanel row4	= new JPanel ();
	   row4. setLayout (new FlowLayout ());
	   row4. add (m_selectedService);
	   row4. add (Box. createRigidArea (new Dimension (100, 0)));

	   JPanel row5	= new JPanel ();
	   row5. setLayout (new FlowLayout ());
	   row5. add (m_dynamicLabel);
	   row5. add (Box. createRigidArea (new Dimension (100, 0)));

	   JPanel leftpart	= new JPanel ();
	   leftpart. setLayout (new BoxLayout (leftpart, BoxLayout. Y_AXIS));
	   leftpart. add (row1);
	   leftpart. add (row2);
	   leftpart. add (row3);
	   leftpart. add (row4);

	   JPanel frame		= new JPanel ();
	   frame. setLayout (new BoxLayout (frame, BoxLayout. X_AXIS));
	   frame. add (leftpart);
	   JPanel rightpart	= new JPanel ();
	   rightpart. setLayout (new BoxLayout (rightpart, BoxLayout. Y_AXIS));
	   m_gainSlider. setOrientation (JSlider. VERTICAL);
	   m_gainSlider. setToolTipText ("The gain for the device, range 1 .. 100");
	   rightpart. add (m_autogainButton);
	   rightpart. add (m_gainSlider);
	   rightpart. add (m_gainLabel);
	   frame.     add (rightpart);
	   frame.     add (Box. createRigidArea (new Dimension (40, 0)));
	   JPanel gui		= new JPanel ();
	   gui. setLayout (new BoxLayout (gui, BoxLayout. Y_AXIS));
	   gui. add (frame);
	   gui. add (row5);
	   //... finalize layout
	   this. setContentPane (gui);
	   this. pack ();
        
	   this. setTitle("Jan's JavaRadio - MVC");
	   // The window closing event should probably be passed to the 
	   // Controller in a real program, but this is a short example.
//	   this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	   m_serviceTable. table. addMouseListener (new MouseAdapter() {
              @Override
              public void mouseClicked (MouseEvent evt) {
                 if (evt. getClickCount() > 0) {
                    int row     = m_serviceTable. table. rowAtPoint (evt. getPoint ());
                    Object o    = m_serviceTable. table. getValueAt (row, 0);
                    if (!(o instanceof String))
                       return;
                    if (!javax. swing. SwingUtilities. isRightMouseButton (evt))
	               tableSelect_withLeft ((String)o);
                    else
	               tableSelect_withRight ((String)o);
	         }
	      }
	   });

	   m_gainSlider.
	      addChangeListener (new ChangeListener() {
                 @Override
	         public void
	               stateChanged(javax. swing.event.ChangeEvent evt) {
	            int gainValue = ((JSlider)evt. getSource ()). getValue ();
	            m_gainLabel. setText (Integer. toString (gainValue));
	            listeners. forEach ((hl) -> {
	               hl.  gainValue (gainValue);
	             });
	         }
           });

	   m_resetButton.
	      addActionListener ((ActionEvent evt) -> {
                  listeners. forEach ((hl) -> {
                      hl. reset ();
                  });
           });

	   m_autogainButton.
	      addActionListener ((ActionEvent evt) -> {
                  listeners. forEach ((hl) -> {
                      hl. autogainButton ();
                  });
           });
	}

	private	void	tableSelect_withLeft (String name) {
	   listeners.forEach((hl) -> {
              hl. tableSelect_withLeft (name);
           });
        }

	private	void	tableSelect_withRight (String name) {
	   listeners.forEach((hl) -> {
              hl. tableSelect_withRight (name);
           });
        }

	public void set_qualityLabel (JLabel l, int v) {
	   if (v > 0) {
	      l. setBackground (Color. green);
	      l. setOpaque (true);
	   }
	   else {
	      l. setBackground (Color. red);
	      l. setOpaque (true);
	   }
	}

	public	void	set_stereo (boolean b) {
	   set_qualityLabel (m_stereo, b ? 1 : 0);
	}

	public	void	set_Synced (boolean b) {
	   set_qualityLabel (m_timesync, b ? 1 : 0);
	}

	public	void	show_SNR (int snr) {
	   m_snrLabel. setText ("SNR = " + snr);
	}

	public	String	get_serviceName (int row, int column) {
	   return m_serviceTable. get_serviceName (row, column);
	}

	public	void	newService (String name) {
	   m_serviceTable. newService (name);
	}

	public	void	setEnsembleName	(String s) {
	   m_ensembleLabel. setText (s);
	}

	public	void	show_isStereo	(boolean b) {
	   if (b)
	      m_stereo  . setBackground (Color. green);
	   else
	      m_stereo  . setBackground (Color. green);
	   m_stereo  . setOpaque (true);
	}

//	q is a value 0 .. 100
	public void show_ficSuccess (int q) {
	switch ((q + 10) / 20) {
	   case 0:
	      set_qualityLabel (m_ficLevel_1, 0);
	      set_qualityLabel (m_ficLevel_2, 0);
	      set_qualityLabel (m_ficLevel_3, 0);
	      set_qualityLabel (m_ficLevel_4, 0);
	      set_qualityLabel (m_ficLevel_5, 0);
	      break;

	   case 1:
	      set_qualityLabel (m_ficLevel_1, 1);
	      set_qualityLabel (m_ficLevel_2, 0);
	      set_qualityLabel (m_ficLevel_3, 0);
	      set_qualityLabel (m_ficLevel_4, 0);
	      set_qualityLabel (m_ficLevel_5, 0);
	      break;

	   case 2:
	      set_qualityLabel (m_ficLevel_1, 1);
	      set_qualityLabel (m_ficLevel_2, 1);
	      set_qualityLabel (m_ficLevel_3, 0);
	      set_qualityLabel (m_ficLevel_4, 0);
	      set_qualityLabel (m_ficLevel_5, 0);
	      break;

	   case 3:
	      set_qualityLabel (m_ficLevel_1, 1);
	      set_qualityLabel (m_ficLevel_2, 1);
	      set_qualityLabel (m_ficLevel_3, 1);
	      set_qualityLabel (m_ficLevel_4, 0);
	      set_qualityLabel (m_ficLevel_5, 0);
	      break;

	   case 4:
	      set_qualityLabel (m_ficLevel_1, 1);
	      set_qualityLabel (m_ficLevel_2, 1);
	      set_qualityLabel (m_ficLevel_3, 1);
	      set_qualityLabel (m_ficLevel_4, 1);
	      set_qualityLabel (m_ficLevel_5, 0);
	      break;

	   case 5:
	      set_qualityLabel (m_ficLevel_1, 1);
	      set_qualityLabel (m_ficLevel_2, 1);
	      set_qualityLabel (m_ficLevel_3, 1);
	      set_qualityLabel (m_ficLevel_4, 1);
	      set_qualityLabel (m_ficLevel_5, 1);
	      break;
	   }
	}

	public	void	showScanning	(String channel) {
	   m_channelLabel. setText ("Scanning " + channel);
	}

	public	void	showSelectedChannel (String channel) {
	   m_channelLabel. setText ("Selected " + channel);
	}

	public	void	showService         (String currentService) {
	   m_selectedService. setText (currentService);
	   m_selectedService. setBackground (Color. green);
	   m_selectedService. setOpaque (true);
	}

	JFrame	warningFrame	= new JFrame ();
	public	void	show_noServices	() {
	   JPanel panel	= new JPanel ();
	   JOptionPane. showMessageDialog (panel,
	                                   "No services detected", 
	                                   "Warning",
	                                   JOptionPane. WARNING_MESSAGE);
	   m_resetButton. setBackground (Color. green);
	   m_resetButton. setOpaque (true);
	}

	public	void	showServiceEnabled (int numofServices) {
	   m_selectedService. setText ("please choose a service");
	   m_selectedService. setBackground (Color. green);
	   m_selectedService. setOpaque (true);
	   showServiceCount (numofServices);
	   m_resetButton. setBackground (Color. green);
	   m_resetButton. setOpaque (true);
	}

	public	void	showServiceCount	(int numofServices) {
	   m_serviceCount.    setBackground (Color. green);
           m_serviceCount.    setOpaque (true);
           m_serviceCount.    setText (Integer. toString (numofServices));
        }

	JFrame	notifier	= new JFrame ();
	JLabel	Label_6		= new JLabel ();
	public	void	show_audioData (String		serviceName,
	                                 String		channel,
	                                 int		startAddr,
	                                 int		length,
	                                 boolean	shortForm,
	                                 int		bitRate,
	                                 int 		protLevel,
	                                 String		programType) {
	   notifier. setPreferredSize (new Dimension (200, 200));
	   JPanel	main	= new JPanel ();
	   main. setLayout (new BoxLayout (main, BoxLayout. Y_AXIS));
	   notifier. setTitle (serviceName);
	   JLabel Label_0	= new JLabel ("channel " + channel);
	   JLabel Label_1	= new JLabel ("startAddr " + startAddr);
	   JLabel Label_2	= new JLabel ("length " + length);
	   JLabel Label_3	= new JLabel ("bitRate " + bitRate);
	   String protL;
	   if (!shortForm) {
	      protL = "EEP ";
	      protL = protL + ((protLevel & 03) + 1);
	      if ((protLevel & (1 << 2)) == 0)
	         protL += "-A";
	      else
	         protL += "-B";
	   }
	   else  {
	      protL = "UEP " + protLevel;
	   }
	   JLabel Label_4      = new JLabel ("protection " + protL);
	   JLabel Label_5      = new JLabel ("program type " + programType);
	   main. add (Label_0);
	   main. add (Label_1);
	   main. add (Label_2);
	   main. add (Label_3);
	   main. add (Label_4);
	   main. add (Label_5);
	   main. add (Label_6);
	   notifier. add (main);
	   notifier. pack ();
	   notifier. setVisible (true);
	}

	public	void	show_packetData (String		serviceName,
	                                 String		channel,
	                                 int		startAddr,
	                                 int		length,
	                                 boolean	shortForm,
	                                 int		bitRate,
	                                 int 		protLevel,
	                                 int		DSCTy,
	                                 int		FEC_scheme,
	                                 int		appType) {
	   notifier. setPreferredSize (new Dimension (200, 200));
	   JPanel	main	= new JPanel ();
	   main. setLayout (new BoxLayout (main, BoxLayout. Y_AXIS));
	   notifier. setTitle (serviceName);
	   JLabel Label_0	= new JLabel ("channel " + channel);
	   JLabel Label_1	= new JLabel ("startAddr " + startAddr);
	   JLabel Label_2	= new JLabel ("length " + length);
	   JLabel Label_3	= new JLabel ("bitRate " + bitRate);
	   String protL;
	   if (!shortForm) {
	      protL = "EEP ";
	      protL = protL + ((protLevel & 03) + 1);
	      if ((protLevel & (1 << 2)) == 0)
	         protL += "-A";
	      else
	         protL += "-B";
	   }
	   else  {
	      protL = "UEP " + protLevel;
	   }
	   JLabel Label_4      = new JLabel ("protection " + protL);
	   JLabel Label_4a	= new JLabel ("DSCTy " + DSCTy);
	   JLabel Label_5      = new JLabel ("FEC scheme " + FEC_scheme);
	   JLabel Label_a      = new JLabel ("app type   " + appType);
	   main. add (Label_0);
	   main. add (Label_1);
	   main. add (Label_2);
	   main. add (Label_3);
	   main. add (Label_4);
	   main. add (Label_4a);
	   main. add (Label_5);
	   main. add (Label_a);
	   main. add (Label_6);
	   notifier. add (main);
	   notifier. pack ();
	   notifier. setVisible (true);
	}


	private	JFrame pictureFrame	= new JFrame ();

	public void show_picture (byte [] data, int subtype, String name) {
	   ByteArrayInputStream bis = new ByteArrayInputStream (data);
	   String pictureType =  subtype == 0 ? "gif" :
	                         subtype == 1 ? "jpg" :
	                         subtype == 2 ? "bmp" : "png";
	   Iterator<?> readers =
	              ImageIO.getImageReadersByFormatName (pictureType);

//	ImageIO is a class containing static methods for locating ImageReaders
//	and ImageWriters, and performing simple encoding and decoding.

	   ImageReader reader = (ImageReader) readers.next();
	   Object source = bis;
	   try {
	      ImageInputStream iis = ImageIO.createImageInputStream (source);
	      reader. setInput (iis, true);
	      ImageReadParam param = reader.getDefaultReadParam ();
	      Image image = reader.read (0, param);

	      pictureFrame. setVisible (false);
	      pictureFrame      = new JFrame ();
	      JLabel lblimage	= new JLabel (new ImageIcon (image));
	      pictureFrame.getContentPane ().
	                            add (lblimage, BorderLayout.CENTER);
	      pictureFrame.setSize (300, 400);
	      pictureFrame.setVisible (true);
	   } catch (Exception e) {
	      System. out. println (e. getMessage ());
	   }
//	got an image file
	   System. out. println ("showing " + name);
	}

	public  void show_dynamicLabel  (String s) {
	   if (s. length () > 50)
	      m_dynamicLabel. setText (s. substring (0, 50));
	   else
	      m_dynamicLabel. setText (s);
        }

	public	void	clear_dynamicLabel	() {
	   String s = " ";
	   m_dynamicLabel. setText (s);
	}

	public  void show_motHandling (boolean flag) {
	   m_motLabel. setOpaque (true);
	   m_motLabel. setBackground (flag ? Color. green : Color. red);
	}

	public	void	show_frameErrors (int errors) {
	   if (notifier. isVisible ())
	      Label_6. setText ("signal quality " + errors);
	}
//
//	Used to set the initial value
	public	void	setDeviceGain (int v) {
	   m_gainSlider. setValue (v);
	   m_gainLabel. setText (Integer. toString (v));
	}

	public	void	clearScreen () {
	   m_dynamicLabel. setText (" ");
	   m_ensembleLabel. setText (" ");
	   m_serviceTable. clearTable ();
	   m_selectedService. setText ("wait for services");
	   m_selectedService. setBackground (Color. red);
	   m_selectedService. setOpaque (true);
	   m_resetButton. setBackground (Color.red);
	   m_resetButton. setOpaque (true);
	}
}
