package net.dankito.deepthought.javafx.dialogs.source.controls

import net.dankito.deepthought.javafx.ui.controls.EditEntityDateField
import net.dankito.deepthought.ui.presenter.EditSourcePresenter
import net.dankito.utils.datetime.asUtilDate
import tornadofx.*
import java.time.LocalDate


class EditSourcePublishingDateField(protected val presenter: EditSourcePresenter, publishingDate: LocalDate? = LocalDate.now())
    : EditEntityDateField(FX.messages["edit.source.publishing.date"], publishingDate) {


    override fun convertDateToString(date: LocalDate?): String? {
        date?.let {
            date.asUtilDate()?.let { javaUtilDate ->
                return presenter.convertPublishingDateToText(javaUtilDate)
            }
        }

        return super.convertDateToString(date)
    }

}