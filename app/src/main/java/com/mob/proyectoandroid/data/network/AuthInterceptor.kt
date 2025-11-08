package com.mob.proyectoandroid.data.network

import com.mob.proyectoandroid.utils.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceManager: PreferenceManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip adding token for auth endpoints
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        val token = preferenceManager.getAccessToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
