package com.deepthink.org.dto;

/**
 * Carries back: - projectId: to form download URL - zipUrl: where frontend can
 * fetch the ZIP - indexHtml: for immediate live preview
 */
public record SiteResponse(String projectId, String zipUrl, String indexHtml, String error) {
}