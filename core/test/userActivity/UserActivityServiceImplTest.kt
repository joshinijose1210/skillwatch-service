package userActivity

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.models.domain.UserActivity
import scalereal.core.userActivity.UserActivityRepository
import scalereal.core.userActivity.UserActivityService
import java.sql.Timestamp

class UserActivityServiceImplTest : StringSpec() {
    private val userActivityRepository = mockk<UserActivityRepository>()
    private val userActivityService = UserActivityService(userActivityRepository)

    init {
        "should return user activity count" {
            every { userActivityRepository.userActivitiesCount(any()) } returns 8
            userActivityService.userActivitiesCount(organisationId = 1) shouldBe 8
            verify(exactly = 1) { userActivityRepository.userActivitiesCount(organisationId = 1) }
        }

        "should return user activities" {
            val now = Timestamp(System.currentTimeMillis())
            val organisationId = 1L
            val userActivities =
                listOf(
                    UserActivity(
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        employeeId = "SR0051",
                        activity = "Review Cycle Created",
                        createdAt = now,
                    ),
                    UserActivity(
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        employeeId = "SR0006",
                        activity = "Review Cycle Updated",
                        createdAt = now,
                    ),
                )
            every { userActivityRepository.fetchUserActivities(any(), any(), any()) } returns userActivities
            userActivityService.fetchUserActivities(
                organisationId = organisationId,
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe userActivities
            verify(exactly = 1) { userActivityRepository.fetchUserActivities(organisationId = 1, offset = 0, limit = Int.MAX_VALUE) }
        }
    }
}
