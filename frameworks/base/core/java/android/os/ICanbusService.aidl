package android.os;

import android.os.ICanbusListener;

interface ICanbusService {

	  /**
	   * Its invoker should make sure the given bufs has enough size
	   */	
	  byte[] queryMessage(byte comId, in byte[] queryMsg);
	
    /**
      * Register a callback.
      */
    void addListener(ICanbusListener listener);

    /**
      * Unregister a callback.
      */
    void removeListener(ICanbusListener listener);
    
    /**
      * Register a callback, according to comId
      */
    void addListener2(int comId, ICanbusListener listener);
    
    /**
      * Unregister a callback, according to comId
      */
    void removeListener2(int comId, ICanbusListener listener);
}