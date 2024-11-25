package io.flipt.client

import io.flipt.client.models.AuthenticationStrategy
import io.flipt.client.models.BatchEvaluationResponse
import io.flipt.client.models.BooleanEvaluationResponse
import io.flipt.client.models.ClientOptions
import io.flipt.client.models.EvaluationRequest
import io.flipt.client.models.FetchMode
import io.flipt.client.models.Flag
import io.flipt.client.models.Result
import io.flipt.client.models.VariantEvaluationResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration


class FliptEvaluationClient(namespace: String, options: ClientOptions) {
    private var engine: Long = 0

    init {
        val clientOptionsSerialized = Json.encodeToString(options)
        engine = CLibrary.INSTANCE.initializeEngine(namespace, clientOptionsSerialized)
    }

    class FliptEvaluationClientBuilder {
        private var namespace = "default"
        private var url = "http://localhost:8080"
        private var authentication: AuthenticationStrategy? = null
        private var reference: String? = null
        private var updateInterval: Duration? = null
        private var fetchMode = FetchMode.polling

        /**
         * url sets the URL for the Flipt server.
         *
         * @param url the URL for the Flipt server
         * @return the FliptEvaluationClientBuilder
         */
        fun url(url: String): FliptEvaluationClientBuilder {
            this.url = url
            return this
        }

        /**
         * namespace sets the namespace for the Flipt server.
         *
         * @param namespace the namespace for the Flipt server
         * @return the FliptEvaluationClientBuilder
         */
        fun namespace(namespace: String): FliptEvaluationClientBuilder {
            this.namespace = namespace
            return this
        }

        /**
         * authentication sets the authentication strategy for the Flipt server.
         *
         * @param authentication the authentication strategy for the Flipt server
         * @return the FliptEvaluationClientBuilder
         */
        fun authentication(authentication: AuthenticationStrategy?): FliptEvaluationClientBuilder {
            this.authentication = authentication
            return this
        }

        /**
         * updateInterval sets the update interval for the Flipt server.
         *
         * @param updateInterval the update interval for the Flipt server
         * @return the FliptEvaluationClientBuilder
         */
        fun updateInterval(updateInterval: Duration?): FliptEvaluationClientBuilder {
            this.updateInterval = updateInterval
            return this
        }

        /**
         * reference sets the reference for the Flipt server.
         *
         * @param reference the reference for the Flipt server
         * @return the FliptEvaluationClientBuilder
         */
        fun reference(reference: String?): FliptEvaluationClientBuilder {
            this.reference = reference
            return this
        }

        /**
         * fetchMode sets the fetch mode for the Flipt server. Note: Streaming is currently only
         * supported when using the SDK with Flipt Cloud (https://flipt.io/cloud).
         *
         * @param fetchMode the fetch mode for the Flipt server
         * @return the FliptEvaluationClientBuilder
         */
        fun fetchMode(fetchMode: FetchMode): FliptEvaluationClientBuilder {
            this.fetchMode = fetchMode
            return this
        }

        /**
         * build builds a new FliptEvaluationClient.
         *
         * @return the FliptEvaluationClient
         * @throws EvaluationException if the FliptEvaluationClient could not be built
         */
        @Throws(EvaluationException::class)
        fun build(): FliptEvaluationClient {
            return FliptEvaluationClient(
                namespace,
                ClientOptions(
                    url,
                    updateInterval,
                    authentication,
                    reference,
                    fetchMode
                )
            )
        }
    }



    @Serializable
    data class InternalEvaluationRequest(
       @SerialName("flag_key") val flagKey: String,
       @SerialName("entity_id") val entityId: String,
       @SerialName("context") val context: Map<String, String>
    )

    fun evaluateVariant(
        flagKey: String,
        entityId: String,
        context: Map<String, String>
    ): VariantEvaluationResponse {
        val evaluationRequest = InternalEvaluationRequest(flagKey, entityId, context)
        val evaluationRequestSerialized = Json.encodeToString(evaluationRequest)

        val value = CLibrary.INSTANCE.evaluateVariant(engine, evaluationRequestSerialized)

        return Json.decodeFromString(VariantEvaluationResponse.serializer(), value)
    }

    fun evaluateBoolean(flagKey: String, entityId: String, context: Map<String, String>): BooleanEvaluationResponse {
        val evaluationRequest = InternalEvaluationRequest(flagKey, entityId, context)
        val evaluationRequestSerialized = Json.encodeToString(evaluationRequest)

        val value = CLibrary.INSTANCE.evaluateBoolean(engine, evaluationRequestSerialized)

        return Json.decodeFromString(BooleanEvaluationResponse.serializer(), value)
    }

    fun evaluateBatch(batchEvaluationRequest: Array<EvaluationRequest>): BatchEvaluationResponse {
        val evaluationrequests = mutableListOf<InternalEvaluationRequest>()

        batchEvaluationRequest.map {
            evaluationrequests.add(InternalEvaluationRequest(it.flagKey, it.entityId, it.context))
        }

        val batchEvaluationRequestSerialized = Json.encodeToString(evaluationrequests)

        return Json.decodeFromString(BatchEvaluationResponse.serializer(), CLibrary.INSTANCE.evaluateBatch(engine, batchEvaluationRequestSerialized))
    }

    fun listFlags(): ArrayList<Flag>? {
        val value = CLibrary.INSTANCE.listFlags(engine)
        val resp = readFlags(value)
        return resp?.result
    }

    fun readFlags(ptr: String): Result<ArrayList<Flag>>? {
        try {
            return Json.decodeFromString<Result<ArrayList<Flag>>>(ptr)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            CLibrary.INSTANCE.destroyString(ptr)
        }

        return null
    }

    fun close() {
        CLibrary.INSTANCE.destroyEngine(engine)
    }

    companion object{
        fun builder(): FliptEvaluationClientBuilder {
            return FliptEvaluationClientBuilder()
        }
    }
}