package scalereal.core.models

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton

@Singleton
class AppConfig(
    @Value("\${APP_URL}") private val instanceUrl: String,
    @Value("\${S3_BUCKET_LOGO_URL}") private val instanceS3BucketUrl: String,
) {
    fun getInstanceUrl(): String = instanceUrl

    fun getS3BucketUrl(): String = instanceS3BucketUrl

    fun getRequestFeedbackUrl(): String = "$instanceUrl/360-degree-feedback/1?tab=inbox"

    fun getMyFeedbackUrl(): String = "$instanceUrl/360-degree-feedback/1?tab=received"

    fun getReviewTimelineUrl(): String = "$instanceUrl/performance-review/review-timeline"

    fun getReceivedSuggestionUrl(): String = "$instanceUrl/suggestions/1?tab=receivedSuggestion"

    fun getSubmittedSuggestionUrl(): String = "$instanceUrl/suggestions/1?tab=submittedSuggestion"
}
