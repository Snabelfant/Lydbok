package dag.lydbok.audioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dag.lydbok.util.JsonMapper

import java.io.File


class AudioPlayerCommands(private val context: Context) {
    fun setTrackFiles(tracksFiles: List<String>, currentTrackFile: String) {
        val intent = Intent(INTENT_IN_TRACKFILES)
        intent.putExtra("trackfiles", JsonMapper().write(tracksFiles))
        intent.putExtra("currenttrackfile", currentTrackFile)
        context.sendBroadcast(intent)
    }

    fun playNewPauseOrResume(file: File, offset: Int) {
        val intent = Intent(INTENT_PLAYNEWPAUSEORRESUME)
        intent.putExtra("filename", file.absolutePath)
        intent.putExtra("offset", offset)
        context.sendBroadcast(intent)
    }

    fun seekTo(position: Int) {
        val intent = Intent(INTENT_SEEKTO).apply { putExtra("position", position) }
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

    fun registerPlaybackStatusReceiver(receiver: BroadcastReceiver) {
        context.registerReceiver(receiver, IntentFilter(INTENT_PLAYBACKSTATUS))
    }

    fun registerPlaybackCompletedReceiver(receiver: BroadcastReceiver) {
        context.registerReceiver(receiver, IntentFilter(INTENT_PLAYBACKCOMPLETED))
    }

    companion object {
        internal const val INTENT_IN_TRACKFILES = "dag.dag.podkast.SelectLydbok"
        internal const val INTENT_PLAYNEWPAUSEORRESUME = "dag.dag.podkast.PlayNewPauseOrResume"
        internal const val INTENT_FORWARDSECS = "dag.dag.podkast.ForwardSecs"
        internal const val INTENT_FORWARDPCT = "dag.dag.podkast.ForwardPct"
        internal const val INTENT_BACKWARDSECS = "dag.dag.podkast.BackwardSecs"
        internal const val INTENT_BACKWARDPCT = "dag.dag.podkast.BackwardPct"
        internal const val INTENT_PLAYBACKSTATUS = "dag.dag.podkast.CurrentPosition"
        internal const val INTENT_PLAYBACKCOMPLETED = "dag.dag.podkast.PlaybackCompleted"
        internal const val INTENT_SEEKTO = "dag.dag.podkast.SeekTo"
    }
}
