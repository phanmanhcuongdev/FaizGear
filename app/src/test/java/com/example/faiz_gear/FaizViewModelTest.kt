package com.example.faiz_gear

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FaizViewModelTest {

    private lateinit var viewModel: FaizViewModel

    @Before
    fun setup() {
        viewModel = FaizViewModel(
            mainDispatcher = Dispatchers.Unconfined,
            ioDispatcher = Dispatchers.Unconfined,
            enableAsyncActions = false
        )
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
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("6")
        viewModel.onKeyPress("7")
        assertEquals("123456", viewModel.inputCode)
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
        assertEquals("STATUS CHECK", viewModel.statusMessage)
        assertEquals("SCAN", viewModel.uiTitle)
        assertEquals(Color(0xFFFFA500), viewModel.statusColor)
        assertTrue(viewModel.isKeypadLocked)
    }

    @Test
    fun testCode106() {
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("0")
        viewModel.onKeyPress("6")
        viewModel.onEnter()
        assertEquals("BATCH START", viewModel.statusMessage)
        assertEquals("BURST", viewModel.uiTitle)
        assertEquals(Color(0xFFFFA500), viewModel.statusColor)
        assertTrue(viewModel.isKeypadLocked)
    }

    @Test
    fun testCode111() {
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("1")
        viewModel.onEnter()
        assertEquals("FAIZ START", viewModel.statusMessage)
        assertEquals("faiz_start", viewModel.currentAction)
        assertEquals("COMPLETE", viewModel.uiTitle)
        assertTrue(viewModel.isKeypadLocked)
    }

    @Test
    fun testErrorCode() {
        viewModel.onKeyPress("7")
        viewModel.onKeyPress("7")
        viewModel.onKeyPress("7")
        viewModel.onEnter()
        assertEquals("ERROR", viewModel.statusMessage)
        assertEquals(Color.Red, viewModel.statusColor)
        assertEquals("777", viewModel.inputCode)
    }

    @Test
    fun testCode888() {
        viewModel.onKeyPress("8")
        viewModel.onKeyPress("8")
        viewModel.onKeyPress("8")
        viewModel.onEnter()
        assertEquals("GUEST SHUTDOWN", viewModel.statusMessage)
        assertEquals("DEFORMATION", viewModel.uiTitle)
        assertEquals("guest_shutdown", viewModel.currentAction)
        assertEquals(Color(0xFFFFA500), viewModel.statusColor)
        assertTrue(viewModel.isKeypadLocked)
    }

    @Test
    fun testCode999() {
        viewModel.onKeyPress("9")
        viewModel.onKeyPress("9")
        viewModel.onKeyPress("9")
        viewModel.onEnter()
        assertEquals("MANAGED SHUTDOWN", viewModel.statusMessage)
        assertEquals("managed_shutdown", viewModel.currentAction)
        assertEquals(Color(0xFFFFA500), viewModel.statusColor)
        assertTrue(viewModel.isKeypadLocked)
    }

    @Test
    fun testCode000RequiresConfirmFirst() {
        viewModel.onKeyPress("0")
        viewModel.onKeyPress("0")
        viewModel.onKeyPress("0")
        viewModel.onEnter()
        assertEquals("CONFIRM", viewModel.statusMessage)
        assertEquals("NODE OFF", viewModel.uiSubtitle)
        assertTrue(viewModel.pendingNodeShutdownConfirm)
    }

    @Test
    fun testCode000SecondEnterStartsConfirmedAction() {
        viewModel.onKeyPress("0")
        viewModel.onKeyPress("0")
        viewModel.onKeyPress("0")
        viewModel.onEnter()
        viewModel.onEnter()
        assertEquals("NODE SHUTDOWN", viewModel.statusMessage)
        assertEquals("proxmox_shutdown", viewModel.currentAction)
        assertTrue(viewModel.isKeypadLocked)
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
