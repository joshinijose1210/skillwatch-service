package organisations

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import io.micronaut.objectstorage.ObjectStorageOperations
import io.mockk.every
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.departments.DepartmentRepository
import scalereal.core.departments.DepartmentService
import scalereal.core.designations.DesignationRepository
import scalereal.core.designations.DesignationService
import scalereal.core.emails.EmailSenderService
import scalereal.core.emails.SuperAdminNotificationMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.employees.EmployeeService
import scalereal.core.kra.KRAService
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.AdminData
import scalereal.core.models.domain.Organisation
import scalereal.core.models.domain.OrganisationData
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.OrganisationDomains
import scalereal.core.models.domain.OrganisationSettings
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.organisations.OrganisationService
import scalereal.core.roles.RoleRepository
import scalereal.core.roles.RoleService
import scalereal.core.teams.TeamRepository
import scalereal.core.teams.TeamService
import scalereal.core.user.UserService
import scalereal.db.organisations.OrganisationRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Date
import java.time.LocalDate

class OrganisationRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var organisationRepositoryImpl: OrganisationRepositoryImpl
    private lateinit var organisationService: OrganisationService
    private val userService = mockk<UserService>()
    private val emailSenderService = mockk<EmailSenderService>()
    private val teamService = mockk<TeamService>()
    private val designationService = mockk<DesignationService>()
    private val roleService = mockk<RoleService>()
    private val kraService = mockk<KRAService>()
    private val designationRepository = mockk<DesignationRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val teamRepository = mockk<TeamRepository>()
    private val organisationRepository = mockk<OrganisationRepository>()
    private val employeeRepository = mockk<EmployeeRepository>()
    private val employeeService = mockk<EmployeeService>()
    private val objectStorage = mockk<ObjectStorageOperations<*, *, *>>()
    private val appConfig =
        mockk<AppConfig> {
            every { getS3BucketUrl() } returns "https://example.cloudfront.net"
        }
    private val departmentService = mockk<DepartmentService>()
    private val departmentRepository = mockk<DepartmentRepository>()
    private val superAdminNotificationMail = mockk<SuperAdminNotificationMail>()

    init {

        "should check if organisation is active or not by email id of organisation's employee" {
            organisationRepositoryImpl.isOrganisationActive(emailId = "your_example@gmail.com") shouldBe false
        }

        "should add new organisation" {
            val organisationName = "ScaleReal Technologies Pvt. Ltd."
            val organisationSize = 100
            val timeZone = "Asia/Kolkata"
            val organisationId = organisationRepositoryImpl.createOrganisation(organisationName, organisationSize, timeZone)
            organisationId shouldBe 1
        }

        "should update organisation time-zone" {
            val organisationId: Long = 1
            val timeZone = "Europe/Samara"
            organisationRepositoryImpl.updateTimeZone(organisationId, timeZone)
        }

        "should fetch organisation name by organisation id" {
            organisationRepositoryImpl.fetchName(organisationId = 1) shouldBe "ScaleReal Technologies Pvt. Ltd."
        }

        "should add organisation admin" {
            val adminData =
                AdminData(
                    firstName = "John",
                    lastName = "Cruise",
                    employeeId = "SR0001",
                    emailId = "johncruise@scalereal.com",
                    contactNo = "+918577974336",
                    organisationId = 1,
                )
            organisationRepositoryImpl.createOrganisationAdmin(adminData) shouldBe 1
        }

        "should add admin id in organisation table" {
            organisationRepositoryImpl.addAdminId(adminId = 1, organisationId = 1) shouldBe Unit
        }

        "should fetch organisation id by organisation name" {
            organisationRepositoryImpl.getOrganisationId(organisationName = "ScaleReal Technologies Pvt. Ltd.") shouldBe 1
        }

        "should fetch organisation id by employee's email id" {
            organisationRepositoryImpl.getOrganisationIdByEmailId(emailId = "johncruise@scalereal.com") shouldBe 1
        }

        "should get all organisations" {
            val expectedOrganisations =
                listOf(
                    Organisation(
                        id = 1,
                        adminId = 1,
                        name = "ScaleReal Technologies Pvt. Ltd.",
                        size = 100,
                        timeZone = "Europe/Samara",
                    ),
                )
            val actualOrganisations = organisationRepositoryImpl.fetchAllOrganisations()
            actualOrganisations shouldBe expectedOrganisations
        }

        "should update organisationName by organisation id" {
            organisationRepositoryImpl.update(
                id = 1,
                organisationName = "Dummy Organisation",
                timeZone = "Europe/Samara",
            ) shouldBe Unit
        }

        "should update organisation's admin contactNo by organisation id" {
            organisationRepositoryImpl.updateAdminContactNo(
                id = 1,
                contactNo = "+918888899999",
            ) shouldBe Unit
        }

        "should get updated organisation details by organisation id" {
            val expectedDetails =
                OrganisationDetails(
                    id = 1,
                    name = "Dummy Organisation",
                    size = 100,
                    contactNo = "+918888899999",
                    activeUsers = 1,
                    inactiveUsers = 0,
                    timeZone = "Europe/Samara",
                )
            val actualDetails = organisationRepositoryImpl.getOrganisationDetails(1)
            actualDetails shouldBe expectedDetails
        }

        "should add allowed domain for organisation by organisation id" {
            organisationRepositoryImpl.addOrganisationDomainMapping(
                organisationId = 1,
                domainName = "@scalereal.com",
            ) shouldBe Unit
        }

        "should add another allowed domain for organisation by organisation id" {
            organisationRepositoryImpl.addOrganisationDomainMapping(
                organisationId = 1,
                domainName = "@gmail.com",
            ) shouldBe Unit
        }

        "should fetch allowed domains by organisation id" {
            val expectedDomains =
                listOf(
                    OrganisationDomains(
                        id = 1,
                        organisationId = 1,
                        name = "@scalereal.com",
                        isDomainUsed = false,
                    ),
                    OrganisationDomains(
                        id = 2,
                        organisationId = 1,
                        name = "@gmail.com",
                        isDomainUsed = false,
                    ),
                )
            organisationRepositoryImpl.getAllowedDomains(organisationId = 1) shouldBe expectedDomains
        }

        "should delete allowed domains by organisation id" {
            organisationRepositoryImpl.deleteDomains(organisationId = 1) shouldBe Unit
        }

        "should return empty list after deleting allowed domains" {
            organisationRepositoryImpl.getAllowedDomains(organisationId = 1).isEmpty() shouldBe true
        }

        "should fetch default general settings of organisation by organisation id" {
            val expectedSettings =
                OrganisationSettings(
                    organisationId = 1,
                    isManagerReviewMandatory = false,
                    isAnonymousSuggestionAllowed = true,
                    isBiWeeklyFeedbackReminderEnabled = true,
                    timeZone = "Europe/Samara",
                )
            organisationRepositoryImpl.getGeneralSettings(organisationId = 1) shouldBe expectedSettings
        }

        "should update general settings of organisation by organisation id" {
            organisationRepositoryImpl.editGeneralSettings(
                organisationId = 1,
                isManagerReviewMandatory = true,
                isAnonymousSuggestionAllowed = false,
                isBiWeeklyFeedbackReminderEnabled = false,
            ) shouldBe Unit
        }

        "should return updated general settings of organisation by organisation id" {
            val expectedSettings =
                OrganisationSettings(
                    organisationId = 1,
                    isManagerReviewMandatory = true,
                    isAnonymousSuggestionAllowed = false,
                    isBiWeeklyFeedbackReminderEnabled = false,
                    timeZone = "Europe/Samara",
                )
            organisationRepositoryImpl.getGeneralSettings(organisationId = 1) shouldBe expectedSettings
        }

        "should return count and get all user-organisation details" {
            val userOrganisationCount = organisationRepositoryImpl.getAllOrganisationCount()
            val userOrganisationDetails = organisationRepositoryImpl.getAllOrganisation(offset = 0, limit = Int.MAX_VALUE)

            val expectedDetails =
                listOf(
                    OrganisationData(
                        date = Date.valueOf(LocalDate.now()),
                        adminFirstName = "Dummy",
                        adminLastName = "User",
                        adminEmailId = "dummy.user@scalereal.com",
                        organisationId = null,
                        organisationName = null,
                        organisationSize = null,
                        contactNo = null,
                        timeZone = null,
                    ),
                    OrganisationData(
                        date = Date.valueOf(LocalDate.now()),
                        adminFirstName = "John",
                        adminLastName = "Cruise",
                        adminEmailId = "johncruise@scalereal.com",
                        organisationId = 1,
                        organisationName = "Dummy Organisation",
                        organisationSize = 100,
                        contactNo = "+918888899999",
                        timeZone = "Europe/Samara",
                    ),
                )

            userOrganisationDetails.size shouldBe userOrganisationCount
            userOrganisationDetails shouldBe expectedDetails
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO users (email_id, first_name, last_name)
                VALUES ('johncruise@scalereal.com', 'John', 'Cruise'),
                       ('dummy.user@scalereal.com', 'Dummy', 'User');
                """.trimIndent(),
            )
        }
        organisationRepositoryImpl = OrganisationRepositoryImpl(dataSource)
        organisationService =
            OrganisationService(
                objectStorage,
                organisationRepository,
                userService,
                emailSenderService,
                teamService,
                designationService,
                roleService,
                designationRepository,
                roleRepository,
                teamRepository,
                employeeService,
                employeeRepository,
                appConfig,
                departmentService,
                departmentRepository,
                superAdminNotificationMail,
                kraService,
            )
    }
}
