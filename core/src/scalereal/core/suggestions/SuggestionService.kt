package scalereal.core.suggestions

import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import scalereal.core.emails.SuggestionMail
import scalereal.core.exception.SuggestionException
import scalereal.core.models.SuggestionProgress
import scalereal.core.models.domain.Suggestion
import scalereal.core.models.domain.SuggestionPayload
import scalereal.core.slack.SuggestionSlackNotifications
import kotlin.Int

@Singleton
class SuggestionService(
    private val suggestionRepository: SuggestionRepository,
    private val suggestionMail: SuggestionMail,
    private val suggestionSlackNotifications: SuggestionSlackNotifications,
) {
    @OptIn(DelicateCoroutinesApi::class)
    private fun sendSuggestionNotifications(
        suggestedById: Long,
        suggestion: String,
    ) {
        GlobalScope.launch {
            suggestionMail.sendMail(suggestedById = suggestedById, suggestion = suggestion)
            suggestionSlackNotifications.sendSuggestionNotification(suggestedById = suggestedById, suggestion = suggestion)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun create(
        suggestion: String,
        markdownText: String,
        suggestedById: Long,
        isDraft: Boolean,
        isAnonymous: Boolean,
    ): SuggestionPayload {
        val addedSuggestion =
            suggestionRepository.create(
                suggestionPayload =
                    SuggestionPayload(
                        id = -99,
                        suggestion = suggestion,
                        suggestedById = suggestedById,
                        isDraft = isDraft,
                        isAnonymous = isAnonymous,
                    ),
            )
        if (!addedSuggestion.isDraft) {
            editSuggestionProgress(
                suggestionId = addedSuggestion.id,
                progressId = SuggestionProgress.TODO.progressId,
                commentedBy = suggestedById,
            )
            sendSuggestionNotifications(suggestedById = suggestedById, suggestion = markdownText)
        }
        return addedSuggestion
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun update(
        id: Long,
        suggestion: String,
        markdownText: String,
        suggestedById: Long,
        isDraft: Boolean,
        isAnonymous: Boolean,
    ): SuggestionPayload {
        try {
            val savedSuggestion =
                suggestionRepository.fetchById(id = id)
                    ?: throw SuggestionException("The suggestion doesn't exist.")

            if (!savedSuggestion.isDraft) {
                throw SuggestionException("This suggestion is in a published state, so it cannot be edited.")
            }

            val updatedSuggestionPayload =
                SuggestionPayload(
                    id = id,
                    suggestion = suggestion,
                    suggestedById = suggestedById,
                    isDraft = isDraft,
                    isAnonymous = isAnonymous,
                )
            val editedSuggestion = suggestionRepository.update(updatedSuggestionPayload)
            if (!editedSuggestion.isDraft) {
                editSuggestionProgress(
                    suggestionId = editedSuggestion.id,
                    progressId = SuggestionProgress.TODO.progressId,
                    commentedBy = suggestedById,
                )
                sendSuggestionNotifications(suggestedById = suggestedById, suggestion = markdownText)
            }
            return editedSuggestion
        } catch (e: SuggestionException) {
            throw e
        } catch (_: Exception) {
            throw Exception("An unexpected error occurred while editing the suggestion.")
        }
    }

    fun countAllSuggestion(
        organisationId: Long,
        suggestionById: List<Int>,
        reviewCycleId: List<Int>,
        isDraft: List<Boolean>,
        progressIds: List<Int>,
    ): Int =
        suggestionRepository.countAllSuggestion(
            organisationId = organisationId,
            suggestionById = suggestionById,
            reviewCycleId = reviewCycleId,
            isDraft = isDraft,
            progressIds = progressIds,
        )

    fun fetch(
        organisationId: Long,
        suggestionById: List<Int>,
        reviewCycleId: List<Int>,
        isDraft: List<Boolean>,
        progressIds: List<Int>,
        page: Int,
        limit: Int,
        sortBy: String,
    ): List<Suggestion> {
        val suggestions =
            suggestionRepository.fetch(
                organisationId = organisationId,
                suggestionById = suggestionById,
                reviewCycleId = reviewCycleId,
                isDraft = isDraft,
                progressIds = progressIds,
                offset = (page - 1) * limit,
                limit = limit,
                sortBy = sortBy,
            )
        suggestions.mapIndexed { index, suggestion ->
            if (suggestion.isAnonymous) {
                suggestions[index].suggestedById = null
                suggestions[index].suggestedByEmployeeId = null
                suggestions[index].suggestedByFirstName = null
                suggestions[index].suggestedByLastName = null
            }
            suggestion.progressId?.let { id ->
                suggestions[index].progressName = SuggestionProgress.getProgressListWithId().find { it.progressId == id }?.progressName
            }
        }
        return suggestions
    }

    fun editSuggestionProgress(
        suggestionId: Long,
        progressId: Int,
        comment: String? = null,
        markDownComment: String? = null,
        commentedBy: Long,
    ) {
        try {
            if (progressId !in SuggestionProgress.entries.map { it.progressId }) {
                throw SuggestionException("Invalid Suggestion Progress.")
            }
            if (!comment.isNullOrBlank()) {
                val plainText = Jsoup.parse(comment).text()
                if (plainText.length < 10 || plainText.length > 200) {
                    throw SuggestionException("Comment should be between 10 and 200 characters.")
                }
            }

            if (progressId == SuggestionProgress.DEFERRED.progressId && (comment == null || comment.isEmpty())) {
                throw SuggestionException("Please add comment.")
            }

            val suggestion = suggestionRepository.fetchById(suggestionId) ?: throw SuggestionException("Invalid Suggestion.")
            if (suggestion.isDraft) throw SuggestionException("The suggestion is in draft state, so its progress cannot be edited.")
            suggestionRepository.editSuggestionProgress(suggestionId = suggestionId, progressId = progressId)
            if (comment != null && comment.isNotEmpty()) {
                suggestionRepository.addSuggestionComment(
                    suggestionId = suggestionId,
                    progressId = progressId,
                    comment = comment,
                    commentedBy = commentedBy,
                )
            }
            sendSuggestionProgressUpdateNotification(
                progressId = progressId,
                comment = markDownComment,
                suggestedById = suggestion.suggestedById,
            )
        } catch (e: SuggestionException) {
            throw e
        } catch (_: Exception) {
            throw Exception("An unexpected error occurred while editing suggestion progress.")
        }
    }

    fun getAllPendingSuggestionsCount(
        organisationId: Long,
        reviewCycleId: List<Int>,
    ): Int = suggestionRepository.getAllPendingSuggestionsCount(organisationId, reviewCycleId)

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendSuggestionProgressUpdateNotification(
        progressId: Int,
        comment: String? = null,
        suggestedById: Long,
    ) {
        GlobalScope.launch {
            suggestionMail.sendSuggestionProgressUpdate(
                suggestedById = suggestedById,
                progressId = progressId,
                comment = comment,
            )
            suggestionSlackNotifications.sendSuggestionProgressUpdate(
                suggestedById = suggestedById,
                progressId = progressId,
                comment = comment,
            )
        }
    }
}
