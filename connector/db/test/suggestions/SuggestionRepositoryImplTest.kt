package suggestions

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import norm.CommandResult
import norm.executeCommand
import scalereal.core.models.domain.Suggestion
import scalereal.core.models.domain.SuggestionPayload
import scalereal.db.suggestions.SuggestionRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Timestamp

class SuggestionRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var suggestionRepositoryImpl: SuggestionRepositoryImpl

    init {

        val now = Timestamp(System.currentTimeMillis())

        "should be able to submit new suggestion" {
            val suggestionPayload =
                SuggestionPayload(
                    id = 1,
                    suggestion = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = false,
                )
            val addedSuggestion = suggestionRepositoryImpl.create(suggestionPayload)

            addedSuggestion shouldBe suggestionPayload
        }

        "should be able to add suggestion in-draft" {
            val suggestionPayload =
                SuggestionPayload(
                    id = 2,
                    suggestion = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    suggestedById = 1,
                    isDraft = true,
                    isAnonymous = false,
                )
            val addedSuggestion = suggestionRepositoryImpl.create(suggestionPayload)

            addedSuggestion shouldBe suggestionPayload
        }

        "should be able to edit in-draft suggestion and submit it anonymously" {
            val suggestionPayload =
                SuggestionPayload(
                    id = 2,
                    suggestion = "Updated suggestion payload to be saved.",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = true,
                )
            val updatedSuggestion = suggestionRepositoryImpl.update(suggestionPayload)

            updatedSuggestion shouldBe suggestionPayload
        }

        "should add another suggestion in-draft" {
            val suggestionPayload =
                SuggestionPayload(
                    id = 3,
                    suggestion = "Sample suggestion -in-draft.",
                    suggestedById = 1,
                    isDraft = true,
                    isAnonymous = false,
                )
            val addedSuggestion = suggestionRepositoryImpl.create(suggestionPayload)

            addedSuggestion shouldBe suggestionPayload
        }

        "should return count and fetch all submitted suggestion" {
            val organisationId = 1L
            val suggestedById = listOf(1)
            val reviewCycleId = listOf(-99)
            val isDraft = listOf(true, false)
            val progressIds = listOf(-99)
            val expectedSuggestions =
                listOf(
                    Suggestion(
                        id = 3,
                        organisationId = 1,
                        date = now,
                        suggestion = "Sample suggestion -in-draft.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "SR00XX",
                        suggestedByFirstName = "Lorem",
                        suggestedByLastName = "Ipsum",
                        isDraft = true,
                        isAnonymous = false,
                        progressId = null,
                        progressName = null,
                        comments = listOf(),
                    ),
                    Suggestion(
                        id = 2,
                        organisationId = 1,
                        date = now,
                        suggestion = "Updated suggestion payload to be saved.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "SR00XX",
                        suggestedByFirstName = "Lorem",
                        suggestedByLastName = "Ipsum",
                        isDraft = false,
                        isAnonymous = true,
                        progressId = null,
                        progressName = null,
                        comments = listOf(),
                    ),
                    Suggestion(
                        id = 1,
                        organisationId = 1,
                        date = now,
                        suggestion = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "SR00XX",
                        suggestedByFirstName = "Lorem",
                        suggestedByLastName = "Ipsum",
                        isDraft = false,
                        isAnonymous = false,
                        progressId = null,
                        progressName = null,
                        comments = listOf(),
                    ),
                )
            val countSubmittedSuggestions =
                suggestionRepositoryImpl.countAllSuggestion(
                    organisationId = organisationId,
                    suggestionById = suggestedById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    progressIds = progressIds,
                )
            val submittedSuggestions =
                suggestionRepositoryImpl.fetch(
                    organisationId = organisationId,
                    suggestionById = suggestedById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    progressIds = progressIds,
                    offset = 0,
                    limit = 10,
                    sortBy = "dateDesc",
                )

            countSubmittedSuggestions shouldBe submittedSuggestions.size
            assertSuggestion(submittedSuggestions, expectedSuggestions)
        }

        "should return count and fetch all received suggestion" {
            val organisationId = 1L
            val suggestedById = listOf(-99)
            val reviewCycleId = listOf(-99)
            val isDraft = listOf(false)
            val progressIds = listOf(-99)
            val expectedSuggestion =
                listOf(
                    Suggestion(
                        id = 2,
                        organisationId = 1,
                        date = now,
                        suggestion = "Updated suggestion payload to be saved.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "SR00XX",
                        suggestedByFirstName = "Lorem",
                        suggestedByLastName = "Ipsum",
                        isDraft = false,
                        isAnonymous = true,
                        progressId = null,
                        progressName = null,
                        comments = listOf(),
                    ),
                    Suggestion(
                        id = 1,
                        organisationId = 1,
                        date = now,
                        suggestion = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "SR00XX",
                        suggestedByFirstName = "Lorem",
                        suggestedByLastName = "Ipsum",
                        isDraft = false,
                        isAnonymous = false,
                        progressId = null,
                        progressName = null,
                        comments = listOf(),
                    ),
                )

            val suggestionCount =
                suggestionRepositoryImpl.countAllSuggestion(
                    organisationId = organisationId,
                    suggestionById = suggestedById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    progressIds = progressIds,
                )
            val suggestions =
                suggestionRepositoryImpl.fetch(
                    organisationId = organisationId,
                    suggestionById = suggestedById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    progressIds = progressIds,
                    offset = 0,
                    limit = 10,
                    sortBy = "dateDesc",
                )

            suggestionCount shouldBe suggestions.size
            assertSuggestion(suggestions, expectedSuggestion)
        }

        "should edit suggestion progress" {
            suggestionRepositoryImpl.editSuggestionProgress(
                suggestionId = 1,
                progressId = 1,
            ) shouldBe CommandResult(updatedRecordsCount = 1)
        }

        "should fetch suggestion by suggestion id" {
            val expectedSuggestion =
                SuggestionPayload(
                    id = 2,
                    suggestion = "Updated suggestion payload to be saved.",
                    suggestedById = 1,
                    isDraft = false,
                    isAnonymous = true,
                )
            suggestionRepositoryImpl.fetchById(id = 2) shouldBe expectedSuggestion
        }

        "should return count and fetch all received suggestion by progressIds" {
            val organisationId = 1L
            val suggestedById = listOf(-99)
            val reviewCycleId = listOf(-99)
            val isDraft = listOf(false)
            val progressIds = listOf(1)
            val expectedSuggestion =
                listOf(
                    Suggestion(
                        id = 1,
                        organisationId = 1,
                        date = now,
                        suggestion = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                        suggestedById = 1,
                        suggestedByEmployeeId = "SR00XX",
                        suggestedByFirstName = "Lorem",
                        suggestedByLastName = "Ipsum",
                        isDraft = false,
                        isAnonymous = false,
                        progressId = 1,
                        progressName = null,
                        comments = listOf(),
                    ),
                )

            val suggestionCount =
                suggestionRepositoryImpl.countAllSuggestion(
                    organisationId = organisationId,
                    suggestionById = suggestedById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    progressIds = progressIds,
                )
            val suggestions =
                suggestionRepositoryImpl.fetch(
                    organisationId = organisationId,
                    suggestionById = suggestedById,
                    reviewCycleId = reviewCycleId,
                    isDraft = isDraft,
                    progressIds = progressIds,
                    offset = 0,
                    limit = 10,
                    sortBy = "dateDesc",
                )

            suggestionCount shouldBe suggestions.size
            assertSuggestion(suggestions, expectedSuggestion)
        }

        "should return count of all pending suggestions" {
            val organisationId = 1L
            val reviewCycleId = listOf(-99)

            val suggestionCount =
                suggestionRepositoryImpl.getAllPendingSuggestionsCount(
                    organisationId = organisationId,
                    reviewCycleId = reviewCycleId,
                )

            suggestionCount shouldBe 1
        }
    }

    private fun assertSuggestion(
        actualSuggestion: List<Suggestion>,
        expectedSuggestion: List<Suggestion>,
    ) {
        actualSuggestion.mapIndexed { index, actual ->
            val expected = expectedSuggestion[index]
            actual.id shouldBe expected.id
            actual.organisationId shouldBe expected.organisationId
            actual.date shouldBeAfter expected.date
            actual.suggestion shouldBe expected.suggestion
            actual.isDraft shouldBe expected.isDraft
            actual.suggestedById shouldBe expected.suggestedById
            actual.suggestedByEmployeeId shouldBe expected.suggestedByEmployeeId
            actual.suggestedByFirstName shouldBe expected.suggestedByFirstName
            actual.suggestedByLastName shouldBe expected.suggestedByLastName
            actual.isAnonymous shouldBe expected.isAnonymous
            actual.progressId shouldBe expected.progressId
            actual.progressName shouldBe expected.progressName
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO organisations (sr_no, admin_id, name, is_active, organisation_size)
                VALUES(1, 1, 'ScaleReal Technologies', true, 50);
                
                INSERT INTO employees (id, emp_id, first_name, last_name, email_id, contact_no, status, onboarding_flow, organisation_id)
                VALUES(1, 'SR00XX', 'Lorem', 'Ipsum', 'lorem.ipsum@scalereal.com', 7389543155, true, false, 1);
                """.trimIndent(),
            )
        }
        suggestionRepositoryImpl = SuggestionRepositoryImpl(dataSource)
    }
}
