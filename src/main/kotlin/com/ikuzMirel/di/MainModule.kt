package com.ikuzMirel.di

import com.ikuzMirel.data.MessageDataSource
import com.ikuzMirel.data.MessageDataSourceImpl
import com.ikuzMirel.room.RoomController
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mongoPW = System.getenv("MONGO_PW")
val dbName = "message_db_yt"

val mainModule = module {
    single {
        KMongo.createClient(
            connectionString = "mongodb+srv://ikuzMirel:$mongoPW@cluster0.ua4o7tr.mongodb.net/$dbName?retryWrites=true&w=majority"
        )
            .coroutine
            .getDatabase(dbName)
    }
    single<MessageDataSource> {
        MessageDataSourceImpl(get())
    }
    single {
        RoomController(get())
    }
}