package jp.co.njc.wearablenoticemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        LabelListFragment.ItemClickedListener {

    static final String FILE_NAME = "WNM_FILE";

    static final String TAG = "WearableNoticeMail";
    LabelListFragment mFragment = null;
    SimpleCursorAdapter mAdapter;

    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    private static final String[] FEATURES_MAIL = {"service_mail"};

    static final String[] COLUMNS_TO_SHOW = new String[] {
            GmailContract.Labels.NAME,
            GmailContract.Labels.NUM_CONVERSATIONS,
            GmailContract.Labels.NUM_UNREAD_CONVERSATIONS };

    static final int[] LAYOUT_ITEMS = new int[] {
            R.id.name_entry,
            R.id.number_entry,
            R.id.unread_count_number_entry };

    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        // There is only one fragment
        mFragment = (LabelListFragment)fragment;
        mFragment.setItemClickedListener(this);

        mAdapter = new SimpleCursorAdapter(this, R.layout.label_list_item, null,
                COLUMNS_TO_SHOW, LAYOUT_ITEMS);
        mFragment.setListAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        TextView txtV = (TextView) findViewById(R.id.MainLabelName);
        // 設定済のラベルを検索
        String lblSaving = getLabelName();
        if (lblSaving.equals("")) {
            txtV.setText("設定されていません");
        } else {
            txtV.setText(lblSaving);
        }

        // register the receiver
        //registerReceiver(myReceiver, intentFilter);

        // Get the account list, and pick the first one
//        AccountManager.get(this).getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE, FEATURES_MAIL,
//                new AccountManagerCallback<Account[]>() {
//                    @Override
//                    public void run(AccountManagerFuture<Account[]> future) {
//                        Account[] accounts = null;
//                        try {
//                            accounts = future.getResult();
//                        } catch (OperationCanceledException oce) {
//                            Log.e(TAG, "Got OperationCanceledException", oce);
//                        } catch (IOException ioe) {
//                            Log.e(TAG, "Got OperationCanceledException", ioe);
//                        } catch (AuthenticatorException ae) {
//                            Log.e(TAG, "Got OperationCanceledException", ae);
//                        }
//                        onAccountResults(accounts);
//                    }
//                }, null /* handler */);
    }

    /**
     * ローカルファイルからラベル名を読み込む
     * @return
     */
    private String getLabelName() {
        // 設定済のラベルを検索
        try {
            // ローカルファイル読み込み
            FileInputStream fis = openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

            int row = 0;
            String lblSaving = "";
            String str = reader.readLine();
            while (str != null) {
                if (row == 0) {
                    // 1行目がラベル名
                    lblSaving = str;
                }
                str = reader.readLine();
                row++;
            }
            reader.close();
            fis.close();
            return lblSaving;

        } catch (IOException ioe) {
            // ファイルがない場合
            return "";
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister the receiver
        //unregisterReceiver(myReceiver);
    }

    private void onAccountResults(Account[] accounts) {
        Log.i(TAG, "received accounts: " + Arrays.toString(accounts));
        if (accounts != null && accounts.length > 0) {
            // Pick the first one, and display a list of labels
            final String account = accounts[0].name;
            Log.i(TAG, "Starting loader for labels of account: " + account);
            final Bundle args = new Bundle();
            args.putString("account", account);
            getSupportLoaderManager().restartLoader(0, args, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String account = args.getString("account");
        final Uri labelsUri = GmailContract.Labels.getLabelsUri(account);
        return new CursorLoader(this, labelsUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            Log.i(TAG, "Received cursor with # rows: " + data.getCount());
        }
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClicked(int position) {

        // Get the cursor from the adapter
        final Cursor cursor = mAdapter.getCursor();

        cursor.moveToPosition(position);

        // get the uri
        final Uri labelUri = Uri.parse(
                cursor.getString(cursor.getColumnIndex(GmailContract.Labels.URI)));

        Log.i(TAG, "got label uri: " + labelUri);
        final Intent intent = new Intent(this, LabelDetailsActivity.class);
        intent.putExtra(LabelDetailsActivity.LABEL_URI_EXTRA, labelUri);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // カスタムビューを設定
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(
                    LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.setting_dialog,
                    (ViewGroup)findViewById(R.id.layout_root));

            // 現在設定されているラベル取得
            final String strLbl = getLabelName();
            if (!strLbl.equals("")) {
                TextView txtDialogV = (TextView)layout.findViewById(R.id.lblName);
                txtDialogV.setText(strLbl);
            }
            // アラーとダイアログ を生成
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("ラベル設定");
            builder.setView(layout);
            builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // OK ボタンクリック処理
                    EditText lblName = (EditText)layout.findViewById(R.id.lblName);

                    // 設定を保存
                    String strLblNm = lblName.getText().toString();
                    try {
                        FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
                        writer.append(strLblNm);
                        writer.close();

                        // メイン画面に反映
                        TextView txtV = (TextView) findViewById(R.id.MainLabelName);
                        if (!strLblNm.equals("")) {
                            txtV.setText(strLblNm);
                        } else {
                            txtV.setText("設定されていません");
                        }
                    } catch (IOException ioe) {
                        //
                    }
                }
            });
            builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Cancel ボタンクリック処理
                }
            });

            // 表示
            builder.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
