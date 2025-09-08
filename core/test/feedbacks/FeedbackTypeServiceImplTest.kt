package feedbacks

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import scalereal.core.feedbacks.FeedbackTypeRepository
import scalereal.core.feedbacks.FeedbackTypeService
import scalereal.core.models.domain.FeedbackType

class FeedbackTypeServiceImplTest : StringSpec() {
    private val feedbackTypeRepository = mockk<FeedbackTypeRepository>()
    private val service = FeedbackTypeService(feedbackTypeRepository)

    init {
        "should get all feedback types" {

            val feedbackTypes =
                listOf(
                    FeedbackType(feedbackTypeId = 1, feedbackType = "Positive Feedback"),
                    FeedbackType(feedbackTypeId = 2, feedbackType = "Negative Feedback"),
                )
            every { feedbackTypeRepository.fetchAll() } returns feedbackTypes
            service.fetchAll() shouldBe feedbackTypes
            coVerify { feedbackTypeRepository.fetchAll() }
        }

        "should show empty list as true" {
            every { feedbackTypeRepository.fetchAll().isEmpty() } returns true
            service.fetchAll().isEmpty() shouldBe true
            coVerify { feedbackTypeRepository.fetchAll() }
        }
    }
}
