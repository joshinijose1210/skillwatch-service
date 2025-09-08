package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.Employee
import scalereal.core.organisations.OrganisationService
import scalereal.core.review.SelfReviewService
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class CheckInWithManagerReminderEmail(
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    private val reviewService: SelfReviewService,
    @Inject private var appConfig: AppConfig,
) {
    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()

    fun sendFiveDayReminderEmailToManager(
        managers: List<Employee>,
        endDate: Date,
        reviewCycleId: Long,
    ) {
        val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        managers.map { manager ->
            val employees =
                reviewService.fetchInCompleteCheckInWithManagerEmployees(
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            val employeesDetails =
                employees.map { it.getEmployeeNameWithEmployeeId() }.sortedWith(
                    compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                        .thenBy { it.substringAfterLast(" ") },
                )
            if (employees.isNotEmpty()) {
                emailSenderService.sendEmail(
                    receiver = manager.emailId,
                    subject = "Only 5 days left to complete Check-in with Member for your team",
                    htmlBody =
                        fiveDaysReminderToManagerHTML(
                            employeesDetails,
                            endDate.toLocalDate().format(fullMonthDateFormatter),
                            organisationService.fetchName(manager.organisationId),
                        ),
                    textBody = checkInWithManagerReminderTEXT(),
                )
            }
        }
    }

    fun sendFiveDayReminderEmailToEmployee(
        managers: List<Employee>,
        endDate: Date,
        reviewCycleId: Long,
    ) {
        val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val employeeHashSet = HashSet<Employee>()
        managers.map { manager ->
            val employees =
                reviewService.fetchInCompleteCheckInWithManagerEmployees(
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            if (employees.isNotEmpty()) {
                employees.forEach { employee ->
                    if (!employeeHashSet.contains(employee)) {
                        emailSenderService.sendEmail(
                            receiver = employee.emailId,
                            subject = "Only 5 Days Left to Complete the Check-in with Manager",
                            htmlBody =
                                fiveDaysReminderToEmployeeHTML(
                                    endDate.toLocalDate().format(fullMonthDateFormatter),
                                    organisationService.fetchName(employee.organisationId),
                                ),
                            textBody = checkInWithManagerReminderTEXT(),
                        )
                        employeeHashSet.add(employee)
                    }
                }
            }
        }
    }

    fun sendLastDayReminderEmailToManager(
        managers: List<Employee>,
        reviewCycleId: Long,
    ) {
        managers.map { manager ->
            val employees =
                reviewService.fetchInCompleteCheckInWithManagerEmployees(
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            val employeesDetails =
                employees.map { it.getEmployeeNameWithEmployeeId() }.sortedWith(
                    compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                        .thenBy { it.substringAfterLast(" ") },
                )
            if (employees.isNotEmpty()) {
                emailSenderService.sendEmail(
                    receiver = manager.emailId,
                    subject = "Last day to complete Check-in with Member for your team",
                    htmlBody =
                        lastDayReminderToManagerHTML(
                            employeesDetails,
                            organisationService.fetchName(manager.organisationId),
                        ),
                    textBody = checkInWithManagerReminderTEXT(),
                )
            }
        }
    }

    fun sendLastDayReminderEmailToEmployee(
        managers: List<Employee>,
        reviewCycleId: Long,
        startDate: Date,
        endDate: Date,
    ) {
        val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val employeeHashSet = HashSet<Employee>()
        managers.map { manager ->
            val employees =
                reviewService.fetchInCompleteCheckInWithManagerEmployees(
                    reviewCycleId = reviewCycleId,
                    managerId = manager.id,
                )
            if (employees.isNotEmpty()) {
                employees.forEach { employee ->
                    if (!employeeHashSet.contains(employee)) {
                        emailSenderService.sendEmail(
                            receiver = employee.emailId,
                            subject = "Last day to Complete the Check-in with Manager",
                            htmlBody =
                                lastDayReminderToEmployeeHTML(
                                    startDate.toLocalDate().format(fullMonthDateFormatter),
                                    endDate.toLocalDate().format(fullMonthDateFormatter),
                                    organisationService.fetchName(employee.organisationId),
                                ),
                            textBody = checkInWithManagerReminderTEXT(),
                        )
                        employeeHashSet.add(employee)
                    }
                }
            }
        }
    }

    fun fiveDaysReminderToManagerHTML(
        employees: List<String>,
        endDate: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>Just sending you a friendly reminder to please complete the check-in with manager " +
        "for the below mentioned employees by <strong>$endDate</strong>.</P>" +
        "<P style = 'padding-left:10px; color:black;'>" +
        "<ul style = 'color:black;'>" +
        employees.map { "<li>$it</li>" }.joinToString(separator = "\n") +
        "</ul></P>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete the check-in with manager -</P>" +
        "<a href='$reviewTimelineURL'>Start Check-in with Team Member</a>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    fun fiveDaysReminderToEmployeeHTML(
        endDate: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>Just sending you a friendly reminder that your check-in with manager " +
        "timeline will end on $endDate.</P>" +
        "<P style = 'color:black;'>Performance review is an opportunity for you to show how well you've performed" +
        " during the period in review, receive feedback and even compensation. Don't leave this for later," +
        " go ahead and remind the manager to complete the check in with manager.</P>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    fun lastDayReminderToManagerHTML(
        employees: List<String>,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>Just sending you a friendly reminder to please complete the check-in with manager" +
        " for the below mentioned employees by <strong>today</strong> - </P>" +
        "<P style = 'padding-left:10px; color:black;'>" +
        "<ul style = 'color:black;'>" +
        employees.map { "<li>$it</li>" }.joinToString(separator = "\n") +
        "</ul></P>" +
        "<P style = 'color:black;'>By now, you should have already submitted the manager review for the current performance review." +
        " If you haven’t, please do so immediately.</P>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete the check-in with manager -</P>" +
        "<a href='$reviewTimelineURL'>Start Check-in with Team Member</a>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    fun lastDayReminderToEmployeeHTML(
        startDate: String,
        endDate: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>Just sending you a friendly reminder that <strong>today</strong> " +
        "is the last date to complete your check-in with manager for the current review cycle ($startDate to $endDate).</P>" +
        "<P style = 'color:black;'>Remember, it is important that your check-in with manager completed and submitted on time." +
        " If not, please remind the manager or the HR to do so.</p>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun checkInWithManagerReminderTEXT() = "Submit Check-in with Manager"
}
