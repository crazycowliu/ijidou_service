package android.os;

import android.os.ICanbusListener;

interface ICanbusService {
	
	boolean getBluetooth();
	
	boolean isActiveBluetooth();

    /**
      * Register a callback.
      */
    void addListener(ICanbusListener listener);

    /**
      * Unregister a callback.
      */
    void removeListener(ICanbusListener listener);
    
}