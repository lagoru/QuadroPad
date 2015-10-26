package com.lagoru.quadromini;

import java.net.InetAddress;
import java.net.UnknownHostException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;


public class MainActivity extends Activity {

    private SharedPreferences mPreferences;
    private NetworkingThread mNetworkThread;
    private DualJoystickView mJoystick;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mJoystick = (DualJoystickView) findViewById(R.id.dual_joystick_view);
        mJoystick.setOnJostickMovedListener(_listenerLeft, _listenerRight);
        mJoystick.setMenuOpenListener(new MenuOpenListener() {
            @Override
            public void menuOpened() {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PreferenceActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void updateNetworking() {
        try {
            mNetworkThread.updateSettings(InetAddress.getByName(mPreferences.getString("ipaddress", "192.168.1.22")),
                    Integer.parseInt(mPreferences.getString("port", "4444")),
                    Integer.parseInt(mPreferences.getString("txinterval", "50")));
        } catch (UnknownHostException e) {
            // Networking exception
        }
    }

    private JoystickMovedListener _listenerLeft = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            mNetworkThread.setLeftX(pan);
            mNetworkThread.setLeftY(tilt);
            mNetworkThread.setLeftActive(true);
        }

        @Override
        public void OnReleased() {
            mNetworkThread.setLeftActive(false);
        }

        public void OnReturnedToCenter()  {
            mNetworkThread.setLeftActive(false);
        }
    };

    private JoystickMovedListener _listenerRight = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            mNetworkThread.setRightX(pan);
            mNetworkThread.setRightY(tilt);
            mNetworkThread.setRightActive(true);
        }

        @Override
        public void OnReleased() {
            mNetworkThread.setRightActive(false);
        }

        public void OnReturnedToCenter()  {
            mNetworkThread.setRightActive(false);
        }
    };

    /* Call this to start the main networking thread */
    public synchronized void startNetworkingThread() {
        if (mNetworkThread == null) {
            mNetworkThread = new NetworkingThread(getBaseContext());
            mNetworkThread.start();
        }
    }

    public synchronized void stopNetworkingThread() {
        if (mNetworkThread != null) {
            mNetworkThread.requestStop();
            mNetworkThread = null;
        }
    }

    @Override
    protected void onPause() {
        // End Ethernet communications
        stopNetworkingThread();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update networking settings
        updateNetworking();

        startNetworkingThread();
    }
}
