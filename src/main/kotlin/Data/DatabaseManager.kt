package org.Data

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.SQLException

class DatabaseManager {

    private val log = LoggerFactory.getLogger(javaClass)

    fun createTables() {
        try {
            transaction {
                log.info("Attempting to create default database")
                SchemaUtils.create(UserTable, RoleTable, StudentTable, LegalGuardianTable, GradeTable, ClassTable, StudentHasClassTable, TeacherTable)
                RoleTable.insert { it[name] = "guest" }
                RoleTable.insert { it[name] = "student" }
                RoleTable.insert { it[name] = "teacher" }
                RoleTable.insert { it[name] = "admin" }
                log.info("Default database created successfully.")
            }
        } catch (e: SQLException) {
            log.error("Database error while creating tables .", e)
        } catch (e: Exception) {
            log.error("An unexpected error occurred during table creation.", e)
        }
    }

    fun dropTables() {
        try {
            transaction {
                log.info("Attempting to drop tables")
                SchemaUtils.drop(UserTable, RoleTable, StudentTable, LegalGuardianTable, GradeTable, ClassTable, StudentHasClassTable, TeacherTable)
                log.info("Tables dropped successfully.")
            }
        } catch (e: SQLException) {
            log.error("Database error while dropping tables.", e)
        } catch (e: Exception) {
            log.error("An unexpected error occurred during table deletion.", e)
        }
    }

}