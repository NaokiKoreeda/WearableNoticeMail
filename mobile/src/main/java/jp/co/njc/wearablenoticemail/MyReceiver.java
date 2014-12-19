package jp.co.njc.wearablenoticemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by njc50031 on 2014/12/17.
 */
public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "Receiver";
    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    private static final String[] FEATURES_MAIL = {"service_mail"};

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "You've Got Mail!!");
        for (String key : intent.getExtras().keySet()) {
            Log.d(TAG, key + ": " + intent.getExtras().get(key));
        }
//        Toast.makeText(context, "Received broadcast; value received: " +
//                intent.getStringExtra("key"), Toast.LENGTH_LONG).show();

        // Get the account list, and pick the first one
        AccountManager.get(context).getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE, FEATURES_MAIL,
                new AccountManagerCallback<Account[]>() {
                    @Override
                    public void run(AccountManagerFuture<Account[]> future) {
                        Account[] accounts = null;
                        try {
                            accounts = future.getResult();
                        } catch (OperationCanceledException oce) {
                            Log.e(TAG, "Got OperationCanceledException", oce);
                        } catch (IOException ioe) {
                            Log.e(TAG, "Got OperationCanceledException", ioe);
                        } catch (AuthenticatorException ae) {
                            Log.e(TAG, "Got OperationCanceledException", ae);
                        }
                        onAccountResults(accounts);
                    }
                }, null /* handler */);

        String account = "";
        if (intent.getExtras().get("account") != null) {
            account = intent.getExtras().get("account").toString();
        }

        // if account does not get
        if (account.equals("")) {
            return;
        }

        final Uri labelsUri = GmailContract.Labels.getLabelsUri(account);
        Loader<Cursor> cursorLoader = new CursorLoader(context, labelsUri, null, null, null, null);

        Cursor c = context.getContentResolver().query(GmailContract.Labels.getLabelsUri(account),
                null, null, null, null);

        // loop through the cursor and find the Inbox
        if (c != null) {
            //final String inboxCanonicalName = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX;
            //TODO
            final String inboxCanonicalName = "wear";
            final int canonicalNameIndex = c.getColumnIndexOrThrow(GmailContract.Labels.CANONICAL_NAME);
            while (c.moveToNext()) {
                if (inboxCanonicalName.equals(c.getString(canonicalNameIndex))) {
                    // this row corresponds to the Inbox
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

    private void onAccountResults(Account[] accounts) {
        Log.i(TAG, "received accounts: " + Arrays.toString(accounts));
        if (accounts != null && accounts.length > 0) {
            // Pick the first one, and display a list of labels
            final String account = accounts[0].name;
            Log.i(TAG, "Starting loader for labels of account: " + account);
            final Bundle args = new Bundle();
            args.putString("account", account);
            //getSupportLoaderManager().restartLoader(0, args, this);


        }
    }
}
