package scalereal.core.slack

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.GoalBasicDetails

@Singleton
class GoalCompletedNotification(
    private val employeeRepository: EmployeeRepository,
    private val slackService: SlackService,
    @Inject private var appConfig: AppConfig,
) {
    private val instanceUrl = appConfig.getInstanceUrl()

    fun sendNotification(goal: GoalBasicDetails) {
        val assignedToDetails = employeeRepository.getEmployeeDataByUniqueId(goal.assignedTo)
        val assignedToSlackUser =
            slackService.getUserByEmailId(assignedToDetails.emailId, assignedToDetails.organisationId)
        if (assignedToSlackUser != null) {
            val createdByDetails = employeeRepository.getEmployeeDataByUniqueId(goal.createdBy)
            var createdByName = "*${createdByDetails.getEmployeeNameWithEmployeeId()} (User not on Slack)*"
            val createdBySlackUser =
                slackService.getUserByEmailId(createdByDetails.emailId, createdByDetails.organisationId)
            if (createdBySlackUser != null) createdByName = "<@${createdBySlackUser.id}>"

            val message =
                "Hi <@${assignedToSlackUser.id}>,\n" +
                    "We want to inform you that the deadline for the Goal (${goal.goalId}) provided by $createdByName " +
                    "has been completed. Kindly click on the below-mentioned link to view goals on the dashboard -\n" +
                    "                                                                           " +
                    "<$dashboardURL|*View Goals*>\n" +
                    "You can also click on the below-mentioned link to request feedback for the Goals -\n" +
                    "                                                                           " +
                    "<$requestFeedbackURL|*Request Feedback*>\n" +
                    "If you have any questions or there's anything at all you're unclear about, " +
                    "don't hesitate to reach out to HR directly.\n"

            val layoutBlock = slackService.blockBuilder(message)
            slackService.sendBlockMessageToUser(
                assignedToDetails.organisationId,
                layoutBlock,
                assignedToSlackUser.id,
                message,
            )
        }
    }

    private val dashboardURL = "$instanceUrl/"
    private val requestFeedbackURL = appConfig.getRequestFeedbackUrl()
}
