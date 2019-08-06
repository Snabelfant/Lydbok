package dag.lydbok.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dag.lydbok.R
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Track
import dag.lydbok.util.DateUtil

class TracksAdapter(context: Context) : ArrayAdapter<Track>(context, R.layout.track) {
    private val inflater: LayoutInflater =
        super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var lydbok: Lydbok? = null

    override fun getCount(): Int = lydbok?.tracks?.size ?: 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val track = getItem(position)
        val trackView = inflater.inflate(R.layout.track, parent, false)
        val titleView = trackView.findViewById<TextView>(R.id.tracktitle)
        titleView.text = track.title
        val durationView = trackView.findViewById<TextView>(R.id.trackduration)
        durationView.text = DateUtil.toMmSs(track.duration)

        val currentTrack = lydbok!!.currentTrack
        val backgroundColor = if (track === currentTrack) 0xFFFFFFCC.toInt() else Color.WHITE
        trackView.setBackgroundColor(backgroundColor)

        return trackView
    }

    fun setLydbok(lydbok: Lydbok) {
        this.lydbok = lydbok
        notifyDataSetChanged()
    }

    override fun getItem(position: Int) = lydbok!!.tracks[position]
}
