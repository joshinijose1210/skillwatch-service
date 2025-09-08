package scalereal.core.models.domain

import java.sql.Date
import java.sql.Timestamp

data class Organisation(
    val id: Long,
    val adminId: Long?,
    val name: String,
    val size: Int,
    val timeZone: String,
)

data class OrganisationDetails(
    val id: Long,
    val name: String,
    val size: Int,
    val contactNo: String,
    val activeUsers: Long?,
    val inactiveUsers: Long?,
    val timeZone: String,
)

data class OrganisationDomains(
    val id: Long,
    val organisationId: Long,
    val name: String,
    var isDomainUsed: Boolean,
)

data class Domain(
    val id: Long,
    val name: String,
)

data class OrganisationResponse(
    val totalCount: Int,
    val users: List<OrganisationData>,
)

data class OrganisationData(
    val date: Date?,
    val adminFirstName: String,
    val adminLastName: String,
    val adminEmailId: String,
    val organisationId: Int?,
    val organisationName: String?,
    val organisationSize: Int?,
    val contactNo: String?,
    val timeZone: String?,
)

data class OrganisationSettings(
    val organisationId: Long,
    val isManagerReviewMandatory: Boolean,
    val isAnonymousSuggestionAllowed: Boolean,
    val isBiWeeklyFeedbackReminderEnabled: Boolean,
    val timeZone: String,
)

data class FeedbackReminderConfiguration(
    val organisationId: Long,
    val isBiWeeklyFeedbackReminderEnabled: Boolean,
    val lastFeedbackReminderSentDate: Timestamp?,
    val lastFeedbackReminderIndex: Int?,
)
