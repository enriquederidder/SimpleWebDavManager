package com.example.simplewebdavmanager.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.fragments.ConnectionDetailsFragment
import com.example.simplewebdavmanager.fragments.dialogFragments.SetWebDavAddresDialog
import com.example.simplewebdavmanager.utils.NetworkScanner


class MainActivity : AppCompatActivity() {

    private val webDavAddressLiveData = MutableLiveData<String>()
    private lateinit var connectionDetailsFragment: ConnectionDetailsFragment
    private lateinit var networkScanner: NetworkScanner
    private val possibleWebDavAddressLiveData = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // disable dark mode for now

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the fragmentContainer
        connectionDetailsFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as ConnectionDetailsFragment

        networkScanner = NetworkScanner(this, possibleWebDavAddressLiveData)
        networkScanner.scanLocalNetwork()

        possibleWebDavAddressLiveData.observe(this, Observer { address ->

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_items, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Dont to anything
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search query text change
                connectionDetailsFragment.filterFiles(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.itemSetWebdavAddres -> {
                val dialog = SetWebDavAddresDialog()
                dialog.show(supportFragmentManager, "dialog")
                Log.d("AddresList", possibleWebDavAddressLiveData.value.toString() )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getWebDavAddressLiveData(): LiveData<String> {
        return webDavAddressLiveData
    }

    fun setWebDavAddress(webDavAddress: String) {
        webDavAddressLiveData.value = webDavAddress
    }
}
