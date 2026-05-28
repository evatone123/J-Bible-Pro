package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Highlights verse insights / devotional commentary.
     */
    suspend fun getVerseInsight(book: String, chapter: Int, verse: Int, text: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Get deeper insights from our Gemini AI Assistant by entering your API Key in the AI Studio Secrets panel."
        }

        val prompt = """
            Provide a short, elegant, inspiring theological and practical commentary (maximum 3 sentences) for the scripture verse:
            $book $chapter:$verse - "$text".
            Focus on encouragement, historical context, and modern application. Keep it warm and comforting.
        """.trimIndent()

        try {
            val responseText = makeApiCall(apiKey, prompt)
            return@withContext parseResponseText(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch verse insight", e)
            return@withContext "Let us trust in the Lord and read His word daily. (API connection unavailable)"
        }
    }

    /**
     * Generates a structural custom 5-day reading plan JSON for any chosen theme or topic.
     */
    suspend fun generateReadingPlan(topic: String): List<GeneratedDayPlan> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val fallback = getFallbackPlanList(topic)
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallback
        }

        val prompt = """
            Generate an inspiring 5-day Bible Reading Plan on the topic: "$topic".
            Return a strictly valid JSON array of objects representing the days of the plan.
            Every object in the array MUST contain exactly these fields:
            - "day": integer (1 to 5)
            - "passage": string (e.g., "John 1:1-5", "Psalms 23:1-4", "Matthew 5:13-16", "Proverbs 3:5-6")
            - "notes": string (brief description / devotional focus of the day in 1 sentence)

            Example JSON format:
            [
              {"day": 1, "passage": "Psalms 23:1-3", "notes": "Resting in the perfect care of our Shepherd."},
              {"day": 2, "passage": "John 1:1-4", "notes": "Finding life and light in the Word of life."}
            ]

            Only return the JSON. Do not write any markdown codeblock characters or preamble.
        """.trimIndent()

        try {
            val responseText = makeApiCall(apiKey, prompt)
            val jsonText = cleanJsonResponse(parseResponseText(responseText))
            val jsonArray = JSONArray(jsonText)
            val result = mutableListOf<GeneratedDayPlan>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    GeneratedDayPlan(
                        day = obj.getInt("day"),
                        passage = obj.getString("passage"),
                        notes = obj.getString("notes")
                    )
                )
            }
            if (result.isNotEmpty()) {
                return@withContext result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate reading plan, using offline fallback", e)
        }
        return@withContext fallback
    }

    /**
     * Provides beautifully composed text illustrations / historical background for the active chapter.
     */
    suspend fun getChapterIllustration(book: String, chapter: Int): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "A beautiful visual illustration depicting the key thematic essence of $book chapter $chapter."
        }

        val prompt = """
            Describe a stunning, artistic, visual illustration painting for $book Chapter $chapter.
            Summarize the key events and depict a highly atmospheric, detailed scene using rich colors and symbolic elements. Keep it respectful, elegant, and inspirational (maximum 3 sentences).
        """.trimIndent()

        try {
            val responseText = makeApiCall(apiKey, prompt)
            return@withContext parseResponseText(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get chapter illustration", e)
            return@withContext "An artistic depiction of the creation, walk of faith, and divine wisdom described in $book Chapter $chapter."
        }
    }

    private fun makeApiCall(apiKey: String, prompt: String): String {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        
        // Build JSON body
        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(jsonRequest.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return response.body?.string() ?: ""
        }
    }

    private fun parseResponseText(responseBody: String): String {
        try {
            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val part = parts.getJSONObject(0)
            return part.getString("text").trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: $responseBody", e)
            return ""
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7)
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3)
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length - 3)
        }
        return cleaned.trim()
    }

    private fun getFallbackPlanList(topic: String): List<GeneratedDayPlan> {
        val cleanTopic = topic.lowercase().trim()
        val passageList = when {
            cleanTopic.contains("peace") || cleanTopic.contains("anxiety") || cleanTopic.contains("comfort") -> {
                listOf(
                    GeneratedDayPlan(1, "Psalms 23:1-3", "Resting in our Shepherd's presence and comfort."),
                    GeneratedDayPlan(2, "Psalms 121:1-4", "Knowing your help comes from the Lord."),
                    GeneratedDayPlan(3, "Proverbs 3:5-6", "Trusting in eternal wisdom rather than fear."),
                    GeneratedDayPlan(4, "Matthew 5:4-6", "The comforting promises of Christ for those who hunger."),
                    GeneratedDayPlan(5, "John 1:4-5", "The light of Christ that shines in the darkest parts.")
                )
            }
            cleanTopic.contains("faith") || cleanTopic.contains("trust") || cleanTopic.contains("hope") -> {
                listOf(
                    GeneratedDayPlan(1, "Proverbs 3:5-8", "Learn to rely fully on the Lord with your whole heart."),
                    GeneratedDayPlan(2, "John 1:14-18", "Experiencing grace upon grace in Christ."),
                    GeneratedDayPlan(3, "Psalms 121:5-8", "The Lord is your guardian and will shield your life."),
                    GeneratedDayPlan(4, "Matthew 5:14-16", "Letting your light shine as a testament of faith."),
                    GeneratedDayPlan(5, "Genesis 1:1-5", "Believing in the God who calls light out of default darkness.")
                )
            }
            else -> {
                listOf(
                    GeneratedDayPlan(1, "John 1:1-5", "Reflecting on the light in the beginning."),
                    GeneratedDayPlan(2, "Psalms 23:4-6", "Walking through dark valleys with divine confidence."),
                    GeneratedDayPlan(3, "Proverbs 3:1-4", "Writing mercy and truth upon your heart's tablets."),
                    GeneratedDayPlan(4, "Matthew 5:5-9", "Developing high character traits: mercy and peacemaking."),
                    GeneratedDayPlan(5, "Psalms 121:1-3", "Lifting up our eyes to receive divine guidance.")
                )
            }
        }
        return passageList
    }
}

data class GeneratedDayPlan(
    val day: Int,
    val passage: String,
    val notes: String
)
