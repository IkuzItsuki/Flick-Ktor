package com.ikuzMirel.di

import com.ikuzMirel.data.auth.AuthSource
import com.ikuzMirel.data.auth.AuthSourceImpl
import com.ikuzMirel.data.chatMessage.ChatMessageDataSource
import com.ikuzMirel.data.chatMessage.ChatMessageDataSourceImpl
import com.ikuzMirel.data.friends.FriendDataSource
import com.ikuzMirel.data.friends.FriendDataSourceImpl
import com.ikuzMirel.data.friends.FriendRequestDataSource
import com.ikuzMirel.data.friends.FriendRequestDataSourceImpl
import com.ikuzMirel.data.user.UserDataSource
import com.ikuzMirel.data.user.UserDataSourceImpl
import com.ikuzMirel.initializer.DatabaseInitializer
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.*
import org.koin.dsl.module

const val authDBName = "auths"
const val userDataDBName = "userData"
const val massageDBName = "messages"

fun provideDBModule(environment: ApplicationEnvironment) = module {
    val mongoUser: String = System.getenv("MONGO_USER") ?: ""
    val mongoPW: String = System.getenv("MONGO_PW") ?: ""
    val mongoUri = environment.config.property("mongo.uri").getString().let {
        if (mongoUser.isNotEmpty() && mongoPW.isNotEmpty()) {
            it.replace("<username>:<password>", "$mongoUser:$mongoPW")
        } else if (mongoPW.isNotEmpty()) {
            it.replace("<password>", mongoPW)
        } else {
            it
        }
    }
    val client = MongoClient.create(mongoUri)

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
    single<ChatMessageDataSource> {
        ChatMessageDataSourceImpl(getDB(massageDBName))
    }
}