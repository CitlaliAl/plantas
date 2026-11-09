package com.upchiapas.plantas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.upchiapas.plantas.ui.navigation.AppNavHost
import com.upchiapas.plantas.ui.theme.PlantasTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PlantasApplication).container
        setContent {
            PlantasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(container = container)
                }
            }
        }
    }
}
