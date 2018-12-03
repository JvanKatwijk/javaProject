
package package_Model;
import	utils.*;

public interface modelSignals {
	public	void	newService	(String s1, ProgramData p);
	public	void	ensembleName	(String s1, int s2);
	public	void	no_signal_found	();
        public  void    show_SNR        (int snr);
	public	void	show_Sync	(boolean flag);
	public	void	show_isStereo	(boolean b);
	public	void	show_ficSuccess	(int successRate);
	public	void	show_freqOffset	(int offset);
	public	void	show_picture	(byte [] data, int subtype, String name);
	public	void	show_dynamicLabel	(String s);
	public	void	show_motHandling	(boolean flag);
	public	void	show_frameErrors	(int errors);
}

