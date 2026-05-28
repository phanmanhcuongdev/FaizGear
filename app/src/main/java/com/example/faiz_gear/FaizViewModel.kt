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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class FaizViewModel : ViewModel() {

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

    // Low-latency Sound Engine
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()
    private var loadingStreamId: Int = 0

    var isInitialized by mutableStateOf(false)
        private set

    private var buttonPressCount = 0
    private val client = OkHttpClient()

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
        }
        isInitialized = true
    }

    private fun playSound(resId: Int, loop: Int = 0): Int {
        val soundId = soundMap[resId] ?: return 0
        return soundPool?.play(soundId, 1f, 1f, 1, loop, 1f) ?: 0
    }

    fun onKeyPress(digit: String) {
        if (isKeypadLocked) return
        
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
            inputCode = ""
            statusMessage = "READY"
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
        }
    }

    fun onEnter() {
        if (isKeypadLocked) return
        if (inputCode.isEmpty()) return

        buttonPressCount = 0

        when (inputCode) {
            "103" -> {
                updateStatus("SINGLE MODE", Color.Green)
                resetAfterDelay()
            }
            "106" -> {
                updateStatus("BURST MODE", Color.Green)
                resetAfterDelay()
            }
            "279" -> {
                updateStatus("CHARGE", Color.Green)
                resetAfterDelay()
            }
            "555" -> {
                executeHenshin()
            }
            else -> {
                updateStatus("ERROR", Color.Red)
                playSound(R.raw.error)
                resetAfterDelay()
            }
        }
    }

    private fun executeHenshin() {
        statusMessage = "STANDING BY..."
        statusColor = Color(0xFFFFA500)
        isKeypadLocked = true
        verificationProgress = 0f

        viewModelScope.launch(Dispatchers.Main) {
            // 1. Play Standing By (Stream A)
            playSound(R.raw.standingby)

            val networkDeferred = async(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("http://100.64.0.6:5555/faiz?code=555")
                    .build()
                try {
                    client.newCall(request).execute().use { response ->
                        response.isSuccessful
                    }
                } catch (e: IOException) {
                    false
                }
            }

            // 2. Start Loading with Manual Overlap Loop
            delay(1000) // Start overlap early
            
            val loadingJob = launch {
                while (isKeypadLocked && statusMessage == "STANDING BY...") {
                    loadingStreamId = playSound(R.raw.loading)
                    // loading.mp3 is very small (18KB), likely ~1s
                    // We trigger the NEXT one slightly BEFORE the current one ends
                    // to hide the MP3 gap.
                    delay(850) // Adjust this to match your file's actual duration minus ~150ms
                }
            }

            // 3. Smooth Progress Bar
            val totalSteps = 100
            for (i in 1..totalSteps) {
                delay(20) 
                verificationProgress = i.toFloat() / totalSteps
            }

            val networkSuccess = networkDeferred.await()
            loadingJob.cancel()
            soundPool?.stop(loadingStreamId)

            if (networkSuccess) {
                updateStatus("COMPLETE", Color.Green)
                playSound(R.raw.complete)
                delay(5000)
                resetToReady()
            } else {
                updateStatus("CONNECTION FAILED", Color.Red)
                playSound(R.raw.error)
                isKeypadLocked = false
                resetAfterDelay(3000L)
            }
        }
    }

    private fun updateStatus(message: String, color: Color) {
        viewModelScope.launch(Dispatchers.Main) {
            statusMessage = message
            statusColor = color
        }
    }

    private fun resetToReady() {
        viewModelScope.launch(Dispatchers.Main) {
            inputCode = ""
            statusMessage = "READY"
            verificationProgress = 0f
            isKeypadLocked = false
            buttonPressCount = 0
        }
    }

    private fun resetAfterDelay(delayMillis: Long = 5000L) {
        viewModelScope.launch {
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
        // Required for compatibility with existing UI if any, but now managed internally
    }
}
