package io.agora.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.myapplication.services.RTEManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val rteManager: RTEManager
): ViewModel() {
    fun logout() {
        rteManager.logout()
    }
}