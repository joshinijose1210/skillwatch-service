package scalereal.core.models.domain

data class AnalyticsEmployeesData(
    val gendersData: GendersData,
    val averageTenure: AverageTenure,
    val averageAge: AverageAge,
    val experienceRangeCount: List<ExperienceRange>,
    val employeesType: EmployeesType,
    val teamEmployeeCount: List<TeamEmployeeCount>,
)
