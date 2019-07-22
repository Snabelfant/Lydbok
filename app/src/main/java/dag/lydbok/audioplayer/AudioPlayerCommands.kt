package dag.lydbok.audioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import java.io.File

class AudioPlayerCommands(private val context: Context) {

    fun playNewAudio(file: File) {
        val intent = Intent(INTENT_PLAYNEWAUDIO)
        intent.putExtra("filename", file.absolutePath)
        context.sendBroadcast(intent)
    }

    fun pauseOrResume() {
        val intent = Intent(INTENT_PAUSEORRESUME)
        context.sendBroadcast(intent)
    }

    fun seekTo(position: Int) {
        val intent = Intent(INTENT_SEEKTO)
        intent.putExtra("position", position)
        context.sendBroadcast(intent)
    }

    fun forwardSecs(secs: Int) {
        val intent = Intent(INTENT_FORWARDSECS)
        intent.putExtra("secs", secs)
        context.sendBroadcast(intent)
    }

    fun forwardPct(pct: Int) {
        val intent = Intent(INTENT_FORWARDPCT)
        intent.putExtra("pct", pct)
        context.sendBroadcast(intent)
    }

    fun backwardSecs(secs: Int) {
        val intent = Intent(INTENT_BACKWARDSECS)
        intent.putExtra("secs", secs)
        context.sendBroadcast(intent)
    }

    fun backwardPct(pct: Int) {
        val intent = Intent(INTENT_BACKWARDPCT)
        intent.putExtra("pct", pct)
        context.sendBroadcast(intent)
    }

    fun stop() {
        val intent = Intent(INTENT_STOP)
        context.sendBroadcast(intent)
    }

    fun registerCurrentPositionReceiver(receiver: BroadcastReceiver) {
        val intentFilter = IntentFilter(INTENT_CURRENTPOSITION)
        context.registerReceiver(receiver, intentFilter)
    }

    fun registerPlaybackStoppedReceiver(receiver: BroadcastReceiver) {
        val intentFilter = IntentFilter(INTENT_STOP)
        context.registerReceiver(receiver, intentFilter)
    }

    fun registerPlaybackCompletedReceiver(receiver: BroadcastReceiver) {
        val intentFilter = IntentFilter(INTENT_COMPLETED)
        context.registerReceiver(receiver, intentFilter)
    }

    companion object {
        internal const val INTENT_PLAYNEWAUDIO = "dag.dag.podkast.PlayNewAudio"
        internal const val INTENT_PAUSEORRESUME = "dag.dag.podkast.PauseOrResume"
        internal const val INTENT_FORWARDSECS = "dag.dag.podkast.ForwardSecs"
        internal const val INTENT_FORWARDPCT = "dag.dag.podkast.ForwardPct"
        internal const val INTENT_BACKWARDSECS = "dag.dag.podkast.BackwardSecs"
        internal const val INTENT_BACKWARDPCT = "dag.dag.podkast.BackwardPct"
        internal const val INTENT_CURRENTPOSITION = "dag.dag.podkast.CurrentPosition"
        internal const val INTENT_COMPLETED = "dag.dag.podkast.PlaybackCompleted"
        internal const val INTENT_STOP = "dag.dag.podkast.Stop"
        internal const val INTENT_SEEKTO = "dag.dag.podkast.SeekTo"
    }
}
