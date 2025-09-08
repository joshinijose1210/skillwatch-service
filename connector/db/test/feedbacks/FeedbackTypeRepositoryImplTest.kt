package feedbacks

import io.kotest.core.spec.Spec
import io.kotest.matchers.ints.beGreaterThan
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.db.feedbacks.FeedbackTypeRepositoryImpl
import util.StringSpecWithDataSource

class FeedbackTypeRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var feedbackTypeRepositoryImpl: FeedbackTypeRepositoryImpl

    init {
        "should get all feedback types in order by id" {
            val feedbackTypes = feedbackTypeRepositoryImpl.fetchAll()

            feedbackTypes[0].feedbackType shouldBe "Positive"
            feedbackTypes[1].feedbackType shouldBe "Improvement"
            feedbackTypes[2].feedbackType shouldBe "Appreciation"
        }

        "feedback types should be greater than 0" {
            val feedbackTypes = feedbackTypeRepositoryImpl.fetchAll()

            feedbackTypes.size shouldBe beGreaterThan(0)
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO feedback_types(name) 
                 VALUES('Positive'), 
                 ('Improvement'), 
                 ('Appreciation');
                """.trimIndent(),
            )
        }
        feedbackTypeRepositoryImpl = FeedbackTypeRepositoryImpl(dataSource)
    }
}
