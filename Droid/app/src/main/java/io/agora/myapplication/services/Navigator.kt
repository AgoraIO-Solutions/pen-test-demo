package io.agora.myapplication.services

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.agora.myapplication.ui.app.NavigationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigatorModule {
    @Provides
    @Singleton
    fun providesNavigator() = Navigator()
}

class Navigator {
    private val _navigationItemFlow: MutableSharedFlow<NavigationItem> = MutableSharedFlow()
    val navigationItemFlow: SharedFlow<NavigationItem> get() = _navigationItemFlow

    suspend fun navigateTo(navigationItem: NavigationItem) = withContext(Dispatchers.Main) {
        _navigationItemFlow.emit(navigationItem)
    }
}