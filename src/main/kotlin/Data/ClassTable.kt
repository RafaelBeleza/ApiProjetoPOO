package org.Data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ClassTable : Table("Class") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val gradeId = integer("gradeId").references(GradeTable.id, onDelete = ReferenceOption.CASCADE)
    val teacherId = integer("teacherId").references(TeacherTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id, name = "PK_Class_ID")
}
