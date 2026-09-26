package com.aizen.service;

/**
 * Result of a text generation request.
 *
 * @param text   generated text
 * @param usedAi true when Gemini produced the text, false for the local fallback
 * @param note   human readable explanation of which generator was used
 */
public record GenerationResult(String text, boolean usedAi, String note) {
}
