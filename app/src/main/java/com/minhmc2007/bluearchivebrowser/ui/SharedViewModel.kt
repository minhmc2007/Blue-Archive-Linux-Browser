package com.minhmc2007.bluearchivebrowser.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // Used to tell the fragment which URL to load.
    val urlToLoad = MutableLiveData<String>()

    // Used to notify the activity that the page has changed (URL, Title).
    val pageStateChanged = MutableLiveData<Pair<String, String>>()
}