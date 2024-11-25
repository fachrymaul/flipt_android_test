package io.flipt.client.models

import kotlinx.serialization.Serializable

@Serializable
class ErrorEvaluationResponse(val flagKey: String, val namespaceKey: String, val reason: String)