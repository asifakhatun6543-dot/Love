package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiService {

    private fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("placeholder", ignoreCase = true)
    }

    suspend fun getIceBreakers(currentName: String, partnerName: String, partnerBio: String, partnerInterests: String): List<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext getMockIcebreakers(partnerName, partnerInterests)
        }

        val prompt = """
            You are the AI Matchmaker in the HeartSync proximity dating app. 
            Generate 3 short, catchy, highly engaging, personalized icebreaker conversation starters or questions that $currentName can use to message $partnerName. 
            $partnerName's bio is: "$partnerBio" and interests are: "$partnerInterests".
            Keep them fun, friendly, and distinct. Respond in a clean list format with 3 bullet points, e.g.:
            1. First icebreaker question...
            2. Second playful statement...
            3. Third engaging question...
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                parseListResponse(text)
            } else {
                getMockIcebreakers(partnerName, partnerInterests)
            }
        } catch (e: Exception) {
            getMockIcebreakers(partnerName, partnerInterests)
        }
    }

    suspend fun getCompatibilityScoreAndAnalysis(meBio: String, meInterests: String, partnerName: String, partnerBio: String, partnerInterests: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext getMockCompatibility(partnerName, meInterests, partnerInterests)
        }

        val prompt = """
            You are the AI Matchmaker inside HeartSync. Compute a compatibility score (an integer percentage from 50 to 99) and a 2-3 sentence visual, playful relationship compatibility analysis between two users.
            User 1 (Me) Interests: $meInterests, Bio: "$meBio".
            User 2 ($partnerName) Interests: $partnerInterests, Bio: "$partnerBio".
            Return your response in this exact format:
            SCORE: [score]
            ANALYSIS: [your 2-3 sentence analysis]
        """.trimIndent()

        val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))

        try {
            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                val scoreRegex = "SCORE:\\s*(\\d+)".toRegex()
                val analysisRegex = "ANALYSIS:\\s*(.+)".toRegex(RegexOption.DOT_MATCHES_ALL)

                val scoreMatch = scoreRegex.find(text)
                val analysisMatch = analysisRegex.find(text)

                val score = scoreMatch?.groupValues?.get(1)?.toIntOrNull() ?: 85
                val analysis = analysisMatch?.groupValues?.get(1)?.trim() ?: "You both share vibrant interests and complementary energies!"
                Pair(score, analysis)
            } else {
                getMockCompatibility(partnerName, meInterests, partnerInterests)
            }
        } catch (e: Exception) {
            getMockCompatibility(partnerName, meInterests, partnerInterests)
        }
    }

    suspend fun getPersonalityInsights(name: String, bio: String, interests: String): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext "Based on $name's profile, they are a vibrant Explorer with a high affinity for creative experiences and social sharing."
        }

        val prompt = """
            Analyze this user's profile and generate a 2-sentence whimsical "AI Personality Insight" showing their unique social vibe (e.g., 'A modern romantic who thrives on espresso and high-fidelity vinyl record vibes').
            Name: $name
            Bio: "$bio"
            Interests: $interests
        """.trimIndent()

        val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))

        try {
            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "A mystery soul waiting to be uncovered."
        } catch (e: Exception) {
            "A creative spirit who seeks real, unfiltered human connection."
        }
    }

    private fun parseListResponse(text: String): List<String> {
        val lines = text.split("\n")
        val result = mutableListOf<String>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.startsWith("-") || trimmed.startsWith("*")) {
                val cleanLine = trimmed.replace(Regex("^[123.\\-*\\s]+"), "").trim()
                if (cleanLine.isNotEmpty()) {
                    result.add(cleanLine)
                }
            }
        }
        return if (result.size >= 2) result.take(3) else listOf(
            "What's your absolute favorite place we should explore first?",
            "If you could only listen to one album for the next year, what would it be?",
            "What's the most exciting adventure on your bucket list right now?"
        )
    }

    private fun getMockIcebreakers(partnerName: String, interests: String): List<String> {
        val interestList = interests.split(",").map { it.trim() }
        val commonInterest = interestList.firstOrNull() ?: "Exploring"
        return listOf(
            "Hey $partnerName! I noticed you love $commonInterest. What's your absolute favorite spot or thing to do with that?",
            "Your bio is super cool! If we got together, are we grabbing a cozy espresso, going stargazing, or exploring a hiking trail?",
            "Let's settle a fun debate: what's the ultimate playlist song or movie for a cozy, rainy afternoon?"
        )
    }

    private fun getMockCompatibility(partnerName: String, myInterests: String, partnerInterests: String): Pair<Int, String> {
        val mySet = myInterests.split(",").map { it.trim().lowercase() }.toSet()
        val partnerSet = partnerInterests.split(",").map { it.trim().lowercase() }.toSet()
        val overlaps = mySet.intersect(partnerSet).size

        val score = when (overlaps) {
            0 -> 78
            1 -> 85
            2 -> 92
            else -> 98
        }

        val overlapWord = if (overlaps > 0) "overlapping passions like ${mySet.intersect(partnerSet).joinToString(", ") { it.capitalize() }}" else "beautifully complementary lifestyles"
        val analysis = "Your compatibility is outstanding! With $overlapWord, conversations between you and $partnerName will feel effortlessly natural and exciting. You both lean heavily toward authentic, experiential storytelling."
        return Pair(score, analysis)
    }
}
