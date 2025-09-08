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
class ManagerReviewReminderMail(
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    private val reviewService: SelfReviewService,
    @Inject private var appConfig: AppConfig,
) {
    fun sendFiveDayReminderEmail(
        managers: List<Employee>,
        endDate: Date,
        reviewCycleId: Long,
    ) {
        val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        managers.map {
            val employees =
                reviewService.fetchInCompleteManagerReviewEmployees(
                    organisationId = it.organisationId,
                    reviewCycleId = reviewCycleId,
                    managerId = it.id,
                )
            val employeesDetails =
                employees.map { it.getEmployeeNameWithEmployeeId() }.sortedWith(
                    compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                        .thenBy { it.substringAfterLast(" ") },
                )
            if (employees.isNotEmpty()) {
                emailSenderService.sendEmail(
                    receiver = it.emailId,
                    subject = "Only 5 Days Left to Complete the Manager Review",
                    htmlBody =
                        fiveDaysReminderHTML(
                            employeesDetails,
                            endDate.toLocalDate().format(fullMonthDateFormatter),
                            organisationService.fetchName(it.organisationId),
                        ),
                    textBody = managerReviewReminderTEXT(),
                )
            }
        }
    }

    fun sendLastDayReminderEmail(
        managers: List<Employee>,
        reviewCycleId: Long,
    ) {
        managers.map {
            val employees =
                reviewService.fetchInCompleteManagerReviewEmployees(
                    organisationId = it.organisationId,
                    reviewCycleId = reviewCycleId,
                    managerId = it.id,
                )
            val employeesDetails =
                employees.map { it.getEmployeeNameWithEmployeeId() }.sortedWith(
                    compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                        .thenBy { it.substringAfterLast(" ") },
                )
            if (employees.isNotEmpty()) {
                emailSenderService.sendEmail(
                    receiver = it.emailId,
                    subject = "Last day to Complete Your Manager Review",
                    htmlBody =
                        lastDayReminderHTML(
                            employeesDetails,
                            organisationService.fetchName(it.organisationId),
                        ),
                    textBody = managerReviewReminderTEXT(),
                )
            }
        }
    }

    fun fiveDaysReminderHTML(
        employees: List<String>,
        endDate: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>" +
        "Just sending you a friendly reminder to please complete the manager review for the below mentioned employees by " +
        "<strong>$endDate</strong>.</P>" +
        "<P style = 'padding-left:10px; color:black;'>" +
        "<ul style = 'color:black;'>" +
        employees.map { "<li>$it</li>" }.joinToString(separator = "\n") +
        "</ul></P>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete the manager review -</P>" +
        "<a href='$reviewTimelineURL'>Start Manager Review</a>" +
        "<P style = 'color:black;'>" +
        "Performance reviews are a great opportunity for you to learn how much the employee has grown in their role.<br>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    fun lastDayReminderHTML(
        employees: List<String>,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>" +
        "Just sending you a friendly reminder to please complete the manager reviews for the below mentioned employees by " +
        "<strong>today</strong>." +
        "</P>" +
        "<P style = 'padding-left:10px; color:black;'>" +
        "<ul style = 'color:black;'>" +
        employees.map { "<li>$it</li>" }.joinToString(separator = "\n") +
        "</ul></P>" +
        "<P style = 'color:black;'>" +
        "By now, you should have already submitted the manager review for the current performance review. " +
        "If you haven’t, please do so immediately.</P>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete the manager review -</P>" +
        "<a href='$reviewTimelineURL'>Start Manager Review</a>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun managerReviewReminderTEXT() = "Submit Manager Review"

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
}
