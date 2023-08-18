package com.ikuzMirel

import com.ikuzMirel.di.mainModule
import com.ikuzMirel.initializer.DatabaseInitializer
import com.ikuzMirel.plugins.*
import com.ikuzMirel.security.hashing.SHA256HashingService
import com.ikuzMirel.security.token.JwtTokenService
import com.ikuzMirel.security.token.TokenConfig
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)
@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(Koin){
        modules(mainModule)
    }

    val mongoClient by inject<DatabaseInitializer>()
    runBlocking {
        mongoClient.init()
    }

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
    configureRouting(hashingService, tokenConfig, tokenService)
    configureSwaggerUI()
    configureCORS()
}