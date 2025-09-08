package scalereal.core.analytics

import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.feedbacks.FeedbackRepository
import scalereal.core.models.Constants
import scalereal.core.models.domain.AnalyticsFeedbackCount
import scalereal.core.models.domain.AnalyticsFeedbackPercentage
import scalereal.core.models.domain.AnalyticsFeedbackResponse
import scalereal.core.models.domain.AverageAge
import scalereal.core.models.domain.AverageTenure
import scalereal.core.models.domain.EmployeeHistory
import scalereal.core.models.domain.EmployeesType
import scalereal.core.models.domain.EmployeesTypeData
import scalereal.core.models.domain.ExperienceRange
import scalereal.core.models.domain.GendersCount
import scalereal.core.models.domain.GendersData
import scalereal.core.models.domain.GendersPercentage
import scalereal.core.models.domain.RatingTypeRange
import scalereal.core.models.domain.Ratings
import scalereal.core.models.domain.RatingsData
import scalereal.core.models.domain.ReviewCount
import scalereal.core.models.domain.ReviewStatus
import scalereal.core.models.domain.TeamEmployeeCount
import scalereal.core.reviewCycle.ReviewCycleService
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
class AnalyticsService(
    private val analyticsRepository: AnalyticsRepository,
    private val employeeService: EmployeeService,
    private val reviewCycleService: ReviewCycleService,
    private val feedbackRepository: FeedbackRepository,
) {
    fun getRatings(
        organisationId: Long,
        reviewCycleId: Long,
    ): Ratings {
        val ratingsData: List<RatingsData> = analyticsRepository.getRatings(organisationId, reviewCycleId)
        val ratings: List<Double> = ratingsData.map { it.checkInRating }
        var unsatisfactory = 0
        var needsImprovement = 0
        var meetsExpectations = 0
        var exceedExpectations = 0
        var outstanding = 0

        for (rating in ratings) {
            when (rating) {
                in 0.00..1.9999 -> unsatisfactory++
                in 2.00..2.9999 -> needsImprovement++
                in 3.00..3.9999 -> meetsExpectations++
                in 4.00..4.9999 -> exceedExpectations++
                5.00 -> outstanding++
            }
        }

        return Ratings(
            unsatisfactory = unsatisfactory,
            needsImprovement = needsImprovement,
            meetsExpectations = meetsExpectations,
            exceedsExpectations = exceedExpectations,
            outstanding = outstanding,
        )
    }

    fun getRatingListing(
        organisationId: Long,
        reviewCycleId: Long,
        ratingType: String,
        employeeId: List<Int>,
        page: Int,
        limit: Int,
    ): List<RatingsData> {
        val ratingTypeRange = ratingTypeRange(ratingType)
        return analyticsRepository.getRatings(
            organisationId,
            reviewCycleId,
            ratingTypeRange.minRange,
            ratingTypeRange.maxRange,
            employeeId,
            offset = (page - 1) * limit,
            limit,
        )
    }

    fun getRatingListingCount(
        organisationId: Long,
        reviewCycleId: Long,
        ratingType: String,
        employeeId: List<Int>,
    ): Int {
        val ratingTypeRange = ratingTypeRange(ratingType)
        return analyticsRepository.getRatingListingCount(
            organisationId,
            reviewCycleId,
            ratingTypeRange.minRange,
            ratingTypeRange.maxRange,
            employeeId,
        )
    }

    private fun ratingTypeRange(ratingType: String): RatingTypeRange {
        var minRange: BigDecimal? = null
        var maxRange: BigDecimal? = null
        when (ratingType) {
            "unsatisfactory" -> {
                minRange = BigDecimal(Constants.UNSATISFACTORY_MIN_RANGE)
                maxRange = BigDecimal(Constants.UNSATISFACTORY_MAX_RANGE)
            }
            "needsImprovement" -> {
                minRange = BigDecimal(Constants.NEEDS_IMPROVEMENT_MIN_RANGE)
                maxRange = BigDecimal(Constants.NEEDS_IMPROVEMENT_MAX_RANGE)
            }
            "meetsExpectations" -> {
                minRange = BigDecimal(Constants.MEETS_EXPECTATIONS_MIN_RANGE)
                maxRange = BigDecimal(Constants.MEETS_EXPECTATIONS_MAX_RANGE)
            }
            "exceedsExpectations" -> {
                minRange = BigDecimal(Constants.EXCEEDS_EXPECTATIONS_MIN_RANGE)
                maxRange = BigDecimal(Constants.EXCEEDS_EXPECTATIONS_MAX_RANGE)
            }
            "outstanding" -> {
                minRange = BigDecimal(Constants.OUTSTANDING_MIN_RANGE)
                maxRange = BigDecimal(Constants.OUTSTANDING_MAX_RANGE)
            }
        }
        return RatingTypeRange(minRange, maxRange)
    }

    fun getRankings(
        organisationId: Long,
        reviewCycleId: Long,
    ): List<RatingsData> {
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val ratings =
            analyticsRepository
                .getRatings(organisationId, reviewCycleId)
                .filter { it.checkInRating != -1.00 }
                .sortedWith(
                    compareByDescending<RatingsData> { it.checkInRating }
                        .thenByDescending {
                            feedbackRepository
                                .fetchEmployeeFeedbackCounts(
                                    it.id,
                                    reviewCycle.startDate,
                                    reviewCycle.endDate,
                                ).receivedAppreciationCount
                        }.thenByDescending {
                            feedbackRepository
                                .fetchEmployeeFeedbackCounts(
                                    it.id,
                                    reviewCycle.startDate,
                                    reviewCycle.endDate,
                                ).receivedPositiveCount
                        }.thenBy {
                            feedbackRepository
                                .fetchEmployeeFeedbackCounts(
                                    it.id,
                                    reviewCycle.startDate,
                                    reviewCycle.endDate,
                                ).receivedImprovementCount
                        },
                ).take(5) // top 5 employees

        return ratings.toList()
    }

    fun getReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
    ): ReviewStatus {
        val activeEmployees = employeeService.fetchActiveEmployeesCountDuringReviewCycle(organisationId, reviewCycleId)
        val selfReviewStatus = getSelfReviewStatus(organisationId, reviewCycleId, activeEmployees)
        val manager1ReviewStatus = getManager1ReviewStatus(organisationId, reviewCycleId, activeEmployees)
        val manager2ReviewStatus = getManager2ReviewStatus(organisationId, reviewCycleId, activeEmployees)
        val checkInReviewStatus = getCheckInReviewStatus(organisationId, reviewCycleId, activeEmployees)

        return ReviewStatus(
            self = selfReviewStatus,
            manager1 = manager1ReviewStatus,
            manager2 = manager2ReviewStatus,
            checkIn = checkInReviewStatus,
        )
    }

    fun getFeedbackGraphData(
        organisationId: Long,
        reviewCycleId: Long,
    ): AnalyticsFeedbackResponse {
        val reviewCycleDates = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val startDate = reviewCycleDates.startDate
        val endDate = reviewCycleDates.endDate
        val feedbackCount = feedbackRepository.fetchTotalFeedbackCounts(organisationId, startDate, endDate)
        return AnalyticsFeedbackResponse(
            analyticsFeedbackCount = feedbackCount,
            analyticsFeedbackPercentage = getFeedbackPercentage(feedbackCounts = feedbackCount),
        )
    }

    fun getGendersData(
        organisationId: Long,
        reviewCycleId: Long,
    ): GendersData {
        val employeesId = employeeService.fetchActiveEmployeesDuringReviewCycle(organisationId, reviewCycleId)
        var malesCount = 0
        var femalesCount = 0
        var othersCount = 0
        for (employeeId in employeesId) {
            val employee = employeeService.getEmployeeById(employeeId.toLong())
            when (employee.genderId) {
                1 -> malesCount++
                2 -> femalesCount++
                3 -> othersCount++
                null -> othersCount++
            }
        }
        val totalEmployees = malesCount + femalesCount + othersCount
        val malesPercentage = if (totalEmployees != 0)(malesCount.toDouble() / totalEmployees) * 100 else 0.00
        val femalesPercentage = if (totalEmployees != 0) (femalesCount.toDouble() / totalEmployees) * 100 else 0.00
        val othersPercentage = if (totalEmployees != 0) (othersCount.toDouble() / totalEmployees) * 100 else 0.00

        return GendersData(
            GendersCount(malesCount, femalesCount, othersCount),
            GendersPercentage(malesPercentage, femalesPercentage, othersPercentage),
        )
    }

    fun getAverageTenure(
        organisationId: Long,
        reviewCycleId: Long,
    ): AverageTenure {
        val employeesId = employeeService.fetchActiveEmployeesDuringReviewCycle(organisationId, reviewCycleId)
        val employeesHistory = employeeService.getEmployeesHistory(employeesId)
        val totalDays = getTotalActiveDays(reviewCycleId, employeesHistory, employeesId)
        return if (employeesHistory.isNotEmpty()) {
            val averageDays = totalDays.toDouble() / employeesId.count()
            val averageYears = (averageDays / 365.25).toInt()
            val remainingDays = averageDays % 365.25
            val averageMonths = (remainingDays / 30.44).toInt()

            AverageTenure(
                years = averageYears,
                months = averageMonths,
            )
        } else {
            AverageTenure(
                years = 0,
                months = 0,
            )
        }
    }

    fun getAverageAge(
        organisationId: Long,
        reviewCycleId: Long,
    ): AverageAge {
        val employeesId = employeeService.fetchActiveEmployeesDuringReviewCycle(organisationId, reviewCycleId)
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)

        var totalEmployees = 0
        var totalAgeInDays = 0L

        for (employeeId in employeesId) {
            val employee = employeeService.getEmployeeById(employeeId.toLong())
            val dob = employee.dateOfBirth?.toLocalDate()
            if (dob != null) {
                val ageInDays = ChronoUnit.DAYS.between(dob, reviewCycle.endDate.toLocalDate())
                totalAgeInDays += ageInDays
                totalEmployees++
            }
        }

        return if (totalEmployees > 0) {
            val averageAgeInDays = totalAgeInDays.toDouble() / totalEmployees
            val averageAgeInYears = (averageAgeInDays / 365.25).toInt()
            val remainingDays = averageAgeInDays % 365.25
            val averageAgeInMonths = (remainingDays / 30.44).toInt()
            return AverageAge(years = averageAgeInYears, months = averageAgeInMonths)
        } else {
            AverageAge(years = 0, months = 0)
        }
    }

    fun getEmployeeCountByTotalExperience(
        organisationId: Long,
        reviewCycleId: Long,
    ): List<ExperienceRange> {
        val employeesId = employeeService.fetchActiveEmployeesDuringReviewCycle(organisationId, reviewCycleId)
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val reviewCycleEndDate = reviewCycle.endDate.toLocalDate()
        val now = LocalDateTime.now()

        val experienceRanges =
            mutableMapOf(
                "0-1 year" to 0,
                "1-3 years" to 0,
                "3-7 years" to 0,
                "7-10 years" to 0,
                "10-15 years" to 0,
                "15-20 years" to 0,
                "20+ years" to 0,
            )

        for (employeeId in employeesId) {
            val employee = employeeService.getEmployeeById(employeeId.toLong())
            val employeeHistory = employeeService.getEmployeesHistory(listOf(employeeId)).maxBy { it.historyId }

            val totalExperience: Long = employee.experienceInMonths?.toLong() ?: 0L
            val deactivatedAt = employeeHistory.deactivatedAt?.toLocalDateTime()

            val experienceInMonths: Long =
                when {
                    deactivatedAt != null && deactivatedAt > reviewCycleEndDate.atTime(23, 59, 59) -> {
                        val extraMonths = ChronoUnit.MONTHS.between(reviewCycleEndDate, deactivatedAt)
                        totalExperience - extraMonths
                    }
                    deactivatedAt == null && now > reviewCycleEndDate.atTime(23, 59, 59) -> {
                        val extraMonths = ChronoUnit.MONTHS.between(reviewCycleEndDate, now)
                        totalExperience - extraMonths
                    }
                    else -> totalExperience
                }

            val experienceInYears = experienceInMonths / 12

            val range =
                when {
                    experienceInYears < 1 -> "0-1 year"
                    experienceInYears < 3 -> "1-3 years"
                    experienceInYears < 7 -> "3-7 years"
                    experienceInYears < 10 -> "7-10 years"
                    experienceInYears < 15 -> "10-15 years"
                    experienceInYears < 20 -> "15-20 years"
                    else -> "20+ years"
                }

            experienceRanges[range] = experienceRanges.getValue(range) + 1
        }

        return experienceRanges.map { ExperienceRange(it.key, it.value) }
    }

    fun getEmployeesType(
        organisationId: Long,
        reviewCycleId: Long,
    ): EmployeesType {
        val employeesId = employeeService.fetchActiveEmployeesDuringReviewCycle(organisationId, reviewCycleId)
        val employees = employeesId.map { employeeService.getEmployeeById(it.toLong()) }
        val totalCount = employees.size
        val fullTimeCount = employees.count { !it.isConsultant }
        val consultantCount = employees.count { it.isConsultant }

        val fullTimePercentage = if (totalCount != 0)(fullTimeCount.toDouble() / totalCount) * 100 else 0.00
        val consultantPercentage = if (totalCount != 0)(consultantCount.toDouble() / totalCount) * 100 else 0.00

        return EmployeesType(
            fullTime = EmployeesTypeData(count = fullTimeCount, percentage = fullTimePercentage),
            consultant = EmployeesTypeData(count = consultantCount, percentage = consultantPercentage),
        )
    }

    fun getEmployeesCountInTeamDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): List<TeamEmployeeCount> {
        val employeeIds = employeeService.fetchActiveEmployeesDuringReviewCycle(organisationId, reviewCycleId)
        val teamEmployeeCountMap = mutableMapOf<Long, TeamEmployeeCount>()

        employeeIds.map { it.toLong() }.forEach { employeeId ->
            val teamSummaryMap = employeeService.fetchEmployeeTeamDuringReviewCycle(organisationId, reviewCycleId, employeeId)
            teamSummaryMap.forEach { (teamId, teamName) ->
                val existingCount = teamEmployeeCountMap[teamId]?.employeeCount ?: 0
                teamEmployeeCountMap[teamId] = TeamEmployeeCount(teamName, existingCount + 1)
            }
        }

        return teamEmployeeCountMap.values.toList()
    }

    private fun getTotalActiveDays(
        reviewCycleId: Long,
        employeesHistory: List<EmployeeHistory>,
        employeesId: List<Int>,
    ): Long {
        var totalDays = 0L
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val reviewCycleEndDateTime = reviewCycle.endDate.toLocalDate().atTime(23, 59, 59)
        val now = LocalDateTime.now()
        employeesId.forEach { employeeId ->
            val dateOfJoining = employeeService.getEmployeeById(employeeId.toLong()).dateOfJoining
            val employeeHistory = employeesHistory.filter { it.employeeId == employeeId.toLong() }
            val activeDays =
                employeeHistory.sumOf { employee ->
                    val activatedAt = employee.activatedAt.toLocalDateTime()
                    val deactivatedAt = employee.deactivatedAt?.toLocalDateTime()
                    val startDate = activatedAt.toLocalDate()
                    if (activatedAt < reviewCycleEndDateTime) {
                        val endDate =
                            if (deactivatedAt != null && deactivatedAt > reviewCycleEndDateTime) {
                                reviewCycleEndDateTime
                            } else if (deactivatedAt != null && deactivatedAt < reviewCycleEndDateTime) {
                                deactivatedAt
                            } else if (deactivatedAt == null && now > reviewCycleEndDateTime) {
                                reviewCycleEndDateTime
                            } else if (deactivatedAt == null && now < reviewCycleEndDateTime) {
                                now
                            } else {
                                reviewCycleEndDateTime
                            }
                        ChronoUnit.DAYS.between(startDate, endDate)
                    } else {
                        0
                    }
                }

            val additionalDays =
                dateOfJoining?.let { date ->
                    val firstActiveDay = employeeHistory.minBy { it.historyId }.activatedAt.toLocalDateTime()
                    val doj = date.toLocalDate().atTime(0, 0, 0)
                    if (doj < firstActiveDay) ChronoUnit.DAYS.between(doj, firstActiveDay) else 0
                } ?: 0
            totalDays += activeDays + additionalDays
        }
        return totalDays
    }

    private fun getSelfReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
        activeEmployees: Long,
    ): ReviewCount {
        val selfReviewData = analyticsRepository.getSelfReviewStatus(organisationId, reviewCycleId)
        return ReviewCount(
            completed = selfReviewData.completed,
            inProgress = selfReviewData.inProgress,
            pending = activeEmployees - selfReviewData.completed - selfReviewData.inProgress,
        )
    }

    private fun getManager1ReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
        activeEmployees: Long,
    ): ReviewCount {
        val managerReviewData = analyticsRepository.getManager1ReviewStatus(organisationId, reviewCycleId)
        return ReviewCount(
            completed = managerReviewData.completed,
            inProgress = managerReviewData.inProgress,
            pending = activeEmployees - managerReviewData.completed - managerReviewData.inProgress,
        )
    }

    private fun getManager2ReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
        activeEmployees: Long,
    ): ReviewCount {
        val managerReviewData = analyticsRepository.getManager2ReviewStatus(organisationId, reviewCycleId)
        return ReviewCount(
            completed = managerReviewData.completed,
            inProgress = managerReviewData.inProgress,
            pending = activeEmployees - managerReviewData.completed - managerReviewData.inProgress,
        )
    }

    private fun getCheckInReviewStatus(
        organisationId: Long,
        reviewCycleId: Long,
        activeEmployees: Long,
    ): ReviewCount {
        val checkInReviewData = analyticsRepository.getCheckInReviewStatus(organisationId, reviewCycleId)
        return ReviewCount(
            completed = checkInReviewData.completed,
            inProgress = checkInReviewData.inProgress,
            pending = activeEmployees - checkInReviewData.completed - checkInReviewData.inProgress,
        )
    }

    private fun getFeedbackPercentage(feedbackCounts: AnalyticsFeedbackCount): AnalyticsFeedbackPercentage {
        val totalFeedback = feedbackCounts.positive + feedbackCounts.improvement + feedbackCounts.appreciation

        val positivePercentage = if (totalFeedback.toInt() != 0) calculatePercentage(feedbackCounts.positive, totalFeedback) else 0.00
        val improvementPercentage = if (totalFeedback.toInt() != 0) calculatePercentage(feedbackCounts.improvement, totalFeedback) else 0.00
        val appreciationPercentage =
            if (totalFeedback.toInt() != 0) {
                calculatePercentage(
                    feedbackCounts.appreciation,
                    totalFeedback,
                )
            } else {
                0.00
            }

        return AnalyticsFeedbackPercentage(
            positive = positivePercentage,
            improvement = improvementPercentage,
            appreciation = appreciationPercentage,
        )
    }

    private fun calculatePercentage(
        feedbackCounts: Long,
        totalFeedbackCounts: Long,
    ): Double = ((feedbackCounts.toDouble() / totalFeedbackCounts.toDouble()) * 100)
}
