package scalereal.core.userActivity

import jakarta.inject.Singleton
import scalereal.core.models.domain.UserActivity

@Singleton
class UserActivityService(
    private val repository: UserActivityRepository,
) {
    fun fetchUserActivities(
        organisationId: Long,
        page: Int,
        limit: Int,
    ): List<UserActivity> = repository.fetchUserActivities(organisationId, offset = (page - 1) * limit, limit = limit)

    fun userActivitiesCount(organisationId: Long): Int = repository.userActivitiesCount(organisationId)
}
