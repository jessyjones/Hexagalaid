package com.makerinthemaking.hexagalet.activities

import android.content.Intent
import android.graphics.Color.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.makerinthemaking.hexagalet.R
import com.makerinthemaking.hexagalet.profile.GaletManager
import no.nordicsemi.android.ble.livedata.state.ConnectionState

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var loggingTag: String ;
    private lateinit var adapter: PreferencesActivity.CustomAdapter
    private lateinit var myListView: ListView
    private lateinit var mConnectionStatus : TextView ;
    var mydb: DBHelper? = null

    private var  mGaletmanager : GaletManager?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loggingTag = getString(R.string.app_name)
        Log.i(loggingTag, "starting up")

        val scannerButton : Button = findViewById(R.id.main_scannerButton)
        val appsButton : Button = findViewById(R.id.main_appsButton)
        mConnectionStatus = findViewById(R.id.main_blestatus)

        // TODO : make it work without delay
        scannerButton.setOnClickListener {
            Handler().postDelayed({
                val intent = Intent(this, ScannerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 1000)
        }

        appsButton.setOnClickListener {
            Handler().postDelayed({
                val intent = Intent(this, PreferencesActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 1000)
        }

        if (mGaletmanager == null) {
            mGaletmanager = GaletManager.getInstance(applicationContext)
        }

        Log.e(TAG, "GaletManager instance " + mGaletmanager.toString())

        getConnectionState().observe(this, connectionStateObserver)
        updateText("TEXT")

    }

    private fun getConnectionState(): LiveData<ConnectionState> {
        return mGaletmanager?.state!!
    }

    fun updateText(updatedStatus: String){
        mConnectionStatus.text = updatedStatus
    }

    val connectionStateObserver =
        Observer { state: ConnectionState ->
            when (state.state) {
                ConnectionState.State.CONNECTING -> {
                    updateText("CONNECTING")
                    Log.d(TAG, "CONNECTING")
                }
                ConnectionState.State.INITIALIZING -> {
                    updateText("INITIALIZING")
                    Log.d(TAG, "INITIALIZING")
                }
                ConnectionState.State.READY -> {
                    updateText("READY")
                    Log.d(TAG, "READY")
                }
                ConnectionState.State.DISCONNECTED -> {
                    updateText("DISCONNECTED")
                    Log.d(TAG, "DISCONNECTED")
                    if (state is ConnectionState.Disconnected) {
                        val stateWithReason: ConnectionState.Disconnected =
                            state
                        if (stateWithReason.isNotSupported) {
                            Log.d(TAG, "DISCONNECTED")
                        }
                    }
                }
                ConnectionState.State.DISCONNECTING -> {
                    updateText("DISCONNECTING")
                }
            }
        }

}
