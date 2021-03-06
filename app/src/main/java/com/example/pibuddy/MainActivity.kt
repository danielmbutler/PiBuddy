package com.example.pibuddy


import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import isPortOpen
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.result.*
import kotlinx.coroutines.*
import org.apache.commons.net.util.SubnetUtils
import org.json.JSONObject



class MainActivity : AppCompatActivity() {

    fun nullcheck(): String {
        if (IPAddressText.text.isEmpty()) {

            return "Missing IP"
        }
        if (UsernameText.text.isEmpty() ){

            return "Missing Username"
        }
        if (PasswordText.text.isEmpty()){

            return "Missing Password"
        }

        return "success"

    }

    private suspend fun NetworkScanIP(): Array<String> {
        //ping scan test

        val utils = SubnetUtils("192.168.1.0/24")
        val allIps: Array<String> = utils.getInfo().getAllAddresses()
//appIps will contain all the ip address in the subnet
        return allIps

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch (Dispatchers.IO) {
            val netAddresses =  async { NetworkScanIP() }
            var addresscount = netAddresses.await().count()

                netAddresses.await().forEach {
                    val pingtest = async{isPortOpen(it.toString(),22,3000)}
                    Log.d("pingtest",it.toString() + " " + pingtest.await())
                    addresscount --
                    Log.d("IPCount", (addresscount).toString())
            }

        }


        // slider

        var drawer: DrawerLayout? = null

            var toolbarid = toolbar.id

            setSupportActionBar(toolbar)
            drawer = findViewById(R.id.drawer_layout)
            val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            )
            drawer!!.addDrawerListener(toggle)
            toggle.syncState()

        val mNavigationView = findViewById<View>(R.id.nav_viewer) as NavigationView
        mNavigationView.bringToFront();







        val pref = applicationContext.getSharedPreferences(
            "Connection",
            0
        ) // 0 - for private mode



//        //check for cached successful values
//       val savedIp       = pref.getString("IPAddress", null)
//        val savedUser     = pref.getString("Username", null)
//        val savedPassword = pref.getString("Password", null)
//
//        //get all preferences
//
              val keys: Map<String, *> = pref.getAll()
//
       for ((key, value) in keys) {
           Log.d("map values", key + ": " + value.toString())
           val testnavView = findViewById(R.id.nav_viewer) as NavigationView
           val menu = testnavView.menu
           menu.add(0,0,0,"$key").setOnMenuItemClickListener {

                   Log.d("onclick listner", key)

                   pref.getString(this.title.toString(), null)
                   var strJson = pref.getString(key, null)

                   val jresponse = JSONObject(strJson)
                   val UsernameFromJson = jresponse.getString("Username")
                   val PasswordFromJson = jresponse.getString("Password")

                   if (strJson != null) {
                       Log.d("onclick listner", strJson)
                       Log.d("onclick listner", "Username: ${UsernameFromJson}, Password: ${PasswordFromJson} ")
                       IPAddressText.setText(key)
                       UsernameText.setText(UsernameFromJson)
                       PasswordText.setText(PasswordFromJson)
                   }


                   drawer.closeDrawer(GravityCompat.START);
                   true
               }.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_computer))

           }


            ConnectButton.setOnClickListener {
            ConnectButton.text = "Connect"
            var validationtest = nullcheck()

            Log.d("Nullcheck", validationtest + IPAddressText.text)

            if (validationtest == "success"){

                GlobalScope.launch(Dispatchers.IO) {

                    //pingtest
                    val pingtest = async{isPortOpen(IPAddressText.text.toString(),22,3000)}
                    Log.d("pingtest",pingtest.await())

                    if (pingtest.await() == "false"){
                        withContext(Dispatchers.Main) {
                            ConnectButton.text = "Connection failure"
                        }

                    } else {

                        val LoggedInUsers = async {
                            executeRemoteCommand(
                                UsernameText.text,
                                PasswordText.text,
                                IPAddressText.text, "who | cut -d' ' -f1 | sort | uniq\n"
                            )
                        }

                        val DiskSpace = async {
                            executeRemoteCommand(
                                UsernameText.text,
                                PasswordText.text,
                                IPAddressText.text, "df -hl | grep \'root\' | awk \'BEGIN{print \"\"} {percent+=$5;} END{print percent}\' | column -t"
                            )
                        }
                        val MemUsage = async {
                            executeRemoteCommand(
                                UsernameText.text,
                                PasswordText.text,
                                IPAddressText.text, "awk '/^Mem/ {printf(\"%u%%\", 100*\$3/\$2);}' <(free -m)"
                            )
                        }
                        val CpuUsage = async {
                            executeRemoteCommand(
                                UsernameText.text,
                                PasswordText.text,
                                IPAddressText.text, "mpstat | grep -A 5 \"%idle\" | tail -n 1 | awk -F \" \" '{print 100 -  \$ 12}'a"
                            )
                        }

                        val results     = LoggedInUsers.await()
                        val diskspace   = DiskSpace.await()
                        val memusage    = MemUsage.await()
                        val cpuusage     = CpuUsage.await()
                        withContext(Dispatchers.Main) {
                            setContentView(R.layout.result)


                            findViewById<View>(R.id.text_dot_loader).visibility =
                                View.VISIBLE

                            // use GetData to check for any saved data in AWS For this PI
                            var res =  async { getData(applicationContext,APIConfig().Lambda.toString(),IPAddressText.text.toString()) }

                            LoggedInUsersTextView.text  = results
                            DiskSpaceTextView.text      = (diskspace.replace("[^0-9a-zA-Z:,]+".toRegex(), "") + "%" + " used") //replace all special charaters due to phantom space
                            CPUusageTextView.text       = cpuusage
                            MemUsageTextView.text       = memusage
                            DiskSpaceTextView.setMovementMethod(ScrollingMovementMethod());

                            //null titles
                            editTextTextPersonName4.keyListener = null
                            editTextTextPersonName3.keyListener = null
                            editTextTextPersonName2.keyListener = null
                            editTextTextPersonName.keyListener = null
                            ResultsTitle.keyListener = null






                            var datares = arrayOf(res.await())
                            var text = ""

                            for(element in datares){
                                text += element
                            }
                            AwsResultTextView.text = Html.fromHtml(text)
                            println(AwsResultTextView.text)
                            AwsResultTextView.setMovementMethod(ScrollingMovementMethod())

                            findViewById<View>(R.id.text_dot_loader).visibility =
                                View.GONE

                            // store successfull connection in shared pref


                            val editor = pref.edit()


                            val Pidata = JSONObject("""{"Username":"${UsernameText.text.toString()}", "Password":"${PasswordText.text.toString()}"}""")
                            editor.putString(IPAddressText.text.toString(), Pidata.toString())



                            editor.apply()




                            BackButton.setOnClickListener {
                                recreate()
                                setContentView(R.layout.activity_main)

                            }
                        }

                    }


                }
            } else {
                ConnectButton.text = validationtest + "...Please try again"
            }


        }
    }



}






