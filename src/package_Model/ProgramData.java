package package_Model;
//
//	It is a little silly but for now we
//	combine the sorts of data (audio or packet)
//	here. It would be better to make this into a class hierarchy
//	with a common base and derived types for audio and packet
public class ProgramData  {
	public boolean	defined;
	public boolean	audioService;
	public String	channel;
	public String	serviceName;
	public int	serviceId;
	public int	subchId;
	public int	startAddr;
	public boolean	shortForm;
	public int	protLevel;
	public int	length;
	public int	bitRate;
//
//	for audio:
	public int	ASCTy;
	public short	language;
	public short	programType;
//
//	for data:
	public	int	FEC_scheme;
	public	int	DSCTy;
	public	int	DGflag;
	public	int	packetAddress;
	public	int	appType;
	public	int	componentNr;
};


