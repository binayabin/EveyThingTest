

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;


public class AppDownloadActivity extends BaseActivity {

    public static final String TAG = "AppDownloadActivity";
    // 存放各个下载器
    Downloader downloader_out = null;
    // 存放与下载器对应的进度条
    ProgressBar progressBar = null;
    //下载的资源路径，这里可以设置网络上的地址
    private String filePath;
    private String fileName;
    private  String urlstr;
    private int notificationId = 0;
    Button btn_start;
    Button btn_pause;
    LinearLayout allLayout;
    TextView progressText;
    private NotificationManager notificationManager;
    private Notification notification;

    WakeLock wakeLock = null;
    NetConnectionChangeReceiver mReceiver;

    @Override
    public int getMouduleContentView() {
        return R.layout.download_item;
    }

    @Override
    public int getMouduleTitle() {
        // TODO Auto-generated method stub
        return R.string.header_appdownload_title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.download_item);
        if (TextUtils.isEmpty(getIntent().getStringExtra("url"))) {
            finish();
        }else {
            init();
        }
        acquireWakeLock();
        registerThisReceiver();
        super.onCreate(savedInstanceState);
    }

    private void registerThisReceiver(){
        if (mReceiver==null) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            mReceiver = new NetConnectionChangeReceiver();
            this.registerReceiver(mReceiver, filter);
        }
    }

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock =
                    pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                        TAG+"Lock");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

  //释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void init(){
        urlstr = getIntent().getStringExtra("url").trim();
        notificationId = getIntent().getIntExtra("notifyId", 0);
        filePath = Environment.getExternalStorageDirectory() +File.separator+ getResources().getString(R.string.app_name_en);
        fileName = urlstr.substring(urlstr.lastIndexOf("/") + 1);// 获得文件名
    }

    @Override
    public void initWidget() {
        super.initWidget();
//        ((TextView)findViewById(R.id.tv_resouce_name)).setText(fileName);
        allLayout = (LinearLayout) findViewById(R.id.download_item_mian);
        progressText = (TextView) findViewById(R.id.download_progress);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_pause = (Button)findViewById(R.id.btn_pause);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notification =  new Notification(android.R.drawable.stat_sys_download, "全途速递", System.currentTimeMillis());;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;//点击后自动消失
        notification.defaults = Notification.DEFAULT_SOUND;//声音默认

    }
    /**
     * 响应开始下载按钮的点击事件
     */
    public void startDownload(View v) {
        if (!isNetOk()){
            Toast.makeText(getApplicationContext(), "当前没有网络，请稍后尝试更新", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(AppContext.getInstance(), AppDownloadActivity.class);
        intent.putExtra("url", urlstr);
        intent.putExtra("notifyId", notificationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(AppContext.getInstance(),0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(this, "全途速递", "下载中", contentIntent);
        notificationManager.notify(notificationId, notification);
        // 得到textView的内容
        String localfile = filePath +File.separator + fileName;
        //设置下载线程数为4，这里是我为了方便随便固定的
        String threadcount = "6";

        File file = new File(filePath,fileName);
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        DownloadTask downloadTask=new DownloadTask(v);
        downloadTask.execute(urlstr,localfile,threadcount);

    };

   class DownloadTask extends AsyncTask<String, Integer, LoadInfo>{
       Downloader downloader=null;
       View v=null;
       String urlstr_in = null;
       public DownloadTask(final View v){
           this.v=v;
       }
       @Override
       protected void onPreExecute() {
           Button btn_start=(Button)((View)v.getParent()).findViewById(R.id.btn_start);
           Button btn_pause=(Button)((View)v.getParent()).findViewById(R.id.btn_pause);
           btn_start.setVisibility(View.GONE);
           btn_pause.setVisibility(View.VISIBLE);
       }
       @Override
        protected LoadInfo doInBackground(String... params) {
            urlstr_in = params[0];
            String localfile = params[1];
            int threadcount = Integer.parseInt(params[2]);

           try {
               URL serverUrl = new URL(urlstr);
               HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
               conn.setRequestMethod("GET");
               // 必须设置false，否则会自动redirect到Location的地址
               conn.setInstanceFollowRedirects(false);

               conn.addRequestProperty("Accept-Charset", "UTF-8;");
               conn.addRequestProperty("User-Agent",
                       "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
               conn.connect();
               String location = conn.getHeaderField("Location");
               Log.e("zybzyb--->>",location);

               if (location != null) {
                   urlstr_in = location;
                   urlstr = urlstr_in;
                   fileName = urlstr_in.substring(urlstr_in.lastIndexOf("/") + 1);// 获得文件名
                   localfile = filePath +File.separator + fileName;
               }
               conn.disconnect();
           } catch (Exception e) {
               e.printStackTrace();
           }

            // 初始化一个downloader下载器
            downloader = downloader_out;
            if (downloader == null) {
                downloader =
                        new Downloader(urlstr, localfile, threadcount, AppDownloadActivity.this, mHandler);
                downloader_out = downloader;
            }
            if (downloader.isdownloading())
                return null;
            // 得到下载信息类的个数组成集合
            return downloader.getDownloaderInfors();
        }

       @Override
       protected void onPostExecute(LoadInfo loadInfo) {
           ((TextView)findViewById(R.id.tv_resouce_name)).setText(fileName);
           if(loadInfo!=null){
                // 显示进度条
                showProgress(loadInfo, urlstr, v);
                // 调用方法开始下载
                downloader.download();
           }
       }

    };
    /**
     * 显示进度条
     */
    private void showProgress(LoadInfo loadInfo, String url, View v) {

        if (progressBar == null) {
            progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setTag(TAG);
            progressBar.setMax(loadInfo.getFileSize());
            progressBar.setProgress(loadInfo.getComplete());
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 5);
            ((LinearLayout)(((LinearLayout) v.getParent().getParent()).findViewById(R.id.progress_in))).addView(progressBar, params);
        }
    }
    /**
     * 响应暂停下载按钮的点击事件
     */
    public void pauseDownload(View v) {
        if (downloader_out != null) {
            downloader_out.pause();
            Button btn_start=(Button)((View)v.getParent()).findViewById(R.id.btn_start);
            Button btn_pause=(Button)((View)v.getParent()).findViewById(R.id.btn_pause);
            btn_pause.setVisibility(View.GONE);
            btn_start.setVisibility(View.VISIBLE);
            notificationManager.cancel(notificationId);
        }
    }

    public void showOperatorSection(View v){
        ImageView up_image =(ImageView) ((View)v.getParent()).findViewById(R.id.layout_down_image);
        ImageView down_image =(ImageView) ((View)v.getParent()).findViewById(R.id.layout_down_up_image);
        if (up_image.getVisibility() == View.VISIBLE) {
            LinearLayout sectionLayout =(LinearLayout) ((LinearLayout) v.getParent()).findViewById(R.id.menu_operator_section);
            sectionLayout.setVisibility(View.VISIBLE);
            down_image.setVisibility(View.VISIBLE);
            up_image.setVisibility(View.GONE);
        }else {
            down_image.setVisibility(View.GONE);
            LinearLayout sectionLayout =(LinearLayout) ((LinearLayout) v.getParent()).findViewById(R.id.menu_operator_section);
            sectionLayout.setVisibility(View.GONE);
            up_image.setVisibility(View.VISIBLE);
        }
    }

    public void reDownloadApp(View v){
        btn_pause.performClick();
        v.setEnabled(false);
        if (progressBar != null) {
            progressBar.setIndeterminate(true);
        }
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = v;
        btnEnableHandler.sendMessageDelayed(msg,1000);
        allLayout.findViewById(R.id.menu_download_main).performClick();
    }

    public void downloadDelete(View v){
        if (btn_pause.getVisibility() == View.VISIBLE  ) {
            btn_pause.performClick();
        }
        View bar = allLayout.findViewWithTag(TAG);
        if (bar != null) {
            ((LinearLayout)bar.getParent()).removeAllViews();
        }
        progressText.setText("");
        progressBar = null;
        downloader_out.delete(urlstr);
        downloader_out.reset();
//        downloader_out = null;
        delete(new File(filePath,fileName));
        allLayout.findViewById(R.id.menu_download_main).performClick();
    }


    Handler btnEnableHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 0:
                View v = (View) msg.obj;
                v.setEnabled(true);
                if (null != progressBar) {
                    progressBar.setIndeterminate(false);
                }
                btn_start.performClick();
                break;
            default:
                break;
            }
        };
    };

    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }
//        final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
//        file.renameTo(to);
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }


    @Override
    protected void onResume() {

        if (btn_start.getVisibility() == View.VISIBLE) {
//            btn_pause.performClick();
            btn_start.performClick();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        try {
            unregisterReceiver(mReceiver);
        }
        catch (Exception e) {
            // TODO: handle exception
        }
        releaseWakeLock();
        super.finish();
    }

    /**
     * 利用消息处理机制适时更新进度条
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int length = msg.arg1;
                if (progressBar != null) {
                    // 设置进度条按读取的length长度更新
                    progressBar.incrementProgressBy(length);
                    ((TextView)((LinearLayout) progressBar.getParent().getParent()).findViewById(R.id.download_progress)).setText( progressBar.getProgress()*100/progressBar.getMax() +"% ");
                    if (progressBar.getProgress() == progressBar.getMax()) {
                        LinearLayout layout = (LinearLayout) progressBar.getParent().getParent().getParent();
                        Toast.makeText(AppDownloadActivity.this, "下载完成！", Toast.LENGTH_SHORT).show();
                        finishAllDownloadTask();
                        Button btn_start=(Button)layout.findViewById(R.id.btn_start);
                        Button btn_pause=(Button)layout.findViewById(R.id.btn_pause);
                        btn_pause.setVisibility(View.GONE);
                        btn_start.setVisibility(View.VISIBLE);
                        installApk(getApplicationContext(), new File(filePath,fileName));
                        notificationManager.cancel(notificationId);
                        finish();
                    }
                }
            }
        }
    };

    /**
     * 下载完成后清除进度条并将downloader_out和progressbar清空
     */
    private void finishAllDownloadTask() {
        ((LinearLayout)progressBar.getParent()).removeView(progressBar);
        progressBar = null;
        downloader_out.delete(urlstr);
        downloader_out.reset();
        downloader_out = null;
    }

    /**
     * 安装APK文件
     *
     * @param apkfile
     *            APK文件名
     * @param mContext
     */
    public void installApk(Context mContext, File apkFile) {
        if (!apkFile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }


    public boolean isNetOk(){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
       return (mobNetInfo.isConnected() || wifiNetInfo.isConnected());
    }

   class NetConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isNetOk() && (btn_pause.getVisibility() == View.VISIBLE)) {
                btn_pause.performClick();
            }
        }

    }
}
