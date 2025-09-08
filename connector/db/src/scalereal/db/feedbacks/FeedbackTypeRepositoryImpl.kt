package scalereal.db.feedbacks

import feedbacks.GetFeedbackTypesParams
import feedbacks.GetFeedbackTypesQuery
import feedbacks.GetFeedbackTypesResult
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.query
import scalereal.core.feedbacks.FeedbackTypeRepository
import scalereal.core.models.domain.FeedbackType
import javax.sql.DataSource

@Singleton
class FeedbackTypeRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : FeedbackTypeRepository {
    override fun fetchAll(): List<FeedbackType> =
        dataSource.connection.use { connection ->
            GetFeedbackTypesQuery()
                .query(connection, GetFeedbackTypesParams())
                .map { it.toTag() }
        }

    private fun GetFeedbackTypesResult.toTag() =
        FeedbackType(
            feedbackTypeId = id,
            feedbackType = name,
        )
}
