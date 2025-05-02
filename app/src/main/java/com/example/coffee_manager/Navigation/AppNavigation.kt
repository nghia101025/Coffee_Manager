package com.example.coffee_manager.Navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.coffee_manager.View.*
import com.example.coffee_manager.View.Admin.HomeAdminScreen
import com.example.coffee_manager.View.Admin.UserListScreen
import com.example.coffee_manager.View.Bep.HomeBepScreen
import com.example.coffee_manager.View.Oder.HomeOrderScreen
import com.example.coffee_manager.View.ThuNgan.HomeThuNganScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }

        // Các màn hình Home theo từng vai trò
        composable("home_admin") { HomeAdminScreen(navController) }
        composable("home_order") { HomeOrderScreen(navController) }
        composable("home_thungan") { HomeThuNganScreen(navController) }
        composable("home_bep") { HomeBepScreen(navController) }
        composable("user_list") { UserListScreen(navController) }

        // Thêm các màn hình khác ở đây nếu có
    }
}
