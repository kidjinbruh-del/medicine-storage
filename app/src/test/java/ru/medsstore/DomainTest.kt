package ru.medsstore.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Проверки доменной части: склонение сроков и подбор справочника.
 *
 * Склонение проверяется отдельно и по числам, на которых оно ломается:
 * 1, 2, 4, 5, 11, 12, 21, 22. Раньше здесь собиралось «осталось 5 дн. дней».
 */
class ExpiryTest {

    private val today: LocalDate = LocalDate.of(2026, 10, 5)

    private fun isoIn(days: Long): String = today.plusDays(days).toString()

    private fun labelIn(days: Long, soon: Int = 30): String = Expiry.label(isoIn(days), today, soon)

    @Test
    fun `срок без даты — это NONE`() {
        assertEquals(ExpiryState.NONE, Expiry.state(null, today, 30))
        assertEquals("срок не указан", Expiry.label(null, today, 30))
    }

    @Test
    fun `битая дата не роняет разбор`() {
        assertNull(Expiry.parse("через месяц"))
        assertNull(Expiry.parse("12.26"))
        assertEquals(ExpiryState.NONE, Expiry.state("через месяц", today, 30))
    }

    @Test
    fun `просроченное отделяется от нормального`() {
        assertEquals(ExpiryState.EXPIRED, Expiry.state(isoIn(-1), today, 30))
        assertEquals(ExpiryState.EXPIRED, Expiry.state(isoIn(-400), today, 30))
        assertEquals(ExpiryState.OK, Expiry.state(isoIn(31), today, 30))
    }

    @Test
    fun `порог скоро настраивается`() {
        assertEquals(ExpiryState.OK, Expiry.state(isoIn(45), today, 30))
        assertEquals(ExpiryState.SOON, Expiry.state(isoIn(45), today, 60))
    }

    @Test
    fun `склонение дней не дублируется`() {
        assertEquals("остался 1 день", labelIn(1))
        assertEquals("осталось 2 дня", labelIn(2))
        assertEquals("осталось 4 дня", labelIn(4))
        assertEquals("осталось 5 дней", labelIn(5))
        assertEquals("осталось 11 дней", labelIn(11))
        assertEquals("остался 21 день", labelIn(21))
        assertEquals("осталось 22 дня", labelIn(22))
    }

    @Test
    fun `просрочка склоняется так же`() {
        assertEquals("просрочен на 1 день", labelIn(-1))
        assertEquals("просрочено на 3 дня", labelIn(-3))
        assertEquals("просрочено на 12 дней", labelIn(-12))
    }

    @Test
    fun `на границе суток`() {
        // Срок, совпадающий с сегодняшним днём, — это ещё «скоро», не просрочка:
        // лекарство пригодно сегодня.
        assertEquals(ExpiryState.SOON, Expiry.state(isoIn(0), today, 30))
        assertEquals("истекает сегодня", labelIn(0))
    }
}

class ReferenceTest {

    @Test
    fun `находится по МНН и по торговому синониму`() {
        assertNotNull(Reference.find("ибупрофен"))
        assertNotNull(Reference.find("  ИБУПРОФЕН "))
        assertNotNull(Reference.find("ibuprofen"))
        assertNotNull(Reference.find("нурофен"))
        assertNotNull(Reference.find("Эутирокс"))
    }

    @Test
    fun `у каждой записи есть и приём, и риски`() {
        for (e in Reference.entries) {
            assertTrue("${e.inn}: пусто «как принимать»", e.how.isNotBlank())
            assertTrue("${e.inn}: пустые риски", e.risks.isNotBlank())
        }
    }

    @Test
    fun `подсказки не предлагают ерунду`() {
        val s = Reference.suggest("ibup")
        assertEquals(1, s.size)
        assertEquals("ибупрофен", s.first().inn)
        assertTrue(Reference.suggest("").isEmpty())
        assertTrue(Reference.suggest("zzzz").isEmpty())
    }

    @Test
    fun `неизвестный препарат не выдумывает справку`() {
        assertNull(Reference.find("какое-то лекарство"))
    }
}