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

package com.raywenderlich.android.bookmanstreasure

import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.raywenderlich.android.bookmanstreasure.databinding.ActivityMainBinding
import com.raywenderlich.android.bookmanstreasure.destinations.AuthorDetailsNavigator
import com.raywenderlich.android.bookmanstreasure.ui.MainActivityDelegate

class MainActivity : AppCompatActivity(), MainActivityDelegate {

  private var _binding: ActivityMainBinding? = null
  // This property is only valid between `onCreate` and `onDestroy`
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)

    _binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val destination = AuthorDetailsNavigator(findChildFragmentManager())
    findNavController().navigatorProvider.addNavigator(destination)

    val inflater = findNavController().navInflater
    val graph = inflater.inflate(R.navigation.nav_graph)
    findNavController().graph = graph
  }

  override fun onDestroy() {
    _binding = null
    super.onDestroy()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    findNavController().handleDeepLink(intent)
  }

  override fun onSupportNavigateUp() = findNavController().navigateUp()

  override fun onBackPressed() {
    if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
      binding.drawerLayout.closeDrawer(GravityCompat.START)
    } else {
      super.onBackPressed()
    }
  }

  override fun setupNavDrawer(toolbar: Toolbar) {
    val toggle = ActionBarDrawerToggle(
      this, binding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    binding.drawerLayout.addDrawerListener(toggle)
    toggle.syncState()

    binding.navView.setupWithNavController(findNavController())
  }

  override fun enableNavDrawer(enable: Boolean) {
    binding.drawerLayout.isEnabled = enable
  }

  private fun findChildFragmentManager(): FragmentManager {
    val navHostFragment = supportFragmentManager
      .findFragmentById(R.id.navHostFragment) as NavHostFragment
    return navHostFragment.childFragmentManager
  }

  private fun findNavController(): NavController {
    // https://developer.android.com/guide/navigation/navigation-getting-started#navigate
    // https://issuetracker.google.com/issues/142847973
    val navHostFragment = supportFragmentManager
      .findFragmentById(R.id.navHostFragment) as NavHostFragment
    return navHostFragment.navController
  }
}
