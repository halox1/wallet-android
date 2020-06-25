package com.mycelium.bequant.remote

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mycelium.bequant.BequantPreference
import com.mycelium.bequant.remote.model.*
import kotlinx.coroutines.CoroutineScope
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File


class KYCRepository {
    fun create(scope: CoroutineScope, applicant: KYCApplicant, success: (() -> Unit)) {
        doRequest(scope, {
            service.create(KYCCreateRequest(applicant))
        }, {
            val uuid = it?.uuid ?: ""
            BequantPreference.setKYCToken(uuid)
            success.invoke()
        }, { code, msg ->

        }, {

        })
    }

    fun mobileVerification(scope: CoroutineScope, success: ((String) -> Unit)) {
        doRequest(scope, {
            service.mobileVerification(BequantPreference.getKYCToken())
        }, {
            success.invoke(it?.message ?: "")
        }, { code, msg ->

        }, {

        })
    }

    fun checkMobileVerification(scope: CoroutineScope, code: String,
                                success: (() -> Unit), error: (() -> Unit)) {
        doRequest(scope, {
            service.checkMobileVerification(BequantPreference.getKYCToken(), code)
        }, {
            if (it?.message == "CODE_VALID") {
                success.invoke()
            } else {
                error.invoke()
            }
        }, { code, msg ->

        }, {

        })
    }

    fun uploadDocument(scope: CoroutineScope, type: KYCDocument, file: File,
                       progress: ((Long, Long) -> Unit), success: () -> Unit, error: () -> Unit) {
        doRequest(scope, {
            val fileBody = ProgressRequestBody(file, "image")
            fileBody.progressListener = progress
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, fileBody)
            service.uploadFile(BequantPreference.getKYCToken(), type, "ITA", multipartBody)
        }, { response ->
            success.invoke()
        }, { code, msg ->
            error.invoke()
        }, {})
    }

    fun status(scope: CoroutineScope, success: ((KYCStatus) -> Unit)) {
        doRequest(scope, {
            service.status(BequantPreference.getKYCToken())
        }, { response ->
            success.invoke(response?.message?.global!!)
        }, { code, msg ->

        }, {

        })
    }

    companion object {
        val ENDPOINT = "https://test006.bqtstuff.com/"


        private val objectMapper = ObjectMapper()
                .registerKotlinModule()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)

        val repository by lazy { KYCRepository() }

        val service by lazy {
            Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .client(OkHttpClient.Builder()
                            .addInterceptor {
                                it.proceed(it.request().newBuilder().apply {
                                    header("Content-Type", "application/json")
                                    if (BequantPreference.getAccessToken().isNotEmpty()) {
                                        header("x-access-token", "xyz")
                                    }
                                }.build())
                            }
                            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                            .build())
                    .addConverterFactory(NullOnEmptyConverterFactory())
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .build()
                    .create(BequantKYCApiService::class.java)
        }
    }
}