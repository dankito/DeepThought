package net.dankito.deepthought.javafx.dialogs.source.controls

import tornadofx.*


class EditDataFieldValueView(fieldName: String, initialFieldValue: String = "") : EditFieldValueView(fieldName, initialFieldValue) {


    init {
        val datePicker = datepicker {
                    isEditable = false
                    isShowWeekNumbers = true

                    minWidth = 0.0
                    maxWidth = 0.0

                    prefHeight = 40.0
                }

        root.add(datePicker)
    }

}