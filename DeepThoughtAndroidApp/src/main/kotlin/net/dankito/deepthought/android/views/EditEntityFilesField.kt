package net.dankito.deepthought.android.views

import android.Manifest
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
import net.dankito.deepthought.data.FileManager
import net.dankito.deepthought.model.FileLink
import java.io.File
import javax.inject.Inject


class EditEntityFilesField : EditEntityField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @Inject
    protected lateinit var fileManager: FileManager


    private var originalFiles: MutableCollection<FileLink> = ArrayList()

    private lateinit var permissionsManager: IPermissionsManager

    private var fileChooserDialog: FileChooserDialog? = null

    private val attachedFilesAdapter = FilesRecyclerAdapter()


    init {
        AppComponent.component.inject(this)

        rcySearchResult.adapter = attachedFilesAdapter
        attachedFilesAdapter.itemClickListener = { showFile(it) }
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

        (txtEntityFieldName.layoutParams as? MarginLayoutParams)?.setLeftMargin(2) // so that is has some indent as list item's txtFileName

        (btnEntityFieldAction.layoutParams as? LayoutParams)?.topMargin = 0
    }

    override fun viewClicked() {
        super.viewClicked()

        selectFileToAdd()
    }


    fun setFiles(originalFiles: MutableCollection<FileLink>, permissionsManager: IPermissionsManager) {
        this.originalFiles = originalFiles
        this.permissionsManager = permissionsManager

        attachedFilesAdapter.items = ArrayList(originalFiles) // make a copy to not edit original files
    }


    private fun selectFileToAdd() {
        if(permissionsManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            selectFileToAddWithPermissionGranted()
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionsManager.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    context.getString(R.string.edit_entity_files_field_read_files_permission_rational))  { _, isGranted ->
                selectFileToAddWithPermissionGranted()
            }
        }
    }

    private fun selectFileToAddWithPermissionGranted() {
        if(fileChooserDialog == null) {
            fileChooserDialog = FileChooserDialog(context)
        }

        fileChooserDialog?.selectFile { file ->
            addFile(file)
        }
    }

    private fun addFile(file: File) {
        val localFile = fileManager.createLocalFile(file)
        attachedFilesAdapter.addItem(localFile)

        updateDidValueChange(didCollectionChange(originalFiles, attachedFilesAdapter.items))
    }


    // TODO: move to Presenter / Router
    private fun showFile(file: FileLink) {
//        val intent = Intent(this, ViewPdfActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//        val parameters = ViewPdfActivityParameters(file, source)
//        val id = parameterHolder.setParameters(parameters)
//
//        intent.putExtra(BaseActivity.ParametersId, id)
//
//        startActivity(intent)
    }

}