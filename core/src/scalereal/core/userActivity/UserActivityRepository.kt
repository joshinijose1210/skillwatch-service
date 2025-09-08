package scalereal.core.userActivity

import scalereal.core.models.domain.UserActivity

interface UserActivityRepository {
    fun addActivity(
        actionBy: Long,
        moduleId: Int,
        activity: String,
        description: String,
        ipAddress: String,
    )

    fun fetchUserActivities(
        organisationId: Long,
        offset: Int,
        limit: Int,
    ): List<UserActivity>

    fun userActivitiesCount(organisationId: Long): Int
}
