package jp.ac.jec.cm0119.mamoru.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.ac.jec.cm0119.mamoru.data.ApiInterface
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository


@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    fun provideFirebaseRepository(api: ApiInterface): FirebaseRepository{
        return FirebaseRepository(api)
    }
}