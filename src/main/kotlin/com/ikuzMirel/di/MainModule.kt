package com.ikuzMirel.di

import com.ikuzMirel.mq.Channel
import com.ikuzMirel.mq.ChatPublisher
import com.ikuzMirel.mq.ChatSubscriber
import com.ikuzMirel.websocket.WebSocketConnection
import com.ikuzMirel.websocket.WebSocketHandler
import org.koin.dsl.module
import java.util.concurrent.ConcurrentHashMap


val connections = ConcurrentHashMap<String, WebSocketConnection>()

val mainModule = module {
    single {
        connections
    }
    single {
        ChatPublisher()
    }
    single {
        Channel("chat")
    }
    single {
        ChatSubscriber(connections)
    }
    single {
        WebSocketHandler(get(), get(), get(), get(), connections)
    }
}