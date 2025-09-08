package scalereal.core.feedbacks

import scalereal.core.models.domain.FeedbackType

interface FeedbackTypeRepository {
    fun fetchAll(): List<FeedbackType>
}
