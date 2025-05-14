@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.coffee_manager.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.coffee_manager.R

@Composable
fun CommonTopBar(
    navController: NavController,
    title: String
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(text = title)
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate("profile") }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_account),
                    contentDescription = "User Profile",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors()
    )
}

