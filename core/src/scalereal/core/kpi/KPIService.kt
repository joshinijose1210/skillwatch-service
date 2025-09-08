package scalereal.core.kpi

import com.opencsv.CSVReaderBuilder
import io.micronaut.http.multipart.CompletedFileUpload
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.text.StringEscapeUtils
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.exception.DuplicateDataException
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.ErrorKPI
import scalereal.core.models.domain.KPI
import scalereal.core.models.domain.KPIData
import scalereal.core.models.domain.KPIDepartmentTeamDesignations
import scalereal.core.models.domain.KPIImportResponse
import scalereal.core.models.domain.KPIOldData
import scalereal.core.models.domain.UserActivityData
import scalereal.core.models.removeExtraSpaces
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.StringReader
import java.util.Locale
import kotlin.math.min

@Singleton
class KPIService(
    private val kpiRepository: KPIRepository,
    private val reviewCycleRepository: ReviewCycleRepository,
    private val userActivityRepository: UserActivityRepository,
    moduleService: ModuleService,
    private val departmentRepository: DepartmentRepository,
    private val kraRepository: KRARepository,
    private val pdfGeneratorService: PdfGeneratorService,
) {
    @Inject
    lateinit var teamRepository: TeamRepository

    @Inject
    lateinit var designationRepository: DesignationRepository

    private val kpiModuleId = moduleService.fetchModuleId(Modules.KPIs.moduleName)

    fun create(
        kpi: KPI,
        userActivityData: UserActivityData,
    ) {
        try {
            val activeReviewCycle = reviewCycleRepository.fetchActiveReviewCycle(kpi.organisationId)
            if (activeReviewCycle != null && activeReviewCycle.isSelfReviewActive) {
                throw Exception("You cannot add new KPI after Self Review Timeline starts")
            } else if (kpi.title.length !in 5..60) {
                throw Exception("KPI Title must be between 5 and 60 characters.")
            } else {
                val maxKpiId = kpiRepository.getMaxKPIId(kpi.organisationId)
                val kpiId = KPIData.getKPIDisplayId(maxKpiId.plus(1).toString())
                kpiRepository.create(
                    organisationId = kpi.organisationId,
                    id = maxKpiId + 1,
                    kpiTitle = kpi.title.removeExtraSpaces(),
                    kpiDescription = kpi.description.removeExtraSpaces(),
                    kpiStatus = kpi.status,
                    kraId = kpi.kraId,
                    kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                    versionNumber = 1,
                )
                val activity =
                    if (kpi.status) {
                        "$kpiId Added and Published"
                    } else {
                        "$kpiId Added and Unpublished"
                    }
                addUserActivityLog(userActivityData = userActivityData, activity = activity, description = activity)
            }
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("unique_index_kpi_department_team_department_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate KPI Department Team Designation getting inserted")
                e.localizedMessage.contains("idx_unique_kpi_version_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate KPI Version getting inserted")
                else -> throw e
            }
        }
    }

    fun fetchKPIs(
        organisationId: Long,
        searchText: String,
        page: Int,
        limit: Int,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        kraId: List<Int>,
        status: List<String>,
    ): List<KPIData> =
        kpiRepository.fetchKPIs(
            organisationId = organisationId,
            searchText = searchText,
            departmentId = departmentId,
            teamId = teamId,
            designationId = designationId,
            kraId = kraId,
            status = status,
            offset = (page - 1) * limit,
            limit = limit,
        )

    fun count(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        kraId: List<Int>,
        status: List<String>,
    ): Int = kpiRepository.count(organisationId, searchText, departmentId, teamId, designationId, kraId, status)

    fun fetchKpiByEmployeeId(
        organisationId: Long,
        reviewToId: Long,
    ): List<KPIData> = kpiRepository.fetchKPIByEmployeeId(organisationId = organisationId, reviewToId = reviewToId)

    fun update(
        kpi: KPI,
        userActivityData: UserActivityData,
    ) {
        try {
            val activeReviewCycle = reviewCycleRepository.fetchActiveReviewCycle(kpi.organisationId)
            if (activeReviewCycle != null &&
                activeReviewCycle.isSelfReviewActive &&
                kpiRepository.hasReviewStartedForEmployeeWithKPI(kpiId = kpi.id, reviewCycleId = activeReviewCycle.reviewCycleId)
            ) {
                throw Exception("KPI editing is disabled as reviews are in progress.")
            }
            if (kpi.title.length !in 5..60) {
                throw Exception("KPI Title must be between 5 and 60 characters.")
            }
            val kpiOldData = kpiRepository.getKPIDataById(kpi.id)
            kpi.title = kpi.title.removeExtraSpaces()
            kpi.description = kpi.description.removeExtraSpaces()
            val oldDepartmentTeamDesignations: List<KPIDepartmentTeamDesignations> =
                kpiRepository.getKPIDepartmentTeamDesignationsMapping(
                    kpi.id,
                )
            val maxVersion = kpiRepository.getMaxVersionNumber(kpiOldData.kpiId.toLong())
            val isKpiDataUpdated = (
                kpiOldData.title != kpi.title ||
                    kpiOldData.description != kpi.description ||
                    oldDepartmentTeamDesignations != kpi.kpiDepartmentTeamDesignations ||
                    kpi.kpiDepartmentTeamDesignations.flatMap {
                        it.designationIds
                    } != oldDepartmentTeamDesignations.flatMap { it.designationIds } ||
                    kpi.kraId != kpiOldData.kraId
            )
            if (isKpiDataUpdated) {
                kpiRepository.update(kpi.organisationId, kpi.id, status = false)
                kpiRepository.create(
                    organisationId = kpi.organisationId,
                    id = kpiOldData.kpiId.toLong(),
                    kpiTitle = kpi.title,
                    kpiDescription = kpi.description,
                    kpiStatus = kpi.status,
                    kraId = kpi.kraId,
                    kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                    versionNumber = maxVersion + 1,
                )
            } else if (kpiOldData.status != kpi.status) {
                kpiRepository.update(kpi.organisationId, kpi.id, kpi.status)
            }
            updateActivityLog(
                isKpiDataUpdated = isKpiDataUpdated,
                kpiOldData = kpiOldData,
                kpiNewData = kpi,
                userActivityData = userActivityData,
            )
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("unique_index_kpi_department_team_department_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate KPI Department Team Designation getting inserted")
                e.localizedMessage.contains("idx_unique_kpi_version_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate KPI Version getting inserted")
                else -> throw e
            }
        }
    }

    private fun updateActivityLog(
        isKpiDataUpdated: Boolean,
        kpiOldData: KPIOldData,
        kpiNewData: KPI,
        userActivityData: UserActivityData,
    ) {
        val kpiId = KPIData.getKPIDisplayId(kpiOldData.kpiId)
        val activity =
            when {
                (isKpiDataUpdated && kpiOldData.status && !kpiNewData.status) -> "$kpiId Edited and Unpublished"
                (isKpiDataUpdated && !kpiOldData.status && kpiNewData.status) -> "$kpiId Edited and Published"
                (kpiOldData.status && !kpiNewData.status) -> "$kpiId Unpublished"
                (!kpiOldData.status && kpiNewData.status) -> "$kpiId Published"
                isKpiDataUpdated -> "$kpiId Edited"
                else -> null
            }
        if (activity != null) addUserActivityLog(userActivityData = userActivityData, activity = activity, description = activity)
    }

    fun readKPI(
        organisationId: Long,
        file: CompletedFileUpload,
        userActivityData: UserActivityData,
    ): KPIImportResponse? {
        val inputStream = file.inputStream
        val fileContent = inputStream.bufferedReader().use { it.readText() }
        val fileCount = countKPI(fileContent)
        val errorKPIS = mutableListOf<ErrorKPI>()
        val descriptionRegex = Regex("^[\\s\\S]{50,1000}\$")
        val statusRegex = Regex("^(yes|no|Yes|No|YES|NO|Y|N|y|n)$")
        val csvReader = CSVReaderBuilder(BufferedReader(StringReader(fileContent))).withSkipLines(1).build()

        when (fileCount) {
            0 -> return KPIImportResponse(writeErrorFile(error = "No KPIs to add", emptyList()), fileCount, errorCount = 0)
            in 501..Int.MAX_VALUE -> return KPIImportResponse(
                writeErrorFile(
                    error = "Max 500 KPIs are allowed to import at a time. Please reduce the number of KPIs and try again.",
                    emptyList(),
                ),
                fileCount,
                errorCount = 0,
            )
        }

        csvReader.forEach { data ->
            val kpiDepartmentTeamDesignationsIds = mutableListOf<KPIDepartmentTeamDesignations>()
            val errors = mutableListOf<String>()

            val kra = data.getOrNull(0)?.trim() ?: ""
            if (kra.isEmpty()) errors.add("Invalid KRA")

            val kpiTitle = data.getOrNull(1)?.trim() ?: ""
            if (kpiTitle.isEmpty() || kpiTitle.length !in 5..60) errors.add("Invalid KPI Title! 5-60 Characters are required.")

            val kpiDescriptionRaw = data.getOrNull(2)?.trim() ?: ""
            val kpiDescription = StringEscapeUtils.unescapeJava(kpiDescriptionRaw)
            if (kpiDescription.isEmpty() || !kpiDescription.matches(descriptionRegex)) {
                errors.add("Invalid KPI description! 50-1000 Characters are required.")
            }

            val status = data.getOrNull(3)?.trim()?.lowercase(Locale.getDefault()) ?: ""
            if (status.isEmpty() || !status.matches(statusRegex)) errors.add("Invalid Active Status. It should be Y/N")
            val kpiStatus = status == "yes" || status == "y"

            if (kra.isEmpty() || !kraRepository.isKRAExists(organisationId = organisationId, kra = kra)) {
                errors.add("KRA does not exist")
            }

            validateDepartmentTeams(data, organisationId, errors, kpiDepartmentTeamDesignationsIds)

            if (errors.isNotEmpty()) {
                val departmentTeamDesignations = mutableListOf<String>()
                for (i in 3 until minOf(data.size, 14)) {
                    val departmentTeamDesignation = data.getOrNull(i)?.trim() ?: ""
                    if (departmentTeamDesignation.isNotEmpty()) {
                        departmentTeamDesignations.add(departmentTeamDesignation)
                    }
                }
                errorKPIS.add(
                    ErrorKPI(
                        kra = kra,
                        kpiTitle = kpiTitle,
                        description = kpiDescriptionRaw,
                        status = status,
                        departmentTeamDesignations = departmentTeamDesignations,
                        error = '"' + errors.joinToString(", ") + '"',
                    ),
                )
                return@forEach
            }

            val kraId = kraRepository.getKRAId(organisationId, kra)

            val kpiData =
                KPI(
                    organisationId = organisationId,
                    id = -999,
                    title = kpiTitle,
                    description = convertDescriptionToHtml(kpiDescription),
                    status = kpiStatus,
                    versionNumber = 1,
                    kraId = kraId!!,
                    kpiDepartmentTeamDesignations = kpiDepartmentTeamDesignationsIds,
                )
            create(kpiData, userActivityData)
        }

        if (errorKPIS.isNotEmpty()) {
            val errorFile = writeErrorFile(null, errorKPIS)
            var errorCount = 0
            for (errorKPI in errorKPIS) {
                errorCount++
            }
            return KPIImportResponse(errorFile, fileCount, errorCount)
        }
        return null
    }

    private fun validateDepartmentTeams(
        data: Array<String>?,
        organisationId: Long,
        errors: MutableList<String>,
        kpiDepartmentTeamDesignationsIds: MutableList<KPIDepartmentTeamDesignations>,
    ) {
        val teamRegex = Regex("^([A-Za-z0-9-|]+[\\s]+)*[A-Za-z0-9-|]+\$")
        val departmentTeam1Designation = data?.getOrNull(4)?.trim() ?: ""
        if (data != null && departmentTeam1Designation.isNotEmpty()) {
            for (i in 4..min(data.size, 13)) {
                val departmentTeamDesignation = data.getOrNull(i)
                if (departmentTeamDesignation.isNullOrEmpty()) {
                    continue
                } else if (!(departmentTeamDesignation.contains("[") && departmentTeamDesignation.contains("]"))) {
                    errors.add("Add at least one Team in Department ${i - 2}.")
                } else if (!(departmentTeamDesignation.contains("(") && departmentTeamDesignation.contains(")"))) {
                    errors.add("Add at least one Designation in Team ${i - 2}.")
                } else {
                    val teamData =
                        departmentTeamDesignation
                            .substring(
                                departmentTeamDesignation.indexOf("[") + 1,
                                departmentTeamDesignation.indexOf("]"),
                            ).trim()
                    val departmentName = departmentTeamDesignation.substring(0, departmentTeamDesignation.indexOf("[")).trim()
                    val isDepartment = departmentRepository.isDepartmentExists(organisationId, departmentName)
                    if (!isDepartment.exists) {
                        errors.add("Department $departmentName does not exist")
                    } else if (!isDepartment.status) {
                        errors.add("Department $departmentName is inactive")
                    } else {
                        val departmentId = departmentRepository.getDepartmentId(organisationId, departmentName)
                        val listOfTeamDesignations = teamData.substring(teamData.indexOf("(") + 1, teamData.indexOf(")")).trim()
                        if (listOfTeamDesignations.isEmpty()) {
                            errors.add("Add at least one Designation in Team ${i - 2}.")
                        } else {
                            val teamName = teamData.substring(0, teamData.indexOf("(")).trim()
                            if (teamName.isEmpty()) continue
                            if (!teamName.matches(teamRegex)) {
                                errors.add("Invalid Team ${i - 2}")
                            } else {
                                val isTeam =
                                    teamRepository.isTeamExists(
                                        organisationId = organisationId,
                                        departmentId = departmentId,
                                        teamName = teamName,
                                    )
                                if (!isTeam.exists) {
                                    errors.add("Team $teamName does not exist")
                                } else if (!isTeam.status) {
                                    errors.add("Team $teamName is inactive")
                                } else {
                                    val teamId =
                                        teamRepository.getTeamId(
                                            organisationId = organisationId,
                                            departmentId = departmentId,
                                            teamName = teamName,
                                        )
                                    val designations = listOfTeamDesignations.split("|")
                                    val designationIds = validateDesignation(organisationId, designations, teamName.trim(), teamId, errors)
                                    if (kpiDepartmentTeamDesignationsIds.any { it.teamId == teamId }) {
                                        errors.add("Duplicate team $teamName found")
                                    } else {
                                        kpiDepartmentTeamDesignationsIds.add(
                                            KPIDepartmentTeamDesignations(departmentId, teamId, designationIds),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            errors.add("Invalid Department [Team 1 (Designation)]")
        }
    }

    private fun validateDesignation(
        organisationId: Long,
        designationNames: List<String>,
        teamName: String,
        teamId: Long,
        errors: MutableList<String>,
    ): MutableList<Long> {
        val designationRegex = Regex("^([A-Za-z0-9-|]+[\\s]+)*[A-Za-z0-9-|]+\$")
        val designationIds = mutableListOf<Long>()
        for (designation in designationNames) {
            if (designation.isEmpty()) continue
            if (!designation.trim().matches(designationRegex)) {
                errors.add("Invalid Designation $designation in $teamName")
            } else {
                val isDesignation =
                    designationRepository.isDesignationExists(
                        organisationId = organisationId,
                        teamId = teamId,
                        designationName = designation.trim(),
                    )
                if (!isDesignation.exists) {
                    errors.add("Designation $designation in $teamName does not exist")
                } else if (!isDesignation.status) {
                    errors.add("Designation $designation in $teamName is inactive")
                } else {
                    val designationId =
                        designationRepository.getDesignationId(
                            organisationId = organisationId,
                            designationName = designation.trim(),
                            teamId = teamId,
                        )
                    if (designationIds.contains(designationId)) {
                        errors.add("Duplicate designation $designation found")
                    } else {
                        designationIds.add(designationId)
                    }
                }
            }
        }
        return designationIds
    }

    private fun convertDescriptionToHtml(description: String): String {
        val lines = description.trim().split("\n")
        val sb = StringBuilder()

        for (line in lines) {
            sb.append("<p>").append(line.trim()).append("</p>")
        }

        return sb.toString()
    }

    private fun addUserActivityLog(
        userActivityData: UserActivityData,
        activity: String,
        description: String,
    ) = userActivityRepository.addActivity(
        actionBy = userActivityData.actionBy,
        moduleId = kpiModuleId,
        activity = activity,
        description = description,
        ipAddress = userActivityData.ipAddress,
    )

    fun exportKpisAsPdf(
        organisationId: Long,
        departmentId: Int,
        teamId: Int,
        designationId: Int,
        status: List<String>?,
    ): ByteArray {
        val kpis =
            fetchKPIs(
                organisationId = organisationId,
                searchText = "",
                departmentId = listOf(departmentId),
                teamId = listOf(teamId),
                designationId = listOf(designationId),
                kraId = listOf(-99),
                status = status ?: listOf("true", "false"),
                page = 1,
                limit = Int.MAX_VALUE,
            )
        return pdfGeneratorService.generateKpiPdf(kpis)
    }
}

private fun countKPI(content: String): Int {
    val csvReader = CSVReaderBuilder(BufferedReader(StringReader(content))).build()
    val records = csvReader.readAll()
    val kpiCount = records.size - 1
    return if (kpiCount > 0) kpiCount else 0
}

private fun writeErrorFile(
    error: String?,
    errorKPIS: List<ErrorKPI>,
): File {
    val errorFile = File("error_KPI.csv")
    errorFile.createNewFile()

    val writer = FileWriter(errorFile)
    if (error != null && errorKPIS.isEmpty()) {
        writer.write(error)
    } else {
        writer.write(
            "KRA, KPI Title, KPI Description, Status (Y/N), Department [Team 1 (Designation)] , Department [Team 2 (Designation)], " +
                "Department [Team 3 (Designation)], Department [Team 4 (Designation)],Department [Team 5 (Designation)], " +
                "Department [Team 6 (Designation)], Department [Team 7 (Designation)], Department [Team 8 (Designation)], " +
                "Department [Team 9 (Designation)], Department [Team 10 (Designation)], Error\n",
        )

        for (errorKPI in errorKPIS) {
            val escapedDescription = StringEscapeUtils.escapeCsv(errorKPI.description)
            writer.write(
                "${errorKPI.kra},${errorKPI.kpiTitle},$escapedDescription,${errorKPI.status},${errorKPI.error}\n",
            )
        }
    }
    writer.flush()
    writer.close()
    return errorFile
}
