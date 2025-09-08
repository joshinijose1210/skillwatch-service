package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationService
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class CheckInWithManagerStartedMail(
    private val employeeService: EmployeeService,
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    @Inject private var appConfig: AppConfig,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun sendMailToManager(
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
                    checkInReviewStartedSubject(
                        startDate.toLocalDate().format(shortMonthDateFormatter),
                        endDate.toLocalDate().format(shortMonthDateFormatter),
                    ),
                htmlBody = checkInReviewToManagerHTML(endDate, organisationService.fetchName(organisationId)),
                textBody = checkInReviewStartedTEXT(),
            )
        }
    }

    fun sendMailToEmployee(
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
                    checkInReviewStartedSubject(
                        startDate.toLocalDate().format(shortMonthDateFormatter),
                        endDate.toLocalDate().format(shortMonthDateFormatter),
                    ),
                htmlBody = checkInReviewToEmployeeHTML(endDate, organisationService.fetchName(organisationId)),
                textBody = checkInReviewStartedTEXT(),
            )
        }
    }

    fun checkInReviewStartedSubject(
        startDate: String,
        endDate: String,
    ) = "Check-in with Manager window has started ($startDate to $endDate)"

    fun checkInReviewToManagerHTML(
        endDate: Date,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>" +
        "This is your friendly reminder that the Check-in with Manager review has started and will end on " +
        "<strong>${endDate.toLocalDate().format(fullMonthDateFormatter)}</strong>.</P>" +
        "<P style = 'color:black;'>We wanted to share some tips to help you get the most out of this process-</P>" +
        "<ul style = 'padding-left:10px; color:black;' >" +
        "<li style = 'color:black;'>Set Objectives.</li>" +
        "<li style = 'color:black;'>Set clear expectations.</li>" +
        "<li style = 'color:black;'>Define the key performance assessment indicators.</li>" +
        "<li style = 'color:black;'>Notify employees so they can prepare for the review.</li>" +
        "<li style = 'color:black;'>Structure the performance review conversation.</li>" +
        "<li style = 'color:black;'>Conclude the meeting by outlining actions to be taken.</li>" +
        "</ul>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete the check-in with manager -</P>" +
        "<a href='$reviewTimelineURL'>Start Check-in with Manager</a>" +
        "<P style = 'color:black;'>If you need any guidance or have any questions, feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    fun checkInReviewToEmployeeHTML(
        endDate: Date,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>" +
        "This is your friendly reminder that the Check-in with Manager review has started and will end on " +
        "<strong>${endDate.toLocalDate().format(fullMonthDateFormatter)}</strong>.</P>" +
        "<P style = 'color:black;'>We wanted to share some tips to help you get the most out of this process-</P>" +
        "<ul style = 'padding-left:10px; color:black;' >" +
        "<li style = 'color:black;'>Make time and space for performance reviews.</li>" +
        "<li style = 'color:black;'>Reflect on the past but focus on the future.</li>" +
        "<li style = 'color:black;'>Choose your phrases carefully.</li>" +
        "<li style = 'color:black;'>Be an active listener.</li>" +
        "<li style = 'color:black;'>Wrap up the conversation with agreed upon next steps.</li>" +
        "</ul>" +
        "<P style = 'color:black;'>If you need any guidance or have any questions, feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun checkInReviewStartedTEXT() = "Check-in With Manager Review Timeline Started"

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
}
