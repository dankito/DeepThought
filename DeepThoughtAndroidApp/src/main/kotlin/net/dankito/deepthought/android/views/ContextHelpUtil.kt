package net.dankito.deepthought.android.views

import android.view.View
import android.widget.Button
import android.widget.TextView
import net.dankito.deepthought.android.R


class ContextHelpUtil {

    fun showContextHelp(lytContextHelp: View, helpTextResourceId: Int) {
        showContextHelp(lytContextHelp, lytContextHelp.context.getString(helpTextResourceId))
    }

    fun showContextHelp(lytContextHelp: View, helpText: String) {
        val txtContextHelpText = lytContextHelp.findViewById(R.id.txtContextHelpText) as TextView
        txtContextHelpText.text = helpText

        lytContextHelp.visibility = View.VISIBLE

        val btnDismissContextHelp = lytContextHelp.findViewById(R.id.btnDismissContextHelp) as Button
        btnDismissContextHelp.setOnClickListener { lytContextHelp.visibility = View.GONE }
    }

}