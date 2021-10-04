package src.tunjid.sample.ui.playground

import android.view.inputmethod.InputMethodManager
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
import androidx.compose.material.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.StateHolder
import com.tunjid.mutator.accept
import com.tunjid.mutator.scopedStateHolder
import kotlinx.coroutines.flow.Flow
import src.tunjid.sample.globalui.UiState
import src.tunjid.sample.nav.Route
import src.tunjid.sample.ui.AppDependencies
import src.tunjid.sample.ui.InitialUiState

data class State(
    val selectedSlice: Slice<*>? = null,
    val slices: List<Slice<*>> = listOf()
)

object PlaygroundRoute : Route {
    @Composable
    override fun Render() {
        val scope = rememberCoroutineScope()
        val globalUiStateHolder = AppDependencies.current.globalUiStateHolder

        PlaygroundScreen(
            globalUiStateHolder = globalUiStateHolder,
            stateHolder = remember {
                scopedStateHolder(
                    scope = scope,
                    initialState = State(),
                    transform = { flow: Flow<Mutation<State>> -> flow }
                )
            }
        )
    }
}

@Composable
private fun PlaygroundScreen(
    globalUiStateHolder: StateHolder<Mutation<UiState>, UiState>,
    stateHolder: StateHolder<Mutation<State>, State>
) {
    val imm = LocalContext.current.getSystemService<InputMethodManager>()
    val showKeyBoard = {
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    InitialUiState(
        UiState(
            toolbarMenuClickListener = {
                showKeyBoard()
            },
            toolbarTitle = "Playground",
            toolbarOverlaps = false,
            toolbarShows = true,
            showsBottomNav = true,
            fabIcon = Icons.Filled.Done,
            fabText = "Done",
            fabShows = true
        )
    )

    val globalUiState by globalUiStateHolder.state.collectAsState()
    val state by stateHolder.state.collectAsState()
    val selectedSlice = state.selectedSlice

    LazyColumn {
        items(
            items = globalUiState.slices(),
            key = { it.name },
            itemContent = { slice ->
                Column {
                    StrokedBox(
                        modifier = Modifier
                            .clickable {
                                stateHolder.accept { copy(selectedSlice = slice) }
                            }
                            .fillMaxWidth(),
                        content = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(0.35F),
                                    text = slice.name
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
                                    text = slice.selectedText
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.padding(1.dp))
                }
            }
        )
    }

    if (selectedSlice != null) ChangeDialog(
        stateHolder = stateHolder,
        selectedSlice = selectedSlice,
        onChange = globalUiStateHolder.accept
    )
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

@Composable
private fun ChangeDialog(
    stateHolder: StateHolder<Mutation<State>, State>,
    selectedSlice: Slice<*>,
    onChange: (Mutation<UiState>) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onCloseRequest.
            stateHolder.accept { copy(selectedSlice = null) }
        },
        title = {
            Text(text = "Dialog Title")
        },
        text = {

            LazyColumn {
                items(
                    count = selectedSlice.options.size,
                    key = { it.toString() },
                    itemContent = { index ->
                        Text(
                            modifier = Modifier.clickable {
                                onChange(selectedSlice.select(index))
                                stateHolder.accept { copy(selectedSlice = null) }
                            },
                            text = selectedSlice.optionName(index)
                        )
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
