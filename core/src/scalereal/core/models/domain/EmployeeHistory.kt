package scalereal.core.models.domain

import java.sql.Timestamp

data class EmployeeHistory(
    val historyId: Long,
    val employeeId: Long,
    val activatedAt: Timestamp,
    val deactivatedAt: Timestamp?,
)
