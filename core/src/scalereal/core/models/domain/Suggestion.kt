package scalereal.core.models.domain

import java.sql.Timestamp

data class Suggestion(
    val id: Long,
    val organisationId: Long,
    val date: Timestamp,
    val suggestion: String,
    var suggestedById: Long?,
    var suggestedByEmployeeId: String?,
    var suggestedByFirstName: String?,
    var suggestedByLastName: String?,
    val isDraft: Boolean,
    val isAnonymous: Boolean,
    val progressId: Int?,
    var progressName: String?,
    val comments: List<SuggestionComments>,
)

data class SuggestionPayload(
    val id: Long,
    val suggestion: String,
    val suggestedById: Long,
    val isDraft: Boolean,
    val isAnonymous: Boolean,
)

data class SuggestionComments(
    val id: Long,
    val date: Timestamp,
    val comment: String,
)

data class SuggestionResponse(
    val totalSuggestions: Int,
    val suggestions: List<Suggestion>,
)
