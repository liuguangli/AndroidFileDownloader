package com.download;

import android.text.TextUtils;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


class DownloadThread extends Thread {
    private FileDownloader mFileDownloader;
    private TaskList mTaskList;
    public DownloadThread(FileDownloader fileDownloader, TaskList list) {
        mTaskList = list;
        mFileDownloader = fileDownloader;
    }

    @Override
    public void run() {
        LogUtil.d("DownloadThread", "(7)run())");
        while (mFileDownloader.isRunning()) {
            FileInfo.FileItem item = mTaskList.getTaskItem();
            if (item == null) {
                _wait();
            } else {
                downloadApk(item);
            }
        }
    }

    private void _wait() {
        synchronized (mTaskList) {
            try {
                Log.d("Thread", "Thread " + getId() + " 我太累了，休息会");
                mTaskList.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void downloadApk(FileInfo.FileItem item) {

        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;
        try {
            System.setProperty("http.keepAlive", "false");
            URL url = new URL(item.info.fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            // 设置范围，格式为Range：bytes x-y;
            connection.setRequestProperty("Range", "bytes=" + (item.startPos + item.completeSize) + "-" + item.endPos);
            // 将要下载的文件写到保存在保存路径下的文件中
            is = new BufferedInputStream(connection.getInputStream());

            randomAccessFile = new RandomAccessFile(item.info.filePath, "rwd");
            randomAccessFile.seek(item.startPos + item.completeSize);
            byte[] buffer = new byte[1024 * 50];//10KB
            int length = -1;

            if (item.info.state != FileInfo.DOWNLOADING) {
                item.info.state = FileInfo.DOWNLOADING;

            }
            int needLength = item.endPos - (item.startPos + item.completeSize) + 1;
            int totalLength = 0;
            while ((length = is.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, length);

                item.completeSize += length;

                totalLength += length;
                if (totalLength == 0) {
                    Log.e("Thread", "什么情况，");
                }
                if (item.completeSize == item.endPos - item.startPos + 1) {
                    item.state = FileInfo.FileItem.COMPLETED;//完成
                }
                // 更新数据库中的下载信息
                if (item.state == FileInfo.FileItem.COMPLETED) {
                    mFileDownloader.update(item.info.fileUrl,item.info.filePath, totalLength);//通知Ui更新视图
                    Log.d("Thread", "Thread " + getId() + "  完成一个ApkItem " + item._id + " lengthL:" + length);
                }
                if (item.info.state == FileInfo.PAUSE) {
                    Log.e("gangli.liu", "暂停了....");
                    return;
                } else if (item.info.state == FileInfo.DELETE) {
                    mFileDownloader.error(item.info.fileUrl, FileInfo.DELETE);
                    Log.e("gangli.liu", "删除了....");
                    return;
                }
            }

            Log.d("Thread", "Item" + item._id + " 实际下载长度:" + totalLength + " 应下载长度：" + needLength);
            if (totalLength > needLength)
                Log.e("Thread", "Item 实际下载长度:" + totalLength + " 应下载长度：" + needLength);
            if (totalLength < needLength) {
                downloadApk(item);
                Log.e("Thread", "Thread -" + getId() + " 网络没请求到实际长度的内容内容，重新下载");
            }
        } catch (EOFException e) {
            downloadApk(item);
            Log.e("Thread", "Thread -" + getId() + " EOFExcetion,数据意外中断，重新下载");
        } catch (Exception e) {
            e.printStackTrace();
            /* 当正在关闭网络时请求失败，但NetworkUtil.isNetworkAvailable()有延时，仍在一小段时间内返回true */
            boolean networkUnreachable = false;
            String msgString = e.getMessage();
            if (!TextUtils.isEmpty(e.getMessage())) {
                if (msgString.contains("Connection reset by peer")) {
                    downloadApk(item);
                    Log.e("Thread", "Thread -" + getId() + " Connection reset by peer, 服务器重置，数据意外断开，重新下载");
                    return;
                }
                if (msgString.contains("Connection timed out") || msgString.contains("Network is unreachable") ||
                        msgString.contains("failed to connect to")) {
                    networkUnreachable = true;
                    Log.e("Thread", "Thread networkUnavailable" + getId());
                }
                if (msgString.contains("write failed")) {
                    Log.e("Thread", "安装包文件删除，randomAccessFile写人错误");
                    return;

                }
            }
            if (!mFileDownloader.netWorkAble() || networkUnreachable) {
                Log.e("Thread", "Thread " + getId() + " 无网络异常" + e.getMessage());
                mTaskList.setApkInfoState(item.info.fileUrl, FileInfo.ERROR);
                mFileDownloader.error(item.info.fileUrl, DownloadListener.NETWORK_UNAVAILABLE_ERROR);
            } else {
                Log.e("Thread", "Thread " + getId() + " 未知异常" + e.getMessage());
                mTaskList.setApkInfoState(item.info.fileUrl, FileInfo.ERROR);
                mFileDownloader.error(item.info.fileUrl, DownloadListener.UNKNOW_ERROR);
            }
            _wait();
        } finally {
            try {
                if (is != null) is.close();
                if (randomAccessFile != null) randomAccessFile.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}