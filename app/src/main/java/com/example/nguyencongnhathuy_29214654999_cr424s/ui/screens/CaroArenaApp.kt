@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.nguyencongnhathuy_29214654999_cr424s.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import com.example.nguyencongnhathuy_29214654999_cr424s.audio.GameAudio
import kotlin.math.max
import kotlin.random.Random

private enum class Screen { LOGIN, HOME, MODES, HEROES, SHOP, MATCH, GAME, RESULT, RANK, SETTINGS }
private enum class ArenaMode(val title: String, val description: String) {
    LOCAL("PvP cục bộ", "Hai người chơi chung thiết bị"),
    AI("Đấu với máy", "Bot Easy / Medium / Hard"),
    CAMPAIGN("Cốt truyện", "Đánh Boss qua từng màn"),
    SURVIVAL("Sinh tồn", "Leo tháp thử thách vô tận"),
    ONLINE("Phòng online", "Tạo hoặc nhập mã phòng")
}
private data class Hero(val name: String, val element: String, val skill: String, val price: Int, val icon: String)
private data class ArenaMove(val index: Int, val player: Char)

private val heroes = listOf(
    Hero("Ember", "Lửa", "Phá hủy một ô đối thủ", 0, "🔥"),
    Hero("Frost", "Băng", "Đóng băng ô trong 1 lượt", 500, "❄"),
    Hero("Volt", "Sấm", "Đổi chiều một quân", 900, "⚡"),
    Hero("Terra", "Đất", "Khiên chặn phá hủy", 1400, "🪨")
)

@Composable
fun CaroArenaApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("caro_arena_profile", Context.MODE_PRIVATE) }
    var screen by remember { mutableStateOf(if (prefs.getBoolean("onboarded", false)) Screen.HOME else Screen.LOGIN) }
    var playerName by remember { mutableStateOf(prefs.getString("name", "Guest") ?: "Guest") }
    var level by remember { mutableIntStateOf(prefs.getInt("level", 1)) }
    var exp by remember { mutableIntStateOf(prefs.getInt("exp", 0)) }
    var gold by remember { mutableIntStateOf(prefs.getInt("gold", 500)) }
    var gems by remember { mutableIntStateOf(prefs.getInt("gems", 20)) }
    var wins by remember { mutableIntStateOf(prefs.getInt("wins", 0)) }
    var losses by remember { mutableIntStateOf(prefs.getInt("losses", 0)) }
    var draws by remember { mutableIntStateOf(prefs.getInt("draws", 0)) }
    var hero by remember { mutableStateOf(heroes.first()) }
    var selectedMode by remember { mutableStateOf(ArenaMode.AI) }
    var roomCode by remember { mutableStateOf("") }
    var music by remember { mutableStateOf(prefs.getBoolean("music", true)) }
    var sound by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
    var graphics by remember { mutableIntStateOf(prefs.getInt("graphics", 2)) }

    fun saveProfile() {
        prefs.edit().putString("name", playerName).putBoolean("onboarded", true)
            .putInt("level", level).putInt("exp", exp).putInt("gold", gold)
            .putInt("gems", gems).putInt("wins", wins).putInt("losses", losses).putInt("draws", draws)
            .putBoolean("music", music).putBoolean("sound", sound).putInt("graphics", graphics).apply()
    }

    fun finishGame(win: Boolean?) {
        when (win) {
            true -> { wins++; gold += 120; exp += 100 }
            false -> { losses++; gold += 20; exp += 25 }
            null -> { draws++; gold += 50; exp += 50 }
        }
        if (exp >= level * 300) { exp -= level * 300; level++ ; gems += 5 }
        saveProfile()
        screen = Screen.RESULT
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF6F4FF)) {
        when (screen) {
            Screen.LOGIN -> LoginScreen(playerName, { playerName = it }, { saveProfile(); screen = Screen.HOME })
            Screen.HOME -> HomeScreen(playerName, level, exp, gold, gems, wins, losses, draws, hero,
                { screen = Screen.MODES }, { screen = Screen.HEROES }, { screen = Screen.SHOP },
                { screen = Screen.RANK }, { screen = Screen.SETTINGS })
            Screen.MODES -> ModeScreen(selectedMode, { selectedMode = it }, { screen = if (it == ArenaMode.ONLINE) Screen.MATCH else Screen.GAME }, { screen = Screen.HOME })
            Screen.HEROES -> HeroesScreen(hero, gold, { candidate ->
                if (candidate.price == 0 || gold >= candidate.price) { hero = candidate; if (candidate.price > 0) { gold -= candidate.price; saveProfile() } }
            }, { screen = Screen.HOME })
            Screen.SHOP -> ShopScreen(gold, gems, { gold += 300; saveProfile() }, { gems += 10; saveProfile() }, { screen = Screen.HOME })
            Screen.MATCH -> MatchScreen(roomCode, { roomCode = it }, { screen = Screen.GAME }, { screen = Screen.HOME })
            Screen.GAME -> ArenaScreen(selectedMode, hero, sound, { finishGame(it) }, { screen = Screen.HOME })
            Screen.RESULT -> ResultScreen(wins, losses, draws, level, gold, exp, { screen = Screen.GAME }, { screen = Screen.HOME })
            Screen.RANK -> RankScreen(playerName, wins, { screen = Screen.HOME })
            Screen.SETTINGS -> SettingsScreen(music, sound, graphics, { music = it; GameAudio.setMusicEnabled(it); saveProfile() }, { sound = it; saveProfile() }, { graphics = it; saveProfile() }, { screen = Screen.HOME })
        }
    }
}

@Composable
private fun LoginScreen(name: String, onName: (String) -> Unit, onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("CARO", style = MaterialTheme.typography.displayMedium, color = Color(0xFF6750A4))
        Text("ARENA", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFE85D04))
        Spacer(Modifier.height(20.dp))
        Text("Chào mừng chiến binh!", style = MaterialTheme.typography.titleLarge)
        Text("Bạn có thể chơi ngay với tài khoản Guest.")
        androidx.compose.material3.OutlinedTextField(name, onName, label = { Text("Tên hiển thị") }, singleLine = true)
        Spacer(Modifier.height(14.dp))
        Button(onClick = onContinue, Modifier.fillMaxWidth()) { Text("VÀO SẢNH") }
    }
}

@Composable
private fun HomeScreen(name: String, level: Int, exp: Int, gold: Int, gems: Int, wins: Int, losses: Int, draws: Int, hero: Hero, onPlay: () -> Unit, onHeroes: () -> Unit, onShop: () -> Unit, onRank: () -> Unit, onSettings: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Caro Arena") }, actions = { TextButton(onClick = onSettings) { Text("⚙") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF6750A4))) {
                    Column(Modifier.padding(18.dp)) {
                        Text("👤 $name", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text("Level $level  •  ${hero.icon} ${hero.name}", color = Color.White.copy(alpha = .85f))
                        LinearProgressIndicator({ exp.toFloat() / (level * 300).coerceAtLeast(1) }, Modifier.fillMaxWidth().padding(top = 12.dp), color = Color(0xFFFFD166))
                        Text("$exp / ${level * 300} EXP", color = Color.White.copy(alpha = .8f))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Currency("🪙", "$gold"); Currency("💎", "$gems"); Currency("🏆", "$wins thắng")
                }
            }
            item { Button(onClick = onPlay, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp)) { Text("⚔  CHƠI NGAY", style = MaterialTheme.typography.titleMedium) } }
            item { SectionCard("Nhiệm vụ hôm nay", listOf("Thắng 3 ván", "Chơi 5 nước kỹ năng", "Đạt chuỗi 5 ô"), "Xem nhiệm vụ") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallMenu("🧙", "Tướng", onHeroes, Modifier.weight(1f))
                    SmallMenu("🛒", "Cửa hàng", onShop, Modifier.weight(1f))
                    SmallMenu("🏅", "Xếp hạng", onRank, Modifier.weight(1f))
                }
            }
            item { Text("Thành tích: $wins thắng • $losses thua • $draws hòa", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable private fun Currency(icon: String, text: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(icon); Text(text, fontWeight = FontWeight.Bold) } }
@Composable private fun SmallMenu(icon: String, title: String, onClick: () -> Unit, modifier: Modifier) { Card(modifier.clickable(onClick = onClick)) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(icon); Text(title) } } }
@Composable private fun SectionCard(title: String, lines: List<String>, action: String) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, fontWeight = FontWeight.Bold); lines.forEach { Text("☐  $it", style = MaterialTheme.typography.bodySmall) }; Text(action, color = Color(0xFF6750A4), style = MaterialTheme.typography.labelMedium) } } }

@Composable
private fun ModeScreen(selected: ArenaMode, onSelect: (ArenaMode) -> Unit, onStart: (ArenaMode) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Chọn chế độ chơi") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ArenaMode.values().toList()) { mode ->
                Card(Modifier.fillMaxWidth().clickable { onSelect(mode) }, colors = CardDefaults.cardColors(containerColor = if (mode == selected) Color(0xFFEADDFF) else Color.White)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (mode == ArenaMode.AI) "🤖" else if (mode == ArenaMode.ONLINE) "🌐" else "⚔", Modifier.size(42.dp)); Column(Modifier.weight(1f)) { Text(mode.title, fontWeight = FontWeight.Bold); Text(mode.description, style = MaterialTheme.typography.bodySmall) }; Text(if (mode == selected) "✓" else "") }
                }
            }
            item { Button(onClick = { onStart(selected) }, Modifier.fillMaxWidth()) { Text("TIẾP TỤC") } }
        }
    }
}

@Composable
private fun HeroesScreen(selected: Hero, gold: Int, onSelect: (Hero) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Tướng & kỹ năng") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(heroes) { hero ->
                Card(Modifier.fillMaxWidth().clickable { onSelect(hero) }, colors = CardDefaults.cardColors(containerColor = if (hero == selected) Color(0xFFEADDFF) else Color.White)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(hero.icon, Modifier.size(46.dp)); Column(Modifier.weight(1f)) { Text(hero.name, fontWeight = FontWeight.Bold); Text("${hero.element} • ${hero.skill}", style = MaterialTheme.typography.bodySmall) }; Text(if (hero.price == 0) "Đã có" else "🪙 ${hero.price}") }
                }
            }
            item { Text("Bạn có 🪙 $gold", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ShopScreen(gold: Int, gems: Int, buyGold: () -> Unit, buyGem: () -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Cửa hàng & kho đồ") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tài sản: 🪙 $gold  •  💎 $gems", style = MaterialTheme.typography.titleMedium)
            ShopItem("Rương vàng", "Nhận 300 Gold", "🪙 100", buyGold)
            ShopItem("Gói kim cương", "Nhận 10 Gem", "💎 1", buyGem)
            ShopItem("Skin Cyberpunk", "Bàn cờ neon độc quyền", "💎 15", {})
            ShopItem("Khung Avatar Rồng", "Danh hiệu Legendary", "💎 30", {})
        }
    }
}
@Composable private fun ShopItem(title: String, desc: String, price: String, onBuy: () -> Unit) { Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(desc, style = MaterialTheme.typography.bodySmall) }; Button(onClick = onBuy) { Text(price) } } } }

@Composable
private fun MatchScreen(code: String, onCode: (String) -> Unit, onJoin: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🌐 PHÒNG ĐẤU", style = MaterialTheme.typography.headlineMedium)
        Text("Tạo phòng local hoặc nhập mã phòng bạn bè.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onCode((100000..999999).random().toString()); onJoin() }, Modifier.fillMaxWidth()) { Text("TẠO PHÒNG") }
        androidx.compose.material3.OutlinedTextField(code, onCode, label = { Text("Mã phòng") }, singleLine = true)
        Button(onClick = onJoin, enabled = code.length == 6, modifier = Modifier.fillMaxWidth()) { Text("VÀO PHÒNG") }
        TextButton(onClick = onBack) { Text("Quay lại") }
    }
}

@Composable
private fun ArenaScreen(mode: ArenaMode, hero: Hero, sound: Boolean, onFinish: (Boolean?) -> Unit, onHome: () -> Unit) {
    val size = 10
    var board by remember { mutableStateOf(List(size * size) { ' ' }) }
    var current by remember { mutableStateOf('X') }
    var mana by remember { mutableIntStateOf(0) }
    var moves by remember { mutableStateOf(emptyList<ArenaMove>()) }
    var message by remember { mutableStateOf("") }
    var ended by remember { mutableStateOf(false) }
    fun tap(index: Int) {
        if (ended || board[index] != ' ') return
        val next = board.toMutableList()
        next[index] = current
        board = next
        moves = moves + ArenaMove(index, current)
        mana = minOf(100, mana + 15)
        if (winner(next, index, current, size)) {
            ended = true
            onFinish(current == 'X')
            return
        }
        if (next.none { it == ' ' }) { ended = true; onFinish(null); return }
        current = if (current == 'X') 'O' else 'X'
        if (mode == ArenaMode.AI && current == 'O') {
            val ai = next.indices.filter { next[it] == ' ' }.randomOrNull() ?: return
            val aiBoard = next.toMutableList()
            aiBoard[ai] = 'O'
            board = aiBoard
            moves = moves + ArenaMove(ai, 'O')
            current = 'X'
        }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("${hero.icon} ${hero.name}  •  ${mode.title}") }, navigationIcon = { TextButton(onClick = onHome) { Text("✕") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PlayerPanel("X", "Bạn", Color(0xFF3B82F6), mana)
                Text(if (ended) "KẾT THÚC" else "Lượt của $current", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                PlayerPanel("O", if (mode == ArenaMode.AI) "Bot" else "Bạn", Color(0xFFEF476F), 0)
            }
            LinearProgressIndicator({ mana / 100f }, Modifier.fillMaxWidth().padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { if (mana >= 50) { mana -= 50; message = hero.skill } }, enabled = mana >= 50 && !ended) { Text("⚡ Kỹ năng 50") }
                OutlinedButton(onClick = { message = "Cẩn thận đấy!" }) { Text("💬 Emote") }
                OutlinedButton(onClick = onHome) { Text("Thoát") }
            }
            if (message.isNotBlank()) Text(message, color = Color(0xFF6750A4), modifier = Modifier.padding(6.dp))
            GameBoard(board, size, onTap = ::tap)
            Text("Nước đi: ${moves.size}  •  ${if (mode == ArenaMode.AI) "Đấu Bot" else "Pass & Play"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PlayerPanel(symbol: String, name: String, color: Color, mana: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).background(color, CircleShape), contentAlignment = Alignment.Center) { Text(symbol, color = Color.White, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(6.dp))
        Column { Text(name, fontWeight = FontWeight.Bold); Text("Mana $mana", style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun GameBoard(board: List<Char>, size: Int, onTap: (Int) -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier.size(350.dp).pointerInput(size) {
                detectTapGestures { offset ->
                    val cell = minOf(this.size.width, this.size.height).toFloat() / size
                    val col = (offset.x / cell).toInt().coerceIn(0, size - 1)
                    val row = (offset.y / cell).toInt().coerceIn(0, size - 1)
                    onTap(row * size + col)
                }
            }
        ) {
            val cell = minOf(this.size.width, this.size.height) / size
            for (i in 0..size) {
                drawLine(Color(0xFF9A8F7A), Offset(i * cell, 0f), Offset(i * cell, cell * size), 1f)
                drawLine(Color(0xFF9A8F7A), Offset(0f, i * cell), Offset(cell * size, i * cell), 1f)
            }
            board.forEachIndexed { index, mark ->
                if (mark != ' ') {
                    val center = Offset((index % size) * cell + cell / 2, (index / size) * cell + cell / 2)
                    val color = if (mark == 'X') Color(0xFF3B82F6) else Color(0xFFEF476F)
                    if (mark == 'X') {
                        drawLine(color, Offset(center.x - cell * .28f, center.y - cell * .28f), Offset(center.x + cell * .28f, center.y + cell * .28f), cell * .08f, StrokeCap.Round)
                        drawLine(color, Offset(center.x + cell * .28f, center.y - cell * .28f), Offset(center.x - cell * .28f, center.y + cell * .28f), cell * .08f, StrokeCap.Round)
                    } else drawCircle(color, cell * .3f, center, style = Stroke(width = cell * .08f))
                }
            }
        }
    }
}

@Composable
private fun ResultScreen(wins: Int, losses: Int, draws: Int, level: Int, gold: Int, exp: Int, onAgain: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🏆 VICTORY", style = MaterialTheme.typography.displaySmall, color = Color(0xFF6750A4))
        Text("Phần thưởng trận đấu", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("⭐ +100 EXP"); Text("🪙 +120 Gold"); Text("Level hiện tại: $level"); Text("Thành tích: $wins thắng • $losses thua • $draws hòa") } }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAgain, Modifier.fillMaxWidth()) { Text("ĐẤU LẠI") }
        OutlinedButton(onClick = onHome, Modifier.fillMaxWidth()) { Text("VỀ SẢNH") }
    }
}

@Composable
private fun RankScreen(name: String, wins: Int, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Xếp hạng & lịch sử") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF))) { Column(Modifier.padding(18.dp)) { Text("Hạng Vàng III", style = MaterialTheme.typography.headlineSmall); Text("$name  •  $wins trận thắng"); LinearProgressIndicator({ (wins % 10) / 10f }, Modifier.fillMaxWidth().padding(top = 10.dp)) } } }
            item { Text("Bảng xếp hạng mùa này", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(listOf("🥇 ProCaro  •  2480 điểm", "🥈 MinhAnh  •  2290 điểm", "🥉 DragonX  •  2105 điểm", "4. $name  •  ${wins * 100} điểm")) { Text(it, Modifier.fillMaxWidth().padding(14.dp)) }
            item { HorizontalDivider(); Text("Lịch sử gần đây", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)); Text("✓ Thắng Bot Medium  +100 EXP"); Text("✓ Thắng PvP cục bộ  +120 Gold"); Text("○ Hòa trận Campaign") }
        }
    }
}

@Composable
private fun SettingsScreen(music: Boolean, sound: Boolean, graphics: Int, onMusic: (Boolean) -> Unit, onSound: (Boolean) -> Unit, onGraphics: (Int) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Cài đặt hệ thống") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Âm thanh", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SettingToggle("🎵 Nhạc nền", music, onMusic)
            SettingToggle("🔊 Hiệu ứng âm thanh", sound, onSound)
            Text("Đồ họa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(if (graphics == 1) "Tiết kiệm" else if (graphics == 2) "Cân bằng" else "Cao")
            Slider(graphics.toFloat(), { onGraphics(it.toInt()) }, valueRange = 1f..3f, steps = 1)
            Text("Luật chơi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Bàn 3x3 thắng 3 ô liên tiếp. Bàn Caro thắng 5 ô liên tiếp. Hết giờ sẽ mất lượt.")
            Text("Phiên bản 2.0 • Chơi offline", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun SettingToggle(label: String, enabled: Boolean, onChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label); FilterChip(enabled, { onChange(!enabled) }, label = { Text(if (enabled) "Bật" else "Tắt") }) } }

private fun winner(board: List<Char>, index: Int, player: Char, size: Int): Boolean {
    val row = index / size
    val col = index % size
    val target = if (size == 3) 3 else 5
    return listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1).any { (dr, dc) ->
        val count = sequenceOf(1, -1).sumOf { direction ->
            var r = row + dr * direction
            var c = col + dc * direction
            var result = 0
            while (r in 0 until size && c in 0 until size && board[r * size + c] == player) { result++; r += dr * direction; c += dc * direction }
            result
        } + 1
        count >= target
    }
}
