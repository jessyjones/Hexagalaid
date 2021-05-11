package com.makerinthemaking.hexagalet.profile

import no.nordicsemi.android.ble.livedata.ObservableBleManager
import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.log.LogSession
import androidx.lifecycle.LiveData
import no.nordicsemi.android.ble.BleManager.BleManagerGattCallback
import com.makerinthemaking.hexagalet.profile.GaletManager.GaletBleManagerGattCallback
import com.makerinthemaking.hexagalet.profile.callback.GaletBatteryDataCallback
import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.log.LogContract
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import com.makerinthemaking.hexagalet.profile.GaletManager
import no.nordicsemi.android.ble.callback.SuccessCallback
import no.nordicsemi.android.ble.data.Data
import java.util.*

// class Manager private constructor(context: Context) {
//    init {
//        // Init using context argument
//    }
//
//    companion object : SingletonHolder<Manager, Context>(::Manager)
//}

 class GaletManager private constructor(context: Context) : ObservableBleManager(context) {

     init {

     }

    val batteryState = MutableLiveData<Int>()
    private var batteryCharacteristic: BluetoothGattCharacteristic? = null
    private var pixelCharacteristic: BluetoothGattCharacteristic? = null
    private var mRXCharacteristic: BluetoothGattCharacteristic? = null
    private var mTXCharacteristic: BluetoothGattCharacteristic? = null
    private val mOutgoingBuffer: ByteArray? = null
    private val mBufferOffset = 0
    private val logSession: LogSession? = null
    private var supported = false
    private val ledOn = false
    override fun getGattCallback(): BleManagerGattCallback {
        return GaletBleManagerGattCallback()
    }

    private val batteryCallback: GaletBatteryDataCallback = object : GaletBatteryDataCallback() {
        override fun onBatteryStateChanged(
            device: BluetoothDevice,
            level: Int
        ) {
            log(LogContract.Log.Level.APPLICATION, "Battery $level")
        }

        override fun onInvalidDataReceived(
            device: BluetoothDevice,
            data: Data
        ) {
            log(Log.WARN, "Invalid data received: $data")
        }
    }

    private inner class GaletBleManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            // TODO add pixels
            setNotificationCallback(batteryCharacteristic).with(batteryCallback)
            readCharacteristic(batteryCharacteristic).with(batteryCallback).enqueue()
            enableNotifications(batteryCharacteristic).enqueue()
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(UART_UUID_SERVICE)
            if (service != null) {
                batteryCharacteristic = service.getCharacteristic(LBS_UUID_BATT_CHAR)
                mRXCharacteristic = service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID)
                mTXCharacteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID)
            }
            var writeRequest = false
            var writeCommand = false
            if (mRXCharacteristic != null) {
                val rxProperties = mRXCharacteristic!!.properties
                writeRequest = rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
                writeCommand =
                    rxProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0

                // Set the WRITE REQUEST type when the characteristic supports it. This will allow to send long write (also if the characteristic support it).
                // In case there is no WRITE REQUEST property, this manager will divide texts longer then 20 bytes into up to 20 bytes chunks.
                if (writeRequest) mRXCharacteristic!!.writeType =
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            // TODO add real characteristics
            //supported = batteryCharacteristic != null ;
            supported = true
            return supported
        }

        override fun onDeviceDisconnected() {
            // TODO : anything to add here ?
            mRXCharacteristic = null
            mTXCharacteristic = null
            batteryCharacteristic = null
            pixelCharacteristic = null
        }
    }

    fun sendStuff(message: String?) {
        beginAtomicRequestQueue()
            .add(enableNotifications(mRXCharacteristic))
            .done { device: BluetoothDevice? -> log(Log.INFO, "Target initialized") }
            .enqueue()
        writeCharacteristic(mRXCharacteristic, Data.from(message!!))
            .split()
            .enqueue()
    }

    companion object : SingletonHolder<GaletManager, Context>(::GaletManager){

        @JvmField
        val UART_UUID_SERVICE = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")

        /** TODO Add Neopixel characteristic UUID.  */
        private val LBS_UUID_PIXEL_CHAR = UUID.fromString("00001524-1212-efde-1523-785feabcd123")

        /** TODO Add Battery characteristic UUID.  */
        private val LBS_UUID_BATT_CHAR = UUID.fromString("00001525-1212-efde-1523-785feabcd123")

        /** RX characteristic UUID  */
        private val UART_RX_CHARACTERISTIC_UUID =
            UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")

        /** TX characteristic UUID  */
        private val UART_TX_CHARACTERISTIC_UUID =
            UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

        /** The maximum packet size is 20 bytes.  */
        private const val MAX_PACKET_SIZE = 20
    }
}
open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}