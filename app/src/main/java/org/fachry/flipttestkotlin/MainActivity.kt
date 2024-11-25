package org.fachry.flipttestkotlin

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.flipt.client.FliptEvaluationClient


class MainActivity : AppCompatActivity() {
    private lateinit var fliptClient: FliptEvaluationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val fliptURL = "https://ff-stgiot.bluebird.id"
        val clientToken = "secret"

        assert(fliptURL != null && !fliptURL.isEmpty())
        assert(clientToken != null && !clientToken.isEmpty())
        fliptClient = FliptEvaluationClient.builder()
            .url(url = fliptURL)
            .namespace("iot")
            .build()


        val context: MutableMap<String, String> = HashMap()
        context["fizz"] = "buzz"

        val response  = fliptClient?.evaluateVariant("iot-driving-pattern-ff", "entity", context)

        Log.i("TAG", response?.flagKey ?: "null")
    }
}