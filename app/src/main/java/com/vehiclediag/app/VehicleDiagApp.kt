package com.vehiclediag.app

import android.app.Application
import com.vehiclediag.app.connectivity.WifiConnectionManager

class VehicleDiagApp : Application() {
    lateinit var wifiManager: WifiConnectionManager
        private set

    override fun onCreate() {
        super.onCreate()
        wifiManager = WifiConnectionManager(this)
    }
}
