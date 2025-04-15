package fr.mathieu.cbjq

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class GetLeftDaysBeforeDLCUnitTest {

    @Test
    fun shouldReturnCorrectValue() {

        val leftDays = 32L

        val product = Product(
            name = "Fake product",
            openDate = null,
            limitDate = Date.from(
                LocalDate.now()
                    .plusDays(leftDays)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
            ))

        assertEquals(32, getLeftDaysBeforeDLC(product))
    }

}