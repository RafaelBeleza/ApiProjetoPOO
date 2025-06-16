package org.Data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object StudentTable : Table("Student") {
    val id = integer("id").autoIncrement()
    val firstName = varchar("firstName", 50)
    val lastName = varchar("lastName", 50)
    val gender = varchar("gender", 50)
    val obs = varchar("obs", 500).nullable()
    val gradeId = integer("gradeId").references(GradeTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val legalGuardianId = integer("legalGuardianId").references(LegalGuardianTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    override val primaryKey = PrimaryKey(id, name = "PK_Student_ID")
}