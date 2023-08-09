package com.ikuzMirel.initializer

import com.mongodb.client.model.ClusteredIndexOptions
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.toList
import org.bson.Document

class DatabaseInitializer(
    private val client: MongoClient
) {
    suspend fun init() {
        val databaseNames = client.listDatabaseNames().toList()
        if (databaseNames.indexOf("auths") == -1) {
            val authDB = client.getDatabase("auths")

            if (authDB.listCollectionNames().toList().indexOf("auth") == -1) {
                authDB.createCollection(
                    "auth",
                    CreateCollectionOptions().clusteredIndexOptions(
                        ClusteredIndexOptions(Document("_id", 1), true)
                    )
                )
            }
        }

        if (databaseNames.indexOf("userData") == -1) {
            val userDataDB = client.getDatabase("userData")
            val collectionNames = userDataDB.listCollectionNames().toList()

            if (collectionNames.indexOf("user") == -1) {
                userDataDB.createCollection(
                    "user",
                    CreateCollectionOptions().clusteredIndexOptions(
                        ClusteredIndexOptions(Document("_id", 1), true)
                    )
                )
            }
            if (collectionNames.indexOf("friendRequest") == -1) {
                userDataDB.createCollection(
                    "friendRequest",
                    CreateCollectionOptions().clusteredIndexOptions(
                        ClusteredIndexOptions(Document("_id", 1), true)
                    )
                )
            }
        }
    }
}