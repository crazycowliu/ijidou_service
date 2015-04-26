package com.android.server;


import android.os.ICanbusListener;
import android.os.ICanbusService;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class CanbusService extends ICanbusService.Stub {
	
	class CarInfo {
		boolean bluetooth = false;
		boolean keyIn = false;
		boolean park = false;
	}

  //  private static final String LOG_TAG = "CanbusService";
  private static final String TAG = "CanbusService_java_12";
  private static final int MAX_MSG_LEN = 1024;
  //FIXME: change to non-static
  private static byte[] message = new byte[MAX_MSG_LEN];
  
  private native boolean canbus_init1();
  
  //interface
  //1 mean bluetooth is open
  //0 mean bluetooth is closed
  // device error ?
  private native boolean get_bluetooth();

  private native boolean is_active_bluetooth();
  
  //change to non-static
  private final static RemoteCallbackList<ICanbusListener> listeners = 
		  new RemoteCallbackList <ICanbusListener>();

  CanbusService() {
	  Log.i(TAG, "CanbusService.java: init");
	  System.out.println("CanbusService.java: init");
	  System.out.flush();
	  
	  canbus_init1();
	  
	  Log.i(TAG, "CanbusService.java: init");
	  System.out.println("CanbusService.java: finished");
	  System.out.flush();
  }
  
  public boolean getBluetooth() {
	  return get_bluetooth();
  }
  
  public boolean isActiveBluetooth(){
		return is_active_bluetooth();
  }

@Override
//TODO: listen to different message
public void addListener(ICanbusListener listener) throws RemoteException {
    if (listener != null) {   
        listeners.register(listener);
    }
}

@Override
public void removeListener(ICanbusListener listener) throws RemoteException {
    if(listener != null) {  
        listeners.unregister(listener);
    }
}

//FIXME: change to non-static
//TODO: 
public static void notifyListeners(int comId) {
    final int N = listeners.beginBroadcast();  
    for (int i=0; i<N; i++) {   
        try {
        	
        	Log.i(TAG, "notifyListeners1 succeed: comId: " + comId + " message[3] = " + message[3] + " length = " + message.length);
        	listeners.getBroadcastItem(i).actionPerformed(comId);
        	Log.i(TAG, "notifyListeners succeed: comId: " + comId);
        	Log.i(TAG, "notifyListeners2 succeed: comId: " + comId + " message[3] = " + message[3] + " length = " + message.length);
        } catch (RemoteException e) {   
            // The RemoteCallbackList will take care of removing   
            // the dead object for us.     
        	Log.i(TAG, "notifyListeners1 " + e);
        	e.printStackTrace();
        }  
    }  
    listeners.finishBroadcast();  
}  

public static void notifyListeners2(int comId, byte[] msg, int length) {
    final int N = listeners.beginBroadcast();  
    for (int i=0; i<N; i++) {   
        try {
        	Log.i(TAG, "notifyListeners2 start: comId: " + comId + " msg[3] = " + msg[3] + " length = " + length);
        	listeners.getBroadcastItem(i).messageArrived(comId, msg, msg.length);
        	Log.i(TAG, "notifyListeners2 succeed: comId: " + comId + " msg[3] = " + msg[3] + " length = " + length);
        } catch (RemoteException e) {   
            // The RemoteCallbackList will take care of removing   
            // the dead object for us.
        	Log.i(TAG, "notifyListeners2 " + e);
        	e.printStackTrace();
        }  
    }  
    listeners.finishBroadcast();  
}  
  
//public CarInfo getCarInfo() {
//	  return get_car_info();
//}

//  private static native CarInfo get_car_info();

}
