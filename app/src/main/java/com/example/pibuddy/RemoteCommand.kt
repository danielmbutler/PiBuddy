package com.example.pibuddy

import android.content.Context
import android.text.Editable
import android.util.Log
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.ByteArrayOutputStream
import java.util.*

suspend fun executeRemoteCommand(
    username: Editable,
    password: Editable,
    hostname: Editable, command: String,
    port: Int = 22): String {
    val jsch = JSch()
    val session = jsch.getSession(username.toString(), hostname.toString(), port)
    session.setPassword(password.toString())

    // Avoid asking for key confirmation.
    val properties = Properties()
    properties.put("StrictHostKeyChecking", "no")
    session.setConfig(properties)

    session.connect()

    // Create SSH Channel.
    val sshChannel = session.openChannel("exec") as ChannelExec
    val outputStream = ByteArrayOutputStream()
    sshChannel.outputStream = outputStream

    // Execute command.
    sshChannel.setCommand(command)
    sshChannel.connect()

    // Sleep needed in order to wait long enough to get result back.
    Thread.sleep(1_000)
    sshChannel.disconnect()

    session.disconnect()

    Log.d("PiMessage", outputStream.toString())



    return outputStream.toString()
}