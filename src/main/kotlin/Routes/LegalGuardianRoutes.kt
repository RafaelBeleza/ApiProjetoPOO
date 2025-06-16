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
import org.Data.LegalGuardianTable
import org.Objects.LegalGuardian
import org.Objects.Role
import org.Objects.Student
import org.Objects.User

@Serializable
data class LegalGuardianCreateRequest(val firstName: String, val lastName: String, val email: String?, val phone: String?)

@Serializable
data class LegalGuardianCreateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class LegalGuardianGetAllResponse(val id: Int, val firstName: String, val lastName: String, val email: String?, val phone: String?)

@Serializable
data class LegalGuardianGetRequest(val id: Int)

@Serializable
data class LegalGuardianGetResponse(val legalGuardian: LegalGuardianData? = null, val error: String? = null)

@Serializable
data class LegalGuardianUpdateRequest(val id: Int, val firstName: String, val lastName: String, val email: String?, val phone: String?)

@Serializable
data class LegalGuardianUpdateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class LegalGuardianDeleteRequest(val id: Int)

@Serializable
data class LegalGuardianDeleteResponse(val success: Boolean, val error: String? = null)

@Serializable
data class LegalGuardianData(
    var id: Int,
    var firstName: String,
    var lastName: String,
    var email: String?,
    var phone: String?
)

fun Route.LegalGuardianRoutes() {

    authenticate("auth-jwt") {

        post("/legalGuardian/create") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(LegalGuardianCreateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<LegalGuardianCreateRequest>()
            val guardianToCreate = LegalGuardian().apply {
                firstName = request.firstName
                lastName = request.lastName
                email = request.email
                phone = request.phone
            }

            guardianToCreate.create()
            call.respond(LegalGuardianCreateResponse(success = true, error = "Legal Guardian created"))
        }

        get("/legalGuardian/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val guardians = LegalGuardian().getAll()
            val response = guardians.map { legalGuardians ->
                LegalGuardianGetAllResponse(
                    id = legalGuardians.id!!,
                    firstName = legalGuardians.firstName,
                    lastName = legalGuardians.lastName,
                    email = legalGuardians.email,
                    phone = legalGuardians.phone,
                )
            }

            call.respond(response)
        }

        post("/legalGuardian/get") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val request = call.receive<LegalGuardianGetRequest>()

            val requesterUser = User().apply { id = requesterId }.getFromId()
            if (requesterUser == null) {
                call.respond(LegalGuardianGetResponse(legalGuardian = null, error = "User not found"))
                return@post
            }

            val role = requesterUser.getRoleFromId()
            val realId = requesterUser.realId

            val isAdmin = role == "admin"
            var isAllowed = false

            if (isAdmin) {
                isAllowed = true
            } else if (role == "student") {
                val student = Student().apply { id = realId }.getFromId()
                if (student != null && student.legalGuardianId == request.id) {
                    isAllowed = true
                }
            }

            if (!isAllowed) {
                call.respond(LegalGuardianGetResponse(legalGuardian = null, error = "Not enough privileges"))
                return@post
            }

            val guardian = LegalGuardian().apply { id = request.id }.getFromId()
            if (guardian == null) {
                call.respond(LegalGuardianGetResponse(legalGuardian = null, error = "Legal guardian not found"))
                return@post
            }

            val guardianData = LegalGuardianData(
                id = guardian.id!!,
                firstName = guardian.firstName,
                lastName = guardian.lastName,
                email = guardian.email,
                phone = guardian.phone
            )

            call.respond(LegalGuardianGetResponse(legalGuardian = guardianData, error = null))
        }

        post("/legalGuardian/update") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(LegalGuardianUpdateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<LegalGuardianUpdateRequest>()
            val guardian = LegalGuardian().apply {
                id = request.id
                firstName = request.firstName
                lastName = request.lastName
                email = request.email
                phone = request.phone
            }

            guardian.update()
            call.respond(LegalGuardianUpdateResponse(success = true, error = null))
        }

        post("/legalGuardian/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(LegalGuardianDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<LegalGuardianDeleteRequest>()
            val guardianToDelete = LegalGuardian().apply { id = request.id }

            guardianToDelete.delete()
            call.respond(LegalGuardianDeleteResponse(success = true, error = "Legal Guardian deleted"))
        }

    }
}
