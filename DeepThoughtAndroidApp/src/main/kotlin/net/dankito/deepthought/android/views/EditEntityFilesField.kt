package net.dankito.deepthought.android.views

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.FilesRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.FileListPresenter
import net.dankito.filechooserdialog.FileChooserDialog
import net.dankito.filechooserdialog.model.FileChooserDialogConfig
import net.dankito.filechooserdialog.service.PreviewImageService
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.android.extensions.setLeftMargin
import net.dankito.utils.android.permissions.IPermissionsService
import net.dankito.utils.extensions.didCollectionChange
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IApplicationsService
import net.engio.mbassy.listener.Handler
import java.io.File
import javax.inject.Inject


class EditEntityFilesField : EditEntityField {

    companion object {
        private const val UnpersistedFilesUrisExtraName = "UNPERSISTED_FILES"
    }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @Inject
    protected lateinit var fileManager: FileManager

    @Inject
    protected lateinit var applicationsService: IApplicationsService

    @Inject
    protected lateinit var localization: Localization

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var previewImageService: PreviewImageService


    private var originalFiles: MutableCollection<FileLink> = ArrayList()

    private lateinit var permissionsService: IPermissionsService

    private val fileListPresenter: FileListPresenter

    private val attachedFilesAdapter: FilesRecyclerAdapter

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)

        fileListPresenter = FileListPresenter(fileManager, applicationsService, localization, router)
        attachedFilesAdapter = FilesRecyclerAdapter(fileListPresenter, previewImageService) { file -> removeFile(file) }

        rcySearchResult.adapter = attachedFilesAdapter
        attachedFilesAdapter.itemClickListener = { showFile(it) }
    }


    override fun onSaveInstanceState(): Parcelable {
        val parcelable = super.onSaveInstanceState()

        (parcelable as? Bundle)?.let { bundle ->
            // TODO: use UiStatePersister
            bundle.putStringArray(UnpersistedFilesUrisExtraName, attachedFilesAdapter.items.filter { it.isPersisted() == false }.map { it.uriString }.toTypedArray())
        }

        return parcelable
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)

        (state as? Bundle)?.let { savedInstanceState ->
            savedInstanceState.getStringArray(UnpersistedFilesUrisExtraName)?.forEach { uri ->
                addLocalFile(File(uri))
            }
        }
    }


    override fun doCustomUiInitialization(rootView: ViewGroup) {
        super.doCustomUiInitialization(rootView)

        setFieldNameOnUiThread(R.string.edit_entity_files_field_files_label)

        rcySearchResult.maxHeightInPixel =  (context.resources.getDimension(R.dimen.list_item_file_height) * 2.25).toInt() // show at max two list items and a little bit from
        // the next item so that user knows there's more

        showAsHasNoInputField()

        showAddFileIconInButtonEntityFieldAction(rootView)
    }

    private fun showAddFileIconInButtonEntityFieldAction(rootView: ViewGroup) {
        showActionIconOnUiThread(android.R.drawable.ic_input_add, true) {
            selectFilesToAdd()
        }

        (btnEntityFieldAction.layoutParams as? LayoutParams)?.topMargin = 0

        (txtEntityFieldName.layoutParams as? MarginLayoutParams)?.let { layoutParams ->
            layoutParams.setLeftMargin(2) // so that is has some indent as list item's txtFileName

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                layoutParams.height = btnEntityFieldAction.maxHeight
            }
        }
    }

    override fun viewClicked() {
        super.viewClicked()

        selectFilesToAdd()
    }


    fun setFiles(originalFiles: MutableCollection<FileLink>, permissionsManager: IPermissionsService, sourceForFile: Source? = null) {
        this.originalFiles = originalFiles
        this.permissionsService = permissionsManager

        attachedFilesAdapter.sourceForFile = sourceForFile

        setEditedFiles(originalFiles)
    }

    fun setEditedFiles(originalFiles: MutableCollection<FileLink>) {
        fileListPresenter.forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(originalFiles)

        attachedFilesAdapter.items = ArrayList(originalFiles) // make a copy to not edit original files
    }

    fun getEditedFiles(): Collection<FileLink> {
        return attachedFilesAdapter.items
    }


    fun selectFilesToAdd() {
        val config = FileChooserDialogConfig(permissionToReadExternalStorageRationaleResourceId = R.string.edit_entity_files_field_read_files_permission_rational)

        FileChooserDialog().showOpenMultipleFilesDialog(context as FragmentActivity, permissionsService, config) { _, selectedFiles ->
            selectedFiles?.forEach { file ->
                addLocalFile(file)
            }
        }
    }

    private fun addLocalFile(file: File) {
        val localFile = fileManager.createLocalFile(file)

        addFile(localFile)
    }

    private fun addFile(file: FileLink) {
        attachedFilesAdapter.addItem(file)

        updateDidValueChange()
    }

    private fun removeFile(file: FileLink) {
        attachedFilesAdapter.removeItem(file)

        updateDidValueChange()
    }

    private fun updateDidValueChange() {
        updateDidValueChange(originalFiles.didCollectionChange(attachedFilesAdapter.items))
    }


    private fun showFile(file: FileLink) {
        fileListPresenter.showFile(file)
    }


    override fun viewBecomesVisible() {
        super.viewBecomesVisible()

        eventBus.register(eventBusListener)
    }

    override fun viewGetsHidden() {
        eventBus.unregister(eventBusListener)

        super.viewGetsHidden()
    }


    inner class EventBusListener {

        @Handler
        fun fileChanged(change: FileChanged) {
            if(attachedFilesAdapter.items.contains(change.entity)) {
                (context as? Activity)?.runOnUiThread {
                    attachedFilesAdapter.notifyDataSetChanged()
                }
            }
        }

    }

}