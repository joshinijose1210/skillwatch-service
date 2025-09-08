package suggestions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.emails.SuggestionMail
import scalereal.core.exception.SuggestionException
import scalereal.core.models.SuggestionProgress
import scalereal.core.models.domain.Suggestion
import scalereal.core.models.domain.SuggestionPayload
import scalereal.core.slack.SuggestionSlackNotifications
import scalereal.core.suggestions.SuggestionRepository
import scalereal.core.suggestions.SuggestionService
import java.sql.Timestamp

class SuggestionServiceImplTest : StringSpec() {
    private val suggestionRepository = mockk<SuggestionRepository>()
    private val suggestionMail = mockk<SuggestionMail>()
    private val suggestionSlackNotifications = mockk<SuggestionSlackNotifications>()
    private val suggestionService = SuggestionService(suggestionRepository, suggestionMail, suggestionSlackNotifications)

    init {

        "should be able to submit new suggestion" {
            val expectedSuggestionPayload =
                SuggestionPayload(
                    id = -99,
                    suggestion = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = false,
                )

            every { suggestionRepository.create(any()) } returns expectedSuggestionPayload
            every { suggestionRepository.fetchById(any()) } returns expectedSuggestionPayload
            every { suggestionRepository.editSuggestionProgress(any(), any()) } returns Unit

            val actualSuggestionPayload =
                suggestionService.create(
                    suggestion = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    markdownText = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = false,
                )
            verify { suggestionRepository.create(expectedSuggestionPayload) }
            actualSuggestionPayload shouldBe expectedSuggestionPayload
        }

        "should be able to add suggestion in-draft" {
            val expectedSuggestionPayload =
                SuggestionPayload(
                    id = -99,
                    suggestion = "Sample suggestion to company.",
                    suggestedById = 2,
                    isDraft = true,
                    isAnonymous = false,
                )

            every { suggestionRepository.create(any()) } returns expectedSuggestionPayload

            val actualSuggestionPayload =
                suggestionService.create(
                    suggestion = "Sample suggestion to company.",
                    markdownText = "Sample suggestion to company.",
                    suggestedById = 2,
                    isDraft = true,
                    isAnonymous = false,
                )
            verify { suggestionRepository.create(expectedSuggestionPayload) }
            actualSuggestionPayload shouldBe expectedSuggestionPayload
        }

        "should be able to edit an existing suggestion and submit as anonymous" {
            val initialSuggestionPayload =
                SuggestionPayload(
                    id = 1,
                    suggestion = "Initial suggestion",
                    suggestedById = 1,
                    isDraft = true,
                    isAnonymous = false,
                )
            val updatedSuggestionPayload =
                SuggestionPayload(
                    id = 1,
                    suggestion = "Updated suggestion",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = true,
                )

            every { suggestionRepository.fetchById(any()) } returns initialSuggestionPayload andThen updatedSuggestionPayload
            every { suggestionRepository.update(any()) } returns updatedSuggestionPayload

            val actualSuggestionPayload =
                suggestionService.update(
                    id = 1,
                    suggestion = "Updated suggestion",
                    markdownText = "Updated suggestion",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = true,
                )

            verify { suggestionRepository.fetchById(1) }
            verify { suggestionRepository.update(updatedSuggestionPayload) }
            actualSuggestionPayload shouldBe updatedSuggestionPayload
        }

        "should be able to edit an existing suggestion and save it as non-anonymous" {
            val initialSuggestionPayload =
                SuggestionPayload(
                    id = 1,
                    suggestion = "Initial suggestion",
                    suggestedById = 1,
                    isDraft = true,
                    isAnonymous = true,
                )
            val updatedSuggestionPayload =
                SuggestionPayload(
                    id = 1,
                    suggestion = "Updated suggestion",
                    suggestedById = 1,
                    isDraft = true,
                    isAnonymous = false,
                )

            every { suggestionRepository.fetchById(any()) } returns initialSuggestionPayload
            every { suggestionRepository.update(any()) } returns updatedSuggestionPayload

            val actualSuggestionPayload =
                suggestionService.update(
                    id = 1,
                    suggestion = "Updated suggestion",
                    markdownText = "Updated suggestion",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = true,
                )
            actualSuggestionPayload shouldBe updatedSuggestionPayload
        }

        "should throw SuggestionException for non-existent suggestion during edit" {
            every { suggestionRepository.fetchById(any()) } returns null

            val exception =
                shouldThrow<SuggestionException> {
                    suggestionService.update(
                        id = 999,
                        suggestion = "Updated suggestion",
                        markdownText = "Updated suggestion",
                        suggestedById = 1,
                        isDraft = false,
                        isAnonymous = false,
                    )
                }

            exception.message shouldBe "The suggestion doesn't exist."
        }

        "should throw SuggestionException for suggestion in a published state during edit" {
            val publishedSuggestionPayload =
                SuggestionPayload(
                    id = 1,
                    suggestion = "Published suggestion",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = true,
                )

            every { suggestionRepository.fetchById(any()) } returns publishedSuggestionPayload

            val exception =
                shouldThrow<SuggestionException> {
                    suggestionService.update(
                        id = 1,
                        suggestion = "Updated suggestion",
                        markdownText = "Updated suggestion",
                        suggestedById = 1,
                        isDraft = false,
                        isAnonymous = true,
                    )
                }

            exception.message shouldBe "This suggestion is in a published state, so it cannot be edited."
        }

        "should throw exception for an unexpected error while editing suggestion" {
            every { suggestionRepository.fetchById(any()) } throws Exception("Can't fetch suggestion by id")
            val exception =
                shouldThrow<Exception> {
                    suggestionService.update(
                        id = 1,
                        suggestion = "Updated suggestion",
                        markdownText = "Updated suggestion",
                        suggestedById = 1,
                        isDraft = false,
                        isAnonymous = true,
                    )
                }

            exception.message shouldBe "An unexpected error occurred while editing the suggestion."
        }

        "should return suggestion count" {
            val organisationId = 1L
            val suggestionById = listOf(-99)
            val reviewCycleId = listOf(-99)
            val isDraft = listOf(true, false)
            val progressIds = listOf(-99)
            every { suggestionRepository.countAllSuggestion(any(), any(), any(), any(), any()) } returns 2
            suggestionService.countAllSuggestion(organisationId, suggestionById, reviewCycleId, isDraft, progressIds) shouldBe 2
            verify(
                exactly = 1,
            ) { suggestionRepository.countAllSuggestion(organisationId, suggestionById, reviewCycleId, isDraft, progressIds) }
        }

        "should be able to fetch all received suggestions" {
            val organisationId = 1L
            val suggestionById = listOf(-99)
            val reviewCycleId = listOf(-99)
            val isDraft = listOf(false)
            val progressIds = listOf(-99)
            val sortBy = "dateDesc"
            val actualSuggestions =
                listOf(
                    Suggestion(
                        id = 2,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-06 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "S0001",
                        suggestedByFirstName = "John",
                        suggestedByLastName = "Cruise",
                        isDraft = false,
                        isAnonymous = false,
                        progressId = SuggestionProgress.TODO.progressId,
                        progressName = null,
                        comments = listOf(),
                    ),
                    Suggestion(
                        id = 1,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-05 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = 5,
                        suggestedByEmployeeId = "S0005",
                        suggestedByFirstName = "Mary",
                        suggestedByLastName = "Cruise",
                        isDraft = false,
                        isAnonymous = true,
                        progressId = SuggestionProgress.COMPLETED.progressId,
                        progressName = null,
                        comments = listOf(),
                    ),
                )
            val expectedSuggestions =
                listOf(
                    Suggestion(
                        id = 2,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-06 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "S0001",
                        suggestedByFirstName = "John",
                        suggestedByLastName = "Cruise",
                        isDraft = false,
                        isAnonymous = false,
                        progressId = SuggestionProgress.TODO.progressId,
                        progressName = SuggestionProgress.TODO.progressName,
                        comments = listOf(),
                    ),
                    Suggestion(
                        id = 1,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-05 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = null,
                        suggestedByEmployeeId = null,
                        suggestedByFirstName = null,
                        suggestedByLastName = null,
                        isDraft = false,
                        isAnonymous = true,
                        progressId = SuggestionProgress.COMPLETED.progressId,
                        progressName = SuggestionProgress.COMPLETED.progressName,
                        comments = listOf(),
                    ),
                )
            every { suggestionRepository.fetch(any(), any(), any(), any(), any(), any(), any(), any()) } returns actualSuggestions
            suggestionService.fetch(
                organisationId = organisationId,
                suggestionById = suggestionById,
                reviewCycleId = reviewCycleId,
                isDraft = isDraft,
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = sortBy,
                progressIds = progressIds,
            ) shouldBe expectedSuggestions
            verify(exactly = 1) {
                suggestionRepository.fetch(
                    organisationId = organisationId,
                    suggestionById = suggestionById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = sortBy,
                    progressIds = progressIds,
                )
            }
        }

        "should be able to fetch all submitted suggestions" {
            val organisationId = 1L
            val suggestionById = listOf(1)
            val reviewCycleId = listOf(-99)
            val isDraft = listOf(true, false)
            val progressIds = listOf(-99)
            val sortBy = "dateDesc"
            val actualSuggestions =
                listOf(
                    Suggestion(
                        id = 2,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-06 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "S0001",
                        suggestedByFirstName = "John",
                        suggestedByLastName = "Cruise",
                        isDraft = true,
                        isAnonymous = false,
                        progressId = null,
                        progressName = null,
                        comments = listOf(),
                    ),
                    Suggestion(
                        id = 1,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-05 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = 5,
                        suggestedByEmployeeId = "S0005",
                        suggestedByFirstName = "Mary",
                        suggestedByLastName = "Cruise",
                        isDraft = false,
                        isAnonymous = true,
                        progressId = 1,
                        progressName = "To Do",
                        comments = listOf(),
                    ),
                )
            val expectedSuggestions =
                listOf(
                    Suggestion(
                        id = 2,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-06 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "S0001",
                        suggestedByFirstName = "John",
                        suggestedByLastName = "Cruise",
                        isDraft = true,
                        isAnonymous = false,
                        progressId = null,
                        progressName = null,
                        comments = listOf(),
                    ),
                    Suggestion(
                        id = 1,
                        organisationId = 1,
                        date = Timestamp.valueOf("2024-01-05 11:10:48.834129"),
                        suggestion = "This is a dummy suggestion 2 for testing purpose.",
                        suggestedById = null,
                        suggestedByEmployeeId = null,
                        suggestedByFirstName = null,
                        suggestedByLastName = null,
                        isDraft = false,
                        isAnonymous = true,
                        progressId = 1,
                        progressName = "To Do",
                        comments = listOf(),
                    ),
                )
            every { suggestionRepository.fetch(any(), any(), any(), any(), any(), any(), any(), any()) } returns actualSuggestions
            suggestionService.fetch(
                organisationId = organisationId,
                suggestionById = suggestionById,
                reviewCycleId = reviewCycleId,
                isDraft = isDraft,
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = sortBy,
                progressIds = progressIds,
            ) shouldBe expectedSuggestions
            verify(exactly = 1) {
                suggestionRepository.fetch(
                    organisationId = organisationId,
                    suggestionById = suggestionById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = sortBy,
                    progressIds = progressIds,
                )
            }
        }

        "should throw exception while updating suggestion progress with invalid progressId" {
            val exception =
                shouldThrow<SuggestionException> {
                    suggestionService.editSuggestionProgress(
                        suggestionId = 1,
                        progressId = 5,
                        comment = null,
                        commentedBy = 1,
                    )
                }
            exception.message shouldBe "Invalid Suggestion Progress."
        }

        "should throw exception while updating suggestion progress with non-existent suggestion" {
            every { suggestionRepository.fetchById(any()) } returns null
            val exception =
                shouldThrow<SuggestionException> {
                    suggestionService.editSuggestionProgress(
                        suggestionId = 1,
                        progressId = 3,
                        comment = "This suggestion is not doable",
                        commentedBy = 1,
                    )
                }
            exception.message shouldBe "Invalid Suggestion."
        }

        "should throw exception while updating suggestion progress of draft suggestion" {
            val suggestion =
                SuggestionPayload(
                    id = 4,
                    suggestion = "Published suggestion",
                    suggestedById = 1,
                    isDraft = true,
                    isAnonymous = true,
                )
            every { suggestionRepository.fetchById(any()) } returns suggestion
            val exception =
                shouldThrow<SuggestionException> {
                    suggestionService.editSuggestionProgress(
                        suggestionId = 4,
                        progressId = 1,
                        comment = null,
                        commentedBy = 1,
                    )
                }
            exception.message shouldBe "The suggestion is in draft state, so its progress cannot be edited."
        }

        "should throw exception for an unexpected error while editing suggestion progress" {
            val suggestion =
                SuggestionPayload(
                    id = 4,
                    suggestion = "Published suggestion",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = true,
                )
            every { suggestionRepository.fetchById(any()) } returns suggestion
            every {
                suggestionRepository.editSuggestionProgress(
                    any(),
                    any(),
                )
            } throws Exception("Can't edit suggestion progress")
            val exception =
                shouldThrow<Exception> {
                    suggestionService.editSuggestionProgress(
                        suggestionId = 4,
                        progressId = 1,
                        comment = null,
                        commentedBy = 1,
                    )
                }
            exception.message shouldBe "An unexpected error occurred while editing suggestion progress."
        }

        "should return all pending suggestions count" {
            val organisationId = 1L
            val reviewCycleId = listOf(-99)
            every { suggestionRepository.getAllPendingSuggestionsCount(any(), any()) } returns 2
            suggestionService.getAllPendingSuggestionsCount(organisationId, reviewCycleId) shouldBe 2
            verify(exactly = 1) { suggestionRepository.getAllPendingSuggestionsCount(organisationId, reviewCycleId) }
        }
    }
}
