package com.example.usbserialconnectionversion2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    public EditText etKomut;
    public Button btnConnect;
    public Button btnGonder;

    public UsbManager usbManager = null;
    public UsbDevice device = null;
    public UsbSerialDevice serial = null;
    public UsbDeviceConnection usbConnection = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeWidgets();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);




        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDevice();
            }
        });


        btnGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etKomut.getText().toString().equals("")){
                    ekranaMesajYaz("komut gir!");
                }else{

                    sendData(etKomut.getText().toString());

                }
            }
        });


    }

    public void sendData(String mesaj){

        if(device == null || serial == null){
            ekranaMesajYaz("baglanilmis cihaz yok!");
        }else{
            serial.write(mesaj.getBytes());
            ekranaMesajYaz("cihaza komut gonderildi: " + mesaj);
        }

    }


    public void connectToDevice(){


        HashMap<String,UsbDevice> usbDevices = usbManager.getDeviceList();


        if(!usbDevices.isEmpty()){
            boolean keep = true;

            for(Map.Entry<String,UsbDevice> pair : usbDevices.entrySet()){
                device = pair.getValue();
                int deviceVendorId = device.getVendorId();
                Toast.makeText(this,"vendorId: " + deviceVendorId,Toast.LENGTH_LONG).show();

                //vendor id ye gore cihazi bulacam ve o cihaza baglanicam! diyelim ki buldum ve baglaniyorum

                if(deviceVendorId == 1027){
                    usbConnection = usbManager.openDevice(device);
                    serial = UsbSerialDevice.createUsbSerialDevice(device,usbConnection);

                    if(serial != null){

                        if(serial.open()){

                            serial.setBaudRate(9600);
                            serial.setDataBits(UsbSerialDevice.DATA_BITS_8);
                            serial.setStopBits(UsbSerialDevice.STOP_BITS_1);
                            serial.setParity(UsbSerialDevice.PARITY_NONE);
                            serial.setFlowControl(UsbSerialDevice.FLOW_CONTROL_OFF);

                            UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {

                                @Override
                                public void onReceivedData(byte[] arg0)
                                {
                                    //data geldiğinde buraya girecek
                                    ekranaMesajYaz("gelen data: " + byteToString(arg0));
                                }

                            };

                            serial.read(mCallback);

                        }else{
                            ekranaMesajYaz("baglanti basarisiz <port acilmadi!>");
                        }

                    }else{
                        ekranaMesajYaz("baglanti basarisiz <port yok>");
                    }

                }





            }
        }else {
            Toast.makeText(this,"bagli cihaz yok",Toast.LENGTH_LONG).show();
        }




    }

    public void ekranaMesajYaz(String mesaj){
        Toast.makeText(this,mesaj,Toast.LENGTH_LONG).show();
    }

    public String byteToString(byte[] data){

        StringBuilder sonuc = new StringBuilder();

        for (byte datum : data) {
            sonuc.append(datum);
        }

        return sonuc.toString();
    }


    public void initializeWidgets(){
        etKomut = findViewById(R.id.etKomut);
        btnConnect = findViewById(R.id.btnConnect);
        btnGonder = findViewById(R.id.btnGonder);
    }

}