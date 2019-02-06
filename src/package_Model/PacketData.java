
package package_Model;
//
//	It is a little silly but for now we
//	combine the sorts of data (audio or packet)
//	here. It would be better to make this into a class hierarchy
//	with a common base and derived types for audio and packet
public class PacketData extends ProgramData  {
	public	int	FEC_scheme;
	public	int	DSCTy;
	public	int	DGflag;
	public	int	packetAddress;
	public	int	appType;
	public	int	componentNr;
};


