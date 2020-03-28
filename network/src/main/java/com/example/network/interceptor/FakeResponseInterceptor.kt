package com.example.network.interceptor

import com.example.network.BuildConfig
import com.example.network.utils.fakeGetItemsResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.HttpURLConnection

class FakeResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!BuildConfig.DEBUG) {
            throw IllegalAccessError("FakeResponseInterceptor is intended to be used only in debug mode")
        }

        val url = chain.request().url

        val response = when (url.pathSegments.first()) {
            "get_items" -> fakeGetItemsResponse(
                url.queryParameter("page_index")!!.toInt(),
                url.queryParameter("page_size")!!.toInt()
            )
            else -> ""
        }

        return chain.proceed(chain.request())
            .newBuilder()
            .code(HttpURLConnection.HTTP_OK)
            .protocol(Protocol.HTTP_2)
            .message(response)
            .body(response.toByteArray().toResponseBody("application/json".toMediaType()))
            .addHeader("content-type", "application/json")
            .build()
    }
}