package com.example.coffee_manager.Navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.coffee_manager.View.*
import com.example.coffee_manager.View.Manager.AddFoodScreen
import com.example.coffee_manager.View.Manager.HomeAdminScreen
import com.example.coffee_manager.View.Manager.RegisterScreen
import com.example.coffee_manager.View.Manager.UserListScreen
import com.example.coffee_manager.View.Chef.HomeBepScreen
import com.example.coffee_manager.View.Order.OrderScreen
import com.example.coffee_manager.View.Cashier.HomeThuNganScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }

        // Các màn hình Home theo từng vai trò
        composable("home_admin") { HomeAdminScreen(navController) }
        composable("home_order") { OrderScreen(navController) }
        composable("home_thungan") { HomeThuNganScreen(navController) }
        composable("home_bep") { HomeBepScreen(navController) }
        composable("user_list") { UserListScreen(navController) }

        // Admin
        composable("add_food") { AddFoodScreen(navController) }

    }
}
