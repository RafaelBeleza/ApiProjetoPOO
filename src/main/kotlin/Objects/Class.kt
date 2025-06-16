package org.Objects

import org.Data.ClassTable
import org.Data.GradeTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class Class {

    var id: Int? = null
    var name: String = ""
    var gradeId: Int = 0
    var teacherId: Int = 0

    fun create() {
        try {
            transaction {
                ClassTable.insert {
                    it[name] = this@Class.name
                    it[gradeId] = this@Class.gradeId
                    it[teacherId] = this@Class.teacherId
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<Class> {
        return try {
            transaction {
                ClassTable.selectAll().map { row ->
                    Class().apply {
                        id = row[ClassTable.id]
                        name = row[ClassTable.name]
                        gradeId = row[ClassTable.gradeId]
                        teacherId = row[ClassTable.teacherId]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getFromId(): Class? {
        val classId = id ?: return null

        return try {
            transaction {
                val row = ClassTable.select { ClassTable.id eq classId }.firstOrNull()
                if (row != null) {
                    Class().apply {
                        id = row[ClassTable.id]
                        name = row[ClassTable.name]
                        gradeId = row[ClassTable.gradeId]
                        teacherId = row[ClassTable.teacherId]
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
        val classId = id ?: return

        try {
            transaction {
                ClassTable.update({ ClassTable.id eq classId }) {
                    it[name] = this@Class.name
                    it[gradeId] = this@Class.gradeId
                    it[teacherId] = this@Class.teacherId
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val classId = id ?: return

        try {
            transaction {
                ClassTable.deleteWhere { ClassTable.id eq classId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}