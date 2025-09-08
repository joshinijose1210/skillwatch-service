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
class CheckInSlackNotifications(
    private val employeeService: EmployeeService,
    private val slackService: SlackService,
    private val selfReviewRepository: SelfReviewRepository,
    @Inject private var appConfig: AppConfig,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    private val instanceUrl = appConfig.getInstanceUrl()

    fun checkInStartedToManager(
        checkInEndDate: Date,
        organisationId: Long,
    ) {
        val tips =
            "Set Objectives.\n" +
                "Set clear expectations.\n" +
                "Define the key performance assessment indicators.\n" +
                "Notify employees so they can prepare for the review.\n" +
                "Structure the performance review conversation.\n" +
                "Conclude the meeting by outlining actions to be taken."
        val formattedTips = tips.lines().joinToString("\n") { "> $it" }
        val message =
            "This is your friendly reminder that the Check in with Manager has started and will end on " +
                "${checkInEndDate.toLocalDate().format(fullMonthDateFormatter)}.\n" +
                "We wanted to share some tips to help you get the most out of this process -\n" +
                formattedTips +
                "\nPlease click on the below-mentioned link to complete the check-in with manager -\n" +
                "                                                                                                           " +
                "<$reviewTimelineURL|*Start Check-in with Manager*>\n" +
                "If you need any guidance or have any questions, feel free to reach out to the HR."

        val layoutBlock = slackService.blockBuilder(message)
        val managersEmail: List<String> = employeeService.getManagers(organisationId).map { it.emailId }
        managersEmail.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun checkInFiveDayReminderToManager(
        managers: List<Employee>,
        managerReviewEndDate: Date,
        reviewCycleId: Long,
    ) {
        managers.map { manager ->
            val employees =
                selfReviewRepository.fetchInCompleteCheckInWithManagerEmployees(
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
                "Just sending you a friendly reminder to please complete the check-in with manager for the below-mentioned employees by " +
                    "${managerReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}." +
                    " *(5 days left for the Check-in with Manager to be completed)*\n" +
                    formattedEmployeesDetails +
                    "\nPlease click on the below-mentioned link to complete the check-in with the manager -\n" +
                    "                                                                                                       " +
                    "<$reviewTimelineURL|*Start Check-in with Manager*>\n" +
                    "If you need any assistance or clarification, please feel free to reach out to the HR."

            val layoutBlock = slackService.blockBuilder(message)
            if (employees.isNotEmpty()) {
                val slackUser = slackService.getUserByEmailId(manager.emailId, manager.organisationId)
                if (slackUser != null) {
                    slackService.sendBlockMessageToUser(manager.organisationId, layoutBlock, slackUser.id, message)
                }
            }
        }
    }

    fun checkInLastDayReminderToManager(
        managers: List<Employee>,
        reviewCycleId: Long,
    ) {
        managers.map { manager ->
            val employees =
                selfReviewRepository.fetchInCompleteCheckInWithManagerEmployees(
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
                "Just sending you a friendly reminder to please complete the check-in with manager for the below-mentioned " +
                    "employees by *today*. *(Last day reminder to complete the Check-in with Manager Review)*\n" +
                    formattedEmployeesDetails +
                    "\nBy now, you should have already submitted the check-in with manager for the current performance review. " +
                    "If you haven’t, please do so immediately.\n" +
                    "Please click on the below-mentioned link to complete the check-in with manager -\n" +
                    "                                                                                                       " +
                    "<$reviewTimelineURL|*Start Check-in with Manager*>\n" +
                    "If you need any assistance or clarification, please feel free to reach out to the HR."

            val layoutBlock = slackService.blockBuilder(message)
            if (employees.isNotEmpty()) {
                val slackUser = slackService.getUserByEmailId(manager.emailId, manager.organisationId)
                if (slackUser != null) {
                    slackService.sendBlockMessageToUser(manager.organisationId, layoutBlock, slackUser.id, message)
                }
            }
        }
    }

    fun checkInSubmittedToManager(
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
            "Congratulations! The Check-in with Manager for the current review cycle is completed successfully for " +
                "the below-mentioned employees.\n" +
                formattedEmployeesDetails +
                "\nTo view your submitted Check-in with Manager click on the link -\n" +
                "                                                                                                           " +
                "<$checkInWithTeamMembersURL|*View Check-in with Manager*>\n" +
                "If you need any assistance or clarification, please feel free to reach out to the HR."

        val layoutBlock = slackService.blockBuilder(message)
        val slackUser = slackService.getUserByEmailId(receiverEmail, organisationId)
        if (slackUser != null) {
            slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
        }
    }

    fun checkInStartedToEmployee(
        checkInEndDate: Date,
        organisationId: Long,
    ) {
        val tips =
            "Make time and space for performance reviews.\n" +
                "Reflect on the past but focus on the future.\n" +
                "Choose your phrases carefully.\n" +
                "Be an active listener.\n" +
                "Wrap up the conversation with agreed upon next steps."
        val formattedTips = tips.lines().joinToString("\n") { "> $it" }
        val message =
            "This is your friendly reminder that the Check in with Manager has started and will end on " +
                "${checkInEndDate.toLocalDate().format(fullMonthDateFormatter)}.\n" +
                "We wanted to share some tips to help you get the most out of this process -\n" +
                formattedTips +
                "\nIf you need any guidance or have any questions, feel free to reach out to the HR."

        val layoutBlock = slackService.blockBuilder(message)
        val employeesEmail: List<String> = employeeService.getActiveEmployees(organisationId).map { it.emailId }
        employeesEmail.map { emailId ->
            val slackUser = slackService.getUserByEmailId(emailId, organisationId)
            if (slackUser != null) {
                slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
            }
        }
    }

    fun checkInFiveDayReminderToEmployee(
        managers: List<Employee>,
        checkInEndDate: Date,
        reviewCycleId: Long,
    ) {
        val employeeHashSet = HashSet<Employee>()
        managers.map { manager ->
            val employees =
                selfReviewRepository.fetchInCompleteCheckInWithManagerEmployees(
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            if (employees.isNotEmpty()) {
                employees.forEach { employee ->
                    if (!employeeHashSet.contains(employee)) {
                        val pointer =
                            "Performance review is an opportunity for you to show how well you've performed during the period in " +
                                "review and receive feedback." +
                                " Don't leave this for later, go ahead and remind the manager to complete the check-in with manager."
                        val formattedPointer = pointer.lines().joinToString("\n") { "> $it" }
                        val message =
                            "Just sending you a friendly reminder that your check in with manager timeline will end on " +
                                "${checkInEndDate.toLocalDate().format(fullMonthDateFormatter)}." +
                                " *(5 days left for the Check-in with Manager to be completed)*\n" +
                                formattedPointer +
                                "\nIf you need any assistance or clarification, please feel free to reach out to the HR."

                        val layoutBlock = slackService.blockBuilder(message)
                        val slackUser = slackService.getUserByEmailId(employee.emailId, employee.organisationId)
                        if (slackUser != null) {
                            slackService.sendBlockMessageToUser(employee.organisationId, layoutBlock, slackUser.id, message)
                        }
                        employeeHashSet.add(employee)
                    }
                }
            }
        }
    }

    fun checkInLastDayReminderToEmployee(
        managers: List<Employee>,
        reviewCycleId: Long,
        startDate: Date,
        endDate: Date,
    ) {
        val employeeHashSet = HashSet<Employee>()
        managers.map { manager ->
            val employees =
                selfReviewRepository.fetchInCompleteCheckInWithManagerEmployees(
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            if (employees.isNotEmpty()) {
                employees.forEach { employee ->
                    if (!employeeHashSet.contains(employee)) {
                        val pointer =
                            "Remember, it is important that your check-in with manager is completed on time. " +
                                "If not, please remind the manager or the HR to do so."
                        val formattedPointer = pointer.lines().joinToString("\n") { "> $it" }
                        val message =
                            "Just sending you a friendly reminder that *today* is the last date to complete your check-in with " +
                                "manager for the current review cycle " +
                                "(${startDate.toLocalDate().format(fullMonthDateFormatter)} to " +
                                "${endDate.toLocalDate().format(fullMonthDateFormatter)})." +
                                " *(Last day reminder to complete the Check-in with Manager Review)*\n" +
                                formattedPointer +
                                "\nIf you need any assistance or clarification, please feel free to reach out to the HR."

                        val layoutBlock = slackService.blockBuilder(message)
                        val slackUser = slackService.getUserByEmailId(employee.emailId, employee.organisationId)
                        if (slackUser != null) {
                            slackService.sendBlockMessageToUser(employee.organisationId, layoutBlock, slackUser.id, message)
                        }
                        employeeHashSet.add(employee)
                    }
                }
            }
        }
    }

    fun checkInSubmittedToEmployee(
        reviewTo: Long?,
        organisationId: Long,
    ) {
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
        val message =
            "Congratulations! Your Check-in with Manager for the current review cycle is completed successfully.\n" +
                "Please click on the below-mentioned link to view your check-in with manager -\n" +
                "                                                                                                           " +
                "<$myCheckInWithManagersURL|*View Check-in with Manager*>\n" +
                "If you need any assistance or clarification, please feel free to reach out to the HR."

        val layoutBlock = slackService.blockBuilder(message)
        val slackUser = slackService.getUserByEmailId(employeeData.emailId, organisationId)
        if (slackUser != null) {
            slackService.sendBlockMessageToUser(organisationId, layoutBlock, slackUser.id, message)
        }
    }

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
    private val myCheckInWithManagersURL = "$instanceUrl/performance-review/manager-check-in/1"
    private val checkInWithTeamMembersURL = "$instanceUrl/performance-review/team's-check-in/1"
}
