package com.dicoding.picodiploma.dicodingstoryapp.view.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.databinding.ActivityDetailBinding
import com.dicoding.picodiploma.dicodingstoryapp.utils.DateFormaterHelper
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.DetailViewModel
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.ViewModelFactory
import com.squareup.picasso.Picasso
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportPostponeEnterTransition()

        setupActionBar()
        setupDetail()

        binding.ivDetailPhoto.viewTreeObserver.addOnPreDrawListener {
            supportStartPostponedEnterTransition()
            true
        }
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.story_detail)
    }

    private fun setupDetail() {
        val storyId = intent.getStringExtra(EXTRA_ID)
        if (storyId != null) {
            viewModel.getStoryDetail(storyId)

            viewModel.storyDetail.observe(this) { result ->
                when (result) {
                    is Result.Loading -> showLoading(true)
                    is Result.Success -> {
                        showLoading(false)
                        result.data.story?.let { story ->
                            binding.apply {
                                tvDetailName.text = story.name
                                tvDetailDescription.text = story.description

                                tvDetailDate.text = story.createdAt?.let {
                                    DateFormaterHelper.formatDate(it)
                                } ?: "-"

                                story.photoUrl?.let { url ->
                                    Picasso.get()
                                        .load(url)
                                        .error(R.drawable.ic_broken_image)
                                        .placeholder(R.drawable.ic_place_holder)
                                        .noFade()
                                        .into(ivDetailPhoto)
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            this@DetailActivity,
                            result.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}