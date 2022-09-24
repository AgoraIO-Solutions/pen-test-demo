package io.agora.myapplication.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import io.agora.myapplication.ui.app.BarNavigationItem
import io.agora.myapplication.ui.app.RTCNavItem
import io.agora.myapplication.ui.app.RTMNavItem
import io.agora.myapplication.ui.app.SettingsNavItem
import io.agora.myapplication.ui.theme.MyApplicationTheme
import io.agora.myapplication.viewmodels.NavigationViewModel

@Composable
fun AppBottomBar(barNavigationItem: BarNavigationItem) {
    val viewModel = hiltViewModel<NavigationViewModel>()
    BottomNavigation {
        listOf<BarNavigationItem>(RTCNavItem, RTMNavItem, SettingsNavItem).forEach { aNavItem ->
            BottomNavigationItem(
                label = { Text(stringResource(id = aNavItem.title)) },
                icon = {
                    Image(
                        painter = painterResource(id = aNavItem.icon) ,
                        contentDescription = stringResource(id =aNavItem.title) )
                       },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                alwaysShowLabel = true,
                selected = aNavItem == barNavigationItem,
                onClick = {
                    viewModel.goto(aNavItem)
                }
            )
        }
    }
}




/*

@Composable
fun AppBottomBar(barNavigationItem: BarNavigationItem) {
    BottomAppBar {
            BottomAppNavItem(item = RTCNavItem, highligted = RTCNavItem == barNavigationItem)
            Spacer(modifier = Modifier.fillMaxWidth(0.1f))
            BottomAppNavItem(item = RTMNavItem, highligted = RTMNavItem == barNavigationItem)
            Spacer(modifier = Modifier.fillMaxWidth(0.1f))
            BottomAppNavItem(
                item = SettingsNavItem,
                highligted = SettingsNavItem == barNavigationItem
            )
    }
}

@Composable
private fun BottomAppNavItem(item: BarNavigationItem, highligted: Boolean) {
        val color = if (!highligted)  Color.Gray else Color.White
        val viewModel = hiltViewModel<NavigationViewModel>()

        Button(onClick = { viewModel.goto(item) }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = item.icon),
                    contentDescription = stringResource(id = item.title),
                    colorFilter = ColorFilter.tint(color)
                )
                Text(
                    text = stringResource(id = item.title),
                    color = color
                )
            }
        }
}
*/
@Preview
@Composable
private fun RTCAppBarPreview() {
    MyApplicationTheme {
        Scaffold(bottomBar = { AppBottomBar(barNavigationItem = RTCNavItem) }) {
            Text("Test")
        }
    }
}

@Preview
@Composable
private fun RTMAppBarPreview() {
    MyApplicationTheme {
        Scaffold(bottomBar = { AppBottomBar(barNavigationItem = RTMNavItem) }) {
            Text("Test")
        }
    }
}

@Preview
@Composable
private fun SettingsAppBarPreview() {
    MyApplicationTheme {
        Scaffold(bottomBar = { AppBottomBar(barNavigationItem = SettingsNavItem) }) {
            Text("Test")
        }
    }
}

