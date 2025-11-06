package com.example.trendsy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class NotificationFragment : Fragment() {

    private lateinit var viewModel: PostViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        statusText = view.findViewById(R.id.statusText)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]

        // Observe upload status
        viewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            statusText.text = status.message
            progressBar.progress = status.progress

            if (status.isSuccess) {
                Toast.makeText(requireContext(), "✅ Upload Successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "❌ Upload Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
