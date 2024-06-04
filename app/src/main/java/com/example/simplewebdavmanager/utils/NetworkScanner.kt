package com.example.simplewebdavmanager.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.net.InetSocketAddress
import java.net.Socket

class NetworkScanner(private val context: Context, private val possibleWebDavAddressLiveData: MutableLiveData<String>) {

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
                if (ipAddress != null) {
                    val subnet = ipAddress.substringBeforeLast(".")
                    val timeout = 100 // Timeout for socket connection in milliseconds

                    Thread {
                        for (i in 1..255) {
                            val currentIPAddress = "$subnet.$i"
                            val inetAddress = InetSocketAddress(currentIPAddress, 80)
                            val socket = Socket()

                            try {
                                socket.connect(inetAddress, timeout)
                                // Port 80 is open
                                Log.d("DeviceFound", "Device at $currentIPAddress has port 80 open")
                                possibleWebDavAddressLiveData.postValue(currentIPAddress)
                                socket.close()
                            } catch (e: Exception) {
                                // Port 80 is not open
                            }
                        }
                        stopScanning()
                    }.start()

                } else {
                    Log.d("NetworkScanner", "No IPv4 address found")
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    private fun getLocalIpAddress(network: Network): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val linkProperties = connectivityManager.getLinkProperties(network)

        linkProperties?.linkAddresses?.forEach { linkAddress ->
            val address = linkAddress.address
            if (address is java.net.Inet4Address) {
                return address.hostAddress
            }
        }
        return null
    }

    fun stopScanning() {
        networkCallback?.let {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(it)
        }
    }
}

