package com.example.pibuddy

import PiAdapter
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pibuddy.utilities.isPortOpen
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_scan_.*
import kotlinx.coroutines.*
import org.apache.commons.net.util.SubnetUtils
import java.math.BigInteger



class Scan_Activity : AppCompatActivity() {
    companion object{
        val TAG = "Scan_Activity"
    }

    private var items: List<String> = ArrayList()

    private lateinit var PiAdapter: PiAdapter

    private suspend fun NetworkScanIP(): Array<String> {

        //get IP

        val wm = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress: String = BigInteger.valueOf(wm.dhcpInfo.netmask.toLong()).toString()
        Log.d(TAG,ipAddress)

        //ping scan test

        val utils = SubnetUtils("192.168.1.0/24")
        val allIps: Array<String> = utils.getInfo().getAllAddresses()
//appIps will contain all the ip address in the subnet
        return allIps

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_)


        GlobalScope.launch(Dispatchers.IO) {
            val netAddresses = async { NetworkScanIP() }
            var addresscount = netAddresses.await().count()

            withContext(Dispatchers.Main){

            Scan_View_text_dot_loader.visibility = VISIBLE
            Scanning_Text_View.visibility = VISIBLE

                Scan_View_RecyclerView.adapter = adapter

                // faint line underneath each row
                Scan_View_RecyclerView.addItemDecoration(DividerItemDecoration(this@Scan_Activity, DividerItemDecoration.VERTICAL))

                //set item click listener for recyclerview

                refreshRecyclerViewMessages()

            }

            netAddresses.await().forEach {
                val pingtest = async {
                    isPortOpen(
                        it.toString(),
                        22,
                        1000
                    )
                }
                Log.d("pingtest", it.toString() + " " + pingtest.await())
                addresscount--
                Log.d("IPCount", (addresscount).toString())

                if(pingtest.await() == "false" ){
                    Log.d(TAG, "${it.toString()} + is available")
                    withContext(Dispatchers.Main){



                        IPs.add(it)
                        refreshRecyclerViewMessages()
                    }
                }

                withContext(Dispatchers.Main){

                    val addtext = "Scanning for Devices with port 22 open ...... $addresscount addresses remaining"
                    Scanning_Text_View.setText(addtext)

                }
            }
        }
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    val IPs: MutableList<String> = mutableListOf()

    private fun refreshRecyclerViewMessages(){
        println("RecyclerviewRefresh called + $IPs")
        adapter.clear()
        IPs.forEach {
            adapter.add(PiAdapter(it))

        }
    }


}