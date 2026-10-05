package ru.medsstore.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Состояние запаса по сроку годности. */
enum class ExpiryState {
    /** Срок вышел: применять нельзя. */
    EXPIRED,

    /** Истекает скоро — скоро входит в переходы. */
    SOON,

    /** В норме. */
    OK,

    /** Срок не указан. */
    NONE,
}

object Expiry {
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val HUMAN: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun parse(iso: String?): LocalDate? {
        if (iso.isNullOrBlank()) return null
        return try {
            LocalDate.parse(iso, ISO)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun toIso(date: LocalDate): String = date.format(ISO)

    fun toHuman(iso: String?): String = parse(iso)?.format(HUMAN) ?: "—"

    /** Сколько дней осталось. Отрицательное — просрочено. */
    fun daysLeft(iso: String?, today: LocalDate): Long? =
        parse(iso)?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it) }

    fun state(iso: String?, today: LocalDate, soonDays: Int): ExpiryState {
        val left = daysLeft(iso, today) ?: return ExpiryState.NONE
        return when {
            left < 0 -> ExpiryState.EXPIRED
            left <= soonDays -> ExpiryState.SOON
            else -> ExpiryState.OK
        }
    }

    /**
     * Короткая подпись под сроком.
     *
     * Склонение пишется здесь целиком, а не собирается из кусков: в первой
     * версии «осталось 5 дн.» и «дней» склеились в «осталось 5 дн. дней».
     */
    fun label(iso: String?, today: LocalDate, soonDays: Int): String {
        val left = daysLeft(iso, today) ?: return "срок не указан"
        return when (state(iso, today, soonDays)) {
            ExpiryState.EXPIRED ->
                if (left == 0L) "срок вышел сегодня" else overdue(-left)
            ExpiryState.SOON ->
                if (left == 0L) "истекает сегодня" else remain(left)
            else -> remain(left)
        }
    }

    private fun dayWord(n: Long): String {
        val m10 = n % 10
        val m100 = n % 100
        return when {
            m100 in 11..14 -> "дней"
            m10 == 1L -> "день"
            m10 in 2..4 -> "дня"
            else -> "дней"
        }
    }

    /** «остался 21 день», «осталось 2 дня», «осталось 5 дней». */
    private fun remain(n: Long): String {
        val verb = if (n % 10 == 1L && n % 100 != 11L) "остался" else "осталось"
        return "$verb $n ${dayWord(n)}"
    }

    /** «просрочен на 1 день», «просрочено на 5 дней». */
    private fun overdue(n: Long): String {
        val verb = if (n % 10 == 1L && n % 100 != 11L) "просрочен на" else "просрочено на"
        return "$verb $n ${dayWord(n)}"
    }
}