package net.dankito.deepthought.android.views

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.adapter.FilesRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.FileChooserDialog
import net.dankito.deepthought.android.extensions.setLeftMargin
import net.dankito.deepthought.android.service.permissions.IPermissionsManager
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.ui.presenter.FileListPresenter
import net.dankito.service.data.messages.FileChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.extensions.didCollectionChange
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IApplicationsService
import net.engio.mbassy.listener.Handler
import java.io.File
import javax.inject.Inject


class EditEntityFilesField : EditEntityField {

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
    protected lateinit var eventBus: IEventBus


    private var originalFiles: MutableCollection<FileLink> = ArrayList()

    private lateinit var permissionsManager: IPermissionsManager

    private var fileChooserDialog: FileChooserDialog? = null

    private val fileListPresenter: FileListPresenter

    private val attachedFilesAdapter: FilesRecyclerAdapter

    private val eventBusListener = EventBusListener()


    init {
        AppComponent.component.inject(this)

        fileListPresenter = FileListPresenter(fileManager, applicationsService, localization)
        attachedFilesAdapter = FilesRecyclerAdapter(fileListPresenter) { file -> removeFile(file) }

        rcySearchResult.adapter = attachedFilesAdapter
        attachedFilesAdapter.itemClickListener = { showFile(it) }

        eventBus.register(eventBusListener)
    }


    override fun doCustomUiInitialization(rootView: ViewGroup) {
        super.doCustomUiInitialization(rootView)

        setFieldNameOnUiThread(R.string.edit_entity_files_field_files_label)

        rcySearchResult.disableMaxHeight()

        showAsDoesNotAcceptInput()

        showAddFileIconInButtonEntityFieldAction(rootView)
    }

    private fun showAddFileIconInButtonEntityFieldAction(rootView: ViewGroup) {
        showActionIconOnUiThread(android.R.drawable.ic_input_add, true) {
            selectFileToAdd()
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

        selectFileToAdd()
    }


    fun setFiles(originalFiles: MutableCollection<FileLink>, permissionsManager: IPermissionsManager) {
        this.originalFiles = originalFiles
        this.permissionsManager = permissionsManager

        fileListPresenter.ensureLocalFileInfoIsSet(originalFiles)

        attachedFilesAdapter.items = ArrayList(originalFiles) // make a copy to not edit original files
    }

    fun getEditedFiles(): Collection<FileLink> {
        return attachedFilesAdapter.items
    }


    fun selectFileToAdd() {
        if(permissionsManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            selectFileToAddWithPermissionGranted()
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionsManager.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    context.getString(R.string.edit_entity_files_field_read_files_permission_rational))  { _, isGranted ->
                if(isGranted) {
                    selectFileToAddWithPermissionGranted()
                }
            }
        }
    }

    private fun selectFileToAddWithPermissionGranted() {
        if(fileChooserDialog == null) {
            fileChooserDialog = FileChooserDialog(context)
        }

        fileChooserDialog?.selectFile { file ->
            addLocalFile(file)
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