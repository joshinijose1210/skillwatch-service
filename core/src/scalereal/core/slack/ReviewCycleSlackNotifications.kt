package scalereal.core.slack

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.ReviewCycle
import java.time.format.DateTimeFormatter

@Singleton
class ReviewCycleSlackNotifications(
    private val employeeService: EmployeeService,
    private val slackService: SlackService,
    @Inject private var appConfig: AppConfig,
) {
    private val instanceUrl = appConfig.getInstanceUrl()
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun reviewCycleStarted(reviewCycle: ReviewCycle) {
        val reviewCycleTimeline =
            "*Review Cycle Timeline -* ${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.endDate.toLocalDate().format(fullMonthDateFormatter)}\n" +
                "*Self Review Timeline -* ${reviewCycle.selfReviewStartDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.selfReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}\n" +
                "*Manager Review Timeline -* ${reviewCycle.managerReviewStartDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.managerReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}\n" +
                "*Check-in with Manager Timeline -* " +
                "${reviewCycle.checkInWithManagerStartDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.checkInWithManagerEndDate.toLocalDate().format(fullMonthDateFormatter)}"
        val formattedReviewCycleTimeline = reviewCycleTimeline.lines().joinToString("\n") { "> $it" }
        val message =
            "*Performance Review Cycle has started (${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)}* " +
                "*to ${reviewCycle.endDate.toLocalDate().format(fullMonthDateFormatter)})* :tada:\n" +
                "We’re thrilled to announce that on ${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)}, " +
                "we'd be starting our new Review Cycle. This process is crucial in determining what progress we've made " +
                "individually in contribution to the company's objectives for the period in review." + "\n" +
                "Here's all you need to know:\n" +
                formattedReviewCycleTimeline +
                "\nIf you have any questions or there's anything at all you're unclear about, " +
                "don't hesitate to reach out to your manager, or HR directly."

        val layoutBlock = slackService.blockBuilder(message)
        val employeesEmail: List<String> = employeeService.getActiveEmployees(reviewCycle.organisationId).map { it.emailId }
        employeesEmail.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, reviewCycle.organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(reviewCycle.organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun reviewCycleEdited(reviewCycle: ReviewCycle) {
        val reviewCycleTimeline =
            "*Review Cycle Timeline -* ${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.endDate.toLocalDate().format(fullMonthDateFormatter)}\n" +
                "*Self Review Timeline -* ${reviewCycle.selfReviewStartDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.selfReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}\n" +
                "*Manager Review Timeline -* ${reviewCycle.managerReviewStartDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.managerReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}\n" +
                "*Check-in with Manager Timeline -* " +
                "${reviewCycle.checkInWithManagerStartDate.toLocalDate().format(fullMonthDateFormatter)} " +
                "to ${reviewCycle.checkInWithManagerEndDate.toLocalDate().format(fullMonthDateFormatter)}"
        val formattedReviewCycleTimeline = reviewCycleTimeline.lines().joinToString("\n") { "> $it" }
        val message =
            "An important update regarding the current review cycle. " +
                "Our team has been working diligently to enhance our performance evaluation process," +
                " and as a result, we have made some revisions to the review cycle :alert:\n" +
                "Revised Timeline:\n" +
                formattedReviewCycleTimeline +
                "\nIf you have any questions or there's anything at all you're unclear about, " +
                "don't hesitate to reach out to your manager, or HR directly."

        val layoutBlock = slackService.blockBuilder(message)
        val employeesEmail: List<String> = employeeService.getActiveEmployees(reviewCycle.organisationId).map { it.emailId }
        employeesEmail.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, reviewCycle.organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(reviewCycle.organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun reviewCycleEnded(
        reviewCycle: ReviewCycle,
        organisationId: Long,
    ) {
        val employees = employeeService.getEmployeesWithReviewCyclePermission(organisationId)
        employees.map { employee ->
            val message =
                "Hi ${employee.firstName},\n" +
                    "We want to inform you that the previous review cycle " +
                    "*(${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)} " +
                    "to ${reviewCycle.endDate.toLocalDate().format(fullMonthDateFormatter)})*" +
                    " has now come to a close. We appreciate your participation and valuable input throughout this process.\n" +
                    "However, we would like to remind you that it's essential to create a new review to ensure you don't miss out on " +
                    "the upcoming review cycle. To start a new review cycle, please click on the below-mentioned link -\n" +
                    "                                                        <$reviewCycleListingURL|*Add Review Cycle*>\n" +
                    "Please ensure that you create the new review cycle promptly and communicate the timeline and expectations to the " +
                    "employees. This will help us maintain a structured approach to performance management and maximize " +
                    "the benefits of the review process."
            val layoutBlock = slackService.blockBuilder(message)
            val slackUser = slackService.getUserByEmailId(employee.emailId, reviewCycle.organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(reviewCycle.organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    private val reviewCycleListingURL = "$instanceUrl/performance-review/review-cycles"
}
