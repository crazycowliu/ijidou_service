package android.os;   
      
interface ICanbusListener {
	//TODO: remove me
	void actionPerformed(int actionId);
	
	//call back, so message is in-type
	void messageArrived(int msgId, in byte[] message, int length);
}