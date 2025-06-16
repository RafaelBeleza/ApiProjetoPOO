package org.Objects

import org.Data.LegalGuardianTable
import org.Data.RoleTable
import org.Data.RoleTable.name
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LegalGuardian {

    var id: Int? = null
    var firstName: String = ""
    var lastName: String = ""
    var email: String? = null
    var phone: String? = null

    fun create() {
        try {
            transaction {
                LegalGuardianTable.insert {
                    it[firstName] = this@LegalGuardian.firstName
                    it[lastName] = this@LegalGuardian.lastName
                    it[email] = this@LegalGuardian.email
                    it[phone] = this@LegalGuardian.phone
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getAll(): List<LegalGuardian> {
        return try {
            transaction {
                LegalGuardianTable.selectAll().map { row ->
                    LegalGuardian().apply {
                        id = row[LegalGuardianTable.id]
                        firstName = row[LegalGuardianTable.firstName]
                        lastName = row[LegalGuardianTable.lastName]
                        email = row[LegalGuardianTable.email]
                        phone = row[LegalGuardianTable.phone]
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun getFromId(): LegalGuardian? {
        val legalGuardianId = id ?: return null

        return try {
            transaction {
                val row = LegalGuardianTable.select { LegalGuardianTable.id eq legalGuardianId }.firstOrNull()
                if (row != null) {
                    LegalGuardian().apply {
                        id = row[LegalGuardianTable.id]
                        firstName = row[LegalGuardianTable.firstName]
                        lastName = row[LegalGuardianTable.lastName]
                        email = row[LegalGuardianTable.email]
                        phone = row[LegalGuardianTable.phone]
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
        val legalGuardianId = id ?: return

        try {
            transaction {
                LegalGuardianTable.update({ LegalGuardianTable.id eq legalGuardianId }) {
                    it[firstName] = this@LegalGuardian.firstName
                    it[lastName] = this@LegalGuardian.lastName
                    it[email] = this@LegalGuardian.email
                    it[phone] = this@LegalGuardian.phone
                }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    fun delete() {
        val legalGuardianId = id ?: return

        try {
            transaction {
                LegalGuardianTable.deleteWhere { LegalGuardianTable.id eq legalGuardianId }
            }
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

}