package scalereal.api.employees

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Part
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Put
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.api.common.ErrorMessage
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.core.employees.EmployeeExperience
import scalereal.core.employees.EmployeeService
import scalereal.core.employees.generateExperienceList
import scalereal.core.exception.UserNotFoundException
import scalereal.core.models.AccessType
import scalereal.core.models.EmployeeGender
import scalereal.core.models.GenderInfo
import scalereal.core.models.domain.EmpData
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.EmployeeManager
import scalereal.core.models.domain.EmployeeResponse
import scalereal.core.models.domain.Employees
import scalereal.core.models.domain.EmployeesData
import scalereal.core.models.domain.ManagerReportees
import scalereal.core.models.domain.ManagerResponse
import scalereal.core.models.domain.ManagerUpdateDataList
import scalereal.core.models.domain.SuperAdminResponse
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import scalereal.core.user.UserService
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.sql.Date
import javax.ws.rs.QueryParam

@Tag(name = "Employee")
@Controller(value = "/api/employees")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class EmployeeController(
    private val employeeService: EmployeeService,
    private val userService: UserService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.EMPLOYEES.moduleName

    @Operation(summary = "Get all employees")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = EmployeeResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/")
    fun fetchAll(
        organisationId: Long,
        sortOrderId: Int?,
        searchText: String?,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        roleId: List<Int>,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    EmployeeResponse(
                        totalEmployees =
                            employeeService.countAllEmployees(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
                                departmentId = departmentId,
                                teamId = teamId,
                                designationId = designationId,
                                roleId = roleId,
                            ),
                        employees =
                            employeeService.fetchAllEmployees(
                                organisationId = organisationId,
                                sortOrderId = sortOrderId ?: 2,
                                searchText = searchText ?: "",
                                departmentId = departmentId,
                                teamId = teamId,
                                designationId = designationId,
                                roleId = roleId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Create employee")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Post("/")
    fun create(
        organisationId: Long,
        employeeId: String,
        firstName: String,
        lastName: String,
        emailId: String,
        contactNo: String,
        genderId: Int,
        dateOfBirth: Date,
        dateOfJoining: Date,
        experienceInMonths: Int,
        status: Boolean,
        departmentId: Long,
        teamId: Long,
        designationId: Long,
        roleId: Long,
        firstManagerId: Long?,
        secondManagerId: Long?,
        isConsultant: Boolean,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId = organisationId, authentication.roles, moduleName)
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    employeeService.create(
                        EmployeesData(
                            organisationId = organisationId,
                            employeeId = employeeId,
                            firstName = firstName,
                            lastName = lastName,
                            emailId = emailId,
                            contactNo = contactNo,
                            genderId = genderId,
                            dateOfBirth = dateOfBirth,
                            dateOfJoining = dateOfJoining,
                            experienceInMonths = experienceInMonths,
                            status = status,
                            departmentId = departmentId,
                            teamId = teamId,
                            designationId = designationId,
                            roleId = roleId,
                            firstManagerId = firstManagerId,
                            secondManagerId = secondManagerId,
                            isConsultant = isConsultant,
                        ),
                        UserActivityData(actionBy, ipAddress),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Edit employee")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Put("/{id}")
    fun update(
        organisationId: Long,
        id: Long,
        employeeId: String,
        firstName: String,
        lastName: String,
        emailId: String,
        contactNo: String,
        genderId: Int,
        dateOfBirth: Date,
        dateOfJoining: Date,
        experienceInMonths: Int,
        status: Boolean,
        departmentId: Long,
        teamId: Long,
        designationId: Long,
        roleId: Long,
        firstManagerId: Long?,
        secondManagerId: Long?,
        isConsultant: Boolean,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId = organisationId, authentication.roles, moduleName)
        if (!permissions.edit) {
            return Response(
                ResponseType.FORBIDDEN,
                body = ErrorMessage("Access Denied! You do not have permission to access this route."),
            ).getHttpResponse()
        }
        return try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    employeeService.update(
                        Employees(
                            organisationId = organisationId,
                            id = id,
                            employeeId = employeeId,
                            firstName = firstName,
                            lastName = lastName,
                            emailId = emailId,
                            contactNo = contactNo,
                            genderId = genderId,
                            dateOfBirth = dateOfBirth,
                            dateOfJoining = dateOfJoining,
                            experienceInMonths = experienceInMonths,
                            status = status,
                            departmentId = departmentId,
                            teamId = teamId,
                            designationId = designationId,
                            roleId = roleId,
                            firstManagerId = firstManagerId,
                            secondManagerId = secondManagerId,
                            isConsultant = isConsultant,
                        ),
                        UserActivityData(actionBy, ipAddress),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Bulk import", description = "Get template for employee bulk import")
    @Get("/template")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun getEmployeeTemplate(): HttpResponse<File> {
        val employeeTemplate = File("employee_template.csv")
        employeeTemplate.createNewFile()
        val writer = FileWriter(employeeTemplate)
        writer.write(
            "Employee Id, First Name, Last Name, Email Id, Contact No.(with Country Code), " +
                "Gender(M/F/O), Date of Birth (DD-MM-YYYY), Active(Y/N), Department, Team, Designation, " +
                "Role, Manager 1 Employee Id, Manager 2 Employee Id (Optional), Date of Joining (DD-MM-YYYY), " +
                "Years of Experience (Years | Months), Consultant(Y/N)\n",
        )
        writer.flush()
        writer.close()

        return HttpResponse.ok(employeeTemplate).header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=employee_template.csv",
        )
    }

    @Operation(summary = "Bulk import", description = "Upload bulk import file")
    @Post("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun readEmployees(
        @Part("file") file: CompletedFileUpload,
        organisationId: Long,
        actionBy: Long,
        ipAddress: String,
    ): HttpResponse<String> {
        try {
            if (!file.filename.endsWith(".csv")) {
                val errorFile = File("error.csv")
                FileOutputStream(errorFile).use {
                    it.write("The file must be of type .csv".toByteArray())
                }
                val responseMap =
                    mapOf(
                        "file" to errorFile.readBytes().toString(Charsets.UTF_8),
                        "message" to "Invalid file type. The file must be of type .csv",
                    )
                val objectMapper = ObjectMapper()
                val responseJson = objectMapper.writeValueAsString(responseMap)
                return HttpResponse
                    .badRequest(responseJson)
                    .header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON,
                    ).contentType(MediaType.TEXT_CSV_TYPE)
            }

            val errorFile = employeeService.readEmployees(organisationId, file, UserActivityData(actionBy, ipAddress))
            when {
                (errorFile?.file != null && errorFile.errorCount == 0) -> {
                    if (errorFile.fileCount == 0) {
                        val responseMap =
                            mapOf(
                                "file" to errorFile.file.readBytes().toString(Charsets.UTF_8),
                                "message" to "The file you are trying to add is empty.",
                            )
                        val objectMapper = ObjectMapper()
                        val responseJson = objectMapper.writeValueAsString(responseMap)
                        return HttpResponse
                            .badRequest(responseJson)
                            .header(
                                HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_JSON,
                            ).contentType(MediaType.TEXT_CSV_TYPE)
                    } else {
                        val responseMap =
                            mapOf(
                                "file" to errorFile.file.readBytes().toString(Charsets.UTF_8),
                                "message" to "The maximum limit for employee uploads has been exceeded. " +
                                    "Please reduce the number of employees up to 500 and try again.",
                            )
                        val objectMapper = ObjectMapper()
                        val responseJson = objectMapper.writeValueAsString(responseMap)
                        return HttpResponse
                            .badRequest(responseJson)
                            .header(
                                HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_JSON,
                            ).contentType(MediaType.TEXT_CSV_TYPE)
                    }
                }

                errorFile?.file != null -> {
                    val successCount = errorFile.fileCount - errorFile.errorCount
                    if (errorFile.fileCount == errorFile.errorCount) {
                        val responseMap =
                            mapOf(
                                "file" to errorFile.file.readBytes().toString(Charsets.UTF_8),
                                "message" to
                                    "The employees were not added due to an error. Please check the error file for more information.",
                            )
                        val objectMapper = ObjectMapper()
                        val responseJson = objectMapper.writeValueAsString(responseMap)
                        return HttpResponse
                            .badRequest(responseJson)
                            .header(
                                HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_JSON,
                            ).contentType(MediaType.TEXT_CSV_TYPE)
                    } else {
                        val responseMap =
                            mapOf(
                                "file" to errorFile.file.readBytes().toString(Charsets.UTF_8),
                                when {
                                    (errorFile.errorCount == 1 && successCount == 1) ->
                                        "message" to "$successCount employee was added successfully. ${errorFile.errorCount} employee " +
                                            "was not added due to an error. Please check the error file for more information."

                                    (successCount == 1) ->
                                        "message" to "$successCount employee was added successfully. ${errorFile.errorCount} employees " +
                                            "were not added due to an error. Please check the error file for more information."

                                    (errorFile.errorCount == 1) ->
                                        "message" to "$successCount employees were added successfully. ${errorFile.errorCount} employee " +
                                            "was not added due to an error. Please check the error file for more information."

                                    else ->
                                        "message" to "$successCount employees were added successfully. ${errorFile.errorCount} employees " +
                                            "were not added due to an error. Please check the error file for more information."
                                },
                            )
                        val objectMapper = ObjectMapper()
                        val responseJson = objectMapper.writeValueAsString(responseMap)
                        return HttpResponse
                            .badRequest(responseJson)
                            .header(
                                HttpHeaders.CONTENT_TYPE,
                                MediaType.APPLICATION_JSON,
                            ).contentType(MediaType.TEXT_CSV_TYPE)
                    }
                }

                else -> {
                    val successFile = File("success.csv")
                    successFile.createNewFile()

                    val writer = FileWriter(successFile)
                    writer.write("All Employees Added Successfully!")
                    writer.flush()
                    writer.close()

                    val responseMap =
                        mapOf(
                            "file" to successFile.readBytes().toString(Charsets.UTF_8),
                            "message" to "All Employees Added Successfully!",
                        )
                    val objectMapper = ObjectMapper()
                    val responseJson = objectMapper.writeValueAsString(responseMap)
                    return HttpResponse
                        .ok(responseJson)
                        .header(
                            HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON,
                        ).header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=success.csv",
                        ).contentType(MediaType.TEXT_CSV_TYPE)
                }
            }
        } catch (e: Exception) {
            val errorFile = File("error.csv")
            errorFile.createNewFile()

            val writer = FileWriter(errorFile)
            writer.write("Remove invalid data and try again!")
            writer.flush()
            writer.close()

            val responseMap =
                mapOf(
                    "file" to errorFile.readBytes().toString(Charsets.UTF_8),
                    "message" to "Error occurred while processing the file. Remove invalid data and try again.",
                )
            val objectMapper = ObjectMapper()
            val responseJson = objectMapper.writeValueAsString(responseMap)
            return HttpResponse
                .badRequest(responseJson)
                .header(
                    HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON,
                ).header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=error.csv",
                ).contentType(MediaType.TEXT_CSV_TYPE)
        }
    }

    @Operation(summary = "Get managers list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ManagerResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/managers/")
    fun fetchAllManagers(
        organisationId: Long,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    ManagerResponse(
                        totalManagers =
                            employeeService.countAllManagers(
                                organisationId = organisationId,
                            ),
                        managers =
                            employeeService.fetchAllManagers(
                                organisationId = organisationId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Update onboarding flow")
    @Put("/onboarding-flow/{id}")
    fun updateOnBoardingFlowStatus(
        organisationId: Long,
        onboardingFlow: Boolean,
        id: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = employeeService.updateOnBoardingFlowStatus(organisationId, onboardingFlow, id),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get employee details by emailId")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = Employee::class))]),
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = SuperAdminResponse::class))]),
        ApiResponse(responseCode = "401", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/email")
    fun fetchByEmailId(
        @QueryParam("emailId") emailId: String,
        authentication: Authentication,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    if (authentication.attributes[AccessType.SUPER_ADMIN.toString()] as Boolean) {
                        userService.fetchSuperAdminDetails(emailId)
                    } else {
                        employeeService.fetchByEmailId(emailId)
                    },
            )
        } catch (e: UserNotFoundException) {
            Response(
                ResponseType.UNAUTHORIZED,
                body = ErrorMessage(e.message.toString()),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get employee manager list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = EmployeeManager::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/manager/list")
    fun getEmployeeManagerList(
        organisationId: Long,
        id: List<Int>,
        firstManagerId: List<Int>,
        secondManagerId: List<Int>,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    EmployeeManager(
                        employeesCount =
                            employeeService.getEmployeeManagerCount(
                                organisationId = organisationId,
                                id = id,
                                firstManagerId = firstManagerId,
                                secondManagerId = secondManagerId,
                            ),
                        employeeManagerData =
                            employeeService.getEmployeeManagerList(
                                organisationId = organisationId,
                                id = id,
                                firstManagerId = firstManagerId,
                                secondManagerId = secondManagerId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get employee", description = "Get employee details by manager id")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = ManagerReportees::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/by-manager")
    fun fetchEmployeesByManager(
        organisationId: Long,
        managerId: Long,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    ManagerReportees(
                        reporteesCount =
                            employeeService.fetchEmployeesByManagerCount(
                                organisationId = organisationId,
                                managerId = managerId,
                            ),
                        managerId = managerId,
                        reporteesData =
                            employeeService.fetchEmployeesByManager(
                                organisationId = organisationId,
                                managerId = managerId,
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Update employee's manager")
    @Put("/")
    fun updateEmployeesManager(managerUpdateDataList: List<ManagerUpdateDataList>): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = employeeService.updateEmployeesManager(managerUpdateDataList),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get employee", description = "Get employee details by unique id")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = EmpData::class))]),
        ApiResponse(responseCode = "401", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/by-id")
    fun getEmployeeById(
        id: Long,
        reviewCycleId: Long?,
    ): HttpResponse<Any> =
        try {
            Response(
                status = ResponseType.SUCCESS,
                message = "",
                body = employeeService.getEmployeeById(id, reviewCycleId),
            )
        } catch (e: UserNotFoundException) {
            Response(
                status = ResponseType.UNAUTHORIZED,
                message = "Unauthorized Access! Please contact System Admin/HR",
                body = ErrorMessage(e.message.toString()),
            )
        } catch (e: Exception) {
            Response(
                status = ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get employees count", description = "Get active employees count during review cycle")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "long"))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/get-all/by-review-cycle")
    fun fetchActiveEmployeesCountDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = employeeService.fetchActiveEmployeesCountDuringReviewCycle(organisationId, reviewCycleId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get gender list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "array", implementation = GenderInfo::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/gender-list")
    fun fetchGenderList(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = EmployeeGender.getGendersWithId(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Get experience list")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "array", implementation = EmployeeExperience::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/experience-list")
    fun fetchExperienceList(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = generateExperienceList(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}
