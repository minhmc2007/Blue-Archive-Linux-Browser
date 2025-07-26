package com.minhmc2007.bluearchivebrowser.ui.data

// Using a unique ID and mutable properties to allow for state updates.
data class Tab(val id: Long, var title: String, var url: String)