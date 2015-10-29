package com.lagoru.quadromini;

import java.net.InetAddress;
import java.net.UnknownHostException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    private SharedPreferences mPreferences;
    private NetworkingThread mNetworkThread = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        JoystickView joystick1 = (JoystickView) findViewById(R.id.joystick_view_1);
        JoystickView joystick2 = (JoystickView) findViewById(R.id.joystick_view_2);
        joystick1.setOnJostickMovedListener(_listenerLeft);
        joystick2.setOnJostickMovedListener(_listenerRight);
        Button options_button = (Button) findViewById(R.id.options_button);
        options_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PreferenceActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        final Button power_button = (Button) findViewById(R.id.on_off_button);
        power_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNetworkThread.isQuadrocopterRunning()){
                    mNetworkThread.setQuadrocopterRunning(false);
                    power_button.setText(R.string.turn_on_string);
                }else{
                    mNetworkThread.setQuadrocopterRunning(true);
                    power_button.setText(R.string.turn_off_string);
                }
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
        }
        updateNetworking();
        mNetworkThread.start();
    }

    public synchronized void stopNetworkingThread() {
        if (mNetworkThread != null) {
            mNetworkThread.requestStop();
        }
        mNetworkThread = null;
    }

    @Override
    protected void onPause() {
        //suspending networking thread
        stopNetworkingThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startNetworkingThread();
    }
}
