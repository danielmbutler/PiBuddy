package com.example.pibuddy


import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.result.*
import kotlinx.coroutines.*


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pref = applicationContext.getSharedPreferences(
            "Connection",
            0
        ) // 0 - for private mode

        //check for cached successful values
        val savedIp       = pref.getString("IPAddress", null)
        val savedUser     = pref.getString("Username", null)
        val savedPassword = pref.getString("Password", null)

        //get all preferences

        val keys: Map<String, *> = pref.getAll()

        for ((key, value) in keys) {
            Log.d("map values", key + ": " + value.toString())
        }


        IPAddressText.setText(savedIp)
        UsernameText.setText(savedUser)
        PasswordText.setText(savedPassword)

        ConnectButton.setOnClickListener {
            ConnectButton.text = "Connect"
            var validationtest = nullcheck()

            Log.d("Nullcheck", validationtest + IPAddressText.text)

            if (validationtest == "success"){

                GlobalScope.launch(Dispatchers.IO) {
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
                            IPAddressText.text, "df -hl | grep 'root' | awk 'BEGIN{print \"Size(GB)\",\"Use%\"} {size+=\$2;percent+=\$5;} END{print size,percent}' | column -t"
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
                        LoggedInUsersTextView.text  = results
                        DiskSpaceTextView.text      = diskspace
                        CPUusageTextView.text       = cpuusage
                        MemUsageTextView.text       = memusage
                        DiskSpaceTextView.setMovementMethod(ScrollingMovementMethod());


                        // store successfull connection in shared pref


                        val editor = pref.edit()

                        editor.putString("IPAddress",   IPAddressText.text.toString()); // Storing string
                        editor.putString("Username",    UsernameText.text.toString()); // Storing string
                        editor.putString("Password",    PasswordText.text.toString()); // Storing string

                        editor.apply()

                        BackButton.setOnClickListener {
                            recreate()
                            setContentView(R.layout.activity_main)
                        }
                    }


                }
            } else {
                ConnectButton.text = validationtest + "...Please try again"
            }


        }
    }
}




