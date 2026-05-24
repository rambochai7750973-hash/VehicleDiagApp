package com.vehiclediag.app.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.PatternMatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

data class WifiConnectionState(
    val isConnected: Boolean = false,
    val ssid: String = "",
    val deviceIp: String = "http://192.168.4.1",
)

class WifiConnectionManager(private val context: Context) {

    private val wifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _connectionState = MutableStateFlow(WifiConnectionState())
    val connectionState: StateFlow<WifiConnectionState> = _connectionState.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    val isWifiConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities,
            ) {
                trySend(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        val currentNetwork = connectivityManager.activeNetwork
        val currentCaps = currentNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        trySend(currentCaps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    fun connectToDevice(ssid: String, password: String, ip: String = "http://192.168.4.1") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .setSsidPatternMatcher(PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .setNetworkSpecifier(specifier)
                .build()

            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _connectionState.value = WifiConnectionState(
                        isConnected = true,
                        ssid = ssid,
                        deviceIp = ip,
                    )
                }

                override fun onLost(network: Network) {
                    _connectionState.value = WifiConnectionState(
                        isConnected = false,
                        deviceIp = ip,
                    )
                }
            }

            connectivityManager.requestNetwork(request, networkCallback!!)
        } else {
            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"$ssid\""
                preSharedKey = "\"$password\""
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            }

            val netId = wifiManager.addNetwork(wifiConfig)
            if (netId != -1) {
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()
                _connectionState.value = WifiConnectionState(
                    isConnected = true,
                    ssid = ssid,
                    deviceIp = ip,
                )
            }
        }
    }

    fun disconnect() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
        _connectionState.value = WifiConnectionState()
    }

    fun updateDeviceIp(ip: String) {
        _connectionState.value = _connectionState.value.copy(deviceIp = ip)
    }
}
