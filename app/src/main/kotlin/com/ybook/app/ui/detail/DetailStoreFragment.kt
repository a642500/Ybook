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
import com.ybook.app.bean.SearchResponse
import com.ybook.app.bean.BookItem
import android.support.v4.app.Fragment
import android.widget.ListView
import android.widget.BaseAdapter
import com.ybook.app.bean.DetailResponse
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.widget.TextView
import com.ybook.app.R
import com.ybook.app.ui.detail.BookDetailActivity.OnDetail

/**
 * Created by carlos on 9/14/14.
 */
public class DetailStoreFragment(val searchObject: SearchResponse.SearchObject?, val bookItem: BookItem?) : Fragment(), OnDetail {
    var mListView: ListView? = null
    var mAdapter: BaseAdapter? = null

    override fun onRefresh(detail: DetailResponse) {
        mDetailResponse = detail
        refresh()
    }

    var mDetailResponse: DetailResponse? = bookItem?.detailResponse
    var mNoHintView: View? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.detail_store_pager_frag, container, false)
        mListView = view.findViewById(R.id.contentListView) as ListView
        mNoHintView = view id R.id.nothingHint
        mAdapter = object : BaseAdapter() {
            override fun getCount() = mDetailResponse?.libInfo?.size ?: 0
            override fun getItem(position: Int) = mDetailResponse?.libInfo?.get(position)
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                val v = convertView ?: inflater.inflate(R.layout.detail_store_item, parent, false)
                val ordBtn = v.findViewById(R.id.ordBtn)
                if (mDetailResponse == null) {
                    ordBtn.setEnabled(false)
                } else {
                    ordBtn.setEnabled(true)
                    (v id R.id.textViewStoreLocation) as TextView setText getItem(position)!!.libLocation
                    (v id R.id.textViewStoreStatus ) as TextView setText getItem(position)!!.libStatus
                    ordBtn setOnClickListener { v ->//TODO order

                    }
                }
                return v
            }

        }
        mListView!!.setAdapter(mAdapter)
        refresh()
        return view
    }

    fun onLoad(detail: DetailResponse) {
        mDetailResponse = detail
        if (isAdded() ) refresh()
    }

    override fun onError() {

    }

    override fun onResume() {
        super<Fragment>.onResume()
        refresh()
    }

    fun showEmpty() = mNoHintView?.setVisibility(View.VISIBLE)
    fun removeEmpty() = mNoHintView?.setVisibility(View.GONE)

    fun refresh() {
        if (mDetailResponse == null || mAdapter?.isEmpty() ?: true) showEmpty()
        else {
            mAdapter?.notifyDataSetInvalidated()
            removeEmpty()
        }
    }
}