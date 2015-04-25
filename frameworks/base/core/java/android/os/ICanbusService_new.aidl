package android.os;

interface ICanbusService {
	
//	boolean getBluetooth();
	
//	boolean isActiveBluetooth();

	
	
	/**
	 *	Get the latest message
	 *  return:
	 *		0: success
	 */
	int getMessage(byte commId, byte[] buf, int length);
	
	/**
	 *	Get the latest message
	 *  return:
	 *		0: success
	 */
	int sendMessage(byte commId, byte[] buf, int length);
		
	int queryMessage(byte commId, byte[] sbuf, int slength, byte[] rbuf, int rlength);

    /**
      * Register a callback.
      */
    void addListener(ICanbusListener listener);


    /**
      * Unregister a callback.
      */
    void removeListener(ICanbusListener listener);
    
}