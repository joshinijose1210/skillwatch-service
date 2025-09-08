package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationService
import scalereal.core.reviewCycle.ReviewCycleService
import java.time.format.DateTimeFormatter

@Singleton
class CheckInWithManagerCompletedMail(
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    private val reviewCycleService: ReviewCycleService,
    private val employeeService: EmployeeService,
    @Inject private var appConfig: AppConfig,
) {
    fun sendMailToEmployee(
        reviewTo: Long,
        reviewCycleId: Long,
        organisationId: Long,
    ) {
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val employeeEmail = employeeService.getEmployeeById(reviewTo).emailId

        emailSenderService.sendEmail(
            receiver = employeeEmail,
            subject =
                checkInCompletedSubject(
                    reviewCycle.startDate.toLocalDate().format(shortMonthDateFormatter),
                    reviewCycle.endDate.toLocalDate().format(shortMonthDateFormatter),
                ),
            htmlBody = checkInCompletedToEmployeeHTML(organisationService.fetchName(organisationId)),
            textBody = checkInCompletedTEXT(),
        )
    }

    fun sendMailToManager(
        reviewFrom: Long,
        reviewCycleId: Long,
        organisationId: Long,
    ) {
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
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

        emailSenderService.sendEmail(
            receiver = receiverEmail,
            subject =
                checkInCompletedSubject(
                    reviewCycle.startDate.toLocalDate().format(shortMonthDateFormatter),
                    reviewCycle.endDate.toLocalDate().format(shortMonthDateFormatter),
                ),
            htmlBody = checkInCompletedToManagerHTML(allEmployees, organisationService.fetchName(organisationId)),
            textBody = checkInCompletedTEXT(),
        )
    }

    fun checkInCompletedSubject(
        startDate: String,
        endDate: String,
    ) = "Check-in with Manager completed for Review Cycle ($startDate to $endDate)"

    fun checkInCompletedToEmployeeHTML(orgName: String) =
        "<!DOCTYPE html>" +
            "<html>" +
            "<body>" +
            "<P style = 'color:black;'>Hi there,</P>" +
            "<P style = 'color:black;'>" +
            "Congratulations! Your Check-in with Manager for the current review cycle is completed successfully." +
            "</P>" +
            "<P style = 'color:black;'>Please click on the below mentioned link to view your check-in with manager -</P>" +
            "<a href='$myCheckInWithManagersURL'>View Check-in with Manager</a>" +
            "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
            "<P style = 'color:black;'>Thank you & Regards,<br>" +
            "$orgName</P>" +
            "</body>" +
            "</html>"

    fun checkInCompletedToManagerHTML(
        employees: List<String>,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>" +
        "Congratulations! The Check-in with Manager for the current review cycle is completed successfully for the below " +
        "mentioned employees.</P>" +
        "<P style = 'padding-left:10px; color:black;'>" +
        "<ul style = 'color:black;'>" +
        employees.map { "<li>$it</li>" }.joinToString(separator = "\n") +
        "</ul>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to view the submitted check-in with manager -</P>" +
        "<a href='$checkInWithTeamMembersURL'>View Check-in with Manager</a>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun checkInCompletedTEXT() = "Check-in with manager completed successfully"

    private val instanceUrl = appConfig.getInstanceUrl()

    private val myCheckInWithManagersURL = "$instanceUrl/performance-review/manager-check-in/1"
    private val checkInWithTeamMembersURL = "$instanceUrl/performance-review/team&apos;s-check-in/1"
}
