package com.mob.proyectoandroid.data.network

import com.mob.proyectoandroid.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SupabaseHeaderInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .build()

        return chain.proceed(request)
    }
}