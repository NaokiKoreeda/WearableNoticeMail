package jp.co.njc.wearablenoticemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by njc50031 on 2014/12/17.
 */
public class MyReceiver extends BroadcastReceiver {

    static final String FILE_NAME = "WNM_FILE";
    private static final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
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
                        // Notification
                        int notificationId = 001;

                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle("重要メール通知")
                                        .setContentText("account:" + intent.getExtras().get("account"));

                        NotificationManagerCompat notificationManager =
                                NotificationManagerCompat.from(context);
                        notificationManager.notify(notificationId, notificationBuilder.build());
                    }
                }
            }
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
}
