package com.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by liuguangli on 15/9/11.
 */
public class TaskCreatorThread extends Thread {
    public static final String TAG =  "TaskCreatorThread";
    private FileDownloader mdownLoader;
    private TaskList mTaskList;

    private String folder;
    private String extend;
    public TaskCreatorThread(FileDownloader downloader,TaskList taskList){
        mTaskList = taskList;
        mdownLoader = downloader;
    }

    @Override
    public void run() {
        while (mdownLoader.isRunning()){
            String url = mTaskList.removeReturnRul();
            if (url == null){
                try {
                    synchronized (mTaskList){
                        mTaskList.wait();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                create(url);
            }
        }
    }



    private void create(String url){
        //之前没有下载
        FileInfo fileInfo = mTaskList.getFileInfoByUrl(url);
        if (fileInfo == null) {
            String apkFilePath = folder + MD5Util.getUpperMD5Str(url) + extend;
            LogUtil.d(TAG,"file:"+apkFilePath);
            fileInfo = new FileInfo(url, 0, 0, apkFilePath);
            fileInfo._id = System.currentTimeMillis();
        }
        fileInfo.fileSize = getFileSize(fileInfo);
        LogUtil.d(TAG,"fileSize:"+ fileInfo.fileSize);
        if (fileInfo.fileSize <= 0) {
            fileInfo.state = FileInfo.ERROR;

            mdownLoader.error(url, DownloadListener.GET_APK_SIZE_FAIL);//通知重新下载
            return;

        }
        boolean isSuccess = createApkFile(fileInfo);
        if (!isSuccess) {
            fileInfo.state = FileInfo.ERROR;
            mdownLoader.error(url, DownloadListener.CREATE_APK_FILE_FAIL);//通知重新下载
            return ;
        }
        createSubsection(fileInfo);
        fileInfo.state = FileInfo.WAITING;
        mdownLoader.update(url, fileInfo.filePath, 0);
        mTaskList.add(fileInfo);


        notifyDownloadThreads();
    }

    private void createSubsection(FileInfo fileInfo) {
        if (fileInfo.fileSize > 0) {
            LogUtil.d(TAG, "(4)fileInfo._id > 0 && fileInfo.fileSize > 0");
            fileInfo.fileItemList = new ArrayList<FileInfo.FileItem>();
            final int COUNT = 100;
            int itemtLen = fileInfo.fileSize / COUNT;
            for (int i = 0; i < COUNT; i++) {
                FileInfo.FileItem fileItem = new FileInfo.FileItem();
                fileItem.info = fileInfo;
                fileItem.infoId = fileInfo._id;
                fileItem.startPos = i * itemtLen;
                fileItem._id = i;
                fileItem.endPos = fileItem.startPos + itemtLen - 1;
                fileInfo.fileItemList.add(fileItem);
            }
            int length = fileInfo.fileSize % itemtLen;
            if (length > 0) {
                FileInfo.FileItem fileItem = new FileInfo.FileItem();
                fileItem.info = fileInfo;
                fileItem.infoId = fileInfo._id;
                fileItem.startPos = COUNT * itemtLen;
                fileItem.endPos = fileItem.startPos + length - 1;
                fileInfo.fileItemList.add(fileItem);
            }
        }
    }

    private void notifyDownloadThreads(){
        synchronized (mTaskList){
            mTaskList.notifyAll();
        }

    }
    /**
     * 获取要下载的包大小
     */
    private int getFileSize(FileInfo fileInfo) {
        HttpURLConnection connection = null;
        int size = -1;
        try {
            URL url = new URL(fileInfo.fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            size = connection.getContentLength();
        } catch (Exception e) {
            e.printStackTrace();
            size = 0;
        } finally {
            if (connection != null) connection.disconnect();
        }
        return size;
    }
    private boolean createApkFile(FileInfo fileInfo) {
        boolean result = true;
        RandomAccessFile accessFile = null;
        try {
            if (fileInfo.fileSize > 0) {
                File file = new File(fileInfo.filePath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                accessFile = new RandomAccessFile(file, "rwd");
                accessFile.setLength(fileInfo.fileSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }




    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setExtend(String extend){
        this.extend = extend;
    }
}


