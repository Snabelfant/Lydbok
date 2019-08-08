package dag.lydbok.ui

import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dag.lydbok.R
import dag.lydbok.audioplayer.AudioPlayerCommands
import dag.lydbok.model.Lydbok
import dag.lydbok.model.getSelected
import dag.lydbok.util.Logger
import dag.lydbok.viewmodel.LydbokViewModel

class PlayerUi(activity: AppCompatActivity, val viewModel: LydbokViewModel) {
    private lateinit var currentLydbok: Lydbok
    private val trackButtonPauseOrResume: Button
    private val trackCurrentPositionView: TextView
    private val trackDurationView: TextView
    private val trackSeekBar: SeekBar
    private val audioPlayerCommands: AudioPlayerCommands
    private val trackButtonForwardSecs: Button
    private val trackButtonForwardPct: Button
    private val trackButtonForwardTrack: Button
    private val trackButtonBackwardSecs: Button
    private val trackButtonBackwardPct: Button

    init {
        audioPlayerCommands = AudioPlayerCommands(activity)
        trackCurrentPositionView = activity.findViewById(R.id.playertrackcurrentposition)
        trackDurationView = activity.findViewById(R.id.playertrackduration)

        trackSeekBar = activity.findViewById(R.id.playertrackseekbar)
        trackSeekBar.max = 0
        trackSeekBar.progress = 0
        trackSeekBar.setOnSeekBarChangeListener(TrackSeekBarChangeListener())

        trackButtonForwardSecs = activity.findViewById(R.id.playerforwardsecs)
        trackButtonForwardPct = activity.findViewById(R.id.playerforwardpct)
        trackButtonForwardTrack = activity.findViewById(R.id.playerforwardtrack)
        trackButtonBackwardSecs = activity.findViewById(R.id.playerbackwardsecs)
        trackButtonBackwardPct = activity.findViewById(R.id.playerbackwardpct)
        trackButtonPauseOrResume = activity.findViewById(R.id.playerpauseorresume)

        trackButtonBackwardPct.setOnClickListener {
            Logger.info("Knapp -%")
            audioPlayerCommands.backwardPct(10)
        }

        trackButtonBackwardSecs.setOnClickListener {
            Logger.info("Knapp -s")
            audioPlayerCommands.backwardSecs(10)
        }

        Logger.info("Knapp +s")
        trackButtonPauseOrResume.setOnClickListener { _ ->
            Logger.info("Knapp ||/>")
            audioPlayerCommands.pauseOrResume(currentLydbok.currentTrack.trackFile, currentLydbok.currentTrackOffset)
            viewModel.saveSelected()
        }

        trackButtonForwardSecs.setOnClickListener {
            Logger.info("Knapp  +s")
            audioPlayerCommands.forwardSecs(10)
        }

        trackButtonForwardPct.setOnClickListener {
            Logger.info("Knapp  +%")
            audioPlayerCommands.forwardPct(10)
        }

        trackButtonForwardTrack.setOnClickListener {
            Logger.info("Knapp +>")
            audioPlayerCommands.forwardTrack()
        }

        viewModel.liveLydbøker.observe(activity, Observer { lydbøker ->
            Logger.info("PlayerUI: Observasjon lydbøker: $lydbøker")
            currentLydbok = lydbøker.getSelected()
            with(currentLydbok) {
                trackSeekBar.max = currentTrack.duration
                trackCurrentPositionView.text = currentTrack.duration.toMmSs()
                trackDurationView.text = currentTrack.duration.toMmSs()
                trackButtonPauseOrResume.text = ">"
            }
        })
    }

    fun updatePlaybackStatus(currentPosition: Int, isPlaying: Boolean) {
        trackSeekBar.progress = currentPosition
        trackCurrentPositionView.text = currentPosition.toMmSs()
        trackButtonPauseOrResume.text = if (isPlaying) "||" else ">"
        viewModel.updateTrackPosition(currentPosition)
    }

    private fun Int.toMmSs(): String {
        val s = this / 1000
        val mins = s / 60
        val secs = s % 60

        return String.format("%d:%02d", mins, secs)
    }

    private inner class TrackSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        private var isTracking = false

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (isTracking && fromUser) {
                audioPlayerCommands.seekTo(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            Logger.info("StartTT T")
            isTracking = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            Logger.info("StopTT T")
            isTracking = false
        }
    }

}
