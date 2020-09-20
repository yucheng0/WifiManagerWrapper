package com.imake.wifimanagerwrapper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiConnectivityCallbackResult
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiScanCallbackResult
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiManagerWrapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.wifi_recycle_view.*

var TAG = "myTag"
class MainActivity : AppCompatActivity(), WifiScanCallbackResult, WifiConnectivityCallbackResult {

    private lateinit var networkNameToConnect: String
    private lateinit var wifiScanResultList: List<ScanResult>
    private var wifiManagerWrapper: WifiManagerWrapper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//一定要加下面這一行, 這是關鍵性的一個動作
        //https://stackoverflow.com/questions/47480732/what-is-the-purpose-of-the-condition-if-build-version-sdk-int-build-version
     //M = android 6.0
        Log.d(TAG, "Build.VERSION.SDK_INT: ${Build.VERSION.SDK_INT} ")  // samsung s8 = api 28
        Log.d(TAG, "Build.VERSION_CODES.M:${Build.VERSION_CODES.M} ")   // api 23
        Log.d(TAG, "checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION): ${checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)}")
        Log.d(TAG, "PackageManager.PERMISSION_GRANTED: ${PackageManager.PERMISSION_GRANTED}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Open ACCESS_COARSE_LOCATION")
// 打開這個權限才能用, 這個也要在Manifest 定義才行
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)

            //do something if have the permissions
        } else {
            //do something, permission was previously granted; or legacy device
        //    scanWifi()
        }


        scanBtn.setOnClickListener {
            wifiManagerWrapper = WifiManagerWrapper()
            wifiManagerWrapper!!.wifiManagerInti(this).autoWifiScanner(this)
        }

        connectBtn.setOnClickListener(View.OnClickListener {
            networkNameToConnect = networkNameEt.text.toString()
            wifiManagerWrapper?.connectWifi(
                networkNameEt.text.toString(),
                networkPasswordEt.text.toString(),
                wifiManagerWrapper!!.WPA_WPA2_PSK,
                this
            )
        })

        forgetBtn.setOnClickListener(View.OnClickListener {
            if (wifiManagerWrapper != null)
                wifiManagerWrapper!!.forgetWifi(networkNameEt.text.toString(),this)
        })
    }

    override fun wifiFailureResult(results: MutableList<ScanResult>) {
        println("Wi-fi Failure Result*****************= $results")
        wifiScanResultList = emptyList()
        setRecycleViewAdapter(results)
    }

    override fun wifiSuccessResult(results: List<ScanResult>) {
        println("Wi-Fi Success Result******************= $results")
        wifiScanResultList = emptyList()
        wifiScanResultList = results
        //Check Available Devices
        checkDeviceConnected(wifiScanResultList)
        setRecycleViewAdapter(wifiScanResultList)
    }

    private fun setRecycleViewAdapter(
        arrayList: List<ScanResult>
    ) {
        // Creates a vertical Layout Manager
        recycleView.layoutManager = LinearLayoutManager(this)
        // Access the RecyclerView Adapter and load the data into it
        recycleView.adapter = WifiRcAdapter(arrayList)
        recycleView.animation
        initOnItemTouchListener()
    }

    private fun initOnItemTouchListener() {
        recycleView.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recycleView,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        networkNameEt.setText(wifiScanResultList[position].SSID.toString())
                    }

                    override fun onLongClick(view: View?, position: Int) {
                    }
                })
        )
    }

    override fun wifiConnectionStatusChangedResult() {
        println("************Connection Status Changed Result************")
        checkDeviceConnected(wifiScanResultList)
        setRecycleViewAdapter(wifiScanResultList)
    }

    private fun checkDeviceConnected(wifiScanResultListCheck: List<ScanResult>): Boolean? {
        for (index in wifiScanResultListCheck.indices) {
            return if (wifiManagerWrapper?.isConnectedTo(wifiScanResultListCheck[index].SSID)!!) {
                wifiScanResultList[index].capabilities = "Connected"
                println("Connected")
                true
            } else {
                wifiScanResultList[index].capabilities = "Connection not established"
                println("Connected not established")
                false
            }
        }
        return null
    }
}