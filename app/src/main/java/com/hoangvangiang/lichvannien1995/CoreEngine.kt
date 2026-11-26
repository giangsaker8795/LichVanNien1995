package com.hoangvangiang.lichvannien1995

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

data class LunarDate(val day: Int, val month: Int, val year: Int, val isLeap: Boolean, val julianDay: Int)
data class PersonalFit1995(val rating: String, val score: Int, val dayElement: String, val relationNote: String, val bestHoursFor1995: List<String>)
data class DayInfo(
    val solarDay: Int, val solarMonth: Int, val solarYear: Int, val dayOfWeek: String,
    val lunar: LunarDate, val yearCanChi: String, val monthCanChi: String, val dayCanChi: String,
    val hourCanChi: String, val hoangDaoHours: List<String>, val hacDaoHours: List<String>,
    val truc: String, val trucMeaning: String, val tietKhi: String, val huongXuatHanh: String,
    val isHoangDaoDay: Boolean, val holiday: String?, val quote: String, val fit1995: PersonalFit1995
)
data class LunarEvent(val id: String, val title: String, val lunarDay: Int, val lunarMonth: Int, val isRecurringYearly: Boolean = true, val reminderEnabled: Boolean = true)

object LunarCalendarEngine {
    private const val TIME_ZONE = 7.0
    fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        return jd
    }
    fun jdToDate(jd: Int): Triple<Int, Int, Int> {
        val a: Int; val b: Int; val c: Int
        if (jd > 2299160) { a = jd + 32044; b = (4 * a + 3) / 146097; c = a - (b * 146097) / 4 }
        else { b = 0; c = jd + 32082 }
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = b * 100 + d - 4800 + m / 10
        return Triple(day, month, year)
    }
    private fun newMoon(k: Int): Double {
        val t = k / 1236.85; val t2 = t * t; val t3 = t2 * t; val dr = PI / 180.0
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * dr * m)
        c1 -= 0.4068 * sin(mpr * dr) + 0.0161 * sin(dr * 2 * mpr) - 0.0004 * sin(dr * 3 * mpr)
        c1 += 0.0104 * sin(dr * 2 * f) - 0.0051 * sin(dr * (m + mpr)) - 0.0074 * sin(dr * (m - mpr))
        c1 += 0.0004 * sin(dr * (2 * f + m)) - 0.0004 * sin(dr * (2 * f - m))
        c1 -= 0.0006 * sin(dr * (2 * f + mpr)) + 0.0010 * sin(dr * (2 * f - mpr)) + 0.0005 * sin(dr * (2 * mpr + m))
        val deltat = if (t < -11) 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        else -0.000278 + 0.000265 * t + 0.000262 * t2
        return jd1 + c1 - deltat
    }
    private fun getNewMoonDay(k: Int): Int = floor(newMoon(k) + 0.5 + TIME_ZONE / 24.0).toInt()
    fun sunLongitude(jdn: Double): Double {
        val t = (jdn - 2451545.0) / 36525.0; val t2 = t * t; val dr = PI / 180.0
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.000290 * sin(dr * 3 * m)
        var l = (l0 + dl) * dr
        l -= PI * 2 * floor(l / (PI * 2))
        return l
    }
    private fun getSunLongitudeIndex(dayNumber: Int): Int = floor(sunLongitude(dayNumber - 0.5 - TIME_ZONE / 24.0) / PI * 6).toInt()
    private fun getLunarMonth11(yy: Int): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = floor(off / 29.530588853).toInt()
        var nm = getNewMoonDay(k)
        if (getSunLongitudeIndex(nm) >= 9) nm = getNewMoonDay(k - 1)
        return nm
    }
    private fun getLeapMonthOffset(a11: Int): Int {
        val k = floor((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var last: Int; var i = 1; var arc = getSunLongitudeIndex(getNewMoonDay(k + i))
        do { last = arc; i++; arc = getSunLongitudeIndex(getNewMoonDay(k + i)) } while (arc != last && i < 14)
        return i - 1
    }
    fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int): LunarDate {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1)
        if (monthStart > dayNumber) monthStart = getNewMoonDay(k)
        var a11 = getLunarMonth11(yy); var b11 = a11; val lunarYear: Int
        if (a11 >= monthStart) { lunarYear = yy; a11 = getLunarMonth11(yy - 1) }
        else { lunarYear = yy + 1; b11 = getLunarMonth11(yy + 1) }
        val lunarDay = dayNumber - monthStart + 1
        val diff = floor((monthStart - a11) / 29.0).toInt()
        var lunarLeap = false; var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11)
            if (diff >= leapMonthDiff) { lunarMonth = diff + 10; if (diff == leapMonthDiff) lunarLeap = true }
        }
        var adjYear = lunarYear
        if (lunarMonth > 12) lunarMonth -= 12
        if (lunarMonth >= 11 && diff < 4) adjYear -= 1
        return LunarDate(lunarDay, lunarMonth, adjYear, lunarLeap, dayNumber)
    }
    fun convertLunar2Solar(lunarDay: Int, lunarMonth: Int, lunarYear: Int, lunarLeap: Boolean): Triple<Int, Int, Int> {
        val a11: Int; val b11: Int
        if (lunarMonth < 11) { a11 = getLunarMonth11(lunarYear - 1); b11 = getLunarMonth11(lunarYear) }
        else { a11 = getLunarMonth11(lunarYear); b11 = getLunarMonth11(lunarYear + 1) }
        val k = floor(0.5 + (a11 - 2415021.076998695) / 29.530588853).toInt()
        var off = lunarMonth - 11; if (off < 0) off += 12
        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11); var leapMonth = leapOff - 2; if (leapMonth < 0) leapMonth += 12
            if (lunarLeap && lunarMonth != leapMonth) return Triple(0, 0, 0)
            else if (lunarLeap || off >= leapOff) off += 1
        }
        return jdToDate(getNewMoonDay(k + off) + lunarDay - 1)
    }
}

object FengShuiEngine {
    private val CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    private val CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    private val GIO_KHUNG = listOf("23h-1h", "1h-3h", "3h-5h", "5h-7h", "7h-9h", "9h-11h", "11h-13h", "13h-15h", "15h-17h", "17h-19h", "19h-21h", "21h-23h")
    private val THU = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật")
    private val TIET_KHI = listOf("Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng", "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ", "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết", "Đông Chí", "Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập")
    private val TRUC_LIST = listOf(
        "Kiến" to "Tốt cho xuất hành, nhậm chức; kiêng động thổ.",
        "Trừ" to "Tốt cho dọn dẹp, chữa bệnh, giải trừ điều xấu.",
        "Mãn" to "Tốt cho cầu tài, khai trương, cưới hỏi.",
        "Bình" to "Tốt cho sửa đường, giao dịch bình thường.",
        "Định" to "Tốt cho ký kết, mua bán, nhập học.",
        "Chấp" to "Tốt cho xây dựng, tuyển dụng; kiêng xuất hành xa.",
        "Phá" to "Xấu, chỉ nên phá dỡ công trình cũ.",
        "Nguy" to "Bất lợi, mọi việc nên thận trọng.",
        "Thành" to "Đại cát: Tốt cho khai trương, giao dịch, khởi sự.",
        "Thu" to "Tốt cho thu hoạch, thu hồi công nợ, nhập kho.",
        "Khai" to "Đại cát: Tốt cho mở cửa hàng, khởi đầu dự án mới.",
        "Bế" to "Xấu, chỉ nên đắp đê, lấp hố, kết thúc công việc."
    )
    private val HOANG_DAO_TABLE = mapOf(
        0 to listOf(1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0), 6 to listOf(1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0),
        1 to listOf(0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1), 7 to listOf(0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1),
        2 to listOf(1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0), 8 to listOf(1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0),
        3 to listOf(0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1), 9 to listOf(0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1),
        4 to listOf(1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0), 10 to listOf(1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0),
        5 to listOf(0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1), 11 to listOf(0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1)
    )
    private val NGAY_HOANG_DAO_THANG = mapOf(
        1 to listOf(0, 1, 5, 6, 8, 11), 7 to listOf(0, 1, 5, 6, 8, 11),
        2 to listOf(2, 3, 7, 8, 10, 1), 8 to listOf(2, 3, 7, 8, 10, 1),
        3 to listOf(4, 5, 9, 10, 0, 3), 9 to listOf(4, 5, 9, 10, 0, 3),
        4 to listOf(6, 7, 11, 0, 2, 5), 10 to listOf(6, 7, 11, 0, 2, 5),
        5 to listOf(8, 9, 1, 2, 4, 7), 11 to listOf(8, 9, 1, 2, 4, 7),
        6 to listOf(10, 11, 3, 4, 6, 9), 12 to listOf(10, 11, 3, 4, 6, 9)
    )
    private val PERSONAL_QUOTES_1995 = listOf(
        "Giảng (Ất Hợi 1995 - Sơn Đầu Hỏa): Rực rỡ đúng lúc, giữ nhịp đều và chốt việc dứt khoát.",
        "Hoàng Văn Giảng: Hỏa sinh Thổ, kỷ luật tạo nền tảng vững chắc cho mọi cú bứt phá.",
        "Chào Giảng, tập trung vào việc tạo ra giá trị thực — bớt ồn ào, tăng tốc độ thực thi.",
        "Tuổi Ất Hợi 1995: Khôn ngoan ở chỗ biết chọn mặt trận để dồn hỏa lực."
    )
    private fun chiElement(chiIdx: Int): String = when (chiIdx) {
        0, 11 -> "Thủy"; 2, 3 -> "Mộc"; 5, 6 -> "Hỏa"; 8, 9 -> "Thổ"; else -> "Kim"
    }
    private fun evaluateFor1995(dayChiIdx: Int, hoangDaoHours: List<String>): PersonalFit1995 {
        val elem = chiElement(dayChiIdx)
        val (rating, score, note) = when {
            dayChiIdx == 5 -> Triple("Xung trực tiếp (Tỵ - Hợi)", 32, "Ngày Tỵ xung với tuổi Hợi (1995). Hạn chế khởi sự lớn.")
            dayChiIdx in setOf(2, 8) -> Triple("Dễ xung khắc (Tứ hành xung)", 48, "Chi ngày thuộc nhóm Tứ hành xung với Hợi (1995). Nên làm việc nhẹ.")
            dayChiIdx in setOf(11, 3, 7) -> Triple("Đại cát cho 1995 (Tam Hợp Hợi-Mão-Mùi)", 94, "Ngày Tam Hợp với tuổi Ất Hợi 1995. Rất thuận lợi triển khai kế hoạch.")
            elem == "Mộc" -> Triple("Thuận lợi (Mộc sinh Hỏa)", 84, "Hành Mộc tiếp sức cho mệnh Hỏa (1995).")
            elem == "Hỏa" -> Triple("Tương hỗ (Hỏa gặp Hỏa)", 76, "Đồng hành cùng mệnh Hỏa 1995, giữ bình tĩnh tránh nóng vội.")
            else -> Triple("Trung bình (Cần chọn giờ tốt)", 62, "Ưu tiên khung giờ Hoàng Đạo để hành sự.")
        }
        val bestHours = hoangDaoHours.filter { l -> listOf("Mão", "Mùi", "Hợi").any { l.startsWith(it) } }.ifEmpty { hoangDaoHours.take(3) }
        return PersonalFit1995(rating, score, elem, note, bestHours)
    }
    fun getDayInfo(date: LocalDate): DayInfo {
        val lunar = LunarCalendarEngine.convertSolar2Lunar(date.dayOfMonth, date.monthValue, date.year)
        val jd = lunar.julianDay
        val yearCanChi = "${CAN[(lunar.year + 6) % 10]} ${CHI[(lunar.year + 8) % 12]}"
        val monthCanChi = "${CAN[(lunar.year * 12 + lunar.month + 3) % 10]} ${CHI[(lunar.month + 1) % 12]}"
        val dayCanIdx = (jd + 9) % 10; val dayChiIdx = (jd + 1) % 12
        val dayCanChi = "${CAN[dayCanIdx]} ${CHI[dayChiIdx]}"
        val hourChiIdx = ((LocalTime.now().hour + 1) / 2) % 12
        val hourCanChi = "${CAN[(dayCanIdx * 2 + hourChiIdx) % 10]} ${CHI[hourChiIdx]}"
        val hdFlags = HOANG_DAO_TABLE[dayChiIdx] ?: List(12) { 0 }
        val hoangDao = mutableListOf<String>(); val hacDao = mutableListOf<String>()
        for (i in 0..11) { val label = "${CHI[i]} (${GIO_KHUNG[i]})"; if (hdFlags[i] == 1) hoangDao.add(label) else hacDao.add(label) }
        val sunLong = LunarCalendarEngine.sunLongitude(jd - 0.5 + 7.0 / 24.0)
        val tietKhiIdx = floor(sunLong / (PI / 12.0)).toInt() % 24
        val trucIdx = (dayChiIdx - (((tietKhiIdx + 3) / 2) % 12) + 12) % 12
        val (truc, trucMeaning) = TRUC_LIST[trucIdx]
        val huong = when (dayCanIdx) {
            0, 5 -> "Hỷ Thần: Đông Bắc | Tài Thần: Đông Nam"
            1, 6 -> "Hỷ Thần: Tây Bắc | Tài Thần: Đông Nam"
            2, 7 -> "Hỷ Thần: Tây Nam | Tài Thần: Chính Đông"
            3, 8 -> "Hỷ Thần: Chính Nam | Tài Thần: Chính Đông"
            else -> "Hỷ Thần: Đông Nam | Tài Thần: Chính Bắc"
        }
        return DayInfo(
            date.dayOfMonth, date.monthValue, date.year, THU[date.dayOfWeek.value - 1], lunar,
            yearCanChi, monthCanChi, dayCanChi, hourCanChi, hoangDao, hacDao, truc, trucMeaning,
            TIET_KHI[tietKhiIdx], huong, NGAY_HOANG_DAO_THANG[lunar.month]?.contains(dayChiIdx) == true,
            null, PERSONAL_QUOTES_1995[jd % PERSONAL_QUOTES_1995.size], evaluateFor1995(dayChiIdx, hoangDao)
        )
    }
}
