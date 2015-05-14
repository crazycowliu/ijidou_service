/*
 * Copyright (c) 2008-2009, Motorola, Inc. All rights reserved.
 * Copyright (c) 2010-2013, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of The Linux Foundation nor
 *          the names of its contributors may be used to endorse or promote
 *          products derived from this software without specific prior written
 *          permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.codeaurora.bluetooth.map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.os.ParcelUuid;
import android.util.Log;
import android.bluetooth.BluetoothUuid;

import org.codeaurora.bluetooth.R;
import org.codeaurora.bluetooth.map.BluetoothMns.MnsClient;
import org.codeaurora.bluetooth.map.MapUtils.EmailUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import javax.obex.ServerSession;

import static org.codeaurora.bluetooth.map.IBluetoothMasApp.MESSAGE_TYPE_EMAIL;
import static org.codeaurora.bluetooth.map.IBluetoothMasApp.MESSAGE_TYPE_MMS;
import static org.codeaurora.bluetooth.map.IBluetoothMasApp.MESSAGE_TYPE_SMS;
import static org.codeaurora.bluetooth.map.IBluetoothMasApp.MESSAGE_TYPE_SMS_MMS;

public class BluetoothMasService extends Service {
    private static final String TAG = "BluetoothMasService";

    /**
     * To enable MAP DEBUG/VERBOSE logging - run below cmd in adb shell, and
     * restart com.android.bluetooth process. only enable DEBUG log:
     * "setprop log.tag.BluetoothMapService DEBUG"; enable both VERBOSE and
     * DEBUG log: "setprop log.tag.BluetoothMapService VERBOSE"
     */
    public static final boolean DEBUG = true;
    public static final boolean VERBOSE = true;
    /**
     * Intent indicating incoming connection request which is sent to
     * BluetoothMasActivity
     */
    public static final String ACCESS_REQUEST_ACTION = "org.codeaurora.bluetooth.map.accessrequest";

    /**
     * Intent indicating incoming connection request accepted by user which is
     * sent from BluetoothMasActivity
     */
    public static final String ACCESS_ALLOWED_ACTION = "org.codeaurora.bluetooth.map.accessallowed";

    /**
     * Intent indicating incoming connection request denied by user which is
     * sent from BluetoothMasActivity
     */
    public static final String ACCESS_DISALLOWED_ACTION = "org.codeaurora.bluetooth.map.accessdisallowed";

    /**
     * Intent indicating incoming obex authentication request which is from
     * PCE(Carkit)
     */
    public static final String AUTH_CHALL_ACTION = "org.codeaurora.bluetooth.map.authchall";

    /**
     * Intent indicating obex session key input complete by user which is sent
     * from BluetoothMasActivity
     */
    public static final String AUTH_RESPONSE_ACTION = "org.codeaurora.bluetooth.map.authresponse";

    /**
     * Intent indicating user canceled obex authentication session key input
     * which is sent from BluetoothMasActivity
     */
    public static final String AUTH_CANCELLED_ACTION = "org.codeaurora.bluetooth.map.authcancelled";

    /**
     * Intent indicating timeout for user confirmation, which is sent to
     * BluetoothMasActivity
     */
    public static final String USER_CONFIRM_TIMEOUT_ACTION = "org.codeaurora.bluetooth.map.userconfirmtimeout";

    public static final String THIS_PACKAGE_NAME = "org.codeaurora.bluetooth";

    /**
     * Intent Extra name indicating always allowed which is sent from
     * BluetoothMasActivity
     */
    public static final String EXTRA_ALWAYS_ALLOWED = "org.codeaurora.bluetooth.map.alwaysallowed";

    /**
     * Intent Extra name indicating session key which is sent from
     * BluetoothMasActivity
     */
    public static final String EXTRA_SESSION_KEY = "org.codeaurora.bluetooth.map.sessionkey";

    /**
     * Intent Extra name indicating BluetoothDevice which is sent to
     * BluetoothMasActivity
     */
    public static final String EXTRA_BLUETOOTH_DEVICE = "org.codeaurora.bluetooth.map.bluetoothdevice";

    private static final String BLUETOOTH_PERM = android.Manifest.permission.BLUETOOTH;

    private static final String BLUETOOTH_ADMIN_PERM = android.Manifest.permission.BLUETOOTH_ADMIN;

    public static final int MSG_SERVERSESSION_CLOSE = 5004;

    public static final int MSG_SESSION_ESTABLISHED = 5005;

    public static final int MSG_SESSION_DISCONNECTED = 5006;

    public static final int MSG_OBEX_AUTH_CHALL = 5007;

    private static final int MSG_INTERNAL_START_LISTENER = 1;

    private static final int MSG_INTERNAL_USER_TIMEOUT = 2;

    private static final int USER_CONFIRM_TIMEOUT_VALUE = 30000;

    public static final int MAS0_PORT_NUM = 16;

    public static final int MAS1_PORT_NUM = 17;

    public static final ParcelUuid MessageAccessServer =
            ParcelUuid.fromString("00001132-0000-1000-8000-00805f9b34fb");

    public static final ParcelUuid MessageNotificationServer =
            ParcelUuid.fromString("00001133-0000-1000-8000-00805f9b34fb");

    // Ensure not conflict with Opp notification ID
    private static final int NOTIFICATION_ID_ACCESS = -1000007;
    private static final int NOTIFICATION_ID_CONNECTED = -1000008;

    private Notification mConnectedNotification = null;

    private BluetoothAdapter mAdapter;

    private Object mAuthSync = new Object();

    private BluetoothMapAuthenticator mAuth = null;

    BluetoothMasObexConnectionManager mConnectionManager = null;

    BluetoothMns mnsClient;
    private static BluetoothDevice mRemoteDevice = null;

    private boolean mHasStarted = false;
    private int mStartId = -1;

    private boolean mIsEmailEnabled = true;

    /**
     * The flag indicating MAP request has been notified.
     * This is set on when initiate notification and set off after accept/time out
     */
    private volatile boolean mIsRequestBeingNotified = false;


    public static class MasInstanceInfo {
        int mSupportedMessageTypes;
        Class<? extends MnsClient> mMnsClientClass;
        int mRfcommPort;

        public MasInstanceInfo(int smt, Class<? extends MnsClient> _class, int port) {
            mSupportedMessageTypes = smt;
            mMnsClientClass = _class;
            mRfcommPort = port;
        }
    }

    public static final int MAX_INSTANCES = 2;
    public static final int EMAIL_MAS_START = 1;
    public static final int EMAIL_MAS_END = 1;
    public static final MasInstanceInfo MAS_INS_INFO[] = new MasInstanceInfo[MAX_INSTANCES];

    // The following information must match with corresponding
    // SDP records supported message types and port number
    // Please refer sdptool.c, BluetoothService.java, & init.qcom.rc
    static {
        MAS_INS_INFO[0] = new MasInstanceInfo(MESSAGE_TYPE_SMS_MMS, BluetoothMnsSmsMms.class, MAS0_PORT_NUM);
        MAS_INS_INFO[1] = new MasInstanceInfo(MESSAGE_TYPE_EMAIL, BluetoothMnsEmail.class, MAS1_PORT_NUM);
    }

    private ContentObserver mEmailAccountObserver;

    private static final String CONF_FILE_PATH =
            "/etc/bluetooth/main.conf";

    public void CheckEmailEnabled() {
        if (VERBOSE)
            Log.v(TAG, " CheckEmailEnabled: Loading from conf");
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(CONF_FILE_PATH);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader file = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = file.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) continue;
                String[] value = line.split(" = ");
                if (value != null && value.length == 2) {
                    if (value[0].equalsIgnoreCase("BluetoothMapEmailEnabled")) {
                        if (value[1].equalsIgnoreCase("false")) {
                            mIsEmailEnabled = false;
                        }
                        else {
                            mIsEmailEnabled = true;
                        }
                        Log.v(TAG, "CheckEmailEnabled: IsEmailEnabled: " + mIsEmailEnabled);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Main.conf File Not found");
        } catch (IOException e) {
            Log.e(TAG, "IOException: read Main.conf File " + e);
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void updateEmailAccount() {
        if (VERBOSE) Log.v(TAG, "updateEmailAccount()");
        List<Long> list = EmailUtils.getEmailAccountIdList(this);
        ArrayList<Long> notAssigned = new ArrayList<Long>();
        EmailUtils.removeMasIdIfNotPresent(list);
        for (Long id : list) {
            int masId = EmailUtils.getMasId(id);
            if (masId == -1) {
                notAssigned.add(id);
            }
        }
        for (int i = EMAIL_MAS_START; i <= EMAIL_MAS_END; i ++) {
            long accountId = EmailUtils.getAccountId(i);
            if (accountId == -1 && notAssigned.size() > 0) {
                EmailUtils.updateMapTable(notAssigned.remove(0), i);
            }
        }
    }

    public BluetoothMasService() {
        CheckEmailEnabled();
        mConnectionManager = new BluetoothMasObexConnectionManager();
        if (VERBOSE)
           Log.v(TAG, "BluetoothMasService: mIsEmailEnabled: " + mIsEmailEnabled);
        if(mIsEmailEnabled) {
            mEmailAccountObserver = new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange) {
                    updateEmailAccount();
                }
            };
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (VERBOSE)
            Log.v(TAG, "Map Service onCreate");

        mConnectionManager.init();
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mHasStarted) {
            mHasStarted = true;
            if (VERBOSE) Log.v(TAG, "Starting MAS instances");

            int state = mAdapter.getState();
            if (state == BluetoothAdapter.STATE_ON) {
                mSessionStatusHandler.sendEmptyMessage(MSG_INTERNAL_START_LISTENER);
            } else if (VERBOSE) {
                Log.v(TAG, "BT is not ON, no start");
            }
        }
        Log.v(TAG, "onCreate: mIsEmailEnabled: " + mIsEmailEnabled);
        if(mIsEmailEnabled) {
            getContentResolver().registerContentObserver(
                EmailUtils.EMAIL_ACCOUNT_URI, true, mEmailAccountObserver);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (VERBOSE)
            Log.v(TAG, "Map Service onStartCommand");
        int retCode = super.onStartCommand(intent, flags, startId);
        if (retCode == START_STICKY) {
            mStartId = startId;
            if (mAdapter == null) {
                Log.w(TAG, "Stopping BluetoothMasService: "
                        + "device does not have BT or device is not ready");
                // Release all resources
                closeService();
            } else {
                // No need to handle the null intent case, because we have
                // all restart work done in onCreate()
                if (intent != null) {
                    parseIntent(intent);
                }
            }
        }
        return retCode;
    }

    // process the intent from receiver
    private synchronized void parseIntent(final Intent intent) {
        String action = intent.getStringExtra("action");
        if (VERBOSE)
            Log.v(TAG, "action: " + action);

        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR);
        boolean removeTimeoutMsg = true;
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                // Release all resources
                closeService();
            } else {
                removeTimeoutMsg = false;
                Log.v(TAG, "parseIntent 1: mIsEmailEnabled: " + mIsEmailEnabled);
                if(mIsEmailEnabled) {
                    if (state == BluetoothAdapter.STATE_ON) {
                        updateEmailAccount();
                    }
                }
            }
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) &&
                                 mIsRequestBeingNotified) {
            if (mRemoteDevice == null) {
               Log.e(TAG, "Unexpected error!");
               return;
            }
            if (mSessionStatusHandler != null) {
               /* Let the user timeout handle this case as well */
               mSessionStatusHandler.sendMessage(mSessionStatusHandler
                   .obtainMessage(MSG_INTERNAL_USER_TIMEOUT));
               removeTimeoutMsg = false;
            }
        } else if (action.equals(ACCESS_ALLOWED_ACTION)) {
            if (mRemoteDevice == null) {
               Log.e(TAG, "Unexpected error!");
               return;
            }
            if (intent.getBooleanExtra(BluetoothMasService.EXTRA_ALWAYS_ALLOWED, false) == true) {
                if(mRemoteDevice != null) {
                   mRemoteDevice.setTrust(true);
                   Log.v(TAG, "setTrust() TRUE " + mRemoteDevice.getName());
                }
            }
            Log.v(TAG, "parseIntent 2: mIsEmailEnabled: " + mIsEmailEnabled);
            if(mIsEmailEnabled) {
                  updateEmailAccount();
            }
            mConnectionManager.initiateObexServerSession(mRemoteDevice);
        }  else if (action.equals(ACCESS_DISALLOWED_ACTION)) {
            mIsRequestBeingNotified = false;
            mConnectionManager.stopObexServerSessionWaiting();
        } else if (AUTH_RESPONSE_ACTION.equals(action)) {
            String sessionkey = intent.getStringExtra(EXTRA_SESSION_KEY);
            notifyAuthKeyInput(sessionkey);
        } else if (AUTH_CANCELLED_ACTION.equals(action)) {
            notifyAuthCancelled();
        } else {
            removeTimeoutMsg = false;
        }

        if (removeTimeoutMsg) {
            mSessionStatusHandler.removeMessages(MSG_INTERNAL_USER_TIMEOUT);
            if (VERBOSE) Log.v(TAG, "MAS access request notification flag off");
            mIsRequestBeingNotified = false;
        }
    }

    @Override
    public void onDestroy() {
        if (VERBOSE)
            Log.v(TAG, "Map Service onDestroy");

        super.onDestroy();
        Log.v(TAG, "onDestroy: mIsEmailEnabled: " + mIsEmailEnabled);
        if(mIsEmailEnabled) {
            getContentResolver().unregisterContentObserver(mEmailAccountObserver);
            EmailUtils.clearMapTable();
        }
        closeService();
    }

    private final void closeService() {
        if (VERBOSE) Log.v(TAG, "MNS_BT: inside closeService");
        try {
            if(mnsClient!=null) {
                if (VERBOSE) Log.v(TAG, "MNS_BT: about to send MNS_BLUETOOTH_OFF");
                mnsClient.getHandler().sendEmptyMessage(BluetoothMns.MNS_BLUETOOTH_OFF);
                mnsClient = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "MNS_BT: exception while sending MNS_BLUETOOTH_OFF");
        } finally {
            if (VERBOSE) Log.v(TAG, "MNS_BT: successfully sent MNS_BLUETOOTH_OFF");
            mConnectionManager.closeAll();
            mHasStarted = false;
            if (stopSelfResult(mStartId)) {
                if (VERBOSE) Log.v(TAG, "successfully stopped map service");
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (VERBOSE) Log.v(TAG, "Map Service onBind");
        return null;
    }

    private final Handler mSessionStatusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (VERBOSE) Log.v(TAG, "Handler(): got msg=" + msg.what);
            Context context = getApplicationContext();
            if (mnsClient == null) {
                Log.v(TAG, "handleMessage: mIsEmailEnabled" + mIsEmailEnabled);
                mnsClient = new BluetoothMns(context, mIsEmailEnabled);
            }

            switch (msg.what) {
                case MSG_INTERNAL_START_LISTENER:
                    if (mAdapter.isEnabled()) {
                        mConnectionManager.startAll();
                    } else {
                        closeService();
                    }
                    break;
                case MSG_INTERNAL_USER_TIMEOUT:
                    Intent intent = new Intent(USER_CONFIRM_TIMEOUT_ACTION);
                    intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mRemoteDevice);
                    sendBroadcast(intent);
                    removeMapNotification(NOTIFICATION_ID_ACCESS);
                    if (VERBOSE) Log.v(TAG, "MAS access request notification flag off");
                    mIsRequestBeingNotified = false;
                    mConnectionManager.stopObexServerSessionWaiting();
                    break;
                case MSG_SERVERSESSION_CLOSE:
                {
                    final int masId = msg.arg1;
                    mConnectionManager.stopObexServerSession(masId);
                    //Dismiss CONNECTED Notification if no Active Connections
                    boolean stopFgNotification = true;
                    for (BluetoothMasObexConnection connection : mConnectionManager.mConnections) {
                       if (connection.mConnSocket != null) {
                           if(VERBOSE) Log.v(TAG,"Active Session exists  ");
                           stopFgNotification = false;
                           break;
                       }
                    }
                    if(stopFgNotification ==  true){
                         stopForeground(true);
                         mConnectedNotification=null;
                    }
                    break;
                }
                case MSG_SESSION_ESTABLISHED:
                    if(mConnectedNotification == null)
                        mConnectedNotification = createMapConnectedNotification(mRemoteDevice);
                    startForeground(NOTIFICATION_ID_CONNECTED, mConnectedNotification);
                    break;
                case MSG_SESSION_DISCONNECTED:
                    break;
                default:
                    break;
            }
        }
    };

    private Notification createMapConnectedNotification(BluetoothDevice device) {
        if (VERBOSE) Log.v(TAG, "Creating MAS access CONNECTED");

        Notification notification = new Notification(android.R.drawable.stat_sys_data_bluetooth,
            getString(R.string.map_notif_active_session), System.currentTimeMillis());
        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.defaultname);
        }
        notification.setLatestEventInfo(this,  getString(R.string.map_notif_active_session),
            getString( R.string.map_notif_connected ,name), null);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        notification.defaults = Notification.DEFAULT_SOUND;
        return notification;


    }
    private void createMapNotification(BluetoothDevice device) {
        if (VERBOSE) Log.v(TAG, "Creating MAS access notification");
        mIsRequestBeingNotified = true;

        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Create an intent triggered by clicking on the status icon.
        Intent clickIntent = new Intent();
        clickIntent.setClass(this, BluetoothMasActivity.class);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        clickIntent.setAction(ACCESS_REQUEST_ACTION);
        clickIntent.putExtra(EXTRA_BLUETOOTH_DEVICE, device);

        // Create an intent triggered by clicking on the
        // "Clear All Notifications" button
        Intent deleteIntent = new Intent();
        deleteIntent.setClass(this, BluetoothMasReceiver.class);

        Notification notification = null;
        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.defaultname);
        }

        deleteIntent.setAction(ACCESS_DISALLOWED_ACTION);
        notification = new Notification(android.R.drawable.stat_sys_data_bluetooth,
            getString(R.string.map_notif_ticker), System.currentTimeMillis());
        notification.setLatestEventInfo(this, getString(R.string.map_notif_ticker),
                getString(R.string.map_notif_message, name), PendingIntent
                        .getActivity(this, 0, clickIntent, PendingIntent.FLAG_ONE_SHOT));

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.deleteIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
        nm.notify(NOTIFICATION_ID_ACCESS, notification);


        if (VERBOSE) Log.v(TAG, "Awaiting Authorization : MAS Connection : " + device.getName());

    }

    private void removeMapNotification(int id) {
        Context context = getApplicationContext();
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }

    private void notifyAuthKeyInput(final String key) {
        synchronized (mAuthSync) {
            if (key != null) {
                mAuth.setSessionKey(key);
            }
            mAuth.setChallenged(true);
            mAuth.notify();
        }
    }

    private void notifyAuthCancelled() {
        synchronized (mAuthSync) {
            mAuth.setCancelled(true);
            mAuth.notify();
        }
    }

    class BluetoothMasObexConnectionManager {
        private ArrayList<BluetoothMasObexConnection> mConnections =
                new ArrayList<BluetoothMasObexConnection>();

        public BluetoothMasObexConnectionManager() {
            int numberOfSupportedInstances = MAX_INSTANCES;
            Log.e(TAG, "BluetoothMasObexConnectionManager: mIsEmailEnabled: " + mIsEmailEnabled);
            if(!mIsEmailEnabled) {
                numberOfSupportedInstances = 1; /*Email instance not supported*/
            }
            for (int i = 0; i < numberOfSupportedInstances; i ++) {
                mConnections.add(new BluetoothMasObexConnection(
                        MAS_INS_INFO[i].mSupportedMessageTypes, i, MAS_INS_INFO[i].mRfcommPort));
            }
        }

        public void initiateObexServerSession(BluetoothDevice device) {
            try {
                for (BluetoothMasObexConnection connection : mConnections) {
                    if (connection.mConnSocket != null && connection.mWaitingForConfirmation) {
                        connection.mWaitingForConfirmation = false;
                        connection.startObexServerSession(device, mnsClient);
                    }
                }
            } catch (IOException ex) {
                Log.e(TAG, "Caught the error: " + ex.toString());
            }
        }

        public void setWaitingForConfirmation(int masId) {
            if (masId < mConnections.size()) {
                final BluetoothMasObexConnection connect = mConnections.get(masId);
                connect.mWaitingForConfirmation = true;
            } else {
                Log.e(TAG, "Attempt to set waiting for user confirmation for MAS id: " + masId);
                Log.e(TAG, "out of index");
            }
        }

        public void stopObexServerSession(int masId) {
            if (masId < mConnections.size()) {
                final BluetoothMasObexConnection connect = mConnections.get(masId);
                if (connect.mConnSocket != null) {
                    connect.stopObexServerSession();
                } else {
                    Log.w(TAG, "Attempt to stop OBEX Server session for MAS id: " + masId);
                    Log.w(TAG, "when there is no connected socket");
                }
            } else {
                Log.e(TAG, "Attempt to stop OBEX Server session for MAS id: " + masId);
                Log.e(TAG, "out of index");
            }
        }

        public void stopObexServerSessionWaiting() {
            for (BluetoothMasObexConnection connection : mConnections) {
                if (connection.mConnSocket != null && connection.mWaitingForConfirmation) {
                    connection.mWaitingForConfirmation = false;
                    connection.stopObexServerSession();
                }
            }
        }

        public void stopObexServerSessionAll() {
            for (BluetoothMasObexConnection connection : mConnections) {
                if (connection.mConnSocket != null) {
                    connection.stopObexServerSession();
                }
            }
        }

        public void closeAll() {
            for (BluetoothMasObexConnection connection : mConnections) {
                // Stop the possible trying to init serverSocket
                connection.mInterrupted = true;
                connection.closeConnection();
            }
        }

        public void startAll() {
            for (BluetoothMasObexConnection connection : mConnections) {
                connection.startRfcommSocketListener(mnsClient);
            }
        }

        public void init() {
            for (BluetoothMasObexConnection connection: mConnections) {
                connection.mInterrupted = false;
            }
        }

        public boolean isAllowedConnection(BluetoothDevice remoteDevice) {
            String remoteAddress = remoteDevice.getAddress();
            if (remoteAddress == null) {
                if (VERBOSE) Log.v(TAG, "Connection request from unknown device");
                return false;
            }
            final int size = mConnections.size();
            for (int i = 0; i < size; i ++) {
                final BluetoothMasObexConnection connection = mConnections.get(i);
                BluetoothSocket socket = connection.mConnSocket;
                if (socket != null) {
                    BluetoothDevice device = socket.getRemoteDevice();
                    if (device != null) {
                        String address = device.getAddress();
                        if (address != null) {
                            if (remoteAddress.equalsIgnoreCase(address)) {
                                if (VERBOSE) {
                                    Log.v(TAG, "Connection request from " + remoteAddress);
                                    Log.v(TAG, "when MAS id:" + i + " is connected to " + address);
                                }
                                return true;
                            } else {
                                if (VERBOSE) {
                                    Log.v(TAG, "Connection request from " + remoteAddress);
                                    Log.v(TAG, "when MAS id:" + i + " is connected to " + address);
                                }
                                return false;
                            }
                        } else {
                            // shall not happen, connected device must has address
                            // just for null pointer dereference
                            Log.w(TAG, "Connected device has no address!");
                        }
                    }
                }
            }

            if (VERBOSE) {
                Log.v(TAG, "Connection request from " + remoteAddress);
                Log.v(TAG, "when no MAS instance is connected.");
            }
            return true;
        }
    }

    private class BluetoothMasObexConnection {
        private volatile boolean mInterrupted;
        private BluetoothServerSocket mServerSocket = null;
        private SocketAcceptThread mAcceptThread = null;
        private BluetoothSocket mConnSocket = null;
        private ServerSession mServerSession = null;
        private BluetoothMasObexServer mMapServer = null;

        private int mSupportedMessageTypes;
        private int mPortNum;
        private int mMasId;
        boolean mWaitingForConfirmation = false;

        public BluetoothMasObexConnection(int supportedMessageTypes, int masId, int portNumber) {
                mSupportedMessageTypes = supportedMessageTypes;
                mMasId = masId;
                mPortNum = portNumber;
        }

        private void startRfcommSocketListener(BluetoothMns mnsClient) {
            if (VERBOSE)
                Log.v(TAG, "Map Service startRfcommSocketListener");

            if (mServerSocket == null) {
                if (!initSocket()) {
                    closeService();
                    return;
                }
            }
            if (mAcceptThread == null) {
                mAcceptThread = new SocketAcceptThread(mnsClient, mMasId);
                mAcceptThread.setName("BluetoothMapAcceptThread " + mPortNum);
                mAcceptThread.start();
            }
        }

        private final boolean initSocket() {
            if (VERBOSE)
                Log.v(TAG, "Map Service initSocket");

            boolean initSocketOK = false;
            final int CREATE_RETRY_TIME = 10;

            // It's possible that create will fail in some cases. retry for 10 times
            for (int i = 0; i < CREATE_RETRY_TIME && !mInterrupted; i++) {
                try {
                    if(mPortNum == MAS1_PORT_NUM)
                       mServerSocket  = mAdapter.listenUsingRfcommWithServiceRecord("Email Message Access", MessageAccessServer.getUuid());
                    else
                       mServerSocket  = mAdapter.listenUsingRfcommWithServiceRecord("SMS/MMS Message Access", MessageAccessServer.getUuid());
                    initSocketOK = true;
                } catch (IOException e) {
                    Log.e(TAG, "Error create RfcommServerSocket " + e.toString());
                    initSocketOK = false;
                }

                if (!initSocketOK) {
                    synchronized (this) {
                        try {
                            if (VERBOSE) Log.v(TAG, "wait 3 seconds");
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "socketAcceptThread thread was interrupted (3)");
                            mInterrupted = true;
                        }
                    }
                } else {
                    break;
                }
            }

            if (initSocketOK) {
                if (VERBOSE)
                    Log.v(TAG, "Succeed to create listening socket on channel "
                            + mPortNum);
            } else {
                Log.e(TAG, "Error to create listening socket after "
                        + CREATE_RETRY_TIME + " try");
            }
            return initSocketOK;
        }

      private final void closeRfcommSocket(boolean server, boolean accept) throws IOException {
          if (VERBOSE) Log.v(TAG, "closeRfcommSocket server: "+server + "  conn: " +accept);
          if (server == true) {
            // Stop the possible trying to init serverSocket
            mInterrupted = true;

             if (mServerSocket != null) {
                 mServerSocket.close();
                 mServerSocket =null ;
             }
          }

          if (accept == true) {
             if (mConnSocket != null) {
                 mConnSocket.close();
                 mConnSocket = null;
             }
          }
       }

        public void closeConnection() {
            if (VERBOSE) Log.v(TAG, "Mas connection closing");
            try {
                closeRfcommSocket(true, true);
            } catch (IOException ex) {
                Log.e(TAG, "CloseSocket error: " + ex);
            }

            if (mAcceptThread != null) {
                try {
                    mAcceptThread.shutdown();
                    mAcceptThread.join();
                } catch (InterruptedException ex) {
                    Log.w(TAG, "mAcceptThread  close error" + ex);
                } finally {
                    mAcceptThread = null;
                }
            }

            if (mServerSession != null) {
                mServerSession.close();
                mServerSession = null;
            }

            if (VERBOSE) Log.v(TAG, "Mas connection closed");
        }

        private final void startObexServerSession(BluetoothDevice device, BluetoothMns mnsClient)
                        throws IOException {
            if (VERBOSE)
                Log.v(TAG, "Map Service startObexServerSession ");

            Context context = getApplicationContext();

            IBluetoothMasApp appIf = null;
            if (((mSupportedMessageTypes & ~MESSAGE_TYPE_SMS_MMS) == 0x00) &&
                    ((mSupportedMessageTypes & MESSAGE_TYPE_SMS) != 0x00) &&
                    ((mSupportedMessageTypes & MESSAGE_TYPE_MMS) != 0x00)) {
                // BluetoothMasAppZero if and only if both SMS and MMS
                appIf = new BluetoothMasAppSmsMms(context, mSessionStatusHandler, mnsClient,
                        mMasId, device.getName());
            } else if (((mSupportedMessageTypes & ~MESSAGE_TYPE_EMAIL) == 0x0) &&
                    ((mSupportedMessageTypes & MESSAGE_TYPE_EMAIL) != 0x0)) {
                // BluetoothMasAppOne if and only if email
                appIf = new BluetoothMasAppEmail(context, mSessionStatusHandler, mnsClient,
                        mMasId, device.getName());
            }

            mMapServer = new BluetoothMasObexServer(mSessionStatusHandler,
                    device, context, appIf);
            synchronized (mAuthSync) {
                mAuth = new BluetoothMapAuthenticator(mSessionStatusHandler);
                mAuth.setChallenged(false);
                mAuth.setCancelled(false);
            }
            BluetoothMapRfcommTransport transport = new BluetoothMapRfcommTransport(
                    mConnSocket);
            mServerSession = new ServerSession(transport, mMapServer, mAuth);

            if (VERBOSE) Log.v(TAG, "startObexServerSession() success!");
        }

        private void stopObexServerSession() {
            if (VERBOSE) Log.v(TAG, "Map Service stopObexServerSession ");

            try {
                closeRfcommSocket(false, true);
            } catch (IOException ex) {
                Log.e(TAG, "CloseSocket error: " + ex);
            }

            if (mAcceptThread != null) {
                try {
                    mAcceptThread.shutdown();
                    mAcceptThread.join();
                } catch (InterruptedException ex) {
                    Log.w(TAG, "mAcceptThread  close error" + ex);
                } finally {
                    mAcceptThread = null;
                }
            }

            if (mServerSession != null) {
                mServerSession.close();
                mServerSession = null;
            }

            // Last obex transaction is finished, we start to listen for incoming
            // connection again
            if (mAdapter.isEnabled()) {
                startRfcommSocketListener(mnsClient);
            }
        }

        /**
         * A thread that runs in the background waiting for remote rfcomm
         * connect.Once a remote socket connected, this thread shall be
         * shutdown.When the remote disconnect,this thread shall run again waiting
         * for next request.
         */
        private class SocketAcceptThread extends Thread {
            private boolean stopped = false;
            private BluetoothMns mnsObj;
            private int mMasId;

            public SocketAcceptThread(BluetoothMns mnsClient, int masId) {
                mnsObj = mnsClient;
                mMasId = masId;
            }

            @Override
            public void run() {
                while (!stopped && mServerSocket != null) {
                    try {
                        mConnSocket = mServerSocket.accept();
                        if (mConnSocket == null) {
                            Log.i(TAG, "CONNECTION SOCKET NULL");
                            break;
                        }
                        mRemoteDevice = mConnSocket.getRemoteDevice();
                        if (mRemoteDevice == null) {
                            Log.i(TAG, "getRemoteDevice() = null");
                            break;
                        }
                        String remoteDeviceName = mRemoteDevice.getName();
                        // In case getRemoteName failed and return null
                        if (TextUtils.isEmpty(remoteDeviceName)) {
                            remoteDeviceName = getString(R.string.defaultname);
                        }

                        if (!mConnectionManager.isAllowedConnection(mRemoteDevice)) {
                            mConnSocket.close();
                            mConnSocket = null;
                            continue;
                        }
                        boolean trust = false;
                        if (mRemoteDevice != null)
                           trust = mRemoteDevice.getTrustState();
                        if (VERBOSE) Log.v(TAG, "GetTrustState() = " + trust);
                        if (mIsRequestBeingNotified) {
                            if (VERBOSE) Log.v(TAG, "Request notification is still on going.");
                            mConnectionManager.setWaitingForConfirmation(mMasId);
                            break;
                        } else if (trust) {
                            if (VERBOSE) {
                                Log.v(TAG, "trust is true::");
                                Log.v(TAG, "incomming connection accepted from: "
                                        + remoteDeviceName + " automatically as trusted device");
                            }
                            try {
                                startObexServerSession(mRemoteDevice, mnsObj);
                            } catch (IOException ex) {
                                Log.e(TAG, "catch exception starting obex server session"
                                    + ex.toString());
                            }
                        } else {
                            if (VERBOSE) Log.v(TAG, "trust is false.");
                            mConnectionManager.setWaitingForConfirmation(mMasId);
                            createMapNotification(mRemoteDevice);
                            if (VERBOSE) Log.v(TAG, "incomming connection accepted from: "
                                + remoteDeviceName);
                            mSessionStatusHandler.sendMessageDelayed(
                                mSessionStatusHandler.obtainMessage(MSG_INTERNAL_USER_TIMEOUT),
                                    USER_CONFIRM_TIMEOUT_VALUE);
                        }
                        stopped = true; // job done ,close this thread;
                    } catch (IOException ex) {
                        if (VERBOSE)
                            Log.v(TAG, "Accept exception: " + ex.toString());
                        stopped = true; // close this thread from exception.
                    }
                }
            }

            void shutdown() {
                if (VERBOSE) Log.v(TAG, "AcceptThread shutdown for MAS id: " + mMasId);
                stopped = true;
                interrupt();
            }
        }
    }
};
