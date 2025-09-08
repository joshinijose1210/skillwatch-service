package scalereal.core.slack

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.models.SuggestionProgress

@Singleton
class SuggestionSlackNotifications(
    private val employeeRepository: EmployeeRepository,
    private val employeeService: EmployeeService,
    private val slackService: SlackService,
    @Inject private var appConfig: AppConfig,
) {
    private val receivedSuggestionURL = appConfig.getReceivedSuggestionUrl()
    private val submittedSuggestionURL = appConfig.getSubmittedSuggestionUrl()

    fun sendSuggestionNotification(
        suggestedById: Long,
        suggestion: String,
    ) {
        val organisationId = employeeRepository.getEmployeeById(suggestedById).organisationId
        val slackUserList =
            employeeService.getEmployeesWithSuggestionsReceivedPermission(organisationId).map { it.emailId }
        val formattedSuggestion = formatSlackMessage(suggestion)
        val message =
            "*Incoming Suggestion Alert!* :bulb:\n" +
                "Someone just dropped a fresh idea in the Suggestion Box.\n" +
                formattedSuggestion + "\n" +
                "Head over to SkillWatch to take action.\n" +
                ":point_right: <$receivedSuggestionURL|View Suggestion Link>"

        val layoutBlock = slackService.blockBuilder(message)

        slackUserList.forEach { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun sendSuggestionProgressUpdate(
        suggestedById: Long,
        progressId: Int,
        comment: String? = null,
    ) {
        val suggestedBy = employeeRepository.getEmployeeById(suggestedById)
        val slackUser = slackService.getUserByEmailId(suggestedBy.emailId, suggestedBy.organisationId) ?: return

        val message = createSuggestionProgressMessage(progressId, comment)
        if (message != null) {
            val layoutBlock = slackService.blockBuilder(message)
            slackService.sendBlockMessageToUser(suggestedBy.organisationId, layoutBlock, slackUser.id, message)
        }
    }

    private fun createSuggestionProgressMessage(
        progressId: Int,
        comment: String?,
    ): String? {
        val (title, body) =
            when (progressId) {
                SuggestionProgress.IN_PROGRESS.progressId ->
                    "*Update: Your Suggestion is Now in Progress* :tada:" to
                        "Yay! The suggestion you submitted on SkillWatch is now marked as *In Progress*. " +
                        "We're actively working on it and will keep you posted!"
                SuggestionProgress.COMPLETED.progressId ->
                    "*Update: Your Suggestion is Now Completed* :white_check_mark:" to
                        "Great news! Your suggestion has been marked as *Completed* on SkillWatch. " +
                        "Thanks for helping us build a better workplace. Keep the ideas coming!"
                SuggestionProgress.DEFERRED.progressId ->
                    "*Update: Your Suggestion is Deferred for Now* :clock3:" to
                        "Your suggestion on SkillWatch is currently *Deferred*. " +
                        "We really appreciate your input and will revisit this later. Thanks for understanding!"
                else -> return null
            }

        return """
            $title
            
            $body
            
            ${comment?.let { "*Comment:* $it\n" } ?: ""}
            :point_right: <$submittedSuggestionURL|View Your Suggestion>
            """.trimIndent()
    }

    private fun formatSlackMessage(suggestion: String): String {
        val regex = Regex("\\[(.*?)]\\((.*?)\\)")
        val formattedSuggestion = regex.replace(suggestion, "<$2|$1>")
        return formattedSuggestion.lines().joinToString("\n") { "> $it" }
    }
}
