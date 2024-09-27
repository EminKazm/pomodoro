package com.syntax.data.di

import com.syntax.data.SessionRepositoryImpl
import com.syntax.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {


    @Binds
    @Singleton
    fun bindSessionRepository(sessionRepositoryImpl: SessionRepositoryImpl): SessionRepository


}
