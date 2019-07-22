package dag.lydbok.ui

import android.app.Activity
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import dag.lydbok.R
import dag.lydbok.audioplayer.AudioPlayerCommands
import dag.lydbok.util.Logger
import java.io.File

class PlayerUi(activity: Activity) {
    private val buttonStop: Button
    private val buttonPauseOrResume: Button
    private val currentPositionView: TextView
    private val totalDurationView: TextView
    private val seekBar: SeekBar
    private val audioPlayerCommands: AudioPlayerCommands
    private val buttonForwardSecs: Button
    private val buttonForwardPct: Button
    private val buttonBackwardSecs: Button
    private val buttonBackwardPct: Button
    private var lastCommandWasStop = false

    init {
        audioPlayerCommands = AudioPlayerCommands(activity)
        currentPositionView = activity.findViewById(R.id.playerlocalcurrentposition)
        totalDurationView = activity.findViewById(R.id.playerlocalduration)

        seekBar = activity.findViewById(R.id.playerlocalseekbar)
        seekBar.max = 0
        seekBar.progress = 0
        seekBar.setOnSeekBarChangeListener(SeekBarChangeListener())

        buttonForwardSecs = activity.findViewById(R.id.playerforwardsecs)
        buttonForwardPct = activity.findViewById(R.id.playerforwardpct)
        buttonBackwardSecs = activity.findViewById(R.id.playerbackwardsecs)
        buttonBackwardPct = activity.findViewById(R.id.playerbackwardpct)
        buttonPauseOrResume = activity.findViewById(R.id.playerpauseorresume)
        buttonStop = activity.findViewById(R.id.playerstop)

        buttonBackwardPct.setOnClickListener {
            Logger.info("Knapp bakover %")
            audioPlayerCommands.backwardPct(10)
            lastCommandWasStop = false
        }

        buttonBackwardSecs.setOnClickListener {
            Logger.info("Knapp bakover 10")
            audioPlayerCommands.backwardSecs(10)
            lastCommandWasStop = false
        }

        buttonPauseOrResume.setOnClickListener { v ->
            Logger.info("Knapp pause/gjenoppta")
            audioPlayerCommands.pauseOrResume()
            val textView = v as TextView
            if (textView.text == "||") {
                textView.text = ">"
            } else {
                textView.text = "||"
            }
            lastCommandWasStop = false
        }

        buttonForwardSecs.setOnClickListener {
            Logger.info("Knapp forover s")
            audioPlayerCommands.forwardSecs(10)
            lastCommandWasStop = false
        }

        buttonForwardPct.setOnClickListener {
            Logger.info("Knapp forover %")
            audioPlayerCommands.forwardPct(10)
            lastCommandWasStop = false
        }

        buttonStop.setOnClickListener {
            if (lastCommandWasStop) {
                audioPlayerCommands.stop()
                disable()
            } else {
                lastCommandWasStop = true
            }
        }

        disable()
    }

    fun play(file: File) {
        Logger.info("Play $file")
        audioPlayerCommands.playNewAudio(file)
        enable()
        lastCommandWasStop = false
    }

    fun disable() {
        enableOrDisableControls(false)
    }

    private fun enable() {
        enableOrDisableControls(true)
    }

    private fun enableOrDisableControls(isEnable: Boolean) {
        buttonPauseOrResume.isEnabled = isEnable
        buttonPauseOrResume.text = "||"
        buttonStop.isEnabled = isEnable
        buttonForwardSecs.isEnabled = isEnable
        buttonForwardPct.isEnabled = isEnable
        buttonBackwardSecs.isEnabled = isEnable
        buttonBackwardPct.isEnabled = isEnable
        seekBar.isEnabled = isEnable
    }

    fun updatePosition(currentPosition: Int, totalDuration: Int) {
        seekBar.progress = currentPosition
        seekBar.max = totalDuration
        currentPositionView.text = toMmSs(totalDuration - currentPosition)
        totalDurationView.text = toMmSs(totalDuration)
    }

    private fun toMmSs(ms: Int): String {
        val s = ms / 1000
        val mins = s / 60
        val secs = s % 60

        return String.format("%d:%02d", mins, secs)
    }

    private inner class SeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        private var isTracking = false

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (isTracking && fromUser) {
                audioPlayerCommands.seekTo(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            Logger.info("StartTT")
            isTracking = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            Logger.info("StopTT")
            isTracking = false
        }
    }
}
