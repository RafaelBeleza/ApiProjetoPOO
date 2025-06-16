package org.Objects

import org.Data.RoleTable
import org.Data.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class Role {

    var id: Int? = null
    var name: String = ""

    fun create() {
        try {
            transaction {
                RoleTable.insert { it[name] = this@Role.name }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<Role> {
        return try {
            transaction {
                RoleTable.selectAll().map { row ->
                    Role().apply {
                        id = row[RoleTable.id]
                        name = row[RoleTable.name]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getFromId(): Role? {
        val roleId = id ?: return null

        return try {
            transaction {
                val row = RoleTable.select { RoleTable.id eq roleId }.firstOrNull()
                if (row != null) {
                    Role().apply {
                        id = row[RoleTable.id]
                        name = row[RoleTable.name]
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
        val roleId = id ?: return

        try {
            transaction {
                RoleTable.update({ RoleTable.id eq roleId }) {
                    it[name] = this@Role.name
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val roleId = id ?: return

        try {
            transaction {
                RoleTable.deleteWhere { RoleTable.id eq roleId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}