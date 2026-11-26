package com.hoangvangiang.lichvannien1995

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

val ImperialRed = Color(0xFFB71C1C)
val GoldAccent = Color(0xFFD4AF37)
val WarmPaper = Color(0xFFFAF6F0)
val DarkSurface = Color(0xFF161313)

private val LightColors = lightColorScheme(primary = ImperialRed, secondary = GoldAccent, background = WarmPaper, surface = Color.White, onPrimary = Color.White, onBackground = Color(0xFF1F1A1A), onSurface = Color(0xFF1F1A1A))
private val DarkColors = darkColorScheme(primary = Color(0xFFE53935), secondary = GoldAccent, background = DarkSurface, surface = Color(0xFF221D1D), onPrimary = Color.White, onBackground = Color(0xFFF5EFE6), onSurface = Color(0xFFF5EFE6))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            MaterialTheme(colorScheme = if (isDarkTheme) DarkColors else LightColors) {
                MainScreen(isDarkTheme = isDarkTheme, onToggleTheme = { isDarkTheme = !isDarkTheme })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    val context = LocalContext.current
    val storage = remember { EventStorage(context) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showConverter by remember { mutableStateOf(false) }
    var events by remember { mutableStateOf(storage.loadEvents()) }
    val dayInfo = remember(selectedDate) { FengShuiEngine.getDayInfo(selectedDate) }
    fun updateEvents(newList: List<LunarEvent>) { events = newList; storage.saveEvents(newList) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lịch Vạn Niên 1995", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Hoàng Văn Giảng • Ất Hợi (Sơn Đầu Hỏa)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { selectedDate = LocalDate.now(); currentMonth = YearMonth.now() }) { Icon(Icons.Default.CalendarToday, contentDescription = "Hôm nay") }
                    IconButton(onClick = { showConverter = true }) { Icon(Icons.Default.SwapHoriz, contentDescription = "Đổi ngày Âm Dương") }
                    IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Đổi giao diện") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TodayCard(info = dayInfo)
            Fit1995Card(fit = dayInfo.fit1995)
            MonthGrid(selectedDate, currentMonth, { selectedDate = it }, { currentMonth = it })
            EventsCard(events, { updateEvents(events + it) }, { id -> updateEvents(events.filterNot { it.id == id }) })
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("LUẬN GIẢI CHI TIẾT NGÀY", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Tiết khí: ${dayInfo.tietKhi}", fontSize = 14.sp)
                    Text("Trực ${dayInfo.truc}: ${dayInfo.trucMeaning}", fontSize = 14.sp)
                    Text("Hướng xuất hành: ${dayInfo.huongXuatHanh}", fontSize = 14.sp)
                    HorizontalDivider()
                    Text("Giờ Hoàng Đạo: ${dayInfo.hoangDaoHours.joinToString(" • ")}", fontSize = 13.sp)
                    Text("Giờ Hắc Đạo: ${dayInfo.hacDaoHours.joinToString(" • ")}", fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    if (showConverter) {
        ConverterDialog(selectedDate, { showConverter = false }, { selectedDate = it; currentMonth = YearMonth.from(it) })
    }
}

@Composable
fun TodayCard(info: DayInfo) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(modifier = Modifier.background(Brush.verticalGradient(listOf(ImperialRed, Color(0xFF7F0000)))).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HOÀNG VĂN GIẢNG • 1995", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("THÁNG ${info.solarMonth} NĂM ${info.solarYear}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("${info.solarDay}", color = Color.White, fontSize = 72.sp, fontWeight = FontWeight.ExtraBold)
            Text(info.dayOfWeek.uppercase(), color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("\"${info.quote}\"", color = Color.White, fontSize = 13.sp, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Âm: ${info.lunar.day}/${info.lunar.month} • Năm ${info.yearCanChi} • Ngày ${info.dayCanChi} • Giờ ${info.hourCanChi}", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun Fit1995Card(fit: PersonalFit1995) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HỢP TUỔI GIẢNG (1995 • ẤT HỢI • SƠN ĐẦU HỎA)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Text("${fit.rating} (${fit.score}/100 • Hành ${fit.dayElement})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            LinearProgressIndicator(progress = { (fit.score.coerceIn(0, 100)) / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
            Text(fit.relationNote, fontSize = 13.sp)
            Text("Giờ tốt cho 1995: ${fit.bestHoursFor1995.joinToString(" • ")}", fontSize = 13.sp)
        }
    }
}

@Composable
fun MonthGrid(selectedDate: LocalDate, currentMonth: YearMonth, onDateSelected: (LocalDate) -> Unit, onMonthChanged: (YearMonth) -> Unit) {
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val firstDayOfMonth = currentMonth.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1
    val daysInMonth = currentMonth.lengthOfMonth()
    val today = LocalDate.now()
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Trước") }
                Text("Tháng ${currentMonth.monthValue} / ${currentMonth.year}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sau") }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEachIndexed { idx, d -> Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (idx >= 5) ImperialRed else MaterialTheme.colorScheme.onSurface) }
            }
            val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7
            for (row in 0 until (totalCells / 7)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val dayNum = row * 7 + col - startOffset + 1
                        if (dayNum in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNum)
                            val lunar = LunarCalendarEngine.convertSolar2Lunar(dayNum, currentMonth.monthValue, currentMonth.year)
                            val isSelected = date == selectedDate; val isToday = date == today
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(0.95f).padding(2.dp).clip(RoundedCornerShape(10.dp))
                                    .background(when { isSelected -> ImperialRed; isToday -> ImperialRed.copy(0.12f); else -> Color.Transparent })
                                    .then(if (isToday && !isSelected) Modifier.border(1.dp, ImperialRed, RoundedCornerShape(10.dp)) else Modifier)
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$dayNum", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                                    Text(if (lunar.day == 1) "${lunar.day}/${lunar.month}" else "${lunar.day}", fontSize = 10.sp, color = if (isSelected) Color.White else ImperialRed)
                                }
                            }
                        } else Spacer(modifier = Modifier.weight(1f).aspectRatio(0.95f))
                    }
                }
            }
        }
    }
}

@Composable
fun EventsCard(events: List<LunarEvent>, onAddEvent: (LunarEvent) -> Unit, onDeleteEvent: (String) -> Unit) {
    var title by remember { mutableStateOf("") }; var lunarDay by remember { mutableStateOf("1") }; var lunarMonth by remember { mutableStateOf("1") }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("SỰ KIỆN & NHẮC LỊCH ÂM CỦA GIẢNG", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên sự kiện / ngày giỗ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = lunarDay, onValueChange = { lunarDay = it.filter(Char::isDigit).take(2) }, label = { Text("Ngày Âm") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = lunarMonth, onValueChange = { lunarMonth = it.filter(Char::isDigit).take(2) }, label = { Text("Tháng Âm") }, modifier = Modifier.weight(1f))
                Button(onClick = {
                    val d = lunarDay.toIntOrNull()?.coerceIn(1, 30) ?: return@Button
                    val m = lunarMonth.toIntOrNull()?.coerceIn(1, 12) ?: return@Button
                    if (title.isBlank()) return@Button
                    onAddEvent(LunarEvent(UUID.randomUUID().toString(), title.trim(), d, m)); title = ""
                }) { Icon(Icons.Default.Add, contentDescription = null); Text("Lưu") }
            }
            events.forEach { ev ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${ev.title} (${ev.lunarDay}/${ev.lunarMonth} Âm)", fontSize = 13.sp)
                    IconButton(onClick = { onDeleteEvent(ev.id) }) { Icon(Icons.Default.Delete, contentDescription = "Xóa") }
                }
            }
        }
    }
}

@Composable
fun ConverterDialog(initialDate: LocalDate, onDismiss: () -> Unit, onJumpToDate: (LocalDate) -> Unit) {
    var modeSolarToLunar by remember { mutableStateOf(true) }
    var dIn by remember { mutableStateOf(initialDate.dayOfMonth.toString()) }
    var mIn by remember { mutableStateOf(initialDate.monthValue.toString()) }
    var yIn by remember { mutableStateOf(initialDate.year.toString()) }
    var isLeap by remember { mutableStateOf(false) }
    var res by remember { mutableStateOf("") }
    var target by remember { mutableStateOf<LocalDate?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi Ngày Âm - Dương") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = modeSolarToLunar, onClick = { modeSolarToLunar = true; res = "" }, label = { Text("Dương → Âm") })
                    FilterChip(selected = !modeSolarToLunar, onClick = { modeSolarToLunar = false; res = "" }, label = { Text("Âm → Dương") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dIn, onValueChange = { dIn = it.filter(Char::isDigit).take(2) }, label = { Text("Ngày") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = mIn, onValueChange = { mIn = it.filter(Char::isDigit).take(2) }, label = { Text("Tháng") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = yIn, onValueChange = { yIn = it.filter(Char::isDigit).take(4) }, label = { Text("Năm") }, modifier = Modifier.weight(1.2f))
                }
                if (!modeSolarToLunar) Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isLeap, onCheckedChange = { isLeap = it }); Text("Tháng nhuận") }
                Button(onClick = {
                    val d = dIn.toIntOrNull() ?: 1; val m = mIn.toIntOrNull() ?: 1; val y = yIn.toIntOrNull() ?: 1995
                    if (modeSolarToLunar) {
                        runCatching {
                            val dt = LocalDate.of(y, m, d); val l = LunarCalendarEngine.convertSolar2Lunar(d, m, y)
                            res = "Âm: ${l.day}/${l.month}${if (l.isLeap) " (Nhuận)" else ""} năm ${l.year}"; target = dt
                        }.onFailure { res = "Ngày Dương không hợp lệ." }
                    } else {
                        val (sd, sm, sy) = LunarCalendarEngine.convertLunar2Solar(d, m, y, isLeap)
                        if (sd == 0) res = "Ngày Âm không tồn tại." else { res = "Dương: $sd/$sm/$sy"; target = LocalDate.of(sy, sm, sd) }
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Tra Cứu") }
                if (res.isNotEmpty()) Text(res, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = { if (target != null) TextButton(onClick = { onJumpToDate(target!!); onDismiss() }) { Text("Xem Ngày") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}
