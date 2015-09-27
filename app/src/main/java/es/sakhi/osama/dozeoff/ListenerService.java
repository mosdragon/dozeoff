package es.sakhi.osama.dozeoff;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandHeartRateEventListener;

import java.io.Serializable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ListenerService extends IntentService {

    private static final String TAG = "ListenerService";

    private BandHeartRateEventListener heartRateListener;
    private BandClient bandClient;

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startListener(Context context, BandClient bandClient,
                                     BandHeartRateEventListener heartRateListener) {
        Intent intent = new Intent(context, ListenerService.class);

        Gson gson = new Gson();
        String client = gson.toJson(bandClient);
        String listener = gson.toJson(heartRateListener);

        intent.putExtra("bandClient", client);
        intent.putExtra("heartRateListener", listener);
        context.startService(intent);
    }

    public ListenerService() {
        super("ListenerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (android.os.Debug.isDebuggerConnected()) {
            android.os.Debug.waitForDebugger();
        };

        String client = intent.getStringExtra("bandClient");
        String listener = intent.getStringExtra("heartRateListener");

        Gson gson = new Gson();
        bandClient = gson.fromJson(client, BandClient.class);
        heartRateListener = gson.fromJson(listener, BandHeartRateEventListener.class);



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

    public void openNav() {}

    public void findCoffee() {}

    public void findGasStation() {}

    public void blareAlarm() {
        increaseVolume();
        //set up MediaPlayer
        MediaPlayer mp = MediaPlayer.create(this, R.raw.alarm);
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
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.FLAG_SHOW_UI);
    }


}
