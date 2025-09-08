package scalereal.core.employees

import io.micronaut.http.multipart.CompletedFileUpload
import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.emails.EmailSenderService
import scalereal.core.exception.DuplicateDataException
import scalereal.core.exception.InvalidDataException
import scalereal.core.exception.UserNotFoundException
import scalereal.core.models.EmployeeGender
import scalereal.core.models.domain.BulkImportResponse
import scalereal.core.models.domain.EmpData
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.EmployeeData
import scalereal.core.models.domain.EmployeeDetails
import scalereal.core.models.domain.EmployeeHistory
import scalereal.core.models.domain.EmployeeManagerData
import scalereal.core.models.domain.Employees
import scalereal.core.models.domain.EmployeesData
import scalereal.core.models.domain.ErrorEmployee
import scalereal.core.models.domain.ManagerData
import scalereal.core.models.domain.ManagerUpdateDataList
import scalereal.core.models.domain.ReporteesData
import scalereal.core.models.domain.UserActivityData
import scalereal.core.models.domain.ValidDate
import scalereal.core.models.removeExtraSpaces
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.ContactNumberValidator
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.roles.RoleRepository
import scalereal.core.roles.Roles
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.StringReader
import java.lang.IllegalArgumentException
import java.sql.Date
import java.sql.Timestamp
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale

@Singleton
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val emailSenderService: EmailSenderService,
    private val organisationRepository: OrganisationRepository,
    private val contactNumberValidator: ContactNumberValidator,
    private val roleRepository: RoleRepository,
    private val teamRepository: TeamRepository,
    private val departmentRepository: DepartmentRepository,
    private val designationRepository: DesignationRepository,
    private val moduleService: ModuleService,
    private val userActivityRepository: UserActivityRepository,
    private val reviewCycleRepository: ReviewCycleRepository,
) {
    private val employeesModuleId = moduleService.fetchModuleId(Modules.EMPLOYEES.moduleName)
    private val reviewForTeamModuleId = moduleService.fetchModuleId(Modules.REVIEW_FOR_TEAM.moduleName)
    private val checkInWithTeamModuleId = moduleService.fetchModuleId(Modules.CHECK_IN_WITH_TEAM.moduleName)

    fun countAllEmployees(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        roleId: List<Int>,
    ): Int = employeeRepository.countAllEmployees(organisationId, searchText, departmentId, teamId, designationId, roleId)

    fun fetchAllEmployees(
        organisationId: Long,
        sortOrderId: Int = 2,
        searchText: String = "",
        departmentId: List<Int> = listOf(-99),
        teamId: List<Int> = listOf(-99),
        designationId: List<Int> = listOf(-99),
        roleId: List<Int> = listOf(-99),
        page: Int = 1,
        limit: Int = Int.MAX_VALUE,
    ): List<EmployeeData> {
        val sortOrder: String =
            when (sortOrderId) {
                1 -> "ASC"
                2 -> "DESC"
                else -> throw IllegalArgumentException("Invalid sorting order : $sortOrderId")
            }
        return employeeRepository.fetchAllEmployees(
            organisationId,
            sortOrder,
            searchText,
            departmentId,
            teamId,
            designationId,
            roleId,
            offset = (page - 1) * limit,
            limit,
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun create(
        employeesData: EmployeesData,
        userActivityData: UserActivityData,
    ) {
        try {
            val roleData = roleRepository.getRoleDataById(employeesData.roleId, employeesData.organisationId)
            when {
                (roleData.roleName != Roles.ORG_ADMIN && employeesData.firstManagerId == null) ->
                    throw Exception("Manager 1 not added")
                (employeeRepository.isEmailIdExists(employeesData.emailId)) ->
                    throw DuplicateDataException("This Email ID already exists")
                (employeeRepository.isContactNumberExists(employeesData.organisationId, employeesData.contactNo)) ->
                    throw DuplicateDataException("This Contact Number already exists")
                (
                    employeeRepository
                        .isEmployeeIdExists(
                            employeesData.organisationId,
                            employeesData.employeeId.removeExtraSpaces(),
                        ).exists
                ) ->
                    throw DuplicateDataException("This Employee ID already exists")
                !isValidDomain(employeesData.organisationId, employeesData.emailId) ->
                    throw InvalidDataException("Invalid Email! This Email domain is not associated with your organisation.")
                else -> {
                    employeeRepository.create(
                        EmployeesData(
                            organisationId = employeesData.organisationId,
                            employeeId = employeesData.employeeId.removeExtraSpaces(),
                            firstName = employeesData.firstName.removeExtraSpaces(),
                            lastName = employeesData.lastName.removeExtraSpaces(),
                            emailId = employeesData.emailId.removeExtraSpaces(),
                            contactNo = employeesData.contactNo,
                            genderId = employeesData.genderId,
                            dateOfBirth = employeesData.dateOfBirth,
                            dateOfJoining = employeesData.dateOfJoining,
                            experienceInMonths = employeesData.experienceInMonths,
                            status = employeesData.status,
                            departmentId = employeesData.departmentId,
                            teamId = employeesData.teamId,
                            designationId = employeesData.designationId,
                            roleId = employeesData.roleId,
                            firstManagerId = employeesData.firstManagerId,
                            secondManagerId = employeesData.secondManagerId,
                            isConsultant = employeesData.isConsultant,
                        ),
                    )
                    val id = employeeRepository.getUniqueIdByEmailId(employeesData.emailId)
                    employeeRepository.createEmployeesDepartments(id, employeesData.departmentId)
                    employeeRepository.createEmployeesTeams(id, employeesData.teamId, joinedAt = Timestamp.from(Instant.now()))
                    employeeRepository.createEmployeesRoles(id, employeesData.roleId)
                    employeeRepository.createEmployeesDesignations(id, employeesData.designationId)
                    if (roleData.roleName == Roles.ORG_ADMIN && employeesData.firstManagerId == null) {
                        createManagerMappings(id, id, null)
                    } else {
                        employeesData.firstManagerId?.let {
                            createManagerMappings(
                                id,
                                it,
                                employeesData.secondManagerId,
                            )
                        }
                    }
                    val encodedEmailId = Base64.getEncoder().encodeToString(employeesData.emailId.toByteArray())
                    val organisationName = organisationRepository.fetchName(employeesData.organisationId)
                    if (employeesData.status) {
                        employeeRepository.createEmployeeHistory(id = id, activatedAt = Timestamp.from(Instant.now()))
                    }
                    if (employeesData.status) {
                        addUserActivityLog(
                            userActivityData,
                            activity = "Employee ${employeesData.getEmployeeNameWithEmployeeId()} Added and Activated",
                            description = "Employee ${employeesData.getEmployeeNameWithEmployeeId()} Added and Activated",
                        )
                    } else {
                        addUserActivityLog(
                            userActivityData,
                            activity = "Employee ${employeesData.getEmployeeNameWithEmployeeId()} Added and Deactivated",
                            description = "Employee ${employeesData.getEmployeeNameWithEmployeeId()} Added and Deactivated",
                        )
                    }
                    GlobalScope.launch {
                        emailSenderService.sendEmail(
                            receiver = employeesData.emailId,
                            subject = emailSenderService.welcomeSubject(),
                            htmlBody = emailSenderService.welcomeHTML(employeesData.firstName, encodedEmailId, organisationName),
                            textBody = emailSenderService.welcomeTEXT(),
                        )
                    }
                }
            }
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("unique_index_employee_manager_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Manager getting inserted")
                e.localizedMessage.contains("unique_index_employees_department_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Department getting inserted")
                e.localizedMessage.contains("unique_index_employees_team_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Team getting inserted")
                e.localizedMessage.contains("unique_index_employees_role_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Role getting inserted")
                e.localizedMessage.contains("unique_index_employees_designation_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Designation getting inserted")
                else -> throw e
            }
        }
    }

    fun update(
        employees: Employees,
        userActivityData: UserActivityData,
    ) {
        val roleData = roleRepository.getRoleDataById(employees.roleId, employees.organisationId)
        val employeeOldData = employeeRepository.getEmployeeDataByUniqueId(employees.id)
        val organisationTimeZone = organisationRepository.getOrganisationDetails(employees.organisationId).timeZone
        try {
            when {
                (roleData.roleName != Roles.ORG_ADMIN && employees.firstManagerId == null) ->
                    throw Exception("Manager 1 not added")
                (!isValidDomain(employees.organisationId, employees.emailId)) ->
                    throw InvalidDataException("Invalid Email! This Email domain is not associated with your organisation.")
                else -> {
                    employeeRepository.update(
                        Employees(
                            organisationId = employees.organisationId,
                            id = employees.id,
                            employeeId = employees.employeeId.removeExtraSpaces(),
                            firstName = employees.firstName.removeExtraSpaces(),
                            lastName = employees.lastName.removeExtraSpaces(),
                            emailId = employees.emailId.removeExtraSpaces(),
                            contactNo = employees.contactNo,
                            genderId = employees.genderId,
                            dateOfBirth = employees.dateOfBirth,
                            dateOfJoining = employees.dateOfJoining,
                            experienceInMonths = employees.experienceInMonths,
                            status = employees.status,
                            departmentId = employees.departmentId,
                            teamId = employees.teamId,
                            designationId = employees.designationId,
                            roleId = employees.roleId,
                            firstManagerId = employees.firstManagerId,
                            secondManagerId = employees.secondManagerId,
                            isConsultant = employees.isConsultant,
                        ),
                    )
                    updateEmployeeHistory(employees, employeeOldData)
                    updateActivityLog(employeeOldData, employees, userActivityData)
                    if (employees.teamId != employeeOldData.teamId) updateEmployeeTeam(employees)
                    val organisationCurrentDate = Date.valueOf(LocalDate.now(ZoneId.of(organisationTimeZone)))
                    val reviewSubmissionStarted =
                        reviewCycleRepository.isReviewSubmissionStarted(
                            employees.organisationId,
                            organisationCurrentDate,
                        )
                    if (reviewSubmissionStarted &&
                        (
                            employees.firstManagerId != employeeOldData.firstManagerId ||
                                employees.secondManagerId != employeeOldData.secondManagerId
                        )
                    ) {
                        throw Exception("Managers could not be edited as Self Review has started")
                    } else {
                        if (roleData.roleName == Roles.ORG_ADMIN && employees.firstManagerId == null) {
                            updateManagerMappings(employees.id, employees.id, null)
                        } else {
                            employees.firstManagerId?.let {
                                updateManagerMappings(employees.id, it, employees.secondManagerId)
                            }
                        }
                    }
                    employeeOldData.departmentId?.let {
                        employeeRepository.updateEmployeesDepartments(employees.id, employees.departmentId)
                    } ?: employeeRepository.createEmployeesDepartments(employees.id, employees.departmentId)
                    employeeRepository.updateEmployeesRoles(employees.id, employees.roleId)
                    employeeRepository.updateEmployeesDesignations(employees.id, employees.designationId)
                }
            }
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("employees_email_id_key") ||
                    e.localizedMessage.contains("unique_email_id")
                -> throw DuplicateDataException("This Email ID already exists")
                e.localizedMessage.contains("employees_contact_no_key")
                -> throw DuplicateDataException("This Contact Number already exists")
                e.localizedMessage.contains("idx_emp_id_org_id")
                -> throw DuplicateDataException("This Employee ID already exists")
                e.localizedMessage.contains("unique_index_employee_manager_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Manager getting inserted")
                e.localizedMessage.contains("unique_index_employees_department_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Department getting inserted")
                e.localizedMessage.contains("unique_index_employees_team_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Team getting inserted")
                e.localizedMessage.contains("unique_index_employees_role_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Role getting inserted")
                e.localizedMessage.contains("unique_index_employees_designation_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Designation getting inserted")
                else -> throw e
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun readEmployees(
        organisationId: Long,
        file: CompletedFileUpload,
        userActivityData: UserActivityData,
    ): BulkImportResponse? {
        val inputStream = file.inputStream
        val fileContent = inputStream.bufferedReader().use { it.readText() }
        val fileCount = countEmployees(fileContent)
        when (fileCount) {
            0 ->
                return BulkImportResponse(
                    writeErrorFile("No employees to add!"),
                    fileCount,
                    0,
                )

            in 501..Int.MAX_VALUE ->
                return BulkImportResponse(
                    writeErrorFile(
                        "Max 500 employees are allowed to upload at a time. Please reduce the number of employees and try again.",
                    ),
                    fileCount,
                    0,
                )
        }

        val errorEmployees = mutableListOf<ErrorEmployee>()

        val employeeIdRegex = Regex("^([A-Za-z0-9]+$)")
        val nameRegex = Regex("^([A-Za-z]+[\\s]?[A-Za-z]+)$")
        val emailRegex = Regex("^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$")
        val statusRegex = Regex("^(yes|no|Yes|No|YES|NO|Y|N|y|n)$")
        val genderRegex = Regex("^(M|F|O|MALE|FEMALE|OTHERS)\$")
        val departmentRegex = Regex("^([A-Za-z0-9-]+[\\s]+)*[A-Za-z0-9-]+\$")
        val teamRegex = Regex("^([A-Za-z0-9-]+[\\s]+)*[A-Za-z0-9-]+\$")
        val designationRegex = Regex("^([A-Za-z0-9-]+[\\s]+)*[A-Za-z0-9-]+\$")
        val roleRegex = Regex("^([A-Za-z0-9-]+[\\s]+)*[A-Za-z0-9-]+\$")
        val experienceRegex = Regex("^(?:[0-9]|[0-5][0-9]|60)\\s*\\|\\s*(?:[0-9]|0[0-9]|1[0-1])\$")

        BufferedReader(StringReader(fileContent)).use { reader ->
            reader.readLine()
            var line = reader.readLine()
            while (line != null) {
                if (line.trim().isEmpty() || line.split(",").all { it.isBlank() }) {
                    line = reader.readLine()
                    continue
                }
                val errors = mutableListOf<String>()
                val data = line.split(",")
                if (data.size < 16) {
                    for (i in data.size..15) line += ","
                    errorEmployees.add(ErrorEmployee(line, "Insufficient Data to Add Employee"))
                    line = reader.readLine()
                    continue
                }

                val employeeId = if (data.size > 0) data[0].trim() else ""
                if (employeeId.isEmpty() || !employeeId.matches(employeeIdRegex)) errors.add("Invalid Employee Id")

                val firstName = if (data.size > 1) data[1].trim() else ""
                if (firstName.isEmpty() || !firstName.matches(nameRegex)) errors.add("Invalid First Name")

                val lastName = if (data.size > 2) data[2].trim() else ""
                if (lastName.isEmpty() || !lastName.matches(nameRegex)) errors.add("Invalid Last Name")

                val emailId = if (data.size > 3) data[3].trim() else ""
                if (emailId.isEmpty() || !emailId.matches(emailRegex)) {
                    errors.add("Invalid Email.")
                } else if (!isValidDomain(organisationId, emailId)) {
                    errors.add("Invalid Email! This Email domain is not associated with your organisation")
                }

                var contactNo = if (data.size > 4) data[4].trim() else ""
                val contactNumberValidity = contactNumberValidator.isValidContactNumber(contactNo)
                if (contactNo.isEmpty() || !contactNumberValidity.isValid) {
                    errors.add("Invalid Contact No")
                } else {
                    contactNo = contactNumberValidity.formattedContact.toString()
                }

                val gender = if (data.size > 5) data[5].trim().uppercase() else ""
                if (gender.isEmpty() || !gender.matches(genderRegex)) errors.add("Invalid Gender")

                val dateOfBirth = if (data.size > 6) data[6].trim() else ""
                val dobValidity = isDateValid(date = dateOfBirth, format = "dd-MM-yyyy")
                if (dateOfBirth.isEmpty() || !dobValidity.isValid) errors.add("Invalid Date of Birth")

                val status = if (data.size > 7) data[7].trim().lowercase(Locale.getDefault()) else ""
                if (status.isEmpty() || !status.matches(statusRegex)) errors.add("Invalid Active Status. It Should be yes/no")
                val employeeStatus = status == "yes" || status == "y"

                val department = if (data.size > 8) data[8].trim() else ""
                if (department.isEmpty() || !department.matches(departmentRegex)) errors.add("Invalid Department Name")

                val team = if (data.size > 9) data[9].trim() else ""
                if (team.isEmpty() || !team.matches(teamRegex)) errors.add("Invalid Team Name")

                val designation = if (data.size > 10) data[10].trim() else ""
                if (designation.isEmpty() || !designation.matches(designationRegex)) errors.add("Invalid Designation")

                val role = if (data.size > 11) data[11].trim() else ""
                if (role.isEmpty() || !role.matches(roleRegex)) errors.add("Invalid Role")

                val firstManagerEmployeeId = if (data.size > 12) data[12].trim() else ""
                if (firstManagerEmployeeId.isEmpty() || !firstManagerEmployeeId.matches(employeeIdRegex)) {
                    errors.add("Invalid Manager 1 Employee Id")
                }
                var secondManagerEmployeeId: String? = null
                if (data.size > 13 && data[13].trim().isNotEmpty() && data[13].trim() != "null") {
                    secondManagerEmployeeId = data[13].trim()
                    if (!secondManagerEmployeeId.matches(employeeIdRegex)) {
                        errors.add("Invalid Manager 2 Employee Id")
                    }
                }

                val dateOfJoining = if (data.size > 14) data[14].trim() else ""
                val dojValidity = isDateValid(date = dateOfJoining, format = "dd-MM-yyyy")
                if (dateOfJoining.isEmpty() || !dojValidity.isValid) errors.add("Invalid Date of Joining")

                val yearsOfExperience = if (data.size > 15) data[15].trim() else ""
                if (yearsOfExperience.isEmpty() || !yearsOfExperience.matches(experienceRegex)) errors.add("Invalid Years of Experience")

                val consultant = if (data.size > 16) data[16].trim().lowercase(Locale.getDefault()) else ""
                if (consultant.isEmpty() || !consultant.matches(statusRegex)) errors.add("Invalid Consultant Value. It Should be yes/no")
                val isConsultant = status == "yes" || status == "y"

                if (employeeRepository.isEmployeeIdExists(organisationId, employeeId).exists) errors.add("Employee Id already exist")
                if (employeeRepository.isEmailIdExists(emailId)) errors.add("Email Id already exist")
                if (employeeRepository.isContactNumberExists(organisationId, contactNo)) errors.add("Contact No. already exist")
                val isDepartment = departmentRepository.isDepartmentExists(organisationId = organisationId, departmentName = department)
                if (department.isNotEmpty() && department.matches(regex = departmentRegex)) {
                    if (!isDepartment.exists || !isDepartment.status) errors.add("Department does not exist or inactive")
                }

                if (team.isNotEmpty() && team.matches(regex = teamRegex)) {
                    if (isDepartment.exists) {
                        val departmentId =
                            departmentRepository.getDepartmentId(
                                organisationId = organisationId,
                                departmentName = department,
                            )
                        val isTeam =
                            teamRepository.isTeamExists(
                                organisationId = organisationId,
                                departmentId = departmentId,
                                teamName = team,
                            )
                        if (!isDepartment.status) {
                            errors.add("Department is inactive of Team $team")
                        } else if (!isTeam.exists || !isTeam.status) {
                            errors.add("Team does not exist in Department $department or inactive")
                        }

                        if (designation.isNotEmpty() && designation.matches(regex = designationRegex)) {
                            if (isTeam.exists) {
                                val teamId = teamRepository.getTeamId(organisationId, departmentId = departmentId, teamName = team)
                                val isDesignation = designationRepository.isDesignationExists(organisationId, teamId, designation)
                                if (!isTeam.status) {
                                    errors.add("Team is inactive of Designation $designation")
                                } else if (!isDesignation.exists || !isDesignation.status) {
                                    errors.add("Designation does not exist in Team $team or is inactive")
                                }
                            } else {
                                errors.add("Designation does not exist in Team $team")
                            }
                        }
                    } else {
                        errors.add("Team does not exist in Department $department")
                    }
                }

                if (role.isNotEmpty() && role.matches(regex = roleRegex)) {
                    val isRole = roleRepository.isRoleExists(organisationId, role)
                    if (!isRole.exists || !isRole.status) errors.add("Role does not exist or inactive")
                }

                if (firstManagerEmployeeId.isNotEmpty() && firstManagerEmployeeId.matches(regex = employeeIdRegex)) {
                    val isFirstManagerEmployeeId = employeeRepository.isEmployeeIdExists(organisationId, firstManagerEmployeeId)
                    if (!isFirstManagerEmployeeId.exists || !isFirstManagerEmployeeId.status) {
                        errors.add("Manager 1 does not exist or inactive")
                    } else if (!fetchAllManagers(organisationId = organisationId, page = 1, limit = Int.MAX_VALUE).any {
                            it.employeeId == firstManagerEmployeeId
                        }
                    ) {
                        errors.add("Manager 1 does not have manager review access")
                    }
                }

                if (!secondManagerEmployeeId.isNullOrEmpty() && secondManagerEmployeeId.matches(regex = employeeIdRegex)) {
                    val isSecondManagerEmployeeId = employeeRepository.isEmployeeIdExists(organisationId, secondManagerEmployeeId)
                    if (!isSecondManagerEmployeeId.exists || !isSecondManagerEmployeeId.status) {
                        errors.add("Manager 2 does not exist or inactive")
                    } else if (!fetchAllManagers(organisationId = organisationId, page = 1, limit = Int.MAX_VALUE).any {
                            it.employeeId == secondManagerEmployeeId
                        }
                    ) {
                        errors.add("Manager 2 does not have manager review access")
                    }
                }

                if (errors.isNotEmpty()) {
                    errorEmployees.add(
                        ErrorEmployee(
                            "$employeeId, $firstName, $lastName, $emailId, $contactNo, $gender, $dateOfBirth, " +
                                "$status, $department, $team, $designation, $role, $firstManagerEmployeeId, " +
                                if (secondManagerEmployeeId.isNullOrEmpty() || secondManagerEmployeeId == "null") {
                                    ", $dateOfJoining, $yearsOfExperience, $consultant"
                                } else {
                                    "$secondManagerEmployeeId, $dateOfJoining, $yearsOfExperience, $consultant"
                                },
                            "${'"' + errors.joinToString(", ") + '"'}",
                        ),
                    )
                    line = reader.readLine()
                    continue
                }

                val genderMap =
                    mapOf(
                        "M" to EmployeeGender.MALE.genderId,
                        "MALE" to EmployeeGender.MALE.genderId,
                        "F" to EmployeeGender.FEMALE.genderId,
                        "FEMALE" to EmployeeGender.FEMALE.genderId,
                        "O" to EmployeeGender.OTHERS.genderId,
                        "OTHERS" to EmployeeGender.OTHERS.genderId,
                    )
                val genderId = genderMap[gender] ?: 3
                val dob = dobValidity.formattedDate
                val departmentId = departmentRepository.getDepartmentId(organisationId = organisationId, departmentName = department)
                val teamId = teamRepository.getTeamId(organisationId = organisationId, departmentId = departmentId, teamName = team)
                val designationId =
                    designationRepository.getDesignationId(
                        organisationId = organisationId,
                        teamId = teamId,
                        designationName = designation,
                    )
                val roleId = roleRepository.getRoleId(organisationId, role)
                val firstManagerId =
                    employeeRepository.getEmployeesUniqueId(
                        organisationId = organisationId,
                        employeeId = firstManagerEmployeeId,
                    )
                val secondManagerId =
                    if (secondManagerEmployeeId.isNullOrEmpty() || secondManagerEmployeeId == "null") {
                        null
                    } else {
                        employeeRepository.getEmployeesUniqueId(organisationId = organisationId, employeeId = secondManagerEmployeeId)
                    }
                val doj = dojValidity.formattedDate
                val (years, months) = yearsOfExperience.split("|").map { it.trim().toInt() }
                val experience = years * 12 + months

                val employeeData =
                    EmployeesData(
                        organisationId = organisationId,
                        employeeId = employeeId,
                        firstName = firstName,
                        lastName = lastName,
                        emailId = emailId,
                        contactNo = contactNo,
                        genderId = genderId,
                        dateOfBirth = Date.valueOf(dob),
                        dateOfJoining = Date.valueOf(doj),
                        experienceInMonths = experience,
                        status = employeeStatus,
                        departmentId = departmentId,
                        teamId = teamId,
                        designationId = designationId,
                        roleId = roleId,
                        firstManagerId = firstManagerId,
                        secondManagerId = secondManagerId,
                        isConsultant = isConsultant,
                    )
                GlobalScope.launch {
                    create(employeesData = employeeData, userActivityData = userActivityData)
                }
                line = reader.readLine()
                continue
            }
        }

        if (errorEmployees.isNotEmpty()) {
            val errorFile = File("error_employees.csv")
            errorFile.createNewFile()

            val writer = FileWriter(errorFile)
            writer.write(
                "Employee Id, First Name, Last Name, Email Id, Contact No.(with Country Code), " +
                    "Gender(M/F/O), " + "Date of Birth (DD-MM-YYYY), Active(Y/N), Department, Team, Designation, " +
                    "Role, Manager 1 Employee Id, Manager 2 Employee Id (Optional), Date of Joining (DD-MM-YYYY), " +
                    "Years of Experience (Years | Months), Consultant(Y/N)\n",
            )
            var errorCount = 0
            for (errorEmployee in errorEmployees) {
                writer.write("${errorEmployee.data}, ${errorEmployee.error}\n")
                errorCount++
            }
            writer.flush()
            writer.close()
            return BulkImportResponse(
                errorFile,
                fileCount,
                errorCount,
            )
        }
        return null
    }

    private fun countEmployees(content: String): Int {
        val reader = BufferedReader(StringReader(content))
        var employeesCount = 0

        while (true) {
            val line = reader.readLine() ?: break
            if (line.trim().isEmpty() || line.split(",").all { it.isBlank() }) {
                continue
            }
            employeesCount++
        }
        return if (employeesCount > 0) employeesCount - 1 else 0
    }

    private fun writeErrorFile(errorMessage: String): File {
        val errorFile = File("error_employees.csv")
        errorFile.createNewFile()

        val writer = FileWriter(errorFile)
        writer.write(errorMessage)
        writer.flush()
        writer.close()

        return errorFile
    }

    private fun isDateValid(
        date: String,
        format: String,
    ): ValidDate =
        try {
            val formatter = DateTimeFormatter.ofPattern(format)
            val formattedDate = LocalDate.parse(date, formatter)
            ValidDate(
                isValid = true,
                formattedDate = formattedDate,
            )
        } catch (e: DateTimeException) {
            ValidDate(
                isValid = false,
                formattedDate = null,
            )
        }

    fun countAllManagers(organisationId: Long): Int =
        employeeRepository.countAllManagers(
            organisationId = organisationId,
            moduleId = listOf(reviewForTeamModuleId, checkInWithTeamModuleId),
        )

    fun fetchAllManagers(
        organisationId: Long,
        page: Int,
        limit: Int,
    ): List<ManagerData> =
        employeeRepository.fetchAllManagers(
            organisationId = organisationId,
            moduleId = listOf(reviewForTeamModuleId, checkInWithTeamModuleId),
            offset = (page - 1) * limit,
            limit = limit,
        )

    fun updateOnBoardingFlowStatus(
        organisationId: Long,
        onboardingFlow: Boolean,
        id: Long,
    ) {
        employeeRepository.updateOnBoardingFlowStatus(organisationId, onboardingFlow, id)
    }

    fun createManagerMappings(
        id: Long,
        firstManagerId: Long,
        secondManagerId: Long?,
    ) {
        employeeRepository.insertEmployeeManagerMapping(
            id,
            firstManagerId,
            ManagerTypes.FIRST_MANAGER.id,
        )
        secondManagerId?.let {
            employeeRepository.insertEmployeeManagerMapping(
                id,
                it,
                ManagerTypes.SECOND_MANAGER.id,
            )
        }
    }

    private fun addUserActivityLog(
        userActivityData: UserActivityData,
        activity: String,
        description: String,
    ) = userActivityRepository.addActivity(
        actionBy = userActivityData.actionBy,
        moduleId = employeesModuleId,
        activity = activity,
        description = description,
        ipAddress = userActivityData.ipAddress,
    )

    private fun updateActivityLog(
        employeeOldData: EmployeeDetails,
        employeeNewData: Employees,
        userActivityData: UserActivityData,
    ) {
        val isEmployeeDataUpdated = (
            employeeOldData.employeeId != employeeNewData.employeeId ||
                employeeOldData.emailId != employeeNewData.emailId ||
                employeeOldData.genderId != employeeNewData.genderId ||
                employeeOldData.dateOfBirth != employeeNewData.dateOfBirth ||
                employeeOldData.dateOfJoining != employeeNewData.dateOfJoining ||
                employeeOldData.experienceInMonths != employeeNewData.experienceInMonths ||
                employeeOldData.contactNo != employeeNewData.contactNo ||
                employeeOldData.firstName != employeeNewData.firstName ||
                employeeOldData.lastName != employeeNewData.lastName ||
                employeeOldData.teamId != employeeNewData.teamId ||
                employeeOldData.roleId != employeeNewData.roleId ||
                employeeOldData.designationId != employeeNewData.designationId ||
                employeeOldData.firstManagerId != employeeNewData.firstManagerId ||
                employeeOldData.secondManagerId != employeeNewData.secondManagerId
        )

        val activity =
            when {
                isEmployeeDataUpdated && employeeOldData.status && !employeeNewData.status ->
                    "Employee ${employeeNewData.getEmployeeNameWithEmployeeId()} Edited and Deactivated"
                isEmployeeDataUpdated && !employeeOldData.status && employeeNewData.status ->
                    "Employee ${employeeNewData.getEmployeeNameWithEmployeeId()} Edited and Activated"
                employeeOldData.status && !employeeNewData.status ->
                    "Employee ${employeeNewData.getEmployeeNameWithEmployeeId()} Deactivated"
                !employeeOldData.status && employeeNewData.status ->
                    "Employee ${employeeNewData.getEmployeeNameWithEmployeeId()} Activated"
                isEmployeeDataUpdated -> "Employee ${employeeNewData.getEmployeeNameWithEmployeeId()} Edited"
                else -> null
            }
        if (activity != null) addUserActivityLog(userActivityData, activity = activity, description = activity)
    }

    private fun updateEmployeeTeam(employees: Employees) {
        val now = Timestamp.from(Instant.now())
        employeeRepository.updateEmployeesTeams(id = employees.id, leftAt = now) // deactivating old mapping
        employeeRepository.createEmployeesTeams(id = employees.id, teamId = employees.teamId, joinedAt = now) // creating new mapping
    }

    private fun updateManagerMappings(
        id: Long,
        firstManagerId: Long,
        secondManagerId: Long?,
    ) {
        val employeeData = employeeRepository.getMapping(id)
        if (firstManagerId != employeeData.firstManagerId) {
            employeeRepository.updateEmployeeManagerMapping(id, ManagerTypes.FIRST_MANAGER.id)
            employeeRepository.insertEmployeeManagerMapping(id, firstManagerId, ManagerTypes.FIRST_MANAGER.id)
        }

        if (secondManagerId?.let { it != employeeData.secondManagerId } != false) {
            employeeRepository.updateEmployeeManagerMapping(id, ManagerTypes.SECOND_MANAGER.id)
            secondManagerId?.let { employeeRepository.insertEmployeeManagerMapping(id, it, ManagerTypes.SECOND_MANAGER.id) }
        }
    }

    private fun isValidDomain(
        organisationId: Long,
        emailId: String,
    ): Boolean {
        val allowedDomains = organisationRepository.getAllowedDomains(organisationId)
        return allowedDomains.any {
            it.name.lowercase() == emailId.lowercase().substring(emailId.indexOf("@"))
        }
    }

    fun fetchByEmailId(emailId: String): Employee? =
        if (!employeeRepository.isEmployeeExists(emailId)) {
            throw UserNotFoundException("Unauthorized access! Please contact System Admin/HR.")
        } else {
            employeeRepository.fetchByEmailId(emailId)
        }

    fun isEmployeeExists(email: Any): Boolean = employeeRepository.isEmployeeExists(email)

    fun getEmployeeManagerList(
        organisationId: Long,
        id: List<Int>,
        firstManagerId: List<Int>,
        secondManagerId: List<Int>,
        page: Int,
        limit: Int,
    ): List<EmployeeManagerData> {
        val offset: Int = (page - 1) * limit
        return employeeRepository.getEmployeeManagerList(
            organisationId = organisationId,
            id = id,
            firstManagerId = firstManagerId,
            secondManagerId = secondManagerId,
            offset = offset,
            limit = limit,
        )
    }

    fun getEmployeeManagerCount(
        organisationId: Long,
        id: List<Int>,
        firstManagerId: List<Int>,
        secondManagerId: List<Int>,
    ): Int =
        employeeRepository.getEmployeeManagerCount(
            organisationId = organisationId,
            id = id,
            firstManagerId = firstManagerId,
            secondManagerId = secondManagerId,
        )

    fun fetchEmployeesByManager(
        organisationId: Long,
        managerId: Long,
        page: Int,
        limit: Int,
    ): List<ReporteesData> = employeeRepository.fetchEmployeesByManager(organisationId, managerId, offset = (page - 1) * limit, limit)

    fun fetchEmployeesByManagerCount(
        organisationId: Long,
        managerId: Long,
    ): Int = employeeRepository.fetchEmployeesByManagerCount(organisationId, managerId)

    fun updateEmployeesManager(managerUpdateDataList: List<ManagerUpdateDataList>) {
        managerUpdateDataList.forEach { managerUpdateData ->
            val employeeData = employeeRepository.getMapping(managerUpdateData.employeeId)
            if (managerUpdateData.currentManagerId == employeeData.firstManagerId) {
                employeeRepository.updateEmployeeManagerMapping(managerUpdateData.employeeId, ManagerTypes.FIRST_MANAGER.id)
                employeeRepository.insertEmployeeManagerMapping(
                    managerUpdateData.employeeId,
                    managerUpdateData.newManagerId,
                    ManagerTypes.FIRST_MANAGER.id,
                )
            }

            if (managerUpdateData.currentManagerId == employeeData.secondManagerId) {
                employeeRepository.updateEmployeeManagerMapping(managerUpdateData.employeeId, ManagerTypes.SECOND_MANAGER.id)
                employeeRepository.insertEmployeeManagerMapping(
                    managerUpdateData.employeeId,
                    managerUpdateData.newManagerId,
                    ManagerTypes.SECOND_MANAGER.id,
                )
            }
        }
    }

    fun getSecret(emailId: Any): String = employeeRepository.getSecret(emailId)

    fun getActiveEmployees(organisationId: Long): List<Employee> = employeeRepository.getActiveEmployees(organisationId)

    fun getEmployeeById(
        id: Long?,
        reviewCycleId: Long? = null,
    ): EmpData = employeeRepository.getEmployeeById(id, reviewCycleId)

    fun getManagers(organisationId: Long): List<Employee> = employeeRepository.getManagers(organisationId)

    fun getEmployeesWithReviewCyclePermission(organisationId: Long): List<Employee> {
        val reviewCycleModuleId = moduleService.fetchModuleId(Modules.REVIEW_CYCLE.moduleName)
        return employeeRepository.getEmployeesWithModulePermission(organisationId, reviewCycleModuleId)
    }

    fun getEmployeesWithSuggestionsReceivedPermission(organisationId: Long): List<Employee> {
        val receivedSuggestionsModuleId = moduleService.fetchModuleId(Modules.RECEIVED_SUGGESTIONS.moduleName)
        return employeeRepository.getEmployeesWithModulePermission(organisationId, receivedSuggestionsModuleId)
    }

    private fun updateEmployeeHistory(
        newData: Employees,
        oldData: EmployeeDetails,
    ) {
        if (!oldData.status && newData.status) {
            employeeRepository.createEmployeeHistory(id = newData.id, activatedAt = Timestamp.from(Instant.now()))
        } else if (oldData.status && !newData.status) {
            employeeRepository.updateEmployeeHistory(id = newData.id, deactivatedAt = Timestamp.from(Instant.now()))
        }
    }

    fun getEmployeesHistory(id: List<Int>): List<EmployeeHistory> = employeeRepository.getEmployeesHistory(id)

    fun fetchActiveEmployeesDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): List<Int> = employeeRepository.fetchActiveEmployeesDuringReviewCycle(organisationId, reviewCycleId)

    fun fetchActiveEmployeesCountDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): Long = employeeRepository.fetchActiveEmployeesCountDuringReviewCycle(organisationId, reviewCycleId)

    fun fetchEmployeeTeamDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
        employeeId: Long,
    ): Map<Long, String> = employeeRepository.fetchEmployeeTeamDuringReviewCycle(organisationId, reviewCycleId, employeeId)
}
