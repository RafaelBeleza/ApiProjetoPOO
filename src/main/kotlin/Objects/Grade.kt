package org.Objects

import org.Data.GradeTable
import org.Data.RoleTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class Grade {

    var id: Int? = null
    var name: String = ""

    fun create() {
        try {
            transaction {
                GradeTable.insert { it[name] = this@Grade.name }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<Role> {
        return try {
            transaction {
                GradeTable.selectAll().map { row ->
                    Role().apply {
                        id = row[GradeTable.id]
                        name = row[GradeTable.name]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getFromId(): Role? {
        val gradeId = id ?: return null

        return try {
            transaction {
                val row = GradeTable.select { GradeTable.id eq gradeId }.firstOrNull()
                if (row != null) {
                    Role().apply {
                        id = row[GradeTable.id]
                        name = row[GradeTable.name]
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
        val gradeId = id ?: return

        try {
            transaction {
                GradeTable.update({ GradeTable.id eq gradeId }) {
                    it[name] = this@Grade.name
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val gradeId = id ?: return

        try {
            transaction {
                GradeTable.deleteWhere { GradeTable.id eq gradeId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}