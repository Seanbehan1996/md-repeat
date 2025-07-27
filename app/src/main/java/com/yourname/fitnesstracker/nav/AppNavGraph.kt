package com.yourname.fitnesstracker.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yourname.fitnesstracker.ui.HomeScreen
import com.yourname.fitnesstracker.ui.HistoryScreen
import com.yourname.fitnesstracker.ui.WorkoutScreen
import com.yourname.fitnesstracker.ui.GoalScreen
import com.yourname.fitnesstracker.ui.ProfileScreen
import com.yourname.fitnesstracker.ui.ProfileSetupScreen
import com.yourname.fitnesstracker.ui.AnalyticsDashboardScreen
import com.yourname.fitnesstracker.ui.AchievementsScreen
import com.yourname.fitnesstracker.viewmodel.MainViewModel
import com.yourname.fitnesstracker.viewmodel.UserProfileViewModel
import com.yourname.fitnesstracker.viewmodel.AnalyticsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val mainViewModel: MainViewModel = viewModel()
    val userProfileViewModel: UserProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        // Main bottom navigation screens
        composable("home") {
            HomeScreen(navController, userProfileViewModel)
        }
        composable("workout") {
            WorkoutScreen(mainViewModel, navController)
        }
        composable("analytics") {
            val analyticsViewModel: AnalyticsViewModel = viewModel()
            AnalyticsDashboardScreen(navController, analyticsViewModel)
        }
        composable("goals") {
            GoalScreen(mainViewModel, navController)
        }
        composable("profile") {
            ProfileScreen(navController, userProfileViewModel)
        }

        // Secondary screens (not in bottom nav)
        composable("history") {
            HistoryScreen(mainViewModel, navController)
        }
        composable("profile_setup") {
            ProfileSetupScreen(navController, userProfileViewModel)
        }
        composable("achievements") {
            val analyticsViewModel: AnalyticsViewModel = viewModel()
            AchievementsScreen(navController, analyticsViewModel)
        }
    }
}