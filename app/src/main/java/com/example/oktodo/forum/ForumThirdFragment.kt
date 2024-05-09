package com.example.oktodo.forum

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.databinding.ForumFragmentSecondBinding
import com.example.oktodo.util.adapter.ForumAdapter
import kotlinx.coroutines.launch

class ForumThirdFragment : Fragment() {
    private lateinit var adapter: ForumAdapter
    private val viewModel by activityViewModels<ForumMainViewModel>()
    private var _binding: ForumFragmentSecondBinding? = null

    private val binding get() = _binding!!

    private fun initRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            this.adapter = adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ForumFragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // getArguments()를 사용하여 번들을 가져옴
        var searchText: String? = null
        val bundle = arguments
        if (bundle != null && bundle.containsKey("searchText")) {
            searchText = bundle.getString("searchText")
        }

        viewModel.queryForFragment3(searchText)

        // recyclerView 초기화 및 설정
        val recyclerView = binding.forumRecyclerView
        adapter = ForumAdapter(
            recyclerView,
            onClick = { forum ->
                viewModel.selectedForum = forum
                val intent = Intent(requireContext(), ForumReadActivity::class.java)
                intent.putExtra("forumCno", forum.cno.toString()) // 선택된 포럼 정보를 인텐트에 추가
                intent.putExtra("forumContent", forum.forumContent) // 선택된 포럼 정보를 인텐트에 추가
                intent.putExtra("forumCategory", forum.forumCategory)
                intent.putExtra("forumPlace1", forum.forumPlace1)
                intent.putExtra("forumPlace2", forum.forumPlace2)
                intent.putExtra("postMno", forum.mno)
                startActivity(intent)
            }
        )
        initRecyclerView(recyclerView, adapter)

        binding.forumRecyclerView.adapter = adapter
        // ViewModel에서 데이터를 수집하여 RecyclerView에 반영
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { newList ->
                    adapter.submitList(newList)
                    adapter.loadData(newList)
                    // 데이터가 로드된 후에 최상단으로 스크롤
                    recyclerView.post {
                        recyclerView.smoothScrollToPosition(0) // 스크롤이 부드럽게 이루어져 사용자가 직접 스크롤 하는 경우에 자주 사용
//                        recyclerView.scrollToPosition(0) // 프로그래밍적으로 스크롤 조작시 사용.. 근데 지금은 scrollToPosition이 작동 안함
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}