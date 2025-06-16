package org.Data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TeacherTable : Table("Teacher") {
    val id = integer("id").autoIncrement()
    val firstName = varchar("firstName", 50)
    val lastName = varchar("lastName", 50)
    val gender = varchar("gender", 50)
    val obs = varchar("obs", 500).nullable()

    override val primaryKey = PrimaryKey(id, name = "PK_Teacher_ID")
}