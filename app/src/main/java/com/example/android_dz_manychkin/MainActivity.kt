package com.example.android_dz_manychkin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.android_dz_manychkin.state.WatchlistStateHolder
import com.example.android_dz_manychkin.ui.theme.Android_dz_manychkinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_dz_manychkinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val stateHolder = remember { WatchlistStateHolder() }


                    WatchlistScreen(
                        uiState = stateHolder.uiState,
                        onSearchQueryChange = stateHolder::onSearchQueryChange,
                        onFilterChange = stateHolder::onFilterChange,
                        onNextStatus = stateHolder::onNextStatus
                    )
                }
            }
        }
    }
}