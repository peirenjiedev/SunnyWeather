package com.sunnyweather.android.ui.place

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.FragmentPlaceBinding

class PlaceFragment :Fragment(){
    val viewModel by lazy{ ViewModelProvider(this).get(PlaceViewModel::class.java) }

    private lateinit var adapter: PlaceAdapter

    private var _binding:FragmentPlaceBinding? = null

    private val binding get() = _binding!!

    inner class MyObserver():LifecycleEventObserver{
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event.targetState == Lifecycle.State.CREATED){
                val layoutManager = LinearLayoutManager(activity)
                binding.recyclerView.layoutManager = layoutManager
                adapter = PlaceAdapter(this@PlaceFragment,viewModel.placeList)
                binding.recyclerView.adapter = adapter
                binding.searchPlaceEdit.addTextChangedListener{ editable->
                    val content = editable.toString()
                    if (content.isNotEmpty()){
                        viewModel.searchPlaces(content)
                    }else{
                        binding.recyclerView.visibility = View.GONE
                        binding.bgImageView.visibility = View.VISIBLE
                        viewModel.placeList.clear()
                        adapter.notifyDataSetChanged()
                    }
                }

                viewModel.placeLiveData.observe(this@PlaceFragment, Observer { result->
                    val places = result.getOrNull()
                    if (places != null){
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.bgImageView.visibility = View.VISIBLE
                        viewModel.placeList.clear()
                        viewModel.placeList.addAll(places)
                        adapter.notifyDataSetChanged()
                    }else{
                        Toast.makeText(activity,"未能查询到任何地点",Toast.LENGTH_SHORT).show()
                        result.exceptionOrNull()?.printStackTrace()
                    }
                })
                requireActivity().lifecycle.removeObserver(this)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding =FragmentPlaceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().lifecycle.addObserver(MyObserver())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}