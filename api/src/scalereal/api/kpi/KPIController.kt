package scalereal.api.kpi

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Part
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
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
import scalereal.core.exception.KPINotFoundException
import scalereal.core.kpi.KPIService
import scalereal.core.models.domain.KPI
import scalereal.core.models.domain.KPIDepartmentTeamDesignations
import scalereal.core.models.domain.KPIImportResponse
import scalereal.core.models.domain.KPIResponse
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter

@Tag(name = "KPI")
@Controller(value = "/api/kpi")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class KPIController(
    private val kpiService: KPIService,
) {
    @Inject
    lateinit var roleService: RoleService

    private val moduleName = Modules.KPIs.moduleName

    @Operation(summary = "Create KPI")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Post("/")
    fun create(
        organisationId: Long,
        kpiTitle: String,
        kpiDescription: String,
        kraId: Long,
        kpiDepartmentTeamDesignations: List<KPIDepartmentTeamDesignations>,
        kpiStatus: Boolean,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
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
                    kpiService.create(
                        KPI(
                            organisationId = organisationId,
                            id = -999,
                            title = kpiTitle,
                            description = kpiDescription,
                            kraId = kraId,
                            kpiDepartmentTeamDesignations = kpiDepartmentTeamDesignations,
                            status = kpiStatus,
                            versionNumber = -999,
                        ),
                        UserActivityData(
                            actionBy = actionBy,
                            ipAddress = ipAddress,
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get all KPI")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = KPIResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/")
    fun fetchKPIs(
        organisationId: Long,
        searchText: String?,
        departmentId: List<Int>?,
        teamId: List<Int>?,
        designationId: List<Int>?,
        kraId: List<Int>?,
        status: List<String>?,
        page: Int?,
        limit: Int?,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body =
                    KPIResponse(
                        totalKPIs =
                            kpiService.count(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
                                departmentId = departmentId ?: listOf(-99),
                                teamId = teamId ?: listOf(-99),
                                designationId = designationId ?: listOf(-99),
                                kraId = kraId ?: listOf(-99),
                                status = status ?: listOf("true", "false"),
                            ),
                        kpis =
                            kpiService.fetchKPIs(
                                organisationId = organisationId,
                                searchText = searchText ?: "",
                                departmentId = departmentId ?: listOf(-99),
                                teamId = teamId ?: listOf(-99),
                                designationId = designationId ?: listOf(-99),
                                kraId = kraId ?: listOf(-99),
                                status = status ?: listOf("true", "false"),
                                page = page ?: 1,
                                limit = limit ?: Int.MAX_VALUE,
                            ),
                    ),
            )
        } catch (e: KPINotFoundException) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Download KPI List as PDF")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "PDF generated successfully"),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/download")
    fun downloadKpiPdf(
        organisationId: Long,
        departmentId: Int,
        teamId: Int,
        designationId: Int,
        status: List<String>?,
    ): HttpResponse<Any> =
        try {
            val pdfBytes =
                kpiService.exportKpisAsPdf(
                    organisationId = organisationId,
                    departmentId = departmentId,
                    teamId = teamId,
                    designationId = designationId,
                    status = status,
                )

            HttpResponse
                .ok<Any>(pdfBytes)
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"KPI-list.pdf\"")
        } catch (e: Exception) {
            HttpResponse.badRequest(ErrorMessage(e.message ?: "An unexpected error occurred"))
        }

    @Operation(summary = "Edit KPI")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Patch("/")
    fun update(
        organisationId: Long,
        id: Long,
        kpiTitle: String,
        kpiDescription: String,
        kraId: Long,
        kpiDepartmentTeamDesignations: List<KPIDepartmentTeamDesignations>,
        kpiStatus: Boolean,
        actionBy: Long,
        ipAddress: String,
        authentication: Authentication,
    ): HttpResponse<Any> {
        val permissions = roleService.hasPermission(organisationId, authentication.roles, moduleName)
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
                    kpiService.update(
                        KPI(
                            organisationId = organisationId,
                            id = id,
                            title = kpiTitle,
                            description = kpiDescription,
                            kraId = kraId,
                            kpiDepartmentTeamDesignations = kpiDepartmentTeamDesignations,
                            status = kpiStatus,
                            versionNumber = -999,
                        ),
                        UserActivityData(
                            actionBy = actionBy,
                            ipAddress = ipAddress,
                        ),
                    ),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
    }

    @Operation(summary = "Get KPI", description = "Get KPI by employee unique id")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(type = "array", implementation = KPI::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
    )
    @Get("/fetch-by-employee-id")
    fun fetchKpiByEmployeeId(
        organisationId: Long,
        reviewToId: Long,
    ): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = kpiService.fetchKpiByEmployeeId(organisationId = organisationId, reviewToId = reviewToId),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.BAD_REQUEST,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Download Engineering KPI template")
    @Get("/engineering-template")
    fun getEngineeringKpiTemplate(): HttpResponse<ByteArray> {
        val resource =
            this::class.java.classLoader.getResourceAsStream("kpi-templates/engineering-template.csv")
                ?: throw FileNotFoundException("Template not found")

        val templateBytes = resource.readAllBytes()

        return HttpResponse.ok(templateBytes).header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=engineering_kpi_template.csv",
        )
    }

    @Operation(summary = "Download BA KPI template")
    @Get("/ba-template")
    fun getBaKpiTemplate(): HttpResponse<ByteArray> {
        val resource =
            this::class.java.classLoader.getResourceAsStream("kpi-templates/ba-template.csv")
                ?: throw FileNotFoundException("Template not found")

        val templateBytes = resource.readAllBytes()

        return HttpResponse.ok(templateBytes).header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=ba_kpi_template.csv",
        )
    }

    @Operation(summary = "Download QA KPI template")
    @Get("/qa-template")
    fun getQaKpiTemplate(): HttpResponse<ByteArray> {
        val resource =
            this::class.java.classLoader.getResourceAsStream("kpi-templates/qa-template.csv")
                ?: throw FileNotFoundException("Template not found")

        val templateBytes = resource.readAllBytes()

        return HttpResponse.ok(templateBytes).header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=qa_kpi_template.csv",
        )
    }

    @Operation(summary = "Download HR KPI template")
    @Get("/hr-template")
    fun getHrKpiTemplate(): HttpResponse<ByteArray> {
        val resource =
            this::class.java.classLoader.getResourceAsStream("kpi-templates/hr-template.csv")
                ?: throw FileNotFoundException("Template not found")

        val templateBytes = resource.readAllBytes()

        return HttpResponse.ok(templateBytes).header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=hr_kpi_template.csv",
        )
    }

    @Operation(summary = "Bulk import", description = "Get template for KPI bulk import")
    @Get("/template")
    fun getKPITemplate(): HttpResponse<File> {
        val kpiTemplate = File("KPI_template.csv")
        kpiTemplate.createNewFile()
        val writer = FileWriter(kpiTemplate)
        writer.write(
            "KRA," + "KPI Title,KPI Description,Status (Y/N)," +
                " Department [Team 1 (Designation)] ," +
                " Department [Team 2 (Designation)]," +
                " Department [Team 3 (Designation)]," +
                " Department [Team 4 (Designation)]," +
                " Department [Team 5 (Designation)]," +
                " Department [Team 6 (Designation)]," +
                " Department [Team 7 (Designation)]," +
                " Department [Team 8 (Designation)]," +
                " Department [Team 9 (Designation)]," +
                " Department [Team 10 (Designation)]\n",
        )
        writer.flush()
        writer.close()

        return HttpResponse.ok(kpiTemplate).header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=KPI_template.csv",
        )
    }

    @Operation(summary = "Bulk import", description = "Upload bulk import file")
    @Post("/bulk-import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun readKPI(
        @Part("file") file: CompletedFileUpload,
        organisationId: Long,
        userActivityData: UserActivityData,
    ): HttpResponse<String> {
        return try {
            if (!file.filename.endsWith(".csv")) {
                handleInvalidFileType()
            }
            val responseFile = kpiService.readKPI(organisationId, file, userActivityData)
            when {
                (responseFile?.file != null && responseFile.errorCount == 0) -> handleEmptyOrExceededDataFile(responseFile)
                responseFile?.file != null -> handleFileWithError(responseFile)
                else -> handleSuccessfulImport()
            }
        } catch (e: Exception) {
            return handleProcessingError()
        }
    }
}

private fun handleInvalidFileType(): HttpResponse<String> {
    val errorFile = File("error.csv")
    FileOutputStream(errorFile).use {
        it.write("The file must be of type .csv".toByteArray())
    }

    val responseMap =
        mapOf(
            "file" to errorFile.readBytes().toString(Charsets.UTF_8),
            "message" to "Invalid file type. The file must be of type .csv",
        )
    return buildBadRequestResponse(responseMap)
}

private fun handleEmptyOrExceededDataFile(errorFile: KPIImportResponse): HttpResponse<String> =
    if (errorFile.fileCount == 0) {
        val responseMap =
            mapOf(
                "file" to errorFile.file.readBytes().toString(Charsets.UTF_8),
                "message" to "The file you are trying to add is empty.",
            )
        buildBadRequestResponse(responseMap)
    } else {
        val responseMap =
            mapOf(
                "file" to errorFile.file.readBytes().toString(Charsets.UTF_8),
                "message" to
                    "The maximum limit for KPI uploads has been exceeded. Please reduce the number of KPI up to 500 and try again.",
            )
        buildBadRequestResponse(responseMap)
    }

private fun handleFileWithError(errorFile: KPIImportResponse): HttpResponse<String> {
    val successCount = errorFile.fileCount - errorFile.errorCount
    val errorMessage =
        when {
            errorFile.errorCount == errorFile.fileCount ->
                "The KPIs are not added due to an error. Please check the error file for more information."
            successCount == 1 && errorFile.errorCount == 1 ->
                "$successCount KPI is added successfully. ${errorFile.errorCount} KPI is not added due to an error. " +
                    "Please check the error file for more information."
            successCount == 1 ->
                "$successCount KPI is added successfully. ${errorFile.errorCount} KPIs are not added due to an error. " +
                    "Please check the error file for more information."
            errorFile.errorCount == 1 ->
                "$successCount KPIs are added successfully. " +
                    "${errorFile.errorCount} KPI is not added due to an error. " +
                    "Please check the error file for more information."
            else ->
                "$successCount KPIs are added successfully. " +
                    "${errorFile.errorCount} KPIs are not added due to an error. " +
                    "Please check the error file for more information."
        }
    val responseMap =
        mapOf(
            "file" to errorFile.file.readBytes().toString(Charsets.UTF_8),
            "message" to errorMessage,
        )
    return buildBadRequestResponse(responseMap)
}

private fun handleSuccessfulImport(): HttpResponse<String> {
    val successFile = File("success.csv")
    successFile.createNewFile()

    val writer = FileWriter(successFile)
    writer.write("KPIs Imported Successfully!")
    writer.flush()
    writer.close()

    val responseMap =
        mapOf(
            "file" to successFile.readBytes().toString(Charsets.UTF_8),
            "message" to "KPIs Imported Successfully!",
        )
    return HttpResponse
        .ok(buildJsonResponse(responseMap))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=success.csv")
        .contentType(MediaType.TEXT_CSV_TYPE)
}

private fun handleProcessingError(): HttpResponse<String> {
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
    return HttpResponse
        .badRequest(buildJsonResponse(responseMap))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=error.csv")
        .contentType(MediaType.TEXT_CSV_TYPE)
}

private fun buildBadRequestResponse(responseMap: Map<String, Any>): HttpResponse<String> {
    val objectMapper = ObjectMapper()
    val responseJson = objectMapper.writeValueAsString(responseMap)
    return HttpResponse
        .badRequest(responseJson)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .contentType(MediaType.TEXT_CSV_TYPE)
}

private fun buildJsonResponse(responseMap: Map<String, Any>): String {
    val objectMapper = ObjectMapper()
    return objectMapper.writeValueAsString(responseMap)
}
