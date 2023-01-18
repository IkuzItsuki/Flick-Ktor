package com.ikuzMirel

import com.ikuzMirel.data.user.MongoUserDataSouce
import io.ktor.server.application.*
import com.ikuzMirel.plugins.*
import com.ikuzMirel.security.hashing.SHA256HashingService
import com.ikuzMirel.security.token.JwtTokenService
import com.ikuzMirel.security.token.TokenConfig
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val mongoPW = System.getenv("MONGO_PW")
    val dbName = "ktor-auth"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://ikuzMirel:$mongoPW@cluster0.ua4o7tr.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)
    val userDataSouce = MongoUserDataSouce(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 1000L * 60L * 60L * 24L * 365L,
        secret = System.getenv("JWT_SECRET")
    )

    val hashingService = SHA256HashingService()

    configureSecurity(tokenConfig)
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureRouting(userDataSouce, hashingService, tokenConfig, tokenService)
}
