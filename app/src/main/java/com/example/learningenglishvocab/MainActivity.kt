package com.example.learningenglishvocab

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learningenglishvocab.ui.bottomNav.BottomNavItem
import com.example.learningenglishvocab.ui.bottomNav.BottomNavigationBar
import com.example.learningenglishvocab.ui.screen.auth.UserLoginView
import com.example.learningenglishvocab.ui.screen.auth.UserRegisterView
import com.example.learningenglishvocab.ui.screen.auth.VerifyEmailView
import com.example.learningenglishvocab.ui.screen.discover.DiscoverMainScreen
import com.example.learningenglishvocab.ui.screen.home.HomeMainScreen
import com.example.learningenglishvocab.ui.screen.library.CreateVocabScreen
import com.example.learningenglishvocab.ui.screen.library.LibraryMainScreen
import com.example.learningenglishvocab.ui.screen.library.MatchCardScreen
import com.example.learningenglishvocab.ui.screen.library.SetDetailScreen
import com.example.learningenglishvocab.ui.screen.library.VocabFlashCardScreen
import com.example.learningenglishvocab.ui.screen.library.VocabLearnResultScreen
import com.example.learningenglishvocab.ui.screen.library.VocabLearnScreen
import com.example.learningenglishvocab.ui.screen.profile.AccountManagementScreen
import com.example.learningenglishvocab.ui.screen.profile.ChangePasswordScreen
import com.example.learningenglishvocab.ui.screen.profile.ChangeUsernameScreen
import com.example.learningenglishvocab.ui.screen.profile.PrivacyAndSupportScreen
import com.example.learningenglishvocab.ui.screen.profile.ProfileMainScreen
import com.example.learningenglishvocab.ui.screen.profile.YearlyOverviewScreen
import com.example.learningenglishvocab.ui.theme.LearningEnglishVocabTheme
import com.example.learningenglishvocab.viewmodel.AuthViewModel
import com.example.learningenglishvocab.viewmodel.LibraryViewModel
import com.example.learningenglishvocab.viewmodel.VocabSetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val authViewModel: AuthViewModel by viewModels()
        val vocabSetViewModel: VocabSetViewModel by viewModels {
            VocabSetViewModelFactory(authViewModel)
        }
        val libraryViewModel: LibraryViewModel by viewModels() {
            LibraryViewModelFactory(authViewModel)
        }

        setContent {
            LearningEnglishVocabTheme {
                EApp(authViewModel, vocabSetViewModel, libraryViewModel)
            }
        }
    }
}

// Factory cho LibraryViewModel
class LibraryViewModelFactory(private val authViewModel: AuthViewModel) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory cho VocabSetViewModel
class VocabSetViewModelFactory(private val authViewModel: AuthViewModel) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabSetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabSetViewModel(authViewModel = authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("NewApi")
@Composable
fun EApp(
    authViewModel: AuthViewModel,
    vocabSetViewModel: VocabSetViewModel,
    libraryViewModel: LibraryViewModel
) {
    Log.d("EApp", "EApp recomposed")
    val navController = rememberNavController()
    var isCheckingAuth by remember { mutableStateOf(true) }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    Log.d("EApp", "isLoggedIn: $isLoggedIn, currentRoute: $currentRoute")

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            Log.d("Navigation", "Navigating to Home")
            navController.navigate(BottomNavItem.Home.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            Log.d("Navigation", "Clearing back stack and navigating to Login")
            // Xóa toàn bộ back stack trước khi điều hướng
            navController.popBackStack(navController.graph.startDestinationId, inclusive = true)
            navController.navigate("login") {
                popUpTo(0) { inclusive = true } // Xóa tất cả các destination
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    BottomNavItem.Home.route,
                    BottomNavItem.Library.route,
                    BottomNavItem.Discover.route,
                    BottomNavItem.Profile.route
                )
            ) {
                BottomNavigationBar(currentRoute = currentRoute ?: "") { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) BottomNavItem.Home.route else "login",
//            startDestination = "libraryMain",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = "login",
                enterTransition = { null },
                exitTransition = { null }
            ) {
                UserLoginView(modifier = Modifier, navController, authViewModel)
            }

            composable("register") {
                UserRegisterView(modifier = Modifier, navController)
            }

            composable("verifyEmail") {
                VerifyEmailView(modifier = Modifier, navController, authViewModel)
            }

            composable(
                route = "home",
                enterTransition = { null },
                exitTransition = { null }
            ) {
                HomeMainScreen(
                    modifier = Modifier,
                    authViewModel = AuthViewModel(),
                    navController = navController
                )

            }

            composable("createVocabSet/{id}", arguments = listOf(navArgument("id") {
                nullable = true
                defaultValue = null
            })) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                CreateVocabScreen(
                    modifier = Modifier,
                    libraryViewModel,
                    vocabSetViewModel,
                    navController,
                    vocabSetId = id
                )
            }

            composable("libraryMain") {
                LibraryMainScreen(
                    modifier = Modifier,
                    authViewModel,
                    libraryViewModel,
                    vocabSetViewModel,
                    navController
                )
            }

            composable("vocabSetDetail/{id}", arguments = listOf(navArgument("id") {
                nullable = true
                defaultValue = null
            })) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                SetDetailScreen(
                    modifier = Modifier,
                    authViewModel,
                    vocabSetViewModel,
                    navController,
                    vocabSetId = id
                )
            }

            composable("vocabFlashCard/{id}", arguments = listOf(navArgument("id") {
                nullable = true
                defaultValue = null
            })) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                VocabFlashCardScreen(
                    modifier = Modifier,
                    vocabSetViewModel,
                    navController,
                    vocabSetId = id
                )
            }

            composable(
                route = "vocabLearn/{id}?retry={retry}",
                arguments = listOf(
                    navArgument("id") { nullable = true; defaultValue = null },
                    navArgument("retry") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                val isRetry = backStackEntry.arguments?.getBoolean("retry") ?: false
                VocabLearnScreen(
                    modifier = Modifier,
                    vocabSetViewModel = vocabSetViewModel,
                    authViewModel = authViewModel,
                    navController = navController,
                    vocabSetId = id,
                    isRetryMode = isRetry
                )
            }

            composable(
                route = "vocabLearnResult/{id}",
                arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                VocabLearnResultScreen(
                    modifier = Modifier,
                    vocabSetViewModel = vocabSetViewModel,
                    authViewModel = authViewModel,
                    navController = navController,
                    vocabSetId = id
                )
            }

            composable(
                route = "matchCard/{id}",
                arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                MatchCardScreen(
                    modifier = Modifier,
                    vocabSetViewModel = vocabSetViewModel,
                    navController = navController,
                    vocabSetId = id
                )
            }

            composable("discover") {
                DiscoverMainScreen(
                    modifier = Modifier,
                    navController = navController,
                )
            }

            composable(route = "profile") {
                ProfileMainScreen (
                    modifier = Modifier,
                    authViewModel = authViewModel,
                    navController = navController,
                )
            }

            composable(route = "yearlyOverview") {
                YearlyOverviewScreen (
                    authViewModel = authViewModel,
                    navController = navController,
                )
            }

            composable(route = "accountManagement") {
                AccountManagementScreen (
                    authViewModel = authViewModel,
                    navController = navController,
                )
            }

            composable("changeUsername") {
                ChangeUsernameScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("changePassword") {
                ChangePasswordScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(route = "privacyAndSupport") {
                PrivacyAndSupportScreen (
                    navController = navController,
                )
            }
        }
    }
}