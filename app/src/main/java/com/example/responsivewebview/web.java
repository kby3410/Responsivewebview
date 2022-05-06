package com.example.responsivewebview;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mylibrary.ItempApi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.provider.Settings.ACTION_SETTINGS;

public class web extends AppCompatActivity implements View.OnClickListener{
    private WebView mWebView;
    private TextView Web_url;
    private String Url,Name;
    private int Check_number;
    private Button Hidekey_button,Back_button, Forward_button, Home_button, Reload_button;
    long then = 0;
    int count = 0;
    private volatile boolean running = true;
    private DBHandler handler;
    private final ArrayList<String> Http_list = new ArrayList<>();
    private final ArrayList<String> Name_list = new ArrayList<>();
    private final ArrayList<Integer> Check_list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();    //액션바 숨기기
        actionBar.hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
       // hideNavigationBar();
        hookWebView();
        setContentView(R.layout.web);

        Hidekey_button = (Button)findViewById(R.id.hidekey);
        Hidekey_button.setOnClickListener(this);
        Back_button = (Button)findViewById(R.id.back_button);
        Back_button.setOnClickListener(this);
        Forward_button = (Button)findViewById(R.id.forward_button);
        Forward_button.setOnClickListener(this);
        Home_button = (Button)findViewById(R.id.home_button);
        Home_button.setOnClickListener(this);
        Reload_button = (Button)findViewById(R.id.reload_button);
        Reload_button.setOnClickListener(this);
        Web_url = (TextView)findViewById(R.id.web_url);

        Back_button.setBackgroundResource(R.drawable.back_button);
        Forward_button.setBackgroundResource(R.drawable.forward_button);


        ItempApi.controlSystemBarHide(this);
        Settings.System.putInt(getContentResolver(), "screen_brightness", 255);

        Intent intent = getIntent();
        Url = intent.getExtras().getString("url");
        Name = intent.getExtras().getString("name");
        Check_number = intent.getExtras().getInt("check_number");

        // 웹뷰 셋팅
        mWebView = (WebView) findViewById(R.id.test);//xml 자바코드 연결



        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setUseWideViewPort(true);
        mWebView.setInitialScale(1);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);  //
        mWebView.loadUrl(Url);//웹뷰 실행
        mWebView.setWebViewClient(new WebViewClient());//웹뷰에 크롬 사용 허용//이 부분이 없으면 크롬에서 alert가 뜨지 않음
        Button_Check();
        Thread();
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String sTitle) {
                super.onReceivedTitle(view, sTitle);
                if (sTitle != null && sTitle.length() > 0) {
                    Web_url.setText(sTitle);
                } else {
                    Web_url.setText("ShopView");
                }
            }
        });

    }


    /*public boolean onKeyDown(int keyCode, KeyEvent event) {//뒤로가기 버튼 이벤트
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {//웹뷰에서 뒤로가기 버튼을 누르면 뒤로가짐
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.hidekey:
                count++;
                System.out.println("hidden_button"+count);
                if(count == 5){
                    handler = DBHandler.open(this);
                    Cursor cursor;
                    cursor = handler.select();
                    while (cursor.moveToNext()){
                        Http_list.add(cursor.getString(0));
                        Name_list.add(cursor.getString(1));
                        Check_list.add(cursor.getInt(2));
                    }
                    for(int i = 0; i < Http_list.size(); i++){
                       handler.update(Http_list.get(i),Name_list.get(i),0);
                       System.out.println("check_number = " + Check_list.get(i));
                    }

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.back_button:
                System.out.println("back_button");
                mWebView.goBack();
                break;
            case R.id.forward_button:
                System.out.println("forward_button");
                mWebView.goForward();
                break;
            case R.id.home_button:
                mWebView.loadUrl(Url);
                break;
            case R.id.reload_button:
                System.out.println("reload_button");
                mWebView.reload();
                break;


        }
    }

    @Override public void onBackPressed() {
        //super.onBackPressed();
    }


    void Thread(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button_Check();
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

    public void terminate() {
        running = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        terminate();
        finish();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        running = true;
        Thread();

    }

    public void Button_Check() {
        if(mWebView.canGoBack()){
            Back_button.setBackgroundResource(R.drawable.back_button);
        }else{
            Back_button.setBackgroundResource(R.drawable.back_empty);
        }
        if(mWebView.canGoForward()){
            Forward_button.setBackgroundResource(R.drawable.forward_button);
        }else{
            Forward_button.setBackgroundResource(R.drawable.forward_empty);
        }
    }

    public static void hookWebView(){         //system.uid 사용 후 웹뷰 사용시 필수
        int sdkInt = Build.VERSION.SDK_INT;
        try {
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                Log.i("11","sProviderInstance isn't null");
                return;
            }

            Method getProviderClassMethod;
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
            } else {
                Log.i("","Don't need to Hook WebView");
                return;
            }
            getProviderClassMethod.setAccessible(true);
            Class<?> factoryProviderClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
            Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor();
            delegateConstructor.setAccessible(true);
            if(sdkInt <26){//Less than Android O version
                Constructor<?> providerConstructor = factoryProviderClass.getConstructor(delegateClass);
                if (providerConstructor != null) {
                    providerConstructor.setAccessible(true);
                    sProviderInstance = providerConstructor.newInstance(delegateConstructor.newInstance());
                }
            } else {
                Field chromiumMethodName = factoryClass.getDeclaredField("CHROMIUM_WEBVIEW_FACTORY_METHOD");
                chromiumMethodName.setAccessible(true);
                String chromiumMethodNameStr = (String)chromiumMethodName.get(null);
                if (chromiumMethodNameStr == null) {
                    chromiumMethodNameStr = "create";
                }
                Method staticFactory = factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass);
                if (staticFactory!=null){
                    sProviderInstance = staticFactory.invoke(null, delegateConstructor.newInstance());
                }
            }

            if (sProviderInstance != null){
                field.set("sProviderInstance", sProviderInstance);
                Log.i("11","Hook success!");
            } else {
                Log.i("11","Hook failed!");
            }
        } catch (Throwable e) {
            Log.w("11",e);
        }
    }


}
