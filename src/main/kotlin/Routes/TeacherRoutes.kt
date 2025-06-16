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
import org.Objects.Teacher
import org.Objects.User

@Serializable
data class TeacherCreateRequest(val firstName: String, val lastName: String, val gender: String, val obs: String?)

@Serializable
data class TeacherCreateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class TeacherGetAllResponse(val id: Int, val firstName: String, val lastName: String, val gender: String, val obs: String?)

@Serializable
data class TeacherGetRequest(val id: Int)

@Serializable
data class TeacherGetResponse(val teacher: TeacherData? = null, val error: String? = null)

@Serializable
data class TeacherUpdateRequest(val id: Int, val firstName: String, val lastName: String, val gender: String, val obs: String?)

@Serializable
data class TeacherUpdateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class TeacherDeleteRequest(val id: Int)

@Serializable
data class TeacherDeleteResponse(val success: Boolean, val error: String? = null)

@Serializable
data class TeacherData(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val obs: String?,
)

fun Route.TeacherRoutes() {

    authenticate("auth-jwt") {

        post("/teacher/create") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(TeacherCreateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<TeacherCreateRequest>()
            val teacherToCreate = Teacher().apply {
                firstName = request.firstName
                lastName = request.lastName
                gender = request.gender
                obs = request.obs
            }

            teacherToCreate.create()
            call.respond(TeacherCreateResponse(success = true, error = "Teacher created"))
        }

        get("/teacher/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val teachers = Teacher().getAll()
            val response = teachers.map {
                TeacherGetAllResponse(
                    id = it.id!!,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    gender = it.gender,
                    obs = it.obs,
                )
            }

            call.respond(response)
        }

        post("/teacher/get") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(TeacherGetResponse(teacher = null, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<TeacherGetRequest>()
            val teacherToGet = Teacher().apply { id = request.id }

            val foundTeacher = teacherToGet.getFromId()

            if (foundTeacher == null) {
                call.respond(TeacherGetResponse(teacher = null, error = "Teacher not found"))
                return@post
            }

            val teacherData = TeacherData(
                id = foundTeacher.id!!,
                firstName = foundTeacher.firstName,
                lastName = foundTeacher.lastName,
                gender = foundTeacher.gender,
                obs = foundTeacher.obs,
            )

            call.respond(TeacherGetResponse(teacher = teacherData, error = null))
        }

        post("/teacher/update") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(TeacherUpdateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<TeacherUpdateRequest>()
            val teacher = Teacher().apply {
                id = request.id
                firstName = request.firstName
                lastName = request.lastName
                gender = request.gender
                obs = request.obs
            }

            teacher.update()

            call.respond(TeacherUpdateResponse(success = true, error = null))
        }

        post("/teacher/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(TeacherDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<TeacherDeleteRequest>()
            val teacherToDelete = Teacher().apply { id = request.id }

            teacherToDelete.delete()
            call.respond(TeacherDeleteResponse(success = true, error = null))
        }
    }
}
