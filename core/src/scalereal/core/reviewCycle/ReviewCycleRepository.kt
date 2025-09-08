package scalereal.core.reviewCycle

import scalereal.core.models.domain.ActiveReviewCycle
import scalereal.core.models.domain.CheckInWithManagerData
import scalereal.core.models.domain.CheckInWithManagerParams
import scalereal.core.models.domain.EmployeeReviewDetails
import scalereal.core.models.domain.ManagerReviewCycleData
import scalereal.core.models.domain.MyManagerReviewCycleData
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewCycleDates
import scalereal.core.models.domain.ReviewCycleTimeline
import scalereal.core.models.domain.StartedReviewCycle
import java.sql.Date

interface ReviewCycleRepository {
    fun create(reviewCycle: ReviewCycle): ReviewCycle

    fun fetch(
        organisationId: Long,
        offset: Int,
        limit: Int,
    ): List<ReviewCycle>

    fun count(organisationId: Long): Int

    fun update(reviewCycle: ReviewCycle): ReviewCycle

    fun fetchSelfReviewCycle(
        organisationId: Long,
        reviewTypeId: List<Int>,
        reviewToId: List<Int>,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<ActiveReviewCycle>

    fun countSelfReviewCycle(
        organisationId: Long,
        reviewTypeId: List<Int>,
        reviewToId: List<Int>,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int

    fun fetchManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int?,
        reviewToId: List<Int>,
        reviewFromId: Long,
        reviewCycleId: List<Int>,
        managerReviewDraft: Boolean?,
        managerReviewPublished: Boolean?,
        offset: Int,
        limit: Int,
    ): List<ManagerReviewCycleData>

    fun countManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: List<Int>,
        reviewFromId: Long,
        reviewCycleId: List<Int>,
        managerReviewDraft: Boolean?,
        managerReviewPublished: Boolean?,
    ): Int

    fun fetchMyManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int?,
        reviewToId: Long,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<MyManagerReviewCycleData>

    fun countMyManagerReviewCycle(
        organisationId: Long,
        reviewTypeId: Int,
        reviewToId: Long,
        reviewFromId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int

    fun fetchCheckInWithManager(checkInParams: CheckInWithManagerParams): List<CheckInWithManagerData>

    fun countCheckInWithManager(checkInParams: CheckInWithManagerParams): Int

    fun fetchReviewCycleData(
        organisationId: Long,
        reviewToId: Long,
    ): List<ReviewCycleTimeline>

    fun fetchEmployeeReview(
        organisationId: Long,
        reviewCycleId: Long,
        reviewFromId: Long,
    ): List<EmployeeReviewDetails>

    fun fetchReviewCycle(reviewCycleId: Long): ReviewCycle

    fun isReviewCycleStartedAt(
        date: Date,
        organisationId: Long,
    ): StartedReviewCycle

    fun isSelfReviewStartedAt(
        date: Date,
        organisationId: Long,
    ): StartedReviewCycle

    fun isManagerReviewStartedAt(
        date: Date?,
        organisationId: Long,
    ): StartedReviewCycle

    fun isCheckInStartedAt(
        date: Date?,
        organisationId: Long,
    ): StartedReviewCycle

    fun fetchActiveReviewCycle(organisationId: Long): ReviewCycle?

    fun unPublishReviewCycle(reviewCycleId: Long): Any

    fun isReviewSubmissionStarted(
        organisationId: Long,
        date: Date,
    ): Boolean

    fun getPreviousReviewCycleId(organisationId: Long): List<Long>

    fun getReviewCycles(
        organisationId: Long,
        reviewCycleId: Long,
        numberOfCycles: Long,
    ): List<ReviewCycleDates>
}
