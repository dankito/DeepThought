package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.FileLinkViewHolder
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.ui.presenter.FileListPresenter


class FilesRecyclerAdapter(private val presenter: FileListPresenter, private val removeFileListener: (FileLink) -> Unit) : ListRecyclerSwipeAdapter<FileLink, FileLinkViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = R.id.fileLinkSwipeLayout


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FileLinkViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_file, parent, false)

        val viewHolder = FileLinkViewHolder(itemView)

        viewHolderCreated(viewHolder)

        return viewHolder
    }

    override fun bindViewForNullValue(viewHolder: FileLinkViewHolder) {
        super.bindViewForNullValue(viewHolder)

        viewHolder.txtFileName.text = ""
        viewHolder.txtFileName.visibility = View.INVISIBLE

        viewHolder.txtFileSize.text = ""
        viewHolder.txtFileSize.visibility = View.INVISIBLE

        viewHolder.txtFilePath.text = ""
        viewHolder.txtFilePath.visibility = View.INVISIBLE
    }

    override fun bindItemToView(viewHolder: FileLinkViewHolder, item: FileLink) {
        viewHolder.txtFileName.text = item.name
        viewHolder.txtFileName.visibility = View.VISIBLE

        viewHolder.txtFileSize.text = presenter.formatFileSize(item.fileSize)
        viewHolder.txtFileSize.visibility = View.VISIBLE

        viewHolder.txtFilePath.text = presenter.getUriOrSynchronizationState(item)
        viewHolder.txtFilePath.visibility = View.VISIBLE
    }

    override fun setupSwipeView(viewHolder: FileLinkViewHolder, item: FileLink) {
        viewHolder.btnOpenContainingFolder.setOnClickListener {
            presenter.openContainingDirectoryOfFile(item)

            closeSwipeView(viewHolder)
        }

        viewHolder.btnRemoveFile.setOnClickListener {
            removeFileListener(item)

            closeSwipeView(viewHolder)
        }
    }

}