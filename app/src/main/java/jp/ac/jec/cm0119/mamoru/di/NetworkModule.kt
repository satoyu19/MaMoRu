package jp.ac.jec.cm0119.mamoru.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.ac.jec.cm0119.mamoru.data.ApiInterface
import jp.ac.jec.cm0119.mamoru.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun ProvideRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi)).build()

    @Provides
    @Singleton
    fun moshiConverterFactory(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): ApiInterface = retrofit.create(ApiInterface::class.java)
}