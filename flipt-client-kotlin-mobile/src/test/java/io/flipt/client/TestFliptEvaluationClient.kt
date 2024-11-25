package io.flipt.client

import io.flipt.client.models.AuthenticationStrategy
import io.flipt.client.models.EvaluationRequest
import org.junit.After
import org.junit.Before
import org.junit.Test


class TestFliptEvaluationClient {
    private var fliptClient: FliptEvaluationClient? = null

    @Before
    @Throws(Exception::class)
    fun initAll() {
//        val fliptURL = System.getenv()["FLIPT_URL"]
//        val clientToken = System.getenv()["FLIPT_AUTH_TOKEN"]
        val fliptURL = "https://ff-stgiot.bluebird.id/:8080"
        val clientToken = "secret"

        assert(fliptURL != null && !fliptURL.isEmpty())
        assert(clientToken != null && !clientToken.isEmpty())
        fliptClient =
            fliptURL?.let {
                FliptEvaluationClient.builder()
                    .url(it)
                    .authentication(clientToken?.let {
                        AuthenticationStrategy.ClientTokenAuthentication(
                            it
                        )
                    })
                    .build()
            }
    }

    @Test
    @Throws(Exception::class)
    fun testEvaluateVariant() {
        val context: MutableMap<String, String> = HashMap()
        context["fizz"] = "buzz"

        val response  = fliptClient?.evaluateVariant("flag1", "entity", context)

        assert("flag1".equals(response?.flagKey))
        assert(response?.match ?: false)
        assert("MATCH_EVALUATION_REASON".equals(response?.reason))
        assert("variant1".equals(response?.variantKey))
        assert("segment1".equals(response?.segmentKeys?.get(0)))
    }

    @Test
    @Throws(Exception::class)
    fun testEvaluateBoolean() {
        val context: MutableMap<String, String> = HashMap()
        context["fizz"] = "buzz"

        val response = fliptClient?.evaluateBoolean("flag_boolean", "entity", context)

        assert("flag_boolean".equals(response?.flagKey))
        assert(response?.enabled ?: false)
        assert("MATCH_EVALUATION_REASON".equals(response?.reason))
    }

    @Test
    @Throws(Exception::class)
    fun testEvaluateBatch() {
        val context: MutableMap<String, String> = HashMap()
        context["fizz"] = "buzz"

        val evalRequests: Array<EvaluationRequest> = arrayOf<EvaluationRequest>(
            EvaluationRequest("flag1", "entity", context),
            EvaluationRequest("flag_boolean", "entity", context),
            EvaluationRequest("notfound", "entity", context)
        )

        val response = fliptClient?.evaluateBatch(evalRequests)

        assert(3.equals(response?.responses?.size))
        val responses = response?.responses

        assert(responses?.get(0)?.variantEvaluationResponse != null)
        val variantResponse  = responses?.get(0)?.variantEvaluationResponse
        assert("flag1".equals(variantResponse?.flagKey))
        assert(variantResponse?.match ?: false)
        assert("MATCH_EVALUATION_REASON".equals(variantResponse?.reason))
        assert("variant1".equals(variantResponse?.variantKey))
        assert("segment1".equals(variantResponse?.segmentKeys?.get(0)))

        assert(responses?.get(1)?.booleanEvaluationResponse != null)
        val booleanResponse = responses?.get(1)?.booleanEvaluationResponse
        assert("flag_boolean".equals(booleanResponse?.flagKey))
        assert(booleanResponse?.enabled ?: false)
        assert("MATCH_EVALUATION_REASON".equals(booleanResponse?.reason))

        assert(responses?.get(2)?.errorEvaluationResponse != null)
        val errorResponse = responses?.get(2)?.errorEvaluationResponse
        assert("notfound".equals(errorResponse?.flagKey))
        assert("default".equals(errorResponse?.namespaceKey))
        assert("NOTlistFlags_FOUND_ERROR_EVALUATION_REASON".equals(errorResponse?.reason))
    }

    @Test
    @Throws(Exception::class)
    fun testListFlags() {
        val flags = fliptClient?.listFlags()
        assert(2.equals(flags?.size))
    }

    @After
    @Throws(Exception::class)
    fun tearDownAll() {
        fliptClient?.close()
    }

}