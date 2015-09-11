package com.download;

import java.util.Vector;

/**
 * Created by liuguangli on 15/9/11.
 */
public class TaskList {
    private Vector<FileInfo> vector = new Vector<>();
    private Vector<String> urls = new Vector<>();
    private Vector<FileInfo.FileItem> taskItemList = new Vector<>();
    public FileInfo getFileInfo(int pos){
        return vector.get(pos);
    }
    public FileInfo getFileInfoByUrl(String url){
        for (FileInfo info:vector){
            if (info.fileUrl.equals(url)){
                return info;
            }
        }
        return null;
    }
    public FileInfo getFileInfoByFileName(String file){
        for (FileInfo info:vector){
            if (info.filePath.equals(file)){
                return info;
            }
        }
        return null;
    }

    public FileInfo.FileItem getTaskItem(){
        FileInfo.FileItem item = null;
        if (!vector.isEmpty()){
            FileInfo info = vector.get(0);
            if (info.fileItemList.isEmpty() && info.state == FileInfo.COMPLETED){
                vector.remove(info);
            } else if (!info.fileItemList.isEmpty()){
                item = info.fileItemList.remove(0);
            }
        }
        return item;
    }

    public boolean hasContain(String url) {
        return  (getFileInfoByUrl(url) != null);

    }

    public void add(FileInfo fileInfo) {
        vector.add(fileInfo);
    }

    public void addUrl(String url){
        urls.add(url);
    }

    public String getAndRemoveUrl(){
      if (urls.isEmpty()){
          return null;
      }
      return urls.remove(0);
    };
    public synchronized void setApkInfoState(String apkUrl, int state) {
        FileInfo info = getFileInfoByUrl(apkUrl);
        if (info != null && info.state != FileInfo.COMPLETED) {
            info.state = state;
        }
    }

    public int getSize() {
        return vector.size();
    }

    public void delete(FileInfo info) {
        urls.remove(info.fileUrl);
        vector.remove(info);

    }
}
