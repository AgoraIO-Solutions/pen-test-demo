package io.agora.myapplication.viewmodels

import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.myapplication.services.Navigator
import io.agora.myapplication.ui.app.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val navigator: Navigator
): ViewModel() {
   fun goto(navigationItem: NavigationItem) {
       viewModelScope.launch {
           navigator.navigateTo(navigationItem)
       }
   }
}