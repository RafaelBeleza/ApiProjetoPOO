package org.Routes

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.Objects.Role
import org.Objects.User

@Serializable
data class RoleCreateRequest(val name: String)

@Serializable
data class RoleCreateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class RoleGetAllResponse(val id: Int, val name: String)

@Serializable
data class RoleGetRequest(val id: Int)

@Serializable
data class RoleGetResponse(val role: RoleData? = null, val error: String? = null)

@Serializable
data class RoleUpdateRequest(val id: Int, val name: String)

@Serializable
data class RoleUpdateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class RoleDeleteRequest(val id: Int)

@Serializable
data class RoleDeleteResponse(val success: Boolean, val error: String? = null)

@Serializable
data class RoleData(
    val id: Int,
    val name: String,
)

fun Route.RoleRoutes() {

    authenticate("auth-jwt") {

        post("/role/create") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(RoleCreateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<RoleCreateRequest>()
            val roleToCreate = Role().apply { name = request.name }

            roleToCreate.create()
            call.respond(RoleCreateResponse(success = true, error = "Role created"))
        }

        get("/role/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val roles = Role().getAll()
            val response = roles.map { role ->
                RoleGetAllResponse(
                    id = role.id!!,
                    name = role.name
                )
            }

            call.respond(response)
        }

        post("/role/get") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(RoleGetResponse(role = null, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<RoleGetRequest>()
            val roleToGet = Role().apply { id = request.id }

            val foundRole = roleToGet.getFromId()

            if (foundRole == null) {
                call.respond(RoleGetResponse(role = null, error = "Role not found"))
                return@post
            }

            val roleData = RoleData(
                id = foundRole.id!!,
                name = foundRole.name
            )

            call.respond(RoleGetResponse(role = roleData, error = null))
        }

        post("/role/update") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(RoleUpdateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<RoleUpdateRequest>()
            val role = Role().apply {
                name = request.name
            }

            role.update()

            call.respond(RoleUpdateResponse(success = true, error = null))
        }

        post("/role/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(RoleDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<RoleDeleteRequest>()
            val roleToDelete = Role().apply { id = request.id }

            roleToDelete.delete()
            call.respond(RoleDeleteResponse(success = true, error = "Role deleted"))
        }

    }

}