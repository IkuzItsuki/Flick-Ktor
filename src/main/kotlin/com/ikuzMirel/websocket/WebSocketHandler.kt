package com.ikuzMirel.websocket

import com.ikuzMirel.data.message.Message
import com.ikuzMirel.data.message.MessageDataSource
import com.ikuzMirel.data.message.MessageWithCid
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.exception.WSUserAlreadyExistsException
import com.ikuzMirel.extension.removeQuotes
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.bson.types.ObjectId
import java.util.concurrent.ConcurrentHashMap

class WebSocketHandler(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource
) {

    val connections = ConcurrentHashMap<String, WebSocketConnection>()

    // TODO: WSUserAlreadyExistsException() will be removed to allow multiple connections like Web and Desktop
    suspend fun onConnect(
        userId: String,
        sessionId: String,
        socket: WebSocketSession
    ) {
        if (connections[userId] != null) {
            throw WSUserAlreadyExistsException()
        }
        val user = userDataSource.getUserById(userId) // TODO: Unsafe when user changes username
        connections[userId] = WebSocketConnection(
            uid = userId,
            username = user!!.username,
            sessionId = sessionId,
            socket = socket
        )
    }

    suspend fun sendMessage(userId: String, message: String) {
        val jsonObject = Json.parseToJsonElement(message).jsonObject
        val msg = jsonObject["message"].removeQuotes()
        val collectionId = jsonObject["cid"].removeQuotes()
        val receiverId = jsonObject["receiverId"].removeQuotes()
        val id = jsonObject["id"].removeQuotes()

        val timestamp = System.currentTimeMillis()
        println("timestamp: $timestamp")

        val messageEntity = Message(
            content = msg,
            senderUid = userId,
            timestamp = timestamp,
            _id = ObjectId(id)
        )
        val writeSuccess = messageDataSource.insertMessage(collectionId, messageEntity)

        val messageWithCid = MessageWithCid(
            content = messageEntity.content,
            senderUid = userId,
            timestamp = messageEntity.timestamp,
            id = id,
            collectionId = collectionId
        )

        val webSocketMessage = WebSocketMessage(
            type = "chatMessage",
            data = messageWithCid
        )
        val parsedMessage = Json.encodeToString(webSocketMessage)

        if (!writeSuccess) {
            connections[userId]?.socket?.send(Frame.Text("Message not sent"))
            return
        }

        connections[userId]?.socket?.send(Frame.Text(parsedMessage))
        println("Message sent to $userId")
        if (connections[receiverId]?.socket != null) {
            connections[receiverId]?.socket?.send(Frame.Text(parsedMessage))
            println("Message sent to $receiverId")
        }
    }

    suspend fun tryDisconnect(userId: String) {
        connections[userId]?.socket?.close()
        connections.remove(userId)
    }
}