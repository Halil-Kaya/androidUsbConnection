package de.kai_morich.simple_usb_terminal;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    WebView wv;
    String tarti;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        String s = "";

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            s = "Landspace";
        }
        else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            s =  "Portrait";
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wv = (WebView) findViewById(R.id.wv);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.setFocusable(true);
        wv.setFocusableInTouchMode(true);

        wv.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setDatabaseEnabled(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.setWebViewClient(new WebViewClient(){

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }

        });

        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);




        JavaScriptInterface JSInterface = new JavaScriptInterface(this);
        wv.addJavascriptInterface(JSInterface, "JSInterface");
        Bundle extras = getIntent().getExtras();
        String URL = "http://zeybim.zeytinburnu.bel.tr/";
        Log.e("test", "onCreate: " +extras );
        //  if (extras == null) {

        Log.e("x", "on create : " + URL);



        wv.loadUrl(URL);




       /* if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
        else
            onBackStackChanged();*/
    }


    public class JavaScriptInterface {



        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @android.webkit.JavascriptInterface
        public void testID(String val1,String val2,String val3)
        {
            Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse("http://y2018.zeytinburnu.bel.tr:6161/proje/dosya_goster.php?url=zeytinburnu/"+val1+"/"+val2 +"/"+val3 ) );

            //   Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse("http://http://10.100.1.81/zeytinburnu/ruhsat/"+val1+"/"+val2 +"/"+val3 ) );

            startActivity(browse);

            Log.e("x","Javascript Tarafından Android Methodu Calistirildi. VAL : "+val1 + val2 + val3);
        }

        @android.webkit.JavascriptInterface
        public String readTart(String link) {


            return "1234";



          //  Intent myIntent = new Intent(MainActivity.this, TerminalFragment);

           // startActivity(myIntent);
        }

        @android.webkit.JavascriptInterface
        public String fn_barkod_al() {


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //wv.setVisibility(View.GONE);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragmentcontainer, new TerminalFragment(), "devices").commit();
                    // findViewById(R.id.fragmentcontainer).setVisibility(View.VISIBLE);
                   // findViewById(R.id.fragmentcontainer).bringToFront();


                }
            });



            return tarti;



        }


    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }
    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            TerminalFragment terminal = (TerminalFragment)getSupportFragmentManager().findFragmentByTag("terminal");
            if (terminal != null)
                terminal.status("USB device detected");
        }
        super.onNewIntent(intent);
    }

}
