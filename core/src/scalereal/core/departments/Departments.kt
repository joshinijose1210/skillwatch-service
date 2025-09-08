package scalereal.core.departments

enum class Department(
    val departmentId: Int,
    val departmentName: String,
) {
    LEADERSHIP(departmentId = 1, departmentName = "Executive Leadership"),
    HR(departmentId = 2, departmentName = "Human Resource"),
    ;

    companion object {
        fun getDefaults(): List<DefaultDepartment> =
            Department.entries.map {
                DefaultDepartment(it.departmentId, it.departmentName)
            }
    }
}

data class DefaultDepartment(
    val departmentId: Int,
    val departmentName: String,
)
