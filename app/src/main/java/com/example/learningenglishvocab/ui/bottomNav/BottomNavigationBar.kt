package com.example.learningenglishvocab.ui.bottomNav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(currentRoute: String, onTabSelected: (BottomNavItem) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xfff2f1eb))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.values().forEach { item ->
            val isSelected = currentRoute == item.route

            val shape = when (item) {
                BottomNavItem.Home, BottomNavItem.Library -> RoundedCornerShape(
                    topStart = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = 15.dp,
                    topEnd = if (isSelected) 0.dp else 15.dp
                )
                else -> RoundedCornerShape(
                    topEnd = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = 15.dp,
                    topStart = if (isSelected) 0.dp else 15.dp
                )
            }

            Surface(
                shape = shape,
                color = if (isSelected) Color(0xfffa6f3e) else Color.Transparent,
                modifier = Modifier
                    .size(width = 56.dp, height = 48.dp)
                    .clickable { onTabSelected(item) }
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isSelected) item.selectedIcon else item.unselectedIcon),
                        contentDescription = item.name,
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}