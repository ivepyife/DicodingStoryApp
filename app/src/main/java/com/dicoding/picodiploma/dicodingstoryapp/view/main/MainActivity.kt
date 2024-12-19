package com.dicoding.picodiploma.dicodingstoryapp.view.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.databinding.ActivityMainBinding
import com.dicoding.picodiploma.dicodingstoryapp.view.adapter.LoadingStateAdapter
import com.dicoding.picodiploma.dicodingstoryapp.view.adapter.StoryAdapter
import com.dicoding.picodiploma.dicodingstoryapp.view.addstory.AddStoryActivity
import com.dicoding.picodiploma.dicodingstoryapp.view.maps.MapsActivity
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.MainViewModel
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.ViewModelFactory
import com.dicoding.picodiploma.dicodingstoryapp.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupViewModel()
        setupSwipeRefresh()
        setupAction()
    }

    private val startAddStory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            adapter.refresh()
        }
    }

    private fun setupRecyclerView() {
        adapter = StoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
    }

    private fun setupViewModel() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                viewModel.story.observe(this) { pagingData ->
                    adapter.submitData(lifecycle, pagingData)
                }

                // Observe loading states
                adapter.addLoadStateListener { loadState ->
                    binding.apply {
                        progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                        swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading

                        // Error state
                        val errorState = loadState.source.refresh as? LoadState.Error
                            ?: loadState.mediator?.refresh as? LoadState.Error
                            ?: loadState.append as? LoadState.Error
                            ?: loadState.prepend as? LoadState.Error

                        errorState?.let { state ->
                            showError(true, state.error.localizedMessage
                                ?: getString(R.string.unknown_error))
                        }

                        // Empty state
                        if (loadState.source.refresh is LoadState.NotLoading &&
                            loadState.append.endOfPaginationReached &&
                            adapter.itemCount < 1) {
                            showEmptyState(true)
                        } else {
                            showEmptyState(false)
                        }
                    }
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun showError(isError: Boolean, message: String = "") {
        binding.errorLayout.visibility = if (isError) View.VISIBLE else View.GONE
        binding.tvError.text = message
        binding.btnRetry.setOnClickListener {
            adapter.retry()
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.errorLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.tvError.text = getString(R.string.no_story_available)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            R.id.btn_maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.logout))
            setMessage(getString(R.string.logout_confirmation))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.logout()
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun setupAction() {
        binding.fabAdd.setOnClickListener {
            startAddStory.launch(Intent(this, AddStoryActivity::class.java))
        }
    }
}