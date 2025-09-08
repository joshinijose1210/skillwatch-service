package employees

import io.kotest.core.spec.Spec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.emails.EmailSenderService
import scalereal.core.employees.EmployeeRepository
import scalereal.core.employees.EmployeeService
import scalereal.core.models.domain.ManagerUpdateDataList
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.ContactNumberValidator
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.roles.RoleRepository
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import scalereal.db.employees.EmployeeRepositoryImpl
import scalereal.db.roles.RoleRepositoryImpl
import util.StringSpecWithDataSource

class EmployeeRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var employeeRepositoryImpl: EmployeeRepositoryImpl
    private lateinit var employeeService: EmployeeService
    private lateinit var roleRepositoryImpl: RoleRepositoryImpl
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

    init {

        "should get employee data by entering email id" {
            val employee = employeeRepositoryImpl.fetchByEmailId(emailId = "gp.aarush@gmail.com")

            employee?.firstName shouldBe "grv"
            employee?.lastName shouldBe "abc"
            employee?.emailId shouldBe "gp.aarush@gmail.com"
            employee?.employeeId shouldBe "SR0049"
            employee?.contactNo shouldBe "+917389543155"
            employee?.departmentName shouldBe "Engineering"
            employee?.teamName shouldBe "Backend"
            employee?.designationName shouldBe "sd22"
            employee?.roleName shouldBe "Backend manager"
            employee?.modulePermission shouldBe roleRepositoryImpl.fetchPermissions(roleId = 1)
            employee?.onboardingFlow shouldBe false
        }

        "should fetch employee manager listing" {
            val employeeManagerList =
                employeeRepositoryImpl.getEmployeeManagerList(
                    organisationId = 1,
                    id = listOf(-99),
                    firstManagerId = listOf(4),
                    secondManagerId = listOf(-99),
                    limit = Int.MAX_VALUE,
                    offset = 0,
                )

            employeeManagerList[0].employeeId shouldBe "SR0043"
            employeeManagerList[0].firstName shouldBe "Syed Ubed"
            employeeManagerList[0].lastName shouldBe "Ali"
            employeeManagerList[0].emailId shouldBe "syed.ali@scalereal.com"
            employeeManagerList[0].firstManagerEmployeeId shouldBe "SR0006"
            employeeManagerList[0].firstManagerFirstName shouldBe "Yogesh"
            employeeManagerList[0].firstManagerLastName shouldBe "Jadhav"

            employeeManagerList[1].employeeId shouldBe "SR0049"
            employeeManagerList[1].firstName shouldBe "grv"
            employeeManagerList[1].lastName shouldBe "abc"
            employeeManagerList[1].emailId shouldBe "gp.aarush@gmail.com"
            employeeManagerList[1].firstManagerEmployeeId shouldBe "SR0006"
            employeeManagerList[1].firstManagerFirstName shouldBe "Yogesh"
            employeeManagerList[1].firstManagerLastName shouldBe "Jadhav"

            employeeManagerList[2].employeeId shouldBe "SR0051"
            employeeManagerList[2].firstName shouldBe "Rushad"
            employeeManagerList[2].lastName shouldBe "Shaikh"
            employeeManagerList[2].emailId shouldBe "rushad.shaikh@scalereal.com"
            employeeManagerList[2].firstManagerEmployeeId shouldBe "SR0006"
            employeeManagerList[2].firstManagerFirstName shouldBe "Yogesh"
            employeeManagerList[2].firstManagerLastName shouldBe "Jadhav"
        }

        "should fetch list of first managers" {
            val managersList = employeeRepositoryImpl.getManagers(1)

            managersList[1].employeeId shouldBe "SR0006"
            managersList[1].firstName shouldBe "Yogesh"
            managersList[1].lastName shouldBe "Jadhav"
            managersList[1].emailId shouldBe "yogesh.jadhav@scalereal.com"
            managersList[1].contactNo shouldBe "+919876543210"

            managersList[0].employeeId shouldBe "SR0049"
            managersList[0].firstName shouldBe "grv"
            managersList[0].lastName shouldBe "abc"
            managersList[0].emailId shouldBe "gp.aarush@gmail.com"
            managersList[0].contactNo shouldBe "+917389543155"
        }

        "should update the reportees of the manager" {
            val managerUpdateData =
                listOf(
                    ManagerUpdateDataList(
                        currentManagerId = 1L,
                        employeeId = 2L,
                        newManagerId = 3L,
                    ),
                )

            employeeRepositoryImpl.updateEmployeesManager(managerUpdateData)
        }

        "fetch the manager list after deactivation" {
            val managersList = employeeRepositoryImpl.getManagers(1)

            managersList[0].employeeId shouldBe "SR0051"
            managersList[0].firstName shouldBe "Rushad"
            managersList[0].lastName shouldBe "Shaikh"
            managersList[0].emailId shouldBe "rushad.shaikh@scalereal.com"
            managersList[0].contactNo shouldBe "+918265079426"

            managersList[1].employeeId shouldBe "SR0006"
            managersList[1].firstName shouldBe "Yogesh"
            managersList[1].lastName shouldBe "Jadhav"
            managersList[1].emailId shouldBe "yogesh.jadhav@scalereal.com"
            managersList[1].contactNo shouldBe "+919876543210"
        }

        "should fetch list of employees by manager id" {
            val reporteesList = employeeRepositoryImpl.fetchEmployeesByManager(organisationId = 1, managerId = 4, offset = 0, limit = 10)

            reporteesList[0].employeeId shouldBe "SR0049"
            reporteesList[0].firstName shouldBe "grv"
            reporteesList[0].lastName shouldBe "abc"

            reporteesList[1].employeeId shouldBe "SR0051"
            reporteesList[1].firstName shouldBe "Rushad"
            reporteesList[1].lastName shouldBe "Shaikh"

            reporteesList[2].employeeId shouldBe "SR0043"
            reporteesList[2].firstName shouldBe "Syed Ubed"
            reporteesList[2].lastName shouldBe "Ali"
        }

        "should get employee data by id" {
            val employee = employeeRepositoryImpl.getEmployeeById(id = 1)

            employee.firstName shouldBe "grv"
            employee.lastName shouldBe "abc"
            employee.emailId shouldBe "gp.aarush@gmail.com"
            employee.employeeId shouldBe "SR0049"
            employee.contactNo shouldBe "+917389543155"
            employee.departmentName shouldBe "Engineering"
            employee.teamName shouldBe "Backend"
            employee.designationName shouldBe "sd22"
            employee.roleName shouldBe "Backend manager"
            employee.firstManagerId shouldBe 4
            employee.firstManagerEmployeeId shouldBe "SR0006"
            employee.secondManagerId shouldBe null
            employee.secondManagerEmployeeId shouldBe null
        }

        "should fetch team of an employee during review cycle" {
            val employeeTeam =
                employeeRepositoryImpl.fetchEmployeeTeamDuringReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    employeeId = 1,
                )

            employeeTeam shouldContain (1L to "Backend")
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                 INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
                 VALUES (1, 1, 'Scalereal', true, 50);

                INSERT INTO employees (id, emp_id, first_name, last_name,  email_id , contact_no, status, onboarding_flow, organisation_id)
                 VALUES (4, 'SR0006', 'Yogesh', 'Jadhav', 'yogesh.jadhav@scalereal.com', '+919876543210', true,false, 1),
                 (1, 'SR0049', 'grv', 'abc', 'gp.aarush@gmail.com','+917389543155', true,false, 1),
                 (2, 'SR0043', 'Syed Ubed', 'Ali', 'syed.ali@scalereal.com', '+916262209099', true,false, 1),
                 (3, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', '+918265079426', true,false, 1);

                INSERT INTO departments(id, department_id, organisation_id, department_name, status) VALUES(1, 1, 1, 'Engineering', true);
                INSERT INTO teams(id, team_id, organisation_id, team_name, status) values(1, 1, 1, 'Backend','true');
                INSERT INTO roles(id, organisation_id, role_id, role_name, status) values(1, 1, 1, 'Backend manager','true');
                INSERT INTO designations(id, designation_name, status, designation_id, organisation_id) values(1, 'sd22','true', 1, 1);
                INSERT INTO employees_department_mapping values(1,1);
                 INSERT INTO employees_team_mapping(emp_id, team_id, joined_at, is_active) VALUES(1, 1, '2022-11-10 19:06:35.142179+05:30', true);
                INSERT INTO employees_role_mapping values(1,1);
                INSERT INTO employees_designation_mapping values(1,1);
                INSERT INTO employee_manager_mapping(emp_id, manager_id, type, is_active) VALUES (1,4,1,true), (2,4,1,true), (2,1,2,true), (3,4,1,true);
                INSERT INTO review_cycle (
                organisation_id, id, start_date, end_date, publish, self_review_start_date, self_review_end_date, manager_review_start_date,
                manager_review_end_date, check_in_start_date, check_in_end_date)
                VALUES
                (1, 1, '12/12/2022', '02/02/2023', false,'12/13/2022','12/14/2022','01/01/2023','10/01/2023','01/20/2023','01/25/2023');
                """.trimIndent(),
            )
        }
        roleRepositoryImpl = RoleRepositoryImpl(dataSource)
        employeeRepositoryImpl = EmployeeRepositoryImpl(dataSource, roleRepositoryImpl)
        employeeService =
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
    }
}
