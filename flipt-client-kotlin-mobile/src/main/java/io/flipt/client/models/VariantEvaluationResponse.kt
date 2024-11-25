package io.flipt.client.models

import kotlinx.serialization.Serializable

@Serializable
data class VariantEvaluationResponse(
    val match: Boolean,
    val segmentKeys: List<String>,
    val reason: String,
    val flagKey: String,
    val variantKey: String,
    val variantAttachment: String,
    val requestDurationMillis: Float,
    val timestamp: String) {
}