package com.void.shell.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object R {
    const val BOOT     = "boot"
    const val MENU     = "menu"
    const val DASH     = "dashboard"
    const val TERMINAL = "terminal"
    const val MAP      = "world_map"
    const val INVENTORY= "inventory"
    const val SKILLS   = "skills"
    const val FACTION  = "faction"
    const val MARKET   = "market"
    const val SETTINGS = "settings"
    const val ACHIEVE  = "achievements"
    const val LOGS     = "logs"

    fun mission(id: String)      = "mission/$id"
    fun missionLive(id: String)  = "mission_live/$id"
    fun story(nodeId: String)    = "story/$nodeId"
}

@Composable
fun VoidNavGraph(nav: NavHostController = rememberNavController()) {
    NavHost(navController = nav, startDestination = R.BOOT) {
        composable(R.BOOT)      { BootRoute(nav) }
        composable(R.MENU)      { MenuRoute(nav) }
        composable(R.DASH)      { DashRoute(nav) }
        composable(R.TERMINAL)  { TerminalRoute(nav) }
        composable(R.MAP)       { MapRoute(nav) }
        composable(R.INVENTORY) { InventoryRoute(nav) }
        composable(R.SKILLS)    { SkillsRoute(nav) }
        composable(R.FACTION)   { FactionRoute(nav) }
        composable(R.MARKET)    { MarketRoute(nav) }
        composable(R.SETTINGS)  { SettingsRoute(nav) }
        composable(R.ACHIEVE)   { AchieveRoute(nav) }
        composable(R.LOGS)      { LogsRoute(nav) }
        composable("mission/{id}")     { ent -> MissionRoute(nav, ent.arguments?.getString("id") ?: "") }
        composable("mission_live/{id}"){ ent -> MissionLiveRoute(nav, ent.arguments?.getString("id") ?: "") }
        composable("story/{id}")       { ent -> StoryRoute(nav, ent.arguments?.getString("id") ?: "") }
    }
}

// Stubs — Phase 7 mein full implementation
@Composable fun BootRoute(n: NavHostController)              { /* Phase 7 */ }
@Composable fun MenuRoute(n: NavHostController)              { /* Phase 7 */ }
@Composable fun DashRoute(n: NavHostController)              { /* Phase 7 */ }
@Composable fun TerminalRoute(n: NavHostController)          { /* Phase 7 */ }
@Composable fun MapRoute(n: NavHostController)               { /* Phase 7 */ }
@Composable fun InventoryRoute(n: NavHostController)         { /* Phase 7 */ }
@Composable fun SkillsRoute(n: NavHostController)            { /* Phase 7 */ }
@Composable fun FactionRoute(n: NavHostController)           { /* Phase 7 */ }
@Composable fun MarketRoute(n: NavHostController)            { /* Phase 7 */ }
@Composable fun SettingsRoute(n: NavHostController)          { /* Phase 7 */ }
@Composable fun AchieveRoute(n: NavHostController)           { /* Phase 7 */ }
@Composable fun LogsRoute(n: NavHostController)              { /* Phase 7 */ }
@Composable fun MissionRoute(n: NavHostController, id: String)    { /* Phase 7 */ }
@Composable fun MissionLiveRoute(n: NavHostController, id: String){ /* Phase 7 */ }
@Composable fun StoryRoute(n: NavHostController, id: String)      { /* Phase 7 */ }
