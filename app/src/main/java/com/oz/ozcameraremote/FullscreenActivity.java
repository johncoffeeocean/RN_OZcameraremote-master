package com.oz.ozcameraremote;

import static android.webkit.WebSettings.PluginState.ON;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.IBinder;
import android.os.Message;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


import com.oz.ozcameraremote.databinding.ActivityFullscreenBinding;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;
    public static WebAppContext wac = new WebAppContext();
    public static byte[] rxdata;
    public static int rxoffset = 0;
    public static String lastsval = "0.0";

    public static int flipFlag = 1; // normal: 1 fliped: -1
    public static float prevRotation = 0; // previous mRotation[0]
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    boolean usbBound = false;
    private SensorManager sensorManager;
    private Sensor sensor;
    private MyRenderer mRenderer;


    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            System.out.println("USB service connected");
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
            usbBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            System.out.println("USB service disconnected");
            usbService = null;
            usbBound = true;
        }
    };

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityFullscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        givePermisson();
        // Allow strict mode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        rxdata = new byte[256];

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mHandler = new MyHandler(this);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRenderer = new MyRenderer();

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;

        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        String url = getIntent().getStringExtra("url");
        if (url.isEmpty()) {
            url = "http://192.168.1.211/cameracontrol.html?yo";
        }
        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.addJavascriptInterface(new WebAppInterface(wac), "ozjsi");
        myWebView.loadUrl(url);
        //myWebView.loadUrl("https://proxy.ozapi.net/camctl.html");
        //myWebView.loadUrl("https://google.com");


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {

                System.out.println("Fresh fresh");
                while (true) {
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        System.out.println("YOYOYO");
                        byte[] ff = new byte[8];
                        ff[0] = (byte) 255;
                        ff[1] = (byte) 255;
                        ff[2] = (byte) 255;
                        ff[3] = (byte) 255;
                        ff[4] = (byte) 255;
                        ff[5] = (byte) 255;
                        ff[6] = (byte) 255;
                        ff[7] = (byte) 255;

                        rxoffset = 0;
                        usbService.write(ff);

                        // display.append("h");
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        ).start();

        binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    private static class MyHandler extends Handler {
        private final WeakReference<FullscreenActivity> mActivity;

        public MyHandler(FullscreenActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    byte[] data = (byte[]) msg.obj;
                    //mActivity.get().display.append(data);
                    byte b;
                    String s="";
                    try {
                        for (int i=0;i<data.length;i++){
                            rxdata[rxoffset] = data[i];
                            rxoffset ++;
                        }
                        if(rxoffset < 8) return;
                        for (int i=0;i<rxdata.length;i++){
                            if (rxdata[i]!=-1)
                                s += ","+rxdata[i];
                        }
                        System.out.println(rxdata.length);

//                        mActivity.get().display.setText(s);

                        String sval="na";

                        if (s.startsWith(",-41")) {
                            sval = ""+rxdata[1];
                        } else {
                            wac.zoomval = "0.0";
                            System.out.println("zero");
                            return;
                        }

                        System.out.println(sval);

                        if      (sval.equals("-31"))sval="0.99";
                        else if (sval.equals("-29"))sval="0.7";
                        else if (sval.equals("-27"))sval="0.6";
                        else if (sval.equals("-25"))sval="0.5";
                        else if (sval.equals("-23"))sval="0.4";
                        else if (sval.equals("-21"))sval="0.3";
                        else if (sval.equals("-19"))sval="0.2";
                        else if (sval.equals("-17"))sval="0.0";

                        else if (sval.equals("-15"))sval="-0.99";
                        else if (sval.equals("-13"))sval="-0.7";
                        else if (sval.equals("-11"))sval="-0.6";
                        else if (sval.equals("-9"))sval="-0.5";
                        else if (sval.equals("-7"))sval="-0.4";
                        else if (sval.equals("-5"))sval="-0.2";
                        else if (sval.equals("-3"))sval="0.0";
                        else break;

                        if (lastsval.equals(sval))return;
                        System.out.println(sval);

                        lastsval = sval;
                        wac.zoomval = sval;


                    } catch(Exception e)
                    {
                        System.out.println("Exception:"+e.getMessage());
                    }
                    break;
            }
        }
    }

    public boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void givePermisson(){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.CAMERA,Manifest.permission.RECEIVE_SMS};

        if(!hasPermissions(this, PERMISSIONS)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, PERMISSION_ALL);
            }
        }
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.start();
        setFilters();  // Start listening notifications from UsbService
        Intent intent = new Intent(this, UsbService.class);
        startService(intent);
        //startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
        bindService(intent, usbConnection, Context.BIND_AUTO_CREATE);
    }


    class MyRenderer implements SensorEventListener {
        private Sensor mRotationVectorSensor;
        private final float[] mRotationMatrix = new float[16];
        private DatagramSocket udpsocket;
        private InetAddress addr;

        public MyRenderer() {
            // find the rotation-vector sensor
            mRotationVectorSensor = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ROTATION_VECTOR);
            // initialize the rotation matrix to identity
            mRotationMatrix[ 0] = 1;
            mRotationMatrix[ 4] = 1;
            mRotationMatrix[ 8] = 1;
            mRotationMatrix[12] = 1;
            try {
                udpsocket = new DatagramSocket();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void start() {
            // enable our sensor when the activity is resumed, ask for
            // 10 ms updates.
            sensorManager.registerListener(this, mRotationVectorSensor, 16683);
        }
        public void stop() {
            // make sure to turn our sensor off when the activity is paused
            sensorManager.unregisterListener(this);
        }

        public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.
                if(prevRotation * mRotationMatrix[0] < 0) {
                    flipFlag = 0 - flipFlag;
                    System.out.println("flipped: " + flipFlag);
                }
                event.values[1] *= flipFlag;
                event.values[2] *= flipFlag;
                prevRotation = mRotationMatrix[0];

                SensorManager.getRotationMatrixFromVector( mRotationMatrix , event.values);


//                System.out.println("GYRO " + mRotationMatrix[0] + " " + mRotationMatrix[1] + " " + mRotationMatrix[2] + " " + mRotationMatrix[3]);


                if(!wac.paused && wac.proxyport != 0) {
                    try {
                        addr = InetAddress.getByName(wac.proxyaddress);
                        String json = wac.cameraid + "{ \"pansetpoint\":" + mRotationMatrix[1] * 57.295778666661658591198025129101 +
                                ",\"tiltsetpoint\":" + mRotationMatrix[0] * -57.295778666661658591198025129101 +
                                ",\"zoom\":" + lastsval + "}";
                        System.out.println(json);
                        byte[] message = json.getBytes();
                        DatagramPacket packet = new DatagramPacket(message, message.length, addr, wac.proxyport);
                        udpsocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }



            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}