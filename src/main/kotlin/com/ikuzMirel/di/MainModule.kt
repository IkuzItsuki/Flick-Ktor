package com.ikuzMirel.di

import com.ikuzMirel.mq.Channel
import com.ikuzMirel.mq.ChatSubscriber
import com.ikuzMirel.mq.FriendRequestSubscriber
import com.ikuzMirel.mq.Publisher
import com.ikuzMirel.websocket.WebSocketConnection
import com.ikuzMirel.websocket.WebSocketHandler
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.ConcurrentHashMap


val connections = ConcurrentHashMap<String, WebSocketConnection>()

val mainModule = module {
    single {
        connections
    }
    single(named("chatPublisher")) {
        Publisher()
    }
    single(named("friendRequestPublisher")) {
        Publisher()
    }
    single(named("chatChannel")) {
        Channel("chat")
    }
    single(named("friendRequestChannel")) {
        Channel("friendRequest")
    }
    single(named("chatSubscriber")) {
        ChatSubscriber(connections)
    }
    single(named("friendRequestSubscriber")) {
        FriendRequestSubscriber(connections)
    }
    single {
        WebSocketHandler(get(), get(), get(named("chatPublisher")), connections)
    }
}