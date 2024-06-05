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
import androidx.core.view.marginEnd
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.fragments.ConnectionDetailsFragment
import com.example.simplewebdavmanager.fragments.dialogFragments.SetWebDavAddresDialog
import com.example.simplewebdavmanager.utils.NetworkScanner

/**
 * Class for the main activity of the app, that manages the menu items clicks and the search view
 *
 */
class MainActivity : AppCompatActivity() {

    private val webDavAddressLiveData = MutableLiveData<String>()
    private lateinit var connectionDetailsFragment: ConnectionDetailsFragment

    /**
     * I disable the dark mode and enable edge to edge for the activity
     * And initialize the fragmentContainer
     *
     * @param savedInstanceState
     */
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

    }

    /**
     * Creation of the menu items and the search view
     *
     * @param menu selected item in the menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_items, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search..."
        searchView.maxWidth = Integer.MAX_VALUE

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

    /**
     * Handle the menu items clicks
     *
     * @param item selected item in the menu
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.itemSetWebdavAddres -> {
                val dialog = SetWebDavAddresDialog()
                dialog.show(supportFragmentManager, "dialog")
                true
            }
            R.id.itemSearchNetwork -> {
                val connectionDetailsFragment: ConnectionDetailsFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as ConnectionDetailsFragment
                item.setIcon(R.drawable.network_wired)
                connectionDetailsFragment.scanLocalNetwork()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Get the live data of the webdav address
     *
     * @return LiveData
     */
    fun getWebDavAddressLiveData(): LiveData<String> {
        return webDavAddressLiveData
    }

    /**
     * Set the webdav address
     *
     * @param webDavAddress
     */
    fun setWebDavAddress(webDavAddress: String) {
        webDavAddressLiveData.value = webDavAddress
    }
}
