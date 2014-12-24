package jp.co.njc.wearablenoticemail;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
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
            Log.i(TAG, "Message Received !!");

            // メール通知とかぶらないように待機
            sleep(1000);
//
//            int notificationId = 1;
//            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//            notificationManagerCompat.notify(notificationId, builder.build());


            vibrator.vibrate(10000);

        } else if (TAP_ACTION_PATH.equals(messageEvent.getPath())) {
            Log.i(TAG, "Tapping Received !!");
            vibrator.cancel();
        }
    }

    private static NotificationCompat.Builder applyBasicOptions(Context context,
                                                                NotificationCompat.Builder builder,
                                                                NotificationCompat.WearableExtender wearableOptions) {
        builder.setContentTitle("重要！！")
                .setContentText("重要メールを確認")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_HIGH);
//                .setDeleteIntent(NotificationUtil.getExamplePendingIntent(
//                        context, R.string.example_notification_deleted));
//        if (options.hasContentIntent) {
//            builder.setContentIntent(NotificationUtil.getExamplePendingIntent(context,
//                    R.string.content_intent_clicked));
//        }
//        if (options.vibrate) {
//            builder.setVibrate(new long[] {0, 100, 50, 100} );
//        }
        return builder;
    }


    public synchronized void sleep(long msec) {
        try
        {
            wait(msec);
        }catch(InterruptedException e){
            //
        }
    }
}
