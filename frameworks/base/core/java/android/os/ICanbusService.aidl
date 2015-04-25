package android.os;

interface ICanbusService {
	
//	boolean getBluetooth();
	
//	boolean isActiveBluetooth();
	
	/**
	 *	Get the latest message
	 *  return:
	 *		0: success
	 */
	int getMessage(int commId, byte[] buf, int length);
	
	/**
	 *	Get the latest message
	 *  return:
	 *		0: success
	 */
	int sendMessage(int commId, byte[] buf, int length);

    /**
      * Register a callback.
      */
    void addListener(ICanbusListener listener);

    /**
      * Unregister a callback.
      */
    void removeListener(ICanbusListener listener);
}