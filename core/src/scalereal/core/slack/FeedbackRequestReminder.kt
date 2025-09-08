package scalereal.core.slack

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.AppConfig
import java.sql.Date

@Singleton
class FeedbackRequestReminder(
    private val employeeRepository: EmployeeRepository,
    private val slackService: SlackService,
    @Inject private var appConfig: AppConfig,
) {
    fun sendNotification(
        requestedById: Long,
        feedbackFromId: Long,
        date: Date,
    ) {
        val feedbackFromDetails = employeeRepository.getEmployeeDataByUniqueId(feedbackFromId)
        val feedbackFromSlackUser =
            slackService.getUserByEmailId(feedbackFromDetails.emailId, feedbackFromDetails.organisationId)
        if (feedbackFromSlackUser != null) {
            val requestedByDetails = employeeRepository.getEmployeeDataByUniqueId(requestedById)
            var requestedByName = "*${requestedByDetails.getEmployeeNameWithEmployeeId()} (User not on Slack)*"
            val requestedBySlackUser =
                slackService.getUserByEmailId(requestedByDetails.emailId, requestedByDetails.organisationId)
            if (requestedBySlackUser != null) requestedByName = "<@${requestedBySlackUser.id}>"

            val message =
                ":loudspeaker: *Reminder: Pending Feedback from $requestedByName on $date*\n" +
                    "Hi <@${feedbackFromSlackUser.id}>,\n" +
                    "Just a quick heads-up about the pending feedback request from $requestedByName. " +
                    "Your insights are crucial, and $requestedByName is eager to hear what you think.\n" +
                    "<$requestFeedbackURL|Click here> to submit requested feedback.\n" +
                    "Your input is highly appreciated!"

            val layoutBlock = slackService.blockBuilder(message)
            slackService.sendBlockMessageToUser(
                feedbackFromDetails.organisationId,
                layoutBlock,
                feedbackFromSlackUser.id,
                message,
            )
        }
    }

    private val requestFeedbackURL = appConfig.getRequestFeedbackUrl()
}
