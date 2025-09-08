package scalereal.core.emails
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.models.Constants
import scalereal.core.models.SuggestionProgress

@Singleton
class SuggestionMail(
    private val employeeService: EmployeeService,
    private val emailSenderService: EmailSenderService,
    private val employeeRepository: EmployeeRepository,
    appConfig: AppConfig,
) {
    fun sendMail(
        suggestedById: Long,
        suggestion: String,
    ) {
        val organisationId = employeeRepository.getEmployeeById(suggestedById).organisationId
        val employeesEmail = employeeService.getEmployeesWithSuggestionsReceivedPermission(organisationId).map { it.emailId }

        employeesEmail.map {
            emailSenderService.sendEmail(
                receiver = it,
                subject = suggestionAlertMailSubject,
                htmlBody = suggestionAlertMailHTML(suggestion),
                textBody = suggestionAlertMailTEXT(),
            )
        }
    }

    fun sendSuggestionProgressUpdate(
        suggestedById: Long,
        progressId: Int,
        comment: String? = null,
    ) {
        val userEmail = employeeRepository.getEmployeeById(suggestedById).emailId
        val (subject, body) =
            when (progressId) {
                SuggestionProgress.IN_PROGRESS.progressId -> createInProgressEmail(comment)
                SuggestionProgress.COMPLETED.progressId -> createCompletedEmail(comment)
                SuggestionProgress.DEFERRED.progressId -> createDeferredEmail(comment)
                else -> return
            }

        emailSenderService.sendEmail(
            receiver = userEmail,
            subject = subject,
            htmlBody = body,
            textBody = "Suggestion Progress Update",
        )
    }

    private val suggestionAlertMailSubject = "Incoming Suggestion Alert!"

    private fun suggestionAlertMailHTML(suggestionText: String) =
        "<html>" +
            "<head><style> a > div {display:none} </style> </head>" +
            "<body>" +
            "<p style='color:black;'>Hi Team,</p>" +
            "<p style='color:black;'>A new suggestion has been added to the Suggestion Box.</p>" +
            "<blockquote style='color:black;font-style:italic;'>" +
            suggestionText +
            "</blockquote>" +
            "<p style='color:black;'>Take a moment to review their idea and respond if needed.</p>" +
            "<div style='text-align: left; color:black;'>" +
            "<a href='$receivedSuggestionURL'>View Suggestion Link</a>" +
            "</div>" +
            "<p style='color:black;'>Best regards,<br>" +
            "<strong>${Constants.APPLICATION_NAME} Team</strong><br>" +
            "Your Feedback & Performance Partner</p>" +
            "</body>" +
            "</html>"

    private fun suggestionAlertMailTEXT() = "Incoming Suggestion Alert"

    private val receivedSuggestionURL = appConfig.getReceivedSuggestionUrl()

    private val submittedSuggestionURL = appConfig.getSubmittedSuggestionUrl()

    private fun createInProgressEmail(comment: String?): Pair<String, String> {
        val subject = "Update: Your Suggestion is Now in Progress!"
        val body =
            """
            <html>
            <head><style> a > div {display:none} </style></head>
            <body>
            <p style='color:black;'>Hi there,</p>
            <p style='color:black;'>Yay! The suggestion you submitted on SkillWatch is now marked as <strong>"In Progress"</strong>.
             We're actively working on it and will keep you posted!</p>
            ${comment?.let {
                "<p style='color:black;font-style:italic;'>Comment: $it</p>"
            } ?: ""}
            <div style='text-align: left; color:black;'>
            <a href='$submittedSuggestionURL'>View Suggestion Link</a>
            </div>
            <p style='color:black;'>Best regards,<br>
            <strong>${Constants.APPLICATION_NAME} Team</strong><br>
            Your Feedback & Performance Partner</p>
            </body>
            </html>
            """.trimIndent()
        return subject to body
    }

    private fun createCompletedEmail(comment: String?): Pair<String, String> {
        val subject = "Update: Your Suggestion is Now Completed!"
        val body =
            """
            <html>
            <head><style> a > div {display:none} </style></head>
            <body>
            <p style='color:black;'>Hi there,</p>
            <p style='color:black;'>Great news! Your suggestion has been marked as <strong>"Completed"</strong> on SkillWatch.
             Thanks for helping us build a better workplace. Keep the ideas coming!</p>
            ${comment?.let {
                "<p style='color:black;font-style:italic;'>Comment: $it</p>"
            } ?: ""}
            <div style='text-align: left; color:black;'>
                <a href='$submittedSuggestionURL'>View Suggestion Link</a>
            </div>
            <p style='color:black;'>Best regards,<br>
            <strong>${Constants.APPLICATION_NAME} Team</strong><br>
            Your Feedback & Performance Partner</p>
            </body>
            </html>
            """.trimIndent()
        return subject to body
    }

    private fun createDeferredEmail(comment: String?): Pair<String, String> {
        val subject = "Update: Your Suggestion is Deferred for Now!"
        val body =
            """
            <html>
            <head><style> a > div {display:none} </style></head>
            <body>
            <p style='color:black;'>Hi there,</p>
            <p style='color:black;'>Your suggestion on SkillWatch is currently <strong>"Deferred"</strong>. We really appreciate your input and will revisit this later. Thanks for understanding!</p>
            ${comment?.let {
                "<p style='color:black;font-style:italic;'>Comment: $it</p>"
            } ?: ""}
            <div style='text-align: left; color:black;'>
                <a href='$submittedSuggestionURL'>View Suggestion Link</a>
            </div>
            <p style='color:black;'>Best regards,<br>
            <strong>${Constants.APPLICATION_NAME} Team</strong><br>
            Your Feedback & Performance Partner</p>
            </body>
            </html>
            """.trimIndent()
        return subject to body
    }
}
