package org.Data

import org.jetbrains.exposed.sql.Table

object GradeTable : Table("Grade") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(id, name = "PK_Grade_ID")
}