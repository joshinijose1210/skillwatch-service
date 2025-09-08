package scalereal.core.organisations

import scalereal.core.models.domain.AdminData
import scalereal.core.models.domain.FeedbackReminderConfiguration
import scalereal.core.models.domain.Organisation
import scalereal.core.models.domain.OrganisationData
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.OrganisationDomains
import scalereal.core.models.domain.OrganisationSettings
import java.sql.Timestamp

interface OrganisationRepository {
    fun fetchName(organisationId: Long): String

    fun getOrganisationId(organisationName: String): Int

    fun getOrganisationIdByEmailId(emailId: Any): Long

    fun addAdminId(
        adminId: Long,
        organisationId: Long,
    )

    fun isOrganisationActive(emailId: Any): Boolean

    fun fetchAllOrganisations(): List<Organisation>

    fun createOrganisation(
        organisationName: String,
        organisationSize: Int,
        timeZone: String,
    ): Long

    fun getAllowedDomains(organisationId: Long): List<OrganisationDomains>

    fun deleteDomains(organisationId: Long)

    fun addOrganisationDomainMapping(
        organisationId: Long,
        domainName: String,
    )

    fun createOrganisationAdmin(adminData: AdminData): Long

    fun update(
        id: Long,
        organisationName: String,
        timeZone: String,
    )

    fun updateAdminContactNo(
        id: Long,
        contactNo: String,
    )

    fun getOrganisationDetails(id: Long): OrganisationDetails

    fun getAllOrganisationCount(): Int

    fun getAllOrganisation(
        offset: Int,
        limit: Int,
    ): List<OrganisationData>

    fun editGeneralSettings(
        organisationId: Long,
        isManagerReviewMandatory: Boolean,
        isAnonymousSuggestionAllowed: Boolean,
        isBiWeeklyFeedbackReminderEnabled: Boolean,
    )

    fun getGeneralSettings(organisationId: Long): OrganisationSettings

    fun getFeedbackReminderSchedule(organisationId: Long): FeedbackReminderConfiguration

    fun updateFeedbackReminderSchedule(
        organisationId: Long,
        feedbackReminderIndex: Int,
        lastFeedbackReminderSent: Timestamp,
    )

    fun updateTimeZone(
        organisationId: Long,
        timeZone: String,
    )
}
