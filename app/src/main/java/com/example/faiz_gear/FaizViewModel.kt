package com.example.faiz_gear

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.io.IOException
import java.util.concurrent.TimeUnit

class FaizViewModel(
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val enableAsyncActions: Boolean = true
) : ViewModel() {

    var inputCode by mutableStateOf("")
        private set

    var statusMessage by mutableStateOf("READY")
        private set

    var statusColor by mutableStateOf(Color.White)
        private set

    var isKeypadLocked by mutableStateOf(false)
        private set

    var verificationProgress by mutableStateOf(0f)
        private set

    var uiTitle by mutableStateOf("READY")
        private set

    var uiSubtitle by mutableStateOf("SYSTEM STANDBY")
        private set

    var resultMessage by mutableStateOf("")
        private set

    var summaryMessage by mutableStateOf("")
        private set

    var targets by mutableStateOf(emptyList<FaizTarget>())
        private set

    var currentAction by mutableStateOf("idle")
        private set

    var pendingNodeShutdownConfirm by mutableStateOf(false)
        private set

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()
    private var loadingStreamId: Int = 0

    var isInitialized by mutableStateOf(false)
        private set

    private var buttonPressCount = 0
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(130, TimeUnit.SECONDS)
        .build()

    fun initSounds(context: Context) {
        if (isInitialized) return

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attributes)
            .build()

        soundPool?.let { pool ->
            soundMap[R.raw.firstbutton] = pool.load(context, R.raw.firstbutton, 1)
            soundMap[R.raw.secondbutton] = pool.load(context, R.raw.secondbutton, 1)
            soundMap[R.raw.thirdbutton] = pool.load(context, R.raw.thirdbutton, 1)
            soundMap[R.raw.standingby] = pool.load(context, R.raw.standingby, 1)
            soundMap[R.raw.loading] = pool.load(context, R.raw.loading, 1)
            soundMap[R.raw.complete] = pool.load(context, R.raw.complete, 1)
            soundMap[R.raw.error] = pool.load(context, R.raw.error, 1)
            soundMap[R.raw.singlemode] = pool.load(context, R.raw.singlemode, 1)
            soundMap[R.raw.burstmode] = pool.load(context, R.raw.burstmode, 1)
            soundMap[R.raw.deformation] = pool.load(context, R.raw.deformation, 1)
        }
        isInitialized = true
    }

    private fun playSound(resId: Int, loop: Int = 0): Int {
        val soundId = soundMap[resId] ?: return 0
        return soundPool?.play(soundId, 1f, 1f, 1, loop, 1f) ?: 0
    }

    fun onKeyPress(digit: String) {
        if (isKeypadLocked) return

        if (statusMessage == "COMPLETE" || statusMessage == "PARTIAL" || statusMessage == "ERROR") {
            resetToReady()
        }

        if (digit in "0".."9" || digit == "*" || digit == "#") {
            if (digit != "#") {
                val soundRes = when (buttonPressCount % 3) {
                    0 -> R.raw.firstbutton
                    1 -> R.raw.secondbutton
                    else -> R.raw.thirdbutton
                }
                playSound(soundRes)
                buttonPressCount++
            }
        }

        if (digit == "*") {
            resetToReady()
            return
        }
        if (digit == "#") {
            onEnter()
            return
        }
        if (inputCode.length < 6) {
            inputCode += digit
        }
    }

    fun onDelete() {
        if (isKeypadLocked) return
        if (inputCode.isNotEmpty()) {
            inputCode = inputCode.dropLast(1)
            if (inputCode != "000") pendingNodeShutdownConfirm = false
        }
    }

    fun onEnter() {
        if (isKeypadLocked) return
        if (inputCode.isEmpty()) return

        buttonPressCount = 0

        when (inputCode) {
            "103" -> executeFaizAction(
                FaizCommand("103", "status_check", "STATUS CHECK", "SCAN", "STATUS CHECK", R.raw.singlemode)
            )
            "106" -> executeFaizAction(
                FaizCommand("106", "batch_start", "BATCH START", "BURST", "BATCH START", R.raw.burstmode)
            )
            "555" -> executeHenshin()
            "111" -> executeFaizAction(
                FaizCommand("111", "faiz_start", "FAIZ START", "COMPLETE", "FAIZ START", R.raw.standingby)
            )
            "888" -> executeFaizAction(
                FaizCommand("888", "guest_shutdown", "GUEST SHUTDOWN", "DEFORMATION", "GUEST OFF", R.raw.deformation)
            )
            "999" -> executeFaizAction(
                FaizCommand("999", "managed_shutdown", "MANAGED SHUTDOWN", "DEFORMATION", "MANAGED OFF", R.raw.deformation)
            )
            "000" -> handleProxmoxShutdown()
            else -> {
                updateResult("error", "DENIED", "BAD CODE", "Exceed Charge!", "", Color.Red, "unknown")
                playSound(R.raw.error)
                resetAfterDelay()
            }
        }
    }

    private fun handleProxmoxShutdown() {
        if (!pendingNodeShutdownConfirm) {
            pendingNodeShutdownConfirm = true
            updateResult(
                status = "confirm",
                title = "CONFIRM",
                subtitle = "NODE OFF",
                message = "Press ENTER again",
                summary = "",
                color = Color(0xFFFFA500),
                action = "proxmox_shutdown"
            )
            playSound(R.raw.error)
            return
        }

        executeFaizAction(
            FaizCommand(
                code = "000",
                action = "proxmox_shutdown",
                verifyingLabel = "NODE SHUTDOWN",
                fallbackTitle = "NODE OFF",
                fallbackSubtitle = "PROXMOX",
                commandSound = R.raw.error,
                requiresConfirm = true
            )
        )
    }

    private fun executeFaizAction(command: FaizCommand) {
        pendingNodeShutdownConfirm = false
        statusMessage = command.verifyingLabel
        uiTitle = command.fallbackTitle
        uiSubtitle = command.fallbackSubtitle
        resultMessage = ""
        summaryMessage = ""
        currentAction = command.action
        statusColor = Color(0xFFFFA500)
        isKeypadLocked = true
        verificationProgress = 0f

        if (!enableAsyncActions) return

        viewModelScope.launch(mainDispatcher) {
            playSound(command.commandSound)

            val networkDeferred = async(ioDispatcher) {
                requestFaiz(command)
            }

            delay(800)
            loadingStreamId = playSound(R.raw.loading, -1)

            val totalSteps = 40
            for (i in 1..totalSteps) {
                delay(30)
                verificationProgress = i.toFloat() / totalSteps
            }

            val serverResult = networkDeferred.await()
            soundPool?.stop(loadingStreamId)
            applyServerResult(serverResult)

            if (serverResult.status == "complete" || serverResult.status == "partial") {
                playSound(if (serverResult.status == "partial") R.raw.error else R.raw.complete)
                if (serverResult.action == "status_check") {
                    inputCode = ""
                    isKeypadLocked = false
                    return@launch
                }
                delay(4000)
                resetToReady()
            } else {
                playSound(R.raw.error)
                isKeypadLocked = false
                resetAfterDelay(2500L)
            }
        }
    }

    private fun executeHenshin() {
        pendingNodeShutdownConfirm = false
        statusMessage = "STANDING BY..."
        uiTitle = "STANDING BY"
        uiSubtitle = "HENSHIN"
        resultMessage = ""
        summaryMessage = ""
        currentAction = "henshin"
        statusColor = Color(0xFFFFA500)
        isKeypadLocked = true
        verificationProgress = 0f

        if (!enableAsyncActions) return

        viewModelScope.launch(mainDispatcher) {
            playSound(R.raw.standingby)

            val command = FaizCommand("555", "henshin", "STANDING BY...", "WAKE", "SENT", R.raw.standingby)
            val networkDeferred = async(ioDispatcher) {
                requestFaiz(command)
            }

            delay(1000)

            val loadingJob = launch {
                while (isKeypadLocked && statusMessage == "STANDING BY...") {
                    loadingStreamId = playSound(R.raw.loading)
                    delay(850)
                }
            }

            val totalSteps = 100
            for (i in 1..totalSteps) {
                delay(20)
                verificationProgress = i.toFloat() / totalSteps
            }

            val serverResult = networkDeferred.await()
            loadingJob.cancel()
            soundPool?.stop(loadingStreamId)
            applyServerResult(serverResult)

            if (serverResult.status == "complete" || serverResult.status == "partial") {
                playSound(if (serverResult.status == "partial") R.raw.error else R.raw.complete)
                delay(4000)
                resetToReady()
            } else {
                playSound(R.raw.error)
                isKeypadLocked = false
                resetAfterDelay(3000L)
            }
        }
    }

    private fun requestFaiz(command: FaizCommand): FaizServerResult {
        val request = Request.Builder()
            .url(command.url)
            .header("Cache-Control", "no-store")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                parseFaizResponse(response.body.string(), response.isSuccessful, command)
            }
        } catch (e: SocketTimeoutException) {
            FaizServerResult(
                status = "error",
                title = "TIMEOUT",
                subtitle = "SERVER WAIT",
                message = "faiz-api did not finish in time",
                summary = "",
                action = command.action
            )
        } catch (e: IOException) {
            FaizServerResult(
                status = "error",
                title = "OFFLINE",
                subtitle = "API LOST",
                message = "Cannot reach faiz-api",
                summary = "",
                action = command.action
            )
        }
    }

    private fun parseFaizResponse(body: String, isHttpSuccessful: Boolean, command: FaizCommand): FaizServerResult {
        return try {
            val json = JSONObject(body.ifBlank { "{}" })
            val ui = json.optJSONObject("ui")
            val summary = json.optJSONObject("summary")
            val targets = json.optJSONArray("targets")
            val status = json.optString("status", if (isHttpSuccessful) "complete" else "error")
            FaizServerResult(
                status = status,
                title = ui?.optString("title")?.takeIf { it.isNotBlank() }
                    ?: if (isHttpSuccessful) command.fallbackTitle else "FAILED",
                subtitle = ui?.optString("subtitle")?.takeIf { it.isNotBlank() }
                    ?: if (isHttpSuccessful) command.fallbackSubtitle else json.optString("error_code", "SERVER ERR"),
                message = json.optString("message", ""),
                summary = summary?.let { "${it.optInt("ok")}/${it.optInt("total")} OK" }.orEmpty(),
                targets = parseTargets(targets),
                action = json.optString("action", command.action),
                errorCode = json.optString("error_code", "")
            )
        } catch (e: Exception) {
            FaizServerResult(
                status = if (isHttpSuccessful) "complete" else "error",
                title = if (isHttpSuccessful) command.fallbackTitle else "FAILED",
                subtitle = if (isHttpSuccessful) command.fallbackSubtitle else "BAD RESPONSE",
                message = body.ifBlank { "Empty server response" },
                summary = "",
                targets = emptyList(),
                action = command.action
            )
        }
    }

    private fun parseTargets(targetsJson: org.json.JSONArray?): List<FaizTarget> {
        if (targetsJson == null) return emptyList()

        return buildList {
            for (index in 0 until targetsJson.length()) {
                val target = targetsJson.optJSONObject(index) ?: continue
                add(
                    FaizTarget(
                        id = target.optString("id", "?"),
                        type = target.optString("type", ""),
                        name = target.optString("name", ""),
                        state = target.optString("state", "unknown"),
                        result = target.optString("result", ""),
                        ok = target.optBoolean("ok", false)
                    )
                )
            }
        }
    }

    private fun applyServerResult(result: FaizServerResult) {
        val color = when (result.status) {
            "complete" -> Color.Green
            "partial", "confirm" -> Color(0xFFFFA500)
            else -> Color.Red
        }
        updateResult(
            status = result.status,
            title = result.title,
            subtitle = result.subtitle,
            message = result.message,
            summary = result.summary,
            color = color,
            action = result.action,
            targets = result.targets
        )
    }

    private fun updateResult(
        status: String,
        title: String,
        subtitle: String,
        message: String,
        summary: String,
        color: Color,
        action: String,
        targets: List<FaizTarget> = emptyList()
    ) {
        statusMessage = status.uppercase()
        uiTitle = title
        uiSubtitle = subtitle
        resultMessage = message
        summaryMessage = summary
        this.targets = targets
        statusColor = color
        currentAction = action
    }

    private fun resetToReady() {
        inputCode = ""
        statusMessage = "READY"
        uiTitle = "READY"
        uiSubtitle = "SYSTEM STANDBY"
        resultMessage = ""
        summaryMessage = ""
        targets = emptyList()
        currentAction = "idle"
        verificationProgress = 0f
        isKeypadLocked = false
        buttonPressCount = 0
        pendingNodeShutdownConfirm = false
    }

    private fun resetAfterDelay(delayMillis: Long = 5000L) {
        if (!enableAsyncActions) return

        viewModelScope.launch(mainDispatcher) {
            delay(delayMillis)
            if (!isKeypadLocked) {
                resetToReady()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPool?.release()
        soundPool = null
    }

    fun onSoundPlayed() {
        // Compatibility hook for older UI code.
    }
}

data class FaizCommand(
    val code: String,
    val action: String,
    val verifyingLabel: String,
    val fallbackTitle: String,
    val fallbackSubtitle: String,
    val commandSound: Int,
    val requiresConfirm: Boolean = false
) {
    val url: String
        get() = if (requiresConfirm) {
            "http://100.64.0.6:5555/faiz?code=$code&confirm=pve-01"
        } else {
            "http://100.64.0.6:5555/faiz?code=$code"
        }
}

data class FaizServerResult(
    val status: String,
    val title: String,
    val subtitle: String,
    val message: String,
    val summary: String,
    val targets: List<FaizTarget> = emptyList(),
    val action: String,
    val errorCode: String = ""
)

data class FaizTarget(
    val id: String,
    val type: String,
    val name: String,
    val state: String,
    val result: String,
    val ok: Boolean
)
