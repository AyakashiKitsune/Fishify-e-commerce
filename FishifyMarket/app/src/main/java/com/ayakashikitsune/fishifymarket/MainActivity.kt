package com.ayakashikitsune.fishifymarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ayakashikitsune.fishifymarket.presentation.HomeScreen
import com.ayakashikitsune.fishifymarket.domain.FishifyViewModel
import com.ayakashikitsune.fishifymarket.ui.theme.FishifyMarketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm = viewModel<FishifyViewModel>()
            FishifyMarketTheme {
                    HomeScreen(vm = vm)
            }
        }
    }
}


