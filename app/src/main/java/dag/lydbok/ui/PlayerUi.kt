package dag.lydbok.ui

import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dag.lydbok.R
import dag.lydbok.audioplayer.AudioPlayerCommands
import dag.lydbok.model.Lydbok
import dag.lydbok.model.selected
import dag.lydbok.util.Logger
import dag.lydbok.viewmodel.LydbokViewModel

class PlayerUi(activity: AppCompatActivity, val viewModel: LydbokViewModel) {
    private lateinit var currentLydbok: Lydbok
    private val pauseOrResumeBtn: Button
    private val currentPositionView: TextView = activity.findViewById(R.id.playertrackcurrentposition)
    private val durationView: TextView = activity.findViewById(R.id.playertrackduration)
    private val seekBar: SeekBar = activity.findViewById(R.id.playertrackseekbar)
    private val audioPlayerCommands: AudioPlayerCommands = AudioPlayerCommands(activity)
    private val forwardSecsBtn: Button
    private val forwardPctBtn: Button
    private val forwardTrackBtn: Button
    private val backwardSecsBtn: Button
    private val backwardPctBtn: Button
    private val backwardTrackBtn: Button

    init {

        seekBar.max = 0
        seekBar.progress = 0
        seekBar.setOnSeekBarChangeListener(TrackSeekBarChangeListener())

        forwardSecsBtn = activity.findViewById(R.id.playerforwardsecs)
        forwardPctBtn = activity.findViewById(R.id.playerforwardpct)
        forwardTrackBtn = activity.findViewById(R.id.playerforwardtrack)
        backwardSecsBtn = activity.findViewById(R.id.playerbackwardsecs)
        backwardPctBtn = activity.findViewById(R.id.playerbackwardpct)
        backwardTrackBtn = activity.findViewById(R.id.playerbackwardtrack)
        pauseOrResumeBtn = activity.findViewById(R.id.playerpauseorresume)

        backwardPctBtn.setOnClickListener {
            Logger.info("Knapp <%")
            audioPlayerCommands.backwardPct(10)
        }

        backwardSecsBtn.setOnClickListener {
            Logger.info("Knapp <S")
            audioPlayerCommands.backwardSecs(10)
        }

        backwardTrackBtn.setOnClickListener {
            Logger.info("Knapp <<")
            audioPlayerCommands.backwardTrack()
        }

        pauseOrResumeBtn.setOnClickListener { _ ->
            Logger.info("Knapp ||/>")
            audioPlayerCommands.pauseOrResume(currentLydbok.currentTrack.trackFile, currentLydbok.currentTrackOffset)
            viewModel.saveSelected()
        }

        forwardSecsBtn.setOnClickListener {
            Logger.info("Knapp  >S")
            audioPlayerCommands.forwardSecs(10)
        }

        forwardPctBtn.setOnClickListener {
            Logger.info("Knapp  >%")
            audioPlayerCommands.forwardPct(10)
        }

        forwardTrackBtn.setOnClickListener {
            Logger.info("Knapp >>")
            audioPlayerCommands.forwardTrack()
        }

        viewModel.liveLydbøker.observe(activity, Observer { lydbøker ->
            Logger.info("PlayerUI: Observasjon lydbøker: $lydbøker")
            currentLydbok = lydbøker.selected
            with(currentLydbok) {
                seekBar.max = currentTrack.duration
                currentPositionView.text = currentTrack.duration.toMmSs()
                durationView.text = currentTrack.duration.toMmSs()
                pauseOrResumeBtn.text = ">"
            }
        })

        viewModel.livePlaybackStatus.observe(activity, Observer { status ->
            //            Logger.info("PlayerUI: Observasjon pos: ${status.first}/${status.second}")
            updatePlaybackStatus(status.first, status.second)
        })
    }

    private fun updatePlaybackStatus(currentPosition: Int, isPlaying: Boolean) {
        seekBar.progress = currentPosition
        currentPositionView.text = currentPosition.toMmSs()

        if (isPlaying) {
            pauseOrResumeBtn.text = "||"
            forwardSecsBtn.isEnabled = true
            forwardPctBtn.isEnabled = true
            backwardSecsBtn.isEnabled = true
            backwardPctBtn.isEnabled = true
        } else {
            pauseOrResumeBtn.text = ">"
            forwardSecsBtn.isEnabled = false
            forwardPctBtn.isEnabled = false
            backwardSecsBtn.isEnabled = false
            backwardPctBtn.isEnabled = false
        }
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
