package fr.mathieu.cbjq

import androidx.compose.ui.graphics.Color
import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GetColorsForDLC {

    @Test
    fun shouldReturnCorrectValue() {

        val redColor = Color.hsv(0F, 0.85F, 0.90F)
        val orangeColor = Color.hsv(28F, 0.85F, 0.90F)
        val greenColor = Color.hsv(145F, 0.85F, 0.90F)

        val redDLC = Date.from(
            LocalDate.now().plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()
        )

        val orangeDLC = Date.from(
            LocalDate.now().plusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant()
        )

        val greenDLC = Date.from(
            LocalDate.now().plusDays(8).atStartOfDay(ZoneId.systemDefault()).toInstant()
        )

        val product = Product(
            name = "Fake product",
            openDate = null,
            limitDate = Date()
            )

        product.limitDate = redDLC
        assertEquals(redColor, getColorsForDLC(product))

        product.limitDate = orangeDLC
        assertEquals(orangeColor, getColorsForDLC(product))

        product.limitDate = greenDLC
        assertEquals(greenColor, getColorsForDLC(product))
    }

}