package dag.lydbok.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dag.lydbok.R
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Lydbøker
import dag.lydbok.util.DateUtil

class LydbøkerAdapter(context: Context) : ArrayAdapter<Lydbok>(context, R.layout.lydbok, R.id.lydboktitle) {
    private val inflater: LayoutInflater =
        super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var lydbøker: Lydbøker? = null

    override fun getCount() = lydbøker?.size ?: 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val lydbok = getItem(position)
        val lydbokView = inflater.inflate(R.layout.lydbok, parent, false)
        val titleView = lydbokView.findViewById<TextView>(R.id.lydboktitle)
        titleView.text = lydbok.title
        val positionView = lydbokView.findViewById<TextView>(R.id.lydbokposition)
        positionView.text = DateUtil.toMmSs(lydbok.currentLydbokOffset)
        val durationView = lydbokView.findViewById<TextView>(R.id.lydbokduration)
        durationView.text = DateUtil.toMmSs(lydbok.duration)
        return lydbokView
    }

    override fun getItem(position: Int) = lydbøker!![position]

    fun setLydbøker(lydbøker: Lydbøker) {
        this.lydbøker = lydbøker
        notifyDataSetChanged()
    }
}
