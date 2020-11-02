package com.example.teraziuygulamasi.usbSerial.driver;

import android.hardware.usb.UsbDevice;

import java.util.List;

public interface UsbSerialDriver {

    /**
     * Returns the raw {@link UsbDevice} backing this port.
     *
     * @return the device
     */
    public UsbDevice getDevice();

    /**
     * Returns all available ports for this device. This list must have at least
     * one entry.
     *
     * @return the ports
     */
    public List<UsbSerialPort> getPorts();
}
