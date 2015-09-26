package es.sakhi.osama.dozeoff;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;

import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

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

    public void blareAlarm() {}


}
