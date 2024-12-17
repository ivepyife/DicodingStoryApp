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
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result
import com.dicoding.picodiploma.dicodingstoryapp.databinding.ActivityMainBinding
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

        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        setupAction()
    }

    private val startAddStory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            resetUIState()
            viewModel.getStory()
        }
    }

    private fun setupRecyclerView() {
        adapter = StoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            resetUIState()
            viewModel.getStory()
        }
    }

    private fun resetUIState() {
        adapter.submitList(emptyList())

        showLoading(true)
        showError(false)
        showEmptyState(false)
    }

    private fun setupViewModel() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else{
                setContentView(binding.root)
                viewModel.updateWidget()
            }
        }

        viewModel.story.observe(this) { result ->
            binding.swipeRefresh.isRefreshing = false

            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                    showError(false)
                }
                is Result.Success -> {
                    showLoading(false)
                    showError(false)
                    val stories = result.data.listStory
                    if (stories.isNullOrEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        adapter.submitList(stories)
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showError(true, result.error)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(isError: Boolean, message: String = "") {
        binding.errorLayout.visibility = if (isError) View.VISIBLE else View.GONE
        binding.tvError.text = message
        binding.btnRetry.setOnClickListener {
            viewModel.getStory()
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvError.text = getString(R.string.no_story_available)
            binding.errorLayout.visibility = View.VISIBLE
        } else {
            binding.errorLayout.visibility = View.GONE
        }
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