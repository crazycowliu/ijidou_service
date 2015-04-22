package android.os;

public class EffectiveInfo {
	public final static char RESERVED 	= 0x80;
	public final static char BLUETOOTH 	= 0x40;
	public final static char RADAR 		= 0x20;
	public final static char KEY_IN		= 0x10;
	public final static char PARK		= 0x08;
	public final static char REV		= 0x04;
	public final static char ILL		= 0x02;
	public final static char ACC		= 0x01;
	char info = 0;
	
	public boolean getKeyIn() {
		return (info & KEY_IN) != 0;
	}
}
