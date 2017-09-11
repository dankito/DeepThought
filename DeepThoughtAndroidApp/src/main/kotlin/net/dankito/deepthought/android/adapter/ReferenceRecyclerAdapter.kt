package net.dankito.deepthought.android.adapter

import net.dankito.deepthought.ui.presenter.ReferencesPresenterBase


class ReferenceRecyclerAdapter(presenter: ReferencesPresenterBase): ReferenceRecyclerAdapterBase(presenter) {

    override val shouldShowImageIsReferenceAddedToEntry: Boolean
        get() = false

    override val shouldShowChevronRight: Boolean
        get() = true

    override val shouldShowButtonEditReference: Boolean
        get() = true

}