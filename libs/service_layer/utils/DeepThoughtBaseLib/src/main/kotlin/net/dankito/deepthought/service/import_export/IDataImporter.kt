package net.dankito.deepthought.service.import_export

import net.dankito.deepthought.model.Entry
import java.io.File


interface IDataImporter {

    val name: String
        get


    fun importAsync(bibTeXFile: File, done: (Collection<Entry>) -> Unit)

    fun import(bibTeXFile: File): Collection<Entry>

}