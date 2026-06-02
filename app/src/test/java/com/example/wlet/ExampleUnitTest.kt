package com.example.wlet

import org.junit.Test
import org.junit.Assert.*
import com.example.wlet.ui.util.formatCurrency

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun formatCurrency_extractsCorrectSymbols() {
        val idrResult = formatCurrency(50000.0, "IDR - Indonesia (Rp)")
        assertTrue("Expected to contain Rp, got: $idrResult", idrResult.contains("Rp"))
        assertFalse("Should not contain description, got: $idrResult", idrResult.contains("Indonesia"))

        val usdResult = formatCurrency(1234.56, "USD - United States ($)")
        assertTrue("Expected to contain $, got: $usdResult", usdResult.contains("$"))
        assertFalse("Should not contain description, got: $usdResult", usdResult.contains("United States"))

        val eurResult = formatCurrency(1234.56, "EUR - Europe (€)")
        assertTrue("Expected to contain €, got: $eurResult", eurResult.contains("€"))
        assertFalse("Should not contain description, got: $eurResult", eurResult.contains("Europe"))

        val jpyResult = formatCurrency(1234.56, "JPY - Japan (¥)")
        assertTrue("Expected to contain ¥ or ￥, got: $jpyResult", jpyResult.contains("¥") || jpyResult.contains("￥"))
        assertFalse("Should not contain description, got: $jpyResult", jpyResult.contains("Japan"))

        val gbpResult = formatCurrency(1234.56, "GBP - United Kingdom (£)")
        assertTrue("Expected to contain £, got: $gbpResult", gbpResult.contains("£"))
        assertFalse("Should not contain description, got: $gbpResult", gbpResult.contains("United Kingdom"))

        // Test direct 3-letter codes
        val directIdr = formatCurrency(50000.0, "IDR")
        assertTrue("Expected to contain Rp, got: $directIdr", directIdr.contains("Rp"))

        val directUsd = formatCurrency(1234.56, "USD")
        assertTrue("Expected to contain $, got: $directUsd", directUsd.contains("$"))

        val directEur = formatCurrency(1234.56, "EUR")
        assertTrue("Expected to contain €, got: $directEur", directEur.contains("€"))
    }
}