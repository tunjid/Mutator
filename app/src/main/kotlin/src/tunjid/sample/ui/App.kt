package src.tunjid.sample.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.StateHolder
import com.tunjid.mutator.scopedStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import src.tunjid.sample.globalui.UiState
import src.tunjid.sample.nav.MultiStackNav

class App {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val navStateHolder: StateHolder<Mutation<MultiStackNav>, MultiStackNav> = scopedStateHolder(
        scope = scope,
        initialState = MultiStackNav(),
        transform = { it }
    )
    val globalUiStateHolder: StateHolder<Mutation<UiState>, UiState> = scopedStateHolder(
        scope = scope,
        initialState = UiState(),
        transform = { it }
    )
}

val AppDependencies = staticCompositionLocalOf { App() }
