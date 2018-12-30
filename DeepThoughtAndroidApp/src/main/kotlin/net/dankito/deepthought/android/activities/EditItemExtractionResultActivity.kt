package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_edit_item.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditItemActivityResult
import net.dankito.deepthought.model.util.ItemExtractionResult


class EditItemExtractionResultActivity : EditItemActivityBase() {

    companion object {
        private const val ITEM_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ITEM_EXTRACTION_RESULT"
    }


    private lateinit var itemExtractionResult: ItemExtractionResult


    override fun getItemExtractionResult(): ItemExtractionResult? {
        return this.itemExtractionResult
    }

    override fun showParameters(parameters: EditItemActivityParameters) {
        parameters.itemExtractionResult?.let {
            editItemExtractionResult(it)
        }
    }

    override fun restoreEntity(savedInstanceState: Bundle) {
        restoreStateFromDisk(savedInstanceState, ITEM_EXTRACTION_RESULT_INTENT_EXTRA_NAME, ItemExtractionResult::class.java)?.let {
            editItemExtractionResult(it)
        }
    }

    private fun editItemExtractionResult(extractionResult: ItemExtractionResult, updateContentPreview: Boolean = true) {
        this.itemExtractionResult = extractionResult

        editItem(extractionResult.item, extractionResult.source, extractionResult.series, extractionResult.tags, extractionResult.files, updateContentPreview)
    }

    override fun saveState(outState: Bundle) {
        serializeStateToDiskIfNotNull(outState, ITEM_EXTRACTION_RESULT_INTENT_EXTRA_NAME, itemExtractionResult)
    }


    override fun resetSeries() {
        itemExtractionResult.series = null
    }


    override fun adjustViewHtmlOptionsMenu(menu: Menu) {
        mnSaveItemExtractionResultForLaterReading?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mnSaveItemExtractionResultForLaterReading -> {
                saveItemExtractionResultForLaterReadingAndCloseDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun saveItemExtractionResultForLaterReadingAndCloseDialog() {
        mnSaveItem?.isEnabled = false // disable to that save cannot be pressed a second time
        mnSaveItemExtractionResultForLaterReading?.isEnabled = false

        saveItemForLaterReading { successful ->
            if(successful) {
                closeDialog()
            }
            else {
                mnSaveItem?.isEnabled = true
                mnSaveItemExtractionResultForLaterReading?.isEnabled = true
            }
        }
    }

    private fun saveItemForLaterReading(callback: (Boolean) -> Unit) {
        itemContentView.getCurrentHtmlAsync { content ->
            val summary = lytSummaryPreview.getCurrentFieldValue()

            updateItem(itemExtractionResult.item, content, summary)
            itemExtractionResult.source = updateSource()
            itemExtractionResult.series = lytSourcePreview.series
            itemExtractionResult.tags = tagsOnItem
            itemExtractionResult.files = lytFilesPreview.getEditedFiles().toMutableList()

            presenter.saveItemExtractionResultForLaterReading(itemExtractionResult)

            runOnUiThread {
                setActivityResult(EditItemActivityResult(didSaveForLaterReading = true, savedItem = itemExtractionResult.item))
                callback(true)
            }
        }
    }

}