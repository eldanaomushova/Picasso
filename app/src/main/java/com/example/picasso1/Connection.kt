package com.example.picasso1
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
class Connection : AppCompatActivity() {
    private lateinit var connectionbtn: Button
    private lateinit var getDevisebtn: Button
    private lateinit var bluetoothAdaptor: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var intentFilter: IntentFilter
    private lateinit var inputStream: InputStream;
    private lateinit var outputStream: OutputStream;
    private lateinit var rxThread: RxThread;
    private lateinit var stopButton: Button;
    private lateinit var torightstepper: Button;
    private lateinit var toleftstepper: Button;
    private var rxData: String = "";
    private val REQUEST_BLUETOOTH_PERMISSION = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        }
        connectionbtn = findViewById(R.id.connbtn)
        getDevisebtn = findViewById(R.id.getDevise)
        stopButton = findViewById(R.id.stopMove)
        torightstepper = findViewById(R.id.rightMove)
        toleftstepper = findViewById(R.id.leftMove)
        bluetoothAdaptor = BluetoothAdapter.getDefaultAdapter()
        intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(Btreceiver, intentFilter)
        connectionbtn.isEnabled = false;
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        getDevisebtn.setOnClickListener(View.OnClickListener {
            val devices: Set<BluetoothDevice> = bluetoothAdaptor.bondedDevices
            var isHC05Found = false

            for (dev in devices) {
                if (dev.name == "HC-05") {
                    bluetoothDevice = dev
                    isHC05Found = true
                    break
                }
            }
            if (isHC05Found) {
                Toast.makeText(applicationContext, "Device found: HC-05", Toast.LENGTH_SHORT).show()
                connectionbtn.isEnabled = true
            } else {
                Toast.makeText(applicationContext, "HC-05 not found among bonded devices", Toast.LENGTH_SHORT).show()
            }
        })
        connectionbtn.setOnClickListener(View.OnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@OnClickListener
            }
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                bluetoothSocket.connect()
                inputStream = bluetoothSocket.inputStream
                outputStream = bluetoothSocket.outputStream
                rxThread = RxThread(inputStream)
                rxThread.isRunning = true
                rxThread.start()
                if(rxThread.isRunning){
                    Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext, "Error with connecting", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error with connecting", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error with connecting", Toast.LENGTH_SHORT).show()
            }
        })


        stopButton.setOnClickListener(View.OnClickListener {
            try {
                outputStream.write(("0\r\n").toByteArray())
            }catch (e: IOException) {
                e.printStackTrace()
            }
        })
        toleftstepper.setOnClickListener(View.OnClickListener {
            try {
                outputStream.write(("1\r\n").toByteArray())
            }catch (e: IOException) {
                e.printStackTrace()
            }
        })
        torightstepper.setOnClickListener(View.OnClickListener {
            try {
                outputStream.write(("2\r\n").toByteArray())
            }catch (e: IOException) {
                e.printStackTrace()
            }
        })

    }
    private fun hasBluetoothPermissions(): Boolean {
        return (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)
    }
    private fun requestBluetoothPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ),
            REQUEST_BLUETOOTH_PERMISSION
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {

                } else {

                }
            }
        }
    }
    class RxThread(private val inputStream: InputStream) : Thread() {
        var isRunning: Boolean = false
        init {
            isRunning = false
        }
        override fun run() {
            while (isRunning) {
                try {
                    if (inputStream.available() > 0) {
                        // Read data from the inputStream and process it
                        val data = ByteArray(inputStream.available())
                        inputStream.read(data)
                        val receivedData = String(data)
                        // Process receivedData as needed
                        println("Received data: $receivedData")
                    }
                    Thread.sleep(10)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private var Btreceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        connectionbtn.isEnabled = true
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        rxThread.isRunning = false;
                    }
                }
            }
        }
    }
}
