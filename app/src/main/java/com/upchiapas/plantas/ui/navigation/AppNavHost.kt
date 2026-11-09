package com.upchiapas.plantas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upchiapas.plantas.auth.ui.LoginScreen
import com.upchiapas.plantas.auth.ui.LoginViewModel
import com.upchiapas.plantas.auth.ui.RegisterScreen
import com.upchiapas.plantas.auth.ui.RegisterViewModel
import com.upchiapas.plantas.di.AppContainer
import com.upchiapas.plantas.plants.ui.PlantDetailScreen
import com.upchiapas.plantas.plants.ui.PlantDetailViewModel
import com.upchiapas.plantas.plants.ui.PlantFormScreen
import com.upchiapas.plantas.plants.ui.PlantFormViewModel
import com.upchiapas.plantas.plants.ui.PlantsListScreen
import com.upchiapas.plantas.plants.ui.PlantsListViewModel

object Routes {
    const val LOGIN = "login?expired={expired}"
    const val REGISTER = "register"
    const val PLANTS = "plants"
    const val DETAIL = "plant/{id}"
    const val FORM = "plant_form?id={id}"

    fun login(expired: Boolean = false) = "login?expired=$expired"
    fun detail(id: Int) = "plant/$id"
    fun form(id: Int? = null) = "plant_form?id=${id ?: -1}"
}

@Composable
fun AppNavHost(container: AppContainer, navController: NavHostController = rememberNavController()) {

    // Cierra la sesión local y vuelve a Login sin dejar pantallas en la pila.
    fun goToLogin(expired: Boolean) {
        navController.navigate(Routes.login(expired)) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun goToPlants() {
        navController.navigate(Routes.PLANTS) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun sessionExpired() {
        container.authRepository.clearSession()
        goToLogin(expired = true)
    }

    // remember: la pantalla inicial se decide una sola vez; si cambiara, NavHost reconstruiría el grafo.
    val start = remember { if (container.authRepository.isLoggedIn()) Routes.PLANTS else Routes.LOGIN }

    NavHost(navController = navController, startDestination = start) {

        composable(
            route = Routes.LOGIN,
            arguments = listOf(navArgument("expired") { type = NavType.BoolType; defaultValue = false })
        ) { entry ->
            val vm: LoginViewModel = viewModel(
                factory = viewModelFactory { initializer { LoginViewModel(container.authRepository) } }
            )
            LoginScreen(
                viewModel = vm,
                sessionExpired = entry.arguments?.getBoolean("expired") ?: false,
                onLoggedIn = { goToPlants() },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            val vm: RegisterViewModel = viewModel(
                factory = viewModelFactory { initializer { RegisterViewModel(container.authRepository) } }
            )
            RegisterScreen(
                viewModel = vm,
                onRegistered = { goToPlants() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.PLANTS) {
            val vm: PlantsListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { PlantsListViewModel(container.plantsRepository, container.authRepository) }
                }
            )
            PlantsListScreen(
                viewModel = vm,
                onPlantClick = { navController.navigate(Routes.detail(it)) },
                onAddPlant = { navController.navigate(Routes.form()) },
                onLoggedOut = { goToLogin(expired = false) },
                onSessionExpired = { sessionExpired() }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            val vm: PlantDetailViewModel = viewModel(
                factory = viewModelFactory { initializer { PlantDetailViewModel(id, container.plantsRepository) } }
            )
            PlantDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.form(it)) },
                onSessionExpired = { sessionExpired() }
            )
        }

        composable(
            route = Routes.FORM,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { entry ->
            val id = (entry.arguments?.getInt("id") ?: -1).takeIf { it > 0 }
            val vm: PlantFormViewModel = viewModel(
                factory = viewModelFactory { initializer { PlantFormViewModel(id, container.plantsRepository) } }
            )
            PlantFormScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onSessionExpired = { sessionExpired() }
            )
        }
    }
}
