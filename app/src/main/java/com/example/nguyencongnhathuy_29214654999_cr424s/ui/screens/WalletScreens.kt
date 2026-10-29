@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.nguyencongnhathuy_29214654999_cr424s.ui.screens

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import com.example.nguyencongnhathuy_29214654999_cr424s.audio.GameAudio
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private enum class Mode { LOCAL, AI }
private enum class Difficulty { EASY, MEDIUM, HARD }
private enum class Skin(val x: String, val o: String) {
    CLASSIC("X", "O"), HEARTS("♥", "★"), FIRE("✦", "◆")
}
private enum class Theme(val accent: Color, val background: Color) {
    OCEAN(Color(0xFF1976D2), Color(0xFFF4F8FF)),
    SUNSET(Color(0xFFE85D04), Color(0xFFFFF5ED)),
    FOREST(Color(0xFF2E7D32), Color(0xFFF1FAF2)),
    GALAXY(Color(0xFF7C4DFF), Color(0xFFF6F2FF))
}

private data class Move(val index: Int, val player: Char)
private data class Result(val winner: Char?, val line: List<Int> = emptyList(), val draw: Boolean = false)

private val XColor = Color(0xFF3B82F6)
private val OColor = Color(0xFFEF476F)
private val BoardLight = Color(0xFFF4F0E8)
private val BoardDark = Color(0xFF1F2937)
private val HighlightColor = Color(0xFFFFC857)

@Composable
fun NeonSurvivorScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences("caro_game", Context.MODE_PRIVATE) }
    var started by remember { mutableStateOf(false) }
    var size by remember { mutableIntStateOf(10) }
    var mode by remember { mutableStateOf(Mode.LOCAL) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var skin by remember { mutableStateOf(Skin.CLASSIC) }
    var dark by remember { mutableStateOf(false) }
    var theme by remember { mutableStateOf(Theme.OCEAN) }
    var musicEnabled by remember { mutableStateOf(prefs.getBoolean("music", true)) }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
    var secondsPerTurn by remember { mutableIntStateOf(20) }
    var board by remember { mutableStateOf(List(size * size) { ' ' }) }
    var current by remember { mutableStateOf('X') }
    var result by remember { mutableStateOf<Result?>(null) }
    var timer by remember { mutableIntStateOf(secondsPerTurn) }
    var history by remember { mutableStateOf(emptyList<Move>()) }
    var xWins by remember { mutableIntStateOf(prefs.getInt("xWins", 0)) }
    var oWins by remember { mutableIntStateOf(prefs.getInt("oWins", 0)) }
    var draws by remember { mutableIntStateOf(prefs.getInt("draws", 0)) }
    var games by remember { mutableIntStateOf(prefs.getInt("games", 0)) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    fun newGame() {
        board = List(size * size) { ' ' }
        current = 'X'
        result = null
        timer = secondsPerTurn
        history = emptyList()
        started = true
    }

    fun finish(outcome: Result) {
        if (result != null) return
        result = outcome
        games++
        prefs.edit().putInt("games", games).apply()
        if (outcome.winner != null && soundEnabled) GameAudio.playWin()
        if (outcome.winner != null) {
            val winner = outcome.winner
            if (winner == 'X') xWins++ else oWins++
            prefs.edit().putInt("xWins", xWins).putInt("oWins", oWins).apply()
        } else if (outcome.draw) {
            draws++
            prefs.edit().putInt("draws", draws).apply()
        }
    }

    fun play(index: Int) {
        if (result != null || board[index] != ' ' || (mode == Mode.AI && current == 'O')) return
        val next = board.toMutableList()
        if (soundEnabled) GameAudio.playMove()
        if (vibrationEnabled) vibrate(context)
        next[index] = current
        val moves = history + Move(index, current)
        val checked = checkResult(next, index, current, size)
        board = next
        history = moves
        if (checked.winner != null || checked.draw) finish(checked)
        else {
            current = if (current == 'X') 'O' else 'X'
            timer = secondsPerTurn
        }
    }

    LaunchedEffect(started, current, result, mode, size) {
        if (!started || result != null) return@LaunchedEffect
        if (mode == Mode.AI && current == 'O') {
            delay(450)
            if (result == null) {
                val aiIndex = chooseAiMove(board, size, difficulty)
                if (aiIndex >= 0) {
                    val next = board.toMutableList()
                    next[aiIndex] = 'O'
                    val checked = checkResult(next, aiIndex, 'O', size)
                    board = next
                    history = history + Move(aiIndex, 'O')
                    if (checked.winner != null || checked.draw) finish(checked)
                    else {
                        current = 'X'
                        timer = secondsPerTurn
                    }
                }
            }
        }
    }

    LaunchedEffect(started, current, result, secondsPerTurn) {
        if (!started || result != null) return@LaunchedEffect
        while (timer > 0 && result == null) {
            delay(1_000)
            timer--
        }
        if (timer == 0 && result == null) finish(Result(if (current == 'X') 'O' else 'X'))
    }

    LaunchedEffect(musicEnabled) { GameAudio.setMusicEnabled(musicEnabled) }
    DisposableEffect(Unit) {
        onDispose { GameAudio.setMusicEnabled(false) }
    }
    Surface(Modifier.fillMaxSize(), color = if (dark) Color(0xFF101827) else theme.background) {
        if (!started) {
            HomeScreen(
                mode = mode,
                difficulty = difficulty,
                size = size,
                skin = skin,
                dark = dark,
                seconds = secondsPerTurn,
                xWins = xWins,
                oWins = oWins,
                draws = draws,
                games = games,
                theme = theme,
                musicEnabled = musicEnabled,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                onMode = { mode = it },
                onDifficulty = { difficulty = it },
                onSize = { size = it },
                onSkin = { skin = it },
                onDark = { dark = it },
                onTheme = { theme = it },
                onMusic = { musicEnabled = it; prefs.edit().putBoolean("music", it).apply() },
                onSound = { soundEnabled = it; prefs.edit().putBoolean("sound", it).apply() },
                onVibration = { vibrationEnabled = it; prefs.edit().putBoolean("vibration", it).apply() },
                onSeconds = { secondsPerTurn = it },
                onStart = ::newGame
            )
        } else {
            GameScreen(
                board = board,
                size = size,
                current = current,
                timer = timer,
                maxTimer = secondsPerTurn,
                mode = mode,
                skin = skin,
                dark = dark,
                result = result,
                history = history,
                xWins = xWins,
                oWins = oWins,
                draws = draws,
                onTap = ::play,
                onNewGame = ::newGame,
                onHome = { started = false },
                onUndo = {
                    if (history.isNotEmpty() && result == null) {
                        val count = if (mode == Mode.AI && history.size >= 2) 2 else 1
                        val kept = history.dropLast(min(count, history.size))
                        board = MutableList(size * size) { ' ' }.also { fresh ->
                            kept.forEach { fresh[it.index] = it.player }
                        }
                        history = kept
                        current = if (kept.size % 2 == 0) 'X' else 'O'
                        timer = secondsPerTurn
                    }
                },
                onHistory = { showHistory = true },
                onSettings = { showSettings = true },
                accent = theme.accent
            )
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Cài đặt ván chơi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Bàn cờ: ${size} x ${size}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 10, 15).forEach {
                            FilterChip(size == it, { size = it }, label = { Text("${it}x$it") })
                        }
                    }
                    Text("Thời gian mỗi lượt: ${secondsPerTurn}s")
                    Slider(secondsPerTurn.toFloat(), { secondsPerTurn = it.toInt() }, valueRange = 10f..60f, steps = 4)
                    Text("Chủ đề")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!dark, { dark = false }, label = { Text("Sáng") })
                        FilterChip(dark, { dark = true }, label = { Text("Tối") })
                    }
                    Text("Âm thanh")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(musicEnabled, { musicEnabled = !musicEnabled; prefs.edit().putBoolean("music", musicEnabled).apply() }, label = { Text("Nhạc nền") })
                        FilterChip(soundEnabled, { soundEnabled = !soundEnabled; prefs.edit().putBoolean("sound", soundEnabled).apply() }, label = { Text("Hiệu ứng") })
                        FilterChip(vibrationEnabled, { vibrationEnabled = !vibrationEnabled; prefs.edit().putBoolean("vibration", vibrationEnabled).apply() }, label = { Text("Rung") })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Đóng") } }
        )
    }
    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            title = { Text("Lịch sử nước đi") },
            text = {
                LazyColumn(Modifier.height(260.dp)) {
                    items(history.reversed()) { move ->
                        Text("${if (move.player == 'X') skin.x else skin.o}: ${coordinate(move.index, size)}", modifier = Modifier.padding(4.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showHistory = false }) { Text("Đóng") } }
        )
    }
}

@Composable
private fun HomeScreen(
    mode: Mode,
    difficulty: Difficulty,
    size: Int,
    skin: Skin,
    dark: Boolean,
    seconds: Int,
    xWins: Int,
    oWins: Int,
    draws: Int,
    games: Int,
    theme: Theme,
    musicEnabled: Boolean,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onMode: (Mode) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onSize: (Int) -> Unit,
    onSkin: (Skin) -> Unit,
    onDark: (Boolean) -> Unit,
    onTheme: (Theme) -> Unit,
    onMusic: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onVibration: (Boolean) -> Unit,
    onSeconds: (Int) -> Unit,
    onStart: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("CARO", color = XColor, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
            Text("ARENA", color = OColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Caro Việt Nam • Offline • Vui cùng bạn bè", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (dark) Color(0xFF1F2937) else Color.White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Chế độ chơi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(mode == Mode.LOCAL, { onMode(Mode.LOCAL) }, label = { Text("2 người") })
                        FilterChip(mode == Mode.AI, { onMode(Mode.AI) }, label = { Text("Đấu máy") })
                    }
                    if (mode == Mode.AI) {
                        Text("Độ khó")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Difficulty.values().forEach { level ->
                                FilterChip(difficulty == level, { onDifficulty(level) }, label = { Text(levelLabel(level)) })
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tùy chỉnh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Kích thước bàn cờ")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 10, 15).forEach { boardSize ->
                            FilterChip(size == boardSize, { onSize(boardSize) }, label = { Text("${boardSize}x$boardSize") })
                        }
                    }
                    Text("Skin quân cờ")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Skin.values().forEach { value ->
                            FilterChip(skin == value, { onSkin(value) }, label = { Text("${value.x} ${value.o}") })
                        }
                        Text("Bộ màu giao diện")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Theme.values().forEach { value ->
                                FilterChip(theme == value, { onTheme(value) }, label = { Text(value.name.lowercase().replaceFirstChar { it.uppercase() }) })
                            }
                        }
                    }
                    Text("Thời gian mỗi lượt: ${seconds}s")
                    Slider(seconds.toFloat(), { onSeconds(it.toInt()) }, valueRange = 10f..60f, steps = 4)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Giao diện tối")
                        FilterChip(dark, { onDark(!dark) }, label = { Text(if (dark) "Bật" else "Tắt") })
                    }
                    Text("Âm thanh & trải nghiệm")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(musicEnabled, { onMusic(!musicEnabled) }, label = { Text("Nhạc") })
                        FilterChip(soundEnabled, { onSound(!soundEnabled) }, label = { Text("Âm thanh") })
                        FilterChip(vibrationEnabled, { onVibration(!vibrationEnabled) }, label = { Text("Rung") })
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Score("X", xWins, XColor)
                    Score("Hòa", draws, Color.Gray)
                    Score("O", oWins, OColor)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Thành tích", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (games >= 10) "🏆 Chiến binh bền bỉ: $games ván" else "Chơi thêm ${10 - games} ván để mở khóa thành tích")
                    Text(if (xWins + oWins >= 5) "⭐ Cao thủ: đã thắng ${xWins + oWins} ván" else "Thắng 5 ván để mở khóa Cao thủ", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Button(onClick = onStart, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                Text("BẮT ĐẦU VÁN MỚI", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Score(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color, fontWeight = FontWeight.Bold)
        Text("$value", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GameScreen(
    board: List<Char>,
    size: Int,
    current: Char,
    timer: Int,
    maxTimer: Int,
    mode: Mode,
    skin: Skin,
    dark: Boolean,
    result: Result?,
    history: List<Move>,
    xWins: Int,
    oWins: Int,
    draws: Int,
    onTap: (Int) -> Unit,
    onNewGame: () -> Unit,
    onHome: () -> Unit,
    onUndo: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
    , accent: Color
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caro Arena") },
                actions = { TextButton(onClick = onSettings) { Text("⚙") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Score("X", xWins, XColor)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(resultText(result, current), fontWeight = FontWeight.Bold, color = if (result == null) accent else OColor)
                    Text("⏱ ${timer}s", color = if (timer <= 5) OColor else MaterialTheme.colorScheme.onSurface)
                }
                Score("O", oWins, OColor)
            }
            LinearProgressIndicator({ timer.toFloat() / maxTimer.coerceAtLeast(1) }, Modifier.fillMaxWidth(), color = if (timer <= 5) OColor else accent)
            Spacer(Modifier.height(8.dp))
            Board(
                board = board,
                size = size,
                winningLine = result?.line ?: emptyList(),
                skin = skin,
                dark = dark,
                onTap = onTap
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onUndo, enabled = history.isNotEmpty() && result == null) { Text("↶ Undo") }
                OutlinedButton(onClick = onHistory) { Text("Lịch sử") }
                OutlinedButton(onClick = onNewGame) { Text("Ván mới") }
            }
            TextButton(onClick = onHome) { Text("← Về màn hình chính") }
            if (result != null) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (result.winner == 'X') XColor.copy(alpha = .12f) else OColor.copy(alpha = .12f))) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (result.draw) "HÒA!" else "🎉 ${result.winner} CHIẾN THẮNG!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Button(onClick = onNewGame) { Text("Chơi lại") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Board(board: List<Char>, size: Int, winningLine: List<Int>, skin: Skin, dark: Boolean, onTap: (Int) -> Unit) {
    val boardColor = if (dark) BoardDark else BoardLight
    Box(
        Modifier.fillMaxWidth().pointerInput(size) {
            detectTapGestures { offset ->
                val cell = minOf(this.size.width, this.size.height).toFloat() / size
                val col = (offset.x / cell).toInt().coerceIn(0, size - 1)
                val row = (offset.y / cell).toInt().coerceIn(0, size - 1)
                onTap(row * size + col)
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(minOf(380, 360).dp)) {
            val cell = minOf(this.size.width / size, this.size.height / size)
            for (i in 0..size) {
                drawLine(Color(0xFF9A8F7A), Offset(i * cell, 0f), Offset(i * cell, cell * size), 1f)
                drawLine(Color(0xFF9A8F7A), Offset(0f, i * cell), Offset(cell * size, i * cell), 1f)
            }
            board.forEachIndexed { index, value ->
                if (value != ' ') {
                    val row = index / size
                    val col = index % size
                    val center = Offset(col * cell + cell / 2, row * cell + cell / 2)
                    val color = if (value == 'X') XColor else OColor
                    if (value == 'X') {
                        drawLine(color, Offset(center.x - cell * .28f, center.y - cell * .28f), Offset(center.x + cell * .28f, center.y + cell * .28f), cell * .08f, StrokeCap.Round)
                        drawLine(color, Offset(center.x + cell * .28f, center.y - cell * .28f), Offset(center.x - cell * .28f, center.y + cell * .28f), cell * .08f, StrokeCap.Round)
                    } else {
                        drawCircle(color, cell * .3f, center, style = Stroke(width = cell * .08f))
                    }
                }
            }
            winningLine.forEach { index ->
                val row = index / size
                val col = index % size
                drawCircle(HighlightColor, cell * .42f, Offset(col * cell + cell / 2, row * cell + cell / 2), style = Stroke(width = cell * .06f))
            }
        }
    }
}

private fun levelLabel(level: Difficulty) = when (level) {
    Difficulty.EASY -> "Dễ"
    Difficulty.MEDIUM -> "Vừa"
    Difficulty.HARD -> "Khó"
}

private fun resultText(result: Result?, current: Char) = when {
    result?.draw == true -> "Hòa"
    result?.winner != null -> "${result.winner} thắng"
    else -> "Lượt của $current"
}

private fun coordinate(index: Int, size: Int): String {
    val row = index / size
    val col = index % size
    return "${('A'.code + col).toChar()}${row + 1}"
}

private fun checkResult(board: List<Char>, index: Int, player: Char, size: Int): Result {
    val target = if (size == 3) 3 else 5
    val row = index / size
    val col = index % size
    val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
    directions.forEach { (dr, dc) ->
        val line = mutableListOf(index)
        for (sign in listOf(1, -1)) {
            var r = row + dr * sign
            var c = col + dc * sign
            while (r in 0 until size && c in 0 until size && board[r * size + c] == player) {
                line.add(r * size + c)
                r += dr * sign
                c += dc * sign
            }
        }
        if (line.size >= target) return Result(player, line)
    }
    return if (board.none { it == ' ' }) Result(null, draw = true) else Result(null)
}

private fun chooseAiMove(board: List<Char>, size: Int, difficulty: Difficulty): Int {
    val empty = board.indices.filter { board[it] == ' ' }
    if (empty.isEmpty()) return -1
    if (difficulty == Difficulty.EASY) return empty.random()
    val win = findWinningMove(board, size, 'O')
    if (win >= 0) return win
    val block = findWinningMove(board, size, 'X')
    if (block >= 0) return block
    if (difficulty == Difficulty.HARD && size == 3) return minimaxMove(board)
    val center = (size * size) / 2
    if (board[center] == ' ') return center
    return empty.maxByOrNull { index -> adjacentScore(board, index, size, 'O') } ?: empty.random()
}

private fun findWinningMove(board: List<Char>, size: Int, player: Char): Int {
    board.indices.filter { board[it] == ' ' }.forEach { index ->
        val copy = board.toMutableList()
        copy[index] = player
        if (checkResult(copy, index, player, size).winner == player) return index
    }
    return -1
}

private fun adjacentScore(board: List<Char>, index: Int, size: Int, player: Char): Int {
    val row = index / size
    val col = index % size
    var score = 0
    for (r in max(0, row - 1)..min(size - 1, row + 1)) {
        for (c in max(0, col - 1)..min(size - 1, col + 1)) {
            if (board[r * size + c] == player) score++
        }
    }
    return score
}

private fun minimaxMove(board: List<Char>): Int {
    var bestScore = Int.MIN_VALUE
    var bestIndex = board.indexOfFirst { it == ' ' }
    board.indices.filter { board[it] == ' ' }.forEach { index ->
        val copy = board.toMutableList()
        copy[index] = 'O'
        val score = minimax(copy, false)
        if (score > bestScore) {
            bestScore = score
            bestIndex = index
        }
    }
    return bestIndex
}

private fun minimax(board: List<Char>, maximizing: Boolean): Int {
    val result = board.indices.firstNotNullOfOrNull { index ->
        if (board[index] != ' ') null else null
    }
    if (checkAnyWinner(board, 'O')) return 10
    if (checkAnyWinner(board, 'X')) return -10
    if (board.none { it == ' ' }) return 0
    val scores = board.indices.filter { board[it] == ' ' }.map { index ->
        val copy = board.toMutableList()
        copy[index] = if (maximizing) 'O' else 'X'
        minimax(copy, !maximizing)
    }
    return if (maximizing) scores.maxOrNull() ?: 0 else scores.minOrNull() ?: 0
}

private fun checkAnyWinner(board: List<Char>, player: Char): Boolean {
    val lines = listOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), intArrayOf(0, 4, 8), intArrayOf(2, 4, 6))
    return lines.any { line -> line.all { board[it] == player } }
}

private fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(35)
    }
}
