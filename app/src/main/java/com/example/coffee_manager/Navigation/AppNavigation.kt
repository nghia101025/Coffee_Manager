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
import com.example.coffee_manager.View.Cashier.CashierBillScreen
import com.example.coffee_manager.View.Manager.Food.AddFoodScreen
import com.example.coffee_manager.View.Manager.HomeAdminScreen
import com.example.coffee_manager.View.Manager.Employee.RegisterScreen
import com.example.coffee_manager.View.Manager.Employee.UserListScreen
import com.example.coffee_manager.View.Order.OrderScreen
import com.example.coffee_manager.View.Order.FoodDetailScreen
import com.example.coffee_manager.View.Manager.Employee.UpdateEmployeeScreen
import com.example.coffee_manager.View.Manager.Food.CategoryListScreen
import com.example.coffee_manager.View.Manager.Food.FoodListScreen
import com.example.coffee_manager.View.Manager.Food.UpdateFoodScreen
import com.example.coffee_manager.View.Manager.Space.TableManagementScreen
import com.example.coffee_manager.View.Manager.Table.TableDetailScreen
import com.example.coffee_manager.View.Order.CartScreen
import com.example.coffee_manager.View.Order.OrderSuccessScreen
import com.example.coffee_manager.View.Order.PaymentScreen
import com.example.coffee_manager.View.Order.TableSelectionScreen
import com.example.coffee_manager.View.Cashier.CashierTableScreen
import com.example.coffee_manager.View.Cashier.FinishSuccessScreen
import com.example.coffee_manager.View.Order.BillListScreen
import com.example.coffee_manager.View.ProfileScreen
import com.example.coffee_manager.View.Statistics.StatisticsScreen
import com.example.coffee_manager.View.Statistics.BillDetailScreen
import com.example.coffee_manager.View.Barista.BaristaScreen
import com.example.coffee_manager.View.Manager.Promotion.PromotionListScreen
import com.example.coffee_manager.View.Manager.Qr.QrScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }

        // Các màn hình Home theo từng vai trò
        composable("home_admin") { HomeAdminScreen(navController) }
        composable("home_order") { OrderScreen(navController) }
        composable("home_brewing") { BaristaScreen(navController) }


        // Admin
        composable("add_food") { AddFoodScreen(navController) }
        composable("add_employee") { RegisterScreen(navController) }

        composable("food_list") { FoodListScreen(navController) }
        composable("user_list") { UserListScreen(navController) }
        composable("table_list") { TableManagementScreen(navController) }
        composable("category_list") { CategoryListScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("promotions") {
            PromotionListScreen(navController)
        }
        composable("qr") {
            QrScreen(navController = navController)
        }

        composable("statistics") {
            StatisticsScreen(navController)
        }
        composable("bill_List") { BillListScreen(navController) }
        composable(
            "billDetail/{billId}",
            arguments = listOf(navArgument("billId") { type = NavType.StringType })
        ) { back ->
            val billId = back.arguments?.getString("billId")!!
            BillDetailScreen(navController, billId)
        }


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
        composable("payment") {
            PaymentScreen(navController)
        }
        composable(
            route = "orderSuccess/{orderId}",
            arguments = listOf(navArgument("orderId") {
                type = NavType.StringType
            })
        ) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(orderId = orderId, navController = navController)
        }
        composable("table_select") {
            TableSelectionScreen(navController)
        }




        // Thu ngân
        composable("home_thungan") {
            CashierTableScreen(
                navController = navController,
                onTableSelected = {
                    navController.navigate("home_order")
                },
                onTableOccupied = { tableId, billId,tableNumber ->
                    navController.navigate("cashier_bill/$tableId/$billId/$tableNumber")
                }
            )
        }
        composable(
            "cashier_bill/{tableId}/{billId}/{tableNumber}",
            arguments = listOf(
                navArgument("tableId") { type = NavType.StringType },
                navArgument("billId")  { type = NavType.StringType },
                navArgument("tableNumber")  { type = NavType.IntType }

            )
        ) { backStackEntry ->
            val tableId = backStackEntry.arguments!!.getString("tableId")!!
            val billId  = backStackEntry.arguments!!.getString("billId")!!
            val tableNumber  = backStackEntry.arguments!!.getInt("tableNumber")

            CashierBillScreen(
                navController = navController,
                tableId = tableId,
                billId = billId,
                tableNumber = tableNumber
            )
        }
        composable(
            "finish_success/{billId}",
            arguments = listOf(navArgument("billId") { type = NavType.StringType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments!!.getString("billId")!!
            FinishSuccessScreen(navController, billId)
        }

    }
}
