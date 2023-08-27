package com.ikuzMirel.mq

class Publisher {

    private val channels: HashSet<Channel> = HashSet()

    fun publish(message: Message) {
        channels.forEach { channel -> channel.publish(message) }
    }

    fun register(channel: Channel) {
        channels.add(channel)
    }
}