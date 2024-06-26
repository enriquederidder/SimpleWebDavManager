package com.example.simplewebdavmanager.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Class that scans the local network for devices that have port 80 open.
 *
 * @property context
 * @property possibleWebDavAddressLiveData MutableLiveData to notify when a device has port 80 open
 */
class NetworkScanner(
    private val context: Context,
    private val possibleWebDavAddressLiveData: MutableLiveData<String>
) {

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val executorService = Executors.newFixedThreadPool(10)
    private var scanning = false

    /**
     * main function that scans the local network for devices that have port 80 open, form 1 to 255.
     * Uses HttpURLConnection to check if a device has port 80.
     *
     */
    fun scanLocalNetwork() {
        if (scanning) return // Prevent multiple scans
        scanning = true

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

                    for (i in 1..255) {
                        val currentIPAddress = "$subnet.$i"
                        executorService.submit {
                            try {
                                val url = URL("http://$currentIPAddress")
                                val connection = url.openConnection() as HttpURLConnection
                                connection.connectTimeout =
                                    500 // Timeout for connection in milliseconds, The ESP is slow to respond btw
                                connection.requestMethod = "HEAD"

                                val responseCode = connection.responseCode
                                Log.d(
                                    "DeviceFound",
                                    "Response code: $responseCode for $currentIPAddress"
                                )
                                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 404) {
                                    Log.d(
                                        "DeviceFound",
                                        "Device at $currentIPAddress has port 80 open"
                                    )
                                    possibleWebDavAddressLiveData.postValue(currentIPAddress)
                                }
                                connection.disconnect()
                            } catch (e: Exception) {
                                // connection failed
                            }
                        }
                    }
                } else {
                    Log.d("NetworkScanner", "No IPv4 address found")
                }
            }

            override fun onLost(network: Network) {
                stopScanning()
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    /**
     * Get the local IP address of the given network.
     *
     * @param network
     * @return
     */
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

    /**
     * Stop scanning.
     */
    fun stopScanning() {
        scanning = false
        networkCallback?.let {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
        executorService.shutdownNow()
    }
}
