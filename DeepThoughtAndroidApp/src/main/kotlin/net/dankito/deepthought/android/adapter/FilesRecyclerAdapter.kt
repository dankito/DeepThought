package net.dankito.deepthought.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.viewholder.FileLinkViewHolder
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.presenter.FileListPresenter
import net.dankito.filechooserdialog.model.FileChooserDialogConfig
import net.dankito.filechooserdialog.service.PreviewImageService
import java.io.File


class FilesRecyclerAdapter(private val presenter: FileListPresenter, private val previewImageService: PreviewImageService, private val removeFileListener: (FileLink) -> Unit)
    : ListRecyclerSwipeAdapter<FileLink, FileLinkViewHolder>() {

    override fun getSwipeLayoutResourceId(position: Int) = R.id.fileLinkSwipeLayout


    var sourceForFile: Source? = null

    private val fileChooserDialogConfig = FileChooserDialogConfig() // needed for PreviewImageService to set if thumbnails should get loaded or not


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileLinkViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_file, parent, false)

        val viewHolder = FileLinkViewHolder(itemView)

        viewHolderCreated(viewHolder)

        return viewHolder
    }

    override fun bindViewForNullValue(viewHolder: FileLinkViewHolder) {
        super.bindViewForNullValue(viewHolder)

        viewHolder.imgPreviewImage.visibility = View.INVISIBLE

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

        val uriOrSynchronizationState = presenter.getUriOrSynchronizationState(item)
        viewHolder.txtFilePath.text = uriOrSynchronizationState
        viewHolder.txtFilePath.visibility = View.VISIBLE

        setPreviewImage(viewHolder, uriOrSynchronizationState)
    }

    private fun setPreviewImage(viewHolder: FileLinkViewHolder, uriOrSynchronizationState: String) {
        viewHolder.imgPreviewImage.visibility = View.VISIBLE

        val file = File(uriOrSynchronizationState)
        previewImageService.setPreviewImage(viewHolder, viewHolder.imgPreviewImage, file, fileChooserDialogConfig)
    }


    override fun setupSwipeView(viewHolder: FileLinkViewHolder, item: FileLink) {
        viewHolder.btnOpenInDeepThought.visibility = if (presenter.canFileBeOpenedInDeepThought(item)) View.VISIBLE else View.GONE
        viewHolder.btnOpenInDeepThought.setOnClickListener {
            presenter.openFileInDeepThought(item, sourceForFile)
        }

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