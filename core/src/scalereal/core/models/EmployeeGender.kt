package scalereal.core.models

/*genderId of this Data class is used in database,
before changing id or name look for its impact*/
enum class EmployeeGender(
    val genderId: Int,
    val genderName: String,
) {
    MALE(genderId = 1, genderName = "Male"),
    FEMALE(genderId = 2, genderName = "Female"),
    OTHERS(genderId = 3, genderName = "Others"),
    ;

    companion object {
        fun getGendersWithId(): List<GenderInfo> =
            values().map { gender ->
                GenderInfo(gender.genderId, gender.genderName)
            }
    }
}

data class GenderInfo(
    val genderId: Int,
    val genderName: String,
)
