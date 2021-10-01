package src.tunjid.sample.nav

import androidx.compose.runtime.Composable

interface Route {
    @Composable
    fun Render()
}

data class StackNav(
    val name: String,
    val routes: List<Route> = listOf()
)

data class MultiStackNav(
    val currentIndex: Int = -1,
    val stacks: List<StackNav> = listOf()
)




val MultiStackNav.current get() = stacks.getOrNull(currentIndex)?.routes?.last()