package scalereal.db.suggestions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.models.domain.Suggestion
import scalereal.core.models.domain.SuggestionPayload
import scalereal.core.suggestions.SuggestionRepository
import suggestions.AddCommentCommand
import suggestions.AddCommentParams
import suggestions.AddSuggestionParams
import suggestions.AddSuggestionQuery
import suggestions.EditSuggestionParams
import suggestions.EditSuggestionProgressCommand
import suggestions.EditSuggestionProgressParams
import suggestions.EditSuggestionQuery
import suggestions.GetAllPendingSuggestionsCountParams
import suggestions.GetAllPendingSuggestionsCountQuery
import suggestions.GetAllSuggestionsCountParams
import suggestions.GetAllSuggestionsCountQuery
import suggestions.GetAllSuggestionsParams
import suggestions.GetAllSuggestionsQuery
import suggestions.GetSuggestionByIdParams
import suggestions.GetSuggestionByIdQuery
import javax.sql.DataSource

@Singleton
class SuggestionRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : SuggestionRepository {
    override fun create(suggestionPayload: SuggestionPayload): SuggestionPayload =
        dataSource.connection.use { connection ->
            AddSuggestionQuery()
                .query(
                    connection,
                    AddSuggestionParams(
                        suggestion = suggestionPayload.suggestion,
                        suggested_by = suggestionPayload.suggestedById,
                        is_draft = suggestionPayload.isDraft,
                        is_anonymous = suggestionPayload.isAnonymous,
                    ),
                ).map {
                    SuggestionPayload(
                        id = it.id,
                        suggestion = it.suggestion,
                        suggestedById = it.suggestedBy,
                        isDraft = it.isDraft,
                        isAnonymous = it.isAnonymous,
                    )
                }.first()
        }

    override fun update(suggestionPayload: SuggestionPayload): SuggestionPayload =
        dataSource.connection.use { connection ->
            EditSuggestionQuery()
                .query(
                    connection,
                    EditSuggestionParams(
                        suggestion = suggestionPayload.suggestion,
                        is_draft = suggestionPayload.isDraft,
                        is_anonymous = suggestionPayload.isAnonymous,
                        id = suggestionPayload.id,
                    ),
                ).map {
                    SuggestionPayload(
                        id = it.id,
                        suggestion = it.suggestion,
                        suggestedById = it.suggestedBy,
                        isDraft = it.isDraft,
                        isAnonymous = it.isAnonymous,
                    )
                }.first()
        }

    override fun countAllSuggestion(
        organisationId: Long,
        suggestionById: List<Int>,
        reviewCycleId: List<Int>,
        isDraft: List<Boolean>,
        progressIds: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllSuggestionsCountQuery()
                .query(
                    connection,
                    GetAllSuggestionsCountParams(
                        organisationId = organisationId,
                        suggestedById = suggestionById.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        isDraft = isDraft.map { it.toString() }.toTypedArray(),
                        progressIds = progressIds.toTypedArray(),
                    ),
                )[0]
                .suggestionCount
                ?.toInt() ?: 0
        }

    override fun fetch(
        organisationId: Long,
        suggestionById: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String,
        isDraft: List<Boolean>,
        progressIds: List<Int>,
    ): List<Suggestion> =
        dataSource.connection.use { connection ->
            GetAllSuggestionsQuery()
                .query(
                    connection,
                    GetAllSuggestionsParams(
                        organisationId = organisationId,
                        suggestedById = suggestionById.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        isDraft = isDraft.map { it.toString() }.toTypedArray(),
                        progressIds = progressIds.toTypedArray(),
                        sortBy = sortBy,
                        offset = offset,
                        limit = limit,
                    ),
                ).map {
                    Suggestion(
                        id = it.id,
                        organisationId = it.organisationId,
                        date = it.updatedAt,
                        suggestion = it.suggestion,
                        suggestedById = it.suggestedById,
                        suggestedByEmployeeId = it.empId,
                        suggestedByFirstName = it.firstName,
                        suggestedByLastName = it.lastName,
                        isDraft = it.isDraft,
                        isAnonymous = it.isAnonymous,
                        progressId = it.progressId,
                        progressName = null,
                        comments = it.comments,
                    )
                }
        }

    override fun fetchById(id: Long): SuggestionPayload? =
        dataSource.connection.use { connection ->
            GetSuggestionByIdQuery()
                .query(
                    connection,
                    GetSuggestionByIdParams(id = id),
                ).map {
                    SuggestionPayload(
                        id = it.id,
                        suggestion = it.suggestion,
                        suggestedById = it.suggestedBy,
                        isDraft = it.isDraft,
                        isAnonymous = it.isAnonymous,
                    )
                }.firstOrNull()
        }

    override fun editSuggestionProgress(
        suggestionId: Long,
        progressId: Int,
    ) = dataSource.connection.use { connection ->
        EditSuggestionProgressCommand()
            .command(connection, EditSuggestionProgressParams(progressId = progressId, suggestionId = suggestionId))
    }

    override fun getAllPendingSuggestionsCount(
        organisationId: Long,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllPendingSuggestionsCountQuery()
                .query(
                    connection,
                    GetAllPendingSuggestionsCountParams(
                        organisationId = organisationId,
                        reviewCycleId = reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .suggestionCount
                ?.toInt() ?: 0
        }

    override fun addSuggestionComment(
        suggestionId: Long,
        progressId: Int,
        comment: String,
        commentedBy: Long,
    ) {
        dataSource.connection.use { connection ->
            AddCommentCommand()
                .command(
                    connection,
                    AddCommentParams(
                        comment = comment,
                        commented_by = commentedBy,
                        suggestion_id = suggestionId,
                        progress_id = progressId,
                    ),
                )
        }
    }
}
