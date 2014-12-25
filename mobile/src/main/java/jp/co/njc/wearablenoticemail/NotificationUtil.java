package jp.co.njc.wearablenoticemail;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * NotificationUtil
 * Created by njc50031 on 2014/12/24.
 */
public class NotificationUtil {

    public static final String EXTRA_MESSAGE =
            "com.example.android.support.wearable.notifications.MESSAGE";

    public static PendingIntent getExamplePendingIntent(Context context, int messageResId) {
        Intent intent = new Intent(NotificationIntentReceiver.ACTION_EXAMPLE)
                .setClass(context, NotificationIntentReceiver.class);
        intent.putExtra(EXTRA_MESSAGE, context.getString(messageResId));
        return PendingIntent.getBroadcast(context, messageResId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
