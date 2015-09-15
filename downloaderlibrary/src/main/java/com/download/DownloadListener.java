package com.download;

import android.os.Handler;
import android.os.Message;

/**
 * 观察者，让上层（界面层）观察下层（下载线程）行为
 */
public abstract class DownloadListener {
    //类型定义
    public static final int UPDATE = 1;
    //错误类型的通知务必大于100
    public static final int UNKNOW_ERROR = 100;
    public static final int EXIST_FULL_APK = 101;
    public static final int GET_APK_SIZE_FAIL = 102;
    public static final int CREATE_APK_FILE_FAIL = 103;
    public static final int NETWORK_UNAVAILABLE_ERROR = 104;

    private Handler mHandler;

    /**
     * 必需在UI线程创建该对象
     */
    public DownloadListener() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == UPDATE) {
                    String urlAndFile = (String)msg.obj;
                    String[] arr = urlAndFile.split(",");

                    onUpdate(arr[0],arr[1], msg.arg1, msg.arg2);
                    if (msg.arg1==msg.arg2 && msg.arg1!=0){
                        onComplete(arr[1]);
                    }
                } else {
                    onError((String) msg.obj, msg.arg1, msg.arg2);
                }
            }
        };
    }

    void sendMessage(Message msg) {
        mHandler.sendMessage(msg);
    }

    /**
     * 方法在UI线程中被调用
     */
    public abstract void onUpdate(String apkUrl,String file, int completeSize, int apkFileSize);
    public abstract void onComplete(String file);

    public abstract void onError(String apkUrl, int type, int arg2);
}
