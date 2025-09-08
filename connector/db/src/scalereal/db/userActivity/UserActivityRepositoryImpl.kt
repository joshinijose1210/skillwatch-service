package scalereal.db.userActivity

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.models.domain.UserActivity
import scalereal.core.userActivity.UserActivityRepository
import userActivity.AddActivityCommand
import userActivity.AddActivityParams
import userActivity.GetUserActivitiesCountParams
import userActivity.GetUserActivitiesCountQuery
import userActivity.GetUserActivitiesParams
import userActivity.GetUserActivitiesQuery
import javax.sql.DataSource

@Singleton
class UserActivityRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : UserActivityRepository {
    override fun addActivity(
        actionBy: Long,
        moduleId: Int,
        activity: String,
        description: String,
        ipAddress: String,
    ): Unit =
        dataSource.connection.use { connection ->
            AddActivityCommand()
                .command(connection, AddActivityParams(actionBy, moduleId.toLong(), activity, description, ipAddress))
        }

    override fun fetchUserActivities(
        organisationId: Long,
        offset: Int,
        limit: Int,
    ): List<UserActivity> =
        dataSource.connection.use { connection ->
            GetUserActivitiesQuery()
                .query(connection, GetUserActivitiesParams(organisationId, offset, limit))
                .map {
                    UserActivity(it.firstName, it.lastName, it.empId, it.activity, it.createdAt)
                }
        }

    override fun userActivitiesCount(organisationId: Long): Int =
        dataSource.connection.use { connection ->
            GetUserActivitiesCountQuery()
                .query(
                    connection,
                    GetUserActivitiesCountParams(organisationId),
                )[0]
                .userActivitiesCount
                ?.toInt() ?: 0
        }
}
