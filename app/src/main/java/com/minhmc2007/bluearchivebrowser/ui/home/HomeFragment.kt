package com.minhmc2007.bluearchivebrowser.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.minhmc2007.bluearchivebrowser.R
import com.minhmc2007.bluearchivebrowser.ui.SharedViewModel

class HomeFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var urlEditText: TextInputEditText
    private lateinit var goButton: MaterialButton
    private lateinit var sharedViewModel: SharedViewModel

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        webView = view.findViewById(R.id.web_view)
        urlEditText = view.findViewById(R.id.url_edit_text)
        goButton = view.findViewById(R.id.go_button)

        // In WebViewClient's onPageFinished, report the new URL and title.
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                urlEditText.setText(url)
                if (url != null && view?.title != null) {
                    sharedViewModel.pageStateChanged.value = Pair(url, view.title!!)
                }
            }
        }
        webView.settings.javaScriptEnabled = true

        // Load initial page
        sharedViewModel.urlToLoad.observe(viewLifecycleOwner) { url ->
            webView.loadUrl(url)
        }

        if (sharedViewModel.urlToLoad.value == null) {
            sharedViewModel.urlToLoad.value = "file:///android_asset/index.html"
        }

        goButton.setOnClickListener { loadUrlFromEditText() }

        urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrlFromEditText()
                true
            } else {
                false
            }
        }

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        return view
    }

    private fun loadUrlFromEditText() {
        var url = urlEditText.text.toString().trim()
        if (url.isNotEmpty()) {
            if (!url.matches("^(https?|ftp)://.*$".toRegex())) {
                url = "https://$url"
            }
            sharedViewModel.urlToLoad.value = url
        }
    }
}

