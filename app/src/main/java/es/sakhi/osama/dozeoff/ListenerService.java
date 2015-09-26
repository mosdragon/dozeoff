package es.sakhi.osama.dozeoff;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ListenerService extends IntentService {

    private static final String TAG = "ListenerService";



    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startListener(Context context) {
        Intent intent = new Intent(context, ListenerService.class);
        context.startService(intent);
    }

    public ListenerService() {
        super("ListenerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

//        if (android.os.Debug.isDebuggerConnected()) {
//            android.os.Debug.waitForDebugger();
//        };

    }

    public void openNav() {}

    public void findCoffee() {}

    public void findGasStation() {}

    public void blareAlarm() {
        increaseVolume();
        //set up MediaPlayer
        MediaPlayer mp = MediaPlayer.create(this, R.raw.alarmsound);
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
