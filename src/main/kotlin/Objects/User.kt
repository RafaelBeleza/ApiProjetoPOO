package org.Objects

import org.Data.RoleTable
import org.Data.RoleTable.name
import org.Data.StudentTable
import org.Data.StudentTable.firstName
import org.Data.StudentTable.gender
import org.Data.StudentTable.gradeId
import org.Data.StudentTable.lastName
import org.Data.StudentTable.legalGuardianId
import org.Data.StudentTable.obs
import org.Data.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt

class User {

    var id: Int? = null
    var username: String = ""
    var email: String = ""
    var password: String = ""
    var roleId: Int = 0
    var realId: Int? = null

    fun login(): User? {
        return try {
            transaction {
                val userInfo = UserTable
                    .select { UserTable.email eq this@User.email }
                    .limit(1)
                    .firstOrNull()

                if (userInfo != null && BCrypt.checkpw(this@User.password, userInfo[UserTable.password])) {
                    User().apply {
                        id = userInfo[UserTable.id]
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: " + e.message)
        }
    }

    fun register() {
        try {
            val hashedPassword = BCrypt.hashpw(this.password, BCrypt.gensalt())

            transaction {
                UserTable.insert {
                    it[username] = this@User.username
                    it[email] = this@User.email
                    it[password] = hashedPassword
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: " + e.message)
        }
    }

    fun emailExists(): Boolean {
        return try {
            transaction {
                UserTable.select { UserTable.email eq this@User.email }
                    .limit(1)
                    .any()
            }
        } catch (e: Exception) {
            throw Exception("Error: " + e.message)
        }
    }

    fun getRoleFromId(): String? {
        val userId = id ?: return null

        return try {
            transaction {
                val roleId = UserTable
                    .select { UserTable.id eq userId }
                    .limit(1)
                    .firstOrNull()
                    ?.get(UserTable.roleId)

                roleId?.let {
                    RoleTable
                        .select { RoleTable.id eq it }
                        .limit(1)
                        .firstOrNull()
                        ?.get(RoleTable.name)
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val userId = id ?: return

        try {
            transaction {
                UserTable.deleteWhere { UserTable.id eq userId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<User> {
        return try {
            transaction {
                UserTable.selectAll().map { row ->
                    User().apply {
                        id = row[UserTable.id]
                        username = row[UserTable.username]
                        email = row[UserTable.email]
                        roleId = row[UserTable.roleId]
                        realId = row[UserTable.realId]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getFromId(): User? {
        val userId = id ?: return null

        return try {
            transaction {
                val row = UserTable.select { UserTable.id eq userId }.firstOrNull()
                if (row != null) {
                    User().apply {
                        id = row[UserTable.id]
                        username = row[UserTable.username]
                        email = row[UserTable.email]
                        password = row[UserTable.password]
                        roleId = row[UserTable.roleId]
                        realId = row[UserTable.realId]
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun updateRoleAndRealId() {
        val userId = id ?: return

        try {
            transaction {
                UserTable.update({ UserTable.id eq userId }) {
                    it[roleId] = this@User.roleId
                    it[realId] = this@User.realId
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}