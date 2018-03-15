package net.dankito.deepthought.android.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.activity_edit_item.*
import kotlinx.android.synthetic.main.view_floating_action_button_item_fields.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditItemActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditItemActivityResult
import net.dankito.deepthought.android.activities.arguments.EditSourceActivityResult
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.OnSwipeTouchListener
import net.dankito.deepthought.android.views.*
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.getPlainTextForHtml
import net.dankito.deepthought.model.fields.ItemField
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditItemPresenter
import net.dankito.filechooserdialog.service.IPermissionsService
import net.dankito.filechooserdialog.service.PermissionsService
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ItemService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IThreadPool
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


abstract class EditItemActivityBase : BaseActivity(), IEditItemView {

    companion object {
        private const val CHANGED_FIELDS_INTENT_EXTRA_NAME = "CHANGED_FIELDS"

        private const val FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_TAGS_PREVIEW"
        private const val FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_SOURCE_PREVIEW"
        private const val FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_SUMMARY_PREVIEW"
        private const val FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME = "FORCE_SHOW_FILES_PREVIEW"

        const val ResultId = "EDIT_ITEM_ACTIVITY_RESULT"

        private const val ShowHideEditTagsAnimationDurationMillis = 250L

        private val log = LoggerFactory.getLogger(EditItemActivityBase::class.java)
    }


    @Inject
    protected lateinit var itemService: ItemService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var itemPersister: ItemPersister

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var threadPool: IThreadPool

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var eventBus: IEventBus


    private var originalTags: MutableCollection<Tag>? = null


    protected lateinit var itemToSave: Item

    protected val tagsOnItem: MutableList<Tag> = ArrayList()

    private val changedFields = HashSet<ItemField>()

    private var forceShowTagsPreview = false

    private var forceShowSourcePreview = false

    private var forceShowSummaryPreview = false

    private var forceShowFilesPreview = false


    protected val presenter: EditItemPresenter

    protected var isEditingTagsOnItem = false


    protected val contextHelpUtil = ContextHelpUtil()

    private val toolbarUtil = ToolbarUtil()

    private val permissionsManager: IPermissionsService

    protected var mnSaveItem: MenuItem? = null

    protected var mnDeleteExistingItem: MenuItem? = null

    protected var mnToggleReaderMode: MenuItem? = null

    protected var mnSaveItemExtractionResultForLaterReading: MenuItem? = null

    protected var mnShareItem: MenuItem? = null

    private lateinit var floatingActionMenu: EditItemActivityFloatingActionMenuButton


    private val dataManager: DataManager


    protected abstract fun showParameters(parameters: EditItemActivityParameters)

    protected abstract fun restoreEntity(savedInstanceState: Bundle)

    protected abstract fun saveState(outState: Bundle)

    override fun getItemExtractionResult(): ItemExtractionResult? {
        return null
    }


    init {
        AppComponent.component.inject(this)

        dataManager = itemService.dataManager

        presenter = EditItemPresenter(itemPersister, readLaterArticleService, clipboardService, router)

        permissionsManager = PermissionsService(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parameterHolder.setActivityResult(ResultId, EditItemActivityResult())

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        if(savedInstanceState == null) {
            (getParameters() as? EditItemActivityParameters)?.let { showParameters(it) }
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        val itemFields = ItemField.values()
        savedInstanceState.getIntArray(CHANGED_FIELDS_INTENT_EXTRA_NAME)?.forEach { ordinal ->
            if(ordinal < itemFields.size) {
                changedFields.add(itemFields[ordinal])
            }
        }

        this.forceShowTagsPreview = savedInstanceState.getBoolean(FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowSourcePreview = savedInstanceState.getBoolean(FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowSummaryPreview = savedInstanceState.getBoolean(FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME, false)
        this.forceShowFilesPreview = savedInstanceState.getBoolean(FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME, false)

        restoreEntity(savedInstanceState)

        floatingActionMenu.restoreInstanceState(savedInstanceState)

        setMenuSaveItemVisibleStateOnUIThread()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            saveState(outState)

            outState.putIntArray(CHANGED_FIELDS_INTENT_EXTRA_NAME, changedFields.map { it.ordinal }.toIntArray())

            outState.putBoolean(FORCE_SHOW_TAGS_PREVIEW_INTENT_EXTRA_NAME, forceShowTagsPreview)
            outState.putBoolean(FORCE_SHOW_SOURCE_PREVIEW_INTENT_EXTRA_NAME, forceShowSourcePreview)
            outState.putBoolean(FORCE_SHOW_SUMMARY_PREVIEW_INTENT_EXTRA_NAME, forceShowSummaryPreview)
            outState.putBoolean(FORCE_SHOW_FILES_PREVIEW_INTENT_EXTRA_NAME, forceShowFilesPreview)

            floatingActionMenu.saveInstanceState(outState)
        }
    }


    private fun setupUI() {
        setContentView(R.layout.activity_edit_item)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }

        lytSummaryPreview.setFieldNameOnUiThread(R.string.activity_edit_item_title_summary_label) { didSummaryChange -> summaryChanged(didSummaryChange) }
        lytSummaryPreview.fieldValueFocusChangedListener = { hasFocus ->
            if(hasFocus == false) {
                summaryChanged(lytSummaryPreview.didValueChange)
            }
        }

        lytSourcePreview.didValueChangeListener = { didSourceTitleChange -> updateItemFieldChangedOnUIThread(ItemField.SourceTitle, didSourceTitleChange) }
        lytSourcePreview.didSecondaryInformationValueChangeListener = { updateItemFieldChangedOnUIThread(ItemField.Indication, it) }

        lytTagsPreview.didValueChangeListener = { didTagsChange ->
            itemPropertySet()
            updateItemFieldChangedOnUIThread(ItemField.Tags, didTagsChange)
        }
        lytTagsPreview.fieldValueFocusChangedListener = { hasFocus -> tagsPreviewFocusChanged(hasFocus) }
        lytTagsPreview.saveChangesListener = {
            if(mnSaveItem?.isEnabled == true) {
                saveItemAndCloseDialog()
            }
        }

        lytFilesPreview.didValueChangeListener = { didFilesChange ->
            itemPropertySet()
            updateItemFieldChangedOnUIThread(ItemField.Files, didFilesChange)
        }

        floatingActionMenu = EditItemActivityFloatingActionMenuButton(findViewById(R.id.floatingActionMenu) as FloatingActionMenu, { addTagsToItem() },
                { addSourceToItem() }, { addSummaryToItem() }, { addFilesToItem() } )

        itemContentView.didContentChangeListener = { didChange ->
            updateItemFieldChanged(ItemField.Content, didChange)
        }

        itemContentView.fullscreenGestureListener = { swipeDirection ->
            when(swipeDirection) {
                OnSwipeTouchListener.SwipeDirection.Left ->  presenter.returnToPreviousView()
                OnSwipeTouchListener.SwipeDirection.Right -> editTagsOnItem()
            }
        }
    }

    private fun addTagsToItem() {
        editTagsOnItem()

        forceShowTagsPreview = true
        setTagsOnItemPreviewOnUIThread()
    }

    private fun addSourceToItem() {
        editSource()

        forceShowSourcePreview = true
        setSourcePreviewOnUIThread()
    }

    private fun addSummaryToItem() {
        forceShowSummaryPreview = true
        updateShowSummaryPreviewOnUiThread()

        lytSummaryPreview.startEditing()
    }

    private fun addFilesToItem() {
        forceShowFilesPreview = true
        setFilesPreviewOnUIThread()

        lytFilesPreview.selectFilesToAdd()
    }

    override fun extractedContentOnUiThread(extractionResult: ItemExtractionResult) {
        // updates source and summary, but avoids that extracted content gets shown (this is important according to our
        // lawyer, user must click on toggleReaderMode menu first)
        editItem(extractionResult.item, extractionResult.source, extractionResult.series, extractionResult.tags, extractionResult.files, false)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        (getAndClearResult(EditSourceActivity.ResultId) as? EditSourceActivityResult)?.let { result ->
            lytSourcePreview.editingSourceDone(result)
        }

        itemContentView.onResume(lytSourcePreview.source)

        lytSourcePreview.viewBecomesVisible()
        lytTagsPreview.viewBecomesVisible()
        lytFilesPreview.viewBecomesVisible()
    }

    private fun summaryChanged(didSummaryChange: Boolean) {
        itemPropertySet()

        updateItemFieldChanged(ItemField.TitleOrSummary, didSummaryChange)
    }

    private fun sourceChanged(source: Source?) {
        updateItemFieldChangedOnUIThread(ItemField.Source, source != lytSourcePreview.originalSource)

        itemPropertySet() // TODO: still senseful?

        updateShowMenuItemShareItem()
    }

    private fun editSource() {
        lytSourcePreview.visibility = View.VISIBLE
        lytSourcePreview.startEditing()
    }

    private fun editTagsOnItem() {
        lytTagsPreview.visibility = View.VISIBLE
        lytTagsPreview.startEditing()
    }

    private fun updateItemFieldChanged(field: ItemField, didChange: Boolean) {
        runOnUiThread {
            updateItemFieldChangedOnUIThread(field, didChange)
        }
    }

    private fun updateItemFieldChangedOnUIThread(field: ItemField, didChange: Boolean) {
        if(didChange) {
            changedFields.add(field)
        }
        else {
            changedFields.remove(field)
        }

        setMenuSaveItemVisibleStateOnUIThread()
    }

    private fun setMenuSaveItemVisibleStateOnUIThread() {
        mnSaveItem?.isVisible = isEntitySavable()
    }

    protected open fun isEntitySavable(): Boolean {
        return true // ItemExtractionResult and ReadLaterArticle always can be saved, only EditItemActivity has to set this value
    }

    private fun itemPropertySet() {
        val localSettings = itemService.dataManager.localSettings

        if(localSettings.didShowAddItemPropertiesHelp == false && itemContentView.currentValue.isBlank() == false) {
            localSettings.didShowAddItemPropertiesHelp = true
            itemService.dataManager.localSettingsUpdated()
        }
    }
    private fun shouldShowOnboardingForItemProperties(): Boolean {
        return itemService.dataManager.localSettings.didShowAddItemPropertiesHelp == false &&
                lytTagsPreview.visibility == View.GONE && lytSourcePreview.visibility == View.GONE &&
                lytSummaryPreview.visibility == View.GONE && lytFilesPreview.visibility == View.GONE
    }


    private fun setSummaryPreviewOnUIThread(summaryToEdit: String) {
        val alsoShowTitleInCaption = lytSourcePreview.source?.url == null && summaryToEdit.length < 35 // TODO: shouldn't it be sourceToEdit == null ?
        lytSummaryPreview.setFieldNameOnUiThread(if(alsoShowTitleInCaption) R.string.activity_edit_item_title_summary_label else R.string.activity_edit_item_summary_only_label)

        lytSummaryPreview.setFieldValueOnUiThread(summaryToEdit.getPlainTextForHtml())

        if(summaryToEdit.isBlank()) {
//            lytSummaryPreview.setOnboardingTextOnUiThread(R.string.activity_edit_item_summary_onboarding_text)
        }

        updateShowSummaryPreviewOnUiThread()

        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun updateShowSummaryPreviewOnUiThread() {
        val showSummaryPreview = (this.forceShowSummaryPreview || lytSummaryPreview.getCurrentFieldValue().isEmpty() == false) && isEditingTagsOnItem == false

        lytSummaryPreview.visibility = if(showSummaryPreview) View.VISIBLE else View.GONE
        if(fabEditItemSummary.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemSummary.visibility = if(showSummaryPreview) View.GONE else View.VISIBLE
        }
    }

    private fun setSourcePreviewOnUIThread() {
        updateShowSourcePreviewOnUiThread()

        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()

        updateShowMenuItemShareItem()
    }

    private fun updateShowSourcePreviewOnUiThread() {
        val showSourcePreview = (this.forceShowSourcePreview || lytSourcePreview.source != null) && isEditingTagsOnItem == false

        lytSourcePreview.visibility = if(showSourcePreview) View.VISIBLE else View.GONE
        if(fabEditItemSource.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemSource.visibility = if(showSourcePreview) View.GONE else View.VISIBLE
        }
    }

    private fun setFilesPreviewOnUIThread() {
        val showFilesPreview = (this.forceShowFilesPreview || lytFilesPreview.getEditedFiles().size > 0) && isEditingTagsOnItem == false

        lytFilesPreview.visibility = if(showFilesPreview) View.VISIBLE else View.GONE
        if(fabEditItemFiles.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemFiles.visibility = if(showFilesPreview) View.GONE else View.VISIBLE
        }
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun updateShowMenuItemShareItem() {
        mnShareItem?.isVisible = lytSourcePreview.source?.url.isNullOrBlank() == false
    }

    private fun tagsPreviewFocusChanged(hasFocus: Boolean) {
        if(hasFocus) {
            lytTagsPreview.visibility = View.VISIBLE

            if(lytSourcePreview.visibility == View.VISIBLE || lytSummaryPreview.visibility == View.VISIBLE || lytFilesPreview.visibility == View.VISIBLE) {
                lytTagsPreview.executeActionAfterMeasuringHeight {
                    playHideOtherItemFieldsPreviewExceptTagsAnimation()
                }
            }

            isEditingTagsOnItem = true
            setFloatingActionButtonVisibilityOnUIThread()
        }
        else {
            isEditingTagsOnItem = false
            restoreLayoutItemFieldsPreview()
            setFloatingActionButtonVisibilityOnUIThread()
        }
    }

    private fun restoreLayoutItemFieldsPreview() {
        if(lytSourcePreview.measuredHeight > 0) { // only if it has been visible before
            lytSourcePreview.y = lytSourcePreview.top.toFloat()
            lytSourcePreview.visibility = View.VISIBLE
        }

        if(lytSummaryPreview.measuredHeight > 0) {
            lytSummaryPreview.y = lytSummaryPreview.top.toFloat()
            lytSummaryPreview.visibility = View.VISIBLE
        }

        if(lytFilesPreview.measuredHeight > 0) {
            lytFilesPreview.y = lytFilesPreview.top.toFloat()
            lytFilesPreview.visibility = View.VISIBLE
        }
    }

    private fun playHideOtherItemFieldsPreviewExceptTagsAnimation() {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(createAnimatorsToHideOtherItemFieldsPreviewExceptTags())

        animatorSet.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator?) { }

            override fun onAnimationRepeat(animation: Animator?) { }

            override fun onAnimationCancel(animation: Animator?) { }

            override fun onAnimationEnd(animation: Animator?) {
                forceLayoutUpdateAfterHideOtherItemFieldsPreviewExceptTagsAnimation()
            }

        })

        animatorSet.start()
    }

    // don't know why we have to force layout to update
    private fun forceLayoutUpdateAfterHideOtherItemFieldsPreviewExceptTagsAnimation() {
        lytSourcePreview.visibility = View.GONE
        lytSummaryPreview.visibility = View.GONE
        lytFilesPreview.visibility = View.GONE
        lytTagsPreview.y = lytTagsPreview.top.toFloat()

        lytItemFieldsPreview.invalidate()
        lytItemFieldsPreview.forceLayout()
        lytItemFieldsPreview.invalidate()
        lytItemFieldsPreview.forceLayout()
    }

    private fun createAnimatorsToHideOtherItemFieldsPreviewExceptTags(): ArrayList<Animator> {
        val animators = ArrayList<Animator>()
        val interpolator = AccelerateInterpolator()

        if(lytSourcePreview.visibility == View.VISIBLE) {
            val sourcePreviewYAnimator = ObjectAnimator
                    .ofFloat(lytSourcePreview, View.Y, lytSourcePreview.top.toFloat(), -1 * lytSourcePreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            sourcePreviewYAnimator.interpolator = interpolator

            animators.add(sourcePreviewYAnimator)
        }

        if(lytSummaryPreview.visibility == View.VISIBLE) {
            val summaryPreviewYAnimator = ObjectAnimator
                    .ofFloat(lytSummaryPreview, View.Y, lytSummaryPreview.top.toFloat(), -1 * lytSummaryPreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            summaryPreviewYAnimator.interpolator = interpolator

            animators.add(summaryPreviewYAnimator)
        }

        if(lytFilesPreview.visibility == View.VISIBLE) {
            val summaryPreviewYAnimator = ObjectAnimator
                    .ofFloat(lytFilesPreview, View.Y, lytFilesPreview.top.toFloat(), -1 * lytFilesPreview.measuredHeight.toFloat())
                    .setDuration(ShowHideEditTagsAnimationDurationMillis)
            summaryPreviewYAnimator.interpolator = interpolator

            animators.add(summaryPreviewYAnimator)
        }

        val location = IntArray(2)
        lytItemFieldsPreview.getLocationOnScreen(location)

        val tagsPreviewYAnimator = ObjectAnimator
                .ofFloat(lytTagsPreview, View.Y, lytTagsPreview.top.toFloat(), location[1].toFloat())
                .setDuration(ShowHideEditTagsAnimationDurationMillis)
        tagsPreviewYAnimator.interpolator = interpolator
        animators.add(tagsPreviewYAnimator)

        return animators
    }

    private fun setTagsOnItemPreviewOnUIThread() {
        lytTagsPreview.setTagsToEdit(tagsOnItem, this)

        val showTagsPreview = this.forceShowTagsPreview || tagsOnItem.size > 0

        lytTagsPreview.visibility = if(showTagsPreview) View.VISIBLE else View.GONE
        if(fabEditItemTags.visibility != View.INVISIBLE) { // visibility already set by FloatingActionMenu
            fabEditItemTags.visibility = if (showTagsPreview) View.GONE else View.VISIBLE
        }
        setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread()
    }

    private fun setOnboardingTextAndFloatingActionButtonVisibilityOnUIThread(showContentOnboarding: Boolean? = null) {
        itemContentView.mayShowOnboardingTextVisibilityOnUIThread(shouldShowOnboardingForItemProperties(), showContentOnboarding)

        setFloatingActionButtonVisibilityOnUIThread()
    }

    override fun setFloatingActionButtonVisibilityOnUIThread() {
        val forceHidingFloatingActionButton = isEditingTagsOnItem || itemContentView.shouldHideFloatingActionButton
        // when user comes to EditItemDialog, don't show floatingActionMenu till some content has been entered. She/he should focus on the content
        val hasUserEverEnteredSomeContent = dataManager.localSettings.didShowAddItemPropertiesHelp || itemContentView.currentValue.isBlank() == false

        floatingActionMenu.setVisibilityOnUIThread(forceHidingFloatingActionButton, hasUserEverEnteredSomeContent)
    }


    override fun onPause() {
        lytSourcePreview.viewGetsHidden()
        lytTagsPreview.viewGetsHidden()
        lytFilesPreview.viewGetsHidden()

        itemContentView.onPause()

        lytSummaryPreview.stopEditing()

        super.onPause()
    }

    override fun onDestroy() {
        itemContentView.onDestroy()

        parameterHolder.clearActivityResults(EditSourceActivity.ResultId)

        super.onDestroy()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if(floatingActionMenu.handlesTouch(event)) { // close menu when menu is opened and touch is outside floatingActionMenuButton
            return true
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onBackPressed() {
        // TODO: haveAllFieldsBeenCleared() doesn't reflect currently set content!
        if(itemContentView.handlesBackButtonPress(getItemExtractionResult() != null || itemToSave.isPersisted() == true || haveAllFieldsBeenCleared() == false)) {
            return
        }
        else if(floatingActionMenu.handlesBackButtonPress()) {
            return
        }
        else if(lytSourcePreview.handlesBackButtonPress()) {
            return
        }
        else if(lytTagsPreview.handlesBackButtonPress()) {
            return
        }

        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }

    protected fun haveAllFieldsBeenCleared(): Boolean {
        return itemContentView.currentValue.isBlank() && tagsOnItem.isEmpty() && lytSourcePreview.source == null
                && lytSummaryPreview.getCurrentFieldValue().isEmpty() && lytFilesPreview.getEditedFiles().size == 0
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(itemContentView.isInEditContentMode) { // TODO to decide which menu to inflate; remove itemContentView.isInEditContentMode
            createEditHtmlOptionsMenu(menu)
        }
        else {
            createViewHtmlOptionsMenu(menu)
        }

        return true
    }

    private fun createEditHtmlOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.activity_edit_item_edit_content_menu, menu)
    }

    protected open fun createViewHtmlOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.activity_edit_item_menu, menu)

        mnSaveItem = menu.findItem(R.id.mnSaveItem)

        mnToggleReaderMode = menu.findItem(R.id.mnToggleReaderMode)
        mnToggleReaderMode?.let { itemContentView.optionMenuCreated(it, toolbarUtil) }

        mnSaveItemExtractionResultForLaterReading = menu.findItem(R.id.mnSaveItemExtractionResultForLaterReading)

        mnShareItem = menu.findItem(R.id.mnShareItem)
        updateShowMenuItemShareItem()

        setMenuSaveItemVisibleStateOnUIThread()

        adjustViewHtmlOptionsMenu(menu) // adjusting icons has to be done before toolbarUtil.setupActionItemsLayout() gets called

        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }
    }

    protected open fun adjustViewHtmlOptionsMenu(menu: Menu) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(itemContentView.handleOptionsItemSelected(item)) {
            return true
        }

        when(item.itemId) {
            android.R.id.home -> {
                askIfUnsavedChangesShouldBeSavedAndCloseDialog()
                return true
            }
            R.id.mnSaveItem -> {
                saveItemAndCloseDialog()
                return true
            }
            R.id.mnShareItem -> {
                showShareItemPopupMenu()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showShareItemPopupMenu() {
        val overflowMenuButton = getOverflowMenuButton()
        if(overflowMenuButton == null) {
            return
        }

        val popup = PopupMenu(this, overflowMenuButton)

        popup.menuInflater.inflate(R.menu.share_item_menu, popup.menu)

        val source = lytSourcePreview.source
        if(source == null || source.url.isNullOrBlank()) {
            popup.menu.findItem(R.id.mnShareItemSourceUrl).isVisible = false
        }

        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.mnShareItemSourceUrl -> shareSourceUrl()
                R.id.mnShareItemContent -> shareItemContent()
            }
            true
        }

        popup.show()
    }

    private fun getOverflowMenuButton(): View? {
        for(i in 0..toolbar.childCount - 1) { // OverflowMenuButton is child of ActionMenuView which is child of toolbar (both don't have an id so i cannot search for them)
            val child = toolbar.getChildAt(i)

            if(child is ActionMenuView) {
                for(j in 0..child.childCount) {
                    val actionMenuViewChild = child.getChildAt(j)

                    if(actionMenuViewChild is AppCompatImageView && actionMenuViewChild is ActionMenuView.ActionMenuChildView) {
                        return actionMenuViewChild
                    }
                }
            }
        }

        return null
    }

    private fun shareSourceUrl() {
        lytSourcePreview.source?.let { source ->
            presenter.shareSourceUrl(source)
        }
    }

    private fun shareItemContent() {
        val currentSource = lytSourcePreview.source

        presenter.shareItem(Item(itemContentView.currentValue, lytSummaryPreview.getCurrentFieldValue()), tagsOnItem,
                Source(lytSourcePreview.getCurrentFieldValue(), currentSource?.url ?: "", currentSource?.publishingDate, currentSource?.subTitle), lytSourcePreview.series)
    }


    private fun saveItemAndCloseDialog() {
        mnSaveItem?.isEnabled = false // disable to that save cannot be pressed a second time
        beforeSavingItem()

        saveItemAsync { successful ->
            if(successful) {
                setActivityResult(EditItemActivityResult(didSaveItem = true, savedItem = itemToSave))
            }
            else {
                mnSaveItem?.isEnabled = true
            }

            savingItemDone(successful)
        }
    }

    protected open fun beforeSavingItem() {

    }

    private fun saveItemAsync(callback: (Boolean) -> Unit) {
        val content = itemContentView.currentValue
        val summary = lytSummaryPreview.getCurrentFieldValue()
        val editedSource = updateSource()
        val editedSeries = lytSourcePreview.series

        // TODO: save extracted content when in reader mode and webSiteHtml when not in reader mode
        // TODO: contentToEdit show now always contain the correct value depending on is or is not in reader mode, doesn't it?

        updateItem(itemToSave, content, summary)
        presenter.saveItemAsync(itemToSave, editedSource, editedSeries, tagsOnItem, lytFilesPreview.getEditedFiles()) { successful ->
            callback(successful)
        }
    }

    protected open fun savingItemDone(successful: Boolean) {
        if(successful) {
            closeDialog()
        }
    }



    protected fun setActivityResult(result: EditItemActivityResult) {
        parameterHolder.setActivityResult(ResultId, result)
    }

    protected fun updateItem(item: Item, content: String, summary: String) {
        item.content = content
        item.summary = summary

        if(changedFields.contains(ItemField.Indication)) {
            item.indication = lytSourcePreview.getEditedSecondaryInformation()
        }

        if(changedFields.contains(ItemField.Tags)) {
            tagsOnItem.clear()
            tagsOnItem.addAll(lytTagsPreview.applyChangesAndGetTags())
        }
    }

    protected fun updateSource(): Source? {
        var source = lytSourcePreview.source

        if(changedFields.contains(ItemField.SourceTitle)) {
            source?.title = lytSourcePreview.getEditedValue() ?: ""
        }

        if(source?.isPersisted() == false && lytSourcePreview.getEditedValue().isNullOrBlank()) {
            source = null
            resetSeries() // TODO: is this really necessary as we then pass lytSourcePreview.series to ItemPersister -> does editSourceField.seriesToEdit know now that series changed?
        }

        if(source != lytSourcePreview.originalSource) {
            resetSeries() // TODO: is this really necessary as we then pass lytSourcePreview.series to ItemPersister -> does editSourceField.seriesToEdit know now that series changed?
        }

        return source
    }

    protected open fun resetSeries() {

    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(hasUnsavedChanges) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    protected val hasUnsavedChanges: Boolean
        get() {
            return changedFields.size > 0
        }

    private fun askIfUnsavedChangesShouldBeSaved() {
        val config = ConfirmationDialogConfig(true, getString(R.string.action_cancel), true, getString(R.string.action_dismiss), getString(R.string.action_save))
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_item_alert_message_item_contains_unsaved_changes), config = config) { selectedButton ->
            runOnUiThread {
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    saveItemAndCloseDialog()
                }
                else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
                    closeDialog()
                }
            }
        }
    }

    protected fun closeDialog() {
        finish()
    }


    protected fun editItem(item: Item, source: Source?, series: Series? = source?.series, tags: MutableCollection<Tag>, files: MutableCollection<FileLink>,
                         updateContentPreview: Boolean = true) {
        itemToSave = item
        itemContentView.initialize(item.content, this)
        originalTags = tags

        if(item.summary.isEmpty() == false) { this.forceShowSummaryPreview = true } // forcing that once it has been shown it doesn't get hidden anymore

        source?.let { this.forceShowSourcePreview = true } // forcing that once it has been shown it doesn't get hidden anymore
        lytSourcePreview.setOriginalSourceToEdit(source, series, item.indication, this) { sourceChanged(it) }

        this.forceShowSourcePreview = forceShowSourcePreview || item.indication.isNotEmpty()

        tags.forEach { tag ->
            if(tagsOnItem.contains(tag) == false) { // to avoid have a tag twice we really have to check each single tag
                tagsOnItem.add(tag)
            }
        }

        forceShowTagsPreview = forceShowTagsPreview || tags.isNotEmpty()

        lytFilesPreview.setFiles(files, permissionsManager)
        forceShowFilesPreview = forceShowFilesPreview || files.isNotEmpty()

        updateDisplayedValuesOnUIThread(source, item.summary, updateContentPreview)
    }

    private fun updateDisplayedValuesOnUIThread(source: Source?, summaryToEdit: String, updateContentPreview: Boolean = true) {
        if(updateContentPreview) {
            itemContentView.setContentPreviewOnUIThread(source)
        }

        setTagsOnItemPreviewOnUIThread()

        setSourcePreviewOnUIThread()

        setSummaryPreviewOnUIThread(summaryToEdit)

        setFilesPreviewOnUIThread()
    }

    private fun restoreTagsOnItemAsync(tagsOnItemIdsString: String) {
        threadPool.runAsync { restoreTagsOnItem(tagsOnItemIdsString) }
    }

    private fun restoreTagsOnItem(tagsOnItemIdsString: String) {
        val restoredTagsOnItem = serializer.deserializeObject(tagsOnItemIdsString, List::class.java, Tag::class.java) as List<Tag>

        tagsOnItem.clear()
        tagsOnItem.addAll(restoredTagsOnItem)

        runOnUiThread { setTagsOnItemPreviewOnUIThread() }
    }


    /*          IEditItemView implementation            */

    override val currentSource: Source?
        get() = lytSourcePreview.source

    override val appBar: AppBarLayout
        get() = appBarLayout

    override val itemFieldsPreview: View
        get() = lytItemFieldsPreview

}
