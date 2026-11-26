package com.hoangvangiang.lichvannien1995

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

class EventStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences("lich_1995_giang_events", Context.MODE_PRIVATE)
    private val keyEvents = "events_json"
    fun loadEvents(): List<LunarEvent> {
        val raw = prefs.getString(keyEvents, null)
        if (raw == null) {
            val defaults = listOf(
                LunarEvent("1", "Rằm tháng Giêng (Cầu an)", 15, 1, true, true),
                LunarEvent("2", "Tết Đoan Ngọ", 5, 5, true, true),
                LunarEvent("3", "Rằm tháng Bảy (Vu Lan)", 15, 7, true, true)
            )
            saveEvents(defaults)
            return defaults
        }
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LunarEvent(o.getString("id"), o.getString("title"), o.getInt("lunarDay"), o.getInt("lunarMonth"), o.optBoolean("isRecurringYearly", true), o.optBoolean("reminderEnabled", true))
            }
        }.getOrElse { emptyList() }
    }
    fun saveEvents(list: List<LunarEvent>) {
        val arr = JSONArray()
        list.forEach { ev ->
            arr.put(JSONObject().apply {
                put("id", ev.id); put("title", ev.title); put("lunarDay", ev.lunarDay); put("lunarMonth", ev.lunarMonth)
                put("isRecurringYearly", ev.isRecurringYearly); put("reminderEnabled", ev.reminderEnabled)
            })
        }
        prefs.edit().putString(keyEvents, arr.toString()).apply()
        list.forEach { scheduleReminder(it) }
    }
    fun scheduleReminder(event: LunarEvent) {
        if (!event.reminderEnabled) return
        val today = LocalDate.now()
        var targetSolar: LocalDate? = null
        for (y in today.year..(today.year + 1)) {
            val ly = if (event.lunarMonth >= 11) y - 1 else y
            val (sd, sm, sy) = LunarCalendarEngine.convertLunar2Solar(event.lunarDay, event.lunarMonth, ly, false)
            if (sd > 0) {
                val c = LocalDate.of(sy, sm, sd)
                if (!c.isBefore(today)) { targetSolar = c; break }
            }
        }
        val triggerDate = targetSolar ?: return
        val epochMillis = triggerDate.atTime(8, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LunarReminderReceiver::class.java).apply {
            putExtra("title", event.title); putExtra("lunarDesc", "Ngày ${event.lunarDay}/${event.lunarMonth} Âm lịch")
        }
        val pi = PendingIntent.getBroadcast(context, event.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pi)
    }
}

class LunarReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Nhắc lịch Âm"
        val desc = intent.getStringExtra("lunarDesc") ?: ""
        val channelId = "lich_1995_giang_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "Nhắc Lịch Âm - 1995", NotificationManager.IMPORTANCE_HIGH))
        }
        val pi = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Giảng: $title").setContentText(desc)
            .setContentIntent(pi).setAutoCancel(true).build()
        nm.notify(title.hashCode(), notif)
    }
}

class LunarHomeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val info = FengShuiEngine.getDayInfo(LocalDate.now())
        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color(0xFF7F0000))).padding(14.dp)) {
                Text("HOÀNG VĂN GIẢNG • 1995", style = TextStyle(color = ColorProvider(Color(0xFFD4AF37)), fontSize = 11.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text("${info.solarDay}/${info.solarMonth}/${info.solarYear} (${info.dayOfWeek})", style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold))
                Text("Âm: ${info.lunar.day}/${info.lunar.month} • ${info.dayCanChi}", style = TextStyle(color = ColorProvider(Color(0xFFFFE082)), fontSize = 13.sp))
                Text("1995: ${info.fit1995.score}/100 (${info.fit1995.dayElement})", style = TextStyle(color = ColorProvider(Color.White), fontSize = 11.sp))
            }
        }
    }
}

class LunarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LunarHomeWidget()
}
