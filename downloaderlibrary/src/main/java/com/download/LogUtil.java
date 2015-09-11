package com.download;

import android.util.Log;

/**
 * Created by liuguangli on 15/9/11.
 */
public class LogUtil {
    public final static boolean DEBUG = true;
    public final static int maxLogSize = 4000;
    public static void d(String tag,String msg) {
        if (DEBUG) {
            for (int i = 0; i <= msg.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > msg.length() ? msg.length() : end;
                Log.d(tag, msg.substring(start, end));
            }
        }
    }




    public static void e(String tag,String msg) {
        if (DEBUG) {
            for (int i = 0; i <= msg.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > msg.length() ? msg.length() : end;
                Log.e(tag, msg.substring(start, end));
            }
        }
    }

}
