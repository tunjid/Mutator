package src.tunjid.sample.ui.playground

import android.graphics.Color
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
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
import androidx.core.content.getSystemService
import com.tunjid.mutator.Mutation
import com.tunjid.mutator.StateHolder
import com.tunjid.mutator.accept
import com.tunjid.mutator.scopedStateHolder
import kotlinx.coroutines.flow.Flow
import src.tunjid.sample.globalui.InsetFlags
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
                Row {
                    Text(text = slice.name)
                    Text(text = slice.selectedText)
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

private val Int.stringHex: String get() = "⦿" + "#${Integer.toHexString(this)}"


private fun UiState.slices() = listOf(
    Slice(
        name = "Status bar color",
        nameTransformer = Int::stringHex,
        options = listOf(
            Color.TRANSPARENT,
            Color.parseColor("#80000000"),
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE
        ),
        selectedText = statusBarColor.stringHex,
    ) {
        Mutation { copy(statusBarColor = it) }
    },
    Slice(
        name = "Is immersive",
        options = listOf(true, false),
        selectedText = isImmersive.toString(),
    ) {
        Mutation { copy(isImmersive = it) }
    },
    Slice(
        name = "Has light status bar icons",
        options = listOf(true, false),
        selectedText = lightStatusBar.toString(),
    ) {
        Mutation { copy(lightStatusBar = it) }
    },
    Slice(
        name = "Toolbar title",
        options = listOf(
            "Ui State Playground",
            "Reality can be whatever I want",
            "I am inevitable",
        ),
        selectedText = toolbarTitle.toString(),
    ) {
        Mutation { copy(toolbarTitle = it) }
    },
    Slice(
        name = "Tool bar shows",
        options = listOf(true, false),
        selectedText = toolbarShows.toString(),
    ) {
        Mutation { copy(toolbarShows = it) }
    },
    Slice(
        name = "Tool bar overlaps",
        options = listOf(true, false),
        selectedText = toolbarOverlaps.toString(),
    ) {
        Mutation { copy(toolbarOverlaps = it) }
    },
    Slice(
        name = "FAB shows",
        options = listOf(true, false),
        selectedText = fabShows.toString(),
    ) {
        Mutation { copy(fabShows = it) }
    },
    Slice(
        name = "FAB icon",
        nameTransformer = { "Icon" },
        options = listOf(
            Icons.Default.Done,
        ),
        selectedText = "Umm",
    ) {
        Mutation { copy(fabIcon = it) }
    },
    Slice(
        name = "FAB text",
        options = listOf("Hello", "Hi", "How do you do"),
        selectedText = fabText.toString(),
    ) {
        Mutation { copy(fabText = it) }
    },
    Slice(
        name = "FAB extended",
        options = listOf(true, false),
        selectedText = fabExtended.toString(),
    ) {
        Mutation { copy(fabExtended = it) }
    },
    Slice(
        name = "Bottom nav shows",
        options = listOf(true, false),
        selectedText = showsBottomNav.toString(),
    ) {
        Mutation { copy(showsBottomNav = it) }
    },
    Slice(
        name = "Nav bar color",
        nameTransformer = Int::stringHex,
        options = listOf(
            Color.TRANSPARENT,
            Color.parseColor("#80000000"),
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE
        ),
        selectedText = navBarColor.toString(),
    ) {
        Mutation { copy(navBarColor = it) }
    },
    Slice(
        name = "Inset Flags",
        options = listOf(
            InsetFlags.ALL,
            InsetFlags.NO_TOP,
            InsetFlags.NO_BOTTOM,
            InsetFlags.NONE
        ),
        selectedText = insetFlags.toString(),
    ) {
        Mutation { copy(insetFlags = it) }
    }
)

data class Slice<T : Any>(
    val name: String,
    val options: List<T>,
    val nameTransformer: (T) -> String = Any?::toString,
    val selectedText: String,
    val setter: (T) -> Mutation<UiState>
) {
    val select: (Int) -> Mutation<UiState> = { index -> setter(options[index]) }
    val optionName: (Int) -> String = { index -> nameTransformer(options[index]) }
}
