package org.Data

import org.jetbrains.exposed.sql.Table

object RoleTable : Table("Role") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(id, name = "PK_Role_ID")
}