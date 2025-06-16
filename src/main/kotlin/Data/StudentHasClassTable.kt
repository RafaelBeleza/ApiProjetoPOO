package org.Data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object StudentHasClassTable : Table("StudentHasClass") {
    val id = integer("id").autoIncrement()
    val studentId = integer("studentId").references(StudentTable.id, onDelete = ReferenceOption.CASCADE)
    val classId = integer("classId").references(ClassTable.id, onDelete = ReferenceOption.CASCADE)
    val grade = integer("grade").nullable()

    override val primaryKey = PrimaryKey(id, name = "PK_StudentHasClass_ID")

    init {
        check("CHK_GradeRange") { grade.between(0, 20) }
    }
}