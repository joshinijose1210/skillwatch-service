package scalereal.core.models

/*progressId of this Data class is used in database,
before changing id or name look for its impact*/
enum class SuggestionProgress(
    val progressId: Int,
    val progressName: String,
) {
    TODO(progressId = 1, progressName = "To Do"),
    IN_PROGRESS(progressId = 4, progressName = "In Progress"),
    COMPLETED(progressId = 2, progressName = "Completed"),
    DEFERRED(progressId = 3, progressName = "Deferred"),
    ;

    companion object {
        fun getProgressListWithId(): List<SuggestionProgressInfo> =
            entries.map { progress ->
                SuggestionProgressInfo(progress.progressId, progress.progressName)
            }
    }
}

data class SuggestionProgressInfo(
    val progressId: Int,
    val progressName: String,
)
