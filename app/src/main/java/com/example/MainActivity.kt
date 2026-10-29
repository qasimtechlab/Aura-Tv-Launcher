package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.AuraHomeScreen
import com.example.ui.theme.AuraTvTheme
import com.example.ui.theme.TvBackground
import com.example.ui.viewmodel.AuraLauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AuraLauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AuraTvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TvBackground
                ) {
                    AuraHomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh installed apps whenever returning from an opened app or settings
        viewModel.loadApps()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Intercept TV remote Menu button to trigger quick options
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            val current = viewModel.focusedApp.value
            if (current != null) {
                viewModel.openContextMenu(current)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
