package net.dankito.jpa.couchbaselite

import com.couchbase.lite.JavaContext

import java.io.File


class DeepThoughtJavaContext : JavaContext {

    protected var databaseDirectory: String? = null


    constructor() : super() {}

    constructor(databaseDirectory: String) : super(databaseDirectory) {
        this.databaseDirectory = databaseDirectory
    }


    override fun getFilesDir(): File {
        if (databaseDirectory == null) {
            return super.getFilesDir()
        }

        val filesDir = File(databaseDirectory!!)
        if (filesDir.isAbsolute) {
            return filesDir
        } else {
            return File(workingDirectory, databaseDirectory!!)
        }
    }

    protected val workingDirectory: String
        get() = System.getProperty("user.dir")

}
