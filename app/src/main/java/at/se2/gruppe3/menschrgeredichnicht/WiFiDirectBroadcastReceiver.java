package at.se2.gruppe3.menschrgeredichnicht;

/**
 * Created by Oliver on 09.05.2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "WiFiDBC";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    public Connect mActivity;
    WifiP2pManager.PeerListListener myPeerListListener;


    private static final int SERVER_PORT = 12345;


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Connect activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;

        Log.d(TAG,"TEST SUCCEED");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.d(TAG,"Wifi P2P is enabled");

            } else {
                // Wi-Fi P2P is not enabled
                Log.d(TAG,"Wifi P2P is not enabled");
            }
        }

         else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {

                        mActivity.displayPeers(peers);


                        Log.d(TAG,String.format("PeerListListener: %d peers available, updating device list", peers.getDeviceList().size()));
                        Log.d(TAG,"Devices: "+peers.getDeviceList().toString());
                        // DO WHATEVER YOU WANT HERE
                        // YOU CAN GET ACCESS TO ALL THE DEVICES YOU FOUND FROM peers OBJECT



                        if (peers.toString().length() == 0) {
                            Log.d(TAG, "No devices found");
                            return;
                        }

                    }

                });
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections

            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {

                    if(p2pInfo.groupFormed){
                        if (!p2pInfo.isGroupOwner) {
                            // Joined group as client - connect to GO

                            mActivity.startClientService(new InetSocketAddress(p2pInfo.groupOwnerAddress, SERVER_PORT));



                        }else{


                        /*

                        try {
                            mActivity.startServer();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        */
                        }
                    }


                }
            });


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }

    }









}
