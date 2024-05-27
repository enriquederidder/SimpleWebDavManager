package com.example.simplewebdavmanager.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import java.net.InetSocketAddress
import java.net.Socket

class NetworkScanner(private val context: Context) {

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun scanLocalNetwork() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val ipAddress = getLocalIpAddress(network)
                val subnet = ipAddress.substringBeforeLast(".")
                val timeout = 2000 // Timeout for socket connection in milliseconds

                for (i in 1..255) {
                    val currentIPAddress = "$subnet.$i"
                    val inetAddress = InetSocketAddress(currentIPAddress, 80)
                    val socket = Socket()

                    try {
                        socket.connect(inetAddress, timeout)
                        // Port 80 is open
                        Log.d("DeviceFound", "Device at $currentIPAddress has port 80 open")
                        socket.close()
                    } catch (e: Exception) {
                        // Port 80 is not open
                    }
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    private fun getLocalIpAddress(network: Network): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val linkProperties = connectivityManager.getLinkProperties(network)
        return linkProperties?.linkAddresses?.firstOrNull()?.address?.hostAddress ?: ""
    }

    fun stopScanning() {
        networkCallback?.let {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(it)
        }
    }
}

