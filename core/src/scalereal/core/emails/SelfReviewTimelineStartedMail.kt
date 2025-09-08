package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationService
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class SelfReviewTimelineStartedMail(
    private val employeeService: EmployeeService,
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    @Inject private var appConfig: AppConfig,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun sendMail(
        startDate: Date,
        endDate: Date,
        organisationId: Long,
    ) {
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val employeesEmail: List<String> = employeeService.getActiveEmployees(organisationId).map { it.emailId }
        employeesEmail.map {
            emailSenderService.sendEmail(
                receiver = it,
                subject =
                    selfReviewStartedSubject(
                        startDate.toLocalDate().format(shortMonthDateFormatter),
                        endDate.toLocalDate().format(shortMonthDateFormatter),
                    ),
                htmlBody = selfReviewStartedHTML(endDate, organisationService.fetchName(organisationId)),
                textBody = selfReviewStartedTEXT(),
            )
        }
    }

    fun selfReviewStartedSubject(
        startDate: String,
        endDate: String,
    ) = "Self Review window has started ($startDate to $endDate)"

    fun selfReviewStartedHTML(
        endDate: Date,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>This is your friendly reminder that Self review has started and will end on " +
        "<strong>${endDate.toLocalDate().format(fullMonthDateFormatter)}</strong>.</P>" +
        "<P style = 'color:black;'>We wanted to share some tips to help you get the most out of this process-" +
        "<ul style = 'padding-left:10px; color:black;' >" +
        "<li>Think back on the previous review cycle and note any accomplishments or achievements you’re proud of.</li>" +
        "<li>When you can, try to be as specific as possible, using numbers and figures to back up your statements.</li>" +
        "<li>Think of your personal goals for your career development and how they may align with your company’s goals and values.</li>" +
        "<li>Prepare any questions you might have for your manager.</li>" +
        "<li>Be ready to lead the conversation. This is your performance review, after all!</li>" +
        "</ul>" +
        "</P>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete your self review -</P>" +
        "<a href='$reviewTimelineURL'>Start Self Review</a>" +
        "<P style = 'color:black;'>If you have any questions or there's anything at all you're unclear about, " +
        "don't hesitate to reach out to your manager, or you can reach out to the HR directly.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun selfReviewStartedTEXT() = "Self Review Timeline Started"

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
}
