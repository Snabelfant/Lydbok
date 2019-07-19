package dag.lydbok.util

import android.app.AlertDialog
import android.content.Context

typealias Action = () -> Unit

val EMPTY: Action = {}

class YesNoCancel {
    fun show1(
        context: Context,
        title: String,
        message: String,
        yesAction: Action?,
        noAction: Action?,
        cancelAction: Action?
    ) {
        val builder = AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setIcon(android.R.drawable.ic_dialog_alert)
        }

        yesAction?.apply {
            builder.setPositiveButton("Ja") { dialog, _ ->
                yesAction()
                dialog.dismiss()
            }

        }

        noAction?.apply {
            builder.setNegativeButton("Nei") { dialog, _ ->
                noAction()
                dialog.dismiss()
            }
        }

        cancelAction?.apply { builder.setNegativeButton("Avbryt") { _, _ -> cancelAction() } }
        builder.show()
    }

    companion object {
        fun show(
            context: Context,
            title: String,
            message: String,
            yesAction: Action?,
            noAction: Action?,
            cancelAction: Action?
        ) {
            YesNoCancel().show1(context, title, message, yesAction, noAction, cancelAction)
        }
    }
}
