package net.dankito.deepthought.service.import_export

import net.dankito.deepthought.model.Item
import java.io.File


interface IDataImporter {

    val name: String
        get


    fun importAsync(bibTeXFile: File, done: (Collection<Item>) -> Unit)

    fun import(bibTeXFile: File): Collection<Item>

}