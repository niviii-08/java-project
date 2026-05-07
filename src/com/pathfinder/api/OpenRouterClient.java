package com.pathfinder.api;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class OpenRouterClient {

    private static final String API_KEY = loadApiKey();
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "openrouter/free"; // Use strictly free models to avoid credit errors
    private static final String SITE_URL = "http://pathfinder.local";
    private static final String SITE_NAME = "Pathfinder Career Guidance";

    // ── System prompt for deep training ───────────────────────────
    private static final String SYSTEM_PROMPT = """
            You are Pathfinder, an elite career guidance AI trained specifically for Indian students and professionals.

            === CORE IDENTITY ===
            You provide deeply personalised, age-appropriate, and academically-calibrated career advice. You understand the Indian education system, competitive exams, cultural expectations, regional opportunities, and the modern job market.

            === TONE ===
            Be warm, encouraging, realistic, and specific. Use Indian examples. Mention rupee salaries. Reference actual Indian universities (IIT, NIT, AIIMS, DU, Mumbai University, etc.). Never be generic.

            === FORMATTING RULES ===
            1. DO NOT use Markdown formatting (no **, no __, no ### headers).
            2. For lists, use simple bullets like "•" or "-".
            3. DO NOT use Markdown tables. Use plain text alignment or simple lists instead.
            4. Keep responses readable as plain text or basic HTML.

            IMPORTANT: Always respond in the requested format (JSON or plain text as specified).
            """;

    // ── Main chat method ───────────────────────────────────────────
    public static String chat(String userMessage) throws IOException {
        return chat(userMessage, null);
    }

    public static String chat(String userMessage, String additionalContext) throws IOException {
        String fullMessage = additionalContext != null
                ? additionalContext + "\n\n" + userMessage
                : userMessage;

        String requestBody = buildRequestBody(fullMessage, false);
        return callAPI(requestBody);
    }

    // ── Career suggestions ─────────────────────────────────────────
    public static String getCareerSuggestions(String profile) throws IOException {
        String prompt = """
                Based on this profile, provide career suggestions.
                
                === REQUIRED JSON FORMAT ===
                {
                  "careers": [
                    {
                      "title": "Career Name",
                      "match": 95,
                      "description": "Why this fits them",
                      "exams": ["Exam1", "Exam2"],
                      "timeframe": "4 years",
                      "avgSalary": "₹8-15 LPA",
                      "growth": "High"
                    }
                  ],
                  "summary": "Overall assessment paragraph",
                  "strengths": ["strength1", "strength2"],
                  "nextSteps": ["Step 1", "Step 2", "Step 3"]
                }

                Profile: """
                + profile + """

                        Return ONLY valid JSON, no markdown fences, no extra text.
                        """;
        return chat(prompt);
    }

    // ── Roadmap generation ─────────────────────────────────────────
    public static String getRoadmap(String career, String profile) throws IOException {
        String prompt = """
                Generate a detailed career roadmap for the career '""" + career + """
                ' based on this profile: """ + profile + """

                === REQUIRED JSON FORMAT ===
                {
                  "career": "Career Title",
                  "phases": [
                    {
                      "phase": "Phase Name",
                      "duration": "6 months",
                      "goals": ["goal1", "goal2"],
                      "resources": ["resource1"],
                      "milestones": ["milestone1"]
                    }
                  ],
                  "totalDuration": "4 years",
                  "keyExams": ["Exam1"],
                  "tips": ["tip1", "tip2"]
                }

                Return ONLY valid JSON roadmap in the format specified, no markdown fences.
                """;
        return chat(prompt);
    }

    // ── Spelling corrector for skills/interests ────────────────────
    public static String correctSpelling(String text) throws IOException {
        String prompt = "Correct any spelling mistakes in these skills/interests and return ONLY the corrected, clean comma-separated list. Do not add new items. Input: "
                + text;
        return chat(prompt);
    }

    // ── Follow-up question generator ──────────────────────────────
    public static String generateFollowUpQuestions(String context) throws IOException {
        String prompt = """
                Based on this student/professional context, generate 3-5 relevant follow-up questions to better understand their background and interests.
                Context: """
                + context + """

                        Return as JSON array: ["Question 1?", "Question 2?", ...]
                        Return ONLY the JSON array.
                        """;
        return chat(prompt);
    }

    // ── HTTP call ─────────────────────────────────────────────────
    private static String callAPI(String requestBody) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("HTTP-Referer", SITE_URL);
        conn.setRequestProperty("X-Title", SITE_NAME);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null)
                response.append(line);
        }

        String raw = response.toString();

        if (code < 200 || code >= 300) {
            throw new IOException("API Error " + code + ": " + raw);
        }

        return extractContent(raw);
    }

    private static String buildRequestBody(String userMessage, boolean jsonMode) {
        String systemJson = escapeJson(SYSTEM_PROMPT);
        String userJson = escapeJson(userMessage);

        return "{" +
                "\"model\":\"" + MODEL + "\"," +
                "\"messages\":[" +
                "{\"role\":\"system\",\"content\":\"" + systemJson + "\"}," +
                "{\"role\":\"user\",\"content\":\"" + userJson + "\"}" +
                "]," +
                "\"max_tokens\":2048," +
                "\"temperature\":0.7" +
                "}";
    }

    private static String extractContent(String json) {
        // Extract content from: {"choices":[{"message":{"content":"..."}}]}
        try {
            int idx = json.indexOf("\"content\":");
            if (idx == -1)
                return json;
            int start = json.indexOf("\"", idx + 10) + 1;
            int end = json.lastIndexOf("\"");
            // Find closing properly
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            for (int i = start; i < json.length(); i++) {
                char ch = json.charAt(i);
                if (escape) {
                    if (ch == 'n')
                        sb.append('\n');
                    else if (ch == 't')
                        sb.append('\t');
                    else if (ch == '"')
                        sb.append('"');
                    else if (ch == '\\')
                        sb.append('\\');
                    else
                        sb.append(ch);
                    escape = false;
                } else if (ch == '\\') {
                    escape = true;
                } else if (ch == '"') {
                    break;
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return json;
        }
    }

    private static String loadApiKey() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            String key = props.getProperty("OPENROUTER_API_KEY");
            if (key == null || key.trim().isEmpty()) {
                System.err.println("Warning: OPENROUTER_API_KEY not found in config.properties");
                return "";
            }
            return key.trim();
        } catch (IOException e) {
            System.err.println("Warning: Could not load config.properties. Using empty API key.");
            return "";
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
