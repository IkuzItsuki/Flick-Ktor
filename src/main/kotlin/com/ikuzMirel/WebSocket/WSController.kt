package com.ikuzMirel.WebSocket

import com.ikuzMirel.data.message.Message
import com.ikuzMirel.data.message.MessageDataSource
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.exception.WSUserAlreadyExistsException
import com.ikuzMirel.extension.removeQuotes
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.util.concurrent.ConcurrentHashMap

class WSController(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource
) {

    val connections = ConcurrentHashMap<String, WSUser>()

    suspend fun onConnect(
        userId: String,
        sessionId: String,
        socket: WebSocketSession
    ) {
        if (connections[userId] != null) {
            throw WSUserAlreadyExistsException()
        }
        val user = userDataSource.getUserById(userId)
        connections[userId] = WSUser(
            uid = userId,
            username = user!!.username,
            sessionId = sessionId,
            socket = socket
        )
    }

    suspend fun sendMessage(userId: String, message: String) {
        val username = userDataSource.getUserById(userId)!!.username
        val jsonObject = Json.parseToJsonElement(message).jsonObject
        val msg = jsonObject["message"].removeQuotes()
        val collectionId = jsonObject["cid"].removeQuotes()
        val receiverId = jsonObject["receiverId"].removeQuotes()

        val messageEntity = Message(
            content = msg,
            senderUsername = username,
            senderUid = userId,
            timestamp = System.currentTimeMillis()
        )
        val sendMsg = messageDataSource.insertMessage(collectionId, messageEntity)

        if (!sendMsg) {
            connections[userId]?.socket?.send(Frame.Text("Message not sent"))
            return
        }

        val parsedMessage = Json.encodeToString(messageEntity)
        connections[userId]?.socket?.send(Frame.Text(parsedMessage))
        if (connections[receiverId] != null) {
            connections[receiverId]?.socket?.send(Frame.Text(parsedMessage))
        }
    }

    suspend fun getAllMessages(collectionId: String): List<Message> {
        return messageDataSource.getAllMessages(collectionId)
    }

    suspend fun tryDisconnect(userId: String) {
        connections[userId]?.socket?.close()
        if (connections.contains(userId)) {
            connections.remove(userId)
        }
    }
}