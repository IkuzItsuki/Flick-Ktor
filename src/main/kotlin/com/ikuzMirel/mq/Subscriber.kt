package com.ikuzMirel.mq

interface Subscriber {

    fun isInterestedIn(message: Message): Boolean

    fun receive(message: Message)

    fun subscribeTo(channel: Channel) {
        channel.subscribeFrom(this)
    }
}