package scalereal.db.organisations

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import organisations.AddAdminIdCommand
import organisations.AddAdminIdParams
import organisations.AddOrganisationAdminParams
import organisations.AddOrganisationAdminQuery
import organisations.AddOrganisationDomainMappingCommand
import organisations.AddOrganisationDomainMappingParams
import organisations.AddOrganisationParams
import organisations.AddOrganisationQuery
import organisations.DeleteAllowedDomainsCommand
import organisations.DeleteAllowedDomainsParams
import organisations.EditOrganisationSettingsCommand
import organisations.EditOrganisationSettingsParams
import organisations.FetchAllOrganisationsParams
import organisations.FetchAllOrganisationsQuery
import organisations.GetAllOrganisationCountParams
import organisations.GetAllOrganisationCountQuery
import organisations.GetAllOrganisationParams
import organisations.GetAllOrganisationQuery
import organisations.GetAllowedDomainsParams
import organisations.GetAllowedDomainsQuery
import organisations.GetFeedbackReminderConfigParams
import organisations.GetFeedbackReminderConfigQuery
import organisations.GetOrganisationDetailsParams
import organisations.GetOrganisationDetailsQuery
import organisations.GetOrganisationIdByEmailIdParams
import organisations.GetOrganisationIdByEmailIdQuery
import organisations.GetOrganisationIdParams
import organisations.GetOrganisationIdQuery
import organisations.GetOrganisationParams
import organisations.GetOrganisationQuery
import organisations.GetOrganisationSettingsParams
import organisations.GetOrganisationSettingsQuery
import organisations.IsOrganisationActiveParams
import organisations.IsOrganisationActiveQuery
import organisations.UpdateAdminContactNoCommand
import organisations.UpdateAdminContactNoParams
import organisations.UpdateFeedbackReminderScheduleCommand
import organisations.UpdateFeedbackReminderScheduleParams
import organisations.UpdateOrganisationCommand
import organisations.UpdateOrganisationParams
import organisations.UpdateOrganisationTimeZoneCommand
import organisations.UpdateOrganisationTimeZoneParams
import scalereal.core.models.domain.AdminData
import scalereal.core.models.domain.FeedbackReminderConfiguration
import scalereal.core.models.domain.Organisation
import scalereal.core.models.domain.OrganisationData
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.OrganisationDomains
import scalereal.core.models.domain.OrganisationSettings
import scalereal.core.organisations.OrganisationRepository
import java.sql.Timestamp
import javax.sql.DataSource

@Singleton
class OrganisationRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : OrganisationRepository {
    override fun fetchName(organisationId: Long): String =
        dataSource.connection.use { connection ->
            GetOrganisationQuery()
                .query(connection, GetOrganisationParams(organisationId.toInt()))
                .map { it.organisationName }
                .first()
        }

    override fun getOrganisationId(organisationName: String): Int =
        dataSource.connection.use { connection ->
            GetOrganisationIdQuery()
                .query(connection, GetOrganisationIdParams(organisationName = organisationName))
                .map { it.organisationId }
                .first()
        }

    override fun getOrganisationIdByEmailId(emailId: Any): Long =
        dataSource.connection.use { connection ->
            GetOrganisationIdByEmailIdQuery()
                .query(connection, GetOrganisationIdByEmailIdParams(emailId.toString()))
                .map { it.organisationId }
                .first()
        }

    override fun addAdminId(
        adminId: Long,
        organisationId: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            AddAdminIdCommand()
                .command(connection, AddAdminIdParams(adminId, organisationId.toInt()))
        }

    override fun isOrganisationActive(emailId: Any): Boolean =
        dataSource.connection.use { connection ->
            val queryResult =
                IsOrganisationActiveQuery().query(
                    connection,
                    IsOrganisationActiveParams(emailId = emailId.toString()),
                )
            val isActive = queryResult.getOrNull(0)?.isActive ?: false
            isActive
        }

    override fun fetchAllOrganisations(): List<Organisation> =
        dataSource.connection.use { connection ->
            FetchAllOrganisationsQuery()
                .query(connection, FetchAllOrganisationsParams())
                .map { Organisation(it.id.toLong(), it.adminId, it.organisationName, it.organisationSize, it.timeZone) }
        }

    override fun createOrganisation(
        organisationName: String,
        organisationSize: Int,
        timeZone: String,
    ): Long =
        dataSource.connection.use { connection ->
            AddOrganisationQuery()
                .query(
                    connection,
                    AddOrganisationParams(
                        organisationName = organisationName,
                        isActive = true,
                        organisationSize = organisationSize,
                        timeZone = timeZone,
                    ),
                ).map { it.srNo.toLong() }
                .first()
        }

    override fun getAllowedDomains(organisationId: Long): List<OrganisationDomains> =
        dataSource.connection.use { connection ->
            GetAllowedDomainsQuery()
                .query(connection, GetAllowedDomainsParams(organisationId))
                .map {
                    OrganisationDomains(
                        id = it.srNo,
                        organisationId = organisationId,
                        name = it.allowedDomain,
                        isDomainUsed = false,
                    )
                }
        }

    override fun deleteDomains(organisationId: Long) {
        dataSource.connection.use { connection ->
            DeleteAllowedDomainsCommand()
                .command(connection, DeleteAllowedDomainsParams(organisationId))
        }
    }

    override fun addOrganisationDomainMapping(
        organisationId: Long,
        domainName: String,
    ): Unit =
        dataSource.connection.use { connection ->
            AddOrganisationDomainMappingCommand()
                .command(connection, AddOrganisationDomainMappingParams(organisationId, domainName))
        }

    override fun createOrganisationAdmin(adminData: AdminData): Long =
        dataSource.connection.use { connection ->
            AddOrganisationAdminQuery()
                .query(
                    connection,
                    AddOrganisationAdminParams(
                        organisationId = adminData.organisationId,
                        firstName = adminData.firstName,
                        lastName = adminData.lastName,
                        empId = adminData.employeeId,
                        emailId = adminData.emailId,
                        contactNo = adminData.contactNo,
                    ),
                ).map { it.id }
                .first()
        }

    override fun update(
        id: Long,
        organisationName: String,
        timeZone: String,
    ) {
        dataSource.connection.use { connection ->
            UpdateOrganisationCommand()
                .command(
                    connection,
                    UpdateOrganisationParams(
                        organisationName = organisationName,
                        id = id.toInt(),
                        timeZone = timeZone,
                    ),
                )
        }
    }

    override fun updateAdminContactNo(
        id: Long,
        contactNo: String,
    ) {
        dataSource.connection.use { connection ->
            UpdateAdminContactNoCommand()
                .command(connection, UpdateAdminContactNoParams(id = id.toInt(), contactNo = contactNo))
        }
    }

    override fun getOrganisationDetails(id: Long): OrganisationDetails =
        dataSource.connection.use { connection ->
            GetOrganisationDetailsQuery()
                .query(connection, GetOrganisationDetailsParams(id.toInt()))
                .map {
                    OrganisationDetails(
                        id = it.id.toLong(),
                        name = it.name,
                        size = it.organisationSize,
                        contactNo = it.contactNo,
                        activeUsers = it.activeusers,
                        inactiveUsers = it.inactiveusers,
                        timeZone = it.timeZone,
                    )
                }.first()
        }

    override fun getAllOrganisationCount(): Int =
        dataSource.connection.use { connection ->
            GetAllOrganisationCountQuery()
                .query(
                    connection,
                    GetAllOrganisationCountParams(),
                )[0]
                .count
                ?.toInt() ?: 0
        }

    override fun getAllOrganisation(
        offset: Int,
        limit: Int,
    ): List<OrganisationData> =
        dataSource.connection.use { connection ->
            GetAllOrganisationQuery()
                .query(connection, GetAllOrganisationParams(offset = offset, limit = limit))
                .map {
                    OrganisationData(
                        date = it.createdAt,
                        adminFirstName = it.firstName,
                        adminLastName = it.lastName,
                        adminEmailId = it.emailId,
                        organisationId = it.organisationId,
                        organisationName = it.organisationName,
                        organisationSize = it.organisationSize,
                        contactNo = it.contactNo,
                        timeZone = it.organisationTimezone,
                    )
                }
        }

    override fun editGeneralSettings(
        organisationId: Long,
        isManagerReviewMandatory: Boolean,
        isAnonymousSuggestionAllowed: Boolean,
        isBiWeeklyFeedbackReminderEnabled: Boolean,
    ) {
        dataSource.connection.use { connection ->
            EditOrganisationSettingsCommand()
                .command(
                    connection,
                    EditOrganisationSettingsParams(
                        managerReviewMandatory = isManagerReviewMandatory,
                        anonymousSuggestionAllowed = isAnonymousSuggestionAllowed,
                        isBiWeeklyFeedbackReminderEnabled = isBiWeeklyFeedbackReminderEnabled,
                        id = organisationId.toInt(),
                    ),
                )
        }
    }

    override fun getGeneralSettings(organisationId: Long): OrganisationSettings =
        dataSource.connection.use { connection ->
            GetOrganisationSettingsQuery()
                .query(connection, GetOrganisationSettingsParams(id = organisationId.toInt()))
                .map {
                    OrganisationSettings(
                        organisationId = organisationId,
                        isManagerReviewMandatory = it.isManagerReviewMandatory,
                        isAnonymousSuggestionAllowed = it.isAnonymousSuggestionAllowed,
                        isBiWeeklyFeedbackReminderEnabled = it.isBiweeklyFeedbackReminderEnabled,
                        timeZone = it.timeZone,
                    )
                }.first()
        }

    override fun getFeedbackReminderSchedule(organisationId: Long): FeedbackReminderConfiguration {
        dataSource.connection.use { connection ->
            return GetFeedbackReminderConfigQuery()
                .query(connection, GetFeedbackReminderConfigParams(organisationId.toInt()))
                .first()
                .let {
                    FeedbackReminderConfiguration(
                        organisationId = it.srNo.toLong(),
                        isBiWeeklyFeedbackReminderEnabled = it.isBiweeklyFeedbackReminderEnabled,
                        lastFeedbackReminderSentDate = it.lastSentAt,
                        lastFeedbackReminderIndex = it.lastReminderIndex,
                    )
                }
        }
    }

    override fun updateFeedbackReminderSchedule(
        organisationId: Long,
        feedbackReminderIndex: Int,
        lastFeedbackReminderSent: Timestamp,
    ) {
        dataSource.connection.use { connection ->
            UpdateFeedbackReminderScheduleCommand()
                .command(
                    connection,
                    UpdateFeedbackReminderScheduleParams(
                        organisationId = organisationId,
                        lastSentAt = lastFeedbackReminderSent,
                        lastReminderIndex = feedbackReminderIndex,
                    ),
                )
        }
    }

    override fun updateTimeZone(
        organisationId: Long,
        timeZone: String,
    ) {
        dataSource.connection.use { connection ->
            UpdateOrganisationTimeZoneCommand()
                .command(
                    connection,
                    UpdateOrganisationTimeZoneParams(
                        timeZone = timeZone,
                        organisationId = organisationId.toInt(),
                    ),
                )
        }
    }
}
