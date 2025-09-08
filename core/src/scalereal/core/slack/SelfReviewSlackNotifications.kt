package scalereal.core.slack

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class SelfReviewSlackNotifications(
    private val employeeService: EmployeeService,
    private val slackService: SlackService,
    @Inject private var appConfig: AppConfig,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    private val instanceUrl = appConfig.getInstanceUrl()

    fun selfReviewStarted(
        selfReviewEndDate: Date,
        organisationId: Long,
    ) {
        val tips =
            "Think back on the previous review cycle and note any accomplishments or achievements you’re proud of.\n" +
                "When you can, try to be as specific as possible, using numbers and figures to back up your statements.\n" +
                "Think of your personal goals for your career development and how they may align with your company’s goals and values.\n" +
                "Prepare any questions you might have for your manager.\n" +
                "Be ready to lead the conversation. This is your performance review, after all!"
        val formattedTips = tips.lines().joinToString("\n") { "> $it" }
        val message =
            "This is your friendly reminder that Self-review has started and will end on " +
                "${selfReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}.\n" +
                "We wanted to share some tips to help you get the most out of this process -\n" +
                formattedTips +
                "\nPlease click on the below-mentioned link to complete your self-review -\n" +
                "                                                                                                                 " +
                "<$reviewTimelineURL|*Start Self Review*>\n" +
                "If you have any questions or there's anything at all you're unclear about, " +
                "don't hesitate to reach out to your manager, or you can reach out to HR directly."

        val layoutBlock = slackService.blockBuilder(message)
        val employeesEmail: List<String> = employeeService.getActiveEmployees(organisationId).map { it.emailId }
        employeesEmail.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun selfReviewFiveDayReminder(
        employeesEmailId: List<String>,
        selfReviewEndDate: Date,
        organisationId: Long,
    ) {
        val pointer =
            "Performance reviews are a great opportunity for you to learn how much you have grown in your role, " +
                "and to explore more growth opportunities with your manager."
        val formattedPointer = pointer.lines().joinToString("\n") { "> $it" }
        val message =
            "Just sending you a friendly reminder to please complete your self-review for your performance review " +
                "${selfReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}. *(5 days left for the Self Review to be completed)*\n" +
                formattedPointer +
                "\nPlease click on the below-mentioned link to complete your self-review -\n" +
                "                                                                                                                 " +
                "<$reviewTimelineURL|*Start Self Review*>\n" +
                "If you need any assistance or clarification, please feel free to reach out to HR."

        val layoutBlock = slackService.blockBuilder(message)
        employeesEmailId.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun selfReviewLastDayReminder(
        employeesEmailId: List<String>,
        organisationId: Long,
    ) {
        val pointer =
            "By now, you should have already submitted a self-review for your performance review. " +
                "If you haven’t, please do so immediately."
        val formattedPointer = pointer.lines().joinToString("\n") { "> $it" }
        val message =
            "Just sending you a friendly reminder to please complete your self-review for your performance review by *today*." +
                " *(Last day reminder to complete the Self Review)*\n" +
                formattedPointer +
                "\nPlease click on the below-mentioned link to complete your self-review -\n" +
                "                                                                                                                 " +
                "<$reviewTimelineURL|*Start Self Review*>\n" +
                "If you need any assistance or clarification, please feel free to reach out to HR."

        val layoutBlock = slackService.blockBuilder(message)
        employeesEmailId.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun selfReviewSubmitted(
        reviewTo: Long?,
        organisationId: Long,
    ) {
        val message =
            "Congratulations! Your self-review for the current review cycle has been submitted successfully.\n" +
                "Please click on the below-mentioned link to view your submitted self-review -\n" +
                "                                                                                                                 " +
                "<$selfReviewListingURL|*View Self Review*>\n" +
                "If you need any assistance or clarification, please feel free to reach out to HR."

        val layoutBlock = slackService.blockBuilder(message)
        val employeeEmail = employeeService.getEmployeeById(reviewTo).emailId
        val slackUser = slackService.getUserByEmailId(employeeEmail, organisationId)
        if (slackUser != null) {
            slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
        }
    }

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
    private val selfReviewListingURL = "$instanceUrl/performance-review/self-review"
}
