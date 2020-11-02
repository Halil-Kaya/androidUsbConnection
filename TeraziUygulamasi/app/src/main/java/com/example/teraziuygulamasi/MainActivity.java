package com.example.teraziuygulamasi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.teraziuygulamasi.usbSerial.CustomProber;
import com.example.teraziuygulamasi.usbSerial.driver.UsbSerialDriver;
import com.example.teraziuygulamasi.usbSerial.driver.UsbSerialPort;
import com.example.teraziuygulamasi.usbSerial.driver.UsbSerialProber;
import com.example.teraziuygulamasi.usbSerial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private enum UsbPermission { Unknown, Requested, Granted, Denied };



    //deviceId bu cihazın id si bu cihazı üreten şirket bütün cihazlara ortak id atıyor bu cihazın id si 1003
    public int deviceId = 1003;
    //baglanilan port 9600
    public int baudRate = 9600;

    public int portNum = 0;


    //cihaza baglanip baglanmadigini bulmak için kullaniyorum
    private boolean connected = false;

    //baglantiyi gerceklestiren objelerim
    private UsbSerialPort usbSerialPort;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private static final String INTENT_ACTION_GRANT_USB = ".GRANT_USB";
    private boolean withIoManager;
    private SerialInputOutputManager usbIoManager;

    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 20000;

    WebView wv;
    public String tarti;



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
        toolbar.setTitle("Temizlik İşleri Müdürlüğü");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

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

        //uygulama ilk acildigina izin aliyorum bu izin tek seferlik 1 kez izin verilmesi yeterli
        izinAl();

    }



    //cihaza baglantiyi gerceklestiren fonksiyon cihaza baglanilmadan read fonksiyonu calisilmaz
    private void connect() {

        //bu aşamadaki komutlarin amaci bağlantıyo oluşturmak
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //burda portlari tarayarak cihazi bulmaya calisiyorum cihazi id sinden buluyorum her cihazın bir id si vardır aynı cihazların id si aynıdır buna vendor id de denir
        for(UsbDevice v : usbManager.getDeviceList().values()) {
            if (v.getDeviceId() == deviceId) {
                //cihazin objesini atıyorum
                device = v;
            }
        }

        //eğer device null ise demekki cihaz bulunamamış demektir
        if(device == null) {
            ekranaYaz("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        //eğer driver null ise o cihazın driverı bulunamadı demektir
        if(driver == null) {
            ekranaYaz("connection failed: no driver for device");
            return;
        }

        //eğer cihazın port sayısını kontrol ediyorum eğer port sayısı 0 dan küçükse (portNum'ın değeri 0) bağlantıyı iptal ediyorum
        if(driver.getPorts().size() < portNum) {
            ekranaYaz("connection failed: not enough ports at device");
            return;
        }


        //seri haberleşmeyi sağlayan objemi oluşturuyorum
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());



        //bu kisimda izinleri kontrol ediyorum eger izinler verilmemisse baglantiyi yapmadan fonksiyonu bitiriyorum
        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }

        //baglantiyi kontrol ediyorum
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                ekranaYaz("connection failed: permission denied");
            else
                ekranaYaz("connection failed: open failed");
            return;
        }

        try {
            //eğer buraya kadar işlem devam edebilmişse bağlantıyı başlatıyorum eğer herhangi bir hata olursa catch bloğuna girip ekrana bilgi bastırıyor
            usbSerialPort.open(usbConnection);

            //burdaki parametrelerde önemli olan baudRate, çoğu cihazın baudRatei 9600 dür diğer parametreler ise neredeyse her cihazda aynıdır eğer farklı bir cihaz denilecekse onun parametrelerine bakılması lazım
            usbSerialPort.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            //kullanıcıya bilgi veriyorum
            ekranaYaz("connected");
            //bağlanıldığı için değişkenimi true yapıyorum
            connected = true;
        } catch (Exception e) {
            ekranaYaz("connection failed: " + e.getMessage());
            //bir hata olursa bağlantıyı kesiyorum
            disconnect();
        }
    }

    //bağlantıyı kestiğim fonksiyon
    private void disconnect() {
        connected = false;
        if(usbIoManager != null)
            usbIoManager.stop();
        usbIoManager = null;
        try {
            usbSerialPort.close();
        } catch (IOException ignored) {}
        usbSerialPort = null;
    }

    //cihazdan veri okuduğumn fonksiyon
    private String read() {
        //eğer bağlantı yok ise ekrana bilgi basıp onu dönüyırm
        if(!connected) {
            ekranaYaz("not connected");
            //değer olarak bilgi mesajını dönüyorum
            return "not connected";
        }
        try {

            //verileri bir buffer objesinin içine yazıyorum
            byte[] buffer = new byte[8192];

            //asıl olay burda okumayı burda yapıyor!
            int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);

            //amacı gelen datayı stringe çevirip, datanın sadece belli bir kısmını kesip döndürüyor
            return datayiDuzenle(Arrays.copyOf(buffer, len));



        } catch (IOException e) {
            disconnect();
            return "err";
        }
    }

    public void ekranaYaz(String mesaj){
        Toast.makeText(this,mesaj,Toast.LENGTH_SHORT).show();
    }


    private String datayiDuzenle(byte[] data) {


        //byte dizisi olarak gelen datayı stringe dönüştürüyor
        String dataString = new String(data);

        /*
        burda bi kırpma işlemi yapıyorum gelen datanın içinde 10 tane bazende 5 tane paket oluyor bize 1 tanesi yeterli
        ayrıca her paket => ST,GS,+ 0.454kg ceya ST,GS,- 0.254kg şeklinde geliyor bana + veya - kısımdan sonra g harfine kadar olan kısım lazım o yüzden
        bir kesme işlemi yapıyorum
        * */

        //datanın + kısmı kaçıncı indexte onu buluyorum
        int dataBaslangic = dataString.indexOf(" ");



        //g harfinin indexini buluyorum ve ona +1 ekliyorum amacım g kısmını da almak
        int dataBitis = dataString.indexOf("g") + 1;

        //datanın başlangıç kısmı küçük bir ihtimalde olsa bazen bozuk geliyor o yüzden eğer dataBaslangic in değeri dataBitisten yüksekse datayı kırpıp tekrardan index konumlarını alıyorum
        if(dataBaslangic > dataBitis){
            dataString = dataString.substring(dataBaslangic,dataString.length());

            dataBaslangic = dataString.indexOf(" ");
            dataBitis = dataString.indexOf("g") + 1;
        }

        //dataBaslangic tan dataBitis e kadar olan kısmı kesiyorum
        String kg = "";
        for(int i = dataBaslangic;i<dataBitis;i++){
            kg = kg + String.valueOf(dataString.charAt(i));
        }

        if(dataString.contains("+")){
            kg = "+" + kg;
        }else{
            kg = "-" + kg;
        }



        //artık kiloyu aldım onu döndürüyorum eğer gelen datada bir problem var ise boş string döndürüyor
        return kg;

    }



    //izin aldığım fonksiyon
    public void izinAl(){

        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == deviceId)
                device = v;
        if(device == null) {
            ekranaYaz("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            ekranaYaz("connection failed: no driver for device");
            return;
        }

        if(driver.getPorts().size() < portNum) {
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());

        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }

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

            //eğer connected fonksiyonu false ise bağlantı yok demek o yüzden bağlantıyı başlatıyorum
            if(!connected){
                izinAl();
                connect();
            }

            //kg degerini alip tartiya atiyorum
            tarti = read();

            //tartiyi dönüyorum
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}