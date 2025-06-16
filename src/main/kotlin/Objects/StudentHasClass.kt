package org.Objects

import org.Data.ClassTable
import org.Data.StudentHasClassTable
import org.Data.StudentTable
import org.Data.StudentTable.firstName
import org.Data.StudentTable.gender
import org.Data.StudentTable.gradeId
import org.Data.StudentTable.lastName
import org.Data.StudentTable.legalGuardianId
import org.Data.StudentTable.obs
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class StudentHasClass {

    var id: Int? = null
    var studentId: Int = 0
    var classId: Int = 0
    var grade: Int? = null
    var teacherId: Int? = null

    fun create() {
        try {
            transaction {
                StudentHasClassTable.insert {
                    it[studentId] = this@StudentHasClass.studentId
                    it[classId] = this@StudentHasClass.classId
                    it[grade] = this@StudentHasClass.grade
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<StudentHasClass> {
        return try {
            transaction {
                StudentHasClassTable.selectAll().map { row ->
                    StudentHasClass().apply {
                        id = row[StudentHasClassTable.id]
                        studentId = row[StudentHasClassTable.studentId]
                        classId = row[StudentHasClassTable.classId]
                        grade = row[StudentHasClassTable.grade]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun update() {
        val studenHasClasstId = id ?: return

        try {
            transaction {
                StudentHasClassTable.update({ StudentHasClassTable.id eq studenHasClasstId }) {
                    it[studentId] = this@StudentHasClass.studentId
                    it[classId] = this@StudentHasClass.classId
                    it[grade] = this@StudentHasClass.grade
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val studenHasClasstId = id ?: return

        try {
            transaction {
                StudentHasClassTable.deleteWhere { StudentHasClassTable.id eq studenHasClasstId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getClassesByStudentId(): List<StudentHasClass> {
        return try{
            transaction {
                StudentHasClassTable.select { StudentHasClassTable.studentId eq studentId }.map { row ->
                        StudentHasClass().apply {
                            id = row[StudentHasClassTable.id]
                            studentId = row[StudentHasClassTable.studentId]
                            classId = row[StudentHasClassTable.classId]
                            grade = row[StudentHasClassTable.grade]
                        }
                    }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getClassesByTeacherId(teacherId: Int): List<StudentHasClass> {
        return try {
            transaction {
                (StudentHasClassTable innerJoin ClassTable)
                    .select { ClassTable.teacherId eq teacherId }
                    .map { row ->
                        StudentHasClass().apply {
                            id = row[StudentHasClassTable.id]
                            studentId = row[StudentHasClassTable.studentId]
                            classId = row[StudentHasClassTable.classId]
                            grade = row[StudentHasClassTable.grade]
                            this.teacherId = row[ClassTable.teacherId]
                        }
                    }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}