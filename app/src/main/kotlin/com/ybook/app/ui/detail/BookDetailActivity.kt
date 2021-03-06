/*
    Copyright 2015 Carlos

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package com.ybook.app.ui.detail

import com.ybook.app.id
import com.ybook.app.swipebacklayout.SwipeBackActivity
import android.view.View
import com.melnykov.fab.FloatingActionButton
import com.ybook.app.bean.SearchResponse
import com.ybook.app.bean.BookItem
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.ybook.app.bean.BookListResponse
import android.widget.TextView
import android.widget.ImageView
import android.support.v4.view.ViewPager
import com.ybook.app.viewpagerindicator.TabPageIndicator
import android.view.MenuItem
import com.ybook.app.bean.DetailResponse
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import com.ybook.app.util.BooksListUtil
import com.ybook.app.R
import com.ybook.app.ui.home
import com.umeng.analytics.MobclickAgent
import com.squareup.picasso.Picasso
import android.widget.Toast
import de.keyboardsurfer.android.widget.crouton.Style
import de.keyboardsurfer.android.widget.crouton.Crouton
import com.ybook.app
import com.ybook.app.bean
import com.ybook.app.net
import com.ybook.app.net.PostHelper
import com.ybook.app.ui
import android.app.Activity
import com.ybook.app.net.DetailRequest

/**
 * This activity is to display the detail of book of the search results.
 * Created by Carlos on 2014/8/1.
 */
public class BookDetailActivity : SwipeBackActivity(), View.OnClickListener {


    var mMarkFAB: FloatingActionButton? = null
    private var mSearchObject: SearchResponse.SearchObject? = null
    private var mBookItem: BookItem? = null
    private var mUtil = BooksListUtil.getInstance(this)
    //http://ftp.lib.hust.edu.cn/record=b2673698~S0*chx

    override fun onCreate(savedInstanceState: Bundle?) {
        super<SwipeBackActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.book_details_activity)
        setSupportActionBar(this.id(R.id.toolBar) as Toolbar)
        setResult(RESULT_CODE_UNCHANGED, getIntent())


        val o = getIntent() getSerializableExtra INTENT_SEARCH_OBJECT ?: getIntent().getSerializableExtra(home.KEY_BOOK_LIST_RESPONSE_EXTRA)
        when (o) {
            is SearchResponse.SearchObject -> mSearchObject = o
            is BookItem -> mBookItem = o
            is BookListResponse.BookListObject -> mSearchObject = o.toSearchObject()
            else -> this.finish()
        }
        initViews()
    }

    override fun onResume() {
        super<SwipeBackActivity>.onResume()
        MobclickAgent.onResume(this);
    }

    override fun onPause() {
        super<SwipeBackActivity>.onPause()
        MobclickAgent.onPause(this);
    }

    var titleView: TextView ? = null
    private fun initViews() {
        mMarkFAB = id(R.id.fab) as FloatingActionButton
        val imageView = id(R.id.image_view_book_cover) as ImageView
        titleView = id(R.id.text_view_book_title) as TextView
        val viewPager = id(R.id.detail_viewPager) as ViewPager
        val indicator = id(R.id.detail_viewPager_indicator) as TabPageIndicator

        var title: String?
        if (mSearchObject == null) {
            Picasso.with(this).load(mBookItem!!.detailResponse.coverImageUrl).error(getResources().getDrawable(R.drawable.ic_error)).resizeDimen(R.dimen.cover_height, R.dimen.cover_width).into(imageView)
            title = mBookItem!!.detailResponse.title.trim()
            viewPager setAdapter MyDetailPagerAdapter(getSupportFragmentManager(), null, mBookItem!!)
            if (mBookItem!! isMarked mUtil) mMarkFAB!! setImageResource  R.drawable.fab_star_unlike
            else mMarkFAB!! setImageResource  R.drawable.fab_drawable_star_like
        } else {
            Picasso.with(this).load(mSearchObject!!.coverImgUrl).error(getResources().getDrawable(R.drawable.ic_error)).resizeDimen(R.dimen.cover_height, R.dimen.cover_width).into(imageView)
            title = mSearchObject!!.title.trim()
            viewPager.setAdapter(MyDetailPagerAdapter(getSupportFragmentManager(), mSearchObject!!, null))
            if (mSearchObject!! isMarked mUtil ) mMarkFAB!! setImageResource  R.drawable.fab_star_unlike
            else mMarkFAB!! setImageResource  R.drawable.fab_drawable_star_like

        }
        indicator setViewPager viewPager
        indicator setBackgroundResource R.drawable.indicator_bg_selector
        if (title!!.trim().length() == 0) title = getString(R.string.noTitleHint)
        titleView!! setText title
        setupActionBar()
    }

    private fun setupActionBar() {
        val bar = getSupportActionBar()
        bar.setTitle(mSearchObject?.title ?: mBookItem?.detailResponse?.title)
        bar.setDisplayShowTitleEnabled(true)
        bar setDisplayHomeAsUpEnabled true
        bar setDisplayUseLogoEnabled false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {
            android.R.id.home -> onBackPressed()
        }
        return super<SwipeBackActivity>.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        when (v.getId()) {
            R.id.fab -> {
                if (mBookItem == null) Toast.makeText(this, "loading, please try again when loaded.", Toast.LENGTH_SHORT).show()
                else {
                    mBookItem!!.markOrCancelMarked(mUtil)
                    setResult(RESULT_CODE_CHANGED, getIntent())
                    if (mBookItem!!.isMarked(mUtil)) {
                        Crouton.makeText(this, getResources().getString(R.string.toastMarked), Style.INFO).show()
                        mMarkFAB!! setImageResource  R.drawable.fab_star_unlike
                        MobclickAgent.onEvent(this, app.util.EVENT_ADD_FROM_DETAIL)
                    } else {
                        Crouton.makeText(this, getResources().getString(R.string.toastCancelMark), Style.INFO).show()
                        mMarkFAB!! setImageResource  R.drawable.fab_drawable_star_like
                        MobclickAgent.onEvent(this, app.util.EVENT_DELETE_FROM_DETAIL)
                    }
                }
            }
        }
    }

    fun onRefresh(detail: DetailResponse) {
        titleView!! setText detail.title
    }
    //            R.id.button_addToList -> {
    //            }

    inner class MyDetailPagerAdapter(fm: FragmentManager, searchObject: SearchResponse.SearchObject?, bookItem: BookItem?) : FragmentPagerAdapter(fm) {
        {
            if (searchObject != null) PostHelper.detail(DetailRequest(searchObject.id, searchObject.idType, bean.getLibCode()), object : Handler() {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        net.MSG_SUCCESS -> (msg.obj as DetailResponse).let {
                            mBookItem = it.toBookItem()
                            pagers.forEach { p -> p.onRefresh(it) }
                            this@BookDetailActivity.onRefresh(it)
                        }
                        net.MSG_ERROR -> pagers.forEach { p -> p.onError() }
                    }
                }
            })
        }
        val pagers = array(DetailInfoFragment(searchObject, bookItem), DetailStoreFragment(searchObject, bookItem))
        val titleResId = array(R.string.detailTabTitleInfo, R.string.detailTabTitleStatus)
        override fun getItem(i: Int) = pagers[i]
        override fun getCount() = 2
        override fun getPageTitle(position: Int) = getResources().getString(titleResId[position])

    }

    class object {
        public val INTENT_SEARCH_OBJECT: String = "searchObject"
        public val RESULT_CODE_UNCHANGED: Int = Activity.RESULT_FIRST_USER
        public val RESULT_CODE_CHANGED: Int = Activity.RESULT_FIRST_USER + 1
    }

    trait OnDetail : Fragment {
        public fun onRefresh(detail: DetailResponse)
        public fun onError()
    }
}