package net.dankito.deepthought.javafx.ui.controls

import javafx.scene.control.DatePicker
import tornadofx.*
import java.time.LocalDate


open class EditEntityDateField(fieldName: String, initialDate: LocalDate? = LocalDate.now()) : EditEntityField(fieldName, "") {


    var selectedDate: LocalDate
        get() = datePicker.value
        set(value) { datePicker.value = value }

    protected val datePicker: DatePicker


    init {
        this.datePicker = datepicker {
            value = initialDate

            isEditable = false
            isShowWeekNumbers = true

            minWidth = 0.0
            maxWidth = 0.0

            prefHeight = 40.0

            valueProperty().addListener { _, _, newValue -> selectedDateChanged(newValue) }
        }

        root.add(datePicker)
    }

    protected open fun selectedDateChanged(newValue: LocalDate?) {
        convertDateToString(newValue)?.let { dateStringRepresentation ->
            currentValue.set(dateStringRepresentation)
        }
    }

    protected open fun convertDateToString(date: LocalDate?): String? {
        return null
    }

}