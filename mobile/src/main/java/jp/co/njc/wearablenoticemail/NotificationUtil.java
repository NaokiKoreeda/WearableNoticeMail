package jp.co.njc.wearablenoticemail;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by njc50031 on 2014/12/24.
 */
public class NotificationUtil {

    public static PendingIntent getExamplePendingIntent(Context context, int messageResId) {
        Intent intent = new Intent(NotificationIntentReceiver.ACTION_EXAMPLE)
                .setClass(context, NotificationIntentReceiver.class);
        return PendingIntent.getBroadcast(context, messageResId /* requestCode */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
