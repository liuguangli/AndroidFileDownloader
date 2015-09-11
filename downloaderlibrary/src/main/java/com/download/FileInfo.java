package com.download;

import java.util.List;


public class FileInfo {
    public static final String TAG = "FileInfo";

    //对象状态
    public static final int PAUSE = 3;
    public static final int DELETE = 4;
    public static final int WAITING = 8;
    //对象及数据库状态
//	public static final int INIT = 1;
    public static final int DOWNLOADING = 2;
    public static final int COMPLETED = 5;
    public static final int ERROR = 7;

    public long _id;
    public String fileUrl;
    public int fileSize;
    public int completeSize;
    public String filePath;
    public int state;
    public List<FileItem> fileItemList;

    public FileInfo(String fileUrl, int fileSize, int completeSize, String filePath) {
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.completeSize = completeSize;
        this.filePath = filePath;
    }

    public FileInfo copy() {
        FileInfo info = new FileInfo(fileUrl, fileSize, completeSize, filePath);
        info._id = _id;
        info.state = state;
        return info;
    }

    public static class FileItem {
        public static final int UNCOMPLETED = 0;
        public static final int COMPLETED = 1;

        public long _id;//数据库自身_ID
        public long infoId;
        public int startPos;
        public int endPos;
        public int completeSize;
        public int state;//1完成；0未完成
        public FileInfo info;
    }

    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        if (o == null) return false;
        if (o instanceof FileInfo) {
            FileInfo info = (FileInfo) o;
            if (info.fileUrl != null) {
                return info.fileUrl.equals(this.fileUrl);
            } else {
                return false;
            }

        }
        return false;
    }
}
