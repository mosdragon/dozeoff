package es.sakhi.osama.dozeoff;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements HeartRateConsentListener {
    public static final String PREFS_NAME = "MyPrefsFile";

    private static final String TAG = "MainActivity";

    private BandHeartRateEventListener heartRateListener;
    private BandClient bandClient;
    private BandPendingResult<ConnectionState> pendingResult;
    private boolean connected;

    private static final String CONNECT = "Start Driving";
    private static final String DISCONNECT = "Stop Driving";

    private Button bandConnection;
    private TextView heartRateView;
    private int heartRate;

    private boolean isFirst = true;
    private int restingHeartRate = 75;
    private int dozingOff = 65;
    private TextToSpeech t1;

    private MediaPlayer mp;
    private AudioManager audioManager;
    private int originalVol;

    private BandPendingResult<Void> disconnectResult;

    private ProgressDialog pd;

    private boolean alreadyRerouted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String shopName = prefs.getString("shopMode", "");
        System.out.println("#### " + shopName);
        if (shopName.equals("")) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        connected = false;
        bandConnection = (Button) findViewById(R.id.button);
        bandConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    disconnectBand();

                    if (mp != null && mp.isPlaying()) {
                        mp.stop();
                    }
                    if (audioManager != null) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                originalVol,
                                0);
                    }
                } else {
                    connectBand();
//                    blareAlarm();

                }
                connected = !connected;
                bandConnection.setText(connected ? DISCONNECT : CONNECT);

            }
        });
        bandConnection.setText(CONNECT);


        heartRateView = (TextView) findViewById(R.id.heart_rt);
        heartRate = restingHeartRate;
        heartRateView.setText("" + heartRate);


        //instantiate the texttospeech
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent goToSettings = new Intent(this, SettingsActivity.class);
            startActivity(goToSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toggleDrive(View view) {
        Button start = (Button) findViewById(R.id.button);
        if (start.getText() == "START DRIVING") {
            start.setText("STOP DRIVING");
        } else {
            start.setText("START DRIVING");
        }
    }

    public void openGoogleService(View view) {
        //Intent intent = getIntent();
        //String myShop = intent.getStringExtra(SettingsActivity.WHAT_SHOP);
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String myShop = prefs.getString("shopMode", "gas station");
        Uri anyAddress = Uri.parse("google.navigation:q=" + Uri.encode(myShop) + "&mode=d");
        Intent mapI = new Intent(Intent.ACTION_VIEW, anyAddress);
        mapI.setPackage("com.google.android.apps.maps");
        if (mapI.resolveActivity(getPackageManager()) != null) {
            startActivity(mapI);
        }

    }

    private void disconnectBand() {

        try {
            bandClient.getSensorManager().unregisterHeartRateEventListeners();
        } catch (BandIOException e) {
            e.printStackTrace();
        }
        disconnectResult = bandClient.disconnect();
        DisconnectAwaitTask task = new DisconnectAwaitTask();
        task.execute();

        alreadyRerouted = false;

        Log.d(TAG, "disconnectBand");

    }


    private void connectBand() {

        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
        bandClient = BandClientManager.getInstance().create(this, pairedBands[0]);

//        Note: the BandPendingResult.await() method must be called from a
//        background thread. An exception will be thrown if called from the UI
//        thread.
        bandClient.disconnect();
        pendingResult = bandClient.connect();

        alreadyRerouted = false;


        ConnectAwaitTask task = new ConnectAwaitTask();
        task.execute();

    }

    public void setBandRetrievalInterval() {}


    /**
     *
     * @param accepted, whether or not app has consent to get heart rate data
     */
    //@Override
    public void userAccepted(boolean accepted) {
        //is this the first heart rate
//        Get the thing
        if (accepted) {
            heartRateListener = new BandHeartRateEventListener() {

                @Override
                public void onBandHeartRateChanged(BandHeartRateEvent event) {
                    // do work on heart rate changed (i.e. update UI)
                    heartRate = event.getHeartRate();
                    if(isFirst) {
                        //if its the first poll, use it as the base
                        //for setting what their dozing off heart rate will be
                        restingHeartRate = heartRate;
                        //lets go with 10% decrease for dozing off
                        dozingOff = (int)((double)restingHeartRate - ((double)restingHeartRate * .1));
                        Log.d(TAG, "resting: " + restingHeartRate);
                        Log.d(TAG, "dozing: " + dozingOff);
                        isFirst = false;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            heartRateView.setText("" + heartRate);
                        }
                    });
                    if (heartRate < 73) {
//                                pd.show();

                        disconnectBand();
                        if ((mp == null || !mp.isPlaying()) && !alreadyRerouted) {
                            blareAlarm();
                            alreadyRerouted = true;
                        }

//                                blareAlarm();
                    } else {
//                                pd.hide();
//                        if (mp != null && mp.isPlaying()) {
//                            mp.stop();
//                        }
//                        if (audioManager != null) {
//                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                                    originalVol,
//                                    AudioManager.FLAG_SHOW_UI);
//                        }
                    }
                    Log.d(TAG, "HEART RATE: " + heartRate);
                }
            };
//            ListenerService.startListener(this, bandClient, heartRateListener);

            try {
                // register the listener

                pd = new ProgressDialog(this);
                pd.setTitle("BE ALARMED");
                bandClient.getSensorManager().registerHeartRateEventListener(heartRateListener);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        blareAlarm();

                    }
                }, 4000);


            } catch (BandIOException ex) {
                // handle BandException
                ex.printStackTrace();

            } catch (BandException e) {
                e.printStackTrace();
            }

        }

    }

    public void speakSleepInstructions() {
        String sleepInstructions = "Did you fall asleep? I'm re-routing you.";
        //text to speech
        t1.speak(sleepInstructions, TextToSpeech.QUEUE_FLUSH, null);
        //look into a wait until tts ends method before calling reroute
        reroute();
        disconnectBand();
        connected = !connected;
        bandConnection.setText(connected ? DISCONNECT : CONNECT);
    }

    public void reroute() {
        //this will start the maps
        //idk what the exact string is so for now
        //our default is a truck stop
//        sendImpIntMaps("gas station");
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String myShop = prefs.getString("shopMode", "coffee");
        Uri anyAddress = Uri.parse("google.navigation:q=" + Uri.encode(myShop) + "&mode=d");
        Intent mapI = new Intent(Intent.ACTION_VIEW, anyAddress);
        mapI.setPackage("com.google.android.apps.maps");
        if (mapI.resolveActivity(getPackageManager()) != null) {
            startActivity(mapI);
        }

    }

    public void sendImpIntMaps(String location) {
        //location can be gas station, coffee, rest stop, etc
        Uri anyAddress = Uri.parse("google.navigation:q=" + Uri.encode(location) + "&mode=d");
        Intent mapI = new Intent(Intent.ACTION_VIEW, anyAddress);
        mapI.setPackage("com.google.android.apps.maps");
        if (mapI.resolveActivity(getPackageManager()) != null) {
            startActivity(mapI);
        }
    }

    public void blareAlarm() {
        increaseVolume();
        //set up MediaPlayer
        mp = MediaPlayer.create(this, R.raw.alarm);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                speakSleepInstructions();
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        originalVol,
                        0);
            }
        });
        mp.start();


//        try {
//            mp.setDataSource(path+ File.separator+fileName);
//            mp.prepare();
//            mp.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void increaseVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int original = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
    }

    /**
     * Used as a seperate thread to await the connection state of the band
     */
    private class ConnectAwaitTask extends AsyncTask<Void, Void, Void> {

        private ConnectionState state;

        @Override
        protected Void doInBackground(Void... params) {
            try {

                state = pendingResult.await();


            } catch(InterruptedException ex) {
//            handle InterruptedException
                ex.printStackTrace();

            } catch(BandException ex) {
//            handle BandException
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (state == ConnectionState.CONNECTED) {
//                do work on success

                // check current user heart rate consent
                if (bandClient.getSensorManager().getCurrentHeartRateConsent() !=
                        UserConsent.GRANTED) {

                    // user has not consented, request it
                    // the calling class is both an Activity and implements
                    // HeartRateConsentListener
                    bandClient.getSensorManager().requestHeartRateConsent(
                            MainActivity.this,
                            MainActivity.this);
                } else {
                    Log.d(TAG, " Consent Already Granted");
                    userAccepted(true);
                }

            } else {
//                do work on failure
                Log.e(TAG, "ConnectionState failure");
            }
        }
    }

    /**
     * Used as a seperate thread to await the connection state of the band
     */
    private class DisconnectAwaitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                disconnectResult.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BandException e) {
                e.printStackTrace();
            }

            return null;

        }
    }
}
