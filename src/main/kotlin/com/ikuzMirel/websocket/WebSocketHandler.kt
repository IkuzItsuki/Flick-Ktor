package com.ikuzMirel.websocket

import com.ikuzMirel.data.chatMessage.ChatMessage
import com.ikuzMirel.data.chatMessage.ChatMessageDataSource
import com.ikuzMirel.data.chatMessage.ChatMessageWithCid
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.exception.WSUserAlreadyExistsException
import com.ikuzMirel.extension.removeQuotes
import com.ikuzMirel.mq.Message
import com.ikuzMirel.mq.Publisher
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.bson.types.ObjectId
import java.util.concurrent.ConcurrentHashMap

class WebSocketHandler(
    private val chatMessageDataSource: ChatMessageDataSource,
    private val userDataSource: UserDataSource,
    private val chatPublisher: Publisher,
    private val connections: ConcurrentHashMap<String, WebSocketConnection>
) {


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

        val chatMessageEntity = ChatMessage(
            content = msg,
            senderUid = userId,
            timestamp = System.currentTimeMillis(),
            _id = ObjectId(id)
        )
        val writeSuccess = chatMessageDataSource.insertMessage(collectionId, chatMessageEntity)

        val chatMessageWithCid = ChatMessageWithCid(
            content = chatMessageEntity.content,
            senderUid = userId,
            timestamp = chatMessageEntity.timestamp,
            id = id,
            collectionId = collectionId
        )

        val webSocketMessage = WebSocketMessage(
            type = "chatMessage",
            data = chatMessageWithCid
        )
        val parsedMessage = Json.encodeToString(webSocketMessage)

        if (!writeSuccess) {
            connections[userId]?.socket?.send(Frame.Text("Message not sent"))
            return
        }

        chatPublisher.publish(
            Message.ChatMessage(
                userId = userId,
                receiverId = receiverId,
                data = parsedMessage
            )
        )
    }

    suspend fun tryDisconnect(userId: String) {
        connections[userId]?.socket?.close()
        connections.remove(userId)
    }
}