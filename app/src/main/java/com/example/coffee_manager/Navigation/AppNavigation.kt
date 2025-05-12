package com.example.coffee_manager.Navigation


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.coffee_manager.View.*
import com.example.coffee_manager.View.Manager.Food.AddFoodScreen
import com.example.coffee_manager.View.Manager.HomeAdminScreen
import com.example.coffee_manager.View.Manager.Employee.RegisterScreen
import com.example.coffee_manager.View.Manager.Employee.UserListScreen
import com.example.coffee_manager.View.Chef.HomeBepScreen
import com.example.coffee_manager.View.Order.OrderScreen
import com.example.coffee_manager.View.Order.FoodDetailScreen
import com.example.coffee_manager.View.Cashier.HomeThuNganScreen
import com.example.coffee_manager.View.Manager.Employee.UpdateEmployeeScreen
import com.example.coffee_manager.View.Manager.Food.CategoryListScreen
import com.example.coffee_manager.View.Manager.Food.FoodListScreen
import com.example.coffee_manager.View.Manager.Food.UpdateFoodScreen
import com.example.coffee_manager.View.Manager.Space.TableManagementScreen
import com.example.coffee_manager.View.Manager.Table.TableDetailScreen
import com.example.coffee_manager.View.Order.CartScreen
import com.example.coffee_manager.View.ProfileScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }

        // Các màn hình Home theo từng vai trò
        composable("home_admin") { HomeAdminScreen(navController) }
        composable("home_order") { OrderScreen(navController) }
        composable("home_thungan") { HomeThuNganScreen(navController) }
        composable("home_bep") { HomeBepScreen(navController) }

        // Admin
        composable("add_food") { AddFoodScreen(navController) }
        composable("add_employee") { RegisterScreen(navController) }

        composable("food_list") { FoodListScreen(navController) }
        composable("user_list") { UserListScreen(navController) }
        composable("table_list") { TableManagementScreen(navController) }
        composable("category_list") { CategoryListScreen(navController) }
        composable("profile") { ProfileScreen(navController) }


        composable(
            route = "update_employee/{idUser}",
            arguments = listOf(navArgument("idUser") { type = NavType.StringType })
        ) { backStackEntry ->
            // Lấy idUser từ arguments
            val idUser = backStackEntry.arguments?.getString("idUser") ?: ""
            UpdateEmployeeScreen(navController, idUser)
        }

        composable(
            route = "update_food/{idFood}",
            arguments = listOf(navArgument("idFood") { type = NavType.StringType })
        ) { backStackEntry ->
            UpdateFoodScreen(navController, backStackEntry)
        }

        composable(
            route = "table_detail/{idTable}",
            arguments = listOf(navArgument("idTable") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            // Lấy idTable từ argument
            val idTable = backStackEntry.arguments?.getString("idTable") ?: return@composable
            TableDetailScreen(
                navController = navController,
                tableId = idTable
            )
        }

        composable(
            "foodDetail/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStack ->
            val foodId = backStack.arguments?.getString("foodId")!!
            FoodDetailScreen(navController, foodId)
        }
        composable("cart") {
            CartScreen(navController)
        }





    }
}
