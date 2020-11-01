package com.example.galleryWithPaging

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*


class GalleryFragment : Fragment() {

    private val galleryViewModel by activityViewModels<GalleryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.swipeToRefreshBtn -> {
                swipeRefreshGallery.isRefreshing = true
                Handler().postDelayed({ galleryViewModel.resetQuery() }, 1000)
            }
            R.id.menuRetry -> {
                galleryViewModel.retry()
            }


        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val galleryAdapter = GalleryAdapter(galleryViewModel)

        recycleView.apply {
            adapter = galleryAdapter
            //  layoutManager = GridLayoutManager(requireContext(), 2)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        }

        galleryViewModel.pagedListLiveData.observe(viewLifecycleOwner, Observer {
            galleryAdapter.submitList(it)
        })

        galleryViewModel.netWorkStatus.observe(viewLifecycleOwner, Observer {
            galleryAdapter.updateNetWorkStatus(it)
            swipeRefreshGallery.isRefreshing = it == NetWorkStatus.INITIAL_LADING
        })


        swipeRefreshGallery.setOnRefreshListener {
            galleryViewModel.resetQuery()
        }
    }

}