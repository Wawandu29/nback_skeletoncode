package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import android.content.Context
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val correctAnswers: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int


    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkMatch()
    val eventInterval: Long
    val numberOfEvents: Int
    val currentIndex: Int
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository,
    context: Context
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val _eventInterval: Long = 2000L  // 2000 ms (2s)
    override val eventInterval: Long
        get() = _eventInterval

    private val _numberOfEvents: Int = 20
    override val numberOfEvents: Int
        get() = _numberOfEvents

    private val _correctAnswers =  MutableStateFlow(0)
    override val correctAnswers: StateFlow<Int>
        get() = _correctAnswers

    // nBack is currently hardcoded
    override val nBack: Int = 2

    private var job: Job? = null  // coroutine job for the game event

    private val gridSize = 9 // Taille de la grille 3x3

    private var _currentIndex: Int = 0
    override val currentIndex: Int
        get() = _currentIndex
    private var matchesRegistered = mutableSetOf<Int>()
    private var events = emptyArray<Int>()
    private var isGameRunning = false
    private var lastMatch = false
    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("GameVM", "Langue non supportée")
                }
            } else {
                Log.e("GameVM", "Initialisation de TextToSpeech échouée")
            }
        })
    }


    private val nBackHelper = NBackHelper()  // Helper that generate the event array

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        //ajout
        isGameRunning = true
        _score.value = 0
        _correctAnswers.value = 0
        matchesRegistered.clear()

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(10, 9, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame()
            }
            // Todo: update the highscore (done)
            if (_score.value > _highscore.value) {
                _highscore.value = _score.value
                // Save the new highscore to persistent storage
                userPreferencesRepository.saveHighScore(_score.value)
            }
        }
    }

    override fun checkMatch() {
        if (!isGameRunning || currentIndex < nBack || currentIndex in matchesRegistered) {
            return
        }

        // Vérifie si la position actuelle correspond à celle d'il y a n positions
        val isMatch = events[currentIndex] == events[currentIndex - nBack]
        matchesRegistered.add(currentIndex)

        // update score
        if (isMatch) {
            _score.value += 1
            _correctAnswers.value += 1
            viewModelScope.launch {
                if (_score.value > _highscore.value) {
                    _highscore.value = _score.value
                    userPreferencesRepository.saveHighScore(_score.value)
                }
            }
        } else {
            _score.value = maxOf(0, _score.value - 1) // Évite les scores négatifs
        }
    }
    private fun getLetterForPosition(position: Int): String {
        val alphabet = "abcdefghijklmnopqrstuvwxyz"
        println(position)
        return alphabet[position-1 % alphabet.length].toString()
    }


    private suspend fun runAudioGame() {
        _currentIndex = 0

        try {
            events.forEachIndexed { index, position ->
                _currentIndex = index

                // Définir la lettre actuelle dans l'état du jeu
                val currentLetter = getLetterForPosition(position)
                _gameState.value = _gameState.value.copy(
                    eventValue = position,
                    currentLetter = currentLetter,
                    isActive = true
                )

                // Jouer l'audio de la lettre
                println(currentLetter)
//                playAudioForLetter(currentLetter)
                textToSpeech?.speak(currentLetter, TextToSpeech.QUEUE_FLUSH, null, null)

                // Attendre la durée de l'événement
                delay(eventInterval)

                // Effacer la lettre actuelle de l'état du jeu
                _gameState.value = _gameState.value.copy(
                    eventValue = -1,
                    currentLetter = null,
                    isActive = false
                )

                // delay between events
                delay(500)
            }
        } catch (e: Exception) {
            // manage potential errors
            Log.e("GameVM", "Error in runAudioGame(): $e")
        } finally {
            // Réinitialization
//            _correctAnswers = MutableStateFlow(0)
            isGameRunning = false
            _gameState.value = _gameState.value.copy(
                eventValue = -1,
                currentLetter = null,
                isActive = false
            )
        }
    }

    private suspend fun runVisualGame() {
        _currentIndex = 0

        try {
            events.forEachIndexed { index, position ->
                _currentIndex = index

                // Met à jour la position active dans la grille
                _gameState.value = _gameState.value.copy(
                    eventValue = position,
                    isActive = true
                )

                delay(eventInterval - 500) // Affiche pendant 1.5 secondes

                // Efface la position
                _gameState.value = _gameState.value.copy(
                    eventValue = -1,
                    isActive = false
                )

                delay(500) // Pause de 0.5 seconde entre les événements
            }
        } finally {
            // Réinitialise l'état à la fin du jeu
//            _correctAnswers.value = 0
            isGameRunning = false
            _gameState.value = _gameState.value.copy(
                eventValue = -1,
                isActive = false
            )
        }
    }


        private fun runAudioVisualGame(){
        // Todo: Make work for Higher grade
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository, application.baseContext)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val isActive: Boolean = false,
    val currentLetter: String? = null
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2
    override val eventInterval: Long
        get() = 2000L
    override val numberOfEvents: Int
        get() = 20
    override val currentIndex: Int
        get() = 0
    override val correctAnswers: StateFlow<Int>
        get() = MutableStateFlow(0).asStateFlow()

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkMatch() {
    }
}