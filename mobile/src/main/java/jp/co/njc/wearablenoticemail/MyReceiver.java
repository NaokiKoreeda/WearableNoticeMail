package jp.co.njc.wearablenoticemail;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by njc50031 on 2014/12/17.
 */
public class MyReceiver extends BroadcastReceiver {

    static final String FILE_NAME = "WNM_FILE";
    public static final String START_ACTIVITY_PATH = "/notice";
    private static final String TAG = "Receiver";

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        // 接続時に行いたい処理を記述する
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        // 一時的に切断された際に行いたい処理を記述する
                    }
                }).build();
        mGoogleApiClient.connect();

//        sleep(8000);
        Log.d(TAG, "You've Got Mail!!");
        for (String key : intent.getExtras().keySet()) {
            Log.d(TAG, key + ": " + intent.getExtras().get(key));
        }

        // アカウント取得
        String account = "";
        if (intent.getExtras().get("account") != null) {
            account = intent.getExtras().get("account").toString();
        }
        // if account does not get
        if (account.equals("")) {
            return;
        }

        final Uri labelsUri = GmailContract.Labels.getLabelsUri(account);
        Cursor c = context.getContentResolver().query(GmailContract.Labels.getLabelsUri(account),
                null, null, null, null);

        // loop through the cursor and find the Inbox
        if (c != null) {
            String lblSaving = "";
            int intReadLocal = 0;
            int intUnreadLocal = 0;
            // ローカルファイルから情報取得
            try {
                // ローカルファイル読み込み
                FileInputStream fis = context.openFileInput(FILE_NAME);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

                int row = 0;
                String str = reader.readLine();
                while (str != null) {
                    if (row == 0) {
                        // 1行目がラベル名
                        lblSaving = str;
                    }
                    if (row == 1) {
                        // 2行目がメール数
                        if (!str.equals("")) {
                            intReadLocal = Integer.parseInt(str);
                        }
                    }
                    if (row == 2) {
                        // 3行目が未読数
                        if (!str.equals("")) {
                            intUnreadLocal = Integer.parseInt(str);
                        }
                    }
                    str = reader.readLine();
                    row++;
                }
                reader.close();
                fis.close();

            } catch (IOException ioe) {
                // ファイルがない場合
                return;
            }
            // ラベルが空白の場合リターン
            if (lblSaving.equals("")) {
                return;
            }

            final String inboxCanonicalName = lblSaving.toLowerCase();
            final int canonicalNameIndex = c.getColumnIndexOrThrow(GmailContract.Labels.CANONICAL_NAME);
            final int readCntIndex = c.getColumnIndexOrThrow(GmailContract.Labels.NUM_CONVERSATIONS);
            final int unreadCntIndex = c.getColumnIndexOrThrow(GmailContract.Labels.NUM_UNREAD_CONVERSATIONS);
            boolean isNotification = false; // 通知可否
            int intReadSv = 0;
            int intUnreadSv = 0;
            while (c.moveToNext()) {
                if (inboxCanonicalName.equals(c.getString(canonicalNameIndex))) {
                    // メールボックスのメール数・未読数
                    String strReadCnt = c.getString(readCntIndex);
                    String strUnreadCnt = c.getString(unreadCntIndex);
                    if (strReadCnt != null) {
                        intReadSv = Integer.parseInt(strReadCnt);
                    }
                    if (strUnreadCnt != null) {
                        intUnreadSv = Integer.parseInt(strUnreadCnt);
                    }
                    if (intUnreadLocal < intUnreadSv) {
                        // 未読数が増加
                        isNotification = true;
                    } else if (intUnreadLocal == intUnreadSv && intReadLocal < intReadSv) {
                        // 未読数が変わらずメール数が増加
                        isNotification = true;
                    }

                    // ローカルファイルに現状を書き込み
                    this.setLocalFile(context, lblSaving, strReadCnt, strUnreadCnt);

                    if (isNotification) {
                        sendMessageToStartActivity();
//                        // Notification
//                        int notificationId = 001;
//
//                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
//                                .setSmallIcon(R.drawable.ic_launcher)
//                                .setContentTitle("重要メール通知")
//                                .setContentText("account:" + intent.getExtras().get("account"));
//                                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
//
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//                        notificationManager.notify(notificationId, notificationBuilder.build());


                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                                R.drawable.ic_launcher, null, NotificationUtil.getExamplePendingIntent(
                                context, R.string.example_content_action_clicked)).build();

                        NotificationCompat.WearableExtender wearableOptions = new NotificationCompat.WearableExtender()
                                .addAction(action)
                                .setContentAction(0)
                                .setHintHideIcon(true);
                        applyBasicOptions(context, builder, wearableOptions);
                        builder.extend(wearableOptions);

                        // メール通知とかぶらないように待機
                        sleep(1000);

                        int notificationId = 1;
                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                        notificationManagerCompat.notify(notificationId, builder.build());

                    }
                }
            }
        }

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    /**
     * ローカルファイルにデータ書き込み
     * @param lblName
     * @param readCnt
     * @param unreadCnt
     */
    private void setLocalFile(Context context, String lblName, String readCnt, String unreadCnt) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
            writer.println(lblName);
            writer.println(readCnt);
            writer.println(unreadCnt);
            writer.close();
        } catch (IOException ioe) {
            //
        }
    }

    /**
     * 接続中デバイスのID取得
     */
    private void getNodes() {
//        HashSet<String> results = new HashSet<String>();
//        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//        for (Node node : nodes.getNodes()) {
//            results.add(node.getId());
//        }

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {

                        HashSet<String> results = new HashSet<String>();
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            results.add(node.getId());
                        }
                        sendMessageApi (results);
                    }
                }
        );

        //return results;
    }

    private void sendMessageApi(Collection<String> nodes) {
        for (String node : nodes) {
//            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
//                    mGoogleApiClient, node, START_ACTIVITY_PATH, null).await();
//            if (!result.getStatus().isSuccess()) {
//                Log.e(TAG, "ERROR: failed to send Message: " + result.getStatus());
//            }

            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, START_ACTIVITY_PATH, null).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "ERROR: failed to send Message: " + sendMessageResult.getStatus());
                            }
                        }
                    }
            );
        }
    }

    private void sendMessageToStartActivity() {
        getNodes();
    }

    public synchronized void sleep(long msec)
    {
        try
        {
            wait(msec);
        }catch(InterruptedException e){}
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
}
