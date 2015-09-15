package com.example.liuguangli.androiddownloader;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.download.DownloadListener;
import com.download.FileDownloader;
import com.download.LogUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final  String TAG = "MainACT";
    private ProgressBar mBar;

    private TextView mTvState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBar = (ProgressBar) findViewById(R.id.progressBar);
        mBar.setMax(100);


        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        mTvState = (TextView) findViewById(R.id.tv_state);
        }

    @Override
    public void onClick(View v) {

        FileDownloader mDownloader  = FileDownloader.getInstance(getApplication());

        mDownloader.setExtend(".apk");
        mDownloader.setFilePath(Environment.getExternalStorageDirectory() + "/apk/");
        mDownloader.addFile("http://zhuzher.vanke.com/uip/zhuzher.apk");
        mDownloader.setDownloadObserver(new DownloadListener() {
            @Override
            public void onUpdate(String apkUrl, String file, int completeSize, int apkFileSize) {
                LogUtil.d(TAG,"apkurl:"+apkUrl);
                LogUtil.d(TAG,"file:"+file);
                LogUtil.d(TAG,"completeSize:"+completeSize);
                LogUtil.d(TAG,"apkFileSize:"+apkFileSize);
                mBar.setProgress(completeSize * 100 / apkFileSize);
                mTvState.setText(completeSize+"/"+apkFileSize);
            }

            @Override
            public void onComplete(String file) {

                mTvState.setText("下载完成");
                mTvState.setTextColor(Color.GREEN);
                LogUtil.d(TAG,"onComplete:"+file);
            }

            @Override
            public void onError(String apkUrl, int type, int state) {
                LogUtil.e(TAG,"onError:state:"+state+",type:"+state);
                mTvState.setText("Download error");
                mTvState.setTextColor(Color.RED);
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
