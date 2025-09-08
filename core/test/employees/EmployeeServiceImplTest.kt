package employees

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.emails.EmailSenderService
import scalereal.core.employees.EmployeeRepository
import scalereal.core.employees.EmployeeService
import scalereal.core.exception.UserNotFoundException
import scalereal.core.models.EmployeeGender
import scalereal.core.models.domain.EmpData
import scalereal.core.models.domain.Employee
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.ContactNumberValidator
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.roles.RoleRepository
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Date

class EmployeeServiceImplTest : StringSpec() {
    private val employeeRepository = mockk<EmployeeRepository>()
    private val emailSenderService = mockk<EmailSenderService>()
    private val organisationRepository = mockk<OrganisationRepository>()
    private val contactNumberValidator = mockk<ContactNumberValidator>()
    private val roleRepository = mockk<RoleRepository>()
    private val userActivityRepository = mockk<UserActivityRepository>()
    private val departmentRepository = mockk<DepartmentRepository>()
    private val teamRepository = mockk<TeamRepository>()
    private val designationRepository = mockk<DesignationRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.EMPLOYEES.moduleName) } returns 6
            every { fetchModuleId(Modules.REVIEW_FOR_TEAM.moduleName) } returns 8
            every { fetchModuleId(Modules.CHECK_IN_WITH_TEAM.moduleName) } returns 9
        }
    private val reviewCycleRepository = mockk<ReviewCycleRepository>()
    private val service =
        EmployeeService(
            employeeRepository,
            emailSenderService,
            organisationRepository,
            contactNumberValidator,
            roleRepository,
            teamRepository,
            departmentRepository,
            designationRepository,
            moduleService,
            userActivityRepository,
            reviewCycleRepository,
        )

    init {

        "should get employee data by entering email id" {
            val employee =
                Employee(
                    organisationId = 1,
                    id = 1,
                    firstName = "grv",
                    lastName = "abc",
                    emailId = "gp.aarush@gmail.com",
                    contactNo = "+917389543155",
                    employeeId = "SR0049",
                    departmentName = "Engineering",
                    teamName = "Backend",
                    designationName = "sd2",
                    roleName = "software engineer",
                    modulePermission = listOf(),
                    onboardingFlow = false,
                )
            val emailId = "gp.aarush@gmail.com"
            every { employeeRepository.isEmployeeExists(emailId) } returns true
            every { employeeRepository.fetchByEmailId(emailId) } returns employee
            service.fetchByEmailId(emailId) shouldBe employee
            verify(exactly = 1) { employeeRepository.fetchByEmailId(emailId) }
        }

        "should throw exception when unauthorised user tries to log in" {
            val emailId = "syed@scalereal.com"
            every { employeeRepository.fetchByEmailId(emailId) } returns null
            every { employeeRepository.isEmployeeExists(emailId) } returns false
            val exception = shouldThrow<UserNotFoundException> { service.fetchByEmailId(emailId) }
            exception.message shouldBe "Unauthorized access! Please contact System Admin/HR."
        }

        "should fetch employee data by id" {
            val employee =
                EmpData(
                    organisationId = 1,
                    id = 1,
                    firstName = "grv",
                    lastName = "abc",
                    emailId = "gp.aarush@gmail.com",
                    contactNo = "+917389543155",
                    genderId = EmployeeGender.MALE.genderId,
                    dateOfBirth = Date.valueOf("1998-05-01"),
                    dateOfJoining = Date.valueOf("2022-06-13"),
                    experienceInMonths = 8,
                    employeeId = "SR0049",
                    status = true,
                    isConsultant = false,
                    teamId = 1,
                    departmentId = 1,
                    departmentName = "Engineering",
                    teamName = "Backend",
                    designationId = 2,
                    designationName = "sd2",
                    roleId = 1,
                    roleName = "software engineer",
                    firstManagerId = 4,
                    firstManagerEmployeeId = "SR0006",
                    firstManagerFirstName = "Yogesh",
                    firstManagerLastName = "Jadhav",
                    secondManagerId = null,
                    secondManagerEmployeeId = null,
                    secondManagerFirstName = null,
                    secondManagerLastName = null,
                    employeeNameWithEmployeeId = "grv abc (SR0049)",
                )
            val id: Long = 1
            every { employeeRepository.getEmployeeById(id) } returns employee
            service.getEmployeeById(id) shouldBe employee
            verify(exactly = 1) { employeeRepository.getEmployeeById(id) }
        }
    }
}
