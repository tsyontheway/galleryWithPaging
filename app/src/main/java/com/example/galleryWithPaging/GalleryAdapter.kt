package com.example.galleryWithPaging

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.cell_recycler.view.*
import kotlinx.android.synthetic.main.gallery_foot.view.*

class GalleryAdapter(private val galleryViewModel: GalleryViewModel) :
    PagedListAdapter<PhotoItem, RecyclerView.ViewHolder>(CallBack) {

    private var netWorkStatus: NetWorkStatus? = null
    private var hasFooter = false

    init {
        galleryViewModel.retry()
    }

    fun updateNetWorkStatus(netWorkStatus: NetWorkStatus?) {
        this.netWorkStatus = netWorkStatus
        if (netWorkStatus == NetWorkStatus.INITIAL_LADING) hideFooter() else showFooter()
    }

    private fun hideFooter() {
        if (hasFooter) {
            notifyItemRemoved(itemCount - 1)
        }
        hasFooter = false
    }

    private fun showFooter() {
        if (hasFooter) {
            notifyItemChanged(itemCount - 1)
        } else {
            hasFooter = true
            notifyItemInserted(itemCount - 1)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasFooter && position == itemCount - 1) R.layout.gallery_foot else R.layout.cell_recycler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.cell_recycler -> return PhotoViewHolder.newInstance(parent).also { holder ->
                holder.itemView.setOnClickListener {
                    Bundle().apply {
                        //                       putParcelableArrayList("PHOTO_LIST", ArrayList(currentList!!))
                        putInt("PHOTO_POSITION", holder.adapterPosition)
                        holder.itemView.findNavController()
                            .navigate(R.id.action_galleryFragment_to_pagerPhotoFragment, this)

                    }
                }
            }
            else -> return FooterViewHolder.newInstance(parent).also {
                it.itemView.setOnClickListener {
                    galleryViewModel.retry()
                }
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            R.layout.gallery_foot -> (holder as FooterViewHolder).bindWithNetWorkStatus(
                netWorkStatus
            )
            else -> {
                val photoItem = getItem(position) ?: return
                (holder as PhotoViewHolder).bindWithPhotoItem(photoItem)
            }
        }
    }

    object CallBack : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem == newItem
        }

    }
}


class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun newInstance(parent: ViewGroup): PhotoViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.cell_recycler, parent, false)
            return PhotoViewHolder(view)
        }
    }

    fun bindWithPhotoItem(photoItem: PhotoItem) {
        with(itemView) {
            shimmerLayoutCell.apply {
                setShimmerColor(0x55FFFFFF)
                setShimmerAngle(0)
                startShimmerAnimation()
            }
            textViewUser.text = photoItem.photoUser
            textViewLikes.text = photoItem.photoLikes.toString()
            textViewFavorites.text = photoItem.photoFavorites.toString()
            //解决瀑布流布局的图片高度重绘问题，提前从网上拿回图片高度，设定死
            itemView.imageView.layoutParams.height = photoItem.photoHeight
        }

        Glide.with(itemView)
            .load(photoItem.previewUrl)
            .placeholder(R.drawable.photo_placeholder)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false.also { itemView.shimmerLayoutCell?.stopShimmerAnimation() }
                }

            })
            .into(itemView.imageView)
    }
}


class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun newInstance(parent: ViewGroup): FooterViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.gallery_foot, parent, false)
            (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
            return FooterViewHolder(view)
        }
    }

    fun bindWithNetWorkStatus(netWorkStatus: NetWorkStatus?) {
        with(itemView) {
            when (netWorkStatus) {
                NetWorkStatus.FAILED -> {
                    textView.text = "点击重试"
                    progressBar.visibility = View.GONE
                    isClickable = true
                }
                NetWorkStatus.COMPLETED -> {
                    textView.text = "加载完毕"
                    progressBar.visibility = View.GONE
                    isClickable = false
                }
                else -> {
                    textView.text = "正在加载"
                    progressBar.visibility = View.VISIBLE
                    isClickable = false
                }
            }
        }
    }
}


