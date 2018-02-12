package net.dankito.deepthought.android.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.FileProvider
import android.webkit.MimeTypeMap
import android.widget.Toast
import net.dankito.deepthought.android.R
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.utils.ui.IApplicationsService
import java.io.File




class AndroidApplicationsService(private val context: Context, private val fileManager: FileManager) : IApplicationsService {

    override fun openFileInOsDefaultApplication(file: FileLink) {
        fileManager.getLocalPathForFile(file)?.let { absoluteFile ->
            try {
                val intent = createOpenFileInOsDefaultApplicationIntent(absoluteFile)

                context.startActivity(intent)
            } catch (e: Exception) {
                showErrorMessage(R.string.files_presenter_error_message_no_app_installed_for_this_file_type, absoluteFile?.extension)
            }
        }
    }

    private fun createOpenFileInOsDefaultApplicationIntent(absoluteFile: File): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

        val mimeTypeMap = MimeTypeMap.getSingleton()
        val mimeType = mimeTypeMap.getMimeTypeFromExtension(absoluteFile.extension)

        // use a FileProvide to give access to file also for devices with Android 7 and above, see https://stackoverflow.com/a/38858040
        val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".net.dankito.deepthought.android.provider", absoluteFile)
        intent.setDataAndType(uri, mimeType)

        return intent
    }


    override fun openDirectoryInOsFileBrowser(file: File) {
        try {
            val selectedUri = Uri.parse(file.absolutePath)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setDataAndType(selectedUri, "resource/folder")

            if(intent.resolveActivityInfo(context.packageManager, 0) != null) {
                context.startActivity(intent)
            }
            else {
                // if you reach this place, it means there is no any file
                // explorer app installed on your device
                showErrorMessage(R.string.files_presenter_error_message_no_file_explorer_app_installed)
            }
        } catch(e: Exception) {
            showErrorMessage(R.string.files_presenter_error_message_could_not_open_directory, e.localizedMessage)
        }
    }

    private fun showErrorMessage(messageStringResourceId: Int, vararg formatArgs: String) {
        Toast.makeText(context, context.getString(messageStringResourceId, *formatArgs), Toast.LENGTH_LONG).show()
    }

}