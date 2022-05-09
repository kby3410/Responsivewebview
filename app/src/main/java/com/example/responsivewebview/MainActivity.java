package com.example.responsivewebview;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mylibrary.ItempApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.provider.Settings.ACTION_SETTINGS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView Internet;
    private volatile boolean running = true;
    private Button Setting_button,Rtc_button,Save_button, Lite_button, Sensor_button;
    private EditText Name,Url;
    private final ArrayList<DBData> DB_list = new ArrayList<>();
    private final ArrayList<String> Http_list = new ArrayList<>();
    private final ArrayList<String> Name_list = new ArrayList<>();
    private final ArrayList<Integer> Check_list = new ArrayList<>();
    private DBHandler handler;
    private DBRecyclerAdapter DBadapter;
    private Handler mHandler;
    private String REBOOT_ACTION = "ads.android.setreboot.action";
    private ProgressDialog pDialog;
    private File targetFile;
    private static final String IP_ADDRESS = "https://www.krizer.co.kr/krizer_edit/responsivewebview1.1.apk";
    private Intent AppListIntent;
    private String Packname = "com.example.responsivewebview";
    private String UpdateUrl = "http://krizer.co.kr/krizer_edit/krizer_app_version.html";
    private String respon_Url;
    private String TAG = "MainActivity" ;
    private String Server_Version;
    private String App_Version;
    private String filename = "update.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.ISSENSOR){
            setContentView(R.layout.activity_main);
            Sensor_button = (Button)findViewById(R.id.sensor_button);
            Sensor_button.setOnClickListener(this);
            respon_Url = "http://krizer.co.kr/krizer_edit/respon_sensor.apk";
        }else {
            setContentView(R.layout.activity_main_auto);
            respon_Url = "http://krizer.co.kr/krizer_edit/respon_basic.apk";
        }

        Log.d("test", respon_Url);
        AppListIntent = new Intent(Intent.ACTION_MAIN, null);
        AppListIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pack = getPackageManager().queryIntentActivities(AppListIntent, 0);
        for (int i = 0; i < pack.size(); i++) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = getPackageManager().getPackageInfo(pack.get(i).activityInfo.applicationInfo.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if ("com.example.responsivewebview.auto".equals(pack.get(i).activityInfo.applicationInfo.packageName)||"com.example.responsivewebview.sensor".equals(pack.get(i).activityInfo.applicationInfo.packageName)) {
                App_Version = packageInfo.versionName;
                Log.d("test", App_Version);
            }
        }

        Log.d("test" , Build.MODEL);
        if (Build.MODEL.equals("Infos_Duple")){
            Log.d("test" , "크라이저 모델입니다.");
        }else {
            finish();
            Toast.makeText(MainActivity.this, "크라이저 제품이 아닙니다.", Toast.LENGTH_SHORT).show();
        }
        ActionBar actionBar = getSupportActionBar();    //액션바 숨기기
        actionBar.hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            //    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Settings.System.putInt(getContentResolver(), "screen_brightness", 255);

        targetFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" + filename);
        Internet = (ImageView) findViewById(R.id.lancheck);
        Setting_button = (Button)findViewById(R.id.setting_button);
        Setting_button.setOnClickListener(this);
        Rtc_button = (Button)findViewById(R.id.rtc_button);
        Rtc_button.setOnClickListener(this);
        Save_button = (Button)findViewById(R.id.save_button);
        Save_button.setOnClickListener(this);
        Lite_button = (Button)findViewById(R.id.lite_button);
        Lite_button.setOnClickListener(this);
        Name = (EditText) findViewById(R.id.name);
        Url = (EditText) findViewById(R.id.url);


        Thread();
        init();
        ItempApi.controlSystemBarShow(this);
        checkPermission();

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {      //wifi 연결시 딜레이 때문에
                DBhandler();
                JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                jsoupAsyncTask.execute(UpdateUrl);

            }
        }, 6000);
    }

    public void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        terminate();
    }

    void Dialog(String text){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);    //다이얼로그
        alert.setTitle("알림");
        alert.setMessage(text);

        alert.setCancelable(false);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    protected void onRestart(){               //메모 수정,삭제했을 경우 반영하기위한 리스타트
        super.onRestart();
        running = true;
        Thread();
        DBhandler();
        ItempApi.controlSystemBarShow(this);
        Settings.System.putInt(getContentResolver(), "screen_brightness", 255);
    }

    void Thread(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            InternetCheck();
                        }
                    });
                    try {
                        Thread.sleep(1000); // 1000 ms = 1초
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // while
            } // run()
        }; // new Thread() { };
        thread.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {      //런처앱 뒤로가기 비활성화
        return false;
    }

    private void hideKeyboard() {              //화면 터치시 키보드 내려감
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideKeyboard();
        return super.dispatchTouchEvent(ev);
    }

    public void terminate() {
        running = false;
    }

    void InternetCheck(){
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();
        if(ninfo == null){
            Internet.setBackgroundResource(R.drawable.offline);
        }else{
            Internet.setBackgroundResource(R.drawable.online);
        }

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.setting_button:
                Intent setting_intent= new Intent(ACTION_SETTINGS);
                startActivity(setting_intent);
                break;
            case R.id.rtc_button:
                Intent rtc_Intent = getPackageManager().getLaunchIntentForPackage("com.example.shutdown");
                startActivity(rtc_Intent);
                break;
            case R.id.save_button:
                System.out.println("save_button");
                String getNameEdit = Name.getText().toString();
                if(getNameEdit.getBytes().length <= 0){//빈값이 넘어올때의 처리
                    Toast.makeText(MainActivity.this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                }else {
                    String getUrlEdit = Url.getText().toString();
                    if(getUrlEdit.getBytes().length <= 0){//빈값이 넘어올때의 처리

                        Toast.makeText(MainActivity.this, "URL을 입력하세요.", Toast.LENGTH_SHORT).show();
                    }else {

                        handler.insert(Url.getText().toString(), Name.getText().toString(), 0);
                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Url.getText().clear();
                                Name.getText().clear();
                            }
                        }, 0);
                    }
                }
                DBhandler();
                break;
            case R.id.lite_button:
                lite_doRootStuff();
                break;
            case R.id.sensor_button:
                Intent sensor_Intent = getPackageManager().getLaunchIntentForPackage("com.example.uartsensor");
                startActivity(sensor_Intent);
                break;


        }
    }

    public void lite_doRootStuff(){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("busybox mount -o remount,rw -t ext4 /dev/block/platform/ff0f0000.dwmmc/by-name/system /system\n").getBytes()); // "Permissive"
            stdin.write("am start com.ayst.adplayer/.home.HomeActivity\n".getBytes());
            //stdin.write(("cp /storage/emulated/0/TEST/test.apk /system/app/ResponsiveWebview/ResponsiveWebview.apk\n").getBytes()); // E/[Error]: cp: /system/media/bootanimation_test.zip: Read-only file system
            //stdin.write(("chmod 644 /system/app/ResponsiveWebview/ResponsiveWebview.apk\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.d("[Output]", line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doRootStuff(){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write(("busybox mount -o remount,rw -t ext4 /dev/block/platform/ff0f0000.dwmmc/by-name/system /system\n").getBytes()); // "Permissive"
            //stdin.write("am start com.ayst.adplayer/.home.HomeActivity\n".getBytes());
            stdin.write(("cp /storage/emulated/0/update.apk /system/app/ResponsiveWebview/ResponsiveWebview.apk\n").getBytes()); // E/[Error]: cp: /system/media/bootanimation_test.zip: Read-only file system
            stdin.write(("chmod 644 /system/app/ResponsiveWebview/ResponsiveWebview.apk\n").getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                Log.d("[Output]", line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void init() {

        System.out.println("init_start");
        RecyclerView recyclerView = findViewById(R.id.DBRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        DividerItemDecoration dividerItemDecoration =           //리사이클러뷰 구분선 커스텀
                new DividerItemDecoration(this,
                        new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);


        recyclerView.setLayoutManager(linearLayoutManager);
        DBadapter = new DBRecyclerAdapter(DB_list,this);
        recyclerView.setAdapter(DBadapter);

    }

    protected InputFilter filter= new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9/]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };


    private void DBhandler(){
        DB_list.clear();
        Http_list.clear();
        Name_list.clear();
        Check_list.clear();
        handler = DBHandler.open(this);
        Cursor cursor;
        cursor = handler.select();
        while (cursor.moveToNext()){
            Http_list.add(cursor.getString(0));
            Name_list.add(cursor.getString(1));
            Check_list.add(cursor.getInt(2));
        }
        for(int i = 0; i < Http_list.size(); i++){
            DBData save_data = new DBData(Http_list.get(i), Name_list.get(i), Check_list.get(i));
            System.out.println(Name_list.get(i));
            System.out.println(Http_list.get(i));

            DB_list.add(i, save_data);
        }
        for (int i = 0; i < Http_list.size(); i++){
            if (Check_list.get(i) == 1){
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ninfo = cm.getActiveNetworkInfo();
                if(ninfo == null) {
                    Dialog("인터넷 연결을 확인해주세요.");
                }else {
                    Intent intent = new Intent(getApplicationContext(), web.class);
                    intent.putExtra("url", "https://" + Http_list.get(i));
                    intent.putExtra("name", Name_list.get(i));
                    intent.putExtra("check_number", Check_list.get(i));
                    startActivity(intent);
                }
            }
        }
        DBadapter.notifyDataSetChanged();
    }

    void CheckVer() {
        new Thread() {
            public void run() {
                try {
                    URL url;
                    HttpURLConnection conn = null;
                    url = new URL(respon_Url); // 서버에 접속한다.
                    conn = (HttpURLConnection) url.openConnection();

                    if (200 != conn.getResponseCode()) {
                        System.out.println("con test" + conn.getResponseCode());
                    } else {
                    System.out.println("con test22 = " + conn.getResponseCode());
                    //System.out.println("contest" + conn.getResponseCode());
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 사용하고자 하는 코드
                                DownloadFileAsync downloadFileAsync = new DownloadFileAsync();
                                downloadFileAsync.execute();
                            }
                            }, 0);
                    }
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }.start();
    }

    class DownloadFileAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog= new ProgressDialog(MainActivity.this); //ProgressDialog 객체 생성
            pDialog.setTitle("업데이트 중 입니다.");                   //ProgressDialog 제목
            pDialog.setMessage("Loading.....");             //ProgressDialog 메세지
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); //막대형태의 ProgressDialog 스타일 설정
            pDialog.setCanceledOnTouchOutside(false); //ProgressDialog가 진행되는 동안 dialog의 바깥쪽을 눌러 종료하는 것을 금지
            pDialog.show(); //ProgressDialog 보여주기
        }

        @Override
        protected String doInBackground(String... strings) {
            int contentLength;
            URL url;
            HttpURLConnection conn = null;
            InputStream inStream = null;
            OutputStream outStream = null;
            BufferedInputStream bin = null;
            BufferedReader reader = null;
            BufferedOutputStream bout = null;
            try {
                //Process p = Runtime.getRuntime().exec("su");
                url = new URL(respon_Url); // 서버에 접속한다.
                conn = (HttpURLConnection)url.openConnection();
                // DataOutputStream os = new DataOutputStream(p.getOutputStream());

                System.out.println("conn" + conn);

                contentLength = conn.getContentLength();
                // BufferedInputStream을 쓰지 않으면 느리기 때문에 쓰는 것이다.
                inStream = conn.getInputStream();
                outStream = new FileOutputStream(targetFile.getPath());
                bin = new BufferedInputStream(inStream);
                bout = new BufferedOutputStream(outStream);
                int bytesRead = 0;
                byte[] buffer = new byte[83886080];
                long total = 0;
                while ((bytesRead = bin.read(buffer, 0, 1024)) != -1) {
                    total += bytesRead;
                    bout.write(buffer, 0, bytesRead);
                    publishProgress((int)((total*100)/contentLength));
                }
                System.out.println("start = ");

            }catch (Exception e){
                try {
                    throw e;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }finally {
                try{
                    bin.close();
                    bout.close();
                    inStream.close();
                    outStream.close();
                    conn.disconnect();
                }catch (Exception e){

                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            pDialog.setProgress(values[0]); //전달받은 pos_dialog값으로 ProgressDialog에 변경된 위치 적용
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            pDialog.dismiss(); //ProgressDialog 보이지 않게 하기
            pDialog=null;      //참조변수 초기화
            doRootStuff();
            ItempApi.rebootDevice(MainActivity.this);
            Intent rebootintent = new Intent(REBOOT_ACTION);
            sendBroadcast(rebootintent);

            //doInBackground() 메소드로부터 리턴된 결과 "Complete Load" string Toast로 화면에 표시
        }
    }

    private class JsoupAsyncTask extends AsyncTask<String, Void, Void> {
        @Override protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override protected Void doInBackground(String... params) {
            Log.i(TAG, Arrays.toString(params));
            try {
                // 파라미터로 넘어온 값을 이용해서 url 와 회차를 설정한다.
                String callUrl = params[0];
                Log.e(TAG, callUrl);
                Document doc = Jsoup.connect(callUrl).get();
                // 위의 html tag에서 결과숫자를 싸고 있는 span tag 을 class명을 이용함.
                Elements links;
                if (BuildConfig.ISSENSOR){
                    links = doc.select(".Respon_sensor_version");
                }else {
                    links = doc.select(".Respon_basic_version");
                }
                Log.e(TAG, "links=" + links.size());

                for(Element el : links) {
                    Log.e(TAG, el.text()) ;
                    Server_Version = el.text() ;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } return null;
        }
        @Override protected void onPostExecute(Void result) {
            if(App_Version.equals(Server_Version)) {
                Log.d(TAG,"Server version = "+Server_Version);
                Toast.makeText(MainActivity.this, "이미 최신 버전입니다.", Toast.LENGTH_SHORT).show();
            }else{
                Log.d(TAG,"else Server version = "+Server_Version);
                if(Internet()){
                    CheckVer();
                }else {
                    Toast.makeText(MainActivity.this, "업데이트가 존재합니다. 네트워크를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    boolean Internet(){
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();
        if(ninfo == null){
            Log.d(TAG,"네트워크가 연결되어있지않아요.");
            return false;
        }else{
            Log.d(TAG,"네트워크가 연결되어있어요.");
            return true;
        }
    }

}