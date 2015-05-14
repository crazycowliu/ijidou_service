/*
 * Copyright (c) 2013, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *            notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *            notice, this list of conditions and the following disclaimer in the
 *            documentation and/or other materials provided with the distribution.
 *        * Neither the name of The Linux Foundation nor
 *            the names of its contributors may be used to endorse or promote
 *            products derived from this software without specific prior written
 *            permission.
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

package org.codeaurora.bluetooth.bttestapp;

import android.app.Activity;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHandsfreeClient;
import android.bluetooth.BluetoothMasInstance;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.bluetooth.BluetoothA2dp;

import org.codeaurora.bluetooth.mapclient.BluetoothMasClient;
import org.codeaurora.bluetooth.pbapclient.BluetoothPbapClient;
import org.codeaurora.bluetooth.bttestapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Iterator;

public class ServicesFragment extends ListFragment {

    private final static String TAG = "ServicesFragment";

    private MainActivity mActivity;

    private ServicesAdapter mAdapter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean connState = false;
            boolean notifState = false;
            boolean connEvent = false;
            boolean notifEvent = false;
            int idx = -1;

            Log.v(TAG, "mReceiver got " + action);

            if (ProfileService.ACTION_HFP_CONNECTION_STATE.equals(action)) {
                connState = intent.getBooleanExtra(ProfileService.EXTRA_CONNECTED, false);

                Service srv = new Service(Service.Type.HFP, null);
                idx = mAdapter.getItemPos(srv);

                if (idx < 0) {
                    Log.w(TAG, "Cannot find HFP service item");
                }

                connEvent = true;

            } else if (ProfileService.ACTION_AVRCP_CONNECTION_STATE.equals(action)) {
                connState = intent.getBooleanExtra(ProfileService.EXTRA_CONNECTED, false);

                Service srv = new Service(Service.Type.AVRCP, null);
                idx = mAdapter.getItemPos(srv);

                if (idx < 0) {
                    Log.w(TAG, "Cannot find AVRCP service item");
                }
                Log.v(TAG, "idx: " + idx + " connection state: " + connState);
                connEvent = true;

            } else if (ProfileService.ACTION_PBAP_CONNECTION_STATE.equals(action)) {
                connState = intent.getBooleanExtra(ProfileService.EXTRA_CONNECTED, false);

                Service srv = new Service(Service.Type.PBAP, null);
                idx = mAdapter.getItemPos(srv);

                if (idx < 0) {
                    Log.w(TAG, "Cannot find PBAP service item");
                }

                connEvent = true;

            } else if (ProfileService.ACTION_MAP_CONNECTION_STATE.equals(action)) {
                connState = intent.getBooleanExtra(ProfileService.EXTRA_CONNECTED, false);
                BluetoothMasInstance inst = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_MAS_INSTANCE);

                Service srv = new Service(Service.Type.MAP, inst);
                idx = mAdapter.getItemPos(srv);

                if (idx < 0) {
                    Log.w(TAG, "Cannot find MAP service item");
                }

                connEvent = true;

            } else if (ProfileService.ACTION_MAP_NOTIFICATION_STATE.equals(action)) {
                notifState = intent.getBooleanExtra(ProfileService.EXTRA_NOTIFICATION_STATE, false);
                BluetoothMasInstance inst = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_MAS_INSTANCE);

                Service srv = new Service(Service.Type.MAP, inst);
                idx = mAdapter.getItemPos(srv);

                if (idx < 0) {
                    Log.w(TAG, "Cannot find MAP service item");
                }

                notifEvent = true;

            }

            if (idx < 0) {
                return;
            }

            View v = (ServicesFragment.this).getListView().getChildAt(idx);
            if (v != null) {
                Switch swSrv = (Switch) v.findViewById(R.id.service_switch);
                Switch swNotif = (Switch) v.findViewById(R.id.notification_switch);

                if (connEvent) {
                    swSrv.setChecked(connState);
                    Service srv = (Service)mAdapter.getItem(idx);
                    if (srv.mType.equals(Service.Type.AVRCP)) {
                        Log.w(TAG, "not enabling checkbox as it is AVRCP");
                        swSrv.setEnabled(false);
                    } else {
                        swSrv.setEnabled(true);
                    }
                    swNotif.setEnabled(connState);
                    swNotif.setChecked(false);
                }
                if (notifEvent) {
                    swNotif.setChecked(notifState);
                    swNotif.setEnabled(true);
                }
            }
        }
    };

    static final class Service {

        final Type mType;

        final BluetoothMasInstance mMasInstance;

        enum Type {
            HFP("Hands-Free Profile (AG)"),
            PBAP("Phone Book Access Profile (PSE)"),
            MAP("Message Access Profile (MSE)"),
            AVRCP("Audio Video Remote Control Profile (TG)");

            final String mTitle;

            Type(String title) {
                mTitle = title;
            }
        }

        Service(Type type, BluetoothMasInstance masInstance) {
            mType = type;
            mMasInstance = masInstance;
        }

        @Override
        public boolean equals(Object srv) {
            if (!mType.equals(((Service) srv).mType)) {
                return false;
            }

            if (mType.equals(Type.MAP)) {
                return mMasInstance.equals(((Service) srv).mMasInstance);
            }

            return true;
        }
    }

    private class ServicesAdapter extends BaseAdapter implements ListAdapter,
            OnCheckedChangeListener {

        private final LayoutInflater mInflater;

        private final ArrayList<Service> mServices = new ArrayList<Service>();

        public ServicesAdapter(Context context) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mServices.size();
        }

        @Override
        public Object getItem(int position) {
            return mServices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            boolean bluetoothOn = BluetoothAdapter.getDefaultAdapter().isEnabled();

            Log.v(TAG, "getView");
            if (v == null) {
                v = mInflater.inflate(R.layout.service_row, parent, false);
            }

            Service srv = mServices.get(position);

            TextView txtName = (TextView) v.findViewById(R.id.service_name);
            txtName.setText(srv.mType.mTitle);

            TextView txtTitle = (TextView) v.findViewById(R.id.service_title);
            if (srv.mType.equals(Service.Type.MAP)) {
                txtTitle.setText(srv.mMasInstance.getName());
            } else {
                txtTitle.setText("");
            }

            Switch swSrv = (Switch) v.findViewById(R.id.service_switch);
            swSrv.setTag(Integer.valueOf(position));
            swSrv.setOnCheckedChangeListener(this);

            Switch swNotif = (Switch) v.findViewById(R.id.notification_switch);
            if (srv.mType.equals(Service.Type.MAP)) {
                swNotif.setVisibility(View.VISIBLE);
                swNotif.setTag(Integer.valueOf(position));
                swNotif.setOnCheckedChangeListener(this);
            } else {
                swNotif.setVisibility(View.INVISIBLE);
            }

            // need to update switch state on new view
            if (mActivity.mProfileService != null) {
                switch (srv.mType) {
                    case HFP: {
                        BluetoothHandsfreeClient cli = mActivity.mProfileService.getHfpClient();

                        if (cli == null || bluetoothOn == false) {
                            swSrv.setChecked(false);
                            swSrv.setEnabled(false);
                            break;
                        }

                        switch (cli.getConnectionState(mActivity.mDevice)) {
                            case BluetoothProfile.STATE_DISCONNECTED:
                                swSrv.setChecked(false);
                                swSrv.setEnabled(true);
                                break;
                            case BluetoothProfile.STATE_CONNECTING:
                                swSrv.setChecked(true);
                                swSrv.setEnabled(false);
                                break;
                            case BluetoothProfile.STATE_CONNECTED:
                                swSrv.setChecked(true);
                                swSrv.setEnabled(true);
                                break;
                            case BluetoothProfile.STATE_DISCONNECTING:
                                swSrv.setChecked(false);
                                swSrv.setEnabled(false);
                                break;
                        }

                        break;
                    }

                    case PBAP: {
                        BluetoothPbapClient cli = mActivity.mProfileService.getPbapClient();

                        if (cli == null || bluetoothOn == false) {
                            swSrv.setChecked(false);
                            swSrv.setEnabled(false);
                            break;
                        }

                        switch (cli.getState()) {
                            case DISCONNECTED:
                                swSrv.setChecked(false);
                                swSrv.setEnabled(true);
                                break;
                            case CONNECTING:
                                swSrv.setChecked(true);
                                swSrv.setEnabled(false);
                                break;
                            case CONNECTED:
                                swSrv.setChecked(true);
                                swSrv.setEnabled(true);
                                break;
                            case DISCONNECTING:
                                swSrv.setChecked(false);
                                swSrv.setEnabled(false);
                                break;
                        }

                        break;
                    }

                    case MAP: {
                        BluetoothMasClient cli = mActivity.mProfileService
                                .getMapClient(srv.mMasInstance.getId());

                        swNotif.setEnabled(false);

                        if (cli == null || bluetoothOn == false) {
                            swSrv.setChecked(false);
                            swSrv.setEnabled(false);
                            break;
                        }

                        switch (cli.getState()) {
                            case DISCONNECTED:
                                swSrv.setChecked(false);
                                swSrv.setEnabled(true);
                                break;
                            case CONNECTING:
                                swSrv.setChecked(true);
                                swSrv.setEnabled(false);
                                break;
                            case CONNECTED:
                                swSrv.setChecked(true);
                                swSrv.setEnabled(true);
                                swNotif.setEnabled(true);
                                break;
                            case DISCONNECTING:
                                swSrv.setChecked(false);
                                swSrv.setEnabled(false);
                                break;
                        }

                        swNotif.setChecked(cli.getNotificationRegistration());

                        break;
                    }

                    case AVRCP: {
                        Log.v(TAG, "getView: AVRCP");
                        BluetoothA2dp cli = mActivity.mProfileService.getA2dp();

                        if (cli == null || bluetoothOn == false) {
                            swSrv.setChecked(false);
                            swSrv.setEnabled(false);
                            break;
                        }

                        if (cli.isAvrcpConnected(mActivity.mDevice)) {
                            Log.v(TAG, "AVRCP Connected: setChecked to True");
                            swSrv.setChecked(true);
                            swSrv.setEnabled(false);
                        } else {
                            Log.v(TAG, "AVRCP Not Connected: setChecked to False");
                            swSrv.setChecked(false);
                            swSrv.setEnabled(false);
                        }
                        break;
                    }

                }
            }

            return v;
        }

        public int getItemPos(Service srv) {
            for (int i = 0; i < mServices.size(); i++) {
                if (mServices.get(i).equals(srv)) {
                    return i;
                }
            }

            return -1;
        }

        public void addService(Service.Type type, BluetoothMasInstance masInstance) {
            Service srv = new Service(type, masInstance);

            if (!mServices.contains(srv)) {
                mServices.add(srv);
            }

            notifyDataSetChanged();
        }

        public void removeService(Service.Type type) {
            for (Iterator<Service> iter = mServices.iterator(); iter.hasNext();) {
                Service srv = iter.next();

                if (type == null || srv.mType.equals(type)) {
                    iter.remove();
                }
            }

            notifyDataSetChanged();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Service srv = mServices.get((Integer) buttonView.getTag());

            switch (srv.mType) {
                case HFP:
                    if (isChecked) {
                        mActivity.mProfileService.getHfpClient().connect(mActivity.mDevice);
                        buttonView.setEnabled(false);
                    } else {
                        mActivity.mProfileService.getHfpClient().disconnect(mActivity.mDevice);
                        buttonView.setEnabled(false);
                    }
                    break;

                case PBAP:
                    if (isChecked) {
                        mActivity.mProfileService.getPbapClient().connect();
                        buttonView.setEnabled(false);
                    } else {
                        mActivity.mProfileService.getPbapClient().disconnect();
                        buttonView.setEnabled(false);
                    }
                    break;

                case MAP:
                    BluetoothMasClient cli = mActivity.mProfileService
                            .getMapClient(srv.mMasInstance.getId());
                    if (isChecked) {
                        if (buttonView.getId() == R.id.service_switch) {
                            cli.connect();
                        } else {
                            cli.setNotificationRegistration(true);
                        }
                        buttonView.setEnabled(false);
                    } else {
                        if (buttonView.getId() == R.id.service_switch) {
                            cli.disconnect();
                        } else {
                            cli.setNotificationRegistration(false);
                        }
                        buttonView.setEnabled(false);
                    }
                    break;
                  
                    case AVRCP: 
                      // not to be handled
                   break; 
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ServicesAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.getClass().getName()
                    + " can only be attached to MainActivity!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ProfileService.ACTION_HFP_CONNECTION_STATE);
        filter.addAction(ProfileService.ACTION_PBAP_CONNECTION_STATE);
        filter.addAction(ProfileService.ACTION_MAP_CONNECTION_STATE);
        filter.addAction(ProfileService.ACTION_AVRCP_CONNECTION_STATE);
        filter.addAction(ProfileService.ACTION_MAP_NOTIFICATION_STATE);
        getActivity().registerReceiver(mReceiver, filter);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        Service srv = (Service) mAdapter.getItem(position);
        Intent intent = null;

        if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) return;

        switch (srv.mType) {
            case PBAP:
                intent = new Intent(getActivity(), PbapTestActivity.class);
                break;

            case HFP:
                if (mActivity.mProfileService.getHfpClient() != null)
                    intent = new Intent(getActivity(), HfpTestActivity.class);
                else {
                    Log.v(TAG, "HfpClient service is null");
                    return;
                }
                break;

            case MAP:
                intent = new Intent(getActivity(), MapTestActivity.class);
                intent.putExtra(ProfileService.EXTRA_MAP_INSTANCE_ID, srv.mMasInstance.getId());
                break;

            case AVRCP:
                if (mActivity.mProfileService.getA2dp().
                    isAvrcpConnected(mActivity.mDevice) != false)
                    intent = new Intent(getActivity(), AvrcpTestActivity.class);
                else {
                    Log.e(TAG, "AVRCP is not connected");
                    return;
                }
                break;

            default:
                // this should never happen!
                throw new IllegalArgumentException();
        }

        startActivity(intent);
    }

    public void addService(Service.Type type, BluetoothMasInstance masInstance) {
        mAdapter.addService(type, masInstance);
    }

    public void removeService(Service.Type type) {
        mAdapter.removeService(type);
    }

    public void persistServices() {
        SharedPreferences.Editor prefs = getActivity().getPreferences(Context.MODE_PRIVATE).edit();

        JSONArray json = new JSONArray();

        int cnt = mAdapter.getCount();
        for (int i = 0; i < cnt; i++) {
            Service srv = (Service) mAdapter.getItem(i);

            try {
                JSONObject jsrv = new JSONObject();

                jsrv.put("type", srv.mType);

                if (srv.mMasInstance != null) {
                    jsrv.put("i_id", srv.mMasInstance.getId());
                    jsrv.put("i_name", srv.mMasInstance.getName());
                    jsrv.put("i_scn", srv.mMasInstance.getChannel());
                    jsrv.put("i_msg", srv.mMasInstance.getMsgTypes());
                }

                json.put(jsrv);
            } catch (JSONException e) {
            }
        }

        prefs.putString(MainActivity.PREF_SERVICES, json.toString());

        prefs.commit();
    }

    public void restoreServices() {
        String str = getActivity().getPreferences(Context.MODE_PRIVATE).getString(
                MainActivity.PREF_SERVICES, null);

        ArrayList<BluetoothMasInstance> insts = new ArrayList<BluetoothMasInstance>();

        if (str == null) {
            return;
        }

        try {
            Object obj = new JSONTokener(str).nextValue();

            if (!(obj instanceof JSONArray)) {
                return;
            }

            JSONArray json = (JSONArray) obj;

            for (int i = 0; i < json.length(); i++) {
                JSONObject jsrv = json.getJSONObject(i);

                String stype = jsrv.getString("type");
                Service.Type type = null;
                BluetoothMasInstance inst = null;

                for (Service.Type t : Service.Type.values()) {
                    if (t.name().equals(stype)) {
                        type = t;
                        break;
                    }
                }

                if (type == null) {
                    continue;
                }

                if (type.equals(Service.Type.MAP)) {
                    inst = new BluetoothMasInstance(jsrv.getInt("i_id"), jsrv.getString("i_name"),
                            jsrv.getInt("i_scn"), jsrv.getInt("i_msg"));
                    insts.add(inst);
                }

                mAdapter.addService(type, inst);
            }
        } catch (JSONException e) {
        }

        mActivity.mProfileService.setMasInstances(insts);
    }
}
