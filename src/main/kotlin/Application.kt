package org

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.Data.DatabaseManager
import org.Plugins.UserRoutes
import org.Routes.ClassRoutes
import org.Routes.GradeRoutes
import org.Routes.LegalGuardianRoutes
import org.Routes.RoleRoutes
import org.Routes.StudentHasClassRoutes
import org.Routes.StudentRoutes
import org.Routes.TeacherRoutes
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    val database = DatabaseManager()

    Database.connect(
        url = dbCon,
        driver = "com.mysql.cj.jdbc.Driver",
        user = dbUsername,
        password = dbPassword
    )

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                val id = credential.payload.getClaim("Id").asInt()

                if (id != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or expired"))
            }
        }
    }

    routing {
        UserRoutes()
        RoleRoutes()
        StudentRoutes()
        LegalGuardianRoutes()
        GradeRoutes()
        ClassRoutes()
        TeacherRoutes()
        StudentHasClassRoutes()

    }

}
