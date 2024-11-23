package com.dicoding.picodiploma.dicodingstoryapp.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.squareup.picasso.Picasso
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.StoryItem
import com.dicoding.picodiploma.dicodingstoryapp.databinding.ItemStoryBinding
import com.dicoding.picodiploma.dicodingstoryapp.utils.DateFormaterHelper
import com.dicoding.picodiploma.dicodingstoryapp.view.detail.DetailActivity

class StoryAdapter : ListAdapter<StoryItem, StoryAdapter.MyViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    class MyViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryItem) {
            binding.apply {
                tvName.text = story.name
                tvDate.text = story.createdAt?.let { DateFormaterHelper.formatDate(it) } ?: "-"
                tvDescription.text = story.description

                story.photoUrl?.let { url ->
                    Picasso.get()
                        .load(url)
                        .fit()
                        .centerCrop()
                        .error(R.drawable.ic_broken_image)
                        .placeholder(R.drawable.ic_place_holder)
                        .into(imgStory)
                }

                root.setOnClickListener {
                    val context = itemView.context
                    val intent = Intent(context, DetailActivity::class.java).apply {
                        putExtra(DetailActivity.EXTRA_ID, story.id)
                    }

                    val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        context as Activity,
                        androidx.core.util.Pair(imgStory, "image_story"),
                        androidx.core.util.Pair(tvName, "detail_name"),
                        androidx.core.util.Pair(tvDate, "date_story"),
                        androidx.core.util.Pair(tvDescription, "description_story")
                    )

                    context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItem>() {
            override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}