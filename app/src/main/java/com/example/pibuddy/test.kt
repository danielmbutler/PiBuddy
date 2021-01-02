package com.example.pibuddy

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.ByteArrayOutputStream


@Throws(Exception::class)
fun listFolderStructure(
    username: String?, password: String?,
    host: String?, port: Int, command: String?
) {
    var session: Session? = null
    var channel: ChannelExec? = null
    try {
        session = JSch().getSession(username, host, port)
        session.setPassword(password)
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()
        channel = session.openChannel("exec") as ChannelExec?
        channel!!.setCommand(command)
        val responseStream = ByteArrayOutputStream()
        channel.outputStream = responseStream
        channel.connect()
        while (channel.isConnected) {
            Thread.sleep(100)
        }
        val responseString = String(responseStream.toByteArray())
        println(responseString)
    } finally {
        if (session != null) {
            session.disconnect()
        }
        channel?.disconnect()
    }
}