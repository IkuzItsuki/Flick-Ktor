package com.ikuzMirel.mq

class Channel(
    private val channelId: String
) {

    private val subscribers: HashSet<Subscriber> = HashSet()
    fun publish(message: Message) {
        subscribers
            .filter { subscriber -> subscriber.isInterestedIn(message) }
            .forEach { subscriber -> subscriber.receive(message) }
    }

    fun subscribeFrom(subscriber: Subscriber) {
        subscribers.add(subscriber)
    }
}