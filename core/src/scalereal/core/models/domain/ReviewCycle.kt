package scalereal.core.models.domain

import java.math.BigDecimal
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId

data class ReviewCycle(
    val organisationId: Long,
    val reviewCycleId: Long,
    val startDate: Date,
    val endDate: Date,
    var publish: Boolean,
    val lastModified: Timestamp?,
    val selfReviewStartDate: Date,
    val selfReviewEndDate: Date,
    val managerReviewStartDate: Date,
    val managerReviewEndDate: Date,
    val checkInWithManagerStartDate: Date,
    val checkInWithManagerEndDate: Date,
    val isReviewCycleActive: Boolean = false,
    val isSelfReviewActive: Boolean = false,
    val isManagerReviewActive: Boolean = false,
    val isCheckInWithManagerActive: Boolean = false,
    val isSelfReviewDatePassed: Boolean,
    val isManagerReviewDatePassed: Boolean,
    val isCheckInWithManagerDatePassed: Boolean,
) {
    fun withActiveFlags(organisationTimeZone: String): ReviewCycle {
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        val isReviewCycleActive =
            publish &&
                organisationCurrentDate in startDate.toLocalDate()..endDate.toLocalDate()

        val isSelfReviewActive =
            publish &&
                organisationCurrentDate in selfReviewStartDate.toLocalDate()..selfReviewEndDate.toLocalDate()

        val isManagerReviewActive =
            publish &&
                organisationCurrentDate in managerReviewStartDate.toLocalDate()..managerReviewEndDate.toLocalDate()

        val isCheckInWithManagerActive =
            publish &&
                organisationCurrentDate in checkInWithManagerStartDate.toLocalDate()..checkInWithManagerEndDate.toLocalDate()

        return this.copy(
            isReviewCycleActive = isReviewCycleActive,
            isSelfReviewActive = isSelfReviewActive,
            isManagerReviewActive = isManagerReviewActive,
            isCheckInWithManagerActive = isCheckInWithManagerActive,
            isSelfReviewDatePassed = organisationCurrentDate > selfReviewEndDate.toLocalDate(),
            isManagerReviewDatePassed = organisationCurrentDate > managerReviewEndDate.toLocalDate(),
            isCheckInWithManagerDatePassed = organisationCurrentDate > checkInWithManagerEndDate.toLocalDate(),
        )
    }
}

data class UserActivityData(
    val actionBy: Long,
    val ipAddress: String,
)

data class ReviewCycleResponse(
    val totalReviewCycles: Int,
    val reviewCycles: List<ReviewCycle>,
)

data class ActiveReviewCycle(
    val reviewCycleId: Long,
    val startDate: Date,
    val endDate: Date,
    val selfReviewStartDate: Date,
    val selfReviewEndDate: Date,
    val draft: Boolean?,
    val publish: Boolean?,
    val updatedAt: Timestamp?,
    val averageRating: BigDecimal?,
    val isReviewCyclePublish: Boolean,
    val isReviewCycleActive: Boolean = false,
    val isSelfReviewActive: Boolean = false,
    val isSelfReviewDatePassed: Boolean,
) {
    fun withActiveFlags(organisationTimeZone: String): ActiveReviewCycle {
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        val isReviewCycleActive =
            isReviewCyclePublish &&
                organisationCurrentDate in startDate.toLocalDate()..endDate.toLocalDate()

        val isSelfReviewActive =
            isReviewCyclePublish &&
                organisationCurrentDate in selfReviewStartDate.toLocalDate()..selfReviewEndDate.toLocalDate()

        return this.copy(
            isReviewCycleActive = isReviewCycleActive,
            isSelfReviewActive = isSelfReviewActive,
            isSelfReviewDatePassed = organisationCurrentDate > selfReviewEndDate.toLocalDate(),
        )
    }
}

data class ActiveReviewCycleResponse(
    val totalReviewCycles: Int,
    val reviewCycles: List<ActiveReviewCycle>,
)

data class ManagerReviewCycleData(
    val reviewCycleId: Long,
    val startDate: Date,
    val endDate: Date,
    val managerReviewStartDate: Date,
    val managerReviewEndDate: Date,
    val team: String,
    val reviewToId: Long,
    val reviewToEmployeeId: String,
    val firstName: String,
    val lastName: String,
    val draft: Boolean?,
    val publish: Boolean?,
    val averageRating: BigDecimal?,
    val isReviewCyclePublish: Boolean,
    val isReviewCycleActive: Boolean = false,
    val isManagerReviewActive: Boolean = false,
    val isManagerReviewDatePassed: Boolean,
) {
    fun withActiveFlags(organisationTimeZone: String): ManagerReviewCycleData {
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        val isReviewCycleActive =
            isReviewCyclePublish &&
                organisationCurrentDate in startDate.toLocalDate()..endDate.toLocalDate()

        val isManagerReviewActive =
            isReviewCyclePublish &&
                organisationCurrentDate in managerReviewStartDate.toLocalDate()..managerReviewEndDate.toLocalDate()

        return this.copy(
            isReviewCycleActive = isReviewCycleActive,
            isManagerReviewActive = isManagerReviewActive,
            isManagerReviewDatePassed = organisationCurrentDate > managerReviewEndDate.toLocalDate(),
        )
    }
}

data class MyManagerReviewCycleData(
    val reviewCycleId: Long,
    val startDate: Date,
    val endDate: Date,
    val managerReviewStartDate: Date,
    val managerReviewEndDate: Date,
    val team: String,
    val reviewToId: Long,
    val reviewToEmployeeId: String,
    val firstName: String,
    val lastName: String,
    val reviewFromId: Long?,
    val reviewFromEmployeeId: String?,
    val managerFirstName: String?,
    val managerLastName: String?,
    val draft: Boolean?,
    val publish: Boolean?,
    val isReviewCyclePublish: Boolean,
    val averageRating: BigDecimal?,
    val isReviewCycleActive: Boolean = false,
    val isManagerReviewActive: Boolean = false,
    val isManagerReviewDatePassed: Boolean,
) {
    fun withActiveFlags(organisationTimeZone: String): MyManagerReviewCycleData {
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        val isReviewCycleActive =
            isReviewCyclePublish &&
                organisationCurrentDate in startDate.toLocalDate()..endDate.toLocalDate()

        val isManagerReviewActive =
            isReviewCyclePublish &&
                organisationCurrentDate in managerReviewStartDate.toLocalDate()..managerReviewEndDate.toLocalDate()

        return this.copy(
            isReviewCycleActive = isReviewCycleActive,
            isManagerReviewActive = isManagerReviewActive,
            isManagerReviewDatePassed = organisationCurrentDate > managerReviewEndDate.toLocalDate(),
        )
    }
}

data class MyManagerReviewCycleResponse(
    val totalManagerReviewCycles: Int,
    val myManagerReviewCycles: List<MyManagerReviewCycleData>,
)

data class ManagerReviewCycleResponse(
    val totalManagerReviewCycles: Int,
    val managerReviewCycles: List<ManagerReviewCycleData>,
)

data class CheckInWithManagerData(
    val reviewCycleId: Long,
    val startDate: Date,
    val endDate: Date,
    val publish: Boolean,
    val checkInStartDate: Date,
    val checkInEndDate: Date,
    val reviewToId: Long,
    val reviewToEmployeeId: String,
    val firstName: String,
    val lastName: String,
    val selfReviewDraft: Boolean?,
    val selfReviewPublish: Boolean?,
    val selfAverageRating: BigDecimal?,
    val firstManagerReviewDraft: Boolean?,
    val firstManagerReviewPublish: Boolean?,
    val firstManagerAverageRating: BigDecimal?,
    val secondManagerReviewDraft: Boolean?,
    val secondManagerReviewPublish: Boolean?,
    val secondManagerAverageRating: BigDecimal?,
    val checkInFromId: Long?,
    val checkInDraft: Boolean?,
    val checkInPublish: Boolean?,
    val checkInAverageRating: BigDecimal?,
    val firstManagerId: Long?,
    val firstManagerEmployeeId: String?,
    val firstManagerFirstName: String?,
    val firstManagerLastName: String?,
    val secondManagerId: Long?,
    val secondManagerEmployeeId: String?,
    val secondManagerFirstName: String?,
    val secondManagerLastName: String?,
    val isReviewCycleActive: Boolean = false,
    val isCheckInWithManagerActive: Boolean = false,
    val isCheckInWithManagerDatePassed: Boolean,
) {
    fun withActiveFlags(organisationTimeZone: String): CheckInWithManagerData {
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        val isReviewCycleActive =
            publish &&
                organisationCurrentDate in startDate.toLocalDate()..endDate.toLocalDate()

        val isCheckInWithManagerActive =
            publish &&
                organisationCurrentDate in checkInStartDate.toLocalDate()..checkInEndDate.toLocalDate()

        return this.copy(
            isReviewCycleActive = isReviewCycleActive,
            isCheckInWithManagerActive = isCheckInWithManagerActive,
            isCheckInWithManagerDatePassed = organisationCurrentDate > checkInEndDate.toLocalDate(),
        )
    }
}

data class CheckInWithManager(
    val totalCheckInWithManager: Int,
    val checkInWithManagers: List<CheckInWithManagerData>,
)

data class ReviewCycleTimeline(
    val organisationId: Long,
    val reviewCycleId: Long,
    val startDate: Date,
    val endDate: Date,
    val publish: Boolean,
    val selfReviewStartDate: Date,
    val selfReviewEndDate: Date,
    val managerReviewStartDate: Date,
    val managerReviewEndDate: Date,
    val selfReviewDraft: Boolean?,
    val selfReviewPublish: Boolean?,
    val selfReviewDate: Timestamp?,
    val selfAverageRating: BigDecimal?,
    val firstManagerId: Long?,
    val firstManagerEmployeeId: String?,
    val firstManagerFirstName: String?,
    val firstManagerLastName: String?,
    val firstManagerReviewDraft: Boolean?,
    val firstManagerReviewPublish: Boolean?,
    val firstManagerReviewDate: Timestamp?,
    val firstManagerAverageRating: BigDecimal?,
    val secondManagerId: Long?,
    val secondManagerEmployeeId: String?,
    val secondManagerFirstName: String?,
    val secondManagerLastName: String?,
    val secondManagerReviewDraft: Boolean?,
    val secondManagerReviewPublish: Boolean?,
    val secondManagerReviewDate: Timestamp?,
    val secondManagerAverageRating: BigDecimal?,
    val checkInFromId: Long?,
    val checkInFromEmployeeId: String?,
    val checkInFromFirstName: String?,
    val checkInFromLastName: String?,
    val checkInWithManagerStartDate: Date,
    val checkInWithManagerEndDate: Date,
    val checkInWithManagerDraft: Boolean?,
    val checkInWithManagerPublish: Boolean?,
    val checkInWithManagerDate: Timestamp?,
    val checkInWithManagerAverageRating: BigDecimal?,
    val isOrWasManager: Boolean?,
    val empDetails: List<EmployeeReviewDetails>?,
    val isReviewCycleActive: Boolean = false,
    val isSelfReviewActive: Boolean = false,
    val isManagerReviewActive: Boolean = false,
    val isCheckInWithManagerActive: Boolean = false,
    val isSelfReviewDatePassed: Boolean,
    val isManagerReviewDatePassed: Boolean,
    val isCheckInWithManagerDatePassed: Boolean,
) {
    fun withActiveFlags(organisationTimeZone: String): ReviewCycleTimeline {
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        val isReviewCycleActive =
            publish &&
                organisationCurrentDate in startDate.toLocalDate()..endDate.toLocalDate()

        val isSelfReviewActive =
            publish &&
                organisationCurrentDate in selfReviewStartDate.toLocalDate()..selfReviewEndDate.toLocalDate()

        val isManagerReviewActive =
            publish &&
                organisationCurrentDate in managerReviewStartDate.toLocalDate()..managerReviewEndDate.toLocalDate()

        val isCheckInWithManagerActive =
            publish &&
                organisationCurrentDate in checkInWithManagerStartDate.toLocalDate()..checkInWithManagerEndDate.toLocalDate()

        return this.copy(
            isReviewCycleActive = isReviewCycleActive,
            isSelfReviewActive = isSelfReviewActive,
            isManagerReviewActive = isManagerReviewActive,
            isCheckInWithManagerActive = isCheckInWithManagerActive,
            isSelfReviewDatePassed = organisationCurrentDate > selfReviewEndDate.toLocalDate(),
            isManagerReviewDatePassed = organisationCurrentDate > managerReviewEndDate.toLocalDate(),
            isCheckInWithManagerDatePassed = organisationCurrentDate > checkInWithManagerEndDate.toLocalDate(),
        )
    }
}

data class EmployeeReviewDetails(
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val checkInFromId: Long?,
    val firstManagerId: Long?,
    val secondManagerId: Long?,
    val selfReviewDraft: Boolean?,
    val selfReviewPublish: Boolean?,
    val selfReviewDate: Date?,
    val firstManagerReviewDraft: Boolean?,
    val firstManagerReviewPublish: Boolean?,
    val firstManagerReviewDate: Date?,
    val secondManagerReviewDraft: Boolean?,
    val secondManagerReviewPublish: Boolean?,
    val secondManagerReviewDate: Date?,
    val checkInFromEmployeeId: String?,
    val checkInFromFirstName: String?,
    val checkInFromLastName: String?,
    val checkInWithManagerDraft: Boolean?,
    val checkInWithManagerPublish: Boolean?,
    val checkInWithManagerDate: Date?,
)

data class CheckInWithManagerParams(
    val organisationId: Long,
    val managerId: List<Int>,
    val reviewCycleId: List<Int>,
    val reviewToId: List<Int>,
    val teamId: List<Int>,
    val selfReviewDraft: Boolean?,
    val selfReviewPublish: Boolean?,
    val firstManagerReviewDraft: Boolean?,
    val firstManagerReviewPublish: Boolean?,
    val secondManagerReviewDraft: Boolean?,
    val secondManagerReviewPublish: Boolean?,
    val checkInDraft: Boolean?,
    val checkInPublished: Boolean?,
    val minFilterRating: Double = -1.0,
    val maxFilterRating: Double = 5.0,
    val filterRatingId: Int = -1,
) {
    fun updateRatingsRange() =
        when (filterRatingId) {
            1 -> this.copy(minFilterRating = 1.0, maxFilterRating = 1.9)
            2 -> this.copy(minFilterRating = 2.0, maxFilterRating = 2.9)
            3 -> this.copy(minFilterRating = 3.0, maxFilterRating = 3.9)
            4 -> this.copy(minFilterRating = 4.0, maxFilterRating = 4.9)
            5 -> this.copy(minFilterRating = 5.0, maxFilterRating = 5.0)
            else -> {
                this.copy(minFilterRating = -1.0, maxFilterRating = 5.0)
            }
        }
}

data class StartedReviewCycle(
    val exists: Boolean,
    val id: Long?,
)

data class ReviewCycleDates(
    val id: Long,
    val startDate: Date,
    val endDate: Date,
)
