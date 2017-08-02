package net.dankito.deepthought.android.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.widget.DatePicker
import java.util.*


class PickDateDialog : DialogFragment(), DatePickerDialog.OnDateSetListener {

    // TODO: save and restore dialog


    private var date: Date? = null

    private lateinit var dateSetListener: (Date) -> Unit


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        date?.let { calendar.time = it }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calender = Calendar.getInstance()

        calender.set(year, month, dayOfMonth)

        dateSetListener(calender.time)
    }


    fun show(fragmentManager: FragmentManager, date: Date?, dateSetListener: (Date) -> Unit) {
        this.date = date
        this.dateSetListener = dateSetListener

        show(fragmentManager, javaClass.name)
    }

}