package com.example.pibuddy

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.*
import org.json.JSONObject
import java.io.StringReader
import java.util.logging.Level.parse
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine



suspend fun getData(context: Context, endpoint: String , ip: String) = suspendCoroutine<String> { cont ->
    val queue = Volley.newRequestQueue(context)
    println("run")
    val jsonobj = JSONObject()
    jsonobj.put("IPAddress","${ip}")
    jsonobj.put("Client","Android")

    val req = JsonObjectRequest(Request.Method.POST,endpoint,jsonobj,

            Response.Listener {
                response ->
                Log.d("Lambda","Response is: ${response}")

                var trimmedres = ""

                val pathMatcher = object : PathMatcher {
                    override fun pathMatches(path: String) = Pattern.matches(".*res.*", path)

                    override fun onMatch(path: String, value: Any) {
                        trimmedres += ("${path.replace(".res","")} : $value")
                    }
                }

                val regex = "\\[(.*?)]" //remove square brackets and anything inside [*]



                Klaxon()
                    .pathMatcher(pathMatcher)
                    .parseJsonObject(StringReader(response.toString()))


                cont.resume(trimmedres.replace("\\[(.*?)]".toRegex(), "").replace("$.",", ").replace("Z,","</p>").replace("T"," ").replace("IP","<p>IP"))



            }, Response.ErrorListener { cont.resume("Something went wrong!") })

    queue.add(req)
}