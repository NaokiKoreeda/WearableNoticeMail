package jp.co.njc.wearablenoticemail;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

/**
 * NotificationIntentReceiver
 * Created by njc50031 on 2014/12/24.
 */
public class NotificationIntentReceiver extends BroadcastReceiver {
    public static final String ACTION_EXAMPLE =
            "com.example.android.support.wearable.notifications.ACTION_EXAMPLE";

    private GoogleApiClient mGoogleApiClient = null;
    public static final String TAP_ACTION_PATH = "/tap";
    private static final String TAG = "NotificationReceiver";

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

        if (intent.getAction().equals(ACTION_EXAMPLE)) {
            // WearにMessage送信
            sendMessageToStartActivity();

            // 通知を削除
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(MyReceiver.NOTIFICATION_ID);
        }

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * 接続中デバイスのID取得
     */
    private void getNodes() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        HashSet<String> results = new HashSet<String>();
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            results.add(node.getId());
                        }
                        sendMessageApi(results);
                    }
                }
        );
    }

    private void sendMessageApi(Collection<String> nodes) {
        for (String node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node, TAP_ACTION_PATH, null).setResultCallback(
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
}
