package dag.lydbok.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dag.lydbok.R
import dag.lydbok.model.Track
import dag.lydbok.model.Tracks
import dag.lydbok.util.DateUtil

class TracksAdapter(context: Context) : ArrayAdapter<Track>(context, R.layout.track) {
    private val inflater: LayoutInflater
    private var tracks: Tracks = mutableListOf()

    init {
        inflater = super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int = tracks.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val track = getItem(position)
        val trackView = inflater.inflate(R.layout.track, parent, false)
        val titleView = trackView.findViewById<TextView>(R.id.tracktitle)
        titleView.text = track.title
        val durationView = trackView.findViewById<TextView>(R.id.trackduration)
        durationView.text = DateUtil.toMmSs(track.duration)
        return trackView
    }

    fun setTracls(tracks: Tracks) {
        this.tracks = tracks
    }

    override fun getItem(position: Int) = tracks[position]
}
