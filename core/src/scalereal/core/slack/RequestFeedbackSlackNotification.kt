package scalereal.core.slack

import jakarta.inject.Inject
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.AppConfig

class RequestFeedbackSlackNotification(
    private val employeeRepository: EmployeeRepository,
    private val slackService: SlackService,
    @Inject private var appConfig: AppConfig,
) {
    fun requestReceivedNotification(
        requestedById: Long,
        feedbackFromId: List<Long>,
        feedbackToId: List<Long>,
        request: String,
    ) {
        val requestedByDetails = employeeRepository.getEmployeeDataByUniqueId(requestedById)
        val requestedByNameRaw = requestedByDetails.getEmployeeNameWithEmployeeId()
        val requestedBySlackUser = slackService.getUserByEmailId(requestedByDetails.emailId, requestedByDetails.organisationId)
        val requestedBySlackMention = requestedBySlackUser?.let { "<@${it.id}>" } ?: "$requestedByNameRaw (User not on Slack)"

        feedbackFromId.forEach { feedbackFrom ->
            val feedbackFromDetails = employeeRepository.getEmployeeDataByUniqueId(feedbackFrom)
            val feedbackFromSlackUser = slackService.getUserByEmailId(feedbackFromDetails.emailId, feedbackFromDetails.organisationId)

            if (feedbackFromSlackUser != null) {
                feedbackToId.forEach { feedbackTo ->

                    val (title, message) =
                        buildInternalFeedbackSlackMessage(
                            requestedBySlackMention = requestedBySlackMention,
                            feedbackToId = feedbackTo,
                            requestedById = requestedById,
                            requestContext = request,
                        )

                    val layoutBlock = slackService.blockBuilder("*$title*\n\n$message")
                    slackService.sendBlockMessageToUser(
                        feedbackFromDetails.organisationId,
                        layoutBlock,
                        feedbackFromSlackUser.id,
                        title,
                    )
                }
            }
        }
    }

    private fun buildInternalFeedbackSlackMessage(
        requestedBySlackMention: String,
        feedbackToId: Long,
        requestedById: Long,
        requestContext: String,
    ): Pair<String, String> {
        val isSelfFeedback = feedbackToId == requestedById

        val (title, subjectLine) =
            if (isSelfFeedback) {
                "Feedback Request from $requestedBySlackMention about their performance" to
                    "*$requestedBySlackMention* has requested your valuable feedback about *their* " +
                    "performance as part of our review process."
            } else {
                val feedbackTo = employeeRepository.getEmployeeDataByUniqueId(feedbackToId)
                val feedbackToNameRaw = feedbackTo.getEmployeeNameWithEmployeeId()
                val feedbackToSlackUser = slackService.getUserByEmailId(feedbackTo.emailId, feedbackTo.organisationId)

                val feedbackToTitle = feedbackToSlackUser?.let { "<@${it.id}>" } ?: "$feedbackToNameRaw (User not on Slack)"
                val feedbackToSubject =
                    feedbackToSlackUser?.let { "<@${it.id}>'s" }
                        ?: "${feedbackTo.firstName} ${feedbackTo.lastName}'s (${feedbackTo.employeeId}) (User not on Slack)"

                "Feedback Request from $requestedBySlackMention about $feedbackToTitle" to
                    "*$requestedBySlackMention* has requested your valuable feedback about *$feedbackToSubject* " +
                    "performance as part of our review process."
            }

        val messageBody =
            buildString {
                appendLine(
                    "$subjectLine Your perspective on the below context will help us better understand their strengths, contributions, " +
                        "and areas they can grow in.",
                )
                appendLine("*Context:*\n$requestContext\n")
                appendLine("Here’s a quick form to share your thoughts and should take less than 2–3 minutes.")
                append("<$requestFeedbackURL|*Feedback Form Link*>")
            }

        return title to messageBody
    }

    fun requestedFeedbackReceivedNotification(
        requestedById: Long,
        feedbackFromId: Long,
    ) {
        val requestedByDetails = employeeRepository.getEmployeeDataByUniqueId(requestedById)
        val requestedBySlackUser =
            slackService.getUserByEmailId(requestedByDetails.emailId, requestedByDetails.organisationId)
        if (requestedBySlackUser != null) {
            val feedbackFromDetails = employeeRepository.getEmployeeDataByUniqueId(feedbackFromId)
            var feedbackFromName = "*${feedbackFromDetails.getEmployeeNameWithEmployeeId()} (User not on Slack)*"
            val feedbackFromSlackUser = slackService.getUserByEmailId(feedbackFromDetails.emailId, feedbackFromDetails.organisationId)
            if (feedbackFromSlackUser != null) feedbackFromName = "<@${feedbackFromSlackUser.id}>"

            val message =
                "Hi <@${requestedBySlackUser.id}>,\n" +
                    "\n" +
                    "We want to inform you that $feedbackFromName has provided feedback for the feedback request raised. " +
                    "Kindly click on the below-mentioned link to view the feedback received.\n" +
                    "                                                                " +
                    "<$requestFeedbackURL|*View Feedback Received*>\n" +
                    "\nIf you have any questions or there's anything at all you're unclear about, " +
                    "don't hesitate to reach out to HR directly."
            val layoutBlock = slackService.blockBuilder(message)
            slackService.sendBlockMessageToUser(
                feedbackFromDetails.organisationId,
                layoutBlock,
                requestedBySlackUser.id,
                message,
            )
        }
    }

    private val requestFeedbackURL = appConfig.getRequestFeedbackUrl()
}
