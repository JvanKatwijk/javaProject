
import	javax.swing.*;
import	devices.*;
import	package_View.*;
import	package_Model.*;
import	package_Controller.*;
import	java.util.Properties;
import	java.io.File;
import	java.io.FileInputStream;
import	java.io.FileOutputStream;
import	java.io.IOException;

public class JavaRadio {
//	... Create model, view, and controller.  They are
//	created once here and passed to the parts that
//	need them so there is only one copy of each.


	public static void main (String[] args) {

	   final Device theDevice = bindDevice (args);
	   if (theDevice. is_nullDevice ()) {
	      System. out. println ("unable to bind to device");
	      System. exit (1);
	   }
	   final boolean fileInput = theDevice. is_fileInput ();
//
	   final String iniFile	=
	                System. getProperty ("user.home") + "/.javaDab.ini";
	   Properties savedValues  = new Properties ();
	   File ff = new File (iniFile);
	   try {
	      savedValues. load (new FileInputStream (ff));
	   } catch (IOException ex) {
	   }

	   String dabMode	= savedValues. getProperty ("dabMode", "1");
	   int	test	= 1;
	   try {
	      test = Integer.parseInt (dabMode);
	   } catch (Exception e) {}
	   
	   String dabBand   = savedValues. getProperty ("dabBand", "Band III");
	   BandHandler my_bandHandler   = new BandHandler (dabBand);

	   final int mode = test;
      /* Create and display the form */
	   java.awt.EventQueue.invokeLater (new Runnable() {
	      final Device tuner = theDevice;
	      final boolean flag = fileInput;
	      @Override
	      public void run () {
	         RadioModel      model      = new RadioModel  (mode, tuner);
	         RadioView       view       = new RadioView   ();
	         RadioController controller =
	                                  new RadioController (model,
	                                                       view,
	                                                       savedValues,
	                                                       my_bandHandler);
	         controller. startRadio (flag);
	         view.setVisible(true);
	         view. setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	         view. addWindowListener(new java.awt.event.WindowAdapter() {
	            @Override
	             public void
	                windowClosing(java.awt.event.WindowEvent windowEvent) {
	                if (JOptionPane.showConfirmDialog (new JFrame (),
	                    "Are you sure to close this window?",
	                                       "Really Closing?",
	                     JOptionPane.YES_NO_OPTION,
	                     JOptionPane.QUESTION_MESSAGE) ==
	                              JOptionPane.YES_OPTION){
	                    File f = new File (iniFile);
	                    try (FileOutputStream fo =
	                                   new FileOutputStream (f)) {
	                       savedValues. store (fo, "javaDab");
	                       fo. close ();
	                    } catch (Exception ex) { }
	                }
	                System.exit(0);
	            }
	         });
	      }
	   });
	}

	private static Device bindDevice (String [] args) {
	   Device tester;
//	   try {
//	      tester = new airspyDevice (220000000, 90, true);
//	      return tester;
//	   } catch (Exception e) {}

	   try {
	      tester = new sdrplayDevice (220000000, 40, true);
	      return tester;
	   } catch (Exception e) {}

	   if (args. length > 0) {
	      try {
	         tester = new fileReader (args [0]);
	         return tester;
	      } catch (Exception e) {}
	   }

	   try {
	      tester = new rtlsdrDevice (220000000, 90, true);
	      return tester;
	   } catch (Exception e) {}
	   return new nullDevice ();
	}
}

