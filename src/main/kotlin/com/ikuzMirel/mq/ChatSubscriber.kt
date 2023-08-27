package com.ikuzMirel.mq

import com.ikuzMirel.websocket.WebSocketConnection
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class ChatSubscriber(
    private val connections: ConcurrentHashMap<String, WebSocketConnection>
) : Subscriber {
    override fun isInterestedIn(message: Message): Boolean {
        return message is Message.ChatMessage
    }

    override fun receive(message: Message) {
        val message = message as Message.ChatMessage
        runBlocking {
            connections[message.userId]?.socket?.send(Frame.Text(message.data))
//            println("Message sent to ${message.userId}")
            if (connections[message.receiverId]?.socket != null) {
                connections[message.receiverId]?.socket?.send(Frame.Text(message.data))
//                println("Message sent to $message.receiverId")
            }
        }
    }
}