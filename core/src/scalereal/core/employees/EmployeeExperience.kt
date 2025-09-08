package scalereal.core.employees

data class EmployeeExperience(
    val id: Int,
    val experienceString: String,
    val totalMonths: Int,
)

fun generateExperienceList(): List<EmployeeExperience> {
    val experienceList = mutableListOf<EmployeeExperience>()
    var uniqueID = 1 // Starting value for the unique ID

    for (totalMonths in 0..(60 * 12 + 11)) {
        val years = totalMonths / 12
        val months = totalMonths % 12

        val experienceString =
            "$years year${if (years != 1) "s" else ""} " +
                "$months month${if (months != 1) "s" else ""}"

        val experience = EmployeeExperience(uniqueID, experienceString, totalMonths)
        experienceList.add(experience)
        uniqueID++
    }
    return experienceList
}
