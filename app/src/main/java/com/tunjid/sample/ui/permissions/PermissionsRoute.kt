package com.tunjid.sample.ui.permissions

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tunjid.mutator.Mutator
import com.tunjid.sample.AppDependencies
import com.tunjid.sample.globalui.UiState
import com.tunjid.sample.nav.Route
import com.tunjid.sample.ui.InitialUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class Item(
    val permission: String,
    val name: String,
    val granted: Boolean
)

data class State(
    val permissions: List<Item> = listOf()
)

object PermissionsRoute : Route {
    @Composable
    override fun Render() {
        val scope = rememberCoroutineScope()
        val permissionMutator = AppDependencies.current.permissionMutator

        PermissionsScreen(
            mutator = remember {
                object : Mutator<String, StateFlow<State>> {
                    override val state: StateFlow<State> = permissionMutator.state.map {
                        State(permissions = it.toList().map { (permission, granted) ->
                            Item(
                                permission = permission,
                                granted = granted,
                                name = when (permission) {
                                    Manifest.permission.ACCESS_FINE_LOCATION -> "Location"
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Storage"
                                    Manifest.permission.CAMERA -> "Camera"
                                    Manifest.permission.RECORD_AUDIO -> "Audio"
                                    else -> "IDK"
                                }
                            )
                        })
                    }
                        .stateIn(
                            scope = scope,
                            initialValue = State(),
                            started = SharingStarted.WhileSubscribed(200)
                        )
                    override val accept: (String) -> Unit = permissionMutator.accept
                }
            }
        )
    }
}

@Composable
private fun PermissionsScreen(
    mutator: Mutator<String, StateFlow<State>>
) {

    InitialUiState(
        UiState(
            toolbarMenuClickListener = {
            },
            toolbarTitle = "Permissions",
            toolbarOverlaps = false,
            toolbarShows = true,
            showsBottomNav = true,
            fabIcon = Icons.Filled.Done,
            fabText = "Done",
            fabShows = true
        )
    )

    val state by mutator.state.collectAsState()

    LazyColumn {
        items(
            items = state.permissions,
            key = { it.permission },
            itemContent = { (permission, name, granted) ->
                Column {
                    StrokedBox(
                        modifier = Modifier
                            .clickable {
                                mutator.accept(permission)
                            }
                            .fillMaxWidth(),
                        content = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(0.35F),
                                    text = name
                                )
                                StrokedBox(
                                    content = {
                                        Spacer(
                                            modifier = Modifier
                                                .padding(1.dp)
                                                .fillMaxHeight()
                                        )
                                    }
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = granted.toString()
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.padding(1.dp))
                }
            }
        )
    }

}

@Composable
private fun StrokedBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        border = BorderStroke(
            width = 1.dp,
            color = androidx.compose.ui.graphics.Color.Black
        ),
        content = {
            content()
        }
    )
}
