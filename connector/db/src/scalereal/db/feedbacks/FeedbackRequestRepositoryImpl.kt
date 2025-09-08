package scalereal.db.feedbacks

import feedbacks.AddExternalEmailForFeedbackRequestParams
import feedbacks.AddExternalEmailForFeedbackRequestQuery
import feedbacks.AddExternalFeedbackParams
import feedbacks.AddExternalFeedbackQuery
import feedbacks.AddExternalFeedbackRequestParams
import feedbacks.AddExternalFeedbackRequestQuery
import feedbacks.AddRequestFeedbackParams
import feedbacks.AddRequestFeedbackQuery
import feedbacks.GetExternalFeedbackRequestDataParams
import feedbacks.GetExternalFeedbackRequestDataQuery
import feedbacks.GetFeedbackRequestDataCountParams
import feedbacks.GetFeedbackRequestDataCountQuery
import feedbacks.GetFeedbackRequestDataParams
import feedbacks.GetFeedbackRequestDataQuery
import feedbacks.GetFeedbackRequestDataResult
import feedbacks.GetFeedbackRequestDetailsParams
import feedbacks.GetFeedbackRequestDetailsQuery
import feedbacks.GetFetchFeedbackByRequestIdParams
import feedbacks.GetFetchFeedbackByRequestIdQuery
import feedbacks.GetPendingFeedbackRequestParams
import feedbacks.GetPendingFeedbackRequestQuery
import feedbacks.UpdateFeedbackRequestStatusCommand
import feedbacks.UpdateFeedbackRequestStatusParams
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.feedbacks.FeedbackRequestRepository
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.ExternalFeedbackRequestData
import scalereal.core.models.domain.FeedbackDetail
import scalereal.core.models.domain.FeedbackRequest
import scalereal.core.models.domain.FeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestParams
import scalereal.core.models.domain.PendingFeedbackRequestDetails
import javax.sql.DataSource

@Singleton
class FeedbackRequestRepositoryImpl(
    @Inject private val datasource: DataSource,
) : FeedbackRequestRepository {
    override fun fetchFeedbackRequestData(
        feedbackRequestParams: FeedbackRequestParams,
        offset: Int,
        limit: Int,
    ): List<FeedbackRequestData> =
        datasource.connection.use { connection ->
            GetFeedbackRequestDataQuery()
                .query(
                    connection,
                    GetFeedbackRequestDataParams(
                        organisationId = feedbackRequestParams.organisationId,
                        requestedById = feedbackRequestParams.requestedById.toTypedArray(),
                        feedbackFromId = feedbackRequestParams.feedbackFromId.toTypedArray(),
                        feedbackToId = feedbackRequestParams.feedbackToId.toTypedArray(),
                        isSubmitted = feedbackRequestParams.isSubmitted.toTypedArray(),
                        reviewCycleId = feedbackRequestParams.reviewCycleId.toTypedArray(),
                        limit = limit,
                        offset = offset,
                        sortBy = feedbackRequestParams.sortBy,
                    ),
                ).map { it.toFeedbackRequestData() }
        }

    override fun countFeedbackRequestData(feedbackRequestParams: FeedbackRequestParams): Int =
        datasource.connection.use { connection ->
            GetFeedbackRequestDataCountQuery()
                .query(
                    connection,
                    GetFeedbackRequestDataCountParams(
                        organisationId = feedbackRequestParams.organisationId,
                        requestedById = feedbackRequestParams.requestedById.toTypedArray(),
                        feedbackFromId = feedbackRequestParams.feedbackFromId.toTypedArray(),
                        feedbackToId = feedbackRequestParams.feedbackToId.toTypedArray(),
                        isSubmitted = feedbackRequestParams.isSubmitted.toTypedArray(),
                        reviewCycleId = feedbackRequestParams.reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .feedbackRequestCount
                ?.toInt() ?: 0
        }

    override fun updateFeedbackRequestStatus(requestId: Long) {
        datasource.connection.use { connection ->
            UpdateFeedbackRequestStatusCommand()
                .command(connection, UpdateFeedbackRequestStatusParams(requestId = requestId))
        }
    }

    override fun addInternalFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromId: List<Long>,
        goalId: Long?,
        request: String,
    ): Unit =
        datasource.connection.use { connection ->
            feedbackToId
                .flatMap { toId ->
                    feedbackFromId.map { fromId -> toId to fromId }
                }.forEach { (toId, fromId) ->
                    AddRequestFeedbackQuery()
                        .query(
                            connection,
                            AddRequestFeedbackParams(
                                requestedBy = requestedBy,
                                feedbackToId = toId,
                                feedbackFromId = fromId,
                                request = request,
                                goalId = goalId,
                            ),
                        )
                }
        }

    override fun fetchFeedbackRequestDetails(requestId: Long): FeedbackRequest =
        datasource.connection.use { connection ->
            GetFeedbackRequestDetailsQuery()
                .query(connection, GetFeedbackRequestDetailsParams(requestId = requestId))
                .map {
                    FeedbackRequest(
                        id = requestId,
                        requestedById = it.requestedById,
                        feedbackToId = it.feedbackToId,
                        feedbackFromId = it.feedbackFromId,
                        goalId = it.goalId,
                        goalDescription = it.goalDescription,
                        request = it.request,
                        createdAt = it.requestedOn,
                        isSubmitted = it.isSubmitted,
                        requestedByEmployeeId = it.requestedByEmployeeId,
                        requestedByFirstName = it.requestedByFirstName,
                        requestedByLastName = it.requestedByLastName,
                        feedbackToEmployeeId = it.feedbackToEmployeeId,
                        feedbackToFirstName = it.feedbackToFirstName,
                        feedbackToLastName = it.feedbackToLastName,
                        feedbackFromEmployeeId = it.feedbackFromEmployeeId,
                        feedbackFromFirstName = it.feedbackFromFirstName,
                        feedbackFromLastName = it.feedbackFromLastName,
                        isExternalRequest = it.isExternalRequest,
                        externalFeedbackFromEmail = it.externalFeedbackFromEmail,
                    )
                }.first()
        }

    override fun addExternalFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromId: List<Long>,
        request: String,
        isExternalRequest: Boolean,
    ): List<Long> {
        val requestId = mutableListOf<Long>()
        datasource.connection.use { connection ->
            feedbackToId
                .flatMap { toId ->
                    feedbackFromId.map { fromId -> toId to fromId }
                }.forEach { (toId, fromId) ->
                    val result =
                        AddExternalFeedbackRequestQuery()
                            .query(
                                connection,
                                AddExternalFeedbackRequestParams(
                                    requestedBy = requestedBy,
                                    feedbackToId = toId,
                                    feedbackFromId = fromId,
                                    request = request,
                                    isExternalRequest = isExternalRequest,
                                ),
                            )
                    requestId.add(result[0].id)
                }
        }
        return requestId
    }

    override fun addExternalEmails(
        feedbackFromEmail: List<String>,
        organisationId: Long,
    ): List<Long> =
        datasource.connection.use { connection ->
            feedbackFromEmail.map { email ->
                AddExternalEmailForFeedbackRequestQuery()
                    .query(
                        connection,
                        AddExternalEmailForFeedbackRequestParams(
                            organisationId = organisationId,
                            emailId = email,
                        ),
                    ).map { it.id }
                    .single()
            }
        }

    override fun getExternalFeedbackRequestData(requestId: Long): ExternalFeedbackRequestData? =
        datasource.connection.use { connection ->
            GetExternalFeedbackRequestDataQuery()
                .query(connection, GetExternalFeedbackRequestDataParams(requestId = requestId))
                .map {
                    it.feedbackFromId?.let { it1 ->
                        ExternalFeedbackRequestData(
                            requestId = it.requestId,
                            request = it.request,
                            requestedById = it.requestedById,
                            requestedByFirstName = it.requestedByFirstName,
                            requestedByLastName = it.requestedByLastName,
                            feedbackToId = it.feedbackToId,
                            feedbackToFirstName = it.feedbackToFirstName,
                            feedbackToLastName = it.feedbackToLastName,
                            feedbackToTeam = it.feedbackToTeam,
                            feedbackFromId = it1,
                            feedbackFromEmail = it.feedbackFromEmail,
                            organisationName = it.organisationName,
                        )
                    }
                }.firstOrNull()
        }

    override fun addExternalFeedback(
        feedback: List<AddFeedbackData>,
        feedbackToId: Long,
        feedbackFromId: Long,
        requestId: Long,
    ) {
        datasource.connection.use { connection ->
            feedback.forEach {
                AddExternalFeedbackQuery()
                    .query(
                        connection,
                        AddExternalFeedbackParams(
                            feedback = it.feedbackText,
                            feedbackToId = feedbackToId,
                            feedbackFromId = feedbackFromId,
                            feedbackTypeId = it.feedbackTypeId,
                            requestId = requestId,
                        ),
                    )
            }
        }
    }

    override fun getPendingFeedbackRequest(): List<PendingFeedbackRequestDetails> =
        datasource.connection.use { connection ->
            GetPendingFeedbackRequestQuery()
                .query(connection, GetPendingFeedbackRequestParams())
                .map {
                    PendingFeedbackRequestDetails(
                        id = it.id,
                        isExternalRequest = it.isExternalRequest,
                        requestedById = it.requestedById,
                        requestedByEmpId = it.requestedByEmpId,
                        requestedByFirstName = it.requestedByFirstName,
                        requestedByLastName = it.requestedByLastName,
                        feedbackFromId = it.feedbackFromId,
                        feedbackFromFirstName = it.feedbackFromFirstName,
                        feedbackFromLastName = it.feedbackFromLastName,
                        feedbackFromEmailId = it.feedbackFromEmailId,
                        externalFeedbackFromEmailId = it.externalFeedbackFromEmailId,
                        date = it.date!!,
                        organisationName = it.organisationName,
                        organisationTimeZone = it.organisationTimeZone,
                    )
                }
        }

    override fun fetchFeedbackByRequestId(requestId: Long): List<FeedbackDetail> =
        datasource.connection.use { connection ->
            GetFetchFeedbackByRequestIdQuery()
                .query(connection, GetFetchFeedbackByRequestIdParams(request_id = requestId))
                .map {
                    FeedbackDetail(
                        feedbackId = it.srNo,
                        feedback = it.feedback,
                        feedbackTypeId = it.feedbackTypeId,
                        feedbackType = it.feedbackType,
                        isDraft = it.isDraft,
                    )
                }
        }
}

private fun GetFeedbackRequestDataResult.toFeedbackRequestData() =
    FeedbackRequestData(
        organisationId = organisationId,
        requestId = requestId,
        requestedOn = requestedOn,
        isSubmitted = isSubmitted,
        isExternalRequest = isExternalRequest,
        requestedById = requestedById,
        requestedByEmployeeId = requestedByEmployeeId,
        requestedByFirstName = requestedByFirstName,
        requestedByLastName = requestedByLastName,
        feedbackToId = feedbackToId,
        feedbackToEmployeeId = feedbackToEmployeeId,
        feedbackToFirstName = feedbackToFirstName,
        feedbackToLastName = feedbackToLastName,
        feedbackFromId = feedbackFromId,
        feedbackFromEmployeeId = feedbackFromEmployeeId,
        feedbackFromFirstName = feedbackFromFirstName,
        feedbackFromLastName = feedbackFromLastName,
        externalFeedbackFromEmail = externalFeedbackFromEmail,
        isDraft = isDraft ?: false,
    )
