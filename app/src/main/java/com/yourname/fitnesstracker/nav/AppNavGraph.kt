package com.yourname.fitnesstracker.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yourname.fitnesstracker.ui.HomeScreen
import com.yourname.fitnesstracker.ui.HistoryScreen
import com.yourname.fitnesstracker.ui.WorkoutScreen
import com.yourname.fitnesstracker.ui.GoalScreen
import com.yourname.fitnesstracker.viewmodel.MainViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    val mainViewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("workout") { WorkoutScreen(mainViewModel, navController) }
        composable("history") { HistoryScreen(mainViewModel, navController) }
        composable("goals") { GoalScreen(mainViewModel, navController) }
    }
}