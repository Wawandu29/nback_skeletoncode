package mobappdev.example.nback_cimpl.ui.components

// Location: ui/components/GameComponents.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GridDisplay(
    modifier: Modifier = Modifier,
    position: Int,
    gridSize: Int = 3
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        items(gridSize * gridSize) { index ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .background(
                        if (index == position)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface
                    )
            )
        }
    }
}

@Composable
fun GameControls(
    onStartGame: () -> Unit,
    onMatchPressed: () -> Unit,
    isGameActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Button(
            onClick = onStartGame,
            enabled = !isGameActive
        ) {
            Text("Start Game")
        }

        Button(
            onClick = onMatchPressed,
            enabled = isGameActive
        ) {
            Text("Match")
        }
    }
}

@Composable
fun GameProgress(
    currentEvent: Int,
    totalEvents: Int,
    correctResponses: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Event: $currentEvent/$totalEvents",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Correct: $correctResponses",
            style = MaterialTheme.typography.titleMedium
        )
    }
}