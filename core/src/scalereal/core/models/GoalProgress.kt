package scalereal.core.models

/*progressId of this Data class is used in database,
before changing id or name look for its impact*/
enum class GoalProgress(
    val progressId: Int,
    val progressName: String,
) {
    TODO(progressId = 1, progressName = "To Do"),
    IN_PROGRESS(progressId = 2, progressName = "In Progress"),
    COMPLETED(progressId = 3, progressName = "Completed"),
    ;

    companion object {
        fun getProgressListWithId(): List<GoalProgressInfo> =
            entries.map { progress ->
                GoalProgressInfo(progress.progressId, progress.progressName)
            }
    }
}

data class GoalProgressInfo(
    val progressId: Int,
    val progressName: String,
)

enum class GoalType(
    val typeId: Int,
    val typeName: String,
) {
    INDIVIDUAL(typeId = 1, typeName = "Individual"),
    TEAM(typeId = 2, typeName = "Team"),
    DEPARTMENT(typeId = 3, typeName = "Department"),
    ORGANISATION(typeId = 4, typeName = "Organisation"),
    ;

    companion object {
        fun getGoalTypes(): List<GoalTypeInfo> =
            entries.map { type ->
                GoalTypeInfo(type.typeId, type.typeName)
            }
    }
}

data class GoalTypeInfo(
    val typeId: Int,
    val typeName: String,
)
