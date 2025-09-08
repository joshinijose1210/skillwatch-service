package scalereal.core.slack

object Constants {
    const val FEEDBACK_DRAFT_TITLE = "Draft Saved Successfully"
    const val FEEDBACK_SAVED_DETAIL = "Feedback submitted successfully and will be notified to the respective team member."
    const val REQUEST_FEEDBACK_SAVED_DETAIL = "Request feedback is saved as draft."
    const val FEEDBACK_DRAFT_DETAIL = "To view the saved draft, close this pop-up and you can go to *'Edit Feedback'* feature."
    const val SUGGESTION_SUBMITTED_SUCCESS_MESSAGE = "Thank you for your suggestion. Your voice matters!"
    const val LENGTH_VALIDATION_ERROR = "Please write more than 50 and less than 1000 characters."
    const val AT_LEAST_ONE_FIELD_REQUIRED_ERROR = "Please fill at least one field."
}

enum class FeedbackType(
    val label: String,
) {
    POSITIVE("positive"),
    IMPROVEMENT("improvement"),
    APPRECIATION("appreciation"),
}
