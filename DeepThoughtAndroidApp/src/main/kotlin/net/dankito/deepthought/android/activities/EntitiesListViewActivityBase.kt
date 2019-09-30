package net.dankito.deepthought.android.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_entities_list_view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.fragments.EntitiesListViewFragment


abstract class EntitiesListViewActivityBase<T : EntitiesListViewFragment<*>>(private val titleResourceId: Int) : BaseActivity() {

    private lateinit var fragment: EntitiesListViewFragment<*>


    abstract fun instantiateListViewFragment(): EntitiesListViewFragment<*>


    override val windowDataClass = null

    override fun getCurrentWindowData() = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        setContentView(R.layout.dialog_entities_list_view)

        fragment = instantiateListViewFragment()

        supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, fragment).commit()

        setSupportActionBar(toolbar)

//        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)

            actionBar.title = getString(titleResourceId)
        }
    }


    override fun onBackPressed() {
        if(fragment.onBackPressed() == false) {
            super.onBackPressed()
        }
    }

}