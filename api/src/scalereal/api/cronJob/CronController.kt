package scalereal.api.cronJob

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Singleton
import scalereal.api.common.ErrorMessage
import scalereal.api.common.Response
import scalereal.api.common.ResponseType
import scalereal.core.cronJob.CronJobService
import java.lang.Exception

@Tag(name = "Cron Jobs")
@Controller(value = "/api/cron")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
class CronController(
    private val cronJobService: CronJobService,
) {
    @Operation(summary = "Send review cycle notifications")
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/notification/")
    fun sendMail(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = cronJobService.taskScheduled(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Unpublish past review cycle")
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/unpublish-review-cycle/")
    fun unPublishReviewCycle(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = cronJobService.unPublishReviewCycle(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Increase employee experience")
    @Get("/increase-employee-experience/")
    fun increaseEmployeesExperience(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = cronJobService.increaseEmployeesExperience(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Send notification for goals with past deadline")
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/goal-completed/")
    fun sendNotification(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = cronJobService.sendGoalDeadlineNotifications(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Send feedback request reminder notification")
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/feedback-request-reminder/")
    fun sendFeedbackRequestReminder(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = cronJobService.sendFeedbackRequestReminder(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()

    @Operation(summary = "Send bi-weekly feedback reminder notification")
    @Produces(MediaType.APPLICATION_JSON)
    @Get("/feedback/reminder/bi-weekly")
    fun sendBiWeeklyFeedbackReminder(): HttpResponse<Any> =
        try {
            Response(
                ResponseType.SUCCESS,
                "",
                body = cronJobService.sendBiWeeklyFeedbackReminder(),
            )
        } catch (e: Exception) {
            Response(
                ResponseType.NOT_FOUND,
                body = ErrorMessage(e.message.toString()),
            )
        }.getHttpResponse()
}
