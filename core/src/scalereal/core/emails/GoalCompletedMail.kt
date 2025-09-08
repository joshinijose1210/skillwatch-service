package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.GoalBasicDetails
import scalereal.core.organisations.OrganisationService

@Singleton
class GoalCompletedMail(
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    private val employeeService: EmployeeService,
    appConfig: AppConfig,
) {
    fun sendMail(goal: GoalBasicDetails) {
        val assignedTo = employeeService.getEmployeeById(goal.assignedTo)
        val createdBy = employeeService.getEmployeeById(goal.createdBy)
        val createdByName = "${createdBy.firstName} ${createdBy.lastName} (${createdBy.employeeId})"
        emailSenderService.sendEmail(
            receiver = assignedTo.emailId,
            subject = "Deadline of Goal has been completed",
            htmlBody =
                goalCompletedHTML(
                    createdBy = createdByName,
                    assignedTo = assignedTo.firstName,
                    orgName = organisationService.fetchName(assignedTo.organisationId),
                    goalId = goal.goalId,
                ),
            textBody = goalCompletedTEXT(),
        )
    }

    private fun goalCompletedHTML(
        createdBy: String,
        assignedTo: String,
        orgName: String,
        goalId: String?,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi $assignedTo,</P>" +
        "<P style = 'color:black;'>" +
        "We want to inform you that the deadline for the Goal ($goalId) provided by $createdBy has been completed. " +
        "Kindly click on the below-mentioned link to view goals on the dashboard -</P>" +
        "<a href='$dashboardURL'>View Goals</a>" +
        "<P style = 'color:black;'>You can also click on the below-mentioned link to request feedback for the Goals -</P>" +
        "<a href='$requestFeedbackURL'>Request Feedback</a>" +
        "<P style = 'color:black;'>" +
        "If you have any questions or there's anything at all you're unclear about, don't hesitate to reach out to HR directly." +
        "</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun goalCompletedTEXT() = "Goal Completed"

    private val instanceUrl = appConfig.getInstanceUrl()

    private val dashboardURL = "$instanceUrl/"

    private val requestFeedbackURL = appConfig.getRequestFeedbackUrl()
}
