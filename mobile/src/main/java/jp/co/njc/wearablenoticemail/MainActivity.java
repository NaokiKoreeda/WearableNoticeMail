package jp.co.njc.wearablenoticemail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    static final String FILE_NAME = "WNM_FILE";
    static final String TAG = "WearableNoticeMail";
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        res = this.getResources();

//        // Create local file
//        try {
//            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
//            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
//
//            writer.println("");
//            writer.println("5");
//            writer.println("0");
//            writer.println("0");
//
//            writer.close();
//            fos.close();
//        } catch (IOException ioe) {
//            Log.e(TAG, ioe.getMessage());
//        }
    }

    @Override
    public void onResume() {
        super.onResume();

        TextView txtV = (TextView) findViewById(R.id.MainLabelName);
        TextView txtVT = (TextView) findViewById(R.id.VibeTime);

        List<String> lblList = getLabelName();
        if (lblList != null && lblList.size() > 0) {
            // 設定済のラベルを検索
            if (lblList.get(0).equals("")) {
                txtV.setText(res.getString(R.string.no_setting));
            } else {
                txtV.setText(lblList.get(0));
            }
            // 設定済の振動時間を検索
            if (lblList.get(1).equals("")) {
                txtVT.setText(res.getString(R.string.five_second));
            } else {
                txtVT.setText(lblList.get(1) + res.getString(R.string.second));
            }
        } else {
            txtV.setText(res.getString(R.string.no_setting));
            txtVT.setText(res.getString(R.string.five_second));
        }
    }

    /**
     * ローカルファイルからラベル名を読み込む
     *
     * @return List<String> retList
     */
    private List<String> getLabelName() {
        List<String> retList = new ArrayList<String>();
        // 設定済のラベルを検索
        try {
            // ローカルファイル読み込み
            FileInputStream fis = openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

            int row = 0;
            String lblSaving = "";
            String lblSavingVibe = "";
            String lblSavingAllCount = "";
            String lblSavingUnreadCount = "";
            String str = reader.readLine();
            while (str != null) {
                if (row == 0) {
                    // 1行目がラベル名
                    lblSaving = str;
                }
                if (row == 1) {
                    // 2行目がバイブレーション時間
                    lblSavingVibe = str;
                }
                if (row == 2) {
                    // 3行目がメール数
                    lblSavingAllCount = str;
                }
                if (row == 3) {
                    // 4行目が未読数
                    lblSavingUnreadCount = str;
                }
                str = reader.readLine();
                row++;
            }
            reader.close();
            fis.close();

            retList.add(lblSaving);
            retList.add(lblSavingVibe);
            retList.add(lblSavingAllCount);
            retList.add(lblSavingUnreadCount);

            return retList;

        } catch (IOException ioe) {
            // ファイルがない場合
            Log.d(TAG, "Local file is nothing");
            return retList;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                    LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.setting_dialog,
                    (ViewGroup) findViewById(R.id.layout_root));

            TextView txtDialogV = (TextView) layout.findViewById(R.id.lblName);
            RadioButton rb2 = (RadioButton) layout.findViewById(R.id.vibe_radio_2);
            RadioButton rb5 = (RadioButton) layout.findViewById(R.id.vibe_radio_5);
            RadioButton rb10 = (RadioButton) layout.findViewById(R.id.vibe_radio_10);
            // 現在設定されているラベル取得
            final List<String> listLbl = getLabelName();
            String strLocalLabelTemp = "";
            String strAllCountTemp = "0";
            String strUnreadCountTemp = "0";
            if (listLbl != null && listLbl.size() > 0) {
                if (!listLbl.get(0).equals("")) {
                    txtDialogV.setText(listLbl.get(0));
                    strLocalLabelTemp = listLbl.get(0);
                }
                // 現在設定されている振動時間を取得
                if (!listLbl.get(1).equals("")) {
                    String strVibe = listLbl.get(1);
                    if (strVibe.equals("2")) {
                        rb2.setChecked(true);
                    } else if (strVibe.equals("10")) {
                        rb10.setChecked(true);
                    } else {
                        rb5.setChecked(true);
                    }
                } else {
                    rb5.setChecked(true);
                }
                // 現在設定されているメール数取得
                if (!listLbl.get(2).equals("")) {
                    strAllCountTemp = listLbl.get(2);
                }
                // 現在設定されている未読数取得
                if (!listLbl.get(3).equals("")) {
                    strUnreadCountTemp = listLbl.get(3);
                }

            } else {
                rb5.setChecked(true);
            }

            final String strLocalLabel = strLocalLabelTemp;
            final String strLocalAllCount = strAllCountTemp;
            final String strLocalUnreadCount = strUnreadCountTemp;

            // アラートダイアログ を生成
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(res.getString(R.string.set_label));
            builder.setView(layout);
            builder.setPositiveButton(res.getString(R.string.btn_save), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // OK ボタンクリック処理
                    EditText lblName = (EditText) layout.findViewById(R.id.lblName);
                    RadioButton rb2 = (RadioButton) layout.findViewById(R.id.vibe_radio_2);
                    RadioButton rb5 = (RadioButton) layout.findViewById(R.id.vibe_radio_5);
                    RadioButton rb10 = (RadioButton) layout.findViewById(R.id.vibe_radio_10);

                    // 設定を保存
                    String strLblNm = lblName.getText().toString();
                    String strVibe = "5";
                    if (rb2.isChecked()) {
                        strVibe = "2";
                    } else if (rb5.isChecked()) {
                        strVibe = "5";
                    } else if (rb10.isChecked()) {
                        strVibe = "10";
                    }

                    try {
                        FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
                        writer.println(strLblNm);   // ラベル名
                        writer.println(strVibe);    // 振動時間
                        if (!strLocalLabel.equals(strLblNm)) {
                            // ラベルが変更されたときはカウントリセット
                            writer.println("0");        // メール数
                            writer.println("0");        // 未読数
                        } else {
                            writer.println(strLocalAllCount);        // メール数
                            writer.println(strLocalUnreadCount);        // 未読数
                        }
                        writer.close();
                        fos.close();

                        // メイン画面に反映
                        TextView txtV = (TextView) findViewById(R.id.MainLabelName);
                        if (!strLblNm.equals("")) {
                            txtV.setText(strLblNm);
                        } else {
                            txtV.setText(res.getString(R.string.no_setting));
                        }

                        TextView txtVT = (TextView) findViewById(R.id.VibeTime);
                        if (!strVibe.equals("")) {
                            if (strVibe.equals("2")) {
                                txtVT.setText(res.getString(R.string.two_second));
                            } else if (strVibe.equals("10")) {
                                txtVT.setText(res.getString(R.string.ten_second));
                            } else {
                                txtVT.setText(res.getString(R.string.five_second));
                            }
                        }

                    } catch (IOException ioe) {
                        Log.e(TAG, ioe.getMessage());
                    }
                }
            });
            builder.setNegativeButton(res.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Cancel ボタンクリック処理
                }
            });

            // 表示
            builder.create().show();
            return true;
        }

        if (id == R.id.action_version) {
            // カスタムビューを設定
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                    LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.version,
                    (ViewGroup) findViewById(R.id.layout_root));
            // アラートダイアログ を生成
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(layout);
            // 表示
            builder.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
