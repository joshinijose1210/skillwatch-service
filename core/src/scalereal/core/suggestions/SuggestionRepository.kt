package scalereal.core.suggestions

import scalereal.core.models.domain.Suggestion
import scalereal.core.models.domain.SuggestionPayload

interface SuggestionRepository {
    fun create(suggestionPayload: SuggestionPayload): SuggestionPayload

    fun update(suggestionPayload: SuggestionPayload): SuggestionPayload

    fun countAllSuggestion(
        organisationId: Long,
        suggestionById: List<Int>,
        reviewCycleId: List<Int>,
        isDraft: List<Boolean>,
        progressIds: List<Int>,
    ): Int

    fun fetch(
        organisationId: Long,
        suggestionById: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String,
        isDraft: List<Boolean>,
        progressIds: List<Int>,
    ): List<Suggestion>

    fun fetchById(id: Long): SuggestionPayload?

    fun editSuggestionProgress(
        suggestionId: Long,
        progressId: Int,
    ): Any // It will return command result, but we can't access it here

    fun getAllPendingSuggestionsCount(
        organisationId: Long,
        reviewCycleId: List<Int>,
    ): Int

    fun addSuggestionComment(
        suggestionId: Long,
        progressId: Int,
        comment: String,
        commentedBy: Long,
    )
}
