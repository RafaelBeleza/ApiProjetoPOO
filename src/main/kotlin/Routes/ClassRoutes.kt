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
import org.Objects.User
import org.Objects.Class
import org.Objects.Grade
import org.Objects.Teacher

@Serializable
data class ClassCreateRequest(val name: String, val gradeId: Int, val teacherId: Int)

@Serializable
data class ClassCreateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class ClassGetAllResponse(val id: Int, val name: String, val gradeId: Int, val teacherId: Int)

@Serializable
data class ClassGetRequest(val id: Int)

@Serializable
data class ClassGetResponse(val role: ClassData? = null, val error: String? = null)

@Serializable
data class ClassUpdateRequest(val id: Int, val name: String, val gradeId: Int, val teacherId: Int)

@Serializable
data class ClassUpdateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class ClassDeleteRequest(val id: Int)

@Serializable
data class ClassDeleteResponse(val success: Boolean, val error: String? = null)

@Serializable
data class ClassData(
    val id: Int,
    val name: String,
    val gradeId: Int,
    val teacherId: Int
)

fun Route.ClassRoutes() {

    authenticate("auth-jwt") {

        post("/class/create") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(ClassCreateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<ClassCreateRequest>()
            val newClass = Class().apply {
                name = request.name
                gradeId = request.gradeId
                teacherId = request.teacherId
            }

            newClass.create()
            call.respond(ClassCreateResponse(success = true, error = "Class created"))
        }

        get("/class/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val classes = Class().getAll()
            val response = classes.map { classItem ->
                ClassGetAllResponse(
                    id = classItem.id!!,
                    name = classItem.name,
                    gradeId = classItem.gradeId,
                    teacherId = classItem.teacherId
                )
            }

            call.respond(response)
        }

        post("/class/get") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(ClassGetResponse(role = null, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<ClassGetRequest>()
            val classToGet = Class().apply { id = request.id }

            val foundClass = classToGet.getFromId()

            if (foundClass == null) {
                call.respond(ClassGetResponse(role = null, error = "Class not found"))
                return@post
            }

            val classData = ClassData(
                id = foundClass.id!!,
                name = foundClass.name,
                gradeId = foundClass.gradeId,
                teacherId = foundClass.teacherId
            )

            call.respond(ClassGetResponse(role = classData, error = null))
        }

        post("/class/update") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(ClassUpdateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<ClassUpdateRequest>()

            val gradeExists = Grade().apply { id = request.gradeId }.getFromId()
            if (gradeExists == null) {
                call.respond(ClassUpdateResponse(success = false, error = "gradeId does not exist"))
                return@post
            }

            val teacherExists = Teacher().apply { id = request.teacherId }.getFromId()
            if (teacherExists == null) {
                call.respond(ClassUpdateResponse(success = false, error = "teacherId does not exist"))
                return@post
            }

            val classToUpdate = Class().apply {
                id = request.id
                name = request.name
                gradeId = request.gradeId
                teacherId = request.teacherId
            }

            classToUpdate.update()
            call.respond(ClassUpdateResponse(success = true, error = null))
        }

        post("/class/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(ClassDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<ClassDeleteRequest>()
            val classToDelete = Class().apply { id = request.id }

            classToDelete.delete()
            call.respond(ClassDeleteResponse(success = true, error = null))
        }
    }
}