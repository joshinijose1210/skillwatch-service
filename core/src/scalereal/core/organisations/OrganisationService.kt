package scalereal.core.organisations

import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.objectstorage.ObjectStorageOperations
import io.micronaut.objectstorage.request.UploadRequest
import jakarta.inject.Singleton
import scalereal.core.departments.Department
import scalereal.core.departments.DepartmentRepository
import scalereal.core.departments.DepartmentService
import scalereal.core.designations.Designation
import scalereal.core.designations.DesignationRepository
import scalereal.core.designations.DesignationService
import scalereal.core.emails.EmailSenderService
import scalereal.core.emails.SuperAdminNotificationMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.employees.EmployeeService
import scalereal.core.exception.DomainInUseException
import scalereal.core.exception.DuplicateDataException
import scalereal.core.kra.KRAService
import scalereal.core.models.AppConfig
import scalereal.core.models.Constants
import scalereal.core.models.domain.AdminData
import scalereal.core.models.domain.Domain
import scalereal.core.models.domain.EmployeeData
import scalereal.core.models.domain.FeedbackReminderConfiguration
import scalereal.core.models.domain.Organisation
import scalereal.core.models.domain.OrganisationData
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.OrganisationDomains
import scalereal.core.models.domain.OrganisationSettings
import scalereal.core.models.removeExtraSpaces
import scalereal.core.roles.RoleRepository
import scalereal.core.roles.RoleService
import scalereal.core.roles.Roles
import scalereal.core.teams.Team
import scalereal.core.teams.TeamRepository
import scalereal.core.teams.TeamService
import scalereal.core.user.UserService
import java.sql.Timestamp
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.util.Base64

@Singleton
class OrganisationService(
    private val objectStorage: ObjectStorageOperations<*, *, *>,
    private val organisationRepository: OrganisationRepository,
    private val userService: UserService,
    private val emailSenderService: EmailSenderService,
    private val teamService: TeamService,
    private val designationService: DesignationService,
    private val roleService: RoleService,
    private val designationRepository: DesignationRepository,
    private val roleRepository: RoleRepository,
    private val teamRepository: TeamRepository,
    private val employeeService: EmployeeService,
    private val employeeRepository: EmployeeRepository,
    private var appConfig: AppConfig,
    private val departmentService: DepartmentService,
    private val departmentRepository: DepartmentRepository,
    private val superAdminNotificationMail: SuperAdminNotificationMail,
    private val kraService: KRAService,
) {
    private val getS3BucketUrl = appConfig.getS3BucketUrl()

    fun fetchName(organisationId: Long): String = organisationRepository.fetchName(organisationId)

    fun getOrganisationId(organisationName: String) = organisationRepository.getOrganisationId(organisationName)

    private fun addAdminId(
        adminId: Long,
        organisationId: Long,
    ) = organisationRepository.addAdminId(adminId, organisationId)

    fun isOrganisationActive(emailId: Any) = organisationRepository.isOrganisationActive(emailId)

    fun fetchAllOrganisations(): List<Organisation> = organisationRepository.fetchAllOrganisations()

    fun createOrganisation(
        userEmailId: String,
        organisationName: String,
        organisationSize: Int,
        contactNo: String,
        timeZone: String? = "Asia/Kolkata",
        departmentName: String,
        teamName: String,
        designationName: String,
    ) {
        val department =
            Department.entries.find { it.departmentName.equals(departmentName, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid department: $departmentName")

        val team =
            Team.entries.find { it.teamName.equals(teamName, ignoreCase = true) && it.departmentId == department.departmentId }
                ?: throw IllegalArgumentException("Invalid team: $teamName for department: ${department.departmentName}")

        val designation =
            Designation.entries.find { it.designationName.equals(designationName, ignoreCase = true) && it.teamId == team.teamId }
                ?: throw IllegalArgumentException("Invalid designation: $designationName for team: ${team.teamName}")

        when {
            (!userService.isUserExist(userEmailId)) ->
                throw Exception("To proceed, please add the details of the organisation admin first.")
            (employeeRepository.isEmailIdExists(userEmailId)) ->
                throw Exception("Organisation details has been already added!")
        }
        val words = organisationName.split(" ")
        val initials =
            if (words.size == 1) {
                words.first().take(1).uppercase()
            } else {
                words
                    .take(2)
                    .map { it.first() }
                    .joinToString("")
                    .uppercase()
            }

        val userDetails = userService.getUser(userEmailId)
        val organisationId =
            organisationRepository.createOrganisation(
                organisationName = organisationName.removeExtraSpaces(),
                organisationSize = organisationSize,
                timeZone = timeZone ?: "Asia/Kolkata",
            )

        val adminData =
            AdminData(
                firstName = userDetails.firstName,
                lastName = userDetails.lastName,
                employeeId = "${initials}0001",
                emailId = userDetails.emailId,
                contactNo = contactNo,
                organisationId = organisationId,
            )

        val organisationData =
            OrganisationData(
                date = null,
                adminFirstName = adminData.firstName,
                adminLastName = adminData.lastName,
                adminEmailId = adminData.emailId,
                organisationId = organisationId.toInt(),
                organisationName = organisationName,
                organisationSize = organisationSize,
                contactNo = contactNo,
                timeZone = timeZone ?: "Asia/Kolkata",
            )
        val domainName = adminData.emailId.substring(adminData.emailId.indexOf('@'))
        addOrganisationDomainMapping(organisationId, domainName)

        val organisationAdminId = createOrganisationAdmin(adminData)
        addAdminId(organisationAdminId, organisationId)
        createBasicOrganisationSetup(
            organisationId = organisationId,
            organisationAdminId = organisationAdminId,
            departmentName = department.departmentName,
            teamName = team.teamName,
            designationName = designation.designationName,
        )
        sendNotification(organisationData)
    }

    private fun sendNotification(organisationData: OrganisationData) {
        val encodedEmailId = Base64.getEncoder().encodeToString(organisationData.adminEmailId.toByteArray())
        emailSenderService.sendEmail(
            receiver = organisationData.adminEmailId,
            subject = emailSenderService.welcomeSubject(),
            htmlBody =
                emailSenderService.welcomeHTML(
                    organisationData.adminFirstName,
                    encodedEmailId,
                    Constants.APPLICATION_NAME,
                ),
            textBody = emailSenderService.welcomeTEXT(),
        )

        // Sending email to super admin
        superAdminNotificationMail.organisationDetailsAddedEmail(organisationData)
    }

    private fun addOrganisationDomainMapping(
        organisationId: Long,
        domainName: String,
    ) = organisationRepository.addOrganisationDomainMapping(organisationId, domainName)

    private fun createOrganisationAdmin(adminData: AdminData): Long {
        val adminId = organisationRepository.createOrganisationAdmin(adminData)
        employeeRepository.createEmployeeHistory(id = adminId, activatedAt = Timestamp.from(Instant.now()))
        return adminId
    }

    private fun createBasicOrganisationSetup(
        organisationId: Long,
        organisationAdminId: Long,
        departmentName: String,
        teamName: String,
        designationName: String,
    ) {
        val maxDepartmentId = departmentRepository.getMaxDepartmentId(organisationId)
        val departmentId =
            departmentRepository.create(
                organisationId = organisationId,
                id = maxDepartmentId + 1,
                departmentName = departmentName,
                departmentStatus = true,
            )

        val maxTeamId = teamRepository.getMaxTeamId(organisationId)
        val teamId =
            teamRepository.create(
                organisationId = organisationId,
                id = maxTeamId + 1,
                departmentId = departmentId,
                teamName = teamName,
                teamStatus = true,
            )

        val maxDesignationId = designationRepository.getMaxDesignationId(organisationId)
        val designationId =
            designationRepository.create(
                organisationId = organisationId,
                id = maxDesignationId + 1,
                teamId = teamId,
                designationName = designationName,
                status = true,
            )
//        departmentService.createDefaultDepartments(organisationId = organisationId)
//        teamService.createDefaultTeams(organisationId = organisationId)
//        designationService.createDefaultDesignations(organisationId = organisationId)
        roleService.createDefaultRoles(organisationId = organisationId)
        kraService.createDefaultKRAs(organisationId = organisationId)
//        val departmentId = departmentRepository.getDepartmentId(organisationId, Departments.EXECUTIVE_LEADERSHIP)
//        val teamId = teamRepository.getTeamId(organisationId, departmentId, Teams.ORG_ADMIN)
//        val designationId = designationRepository.getDesignationId(organisationId, Designations.ORG_ADMIN, teamId)
        val roleId = roleRepository.getRoleId(organisationId, Roles.ORG_ADMIN)
        employeeRepository.createEmployeesDepartments(id = organisationAdminId, departmentId = departmentId)
        employeeRepository.createEmployeesTeams(organisationAdminId, teamId, joinedAt = Timestamp.from(Instant.now()))
        employeeRepository.createEmployeesRoles(organisationAdminId, roleId)
        employeeRepository.createEmployeesDesignations(organisationAdminId, designationId)
        employeeService.createManagerMappings(organisationAdminId, organisationAdminId, null)
    }

    fun update(
        id: Long,
        organisationName: String,
        contactNo: String,
        timeZone: String,
        organisationLogo: CompletedFileUpload?,
    ) {
        organisationLogo?.let { logo ->
            require(logo.filename.endsWith(".svg") || logo.filename.endsWith(".png")) {
                "The logo must be of type .svg or .png"
            }
            val request = UploadRequest.fromCompletedFileUpload(logo, id.toString())
            objectStorage.upload(request)
        }
        try {
            if (!isValidTimeZone(timeZone)) throw DateTimeException("Invalid time zone.")
            organisationRepository.update(id, organisationName.removeExtraSpaces(), timeZone)
            organisationRepository.updateAdminContactNo(id, contactNo)
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("employees_contact_no_key")
                -> throw DuplicateDataException("This Contact Number already exists")
                else -> throw e
            }
        }
    }

    fun getOrganisationDetails(id: Long): OrganisationDetails = organisationRepository.getOrganisationDetails(id)

    fun getOrganisationLogo(id: Long): Map<String, String> {
        val isLogoExists = objectStorage.exists(id.toString())
        return if (isLogoExists) {
            mapOf("logoUrl" to "$getS3BucketUrl/$id")
        } else {
            throw Exception("Logo Not Found")
        }
    }

    fun deleteOrganisationLogo(id: Long) {
        try {
            objectStorage.delete(id.toString())
        } catch (_: Exception) {
            throw Exception("Cannot delete logo.")
        }
    }

    fun getAllowedDomains(id: Long): List<OrganisationDomains> {
        val domains = organisationRepository.getAllowedDomains(id)
        val allEmployees = employeeService.fetchAllEmployees(id)

        for (domain in domains) {
            val isUsed = allEmployees.any { it.emailId.lowercase().endsWith(domain.name.lowercase()) }
            if (isUsed) domain.isDomainUsed = true
        }
        return domains
    }

    fun saveAllowedDomains(
        organisationId: Long,
        domains: List<Domain>,
    ) {
        val existingDomains = organisationRepository.getAllowedDomains(organisationId)

        val existingDomainsMapped = if (existingDomains.isNotEmpty()) existingDomains.map { Domain(it.id, it.name) } else emptyList()

        when {
            existingDomains.isEmpty() -> saveDomains(domains, organisationId)

            isDomainsAlreadyPresentInDB(existingDomainsMapped, domains) -> {} // Nothing new added than existing

            // If removed existing domains and added new
            existingDomainsMapped.subtract(domains.toSet()).isNotEmpty() -> {
                val deletedDomainUsingEmployees = checkIfDeletedDomainHasEmployees(existingDomainsMapped, domains, organisationId)

                if (deletedDomainUsingEmployees.isNotEmpty()) {
                    throw DomainInUseException(
                        "Deleted domains currently in use by below users \n" +
                            " ${deletedDomainUsingEmployees.map { it.getEmployeeNameWithEmployeeId() }}",
                    )
                } else {
                    deleteDomain(
                        existingDomainsMapped.subtract(domains.toSet()).toList(),
                        organisationId,
                    )
                    saveDomains(domains, organisationId)
                }
            }

            // if added new domains
            domains.subtract(existingDomainsMapped.toSet()).isNotEmpty() ->
                saveDomains(
                    domains.subtract(existingDomainsMapped.toSet()).toList(),
                    organisationId,
                )
        }
    }

    private fun saveDomains(
        domains: List<Domain>,
        organisationId: Long,
    ) = domains.forEach { domain ->
        organisationRepository.addOrganisationDomainMapping(organisationId, domain.name)
    }

    private fun deleteDomain(
        domains: List<Domain>,
        organisationId: Long,
    ) = domains.forEach {
        organisationRepository.deleteDomains(organisationId)
    }

    private fun isDomainsAlreadyPresentInDB(
        existingDomainsMapped: List<Domain>,
        domains: List<Domain>,
    ) = existingDomainsMapped.toTypedArray().contentEquals(domains.toTypedArray())

    private fun checkIfDeletedDomainHasEmployees(
        existingDomainsMapped: List<Domain>,
        domains: List<Domain>,
        organisationId: Long,
    ): List<EmployeeData> {
        val allEmployees = employeeService.fetchAllEmployees(organisationId)
        val deletedDomainsList = existingDomainsMapped.subtract(domains.toSet()).map { it.name.lowercase() }

        return allEmployees.filter { employee ->
            employee.emailId.lowercase().substring(employee.emailId.indexOf("@")) in deletedDomainsList
        }
    }

    fun getAllOrganisationCount(): Int = organisationRepository.getAllOrganisationCount()

    fun getAllOrganisation(
        page: Int,
        limit: Int,
    ): List<OrganisationData> =
        organisationRepository.getAllOrganisation(
            offset = (page - 1) * limit,
            limit = limit,
        )

    fun editGeneralSettings(
        organisationId: Long,
        isManagerReviewMandatory: Boolean,
        isAnonymousSuggestionAllowed: Boolean,
        isBiWeeklyFeedbackReminderEnabled: Boolean,
    ) = try {
        organisationRepository.editGeneralSettings(
            organisationId,
            isManagerReviewMandatory,
            isAnonymousSuggestionAllowed,
            isBiWeeklyFeedbackReminderEnabled,
        )
    } catch (_: Exception) {
        throw Exception("Organisation settings cannot be updated.")
    }

    fun getGeneralSettings(organisationId: Long): OrganisationSettings = organisationRepository.getGeneralSettings(organisationId)

    fun getFeedbackReminderSchedule(organisationId: Long): FeedbackReminderConfiguration =
        organisationRepository.getFeedbackReminderSchedule(organisationId)

    fun updateFeedbackReminderSchedule(
        organisationId: Long,
        feedbackReminderIndex: Int,
        lastFeedbackReminderSent: Timestamp,
    ) = organisationRepository.updateFeedbackReminderSchedule(organisationId, feedbackReminderIndex, lastFeedbackReminderSent)

    fun updateTimeZone(
        organisationId: Long,
        timeZone: String,
    ) = try {
        if (!isValidTimeZone(timeZone)) throw DateTimeException("Invalid time zone.")
        organisationRepository.updateTimeZone(organisationId, timeZone)
    } catch (e: DateTimeException) {
        throw e
    } catch (_: Exception) {
        throw Exception("Organisation time zone cannot be updated.")
    }

    fun isValidTimeZone(timeZone: String): Boolean =
        try {
            ZoneId.of(timeZone)
            true
        } catch (_: DateTimeException) {
            false
        }
}
