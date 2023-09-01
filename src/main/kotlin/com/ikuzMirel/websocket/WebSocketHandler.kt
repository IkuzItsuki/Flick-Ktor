package com.ikuzMirel.websocket

import com.ikuzMirel.data.chatMessage.ChatMessage
import com.ikuzMirel.data.chatMessage.ChatMessageDataSource
import com.ikuzMirel.data.chatMessage.ChatMessageRequest
import com.ikuzMirel.data.chatMessage.toChatMessageWithCid
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.exception.WSUserAlreadyExistsException
import com.ikuzMirel.mq.Message
import com.ikuzMirel.mq.Publisher
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId
import java.util.concurrent.ConcurrentHashMap

class WebSocketHandler(
    private val chatMessageDataSource: ChatMessageDataSource,
    private val userDataSource: UserDataSource,
    private val friendDataSource: FriendDataSource,
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
        println("message: $message")

        val websocketMessage = Json.decodeFromString(WebSocketMessage.serializer(), message)

        when (websocketMessage.type) {
            "chatMessageRequest" -> {
                handleChatMessage(userId, websocketMessage.data as ChatMessageRequest)
            }

            "lastReadMessage" -> {
                handleLastReadMessage(userId, websocketMessage.data as LastReadMessageSet)
            }
        }
    }

    private suspend fun handleChatMessage(userId: String, data: ChatMessageRequest) {
        val chatMessageEntity = ChatMessage(
            content = data.message,
            senderUid = userId,
            timestamp = System.currentTimeMillis(),
            _id = ObjectId(data.id)
        )
        val writeSuccess = chatMessageDataSource.insertMessage(data.collectionId, chatMessageEntity)

        val chatMessageWithCid = chatMessageEntity.toChatMessageWithCid(data.collectionId)

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
                receiverId = data.receiverId,
                data = parsedMessage
            )
        )
    }

    private suspend fun handleLastReadMessage(userId: String, data: LastReadMessageSet) {
        data.lastReadMessageList.forEach {
            friendDataSource.updateLastReadMessageTime(userId, it.friendUserId, it.lastReadMessageTime)
        }
    }

    suspend fun tryDisconnect(userId: String) {
        connections[userId]?.socket?.close()
        connections.remove(userId)
    }
}