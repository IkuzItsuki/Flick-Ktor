package com.ikuzMirel.di

import com.ikuzMirel.WebSocket.WSController
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
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mongoPW = System.getenv("MONGO_PW")
val authDBName = "auths"
val userDBName = "users"
val friendReqDBName = "friendRequests"
val massageDBName = "messages"

val mainModule = module {

    fun getDB(dbName: String): CoroutineDatabase {
        return KMongo.createClient(
            connectionString = "mongodb+srv://ikuzMirel:$mongoPW@cluster1.cailxou.mongodb.net/$dbName?retryWrites=true&w=majority"
        )
            .coroutine
            .getDatabase(dbName)
    }

    single<AuthSource> {
        AuthSourceImpl(getDB(authDBName))
    }
    single<UserDataSource> {
        UserDataSourceImpl(getDB(userDBName))
    }
    single<FriendDataSource> {
        FriendDataSourceImpl(getDB(userDBName))
    }
    single<FriendRequestDataSource> {
        FriendRequestDataSourceImpl(getDB(friendReqDBName))
    }
    single<MessageDataSource> {
        MessageDataSourceImpl(getDB(massageDBName))
    }
    single {
        WSController(get(), get())
    }
}