package organisations

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.objectstorage.ObjectStorageOperations
import io.micronaut.objectstorage.response.UploadResponse
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import scalereal.core.kra.KRAService
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.Domain
import scalereal.core.models.domain.EmployeeData
import scalereal.core.models.domain.Organisation
import scalereal.core.models.domain.OrganisationData
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.OrganisationDomains
import scalereal.core.models.domain.OrganisationSettings
import scalereal.core.models.domain.User
import scalereal.core.models.removeExtraSpaces
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.organisations.OrganisationService
import scalereal.core.roles.RoleRepository
import scalereal.core.roles.RoleService
import scalereal.core.teams.Team
import scalereal.core.teams.TeamRepository
import scalereal.core.teams.TeamService
import scalereal.core.user.UserService
import java.sql.Date

class OrganisationServiceImplTest : StringSpec() {
    private val organisationRepository = mockk<OrganisationRepository>()
    private val userService = mockk<UserService>()
    private val emailSenderService = mockk<EmailSenderService>()
    private val teamService = mockk<TeamService>()
    private val designationService = mockk<DesignationService>()
    private val roleService = mockk<RoleService>()
    private val designationRepository = mockk<DesignationRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val teamRepository = mockk<TeamRepository>()
    private val employeeRepository = mockk<EmployeeRepository>()
    private val employeeService = mockk<EmployeeService>()
    private val kraService = mockk<KRAService>()
    private val objectStorage = mockk<ObjectStorageOperations<*, *, *>>()
    private val appConfig =
        mockk<AppConfig> {
            every { getS3BucketUrl() } returns "https://example.cloudfront.net"
        }
    private val departmentService = mockk<DepartmentService>()
    private val departmentRepository = mockk<DepartmentRepository>()
    private val superAdminNotificationMail = mockk<SuperAdminNotificationMail>()
    private val organisationService =
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

    init {

        "should fetch organisation name by organisation id" {
            val organisationId = 1L
            val organisationName = "Dummy Organisation"
            every { organisationRepository.fetchName(any()) } returns organisationName
            organisationService.fetchName(organisationId = organisationId) shouldBe organisationName
            verify(exactly = 1) { organisationRepository.fetchName(organisationId = organisationId) }
        }

        "should fetch organisation id by organisation name" {
            val organisationId = 1
            val organisationName = "Dummy Organisation"
            every { organisationRepository.getOrganisationId(any()) } returns organisationId
            organisationService.getOrganisationId(organisationName = organisationName) shouldBe organisationId
            verify(exactly = 1) { organisationRepository.getOrganisationId(organisationName = organisationName) }
        }

        "should verify if an organisation is active" {
            val emailId = "your_email@example.com"
            every { organisationRepository.isOrganisationActive(any()) } returns true
            organisationService.isOrganisationActive(emailId = emailId) shouldBe true
            verify(exactly = 1) { organisationRepository.isOrganisationActive(emailId = emailId) }
        }

        "should throw exception if trying to add organisation with invalid department" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            val exception =
                shouldThrow<IllegalArgumentException> {
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = "Dummy Department",
                        teamName = "Dummy Team",
                        designationName = "Dummy Designation",
                    )
                }
            exception.message shouldBe "Invalid department: Dummy Department"
        }

        "should throw exception if trying to add organisation with invalid team" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            val exception =
                shouldThrow<IllegalArgumentException> {
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = Department.LEADERSHIP.departmentName,
                        teamName = "Dummy Team",
                        designationName = "Dummy Designation",
                    )
                }
            exception.message shouldBe "Invalid team: Dummy Team for department: Executive Leadership"
        }

        "should throw exception if trying to add organisation with invalid designation" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            val exception =
                shouldThrow<IllegalArgumentException> {
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = Department.LEADERSHIP.departmentName,
                        teamName = Team.C_SUITE.teamName,
                        designationName = "Dummy Designation",
                    )
                }
            exception.message shouldBe "Invalid designation: Dummy Designation for team: C-Suite"
        }

        "should throw exception if trying to add organisation with invalid department-team mapping" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            val exception =
                shouldThrow<IllegalArgumentException> {
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = Department.LEADERSHIP.departmentName,
                        teamName = Team.PEOPLE_EXPERIENCE.teamName,
                        designationName = Designation.CEO.designationName,
                    )
                }
            exception.message shouldBe "Invalid team: People Experience for department: Executive Leadership"
        }

        "should throw exception if trying to add organisation with invalid team-designation mapping" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            val exception =
                shouldThrow<IllegalArgumentException> {
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = Department.LEADERSHIP.departmentName,
                        teamName = Team.SENIOR_MANAGEMENT.teamName,
                        designationName = Designation.CEO.designationName,
                    )
                }
            exception.message shouldBe "Invalid designation: Chief Executive Officer for team: Senior Management"
        }

        "should throw exception if trying to add organisation without registering admin" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            every { userService.isUserExist(any()) } returns false
            val exception =
                shouldThrow<Exception> {
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = Department.LEADERSHIP.departmentName,
                        teamName = Team.C_SUITE.teamName,
                        designationName = Designation.CEO.designationName,
                    )
                }
            exception.message shouldBe "To proceed, please add the details of the organisation admin first."
        }

        "should throw exception if organisation already exists" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            every { userService.isUserExist(any()) } returns true
            every { employeeRepository.isEmailIdExists(any()) } returns true
            val exception =
                shouldThrow<Exception> {
                    organisationService.createOrganisation(
                        userEmailId = userEmailId,
                        organisationName = organisationName,
                        organisationSize = organisationSize,
                        contactNo = contactNo,
                        departmentName = Department.LEADERSHIP.departmentName,
                        teamName = Team.C_SUITE.teamName,
                        designationName = Designation.CEO.designationName,
                    )
                }
            exception.message shouldBe "Organisation details has been already added!"
        }

        "should create new organisation" {
            val userEmailId = "your_email@example.com"
            val organisationName = "Dummy Organisation"
            val organisationSize = 75
            val contactNo = "+918869933999"
            val userDetails =
                User(
                    id = 1,
                    firstName = "Dummy",
                    lastName = "User",
                    emailId = "your_email@example.com",
                )
            every { userService.isUserExist(any()) } returns true
            every { employeeRepository.isEmailIdExists(any()) } returns false
            every { userService.getUser(any()) } returns userDetails
            every { organisationRepository.createOrganisation(any(), any(), any()) } returns 1
            every { organisationRepository.addOrganisationDomainMapping(any(), any()) } returns Unit
            every { organisationRepository.createOrganisationAdmin(any()) } returns 1
            every { employeeRepository.createEmployeeHistory(any(), any()) } returns Unit
            every { organisationRepository.addAdminId(any(), any()) } returns Unit
            every { departmentRepository.getMaxDepartmentId(any()) } returns 0
            every { teamRepository.getMaxTeamId(any()) } returns 0
            every { designationRepository.getMaxDesignationId(any()) } returns 0
            every { departmentRepository.create(any(), any(), any(), any()) } returns 1
            every { teamRepository.create(any(), any(), any(), any(), any()) } returns 1
            every { designationRepository.create(any(), any(), any(), any(), any()) } returns 1
            every { roleService.createDefaultRoles(any()) } returns Unit
            every { kraService.createDefaultKRAs(any()) } returns Unit
            every { roleRepository.getRoleId(any(), any()) } returns 1
            every { employeeRepository.createEmployeesDepartments(any(), any()) } returns Unit
            every { employeeRepository.createEmployeesTeams(any(), any(), any()) } returns Unit
            every { employeeRepository.createEmployeesDesignations(any(), any()) } returns Unit
            every { employeeRepository.createEmployeesRoles(any(), any()) } returns Unit
            every { employeeService.createManagerMappings(any(), any(), any()) } returns Unit
            every { emailSenderService.sendEmail(any(), any(), any(), any()) } returns Unit
            every { emailSenderService.welcomeSubject() } returns "Welcome Email"
            every { emailSenderService.welcomeHTML(any(), any(), any()) } returns "Welcome Email Body"
            every { emailSenderService.welcomeTEXT() } returns "Welcome Email Text"
            every { superAdminNotificationMail.organisationDetailsAddedEmail(any()) } returns Unit
            organisationService.createOrganisation(
                userEmailId = userEmailId,
                organisationName = organisationName,
                organisationSize = organisationSize,
                contactNo = contactNo,
                departmentName = Department.LEADERSHIP.departmentName,
                teamName = Team.C_SUITE.teamName,
                designationName = Designation.CEO.designationName,
            ) shouldBe Unit
            verify(
                exactly = 1,
            ) { organisationRepository.createOrganisation(organisationName.removeExtraSpaces(), organisationSize, "Asia/Kolkata") }
        }

        "should fetch all organisations" {
            val organisations = listOf(Organisation(id = 1, adminId = 1, name = "Dummy Organisation", size = 75, timeZone = "Asia/Kolkata"))
            every { organisationRepository.fetchAllOrganisations() } returns organisations
            organisationService.fetchAllOrganisations() shouldBe organisations
        }

        "should fetch organisation details by organisation id" {
            val organisationId = 1L
            val organisationDetails =
                OrganisationDetails(
                    id = 1,
                    name = "Dummy Organisation",
                    size = 75,
                    contactNo = "+918869933999",
                    activeUsers = 20,
                    inactiveUsers = 0,
                    timeZone = "Asia/Kolkata",
                )
            every { organisationRepository.getOrganisationDetails(any()) } returns organisationDetails
            organisationService.getOrganisationDetails(id = organisationId) shouldBe organisationDetails
        }

        "update function should throw exception for invalid logo type" {
            val organisationId = 1L
            val organisationName = "Dummy Organisation"
            val contactNo = "+918869933999"
            val organisationLogo = mockk<CompletedFileUpload>()
            every { organisationLogo.filename } returns "logo.txt"
            val exception =
                shouldThrow<Exception> {
                    organisationService.update(
                        id = organisationId,
                        organisationName = organisationName,
                        contactNo = contactNo,
                        timeZone = "Asia/Kolkata",
                        organisationLogo = organisationLogo,
                    )
                }
            exception.message shouldBe "The logo must be of type .svg or .png"
        }

        "should update organisation details" {
            val organisationId = 1L
            val organisationName = "Dummy Organisation Name Updated"
            val contactNo = "+918869933999"
            val timeZone = "Asia/Kolkata"
            val organisationLogo = mockk<CompletedFileUpload>()
            every { organisationLogo.filename } returns "logo.svg"
            every { organisationRepository.update(any(), any(), any()) } returns Unit
            every { organisationRepository.updateAdminContactNo(any(), any()) } returns Unit
            // Stub objectStorage.upload to return a mock UploadResponse
            every { objectStorage.upload(any()) } returns mockk<UploadResponse<Any>>()

            organisationService.update(
                id = organisationId,
                organisationName = organisationName,
                contactNo = contactNo,
                organisationLogo = organisationLogo,
                timeZone = timeZone,
            ) shouldBe Unit
            verify(exactly = 1) {
                organisationRepository.update(
                    id = organisationId,
                    organisationName = organisationName.removeExtraSpaces(),
                    timeZone = timeZone,
                )
                organisationRepository.updateAdminContactNo(id = organisationId, contactNo = contactNo)
            }
        }

        "should throw Exception while uploading duplicate contact number" {
            val organisationId = 1L
            val organisationName = "Dummy Organisation Name Updated"
            val contactNo = "+918869933999"
            val timeZone = "Asia/Kolkata"
            every { organisationRepository.update(any(), any(), any()) } returns Unit
            every {
                organisationRepository.updateAdminContactNo(any(), any())
            } throws Exception("duplicate key value violates unique constraint : employees_contact_no_key")

            val exception =
                shouldThrow<Exception> {
                    organisationService.update(
                        id = organisationId,
                        organisationName = organisationName,
                        contactNo = contactNo,
                        organisationLogo = null,
                        timeZone = timeZone,
                    )
                }
            exception.message shouldBe "This Contact Number already exists"
        }

        "should throw exception while updating invalid timezone" {
            val organisationId = 1L
            val organisationName = "Dummy Organisation Name Updated"
            val contactNo = "+918869933999"
            val timeZone = "Asia"

            val exception =
                shouldThrow<Exception> {
                    organisationService.update(
                        id = organisationId,
                        organisationName = organisationName,
                        contactNo = contactNo,
                        organisationLogo = null,
                        timeZone = timeZone,
                    )
                }
            exception.message shouldBe "Invalid time zone."
        }

        "should fetch S3 bucket URL to get organisation logo" {
            val s3BucketUrl = "https://example.cloudfront.net"
            val organisationId = 5L
            val expectedResult = mapOf("logoUrl" to "$s3BucketUrl/$organisationId")
            every { objectStorage.exists(any()) } returns true
            every { appConfig.getS3BucketUrl() } returns s3BucketUrl
            organisationService.getOrganisationLogo(id = organisationId) shouldBe expectedResult
        }

        "should throw exception if organisation logo does not exist" {
            val organisationId = 5L
            every { objectStorage.exists(any()) } returns false
            val exception =
                shouldThrow<Exception> {
                    organisationService.getOrganisationLogo(id = organisationId)
                }
            exception.message shouldBe "Logo Not Found"
        }

        "should delete organisation logo" {
            val organisationId = 5L
            every { objectStorage.delete(any()) } returns Unit
            organisationService.deleteOrganisationLogo(id = organisationId)
            verify(exactly = 1) { objectStorage.delete(organisationId.toString()) }
        }

        "should throw exception while deleting organisation logo" {
            val organisationId = 5L
            every { objectStorage.delete(any()) } throws Exception("Error while deleting logo")
            val exception = shouldThrow<Exception> { organisationService.deleteOrganisationLogo(id = organisationId) }
            exception.message shouldBe "Cannot delete logo."
        }

        "should fetch allowed domains by organisation id" {
            val organisationId = 1L
            val domains =
                listOf(
                    OrganisationDomains(id = 1, organisationId = 1, name = "@example.com", isDomainUsed = false),
                    OrganisationDomains(id = 2, organisationId = 1, name = "@gmail.com", isDomainUsed = false),
                )
            val employees =
                listOf(
                    EmployeeData(
                        organisationId = 1,
                        id = 1,
                        employeeId = "D0001",
                        firstName = "Dummy",
                        lastName = "User",
                        emailId = "your_email@example.com",
                        contactNo = "+918869933999",
                        genderId = 1,
                        dateOfBirth = null,
                        dateOfJoining = null,
                        experienceInMonths = null,
                        status = true,
                        isConsultant = false,
                        departmentName = "Executive Leadership",
                        teamName = "Org Admin",
                        designationName = "Org Admin",
                        roleName = "Org Admin",
                        firstManagerId = 1,
                        firstManagerEmployeeId = "D0001",
                        firstManagerFirstName = "Dummy",
                        firstManagerLastName = "User",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                    ),
                )
            every { organisationRepository.getAllowedDomains(any()) } returns domains
            every { employeeService.fetchAllEmployees(any()) } returns employees
            val expectedDomains = organisationService.getAllowedDomains(id = organisationId)
            expectedDomains shouldBe domains
        }

        "should add one new allowed domain for an organisation" {
            val domainsToBeSaved =
                listOf(
                    Domain(id = 1, name = "@example.com"),
                    Domain(id = 2, name = "@gmail.com"),
                    Domain(id = 3, name = "@outlook.com"),
                )
            val organisationId = 1L
            val existingDomains =
                listOf(
                    OrganisationDomains(id = 1, organisationId = 1, name = "@example.com", isDomainUsed = false),
                    OrganisationDomains(id = 2, organisationId = 1, name = "@gmail.com", isDomainUsed = false),
                )
            every { organisationRepository.getAllowedDomains(any()) } returns existingDomains
            every { organisationRepository.addOrganisationDomainMapping(any(), any()) } returns Unit
            organisationService.saveAllowedDomains(organisationId, domainsToBeSaved) shouldBe Unit
        }

        "should add multiple allowed domains for an organisation when existing domains are empty" {
            clearMocks(organisationRepository)
            val domainsToBeSaved =
                listOf(
                    Domain(id = 1, name = "@example.com"),
                    Domain(id = 2, name = "@gmail.com"),
                    Domain(id = 3, name = "@outlook.com"),
                )
            val organisationId = 1L
            every { organisationRepository.getAllowedDomains(any()) } returns emptyList()
            every { organisationRepository.addOrganisationDomainMapping(any(), any()) } returns Unit
            organisationService.saveAllowedDomains(organisationId, domainsToBeSaved) shouldBe Unit
            verify(exactly = 3) { organisationRepository.addOrganisationDomainMapping(any(), any()) }
        }

        "should not add domain in database if all domains are already present" {
            val domainsToBeSaved =
                listOf(
                    Domain(id = 1, name = "@example.com"),
                    Domain(id = 2, name = "@gmail.com"),
                )
            val organisationId = 1L
            val existingDomains =
                listOf(
                    OrganisationDomains(id = 1, organisationId = 1, name = "@example.com", isDomainUsed = false),
                    OrganisationDomains(id = 2, organisationId = 1, name = "@gmail.com", isDomainUsed = false),
                )
            every { organisationRepository.getAllowedDomains(any()) } returns existingDomains
            organisationService.saveAllowedDomains(organisationId, domainsToBeSaved) shouldBe Unit
        }

        "should delete few domains and add some new domains" {
            val domainsToBeSaved =
                listOf(
                    Domain(id = 1, name = "@example.com"),
                    Domain(id = 2, name = "@gmail.com"),
                    Domain(id = 3, name = "@outlook.com"),
                )
            val organisationId = 1L
            val existingDomains =
                listOf(
                    OrganisationDomains(id = 1, organisationId = 1, name = "@example.com", isDomainUsed = false),
                    OrganisationDomains(id = 2, organisationId = 1, name = "@gmail.com", isDomainUsed = false),
                    OrganisationDomains(id = 3, organisationId = 1, name = "@zoho.in", isDomainUsed = false),
                )
            val employees =
                listOf(
                    EmployeeData(
                        organisationId = 1,
                        id = 1,
                        employeeId = "D0001",
                        firstName = "Dummy",
                        lastName = "User",
                        emailId = "your_email@example.com",
                        contactNo = "+918869933999",
                        genderId = 1,
                        dateOfBirth = null,
                        dateOfJoining = null,
                        experienceInMonths = null,
                        status = true,
                        isConsultant = false,
                        departmentName = "Executive Leadership",
                        teamName = "Org Admin",
                        designationName = "Org Admin",
                        roleName = "Org Admin",
                        firstManagerId = 1,
                        firstManagerEmployeeId = "D0001",
                        firstManagerFirstName = "Dummy",
                        firstManagerLastName = "User",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                    ),
                )
            every { organisationRepository.getAllowedDomains(any()) } returns existingDomains
            every { employeeService.fetchAllEmployees(any()) } returns employees
            every { organisationRepository.deleteDomains(any()) } returns Unit
            every { organisationRepository.addOrganisationDomainMapping(any(), any()) } returns Unit
            organisationService.saveAllowedDomains(organisationId, domainsToBeSaved) shouldBe Unit
            verify(exactly = 1) { organisationRepository.deleteDomains(organisationId) }
        }

        "should throw exception while deleting domain that is already used" {
            val domainsToBeSaved =
                listOf(
                    Domain(id = 2, name = "@gmail.com"),
                    Domain(id = 3, name = "@outlook.com"),
                )
            val organisationId = 1L
            val existingDomains =
                listOf(
                    OrganisationDomains(id = 1, organisationId = 1, name = "@example.com", isDomainUsed = false),
                    OrganisationDomains(id = 2, organisationId = 1, name = "@gmail.com", isDomainUsed = false),
                )
            val employees =
                listOf(
                    EmployeeData(
                        organisationId = 1,
                        id = 1,
                        employeeId = "D0001",
                        firstName = "Dummy",
                        lastName = "User",
                        emailId = "your_email@example.com",
                        contactNo = "+918869933999",
                        genderId = 1,
                        dateOfBirth = null,
                        dateOfJoining = null,
                        experienceInMonths = null,
                        status = true,
                        isConsultant = false,
                        departmentName = "Executive Leadership",
                        teamName = "Org Admin",
                        designationName = "Org Admin",
                        roleName = "Org Admin",
                        firstManagerId = 1,
                        firstManagerEmployeeId = "D0001",
                        firstManagerFirstName = "Dummy",
                        firstManagerLastName = "User",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                    ),
                )
            every { organisationRepository.getAllowedDomains(any()) } returns existingDomains
            every { employeeService.fetchAllEmployees(any()) } returns employees
            val exception = shouldThrow<Exception> { organisationService.saveAllowedDomains(organisationId, domainsToBeSaved) }
            exception.message shouldBe "Deleted domains currently in use by below users \n [ Dummy User ( D0001 )]"
        }

        "should fetch general settings of organisation by organisation id" {
            val settings =
                OrganisationSettings(
                    organisationId = 1,
                    isManagerReviewMandatory = false,
                    isAnonymousSuggestionAllowed = true,
                    isBiWeeklyFeedbackReminderEnabled = true,
                    timeZone = "Asia/Kolkata",
                )
            every { organisationRepository.getGeneralSettings(any()) } returns settings
            organisationService.getGeneralSettings(organisationId = 1) shouldBe settings
            verify(exactly = 1) { organisationRepository.getGeneralSettings(organisationId = 1) }
        }

        "should throw exception while editing general settings" {
            every { organisationRepository.editGeneralSettings(any(), any(), any(), any()) } throws Exception("Exception from database.")
            val exception =
                shouldThrow<Exception> {
                    organisationService.editGeneralSettings(
                        organisationId = 1,
                        isManagerReviewMandatory = true,
                        isAnonymousSuggestionAllowed = false,
                        isBiWeeklyFeedbackReminderEnabled = false,
                    )
                }
            exception.message shouldBe "Organisation settings cannot be updated."
        }

        "should edit general settings of an organisation" {
            every { organisationRepository.editGeneralSettings(any(), any(), any(), any()) } returns Unit
            organisationService.editGeneralSettings(
                organisationId = 1,
                isManagerReviewMandatory = false,
                isAnonymousSuggestionAllowed = false,
                isBiWeeklyFeedbackReminderEnabled = false,
            ) shouldBe Unit
            verify(exactly = 1) {
                organisationRepository.editGeneralSettings(
                    organisationId = 1,
                    isManagerReviewMandatory = false,
                    isAnonymousSuggestionAllowed = false,
                    isBiWeeklyFeedbackReminderEnabled = false,
                )
            }
        }

        "should fetch all organisation and organisation admin details" {
            val organisationData =
                listOf(
                    OrganisationData(
                        date = Date.valueOf("2023-11-11"),
                        adminFirstName = "Antonia Galloway",
                        adminLastName = "Ezra Hess",
                        adminEmailId = "bernadette.trujillo@example.com",
                        organisationId = null,
                        organisationName = null,
                        organisationSize = null,
                        contactNo = null,
                        timeZone = "Asia/Kolkata",
                    ),
                    OrganisationData(
                        date = Date.valueOf("2023-11-11"),
                        adminFirstName = "Dummy",
                        adminLastName = "User",
                        adminEmailId = "your_email@example.com",
                        organisationId = 1,
                        organisationName = "Dummy Organisation",
                        organisationSize = 75,
                        contactNo = "+918869933999",
                        timeZone = "Asia/Kolkata",
                    ),
                )
            every { organisationRepository.getAllOrganisation(any(), any()) } returns organisationData
            organisationService.getAllOrganisation(page = 1, limit = Int.MAX_VALUE) shouldBe organisationData
            verify(exactly = 1) { organisationRepository.getAllOrganisation(offset = 0, limit = Int.MAX_VALUE) }
        }

        "should return all organisation count" {
            val count = 2
            every { organisationRepository.getAllOrganisationCount() } returns count
            organisationService.getAllOrganisationCount() shouldBe count
            verify(exactly = 1) { organisationRepository.getAllOrganisationCount() }
        }
    }
}
