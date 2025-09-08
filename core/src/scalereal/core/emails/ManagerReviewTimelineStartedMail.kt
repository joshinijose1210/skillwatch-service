package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationService
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class ManagerReviewTimelineStartedMail(
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
        val managersEmail: List<String> = employeeService.getManagers(organisationId).map { it.emailId }
        managersEmail.map {
            emailSenderService.sendEmail(
                receiver = it,
                subject =
                    managerReviewStartedSubject(
                        startDate.toLocalDate().format(shortMonthDateFormatter),
                        endDate.toLocalDate().format(shortMonthDateFormatter),
                    ),
                htmlBody = managerReviewStartedHTML(endDate, organisationService.fetchName(organisationId)),
                textBody = managerReviewStartedTEXT(),
            )
        }
    }

    fun managerReviewStartedSubject(
        startDate: String,
        endDate: String,
    ) = "Manager Review window has started ($startDate to $endDate)"

    fun managerReviewStartedHTML(
        endDate: Date,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>" +
        "This is your friendly reminder that the Manager review has started and will end on " +
        "<strong>${endDate.toLocalDate().format(fullMonthDateFormatter)}</strong>.</P>" +
        "<P style = 'color:black;'>We wanted to share some tips to help you get the most out of this process-" +
        "<ul style = 'padding-left:10px; color:black;' >" +
        "<li>Create and review expectations, standards, and rules.</li>" +
        "<li>Educate employees about any behaviours they need improvement or modification.</li>" +
        "<li>Identify any strengths and weaknesses that weren’t already known.</li>" +
        "<li>Learn more about the employee.</li>" +
        "<li>Give specific, actionable feedback.</li>" +
        "<li>Plan for the future during performance review.</li>" +
        "</ul>" +
        "</P>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete the manager review -</P>" +
        "<a href='$reviewTimelineURL'>Start Manager Review</a>" +
        "<P style = 'color:black;'>If you need any guidance or have any questions, feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun managerReviewStartedTEXT() = "Manager Review Timeline Started"

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
}
