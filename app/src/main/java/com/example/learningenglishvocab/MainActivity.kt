package com.example.learningenglishvocab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learningenglishvocab.ui.screen.auth.UserLoginView
import com.example.learningenglishvocab.ui.screen.auth.UserRegisterView
import com.example.learningenglishvocab.ui.screen.auth.VerifyEmailView
import com.example.learningenglishvocab.ui.screen.home.HomeView
import com.example.learningenglishvocab.ui.theme.LearningEnglishVocabTheme
import com.example.learningenglishvocab.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val authViewModel: AuthViewModel by viewModels()
        setContent {
            LearningEnglishVocabTheme {
                EApp(authViewModel)
            }
        }
    }
}

@Composable
fun EApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.fillMaxSize().padding(innerPadding)
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
                ProtectedScreen(authViewModel, navController) {
                    HomeView(modifier = Modifier, navController)
                }
            }
        }
    }
}

@Composable
fun ProtectedScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    content: @Composable () -> Unit
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    if (isLoggedIn) {
        content()
    }
}