package net.dankito.deepthought.android.dialogs

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import kotlinx.android.synthetic.main.dialog_entities_list_view.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment
import net.dankito.utils.android.ui.dialogs.FullscreenDialogFragment


abstract class EntitiesListViewDialogBase<T : EntitiesListViewFragment<*>> : FullscreenDialogFragment() {

    abstract fun instantiateListViewFragment(): EntitiesListViewFragment<*>

    abstract fun getTitleResourceId(): Int


    private var fragment: EntitiesListViewFragment<*>? = null

    private var rootView: View? = null


    override fun getLayoutId() = R.layout.dialog_entities_list_view


    override fun setupUI(rootView: View) {
        this.rootView = rootView

        rootView.toolbar.setTitle(getTitleResourceId())

        initFragment(activity)

        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(fragment == null && rootView != null) {
            initFragment(activity)
        }
    }

    private fun initFragment(activity: FragmentActivity?) {
        activity?.let {
            this.fragment = instantiateListViewFragment()

            activity.supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, fragment, fragment?.tag).commit()

            rootView?.let { rootView ->
                val toolbar = rootView.toolbar

                fragment?.let { fragment ->
                    fragment.onCreateOptionsMenu(toolbar.menu, activity.menuInflater)
                    toolbar.setOnMenuItemClickListener { fragment.onOptionsItemSelected(it) ?: false }
                }
            }
        }
    }


    override fun handlesBackButtonPress(): Boolean {
        return fragment?.onBackPressed() ?: super.handlesBackButtonPress()
    }


    fun show(fragmentManager: FragmentManager) {
        showInFullscreen(fragmentManager)
    }

}