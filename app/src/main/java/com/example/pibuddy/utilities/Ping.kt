package com.example.pibuddy.utilities

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException


fun isPortOpen(ip : String, port : Int, timeout : Int): String {

    try {
        val socket = Socket();
        socket.connect(InetSocketAddress(ip, port), timeout);

        socket.close();
        return "connection successfull";

    }

    catch(ce: ConnectException){
        ce.printStackTrace();
        return "false"
    }

    catch(ce: SocketTimeoutException){

        return "false"
    }

    catch (ex: Exception ) {
        ex.printStackTrace();
        return "false"
    }
}


