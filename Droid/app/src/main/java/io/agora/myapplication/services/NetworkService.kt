package io.agora.myapplication.services

import android.content.Context
import retrofit2.Response
import retrofit2.http.GET
import com.google.gson.annotations.SerializedName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.agora.myapplication.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query
import java.nio.channels.Channel
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RestClientModule {
    @Singleton
    @Provides
    fun providesSampleTokensApiService(@ApplicationContext context: Context) : NetworkService {
        val apiKey = context.getString(R.string.aws_api_key)
        val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()

            val request = original.newBuilder()
                .addHeader("x-api-key", apiKey)
                .method(original.method(), original.body())
                .build()

            chain.proceed(request)
        }.build()

        return  Retrofit.Builder()
            .baseUrl("https://${context.getString(R.string.aws_api_base)}")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(NetworkService::class.java)
    }
}

data class AesKey(
    @SerializedName("key") val key: String
    )
data class Tokens(
    @SerializedName("uid") val uid: Long, // not an UInt because of converters
    @SerializedName("rtmuid") val rtmuid: String,
    @SerializedName("rtc") val rtc: String,
    @SerializedName("rtm") val rtm: String
    )

interface NetworkService {
    @GET("dev/api/pen_test_aes_key")
    suspend fun getAesToken(@Query("channel") channel: String): Response<AesKey>

    @GET("dev/api/pen_test_token")
    suspend fun getToken(@Query("channel") channel: String): Response<Tokens>
}