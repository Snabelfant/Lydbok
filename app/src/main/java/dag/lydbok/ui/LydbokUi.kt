package dag.lydbok.ui

import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dag.lydbok.R
import dag.lydbok.model.getSelected
import dag.lydbok.model.getSelectedIndex
import dag.lydbok.util.Logger
import dag.lydbok.viewmodel.LydbokViewModel

object LydbokUi {
    fun build(activity: AppCompatActivity, playerUi: PlayerUi, viewModel: LydbokViewModel) {
        val lydbokSpinner = activity.findViewById<Spinner>(R.id.lydbokspinner)
        val lydbokSpinnerAdapter = LydbøkerAdapter(activity)
        lydbokSpinnerAdapter.setDropDownViewResource(R.layout.lydbok)
        lydbokSpinner.adapter = lydbokSpinnerAdapter
        lydbokSpinner.setSelection(0)
        lydbokSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                viewModel.selectLydbok(lydbokSpinnerAdapter.getItem(i))
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        val tracksView = activity.findViewById<ListView>(R.id.tracks)
        val tracksAdapter = TracksAdapter(activity)
        tracksView.adapter = tracksAdapter

        viewModel.liveLydbøker.observe(activity, Observer { lydbøker ->
            Logger.info("Observasjon lydbøker: $lydbøker")
            lydbokSpinnerAdapter.setLydbøker(lydbøker)
            lydbokSpinner.setSelection(lydbøker.getSelectedIndex())
            tracksAdapter.setLydbok(lydbøker.getSelected())
        })

//        podcastListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
//            val podcast = podcastListAdapter.getItem(position)
//
//            when (podcast.state) {
//                Podcast.State.NEW -> viewModel.download(podcast)
//
//                Podcast.State.COMPLETED, Podcast.State.DOWNLOADED -> {
//                    val localFile = podcast.localFile!!
//                    if (!localFile.exists()) {
//                        ToastOnUiThread.show(activity, "Fant ikke fil " + localFile.absolutePath)
//                        viewModel.deleteDownloadedFile(podcast)
//                    } else {
//                        playerUi.play(localFile)
//                        viewModel.setPlaying(podcast)
//                    }
//                }
//                else -> {
//                }
//            }
//        }

    }

}
