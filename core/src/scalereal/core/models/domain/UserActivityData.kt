package scalereal.core.models.domain

import java.sql.Timestamp

data class UserActivity(
    val firstName: String,
    val lastName: String,
    val employeeId: String,
    val activity: String,
    val createdAt: Timestamp,
)

data class UserActivityResponse(
    val totalUserActivities: Int,
    val userActivities: List<UserActivity>,
)
