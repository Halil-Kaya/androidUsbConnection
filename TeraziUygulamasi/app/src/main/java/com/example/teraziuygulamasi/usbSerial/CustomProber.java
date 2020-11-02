package com.example.teraziuygulamasi.usbSerial;

import com.example.teraziuygulamasi.usbSerial.driver.CdcAcmSerialDriver;
import com.example.teraziuygulamasi.usbSerial.driver.ProbeTable;
import com.example.teraziuygulamasi.usbSerial.driver.UsbSerialProber;

public class CustomProber {

    public static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x16d0, 0x087e, CdcAcmSerialDriver.class); // e.g. Digispark CDC
        return new UsbSerialProber(customTable);
    }

}
