package com.example.usbserialconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {





    public UsbManager m_usbManager;
    public UsbDevice m_device = null;
    public UsbSerialDevice m_serial = null;
    public UsbDeviceConnection m_connection = null;

    public String ACTION_USB_PERMISSION = "permission";

    public Button btnOn;
    public Button btnOff;
    public Button btnConnect;
    public Button btnDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeWidgets();

        /*

        UsbManager dan UsbDevice i buluyorum

        */

        m_usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);


        UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
            @Override
            public void onReceivedData(byte[] data) {
                //dataya cevap buraya geliyor!
                String rslt = byteToString(data);
                Toast.makeText(MainActivity.this,"data geldi!: " + rslt,Toast.LENGTH_LONG).show();
            }
        };


        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().toString().equals(ACTION_USB_PERMISSION)){
                    Boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if(granted){
                        m_connection = m_usbManager.openDevice(m_device);
                        m_serial = UsbSerialDevice.createUsbSerialDevice(m_device,m_connection);
                        if(m_serial != null){

                            if(m_serial.open()){

                                m_serial.setBaudRate(9600);
                                m_serial.setDataBits(UsbSerialDevice.DATA_BITS_8);
                                m_serial.setStopBits(UsbSerialDevice.STOP_BITS_1);
                                m_serial.setParity(UsbSerialDevice.PARITY_NONE);
                                m_serial.setFlowControl(UsbSerialDevice.FLOW_CONTROL_OFF);
                                m_serial.read(mCallback);

                            }else{
                                Toast.makeText(MainActivity.this,"baglanti basarisiz<port acilmadi>",Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(MainActivity.this,"baglanti basarisiz<port yok>",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"izin yok?",Toast.LENGTH_LONG).show();
                    }
                }else if(intent.getAction().toString().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                    startUsbConnecting();
                }else if(intent.getAction().toString().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                    disconnecting();
                }

            }
        };



        IntentFilter filter = new IntentFilter();

        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(broadcastReceiver, filter);


        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //baglantiyi aciyor
                sendData("");
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //baglantiyi kapatiyor
                sendData("");
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnecting();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUsbConnecting();
            }
        });


    }

    public String byteToString(byte[] data){

        StringBuilder sonuc = new StringBuilder();

        for (byte datum : data) {
            sonuc.append(datum);
        }

        return sonuc.toString();
    }

    private void startUsbConnecting(){
        HashMap<String,UsbDevice> usbDevices = m_usbManager.getDeviceList();

        if(!usbDevices.isEmpty()){
            boolean keep = true;

            for(Map.Entry<String,UsbDevice> pair : usbDevices.entrySet()){
                m_device = pair.getValue();
                int deviceVendorId = m_device.getVendorId();
                Toast.makeText(this,"vendorId: " + deviceVendorId,Toast.LENGTH_LONG).show();
                if(deviceVendorId == 1027){
                    PendingIntent intent = PendingIntent.getBroadcast(this,0, new Intent(ACTION_USB_PERMISSION),0);
                    m_usbManager.requestPermission(m_device,intent);
                    keep = false;
                    Toast.makeText(this,"baglanti basarili!",Toast.LENGTH_LONG).show();
                }else{

                    m_connection = null;
                    m_device = null;

                }

                if(!keep){
                    return;
                }
            }
        }else {

            Toast.makeText(this,"bagli cihaz yok",Toast.LENGTH_LONG).show();

        }


    }

    private void sendData(String mesaj){
        m_serial.write(mesaj.getBytes());
        Toast.makeText(this,"mesaj gonderildi!: " + Arrays.toString(mesaj.getBytes()),Toast.LENGTH_LONG).show();
    }

    private void disconnecting(){
        if(m_serial != null){
            m_serial.close();
        }else{
            Toast.makeText(this,"once baglan!",Toast.LENGTH_LONG).show();
        }
    }


    public void InitializeWidgets(){

        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);

    }


}