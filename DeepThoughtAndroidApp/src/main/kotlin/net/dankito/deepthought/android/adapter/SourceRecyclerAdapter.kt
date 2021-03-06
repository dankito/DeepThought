package net.dankito.deepthought.android.adapter

import net.dankito.deepthought.ui.presenter.SourcePresenterBase


class SourceRecyclerAdapter(presenter: SourcePresenterBase): SourceRecyclerAdapterBase(presenter) {

    override val shouldShowImageIsSourceAddedToItem: Boolean
        get() = false

    override val shouldShowChevronRight: Boolean
        get() = true

    override val shouldShowButtonEditSource: Boolean
        get() = true

}