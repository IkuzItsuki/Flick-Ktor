package com.ikuzMirel.serializer

import com.ikuzMirel.data.chatMessage.ChatMessageRequest
import com.ikuzMirel.data.chatMessage.ChatMessageWithCid
import com.ikuzMirel.data.friends.FriendRequest
import com.ikuzMirel.websocket.LastReadMessageSet
import com.ikuzMirel.websocket.WebSocketMessage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer

object WebSocketMsgSerializer : KSerializer<WebSocketMessage> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WebSocketMessage") {
        element("type", serialDescriptor<String>())
        element("data", buildClassSerialDescriptor("Any"))
    }

    @Suppress("UNCHECKED_CAST")
    private val typeSerializer: Map<String, KSerializer<Any>> = mapOf(
        "chatMessage" to serializer<ChatMessageWithCid>(),
        "chatMessageRequest" to serializer<ChatMessageRequest>(),
        "friendRequest" to serializer<FriendRequest>(),
        "lastReadMessage" to serializer<LastReadMessageSet>()
    ).mapValues { (_, v) -> v as KSerializer<Any> }

    private fun getDataSerializer(type: String): KSerializer<Any> {
        return typeSerializer[type] ?: throw SerializationException("Unknown type $type")
    }

    override fun serialize(encoder: Encoder, value: WebSocketMessage) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.type)
            encodeSerializableElement(descriptor, 1, getDataSerializer(value.type), value.data)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): WebSocketMessage = decoder.decodeStructure(descriptor) {
        if (decodeSequentially()) {
            val type = decodeStringElement(descriptor, 0)
            val data = decodeSerializableElement(descriptor, 1, getDataSerializer(type))
            WebSocketMessage(type, data)
        } else {
            require(decodeElementIndex(descriptor) == 0) { "Type field should be precede data field" }
            val type = decodeStringElement(descriptor, 0)
            val data = when (val index = decodeElementIndex(descriptor)) {
                1 -> decodeSerializableElement(descriptor, 1, getDataSerializer(type))
                CompositeDecoder.DECODE_DONE -> throw SerializationException("Data is missing")
                else -> error("Unexpected index $index")
            }
            WebSocketMessage(type, data)
        }
    }
}