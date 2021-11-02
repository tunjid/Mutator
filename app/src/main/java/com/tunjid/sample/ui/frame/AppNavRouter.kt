package com.tunjid.sample.ui.frame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import com.tunjid.sample.nav.MultiStackNav
import com.tunjid.sample.nav.Route
import com.tunjid.sample.nav.current
import com.tunjid.sample.ui.mapState

@Composable
internal fun AppNavRouter(
    navStateFlow: StateFlow<MultiStackNav>
) {
    val scope = rememberCoroutineScope()
    val routeState = navStateFlow
        .mapState(scope, MultiStackNav::current)
        .collectAsState()

    when (val route = routeState.value) {
        is Route -> route.Render()
        else -> Box {
            Text(
                modifier = Modifier
                    .padding(),
                text = "404"
            )
        }
    }
}