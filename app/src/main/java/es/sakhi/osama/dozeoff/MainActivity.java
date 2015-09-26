package es.sakhi.osama.dozeoff;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements HeartRateConsentListener {

    private static final String TAG = "MainActivity";

    private BandHeartRateEventListener heartRateListener;
    private BandClient bandClient;
    private BandPendingResult<ConnectionState> pendingResult;
    private boolean connected;

    private static final String CONNECT = "Connect";
    private static final String DISCONNECT = "Disconnect";

    private Button bandConnection;
    private TextView heartRateView;
    private int heartRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListenerService.startListener(this);

        connected = false;
        bandConnection = (Button) findViewById(R.id.bandConnection);
        bandConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    disconnectBand();

                } else {
                    connectBand();

                }
                connected = !connected;
                bandConnection.setText(connected ? DISCONNECT : CONNECT);
            }
        });
        bandConnection.setText(CONNECT);
        heartRateView = (TextView) findViewById(R.id.heartRate);
        heartRate = 80;
        heartRateView.setText("" + heartRate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void disconnectBand() {
        bandClient.disconnect();
        Log.d(TAG, "disconnectBand");
    }


    private void connectBand() {

        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
        bandClient = BandClientManager.getInstance().create(this, pairedBands[0]);

//        Note: the BandPendingResult.await() method must be called from a
//        background thread. An exception will be thrown if called from the UI
//        thread.
        pendingResult = bandClient.connect();


        AwaitTask task = new AwaitTask();
        task.execute();

    }

    public void setBandRetrievalInterval() {}


    /**
     *
     * @param accepted, whether or not app has consent to get heart rate data
     */
    @Override
    public void userAccepted(boolean accepted) {
//        Get the thing
        if (accepted) {
            heartRateListener = new BandHeartRateEventListener() {

                @Override
                public void onBandHeartRateChanged(BandHeartRateEvent event) {
                    // do work on heart rate changed (i.e. update UI)
                    int rate = event.getHeartRate();
                    Log.d(TAG, "HEART RATE: " + rate);
                    heartRateView.setText("" + heartRate);
                }
            };

            try {
                // register the listener
                bandClient.getSensorManager().registerHeartRateEventListener(heartRateListener);

            } catch (BandIOException ex) {
                // handle BandException
                ex.printStackTrace();

            } catch (BandException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Used as a seperate thread to await the connection state of the band
     */
    private class AwaitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                ConnectionState state = pendingResult.await();

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
            } catch(InterruptedException ex) {
//            handle InterruptedException
                ex.printStackTrace();

            } catch(BandException ex) {
//            handle BandException
                ex.printStackTrace();
            }

            return null;

        }
    }
}
