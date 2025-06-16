package org.Routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.Objects.Class
import org.Objects.Grade
import org.Objects.Student
import org.Objects.StudentHasClass
import org.Objects.Teacher
import org.Objects.User

@Serializable
data class StudentHasClassCreateRequest(val studentId: Int, val classId: Int, val grade: Int? = null)

@Serializable
data class StudentHasClassCreateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class StudentHasClassGetResponse(val id: Int, val studentId: Int, val classId: Int, val grade: Int? = null)

@Serializable
data class StudentHasClassUpdateRequest(val id: Int, val studentId: Int, val classId: Int, val grade: Int? = null)

@Serializable
data class StudentHasClassGetClassesByStudentIdRequest(val studentId: Int)

@Serializable
data class StudentHasClassGetClassesByTeacherIdRequest(val teacherId: Int)

@Serializable
data class StudentHasClassDeleteRequest(val id: Int)

@Serializable
data class StudentHasClassGetDetailedResponse(
    val id: Int,
    val studentName: String,
    val className: String,
    val grade: Int?
)

fun Route.StudentHasClassRoutes() {

    authenticate("auth-jwt") {

        post("/studentHasClass/create") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(StudentHasClassCreateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<StudentHasClassCreateRequest>()
            val studentHasClassToCreate = StudentHasClass().apply {
                studentId = request.studentId
                classId = request.classId
                grade = request.grade
            }

            studentHasClassToCreate.create()
            call.respond(StudentHasClassCreateResponse(success = true, error = null))
        }

        get("/studentHasClass/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val StudentHasClasses = StudentHasClass().getAll()
            val response = StudentHasClasses.map { studentHasClasses ->
                StudentHasClassGetResponse(
                    id = studentHasClasses.id!!,
                    studentId = studentHasClasses.studentId,
                    classId = studentHasClasses.classId,
                    grade = studentHasClasses.grade
                )
            }

            call.respond(response)
        }

        post("/studentHasClass/update") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(StudentUpdateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<StudentHasClassUpdateRequest>()

            if (request.grade !in 0..20) {
                call.respond(StudentUpdateResponse(success = false, error = "grade must be between 0 and 20"))
                return@post
            }

            val studentIdExists = Student().apply { id = request.studentId }.getFromId()
            if (studentIdExists == null) {
                call.respond(StudentUpdateResponse(success = false, error = "studentId does not exist"))
                return@post
            }

            val classIdExists = Class().apply { id = request.classId }.getFromId()
            if (classIdExists == null) {
                call.respond(StudentUpdateResponse(success = false, error = "classId does not exist"))
                return@post
            }

            val studentHasClass = StudentHasClass().apply {
                id = request.id
                studentId = request.studentId
                classId = request.classId
                grade = request.grade
            }

            studentHasClass.update()

            call.respond(StudentUpdateResponse(success = true, error = null))
        }

        post("/studentHasClass/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(StudentDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<StudentHasClassDeleteRequest>()
            val studentHasClassToDelete = StudentHasClass().apply { id = request.id }

            studentHasClassToDelete.delete()
            call.respond(StudentDeleteResponse(success = true, error = null))
        }

        post("/studentHasClass/getClassesByStudentId") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)

            val requesterUser = User().apply { id = requesterId }.getFromId()
            if (requesterUser == null) {
                call.respondText("User not found", status = HttpStatusCode.Unauthorized)
                return@post
            }

            val requesterRole = requesterUser.getRoleFromId()
            val requesterRealId = requesterUser.realId

            val request = call.receive<StudentHasClassGetClassesByStudentIdRequest>()

            val isOwnStudentRecord = requesterRealId == request.studentId
            val isAdmin = requesterRole == "admin"

            if (!isAdmin && !isOwnStudentRecord) {
                call.respondText("Not enough privileges", status = HttpStatusCode.Forbidden)
                return@post
            }

            val studentToGetClasses = StudentHasClass().apply { studentId = request.studentId }
            val studentHasClasses = studentToGetClasses.getClassesByStudentId()

            val student = Student().apply { id = request.studentId }.getFromId()
            val studentName = student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Student"

            val response = studentHasClasses.map { entry ->
                val className = Class().apply { id = entry.classId }.getFromId()?.name ?: "Unknown Class"

                StudentHasClassGetDetailedResponse(
                    id = entry.id!!,
                    studentName = studentName,
                    className = className,
                    grade = entry.grade
                )
            }

            call.respond(response)
        }

        post("/studentHasClass/getClassesByTeacherId") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            if (requesterUser == null) {
                call.respondText("User not found", status = HttpStatusCode.Unauthorized)
                return@post
            }

            val requesterRole = requesterUser.getRoleFromId()
            val requesterRealId = requesterUser.realId

            val request = call.receive<StudentHasClassGetClassesByTeacherIdRequest>()

            val isOwnTeacherRecord = requesterRealId == request.teacherId
            val isAdmin = requesterRole == "admin"

            if (!isAdmin && !isOwnTeacherRecord) {
                call.respondText("Not enough privileges", status = HttpStatusCode.Forbidden)
                return@post
            }

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@post
            }

            val data = StudentHasClass()

            val studentHasClasses = data.getClassesByTeacherId(request.teacherId)
            val response = studentHasClasses.map { studentHasClasses ->
                StudentHasClassGetResponse(
                    id = studentHasClasses.id!!,
                    studentId = studentHasClasses.studentId,
                    classId = studentHasClasses.classId,
                    grade = studentHasClasses.grade
                )
            }

            call.respond(response)
        }

    }

}