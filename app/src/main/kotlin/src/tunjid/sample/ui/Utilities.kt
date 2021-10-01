package src.tunjid.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tunjid.mutator.Mutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import src.tunjid.sample.globalui.UiState


fun <T, R> StateFlow<T>.mapState(scope: CoroutineScope, mapper: (T) -> R) =
    map { mapper(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            initialValue = mapper(value),
            started = SharingStarted.WhileSubscribed(2000),
        )

private data class MutableFunction<T>(var backing: (T) -> Unit = {}) : (T) -> Unit {
    override fun invoke(item: T) = backing(item)
}

@Composable
fun InitialUiState(state: UiState) {
    val uiStateHolder = AppDependencies.current.globalUiStateHolder

    val toolbarMenuClickListener = remember {
        MutableFunction(state.toolbarMenuClickListener)
    }
    val altToolbarMenuClickListener = remember {
        MutableFunction(state.altToolbarMenuClickListener)
    }

    DisposableEffect(true) {
        uiStateHolder.accept(Mutation {
            state.copy(
                systemUI = systemUI,
                toolbarMenuClickListener = toolbarMenuClickListener,
                altToolbarMenuClickListener = altToolbarMenuClickListener
            )
        })
        onDispose {
            toolbarMenuClickListener.backing = {}
            altToolbarMenuClickListener.backing = {}
        }
    }
}

data class UISizes(
    val toolbarSize: Dp,
    val bottomNavSize: Dp,
    val snackbarPadding: Dp,
    val navBarHeightThreshold: Dp
)

val uiSizes = UISizes(
    toolbarSize = 56.dp,
    bottomNavSize = 56.dp,
    snackbarPadding = 8.dp,
    navBarHeightThreshold = 80.dp
)

infix fun Dp.countIf(condition: Boolean) = if (condition) this else 0.dp

infix fun Int.countIf(condition: Boolean) = if (condition) this else 0
