<p align="center"><img src="docs/images/icon.png" width="200"></p>
<h1 align="center"><b>Flick-Ktor</b></h1>
<h3 align="center">A fully customizable backend server for Flick using Ktor.</h3>

## About

This is the backend server for [Flick](https://github.com/IkuzItsuki/FLICK) using [Ktor](https://ktor.io/).
It is a fully customizable server that can be used to host your own Flick server.

## Frameworks
- Database - [MongoDB](https://www.mongodb.com/)
- Network - [Ktor](https://ktor.io/)
- API - REST API
- Realtime connection - WebSocket

## API Documentation

This project uses [OpenAPI](https://swagger.io/specification/) to document the API.
The documentation can be found at `localhost:8080/swagger-ui/index.html` once the server is running.

## Building

### Requirements

- MongoDB Database (can be hosted on your own or use [MongoDB Atlas](https://www.mongodb.com/cloud/atlas))

### Steps

1. Clone the repository
2. Open the project in IntelliJ IDEA (or any other IDE, but the build process may differ)
3. Open the `application.conf` file and you will see the following:

```HOCON
ktor {
  deployment {
    port = 8080 // Change this to the port you want to run the server on
  }
  application {
    modules = [com.ikuzMirel.ApplicationKt.module]
  }
}
jwt {
  issuer = "https://example.com" // Change this to your domain
  domain = "https://example.com" // Change this to your domain
  audience = "users"
  realm = "Flick" // Change this to your app name
}
mongo {
  uri = "mongodb://<username>:<password>@localhost:27017" // Change this to your MongoDB Uri (*required)
}
```

4. Change the values to your own values (the ones with `*required` are required to be changed)
5. Edit the run configuration for Application.kt and add the following to the environment variables:

```
JWT_SECRET=<Your JWT secret>;MONGO_PW=<Your MongoDB Password>;MONGO_USER=<Your MongoDB Username>
```

> Tips: If you want to store your MongoDB credentials in environment variables, you have to replace the values
> in `application.conf` with `<username>` and `<password>` respectively. If you are not using MongoDB authentication, you
> can just ignore adding them.

6. Build and run the project

## Deployment

If you want to deploy it on a cloud server, you can follow this
guide: [Deploying Ktor](https://gist.github.com/philipplackner/bbb3581502b77edfd2b71b7e3f7b18bd). Some of the steps may
not be necessary, but it is a good guide to follow.