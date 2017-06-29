package net.dankito.deepthought.android.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.widget.EditText
import kotlinx.android.synthetic.main.dialog_ask_extractor_name.*
import net.dankito.deepthought.android.R


class ArticleSummaryExtractorConfigDialog {

    fun askForName(context: Context, currentName: String, showCancelButton: Boolean, callback: (didSelectName: Boolean, selectedName: String?) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_ask_extractor_name)

        var input: EditText? = null

        if(showCancelButton) {
            builder.setNegativeButton(android.R.string.cancel, { dialog, _ ->
                dialog.cancel()
                callback(false, null)
            })
        }

        builder.setPositiveButton(android.R.string.ok, { dialog, _ ->
            dialog.cancel()
            callback(true, input?.text.toString())
        })

        val dialog = builder.create()
        dialog.show()

        input = dialog.edtxtAskExtractorName

        configureEditText(input, dialog, currentName)
    }

    private fun configureEditText(input: EditText, dialog: AlertDialog, currentName: String) {
        input.setText(currentName)

        input.selectAll()

        input.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }
}