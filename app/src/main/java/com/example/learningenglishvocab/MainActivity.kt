package com.example.learningenglishvocab

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
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.example.learningenglishvocab.ui.screen.home.HomeView
import com.example.learningenglishvocab.ui.screen.library.CreateVocabScreen
import com.example.learningenglishvocab.ui.screen.library.LibraryMainScreen
import com.example.learningenglishvocab.ui.screen.library.MatchCardScreen
import com.example.learningenglishvocab.ui.screen.library.SetDetailScreen
import com.example.learningenglishvocab.ui.screen.library.VocabFlashCardScreen
import com.example.learningenglishvocab.ui.screen.library.VocabLearnResultScreen
import com.example.learningenglishvocab.ui.screen.library.VocabLearnScreen
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

@Composable
fun EApp(
    authViewModel: AuthViewModel,
    vocabSetViewModel: VocabSetViewModel,
    libraryViewModel: LibraryViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(BottomNavItem.Home.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else if (currentRoute !in listOf("login", "register", "verifyEmail", null)) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            Log.d("EApp", "No navigation performed - isLoggedIn: $isLoggedIn, currentRoute: $currentRoute")
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
            composable("login") {
                UserLoginView(modifier = Modifier, navController)
            }

            composable("register") {
                UserRegisterView(modifier = Modifier, navController)
            }

            composable("verifyEmail") {
                VerifyEmailView(modifier = Modifier, navController, authViewModel)
            }

            composable("home") {
                HomeView(modifier = Modifier, navController, authViewModel)
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
                HomeView(modifier = Modifier, navController, authViewModel)
            }
        }
    }
}