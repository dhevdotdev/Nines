package dev.dhev.nines.ui.habits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SmokeFree
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Yard
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIcons {
    val options: List<Pair<String, ImageVector>> =
        listOf(
            // Fitness & health
            "fitness" to Icons.Filled.FitnessCenter,
            "run" to Icons.AutoMirrored.Filled.DirectionsRun,
            "walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
            "bike" to Icons.AutoMirrored.Filled.DirectionsBike,
            "pool" to Icons.Filled.Pool,
            "weight" to Icons.Filled.MonitorWeight,
            "spa" to Icons.Filled.Spa,
            // Mind & sleep
            "meditate" to Icons.Filled.SelfImprovement,
            "sleep" to Icons.Filled.Bedtime,
            "night" to Icons.Filled.NightsStay,
            "alarm" to Icons.Filled.Alarm,
            "psychology" to Icons.Filled.Psychology,
            "lightbulb" to Icons.Filled.Lightbulb,
            // Food & drink
            "food" to Icons.Filled.Restaurant,
            "coffee" to Icons.Filled.LocalCafe,
            "water" to Icons.Filled.LocalDrink,
            "blender" to Icons.Filled.Blender,
            "noSmoking" to Icons.Filled.SmokeFree,
            // Learning & work
            "book" to Icons.AutoMirrored.Filled.MenuBook,
            "school" to Icons.Filled.School,
            "language" to Icons.Filled.Language,
            "code" to Icons.Filled.Code,
            "work" to Icons.Filled.Work,
            "checklist" to Icons.Filled.Checklist,
            "timer" to Icons.Filled.Timer,
            // Creative
            "music" to Icons.Filled.MusicNote,
            "piano" to Icons.Filled.Piano,
            "art" to Icons.Filled.Brush,
            "palette" to Icons.Filled.Palette,
            "camera" to Icons.Filled.PhotoCamera,
            // Social & money
            "heart" to Icons.Filled.Favorite,
            "people" to Icons.Filled.People,
            "call" to Icons.Filled.Call,
            "savings" to Icons.Filled.Savings,
            "money" to Icons.Filled.AttachMoney,
            // Home & outdoors
            "home" to Icons.Filled.Home,
            "cleaning" to Icons.Filled.CleaningServices,
            "yard" to Icons.Filled.Yard,
            "park" to Icons.Filled.Park,
            "nature" to Icons.Filled.Nature,
            "sun" to Icons.Filled.WbSunny,
            "pet" to Icons.Filled.Pets,
            // Misc
            "games" to Icons.Filled.SportsEsports,
            "phone" to Icons.Filled.PhoneAndroid,
            "trophy" to Icons.Filled.EmojiEvents,
        )

    const val default = "fitness"

    fun iconFor(key: String): ImageVector = options.firstOrNull { it.first == key }?.second ?: Icons.Filled.Star
}
