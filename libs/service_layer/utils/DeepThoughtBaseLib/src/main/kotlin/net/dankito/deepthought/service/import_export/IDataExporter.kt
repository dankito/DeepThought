package net.dankito.deepthought.service.import_export

import net.dankito.deepthought.model.Item
import java.io.File


interface IDataExporter {

    val name: String
        get


    fun exportAsync(destinationFile: File, items: Collection<Item>)

    fun export(destinationFile: File, items: Collection<Item>)

}