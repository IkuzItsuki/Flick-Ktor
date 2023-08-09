package com.ikuzMirel.di

import com.ikuzMirel.data.auth.AuthSource
import com.ikuzMirel.data.auth.AuthSourceImpl
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.friends.FriendDataSourceImpl
import com.ikuzMirel.data.friends.FriendRequestDataSource
import com.ikuzMirel.data.friends.FriendRequestDataSourceImpl
import com.ikuzMirel.data.message.MessageDataSource
import com.ikuzMirel.data.message.MessageDataSourceImpl
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.data.user.UserDataSourceImpl
import com.ikuzMirel.initializer.DatabaseInitializer
import com.ikuzMirel.websocket.WebSocketHandler
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.koin.dsl.module

val mongoPW: String = System.getenv("MONGO_PW")
const val authDBName = "auths"
const val userDataDBName = "userData"
const val massageDBName = "messages"
val client =
    MongoClient.create("mongodb+srv://ikuzMirel:$mongoPW@cluster1.cailxou.mongodb.net/?retryWrites=true&w=majority")

val mainModule = module {

    fun getDB(dbName: String): MongoDatabase {
        return client.getDatabase(dbName)
    }

    single<DatabaseInitializer> {
        DatabaseInitializer(client)
    }
    single<AuthSource> {
        AuthSourceImpl(getDB(authDBName))
    }
    single<UserDataSource> {
        UserDataSourceImpl(getDB(userDataDBName))
    }
    single<FriendDataSource> {
        FriendDataSourceImpl(getDB(userDataDBName))
    }
    single<FriendRequestDataSource> {
        FriendRequestDataSourceImpl(getDB(userDataDBName))
    }
    single<MessageDataSource> {
        MessageDataSourceImpl(getDB(massageDBName))
    }
    single {
        WebSocketHandler(get(), get())
    }
}