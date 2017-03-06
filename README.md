# AndroidFileDownloader

[![](https://jitpack.io/v/liuguangli/AndroidFileDownloader.svg)](https://jitpack.io/#liuguangli/AndroidFileDownloader)

##使用场景
###apk应用内更新，其他中小文件的下载，断点续传下载等，先来看看效果图：
<br/>

<img src="https://github.com/liuguangli/AndroidFileDownloader/blob/master/simple.gif" width="240" heigth="360"/>

## 开始
项目使用 [jitpack](https://jitpack.io) 做开源库的托管，你需要在 .gradle 中添加  [jitpack](https://jitpack.io) 
的仓库。


    allprojects {
        repositories {
            jcenter()
            maven { url "https://jitpack.io" }
        }
    }
    
在添加 FloatUtil 的依赖引用：
   
   
    dependencies {
         compile 'com.github.liuguangli:downloaderlibrary:-SNAPSHOT'
    }
##简单方便的调用接口
###调用者只要设置好下载路径、文件后缀，丢一个url即可开始下载，并且可以根据需要监听进度，例如：
<pre><code>
        FileDownloader mDownloader  = FileDownloader.getInstance(getApplication());
        mDownloader.setExtend(".apk");
        mDownloader.setFilePath(Environment.getExternalStorageDirectory() + "/apk/");
        mDownloader.addFile("http://zhuzher.vanke.com/uip/zhuzher.apk");
        mDownloader.setDownloadObserver(new DownloadObserver() {
            @Override
            public void onUpdate(String apkUrl, String file, int completeSize, int apkFileSize) {
             //do your thing
            }
            @Override
            public void onComplete(String file) {
             //do your thing
            }

            @Override
            public void onError(String apkUrl, int type, int state) {
                //do your thing
            }
        });
</code></pre>


