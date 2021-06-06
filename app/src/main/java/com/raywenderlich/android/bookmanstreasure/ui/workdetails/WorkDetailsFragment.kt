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

package com.raywenderlich.android.bookmanstreasure.ui.workdetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.raywenderlich.android.bookmanstreasure.R
import com.raywenderlich.android.bookmanstreasure.data.Author
import com.raywenderlich.android.bookmanstreasure.data.Work
import com.raywenderlich.android.bookmanstreasure.databinding.FragmentWorkDetailsBinding
import com.raywenderlich.android.bookmanstreasure.source.NetworkState
import com.raywenderlich.android.bookmanstreasure.ui.authordetails.AuthorDetailsViewModel
import com.raywenderlich.android.bookmanstreasure.ui.bookdetails.BookDetailsViewModel
import com.raywenderlich.android.bookmanstreasure.util.CoverSize
import com.raywenderlich.android.bookmanstreasure.util.initToolbar
import com.raywenderlich.android.bookmanstreasure.util.loadCover

class WorkDetailsFragment : Fragment() {

  private var _binding: FragmentWorkDetailsBinding? = null
  private val binding get() = _binding!!

  private val viewModel by viewModels<WorkDetailsViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    // Inflate the layout for this fragment
    _binding = FragmentWorkDetailsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    initToolbar(binding.toolbar, 0, true)
    initDetails()
    initEditionsAdapter()

    binding.toolbar.postDelayed({ viewModel.loadArguments(arguments) }, 100)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.work_details, menu)

    // Make menu items invisible until details are loaded.
    menu.findItem(R.id.menuAddFavorite)?.isVisible = false
    menu.findItem(R.id.menuRemoveFavorite)?.isVisible = false
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menuAddFavorite -> viewModel.addAsFavorite()
      R.id.menuRemoveFavorite -> viewModel.removeFromFavorites()
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun initDetails() {
    viewModel.work.observe(viewLifecycleOwner, { work ->

      if (work?.coverId != null) {
        Glide.with(this)
            .loadCover(work.coverId, CoverSize.M)
            .error(Glide.with(this).load(R.drawable.book_cover_missing))
            .into(binding.ivCover)
      } else {
        Glide.with(this)
            .load(R.drawable.book_cover_missing)
            .into(binding.ivCover)
      }

      binding.toolbar.title = work?.title
      binding.toolbar.subtitle = work?.subtitle

      viewModel.favorite.observe(viewLifecycleOwner, { favorite ->
        if (favorite != null) {
          binding.toolbar.menu.findItem(R.id.menuAddFavorite)?.isVisible = false
          binding.toolbar.menu.findItem(R.id.menuRemoveFavorite)?.isVisible = true
        } else {
          binding.toolbar.menu.findItem(R.id.menuAddFavorite)?.isVisible = true
          binding.toolbar.menu.findItem(R.id.menuRemoveFavorite)?.isVisible = false
        }
      })

      val adapter = AuthorsAdapter(getAuthors(work))
      adapter.itemCLickListener = {
        findNavController().navigate(
            R.id.actionShowAuthor,
            AuthorDetailsViewModel.createArguments(it)
        )
      }

      binding.rvAuthors.adapter = adapter

      val numberOfEditions = work?.editionIsbns?.size ?: 0

      binding.tvEditions.text = resources.getQuantityString(R.plurals.editions_available,
          numberOfEditions, numberOfEditions)
    })
  }

  private fun initEditionsAdapter() {
    val adapter = BooksAdapter(Glide.with(this))

    binding.rvEditions.adapter = adapter
    adapter.itemClickListener = {
      findNavController().navigate(
          R.id.actionShowEdition,
          BookDetailsViewModel.createArguments(it)
    )}

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

  private fun getAuthors(it: Work?): List<Author> {
    val authors = ArrayList<Author>()

    if (it?.authorName?.size != null) {
      for (i in it.authorName.indices) {
        authors.add(
            Author(
                it.authorName[i],
                it.authorKey[i]
            )
        )
      }
    }

    return authors
  }
}
