package com.example.faiz_gear

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FaizViewModelTest {

    private lateinit var viewModel: FaizViewModel

    @Before
    fun setup() {
        viewModel = FaizViewModel()
    }

    @Test
    fun testKeypress() {
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("5")
        assertEquals("555", viewModel.inputCode)
    }

    @Test
    fun testMaxLength() {
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("2")
        viewModel.onKeyPress("3")
        viewModel.onKeyPress("4")
        assertEquals("123", viewModel.inputCode)
    }

    @Test
    fun testDelete() {
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("2")
        viewModel.onDelete()
        assertEquals("1", viewModel.inputCode)
    }

    @Test
    fun testCode103() {
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("0")
        viewModel.onKeyPress("3")
        viewModel.onEnter()
        assertEquals("SINGLE MODE", viewModel.statusMessage)
        assertEquals(Color.Green, viewModel.statusColor)
        assertEquals("", viewModel.inputCode)
    }

    @Test
    fun testCode106() {
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("0")
        viewModel.onKeyPress("6")
        viewModel.onEnter()
        assertEquals("BURST MODE", viewModel.statusMessage)
        assertEquals(Color.Green, viewModel.statusColor)
        assertEquals("", viewModel.inputCode)
    }

    @Test
    fun testCode279() {
        viewModel.onKeyPress("2")
        viewModel.onKeyPress("7")
        viewModel.onKeyPress("9")
        viewModel.onEnter()
        assertEquals("CHARGE", viewModel.statusMessage)
        assertEquals(Color.Green, viewModel.statusColor)
        assertEquals("", viewModel.inputCode)
    }

    @Test
    fun testErrorCode() {
        viewModel.onKeyPress("9")
        viewModel.onKeyPress("9")
        viewModel.onKeyPress("9")
        viewModel.onEnter()
        assertEquals("ERROR", viewModel.statusMessage)
        assertEquals(Color.Red, viewModel.statusColor)
        assertEquals("", viewModel.inputCode)
    }

    @Test
    fun testCode555StandingBy() {
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("5")
        viewModel.onEnter()
        assertEquals("STANDING BY...", viewModel.statusMessage)
        // Note: We can't easily test the full network coroutine in a simple JUnit test
        // without more complex mocking, but we can verify the immediate state change.
    }
}
