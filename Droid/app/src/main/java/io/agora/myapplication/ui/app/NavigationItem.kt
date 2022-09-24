package io.agora.myapplication.ui.app

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.agora.myapplication.R

sealed class NavigationItem(val route: String)
open class BarNavigationItem(route: String, @DrawableRes val icon: Int, @StringRes val title: Int) : NavigationItem(route = route)
object RTMNavItem:  BarNavigationItem(route =  "rtm", icon = R.drawable.ic_text, title = R.string.rtm)
object RTCNavItem:  BarNavigationItem(route =  "rtc", icon = R.drawable.ic_video, title = R.string.rtc)
object SettingsNavItem:  BarNavigationItem(route =  "settings", icon = R.drawable.ic_settings, title = R.string.settings)
object LoginNavItem: NavigationItem(route = "login")

