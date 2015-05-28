package com.cvte.upnp.demo;

import android.net.wifi.WifiManager;
import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpServiceImpl;

public class BrowserUpnpService extends AndroidUpnpServiceImpl {

    @Override
    protected AndroidUpnpServiceConfiguration createConfiguration(
            WifiManager wifiManager) {
        return new AndroidUpnpServiceConfiguration(wifiManager) {

        };
    }
}
