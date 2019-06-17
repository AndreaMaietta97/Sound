package com.example.sound

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.log10
import kotlin.math.roundToInt


const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity() {
    lateinit var timer: Timer
    var uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")!!
    var mBluetoothAdapter: BluetoothAdapter? = null
    var mmSocket: BluetoothSocket? = null
    var mmDevice: BluetoothDevice? = null
    var outStream: OutputStream? = null
    private var permissionToRecordAccepted = false
    private var recorder: SoundMeter? = null

    var permissions: Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    var address = ""
    private var maxi = 0
    var pin = 0
    private var vmax = 0.00
    var valori: ArrayList<Pair<Int, Int>> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        bttStart.setOnClickListener { onRecord(true) }
        bttStop.setOnClickListener { onRecord(false) }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = mBluetoothAdapter!!.bondedDevices
        address = pairedDevices.first { it.name == "ESP32test" }.address

        listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        listView.adapter = ListAdapter(valori, this)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun startRecording() {
        pin = 0
        maxi = 0
        vmax = 0.0
        timer = Timer()
        recorder = SoundMeter()
        recorder!!.start()
        mmDevice = mBluetoothAdapter?.getRemoteDevice(address)//MAC address del bluetooth di arduino
        try {
            mmSocket = mmDevice?.createRfcommSocketToServiceRecord(uuid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            // CONNETTE IL DISPOSITIVO TRAMITE IL SOCKET mmSocket
            mmSocket?.connect()
            outStream = mmSocket?.outputStream
            Toast.makeText(this, "ON", Toast.LENGTH_SHORT).show()//bluetooth è connesso
        } catch (e: Exception) {
            e.printStackTrace()
        }
        timer.scheduleAtFixedRate(0, 100) {
            progressBar.progress = ((recorder!!.amplitude / 32768) * 8).roundToInt()
            if (mmSocket!!.isConnected) {
                sendMessageBluetooth(((recorder!!.amplitude / 32768) * 8).roundToInt().toString())
            }

            pin = ((recorder!!.amplitude / 32768) * 8).roundToInt()
            if (pin > maxi) {
                maxi = pin
            }
        }
    }

    private fun stopRecording() {
        recorder?.let {
            timer.cancel()
            it.stop()
        }
        progressBar.progress = 0
        if (maxi > 0.0) {
            vmax = 20 * log10((maxi.toDouble() / 8) * 32768)
        } else vmax = 45.0

        valori.add(0, Pair(vmax.toInt(), maxi))
        listView.adapter!!.notifyDataSetChanged()
        try {
            mmSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "connessione non rieuscita", Toast.LENGTH_SHORT).show()
        }
        recorder = null

    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun sendMessageBluetooth(message: String) {
        if (outStream == null) {
            error("ciao")
        }
        val msgBuffer = message.toByteArray()
        try {
            outStream!!.write(msgBuffer)
        } catch (e: IOException) {
            Toast.makeText(this@MainActivity, "Messaggio non Inviato", Toast.LENGTH_SHORT).show()
        }

    }
}






