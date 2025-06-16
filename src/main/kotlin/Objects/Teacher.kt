package org.Objects

import org.Data.TeacherTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class Teacher {

    var id: Int? = null
    var firstName: String = ""
    var lastName: String = ""
    var gender: String = ""
    var obs: String? = null

    fun create() {
        try {
            transaction {
                TeacherTable.insert {
                    it[TeacherTable.firstName] = this@Teacher.firstName
                    it[TeacherTable.lastName] = this@Teacher.lastName
                    it[TeacherTable.gender] = this@Teacher.gender
                    it[TeacherTable.obs] = this@Teacher.obs
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<Teacher> {
        return try {
            transaction {
                TeacherTable.selectAll().map { row ->
                    Teacher().apply {
                        id = row[TeacherTable.id]
                        firstName = row[TeacherTable.firstName]
                        lastName = row[TeacherTable.lastName]
                        gender = row[TeacherTable.gender]
                        obs = row[TeacherTable.obs]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getFromId(): Teacher? {
        val teacherId = id ?: return null

        return try {
            transaction {
                val row = TeacherTable.select { TeacherTable.id eq teacherId }.firstOrNull()
                if (row != null) {
                    Teacher().apply {
                        id = row[TeacherTable.id]
                        firstName = row[TeacherTable.firstName]
                        lastName = row[TeacherTable.lastName]
                        gender = row[TeacherTable.gender]
                        obs = row[TeacherTable.obs]
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
        val teacherId = id ?: return

        try {
            transaction {
                TeacherTable.update({ TeacherTable.id eq teacherId }) {
                    it[TeacherTable.firstName] = this@Teacher.firstName
                    it[TeacherTable.lastName] = this@Teacher.lastName
                    it[TeacherTable.gender] = this@Teacher.gender
                    it[TeacherTable.obs] = this@Teacher.obs
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val teacherId = id ?: return

        try {
            transaction {
                TeacherTable.deleteWhere { TeacherTable.id eq teacherId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}