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
    val slices: List<Slice<*>> = slices()
)

object PlaygroundRoute : Route {
    @Composable
    override fun render() {
        val uiStateHolder = AppDependencies.current.globalUiStateHolder
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

        val scope = rememberCoroutineScope()
        val stateHolder = remember {
            scopedStateHolder(
                scope = scope,
                initialState = State(),
                transform = { flow: Flow<Mutation<State>> -> flow }
            )
        }

        val state by stateHolder.state.collectAsState()
        val selectedSlice = state.selectedSlice


        LazyColumn {
            items(
                items = state.slices,
                key = { it.name },
                itemContent = { slice ->
                    slice.apply {
                        getter
                    }
                    Row() {
                        Text(text = slice.name)
                        Text(
                            text = slice.nameTransformer(slice.getter(uiStateHolder.state.value))
                        )
                    }
                }
            )
        }

        if (selectedSlice != null) AlertDialog(
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
                        items = selectedSlice.options,
                        key = { it.toString() },
                        itemContent = { option ->
                            Text(
                                modifier = Modifier.clickable {
                                    uiStateHolder.accept {
                                        selectedSlice.setter(this, option)
                                    }
                                },
                                text = selectedSlice.nameTransformer(option).toString()
                            )
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

private fun <T> Slice<T>.optionNames() = options.map(nameTransformer).toTypedArray()

private val Int.stringHex: CharSequence get() = "⦿".color(this) + "#${Integer.toHexString(this)}"


private fun slices() = listOf(
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
        getter = UiState::statusBarColor
    ) {
        copy(statusBarColor = it)
    },
    Slice(
        name = "Is immersive",
        options = listOf(true, false),
        getter = UiState::isImmersive
    ) {
        copy(isImmersive = it)
    },
    Slice(
        name = "Has light status bar icons",
        options = listOf(true, false),
        getter = UiState::lightStatusBar
    ) {
        copy(lightStatusBar = it)
    },
    Slice(
        name = "Toolbar title",
        options = listOf(
            "Ui State Playground",
            "Reality can be whatever I want",
            "I am inevitable",
        ),
        getter = UiState::toolbarTitle
    ) {
        copy(toolbarTitle = it)
    },
    Slice(
        name = "Tool bar shows",
        options = listOf(true, false),
        getter = UiState::toolbarShows
    ) {
        copy(toolbarShows = it)
    },
    Slice(
        name = "Tool bar overlaps",
        options = listOf(true, false),
        getter = UiState::toolbarOverlaps
    ) {
        copy(toolbarOverlaps = it)
    },
    Slice(
        name = "FAB shows",
        options = listOf(true, false),
        getter = UiState::fabShows
    ) {
        copy(fabShows = it)
    },
    Slice(
        name = "FAB icon",
        nameTransformer = { "Icon" },
        options = listOf(
            Icons.Default.Done,
        ),
        getter = { it.fabIcon }
    ) {
        copy(fabIcon = it)
    },
    Slice(
        name = "FAB text",
        options = listOf("Hello", "Hi", "How do you do"),
        getter = UiState::fabText
    ) {
        copy(fabText = it)
    },
    Slice(
        name = "FAB extended",
        options = listOf(true, false),
        getter = UiState::fabExtended
    ) {
        copy(fabExtended = it)
    },
    Slice(
        name = "Bottom nav shows",
        options = listOf(true, false),
        getter = UiState::showsBottomNav
    ) {
        copy(showsBottomNav = it)
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
        getter = UiState::navBarColor
    ) {
        copy(navBarColor = it)
    },
    Slice(
        name = "Inset Flags",
        options = listOf(
            InsetFlags.ALL,
            InsetFlags.NO_TOP,
            InsetFlags.NO_BOTTOM,
            InsetFlags.NONE
        ),
        getter = UiState::insetFlags
    ) {
        copy(insetFlags = it)
    }
)

data class Slice<T>(
    val name: String,
    val options: List<T>,
    val nameTransformer: (T) -> CharSequence = Any?::toString,
    val getter: (UiState) -> T,
    val setter: UiState.(T) -> UiState
)