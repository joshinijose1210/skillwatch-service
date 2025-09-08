package scalereal.core.feedbacks

import jakarta.inject.Singleton
import scalereal.core.models.domain.FeedbackType

@Singleton
class FeedbackTypeService(
    private val repository: FeedbackTypeRepository,
) {
    fun fetchAll(): List<FeedbackType> = repository.fetchAll()
}
