package scalereal.core.models

enum class FeedbackReminder(
    val message: String,
) {
    OPTION_1(
        """
        <!channel> *Heads-up! Your 1:1s for this month is due.*
        Take a few minutes to:
        :bulb: Share praise.
        :mag: Drop a helpful tip.
        :sparkling_heart: Leave an appreciation note.
        Start your 1:1s with kindness and clarity!
        """.trimIndent(),
    ),
    OPTION_2(
        """
        <!channel> *Ping ping! Your 1:1 feedback moment is here!*
        It's your reminder to:
        :bulb: Exchange thoughts
        :sparkles: Give and receive valuable feedback
        :busts_in_silhouette: Discuss about improvement pointers
        Great teams talk often—make it count!
        """.trimIndent(),
    ),
    OPTION_3(
        """
        <!channel> *Let’s Catch Up! It’s time for your 1:1s!*
        Don’t forget to:
        :heavy_check_mark: Shout out great work
        :compass: Offer a helpful suggestion
        :speech_balloon: Drop a thoughtful appreciation
        Take a few minutes to share feedback, not just tasks!
        """.trimIndent(),
    ),
    OPTION_4(
        """
        <!channel> *Quick Nudge: Schedule monthly 1:1s with your teammates!*
        Use this chance to talk about:
        :white_check_mark:  What went well
        :wrench: What could be better
        :heart: Something they should be proud of
        Talk, share, laugh, and grow — one sync-up at a time
        """.trimIndent(),
    ),
    OPTION_5(
        """
        <!channel> *Your Bi-weekly reminder for Roses, Thorns, and Buds!*
        Block your 15-20 mins to share:
        :rose: Roses – What’s going well, wins, positives
        :dagger_knife: Thorns – Challenges or blockers
        :seedling: Buds – Opportunities, ideas, or things you’re excited about
        Let’s keep the good vibes (and feedback) flowing!
        """.trimIndent(),
    ),
}
