package com.savares.dailyinstallmentsaver

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.savares.dailyinstallmentsaver.ui.AddInstallmentScreen
import com.savares.dailyinstallmentsaver.ui.DashboardScreen
import com.savares.dailyinstallmentsaver.ui.theme.DailyInstallmentSaverTheme
import com.savares.dailyinstallmentsaver.util.LanguageConfig
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var languageCode by remember { mutableStateOf(LanguageConfig.getLanguage(this)) }
            
            // Re-apply configuration when language changes
            CompositionLocalProvider {
                DailyInstallmentSaverTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DailyInstallmentApp(
                            currentLanguage = languageCode,
                            onLanguageChange = { newLang ->
                                LanguageConfig.setLanguage(this, newLang)
                                languageCode = newLang
                                // Trigger activity recreate to apply locale changes globally
                                recreate()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = LanguageConfig.getLanguage(newBase)
        super.attachBaseContext(LanguageConfig.updateResources(newBase, lang))
    }
}

@Composable
fun DailyInstallmentApp(currentLanguage: String, onLanguageChange: (String) -> Unit) {
    val navController = rememberNavController()
    val viewModel: InstallmentViewModel = viewModel()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate("add_installment") },
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
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
