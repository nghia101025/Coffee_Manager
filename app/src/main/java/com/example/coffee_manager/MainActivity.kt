package com.example.coffee_manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.coffee_manager.Navigation.AppNavigation
import com.example.coffee_manager.ui.theme.Coffee_ManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Coffee_ManagerTheme {
                // Wrap the app in a MaterialTheme and Surface for styling
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Set up navigation with NavController
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)  // Passing navController to the navigation function
                }
            }
        }
    }
}
