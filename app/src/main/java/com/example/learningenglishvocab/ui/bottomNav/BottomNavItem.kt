package com.example.learningenglishvocab.ui.bottomNav

import com.example.learningenglishvocab.R

enum class BottomNavItem(val route: String, val selectedIcon: Int, val unselectedIcon: Int) {
    Home("home", R.drawable.home_active, R.drawable.home),
    Library("libraryMain", R.drawable.book_active, R.drawable.book),
    Discover("discover", R.drawable.discover_active, R.drawable.discover),
    Profile("profile", R.drawable.profile_active, R.drawable.profile)
}