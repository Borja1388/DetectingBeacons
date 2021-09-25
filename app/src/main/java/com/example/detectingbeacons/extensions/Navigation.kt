package com.example.detectingbeacons.extensions

import androidx.navigation.NavController
import androidx.navigation.NavDirections

fun NavController.navigateSafe(destination: NavDirections) {
    currentDestination?.getAction(destination.actionId)?.let { navigate(destination) }
}