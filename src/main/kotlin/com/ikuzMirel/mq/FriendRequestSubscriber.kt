package com.ikuzMirel.mq

import com.ikuzMirel.websocket.WebSocketConnection
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class FriendRequestSubscriber(
    private val connections: ConcurrentHashMap<String, WebSocketConnection>
) : Subscriber {

    override fun isInterestedIn(message: Message): Boolean {
        return message is Message.FriendRequestMessage
    }

    override fun receive(message: Message) {
        val message = message as Message.FriendRequestMessage
        runBlocking {
            if (connections[message.targetUserId]?.socket != null) {
                connections[message.targetUserId]?.socket?.send(Frame.Text(message.data))
            }
        }
    }
}