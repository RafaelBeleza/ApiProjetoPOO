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
import org.Objects.Grade
import org.Objects.User

@Serializable
data class GradeCreateRequest(val name: String)

@Serializable
data class GradeCreateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class GradeGetAllResponse(val id: Int, val name: String)

@Serializable
data class GradeGetRequest(val id: Int)

@Serializable
data class GradeGetResponse(val grade: GradeData? = null, val error: String? = null)

@Serializable
data class GradeUpdateRequest(val id: Int, val name: String)

@Serializable
data class GradeUpdateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class GradeDeleteRequest(val id: Int)

@Serializable
data class GradeDeleteResponse(val success: Boolean, val error: String? = null)

@Serializable
data class GradeData(
    val id: Int,
    val name: String
)

fun Route.GradeRoutes() {

    authenticate("auth-jwt") {

        post("/grade/create") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(GradeCreateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<GradeCreateRequest>()
            val gradeToCreate = Grade().apply { name = request.name }

            gradeToCreate.create()
            call.respond(GradeCreateResponse(success = true, error = "Grade created"))
        }

        get("/grade/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val grades = Grade().getAll()
            val response = grades.map {
                GradeGetAllResponse(
                    id = it.id!!,
                    name = it.name
                )
            }

            call.respond(response)
        }

        post("/grade/get") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(GradeGetResponse(grade = null, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<GradeGetRequest>()
            val gradeToGet = Grade().apply { id = request.id }

            val foundGrade = gradeToGet.getFromId()

            if (foundGrade == null) {
                call.respond(GradeGetResponse(grade = null, error = "Grade not found"))
                return@post
            }

            val gradeData = GradeData(
                id = foundGrade.id!!,
                name = foundGrade.name
            )

            call.respond(GradeGetResponse(grade = gradeData, error = null))
        }

        post("/grade/update") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(GradeUpdateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<GradeUpdateRequest>()
            val grade = Grade().apply {
                id = request.id
                name = request.name
            }

            grade.update()
            call.respond(GradeUpdateResponse(success = true, error = null))
        }

        post("/grade/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(GradeDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<GradeDeleteRequest>()
            val gradeToDelete = Grade().apply { id = request.id }

            gradeToDelete.delete()
            call.respond(GradeDeleteResponse(success = true, error = null))
        }

    }
}


