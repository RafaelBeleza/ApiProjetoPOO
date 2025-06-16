package org.Data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserTable : Table("User") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 60)
    val realId = integer("realId").nullable()
    val roleId = integer("roleId").default(1).references(RoleTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID")
}