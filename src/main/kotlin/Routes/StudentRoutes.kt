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
import org.Objects.Student
import org.Objects.User

@Serializable
data class StudentCreateRequest(val firstName: String, val lastName: String, val gender: String, val obs: String?, val gradeId: Int?, val legalGuardianId: Int?)

@Serializable
data class StudentCreateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class StudentGetAllResponse(val id: Int, val firstName: String, val lastName: String, val gender: String, val obs: String?, val gradeId: Int?, val legalGuardianId: Int?)

@Serializable
data class StudentGetRequest(val id: Int)

@Serializable
data class StudentGetResponse(val student: StudentData? = null, val error: String? = null)

@Serializable
data class StudentUpdateRequest(val id: Int, val firstName: String, val lastName: String, val gender: String, val obs: String?, val gradeId: Int?, val legalGuardianId: Int?)

@Serializable
data class StudentUpdateResponse(val success: Boolean, val error: String? = null)

@Serializable
data class StudentDeleteRequest(val id: Int)

@Serializable
data class StudentDeleteResponse(val success: Boolean, val error: String? = null)

@Serializable
data class StudentData(
    var id: Int,
    var firstName: String,
    var lastName: String,
    var gender: String,
    var obs: String?,
    var gradeId: Int?,
    var legalGuardianId: Int?
)

fun Route.StudentRoutes() {

    authenticate("auth-jwt") {

        post("/student/create") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(StudentCreateResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<StudentCreateRequest>()
            val studentToCreate = Student().apply {
                firstName = request.firstName
                lastName = request.lastName
                gender = request.gender
                obs = request.obs
                gradeId = request.gradeId
                legalGuardianId = request.legalGuardianId
            }

            studentToCreate.create()
            call.respond(StudentCreateResponse(success = true, error = "Student created"))
        }

        get("/student/getAll") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond("Not enough privileges")
                return@get
            }

            val students = Student().getAll()
            val response = students.map { student ->
                StudentGetAllResponse(
                    id = student.id!!,
                    firstName = student.firstName,
                    lastName = student.lastName,
                    gender = student.gender,
                    obs = student.obs,
                    gradeId = student.gradeId,
                    legalGuardianId = student.legalGuardianId
                )
            }

            call.respond(response)
        }


        post("/student/get") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val request = call.receive<StudentGetRequest>()

            val requesterUser = User().apply { id = requesterId }.getFromId()
            if (requesterUser == null) {
                call.respond(StudentGetResponse(student = null, error = "User not found"))
                return@post
            }

            val requesterRole = requesterUser.getRoleFromId()
            val requesterRealId = requesterUser.realId

            val isAdmin = requesterRole == "admin"
            val isOwnStudentRecord = requesterRealId == request.id

            if (!isAdmin && !isOwnStudentRecord) {
                call.respond(StudentGetResponse(student = null, error = "Not enough privileges"))
                return@post
            }

            val studentToGet = Student().apply { id = request.id }
            val foundStudent = studentToGet.getFromId()

            if (foundStudent == null) {
                call.respond(StudentGetResponse(student = null , error = "Student not found"))
                return@post
            }

            val studentData = StudentData(
                id = foundStudent.id!!,
                firstName = foundStudent.firstName,
                lastName = foundStudent.lastName,
                gender = foundStudent.gender,
                obs = foundStudent.obs,
                gradeId = foundStudent.gradeId,
                legalGuardianId = foundStudent.legalGuardianId
            )

            call.respond(StudentGetResponse(student = studentData, error = null))
        }

        post("/student/delete") {
            val requesterId = call.principal<JWTPrincipal>()?.getClaim("Id", Int::class)
            val requesterUser = User().apply { id = requesterId }
            val requesterRole = requesterUser.getRoleFromId()

            if (requesterRole != "admin") {
                call.respond(StudentDeleteResponse(success = false, error = "Not enough privileges"))
                return@post
            }

            val request = call.receive<StudentDeleteRequest>()
            val studentToDelete = Student().apply { id = request.id }

            studentToDelete.delete()
            call.respond(StudentDeleteResponse(success = true, error = "Student deleted"))
        }

    }

}