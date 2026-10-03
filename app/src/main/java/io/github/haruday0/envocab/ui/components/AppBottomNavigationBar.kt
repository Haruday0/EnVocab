package io.github.haruday0.envocab.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.model.Screen

/**
 * ホーム・カード・リスト・設定の画面下部ナビゲーションバー
 */
@Composable
fun AppBottomNavigationBar(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        @Composable
        fun item(
            screen: Screen,
            label: String,
            selectedIcon: Int,
            unselectedIcon: Int
        ) {
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        painter = painterResource(
                            if (currentScreen == screen) selectedIcon else unselectedIcon
                        ),
                        contentDescription = label
                    )
                },
                label = { Text(label) }
            )
        }

        item(Screen.Home, "ホーム", R.drawable.ic_home_filled, R.drawable.ic_home_outlined)
        item(Screen.Card, "カード", R.drawable.ic_quiz_filled, R.drawable.ic_quiz_outlined)
        item(Screen.List, "リスト", R.drawable.ic_list_alt_filled, R.drawable.ic_list_alt_outlined)
        item(Screen.Settings, "設定", R.drawable.ic_settings_filled, R.drawable.ic_settings_outlined)
    }
}
