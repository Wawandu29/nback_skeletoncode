package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import androidx.navigation.NavController

@Composable
fun GameScreen(
    vm: GameViewModel,
    navController: NavController
) {
    val gameState by vm.gameState.collectAsState()
//    println(gameState.gameType == GameType.Audio)
    val score by vm.score.collectAsState()
    val correctAnswersValue by vm.correctAnswers.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Score et informations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "N-back: ${vm.nBack}",
                    style = MaterialTheme.typography.headlineMedium
                )

            }
            Column(modifier = Modifier //to edit
                .fillMaxWidth()
                .padding(16.dp),
            ){
                Text(
                    text = "Current event = ${vm.currentIndex + 1}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(text = "Correct answers = $correctAnswersValue",
                    style = MaterialTheme.typography.headlineMedium)
            }

            // 3x3 grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..2) {
                                val position = row * 3 + col
                                // Vérifiez si ce carré doit être actif
                                val isActive = position == gameState.eventValue
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            color = if (isActive && gameState.gameType == GameType.Visual) {
                                                Color.Blue // Active
                                            } else {
                                                Color.LightGray // Inactive
                                            },
                                            shape = MaterialTheme.shapes.medium
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Boutons de contrôle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = vm::startGame,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Start")
                }
                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Home")
                }
                Button(
                    onClick = vm::checkMatch,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Match")
                }
            }
        }
    }
}