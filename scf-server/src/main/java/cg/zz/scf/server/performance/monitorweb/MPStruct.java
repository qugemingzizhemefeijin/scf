package cg.zz.scf.server.performance.monitorweb;

public class MPStruct {
	
	public static final int Version = 1;
	public static final int TotalLen = 4;
	public static final int Type = 2;
	public static final int ExType = 2;
	public static int HeadLength = MonitorProtocol.HEADER_LENGTH;

	public static int getHeadLength() {
		return HeadLength;
	}

}
