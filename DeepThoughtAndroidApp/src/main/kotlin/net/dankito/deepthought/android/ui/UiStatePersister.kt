package net.dankito.deepthought.android.ui

import android.os.Bundle
import net.dankito.utils.io.IFileStorageService
import net.dankito.utils.serialization.ISerializer
import org.slf4j.LoggerFactory
import java.io.File


class UiStatePersister(private val fileStorageService: IFileStorageService, private val serializer: ISerializer) {

    companion object {
        private val log = LoggerFactory.getLogger(UiStatePersister::class.java)
    }


    /**
     * Sometimes objects are too large for bundle in onSaveInstance() which would crash application, so save them in-memory as almost always activity gets destroyed but not Application
     */

    /**
     * When objects are too large to put them into a bundle in onSaveInstance(), write them to disk and put only their temp path to bundle
     */
    fun serializeStateToDiskIfNotNull(outState: Bundle, bundleKey: String, state: Any?) {
        state?.let {
            serializeToTempFileOnDisk(state)?.let { restoreKey ->
                outState.putString(bundleKey, restoreKey)
            }
        }
    }

    /**
     * When objects are too large to put them into a bundle in onSaveInstance(), write them to disk and put only their temp path to bundle
     */
    private fun serializeToTempFileOnDisk(objectToRestore: Any): String? {
        try {
            val serializedObject = serializer.serializeObject(objectToRestore)

            val tempFile = File.createTempFile("ToRestore", ".json")
            fileStorageService.writeToTextFile(serializedObject, tempFile)

            return tempFile.absolutePath
        } catch(e: Exception) { log.error("Could not write object to restore $objectToRestore to disk", e) }

        return null
    }


    fun<T> restoreStateFromDisk(savedInstanceState: Bundle, bundleKey: String, stateClass: Class<T>): T? {
        savedInstanceState.getString(bundleKey)?.let { restoreKey ->
            restoreSerializedObjectFromDisk(restoreKey, stateClass)?.let {
                return it
            }
        }

        return null
    }

    private fun<T> restoreSerializedObjectFromDisk(id: String, objectClass: Class<T>): T? {
        try {
            val file = File(id)
            fileStorageService.readFromTextFile(file)?.let { serializedObject ->
                val deserializedObject = serializer.deserializeObject(serializedObject, objectClass)

                try { file.delete() } catch(ignored: Exception) { }

                return deserializedObject
            }
        } catch(e: Exception) { log.error("Could not restore object of type $objectClass from $id", e) }

        return null
    }

}