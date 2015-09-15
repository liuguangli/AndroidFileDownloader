package com.download;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;



import java.io.File;
import java.util.ArrayList;

public class FileDownloader {
   
    public static final String TAG = FileDownloader.class.getSimpleName();
    private final int THREAD_COUNT = 5;//线程数
    private ArrayList<DownloadThread> threadList;//消费者线程集
    private TaskCreatorThread mFileAdder;//生产者线程
    private DownloadListener mObserver;//状态观察者
    private Application context;
    private String mFolder = Environment.getExternalStorageDirectory()+"/fileDownload/";
    private String mExtend = ".apk";
    private TaskList mTaskList;
    private boolean running;
    private static FileDownloader fileDownloader;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (netWorkAble()) {
                LogUtil.d("Thread", "mBroadcastReceiver 说：终于有网络了。");
                fileDownloader.onNetWorkResume();


            }
        }
    };




    private FileDownloader(Application context) {
        this.context = context;
        init();
        registerNetStateReceiver();
    }
    public boolean isRunning(){
        return running;
    }
    public void registerNetStateReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mBroadcastReceiver, filter);
    }
    public void unregisterNetStateReceiver() {
        if (null != mBroadcastReceiver) {

            context.unregisterReceiver(mBroadcastReceiver);
        }
    }
    public void setFilePath(String path){
        mFolder = path;
        mFileAdder.setFolder(path);
        createFolder();
    }

    public void setExtend(String extend){
        mExtend = extend;
        mFileAdder.setExtend(extend);
    }

    private void onNetWorkResume(){
        resume();

    }

    private void resume(){
        synchronized (mTaskList){
            mTaskList.notifyAll();
        }
    }
    public static FileDownloader getInstance(Application context) {
        if (fileDownloader == null) {
            fileDownloader = new FileDownloader(context);
        }
        return fileDownloader;
    }





    private void init() {
        running = true;
        mTaskList = new TaskList();
        //初始化下载线程
        threadList = new ArrayList<DownloadThread>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            DownloadThread thread = new DownloadThread(this,mTaskList);
            thread.start();
            threadList.add(thread);
        }


        createFolder();

        //启动生产者线程
        mFileAdder = new TaskCreatorThread(this,mTaskList);
        mFileAdder.setFolder(mFolder);
        mFileAdder.setExtend(mExtend);
        mFileAdder.start();
    }

    private void createFolder() {
        File file = new File(mFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    /**
     * 设置观察者以观察下载进度
     */
    public synchronized void setDownloadObserver(DownloadListener observer) {
        this.mObserver = observer;
    }

    /**
     * 下载进度更新
     */
    synchronized void update(String fileUrl,String file, int arg1) {
        FileInfo info = mTaskList.getFileInfoByUrl(fileUrl);
        LogUtil.d(TAG, "info = " + info);
        if (info != null) {
            info.completeSize += arg1;
            if (mObserver != null && info.completeSize > 0) {
                Message msg = Message.obtain();
                msg.what = DownloadListener.UPDATE;
                msg.arg1 = info.completeSize;
                msg.arg2 = info.fileSize;
                msg.obj = info.fileUrl+","+file;
                mObserver.sendMessage(msg);
            }

            if (info.fileSize == info.completeSize){
                info.state = FileInfo.COMPLETED;
            }
            //通知观察者
        } else {
            LogUtil.e(TAG,"has delete");
        }
    }


    /**
     * 事件通知
     */
    synchronized void error(String fileUrl, int state) {
        FileInfo info = mTaskList.getFileInfoByUrl(fileUrl);
        if (info != null) {
            switch (state) {
                case DownloadListener.CREATE_APK_FILE_FAIL:
                case DownloadListener.GET_APK_SIZE_FAIL:
                case DownloadListener.UNKNOW_ERROR:
                case DownloadListener.EXIST_FULL_APK:
                    info.state = FileInfo.ERROR;

                    delete(info);

                    break;
                case DownloadListener.NETWORK_UNAVAILABLE_ERROR:
                    info.state = FileInfo.ERROR;
                    break;
            }

        }
        if (mObserver != null) {
            Message msg = Message.obtain();
            msg.what = state;
            msg.arg1 = state;
            msg.obj = fileUrl;
            mObserver.sendMessage(msg);
        }
    }


    public synchronized void delete(FileInfo info) {
        if (info == null) return;

        mTaskList.delete(info);
    }



    /**
     * 添加一个下载包
     */
    public void addFile(String url) {
        if (TextUtils.isEmpty(url)) return;
        if (!netWorkAble()) {
            error(url, DownloadListener.NETWORK_UNAVAILABLE_ERROR);
            return;
        }
        Log.d(TAG, "(0)apkMap.containsKey(fileUrl);");
        if (mTaskList.hasContain(url)) {
            return;
        }

        mTaskList.addUrl(url);
        running = true;
        resume();
    }



    public void release() {
        unregisterNetStateReceiver();
        running = false;
    }

    /**
     * 是否有网络连接
     *
     * @return
     */
    public  boolean isInternetConnected() {
        boolean flag = false;

        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo[] info = manager.getAllNetworkInfo();
            if (info != null) {
                int length = info.length;
                for (int count = 0; count < length; count++) {
                    if (info[count].getState() == NetworkInfo.State.CONNECTED) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
    }
    public boolean netWorkAble() {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mobNetInfo.isConnected() || wifiNetInfo.isConnected();
    }
}
