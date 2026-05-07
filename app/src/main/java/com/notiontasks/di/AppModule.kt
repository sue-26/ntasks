package com.notiontasks.di

import android.content.Context
import androidx.room.Room
import com.notiontasks.BuildConfig
import com.notiontasks.data.api.*
import com.notiontasks.data.repository.PreferencesRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotionTasksDatabase =
        Room.databaseBuilder(context, NotionTasksDatabase::class.java, "notion_tasks.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSourceDao(db: NotionTasksDatabase) = db.sourceDao()
    @Provides fun provideTaskDao(db: NotionTasksDatabase) = db.taskDao()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttp(prefs: PreferencesRepository): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { prefs.accessToken.first() }
            val request = chain.request().newBuilder().apply {
                if (!token.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
                addHeader("Notion-Version", "2022-06-28")
                addHeader("Content-Type", "application/json")
            }.build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.NOTION_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideNotionApiService(retrofit: Retrofit): NotionApiService =
        retrofit.create(NotionApiService::class.java)
}
