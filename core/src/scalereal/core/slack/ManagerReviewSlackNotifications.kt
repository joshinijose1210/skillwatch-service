package scalereal.core.slack

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.Employee
import scalereal.core.review.SelfReviewRepository
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class ManagerReviewSlackNotifications(
    private val employeeService: EmployeeService,
    private val slackService: SlackService,
    private val selfReviewRepository: SelfReviewRepository,
    @Inject private var appConfig: AppConfig,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    private val instanceUrl = appConfig.getInstanceUrl()

    fun managerReviewStarted(
        managerReviewEndDate: Date,
        organisationId: Long,
    ) {
        val tips =
            "Create and review expectations, standards, and rules.\n" +
                "Educate employees about any behaviors they need improvement or modification.\n" +
                "Identify any strengths and weaknesses that weren’t already known.\n" +
                "Learn more about the employee.\n" +
                "Give specific, actionable feedback.\n" +
                "Plan for the future during the performance review."
        val formattedTips = tips.lines().joinToString("\n") { "> $it" }
        val message =
            "This is your friendly reminder that the Manager review has started and will end on " +
                "${managerReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}.\n" +
                "We wanted to share some tips to help you get the most out of this process -\n" +
                formattedTips +
                "\nPlease click on the below-mentioned link to complete the manager review -\n" +
                "                                                                                                                 " +
                "<$reviewTimelineURL|*Start Manager Review*>\n" +
                "If you need any guidance or have any questions, feel free to reach out to HR."

        val layoutBlock = slackService.blockBuilder(message)
        val managersEmail: List<String> = employeeService.getManagers(organisationId).map { it.emailId }
        managersEmail.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun managerReviewFiveDayReminder(
        managers: List<Employee>,
        managerReviewEndDate: Date,
        reviewCycleId: Long,
    ) {
        managers.map { manager ->
            val employees =
                selfReviewRepository.fetchInCompleteManagerReviewEmployees(
                    organisationId = manager.organisationId,
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            val employeesNames =
                employees.map { it.getEmployeeNameWithEmployeeId() }.sortedWith(
                    compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                        .thenBy { it.substringAfterLast(" ") },
                )
            val formattedEmployeesDetails = employeesNames.joinToString("\n") { "> $it" }
            val message =
                "Just sending you a friendly reminder to please complete the manager review for the below-mentioned employees by " +
                    "${managerReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}. " +
                    "*(5 days left for the Manager Review to be completed)*\n $formattedEmployeesDetails" +
                    "\nPlease click on the below-mentioned link to complete the manager review -\n" +
                    "                                                                                                                 " +
                    "<$reviewTimelineURL|*Start Manager Review*>\n" +
                    "Performance reviews are a great opportunity for you to learn how much the employee has grown in their role.\n" +
                    "If you need any assistance or clarification, please feel free to reach out to HR."

            val layoutBlock = slackService.blockBuilder(message)
            if (employees.isNotEmpty()) {
                val slackUser = slackService.getUserByEmailId(manager.emailId, manager.organisationId)
                if (slackUser != null) {
                    slackService.sendBlockMessageToUser(manager.organisationId, layoutBlock, slackUser.id, message)
                }
            }
        }
    }

    fun managerReviewLastDayReminder(
        managers: List<Employee>,
        reviewCycleId: Long,
    ) {
        managers.map { manager ->
            val employees =
                selfReviewRepository.fetchInCompleteManagerReviewEmployees(
                    organisationId = manager.organisationId,
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            val employeesNames =
                employees.map { it.getEmployeeNameWithEmployeeId() }.sortedWith(
                    compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                        .thenBy { it.substringAfterLast(" ") },
                )
            val formattedEmployeesDetails = employeesNames.joinToString("\n") { "> $it" }
            val message =
                "Just sending you a friendly reminder to please complete the manager review for the " +
                    "below-mentioned employees by *today*." +
                    " *(Last day reminder to complete the Manager Review)*\n" +
                    formattedEmployeesDetails +
                    "\nBy now, you should have already submitted the manager review for the current performance review. " +
                    "If you haven’t, please do so immediately.\n" +
                    "Please click on the below-mentioned link to complete the manager review -\n" +
                    "                                                                                                                 " +
                    "<$reviewTimelineURL|*Start Manager Review*>\n" +
                    "Performance reviews are a great opportunity for you to learn how much the employee has grown in their role.\n" +
                    "If you need any assistance or clarification, please feel free to reach out to HR."

            val layoutBlock = slackService.blockBuilder(message)
            if (employees.isNotEmpty()) {
                val slackUser = slackService.getUserByEmailId(manager.emailId, manager.organisationId)
                if (slackUser != null) {
                    slackService.sendBlockMessageToUser(manager.organisationId, layoutBlock, slackUser.id, message)
                }
            }
        }
    }

    fun managerReviewSubmittedToManager(
        reviewFrom: Long,
        organisationId: Long,
    ) {
        val firstManagerEmployeeList =
            employeeService.getEmployeeManagerList(
                organisationId = organisationId,
                id = listOf(-99),
                firstManagerId = listOf(reviewFrom.toInt()),
                secondManagerId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            )
        val secondManagerEmployeeList =
            employeeService.getEmployeeManagerList(
                organisationId = organisationId,
                id = listOf(-99),
                firstManagerId = listOf(-99),
                secondManagerId = listOf(reviewFrom.toInt()),
                page = 1,
                limit = Int.MAX_VALUE,
            )

        val firstManagerEmployees = firstManagerEmployeeList.map { it.getEmployeeNameWithEmployeeId() }
        val secondManagerEmployees = secondManagerEmployeeList.map { it.getEmployeeNameWithEmployeeId() }
        val allEmployees =
            (firstManagerEmployees + secondManagerEmployees).sortedWith(
                compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                    .thenBy { it.substringAfterLast(" ") },
            )
        val receiverEmail = employeeService.getEmployeeById(reviewFrom).emailId
        val formattedEmployeesDetails = allEmployees.joinToString("\n") { "> $it" }
        val message =
            "Congratulations! The manager review for the current review cycle is submitted successfully for " +
                "the below mentioned employees.\n" +
                formattedEmployeesDetails +
                "\nPlease click on the below-mentioned link to view the submitted manager review -\n" +
                "                                                                                                                 " +
                "<$reviewForTeamMembersURL|*View Manager Review*>\n" +
                "If you need any assistance or clarification, please feel free to reach out to HR."

        val layoutBlock = slackService.blockBuilder(message)
        val slackUser = slackService.getUserByEmailId(receiverEmail, organisationId)
        if (slackUser != null) {
            slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
        }
    }

    fun managerReviewSubmittedToEmployee(
        reviewFrom: Long?,
        reviewTo: Long?,
        organisationId: Long,
    ) {
        var managerInfo = "Manager"
        val employeeData =
            employeeService
                .getEmployeeManagerList(
                    organisationId = organisationId,
                    id = listOf(reviewTo!!.toInt()),
                    firstManagerId = listOf(-99),
                    secondManagerId = listOf(-99),
                    page = 1,
                    limit = Int.MAX_VALUE,
                ).first()
        when (reviewFrom) {
            employeeData.firstManagerId ->
                managerInfo = "Manager 1 (${employeeData.firstManagerFirstName} ${employeeData.firstManagerLastName} - " +
                    "${employeeData.firstManagerEmployeeId})"

            employeeData.secondManagerId ->
                managerInfo = "Manager 2 (${employeeData.secondManagerFirstName} ${employeeData.secondManagerLastName} - " +
                    "${employeeData.secondManagerEmployeeId})"
        }
        val message =
            "Congratulations! Your $managerInfo Review for the current review cycle is submitted successfully.\n" +
                "Please click on the below-mentioned link to view the submitted manager review -\n" +
                "                                                                                                                 " +
                "<$myManagerReviewListingURL|*View Manager Review*>\n" +
                "If you need any assistance or clarification, please feel free to reach out to HR."

        val layoutBlock = slackService.blockBuilder(message)
        val slackUser = slackService.getUserByEmailId(employeeData.emailId, organisationId)
        if (slackUser != null) {
            slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
        }
    }

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
    private val myManagerReviewListingURL = "$instanceUrl/performance-review/manager-review"
    private val reviewForTeamMembersURL = "$instanceUrl/performance-review/team's-review"
}
