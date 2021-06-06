/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.bookmanstreasure.ui.booksearch

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.raywenderlich.android.bookmanstreasure.R
import com.raywenderlich.android.bookmanstreasure.data.SearchCriteria
import com.raywenderlich.android.bookmanstreasure.databinding.FragmentBookSearchBinding
import com.raywenderlich.android.bookmanstreasure.source.NetworkState
import com.raywenderlich.android.bookmanstreasure.ui.MainActivityDelegate
import com.raywenderlich.android.bookmanstreasure.ui.workdetails.WorkDetailsViewModel
import com.raywenderlich.android.bookmanstreasure.util.initToolbar

class BookSearchFragment : Fragment() {
  private var _binding: FragmentBookSearchBinding? = null
  private val binding get() = _binding!!

  private val viewModel by viewModels<BookSearchViewModel>()

  private lateinit var mainActivityDelegate: MainActivityDelegate

  override fun onAttach(context: Context) {
    super.onAttach(context)

    try {
      mainActivityDelegate = context as MainActivityDelegate
    } catch (e: ClassCastException) {
      throw ClassCastException()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    // Inflate the layout for this fragment
    _binding = FragmentBookSearchBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.updateSearchCriteria(SearchCriteria.ALL)

    initToolbar(binding.toolbar, R.string.book_search, false)
    mainActivityDelegate.setupNavDrawer(binding.toolbar)
    mainActivityDelegate.enableNavDrawer(true)

    initCriteriaSpinner()
    initAdapter()

    binding.tvSearch.setOnEditorActionListener { textView, actionId, _ ->

      when (actionId) {
        EditorInfo.IME_ACTION_SEARCH -> {
          hideKeyboard(textView)
          viewModel.updateSearchTerm(textView.text.toString())

          true
        }
        EditorInfo.IME_ACTION_DONE -> {
          viewModel.updateSearchTerm(textView.text.toString())
          true
        }
        else -> false
      }
    }
  }

  private fun hideKeyboard(view: View) {
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
  }

  private fun initCriteriaSpinner() {
    val adapter = ArrayAdapter(
        requireContext(),
        R.layout.item_search_criteria,
        SearchCriteria.values().map { searchCriteria ->
          searchCriteria.name
        }
    )
    adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)

    binding.spnCriteria.adapter = adapter

    binding.spnCriteria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        viewModel.updateSearchCriteria(SearchCriteria.valueOf(binding.spnCriteria.adapter.getItem(p2) as String))
      }

      override fun onNothingSelected(p0: AdapterView<*>?) {
      }
    }
  }

  private fun initAdapter() {
    val adapter = WorksAdapter(Glide.with(this))

    binding.rvBooks.adapter = adapter
    adapter.itemClickListener = {
      findNavController().navigate(
          R.id.bookDetailsGraph, //R.id.actionBookDetails,
          WorkDetailsViewModel.createArguments(it)
      )
    }

    viewModel.data.observe(viewLifecycleOwner, {
      adapter.submitList(it)
    })

    viewModel.networkState.observe(viewLifecycleOwner, {
      binding.progressBar.visibility = when (it) {
        NetworkState.LOADING -> View.VISIBLE
        else -> View.GONE
      }
    })
  }
}
