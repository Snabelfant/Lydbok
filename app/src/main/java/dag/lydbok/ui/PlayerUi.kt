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
import java.io.File

class PlayerUi(activity: AppCompatActivity, val viewModel: LydbokViewModel) {
    private lateinit var currentLydbok: Lydbok
    private val trackButtonPlayNewPauseOrResume: Button
    private val trackCurrentPositionView: TextView
    private val trackDurationView: TextView
    private val trackSeekBar: SeekBar
    private val audioPlayerCommands: AudioPlayerCommands
    private val trackButtonForwardSecs: Button
    private val trackButtonForwardPct: Button
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
        trackButtonBackwardSecs = activity.findViewById(R.id.playerbackwardsecs)
        trackButtonBackwardPct = activity.findViewById(R.id.playerbackwardpct)
        trackButtonPlayNewPauseOrResume = activity.findViewById(R.id.playerpauseorresume)

        trackButtonBackwardPct.setOnClickListener {
            Logger.info("Knapp bakover %")
            audioPlayerCommands.backwardPct(10)
        }

        trackButtonBackwardSecs.setOnClickListener {
            Logger.info("Knapp bakover 10")
            audioPlayerCommands.backwardSecs(10)
        }

        Logger.info("Knapp forover s")
        trackButtonPlayNewPauseOrResume.setOnClickListener { _ ->
            Logger.info("Knapp spill/pause/gjenoppta")
            play(currentLydbok.currentTrack.trackFile, currentLydbok.currentTrackOffset)
            viewModel.saveSelected()
        }

        trackButtonForwardSecs.setOnClickListener {
            audioPlayerCommands.forwardSecs(10)
        }

        trackButtonForwardPct.setOnClickListener {
            Logger.info("Knapp forover %")
            audioPlayerCommands.forwardPct(10)
        }

        viewModel.liveLydbøker.observe(activity, Observer { lydbøker ->
            Logger.info("PlayerUI: Observasjon lydbøker: $lydbøker")
            currentLydbok = lydbøker.getSelected()
            with(currentLydbok) {
                trackSeekBar.max = currentTrack.duration
                trackCurrentPositionView.text = currentTrack.duration.toMmSs()
                trackDurationView.text = currentTrack.duration.toMmSs()
                trackButtonPlayNewPauseOrResume.text = ">"
                play(currentTrack.trackFile, currentTrackOffset)
            }
        })
    }

    private fun play(file: File, currentTrackOffset: Int) {
        Logger.info("Play $file")
        audioPlayerCommands.playNewPauseOrResume(file, currentTrackOffset)
    }

    fun updatePlaybackStatus(currentPosition: Int, isPlaying: Boolean) {
        trackSeekBar.progress = currentPosition
        trackCurrentPositionView.text = currentPosition.toMmSs()
        trackButtonPlayNewPauseOrResume.text = if (isPlaying) "||" else ">"
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
