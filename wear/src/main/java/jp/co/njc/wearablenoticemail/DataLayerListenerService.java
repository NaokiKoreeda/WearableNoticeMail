package jp.co.njc.wearablenoticemail;

import android.os.PowerManager;
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
    Vibrator vibrator;

    MessageEvent msgEvent;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        msgEvent = messageEvent;

        if (START_ACTIVITY_PATH.equals(messageEvent.getPath())) {
            Log.d(TAG, "Message Received !!");

            int vibeTime = 5000;
            String strVibeTime = new String(messageEvent.getData());
            if (!strVibeTime.equals("") && !strVibeTime.equals("0")) {
                vibeTime = (Integer.parseInt(strVibeTime)) * 1000;
            }

            Log.d(TAG, "vibeTime :" + String.valueOf(vibeTime));

            // メール通知とかぶらないように待機
            sleep(1000);

//            Context context = getApplicationContext();
//            Toast.makeText(context, "Vibration Time: " + vibeTime, Toast.LENGTH_LONG).show();

            //long[] strVibePtn = {500, vibeTime};
            // バイブレーション
            //vibrator.vibrate(vibeTime);

            long[] strVibePtn;
            if (vibeTime == 2000) {
                strVibePtn = new long[]{300, 700, 300, 700};
            } else if (vibeTime == 10000) {
                strVibePtn = new long[]{300, 700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700};
            } else {
                strVibePtn = new long[]{300, 700, 300, 700, 300, 700, 300, 700, 300, 700};
            }

            // バイブレーション
            vibrator.vibrate(strVibePtn, -1);

            PowerManager.WakeLock mWakeLock;
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(
                    (PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "MyWakeLockTag");
            mWakeLock.acquire(vibeTime);

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
