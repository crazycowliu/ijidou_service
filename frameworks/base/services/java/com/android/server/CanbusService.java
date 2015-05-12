package com.android.server;

import java.util.HashMap;
import java.util.Map;

import android.os.ICanbusListener;
import android.os.ICanbusService;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class CanbusService extends ICanbusService.Stub {

	private static final String TAG = "CanbusService_java";

	private final RemoteCallbackList<ICanbusListener> allListeners = new RemoteCallbackList<ICanbusListener>();

	private final Map<Integer, RemoteCallbackList<ICanbusListener>> msgListeners = new HashMap<Integer, RemoteCallbackList<ICanbusListener>>();

	private native boolean native_init();

	private native byte[] native_query_message(int comId, byte[] sendMsg);

	CanbusService() {
		Log.i(TAG, "CanbusService.java: init");
		System.out.println("CanbusService.java: init");

		native_init();

		Log.i(TAG, "CanbusService.java: init");
		System.out.println("CanbusService.java: finished");
	}

	/**
	 * 
	 * TODO: consider blocking
	 */
	public byte[] queryMessage(byte comId, byte[] queryMsg)
	    throws RemoteException {
		return native_query_message(comId, queryMsg);
	}

	public void addListener(ICanbusListener listener) throws RemoteException {
		if (listener != null) {
			allListeners.register(listener);
		}
	}

	public void removeListener(ICanbusListener listener) throws RemoteException {
		if (listener != null) {
			allListeners.unregister(listener);
		}
	}

	public void addListener2(int comId, ICanbusListener listener)
	    throws RemoteException {
		if (listener == null) {
			Log.w(TAG, "listener is empty!");
			return;
		}

		RemoteCallbackList<ICanbusListener> listeners = msgListeners.get(comId);
		if (listeners == null) {
			listeners = new RemoteCallbackList<ICanbusListener>();
			msgListeners.put(comId, listeners);
		}
		listeners.register(listener);
	}

	public void removeListener2(int comId, ICanbusListener listener)
	    throws RemoteException {
		if (listener == null) {
			Log.w(TAG, "listener is empty!");
			return;
		}

		RemoteCallbackList<ICanbusListener> listeners = msgListeners.get(comId);
		if (listeners == null) {
			Log.w(TAG, "listener is not registered!");
		}

		listeners.unregister(listener);
	}

	/**
	 * notifyListeners will be invoked by native method, msg will bed revoked
	 * after this method finished.
	 * 
	 * @param comId
	 * @param msg
	 * 					message received
	 * @param length
	 * 					length of the message received
	 */
	public void notifyListeners(int comId, byte[] msg, int length) {
		// notify according to all listeners
		int n = allListeners.beginBroadcast();
		for (int i = 0; i < n; i++) {
			try {
				Log.i(TAG, "notifyListeners start: comId: " + comId + " msg[3] = "
				    + msg[3] + " length = " + length);
				// message will be sent to other side
				allListeners.getBroadcastItem(i).messageArrived(comId, msg, msg.length);
				Log.i(TAG, "notifyListeners succeed: comId: " + comId + " msg[3] = "
				    + msg[3] + " length = " + length);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
				Log.i(TAG, "notifyListeners " + e.getMessage());
				e.printStackTrace();
			}
		}
		allListeners.finishBroadcast();

		// notify to listeners according to comId
		RemoteCallbackList<ICanbusListener> listeners = msgListeners.get(comId);
		if (listeners == null) {
			return;
		}

		n = listeners.beginBroadcast();
		for (int i = 0; i < n; i++) {
			try {
				Log.i(TAG, "notifyListeners start: comId: " + comId + " msg[3] = "
				    + msg[3] + " length = " + length);
				allListeners.getBroadcastItem(i).messageArrived(comId, msg, msg.length);
				Log.i(TAG, "notifyListeners succeed: comId: " + comId + " msg[3] = "
				    + msg[3] + " length = " + length);
			} catch (RemoteException e) {
				Log.i(TAG, "notifyListeners " + e.getMessage());
				e.printStackTrace();
			}
		}
		listeners.finishBroadcast();
	}
}
