package org.Objects

import org.Data.RoleTable
import org.Data.StudentTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class Student {

    var id: Int? = null
    var firstName: String = ""
    var lastName: String = ""
    var gender: String = ""
    var obs: String? = null
    var gradeId: Int? = null
    var legalGuardianId: Int? = null

    fun create() {
        try {
            transaction {
                StudentTable.insert {
                    it[firstName] = this@Student.firstName
                    it[lastName] = this@Student.lastName
                    it[gender] = this@Student.gender
                    it[obs] = this@Student.obs
                    it[gradeId] = this@Student.gradeId
                    it[legalGuardianId] = this@Student.legalGuardianId
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<Student> {
        return try {
            transaction {
                StudentTable.selectAll().map { row ->
                    Student().apply {
                        id = row[StudentTable.id]
                        firstName = row[StudentTable.firstName]
                        lastName = row[StudentTable.lastName]
                        gender = row[StudentTable.gender]
                        obs = row[StudentTable.obs]
                        gradeId = row[StudentTable.gradeId]
                        legalGuardianId = row[StudentTable.legalGuardianId]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getFromId(): Student? {
        val studentId = id ?: return null

        return try {
            transaction {
                val row = StudentTable.select { StudentTable.id eq studentId }.firstOrNull()
                if (row != null) {
                    Student().apply {
                        id = row[StudentTable.id]
                        firstName = row[StudentTable.firstName]
                        lastName = row[StudentTable.lastName]
                        gender = row[StudentTable.gender]
                        obs = row[StudentTable.obs]
                        gradeId = row[StudentTable.gradeId]
                        legalGuardianId = row[StudentTable.legalGuardianId]
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun update() {
        val studentId = id ?: return

        try {
            transaction {
                StudentTable.update({ StudentTable.id eq studentId }) {
                    it[firstName] = this@Student.firstName
                    it[lastName] = this@Student.lastName
                    it[gender] = this@Student.gender
                    it[obs] = this@Student.obs
                    it[gradeId] = this@Student.gradeId
                    it[legalGuardianId] = this@Student.legalGuardianId
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val studentId = id ?: return

        try {
            transaction {
                StudentTable.deleteWhere { StudentTable.id eq studentId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}