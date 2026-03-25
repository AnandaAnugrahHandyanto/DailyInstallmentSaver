package com.savares.dailyinstallmentsaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.savares.dailyinstallmentsaver.ui.AddInstallmentScreen
import com.savares.dailyinstallmentsaver.ui.DashboardScreen
import com.savares.dailyinstallmentsaver.ui.theme.DailyInstallmentSaverTheme
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyInstallmentSaverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DailyInstallmentApp()
                }
            }
        }
    }
}

@Composable
fun DailyInstallmentApp() {
    val navController = rememberNavController()
    val viewModel: InstallmentViewModel = viewModel()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate("add_installment") }
            )
        }
        composable("add_installment") {
            AddInstallmentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
