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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import com.example.nguyencongnhathuy_29214654999_cr424s.audio.GameAudio
import kotlin.math.max
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Screen { LOGIN, HOME, MODES, HEROES, SHOP, MATCH, GAME, RESULT, RANK, SETTINGS, GUIDE, STORY }
private enum class ArenaMode(val title: String, val description: String) {
    LOCAL("PvP cục bộ", "Hai người chơi chung thiết bị"),
    AI("Đấu với máy", "Bot Easy / Medium / Hard"),
    CAMPAIGN("Cốt truyện", "Đánh Boss qua từng màn"),
    SURVIVAL("Sinh tồn", "Leo tháp thử thách vô tận"),
    ONLINE("Phòng online", "Tạo hoặc nhập mã phòng")
}
private data class Hero(
    val id: String,
    val name: String,
    val element: String,
    val skill: String,
    val price: Int,
    val gemPrice: Int = 0,
    val icon: String
)
private data class ArenaMove(val index: Int, val player: Char)

private val heroes = listOf(
    Hero("ember", "Ember", "Lửa", "Fireball • Meteor Strike", 0, icon = "🔥"),
    Hero("frost", "Frost", "Băng", "Ice Spike • Blizzard", 1000, icon = "❄"),
    Hero("volt", "Volt", "Sấm", "Lightning Bolt • Storm Run", 1500, 100, "⚡"),
    Hero("terra", "Terra", "Đất", "Rock Shield • Earthquake", 2000, icon = "🪨"),
    Hero("bolt", "Bolt", "Cơ khí", "Autoblast • Rocket Dash", 2500, 150, "🤖"),
    Hero("bun", "Luna Bun", "Lunar", "Moon Shield • Hop Strike", 1800, icon = "🐰"),
    Hero("titan_rex", "Titan Rex", "Kaiju", "Roar Beam • Meteor Claw", 0, icon = "🦖"),
    Hero("stone_ape", "Stone Ape", "Cổ đại", "Mountain Punch • Ground Slam", 0, icon = "🦍")
)
private val CyberBackground = Color(0xFF080B14)
private val CyberPanel = Color(0xFF121A2B)
private val CyberPanelAlt = Color(0xFF1B2940)
private val NeonCyan = Color(0xFF65E6FF)
private val NeonMagenta = Color(0xFFFF4FD8)
private val NeonGold = Color(0xFFFFD166)

@Composable
private fun HeroPortrait(hero: Hero, modifier: Modifier = Modifier) {
    val accent = when (hero.element) {
        "Lửa" -> Color(0xFFFF7043)
        "Băng" -> Color(0xFF42A5F5)
        "Sấm" -> Color(0xFFFFC107)
        "Cơ khí" -> Color(0xFFB8C7D9)
        "Lunar" -> Color(0xFFC084FC)
        "Kaiju" -> Color(0xFF53D39B)
        "Cổ đại" -> Color(0xFFD49A7A)
        else -> Color(0xFF66BB6A)
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .8f), accent.copy(alpha = .15f))), size.minDimension / 2f, center)
            drawCircle(Color.White.copy(alpha = .18f), size.minDimension * .39f, center)
            if (hero.id == "titan_rex") {
                drawOval(Color(0xFF315D58), Offset(size.width * .15f, size.height * .45f), Size(size.width * .7f, size.height * .43f))
                drawCircle(Color(0xFF3F7D70), size.minDimension * .27f, Offset(center.x, center.y - size.height * .12f))
                for (i in 0..4) {
                    val x = size.width * (.25f + i * .125f)
                    drawLine(Color(0xFF78D5A5), Offset(x, size.height * .18f), Offset(x + size.width * .04f, size.height * .38f), size.minDimension * .06f, StrokeCap.Round)
                }
                drawCircle(Color(0xFFFFC857), size.minDimension * .035f, Offset(center.x - size.width * .1f, center.y - size.height * .13f))
                drawCircle(Color(0xFFFFC857), size.minDimension * .035f, Offset(center.x + size.width * .1f, center.y - size.height * .13f))
                drawArc(Color(0xFF102B31), 10f, 160f, false, Offset(size.width * .35f, size.height * .38f), Size(size.width * .3f, size.height * .2f), style = Stroke(size.minDimension * .04f))
            } else if (hero.id == "stone_ape") {
                drawOval(Color(0xFF725A52), Offset(size.width * .13f, size.height * .44f), Size(size.width * .74f, size.height * .46f))
                drawCircle(Color(0xFF96776B), size.minDimension * .3f, Offset(center.x, center.y - size.height * .08f))
                drawCircle(Color(0xFF5A403C), size.minDimension * .13f, Offset(size.width * .22f, center.y - size.height * .04f))
                drawCircle(Color(0xFF5A403C), size.minDimension * .13f, Offset(size.width * .78f, center.y - size.height * .04f))
                drawCircle(Color(0xFF202124), size.minDimension * .035f, Offset(center.x - size.width * .1f, center.y - size.height * .1f))
                drawCircle(Color(0xFF202124), size.minDimension * .035f, Offset(center.x + size.width * .1f, center.y - size.height * .1f))
                drawOval(Color(0xFF3B2927), Offset(size.width * .39f, size.height * .3f), Size(size.width * .22f, size.height * .18f))
            } else if (hero.id == "bolt") {
                drawRoundRect(Color(0xFF30445B), Offset(size.width * .25f, size.height * .25f), Size(size.width * .5f, size.height * .48f), androidx.compose.ui.geometry.CornerRadius(size.minDimension * .08f))
                drawCircle(Color(0xFF09111D), size.minDimension * .16f, Offset(center.x, center.y - size.height * .1f))
                drawCircle(NeonCyan, size.minDimension * .035f, Offset(center.x - size.width * .07f, center.y - size.height * .1f))
                drawCircle(NeonCyan, size.minDimension * .035f, Offset(center.x + size.width * .07f, center.y - size.height * .1f))
                drawLine(accent, Offset(center.x - size.width * .12f, size.height * .7f), Offset(center.x - size.width * .24f, size.height * .88f), size.minDimension * .06f, StrokeCap.Round)
                drawLine(accent, Offset(center.x + size.width * .12f, size.height * .7f), Offset(center.x + size.width * .24f, size.height * .88f), size.minDimension * .06f, StrokeCap.Round)
            } else if (hero.id == "bun") {
                drawOval(accent.copy(alpha = .8f), Offset(size.width * .2f, size.height * .5f), Size(size.width * .6f, size.height * .38f))
                drawOval(accent, Offset(size.width * .3f, size.height * .05f), Size(size.width * .14f, size.height * .38f))
                drawOval(accent, Offset(size.width * .56f, size.height * .05f), Size(size.width * .14f, size.height * .38f))
                drawCircle(Color(0xFFF7E9FF), size.minDimension * .25f, Offset(center.x, center.y - size.height * .08f))
                drawCircle(Color(0xFF25152F), size.minDimension * .035f, Offset(center.x - size.width * .09f, center.y - size.height * .08f))
                drawCircle(Color(0xFF25152F), size.minDimension * .035f, Offset(center.x + size.width * .09f, center.y - size.height * .08f))
            } else {
                drawOval(accent.copy(alpha = .8f), Offset(size.width * .2f, size.height * .5f), Size(size.width * .6f, size.height * .38f))
                drawCircle(Color(0xFFFFD1B3), size.minDimension * .25f, Offset(center.x, center.y - size.height * .08f))
                drawArc(accent, 190f, 160f, false, Offset(size.width * .25f, size.height * .2f), Size(size.width * .5f, size.height * .34f), style = Stroke(size.minDimension * .09f))
                drawCircle(Color(0xFF202124), size.minDimension * .035f, Offset(center.x - size.width * .09f, center.y - size.height * .08f))
                drawCircle(Color(0xFF202124), size.minDimension * .035f, Offset(center.x + size.width * .09f, center.y - size.height * .08f))
                drawLine(Color(0xFF7A3F3F), Offset(center.x - size.width * .06f, center.y + size.height * .08f), Offset(center.x + size.width * .06f, center.y + size.height * .08f), size.minDimension * .025f, StrokeCap.Round)
            }
        }
        Text(hero.icon, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ShopBanner() {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF101F3A))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(76.dp).background(Color(0xFF334155), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Text("🛒", style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("ARENA MARKET", color = NeonGold, fontWeight = FontWeight.Bold)
                Text("Skin, tướng và vật phẩm độc quyền", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

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
    var unlockedHeroes by remember {
        mutableStateOf((prefs.getStringSet("unlockedHeroes", setOf("ember")) ?: setOf("ember")).toSet())
    }
    var inventory by remember {
        mutableStateOf((prefs.getStringSet("inventory", emptySet()) ?: emptySet()).toSet())
    }
    var hero by remember {
        mutableStateOf(heroes.firstOrNull { it.id == prefs.getString("selectedHero", "ember") } ?: heroes.first())
    }
    var selectedMode by remember { mutableStateOf(ArenaMode.AI) }
    var roomCode by remember { mutableStateOf("") }
    var music by remember { mutableStateOf(prefs.getBoolean("music", true)) }
    var sound by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
    var graphics by remember { mutableIntStateOf(prefs.getInt("graphics", 2)) }
    var lastResult by remember { mutableStateOf<Boolean?>(null) }
    var showLevelUp by remember { mutableStateOf(false) }
    var history by remember {
        mutableStateOf((prefs.getStringSet("history", emptySet()) ?: emptySet()).toList().sortedDescending())
    }
    var authRegister by remember { mutableStateOf(false) }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf("") }

    fun saveProfile() {
        prefs.edit().putString("name", playerName).putBoolean("onboarded", true)
            .putInt("level", level).putInt("exp", exp).putInt("gold", gold)
            .putInt("gems", gems).putInt("wins", wins).putInt("losses", losses).putInt("draws", draws)
            .putString("selectedHero", hero.id).putStringSet("unlockedHeroes", unlockedHeroes)
            .putStringSet("inventory", inventory).putStringSet("history", history.toSet())
            .putBoolean("music", music).putBoolean("sound", sound).putInt("graphics", graphics).apply()
    }

    fun finishGame(win: Boolean?) {
        val earnedExp: Int
        lastResult = win
        when (win) {
            true -> { wins++; gold += 15; earnedExp = 50; history = (listOf("THẮNG • +50 EXP • +15 Gold") + history).take(10) }
            false -> { losses++; gold += 5; earnedExp = 20; history = (listOf("THUA • +20 EXP • +5 Gold") + history).take(10) }
            null -> { draws++; gold += 10; earnedExp = 30; history = (listOf("HÒA • +30 EXP • +10 Gold") + history).take(10) }
        }
        exp += earnedExp
        while (exp >= level * 100) {
            exp -= level * 100
            level++
            gems += 5
            showLevelUp = true
        }
        saveProfile()
        screen = Screen.RESULT
    }

    Surface(Modifier.fillMaxSize(), color = CyberBackground) {
        when (screen) {
            Screen.LOGIN -> AuthScreen(
                register = authRegister,
                name = playerName,
                email = authEmail,
                password = authPassword,
                error = authError,
                onRegisterChange = { authRegister = it; authError = "" },
                onName = { playerName = it },
                onEmail = { authEmail = it },
                onPassword = { authPassword = it },
                onGuest = { playerName = "Guest"; saveProfile(); screen = Screen.HOME },
                onSubmit = {
                    val savedPassword = prefs.getString("accountPassword", null)
                    if (authRegister) {
                        if (playerName.trim().length < 2 || authEmail.trim().length < 4 || authPassword.length < 4) {
                            authError = "Tên, email và mật khẩu cần hợp lệ."
                        } else {
                            prefs.edit().putString("accountEmail", authEmail.trim()).putString("accountPassword", authPassword).apply()
                            saveProfile()
                            screen = Screen.HOME
                        }
                    } else if (savedPassword == null || prefs.getString("accountEmail", "") != authEmail.trim() || savedPassword != authPassword) {
                        authError = "Thông tin đăng nhập chưa đúng."
                    } else {
                        playerName = prefs.getString("name", playerName) ?: playerName
                        screen = Screen.HOME
                    }
                }
            )
            Screen.HOME -> HomeScreen(playerName, level, exp, gold, gems, wins, losses, draws, hero,
                { screen = Screen.MODES }, { screen = Screen.HEROES }, { screen = Screen.SHOP },
                { screen = Screen.RANK }, { screen = Screen.SETTINGS }, { screen = Screen.GUIDE }, { screen = Screen.STORY })
            Screen.GUIDE -> GuideScreen { screen = Screen.HOME }
            Screen.STORY -> StoryScreen { screen = Screen.MODES }
            Screen.MODES -> ModeScreen(selectedMode, { selectedMode = it }, { screen = if (it == ArenaMode.ONLINE) Screen.MATCH else Screen.GAME }, { screen = Screen.HOME })
            Screen.HEROES -> HeroesScreen(hero, unlockedHeroes, gold, gems, { candidate ->
                if (candidate.id in unlockedHeroes) {
                    hero = candidate
                    saveProfile()
                } else if (gold >= candidate.price) {
                    gold -= candidate.price
                    unlockedHeroes = unlockedHeroes + candidate.id
                    hero = candidate
                    saveProfile()
                } else if (candidate.gemPrice > 0 && gems >= candidate.gemPrice) {
                    gems -= candidate.gemPrice
                    unlockedHeroes = unlockedHeroes + candidate.id
                    hero = candidate
                    saveProfile()
                }
            }, { screen = Screen.HOME })
            Screen.SHOP -> ShopScreen(gold, gems, inventory,
                { if (gold >= 500) { gold -= 500; gold += Random.nextInt(600, 1001); saveProfile() } },
                { if (gems >= 1) { gems--; gems += 10; saveProfile() } },
                { if (gems >= 15 && "cyberpunk_skin" !in inventory) { gems -= 15; inventory = inventory + "cyberpunk_skin"; saveProfile() } },
                { if (gems >= 30 && "dragon_frame" !in inventory) { gems -= 30; inventory = inventory + "dragon_frame"; saveProfile() } },
                { screen = Screen.HOME })
            Screen.MATCH -> MatchScreen(roomCode, { roomCode = it }, { screen = Screen.GAME }, { screen = Screen.HOME })
            Screen.GAME -> ArenaScreen(selectedMode, hero, sound, { finishGame(it) }, { screen = Screen.HOME })
            Screen.RESULT -> ResultScreen(lastResult, wins, losses, draws, level, gold, exp, { screen = Screen.GAME }, { screen = Screen.HOME })
            Screen.RANK -> RankScreen(playerName, wins, history, { screen = Screen.HOME })
            Screen.SETTINGS -> SettingsScreen(music, sound, graphics, { music = it; GameAudio.setMusicEnabled(it); saveProfile() }, { sound = it; saveProfile() }, { graphics = it; saveProfile() }, { screen = Screen.HOME }, { prefs.edit().putBoolean("onboarded", false).apply(); screen = Screen.LOGIN })
        }
        if (showLevelUp) {
            AlertDialog(
            onDismissRequest = { showLevelUp = false },
            title = { Text("LÊN CẤP!") },
            text = { Text("Chúc mừng! Bạn đã đạt Level $level và nhận thêm 5 Gem.") },
            confirmButton = { TextButton(onClick = { showLevelUp = false }) { Text("Tuyệt vời") } }
            )
        }
    }
}

@Composable
private fun AuthScreen(
    register: Boolean,
    name: String,
    email: String,
    password: String,
    error: String,
    onRegisterChange: (Boolean) -> Unit,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onGuest: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("CARO", style = MaterialTheme.typography.displayMedium, color = NeonCyan)
        Text("LEGENDS", style = MaterialTheme.typography.headlineMedium, color = NeonMagenta)
        Spacer(Modifier.height(20.dp))
        Text(if (register) "Tạo tài khoản chiến binh" else "Đăng nhập vào đấu trường", style = MaterialTheme.typography.titleLarge)
        androidx.compose.material3.OutlinedTextField(name, onName, label = { Text("Tên hiển thị") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(email, onEmail, label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.OutlinedTextField(password, onPassword, label = { Text("Mật khẩu") }, singleLine = true)
        if (error.isNotBlank()) Text(error, color = Color(0xFFFF7B8A), modifier = Modifier.padding(8.dp))
        Spacer(Modifier.height(14.dp))
        Button(onClick = onSubmit, Modifier.fillMaxWidth()) { Text(if (register) "ĐĂNG KÝ" else "ĐĂNG NHẬP") }
        TextButton(onClick = { onRegisterChange(!register) }) { Text(if (register) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký") }
        OutlinedButton(onClick = onGuest, Modifier.fillMaxWidth()) { Text("CHƠI KHÁCH (GUEST)") }
    }
}

@Composable
private fun HomeScreen(name: String, level: Int, exp: Int, gold: Int, gems: Int, wins: Int, losses: Int, draws: Int, hero: Hero, onPlay: () -> Unit, onHeroes: () -> Unit, onShop: () -> Unit, onRank: () -> Unit, onSettings: () -> Unit, onGuide: () -> Unit, onStory: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Caro Arena") }, actions = { TextButton(onClick = onSettings) { Text("⚙") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberPanelAlt)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        HeroPortrait(hero, Modifier.size(110.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                        Text("👤 $name", color = NeonCyan, style = MaterialTheme.typography.titleLarge)
                        Text("Level $level  •  ${hero.icon} ${hero.name}", color = Color.White.copy(alpha = .85f))
                        LinearProgressIndicator({ exp.toFloat() / (level * 100).coerceAtLeast(1) }, Modifier.fillMaxWidth().padding(top = 12.dp), color = NeonGold)
                        Text("$exp / ${level * 100} EXP", color = Color.White.copy(alpha = .8f))
                        }
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
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallMenu("📖", "Hướng dẫn", onGuide, Modifier.weight(1f))
                    SmallMenu("🌌", "Cốt truyện", onStory, Modifier.weight(1f))
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
private fun GuideScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Hướng dẫn chơi") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { GuideCard("1. Đặt quân", "Chạm vào ô trống trên bàn 10x10. X và O thay phiên nhau.") }
            item { GuideCard("2. Chiến thắng", "Tạo chuỗi 5 quân liên tiếp theo hàng ngang, dọc hoặc đường chéo.") }
            item { GuideCard("3. Thời gian", "Mỗi lượt có 30 giây. Hết giờ, lượt đó bị xử thua.") }
            item { GuideCard("4. Kỹ năng", "Tích Mana qua từng nước đi rồi dùng kỹ năng tướng để tạo lợi thế.") }
            item { GuideCard("5. Chế độ", "PvP là hai người trên một máy; PvE đấu Bot; Campaign theo cốt truyện; Survival thử chuỗi thắng.") }
        }
    }
}

@Composable
private fun GuideCard(title: String, text: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberPanelAlt)) {
        Column(Modifier.padding(16.dp)) { Text(title, color = NeonGold, fontWeight = FontWeight.Bold); Text(text, modifier = Modifier.padding(top = 6.dp)) }
    }
}

@Composable
private fun StoryScreen(onPlay: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Cốt truyện Caro Legends") }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberPanelAlt)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("KỶ NGUYÊN TINH VÂN", color = NeonCyan, style = MaterialTheme.typography.headlineSmall)
                        Text("Khi bốn nguyên tố thức tỉnh, các chiến binh phải bảo vệ lõi Caro khỏi đội quân máy tự động của Void.", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            item { StoryChapter("Ải 1 • Ember", "Ngọn lửa đầu tiên đánh thức chiến trường neon.") }
            item { StoryChapter("Ải 2 • Frost", "Băng tinh thể khóa thời gian và thử thách sự kiên nhẫn.") }
            item { StoryChapter("Ải 3 • Volt", "Sấm sét phá vỡ mọi dự đoán trong một lượt.") }
            item { StoryChapter("Ải 4 • Bolt & Luna Bun", "Robot Bolt và thỏ máy Luna Bun cùng chống lại đại tướng Void.") }
            item { Button(onClick = onPlay, Modifier.fillMaxWidth()) { Text("BẮT ĐẦU HÀNH TRÌNH") } }
        }
    }
}

@Composable
private fun StoryChapter(title: String, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("◆", color = NeonMagenta, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(10.dp))
        Column { Text(title, fontWeight = FontWeight.Bold); Text(text, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun ModeScreen(selected: ArenaMode, onSelect: (ArenaMode) -> Unit, onStart: (ArenaMode) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Chọn chế độ chơi") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ArenaMode.values().toList()) { mode ->
                Card(Modifier.fillMaxWidth().clickable { onSelect(mode) }, colors = CardDefaults.cardColors(containerColor = if (mode == selected) CyberPanelAlt else CyberPanel)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (mode == ArenaMode.AI) "🤖" else if (mode == ArenaMode.ONLINE) "🌐" else "⚔", Modifier.size(42.dp)); Column(Modifier.weight(1f)) { Text(mode.title, fontWeight = FontWeight.Bold); Text(mode.description, style = MaterialTheme.typography.bodySmall) }; Text(if (mode == selected) "✓" else "") }
                }
            }
            item { Button(onClick = { onStart(selected) }, Modifier.fillMaxWidth()) { Text("TIẾP TỤC") } }
        }
    }
}

@Composable
private fun HeroesScreen(selected: Hero, unlocked: Set<String>, gold: Int, gems: Int, onSelect: (Hero) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Tướng & kỹ năng") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(heroes) { hero ->
                Card(Modifier.fillMaxWidth().clickable { onSelect(hero) }, colors = CardDefaults.cardColors(containerColor = if (hero == selected) CyberPanelAlt else CyberPanel)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        HeroPortrait(hero, Modifier.size(76.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(hero.name, fontWeight = FontWeight.Bold)
                            Text("${hero.element} • ${hero.skill}", style = MaterialTheme.typography.bodySmall)
                            Text(if (hero.id in unlocked) "Đã mở khóa" else "🪙 ${hero.price}${if (hero.gemPrice > 0) " / 💎 ${hero.gemPrice}" else ""}", color = NeonGold, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(if (hero.id in unlocked) "CHỌN" else "MỞ", color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { Text("Bạn có 🪙 $gold  •  💎 $gems", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ShopScreen(gold: Int, gems: Int, inventory: Set<String>, buyGold: () -> Unit, buyGem: () -> Unit, buySkin: () -> Unit, buyFrame: () -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Cửa hàng & kho đồ") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShopBanner()
            Text("Tài sản: 🪙 $gold  •  💎 $gems", style = MaterialTheme.typography.titleMedium)
            ShopItem("Rương vàng", "Mở ra ngẫu nhiên 600–1000 Gold", "🪙 500", buyGold, false)
            ShopItem("Gói kim cương", "Nhận 10 Gem", "💎 1", buyGem, false)
            ShopItem("Skin Cyberpunk", "Hiệu ứng màu sắc độc quyền", "💎 15", buySkin, "cyberpunk_skin" in inventory)
            ShopItem("Khung Avatar Rồng", "Khung Legendary ngoài sảnh", "💎 30", buyFrame, "dragon_frame" in inventory)
        }
    }
}
@Composable private fun ShopItem(title: String, desc: String, price: String, onBuy: () -> Unit, owned: Boolean) { Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(desc, style = MaterialTheme.typography.bodySmall) }; Button(onClick = onBuy, enabled = !owned) { Text(if (owned) "ĐÃ CÓ" else price) } } } }

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
    val bot = if (mode == ArenaMode.AI) heroes.first { it.id == "titan_rex" } else heroes.first { it.id == "stone_ape" }
    val animationScope = rememberCoroutineScope()
    var board by remember { mutableStateOf(List(size * size) { ' ' }) }
    var current by remember { mutableStateOf('X') }
    var mana by remember { mutableIntStateOf(0) }
    var moves by remember { mutableStateOf(emptyList<ArenaMove>()) }
    var message by remember { mutableStateOf("") }
    var ended by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(30) }
    var resultVisible by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Boolean?>(null) }
    var attacking by remember { mutableStateOf<Char?>(null) }

    fun finishRound(value: Boolean?) {
        ended = true
        result = value
        if (sound) GameAudio.playWin()
        animationScope.launch {
            delay(260)
            resultVisible = true
        }
    }

    LaunchedEffect(current, ended) {
        if (!ended) {
            secondsLeft = 30
            while (secondsLeft > 0 && !ended) {
                delay(1_000)
                secondsLeft--
            }
            if (secondsLeft == 0 && !ended) {
                finishRound(if (current == 'X') false else true)
            }
        }
    }

    fun tap(index: Int) {
        if (ended || board[index] != ' ') return
        val next = board.toMutableList()
        next[index] = current
        board = next
        moves = moves + ArenaMove(index, current)
        attacking = current
        animationScope.launch {
            delay(220)
            attacking = null
        }
        mana = minOf(100, mana + 15)
        if (winner(next, index, current, size)) {
            finishRound(current == 'X')
            return
        }
        if (next.none { it == ' ' }) { finishRound(null); return }
        current = if (current == 'X') 'O' else 'X'
        if (mode == ArenaMode.AI && current == 'O') {
            val open = next.indices.filter { next[it] == ' ' }
            val ai = bestAiMove(next, open, size)
            if (ai == null) return
            val aiBoard = next.toMutableList()
            aiBoard[ai] = 'O'
            board = aiBoard
            moves = moves + ArenaMove(ai, 'O')
            attacking = 'O'
            animationScope.launch {
                delay(220)
                attacking = null
            }
            if (winner(aiBoard, ai, 'O', size)) {
                finishRound(false)
                return
            }
            if (aiBoard.none { it == ' ' }) {
                finishRound(null)
                return
            }
            current = 'X'
        }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("${hero.icon} ${hero.name}  •  ${mode.title}") }, navigationIcon = { TextButton(onClick = onHome) { Text("✕") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BattleFighter(hero, "X", "Bạn", attacking == 'X', Modifier.weight(1f))
                Text(if (ended) "KẾT THÚC" else "Lượt của $current • ${secondsLeft}s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                BattleFighter(bot, "O", if (mode == ArenaMode.AI) "BOT • ${bot.name}" else "Bạn", attacking == 'O', Modifier.weight(1f))
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
        if (resultVisible) {
            Box(Modifier.fillMaxSize().background(Color(0xCC050812)), contentAlignment = Alignment.Center) {
                Card(Modifier.fillMaxWidth(.9f), colors = CardDefaults.cardColors(containerColor = CyberPanelAlt), shape = RoundedCornerShape(28.dp)) {
                    Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when (result) { true -> "VICTORY"; false -> "DEFEAT"; null -> "DRAW" },
                            style = MaterialTheme.typography.displayMedium,
                            color = when (result) { true -> NeonGold; false -> Color(0xFFFF6B8A); null -> NeonCyan },
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            when (result) { true -> "Chiến binh ${hero.name} đã thống trị bàn cờ!"; false -> "Đối thủ đã chiếm lợi thế."; null -> "Không ai khuất phục được ai." },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        Button(onClick = { resultVisible = false; onFinish(result) }, Modifier.fillMaxWidth()) { Text("XEM PHẦN THƯỞNG") }
                        OutlinedButton(onClick = onHome, Modifier.fillMaxWidth()) { Text("VỀ SẢNH") }
                    }
                }

            }
        }
    }
}

@Composable
private fun BattleFighter(hero: Hero, symbol: String, label: String, attacking: Boolean, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HeroPortrait(hero, Modifier.size(74.dp))
        Text("$symbol  $label", color = if (symbol == "X") NeonCyan else Color(0xFFFF738A), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        if (attacking) Text("⚡ TẤN CÔNG!", color = NeonGold, fontWeight = FontWeight.Black)
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
private fun ResultScreen(result: Boolean?, wins: Int, losses: Int, draws: Int, level: Int, gold: Int, exp: Int, onAgain: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val title = when (result) {
            true -> "VICTORY!"
            false -> "DEFEAT"
            null -> "DRAW"
        }
        Text(title, style = MaterialTheme.typography.displaySmall, color = if (result == true) NeonGold else NeonCyan)
        Text("Phần thưởng trận đấu", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberPanelAlt)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("⭐ EXP hiện tại: $exp / ${level * 100}"); Text("🪙 Gold: $gold"); Text("Level hiện tại: $level"); Text("Thành tích: $wins thắng • $losses thua • $draws hòa"); LinearProgressIndicator({ exp.toFloat() / (level * 100).coerceAtLeast(1) }, Modifier.fillMaxWidth(), color = NeonCyan) } }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAgain, Modifier.fillMaxWidth()) { Text("ĐẤU LẠI") }
        OutlinedButton(onClick = onHome, Modifier.fillMaxWidth()) { Text("VỀ SẢNH") }
    }
}

@Composable
private fun RankScreen(name: String, wins: Int, history: List<String>, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Xếp hạng & lịch sử") }, navigationIcon = { TextButton(onClick = onBack) { Text("←") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyberPanelAlt)) { Column(Modifier.padding(18.dp)) { Text("Hạng Vàng III", color = NeonGold, style = MaterialTheme.typography.headlineSmall); Text("$name  •  $wins trận thắng"); LinearProgressIndicator({ (wins % 10) / 10f }, Modifier.fillMaxWidth().padding(top = 10.dp), color = NeonCyan) } } }
            item { Text("Bảng xếp hạng mùa này", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(listOf("🥇 ProCaro  •  2480 điểm", "🥈 MinhAnh  •  2290 điểm", "🥉 DragonX  •  2105 điểm", "4. $name  •  ${wins * 100} điểm")) { Text(it, Modifier.fillMaxWidth().padding(14.dp)) }
            item { HorizontalDivider(); Text("Lịch sử gần đây", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            if (history.isEmpty()) {
                item { Text("Chưa có trận đấu nào.") }
            } else {
                items(history) { Text("• $it", Modifier.padding(vertical = 3.dp)) }
            }
        }
    }
}

@Composable
private fun SettingsScreen(music: Boolean, sound: Boolean, graphics: Int, onMusic: (Boolean) -> Unit, onSound: (Boolean) -> Unit, onGraphics: (Int) -> Unit, onBack: () -> Unit, onLogout: () -> Unit) {
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
            OutlinedButton(onClick = onLogout, Modifier.fillMaxWidth()) { Text("ĐĂNG XUẤT") }
        }
    }
}

@Composable private fun SettingToggle(label: String, enabled: Boolean, onChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label); FilterChip(enabled, { onChange(!enabled) }, label = { Text(if (enabled) "Bật" else "Tắt") }) } }

private fun bestAiMove(board: List<Char>, open: List<Int>, size: Int): Int? {
    fun winningMove(player: Char): Int? = open.firstOrNull { candidate ->
        val test = board.toMutableList()
        test[candidate] = player
        winner(test, candidate, player, size)
    }
    return winningMove('O')
        ?: winningMove('X')
        ?: open.minByOrNull { index ->
            val row = index / size
            val col = index % size
            kotlin.math.abs(row - size / 2) + kotlin.math.abs(col - size / 2)
        }
}

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
