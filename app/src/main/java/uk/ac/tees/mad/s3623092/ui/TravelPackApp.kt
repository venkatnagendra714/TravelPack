package uk.ac.tees.mad.s3623092.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object TravelPackRoutes {
    const val SECURE_ACCESS = "secure_access"
    const val DASHBOARD = "dashboard"
    const val ADD_TRIP = "add_trip"
    const val MANAGE_TRIPS = "manage_trips"
    const val EDIT_TRIP = "edit_trip/{tripId}"

    fun editTripRoute(tripId: Long): String {
        return "edit_trip/$tripId"
    }
}

@Composable
fun TravelPackApp(
    startDestination: String = TravelPackRoutes.SECURE_ACCESS,
    secureAccessContent: @Composable (
        onAuthenticated: () -> Unit
    ) -> Unit,
    dashboardContent: @Composable (
        onAddTrip: () -> Unit,
        onManageTrips: () -> Unit
    ) -> Unit,
    addTripContent: @Composable (
        onBack: () -> Unit,
        tripId: Long?
    ) -> Unit,
    manageTripsContent: @Composable (
        onBack: () -> Unit,
        onEditTrip: (Long) -> Unit
    ) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = TravelPackRoutes.SECURE_ACCESS
        ) {
            secureAccessContent {
                navController.navigate(
                    TravelPackRoutes.DASHBOARD
                ) {
                    popUpTo(
                        TravelPackRoutes.SECURE_ACCESS
                    ) {
                        inclusive = true
                    }
                }
            }
        }

        composable(
            route = TravelPackRoutes.DASHBOARD
        ) {
            dashboardContent(
                {
                    navController.navigate(
                        TravelPackRoutes.ADD_TRIP
                    )
                },
                {
                    navController.navigate(
                        TravelPackRoutes.MANAGE_TRIPS
                    )
                }
            )
        }

        composable(
            route = TravelPackRoutes.ADD_TRIP
        ) {
            addTripContent(
                {
                    navController.popBackStack()
                },
                null
            )
        }

        composable(
            route = TravelPackRoutes.EDIT_TRIP,
            arguments = listOf(
                navArgument("tripId") {
                    type = NavType.LongType
                }
            )
        ) { entry ->
            val tripId = entry.arguments?.getLong("tripId")

            addTripContent(
                {
                    navController.popBackStack()
                },
                tripId
            )
        }

        composable(
            route = TravelPackRoutes.MANAGE_TRIPS
        ) {
            manageTripsContent(
                {
                    navController.popBackStack()
                },
                { tripId ->
                    navController.navigate(
                        TravelPackRoutes.editTripRoute(tripId)
                    )
                }
            )
        }
    }
}