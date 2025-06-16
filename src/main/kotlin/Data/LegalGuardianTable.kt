package org.Data

import org.jetbrains.exposed.sql.Table

object LegalGuardianTable : Table("LegalGuardian") {
    val id = integer("id").autoIncrement()
    val firstName = varchar("firstName", 50)
    val lastName = varchar("lastName", 50)
    val email = varchar("email", 50).nullable()
    val phone = varchar("phone", 50).nullable()

    override val primaryKey = PrimaryKey(id, name = "PK_LegalGuardian_ID")
}