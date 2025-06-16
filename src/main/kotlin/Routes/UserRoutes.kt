package org.Plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.Objects.User
import org.jwtAudience
import org.jwtIssuer
import org.jwtSecret
import kotlinx.serialization.Serializable
import org.Objects.Role
import org.Objects.Teacher
import org.Routes.ClassUpdateResponse
import java.util.Date

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String? = null, val error: String? = null)

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String, val confirmPassword: String)

@Serializable
data class RegisterResponse(val success: Boolean, val error: String? = null)

@Serializable
data class UserDeleteRequest(val id: Int)

@Serializable
data class UserDeleteResponse(val success: Boolean, val error: String? = null)

@Serializable
data class UserGetAllResponse(val id: Int, val username: String, val email: String, val realId: Int?, val roleId: Int)

@Serializable
data class UserGetRequest(val id: Int)

@Serializable
data class UserGetResponse(val user: UserData? = null, val error: String? = null)

@Serializable
data class UserUpdateRoleAndRealIdRequest(val id: Int, val realId: Int?, val roleId: Int)

@Serializable
data class UserUpdateRoleAndRealIdResponse(val success: Boolean, val error: String? = null)

@Serializable
data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val realId: Int?
)

fun Route.UserRoutes() {

    post("/login") {

        val request = call.receive<LoginRequest>()

        val userInput = User().apply {
            email = request.email
            password = request.password
        }

        val loggedInUser = userInput.login()

        if (loggedInUser == null) {
            call.respond(
                LoginResponse(token = null, error = "Invalid email or password")
            )
            return@post
        }

        val token = JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("Id", loggedInUser.id)
            .withIssuedAt(Date())
            .sign(Algorithm.HMAC256(jwtSecret))

        call.respond(
            LoginResponse(token = token)
        )

    }

    post("/register") {

        val request = call.receive<RegisterRequest>()

        val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        if (!emailRegex.matches(request.email)) {
            call.respond(
                RegisterResponse(success = false, error = "Invalid email format")
            )
            return@post
        }

        if (request.password != request.confirmPassword) {
            call.respond(
                RegisterResponse(success = false, error = "Passwords do not match")
            )
            return@post
        }

        val user = User().apply {
            username = request.username
            email = request.email
            password = request.password
        }

        if(user.emailExists()){
            call.respond(
                RegisterResponse(success = false, error = "Email already exists")
            )
        } else {
            user.register()
            call.respond(
                RegisterResponse(success = true)
            )
        }

    }

    authenticate("auth-jwt") {

        post("/user/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(UserDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<UserDeleteRequest>()
            val userToDelete = User().apply { id = request.id }

            val userRole = userToDelete.getRoleFromId()
            if (userRole == "admin") {
                call.respond(UserDeleteResponse(success = false, error = "Cannot delete another admin"))
                return@post
            }

            userToDelete.delete()
            call.respond(UserDeleteResponse(success = true, error = "User deleted"))
        }

        get("/user/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val users = User().getAll()
            val response = users.map { user ->
                UserGetAllResponse(
                    id = user.id!!,
                    username = user.username,
                    email = user.email,
                    realId = user.realId,
                    roleId = user.roleId
                )
            }

            call.respond(response)
        }

        post("/user/get") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()
            val request = call.receive<UserGetRequest>()

            if (requesterRole != "admin" && requesterId != request.id) {
                call.respond(UserGetResponse(user = null, error = "Not enough privileges"))
                return@post
            }

            val targetUser = User().apply { id = request.id }

            val foundUser = targetUser.getFromId()

            if (foundUser == null) {
                call.respond(UserGetResponse(user = null, error = "User not found"))
                return@post
            }

            val role = foundUser.getRoleFromId() ?: "unknown"

            val userData = UserData(
                id = foundUser.id!!,
                username = foundUser.username,
                email = foundUser.email,
                role = role,
                realId = foundUser.realId
            )

            call.respond(UserGetResponse(user = userData, error = null))
        }

        post("/user/updateRoleAndRealId") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(UserUpdateRoleAndRealIdResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<UserUpdateRoleAndRealIdRequest>()

            val roleIdExists = Role().apply { id = request.roleId }.getFromId()
            if (roleIdExists == null) {
                call.respond(ClassUpdateResponse(success = false, error = "roleId does not exist"))
                return@post
            }

            val userToUpdate = User().apply {
                id = request.id
                roleId = request.roleId
                realId = request.realId
            }

            val userRole = userToUpdate.getRoleFromId()
            if (userRole == "admin") {
                call.respond(UserUpdateRoleAndRealIdResponse(success = false, error = "Cannot update another admin"))
                return@post
            }

            userToUpdate.updateRoleAndRealId()
            call.respond(UserUpdateRoleAndRealIdResponse(success = true, error = "User role updated"))
        }

    }

}