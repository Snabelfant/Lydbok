package dag.lydbok.util

import android.app.Activity
import android.app.AlertDialog

class MessageBox(private val activity: Activity, title: String, message: String) {
    private lateinit var alertDialog: AlertDialog

    init {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { _, _ -> }
        builder.setCancelable(true)
        activity.runOnUiThread { alertDialog = builder.create() }
    }

    fun setMessage(message: CharSequence) {
        activity.runOnUiThread { alertDialog.setMessage(message) }
    }

    fun show() {
        activity.runOnUiThread { alertDialog.show() }
    }
}
