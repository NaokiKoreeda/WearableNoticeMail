package jp.co.njc.wearablenoticemail;

import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * DataLayerListenerService
 * Created by njc50031 on 2014/12/19.
 */
public class DataLayerListenerService extends WearableListenerService {

    public static final String START_ACTIVITY_PATH = "/notice";
    public static final String TAP_ACTION_PATH = "/tap";
    private static final String TAG = "DataLayerListenerService";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (START_ACTIVITY_PATH.equals(messageEvent.getPath())) {
            Log.d(TAG, "Message Received !!");

            int vibeTime = 5000;
            String strVibeTime = new String(messageEvent.getData());
            if (!strVibeTime.equals("") && !strVibeTime.equals("0")) {
                vibeTime = (Integer.parseInt(strVibeTime)) * 1000;
            }

            // メール通知とかぶらないように待機
            sleep(1000);
            // バイブレーション
            vibrator.vibrate(vibeTime);

        } else if (TAP_ACTION_PATH.equals(messageEvent.getPath())) {
            Log.d(TAG, "Tapping Received !!");
            vibrator.cancel();
        }
    }

    public synchronized void sleep(long msec) {
        try
        {
            wait(msec);
        }catch(InterruptedException e){
            Log.e(TAG, e.getMessage());
        }
    }
}
