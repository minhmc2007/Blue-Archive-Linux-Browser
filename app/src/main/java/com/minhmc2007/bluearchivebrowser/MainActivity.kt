package com.minhmc2007.bluearchivebrowser

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.minhmc2007.bluearchivebrowser.databinding.ActivityMainBinding
import com.minhmc2007.bluearchivebrowser.ui.SharedViewModel
import com.minhmc2007.bluearchivebrowser.ui.data.Tab
import com.minhmc2007.bluearchivebrowser.ui.data.TabAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var tabsAdapter: TabAdapter
    private val tabs = mutableListOf<Tab>()
    private var currentTabId: Long = -1
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        applyTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        // Observe page changes from the fragment to update tab state
        sharedViewModel.pageStateChanged.observe(this) { (url, title) ->
            val tab = tabs.find { it.id == currentTabId }
            tab?.let {
                it.url = url
                it.title = title
                tabsAdapter.notifyDataSetChanged() // Update the list in the drawer
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        setupTabs(navView)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> navController.navigate(R.id.nav_home)
                R.id.nav_settings -> showSettingsDialog()
                R.id.nav_about -> showAboutDialog()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun applyTheme() {
        val themeName = sharedPreferences.getString("theme", "SkyBlue")
        val isNightMode = sharedPreferences.getBoolean("night_mode", false)
        val themeResId = when (themeName) {
            "System" -> R.style.Theme_BlueArchiveBrowser_System
            "SakuraPink" -> if (isNightMode) R.style.Theme_BlueArchiveBrowser_SakuraPink_Night else R.style.Theme_BlueArchiveBrowser_SakuraPink
            "MintGreen" -> if (isNightMode) R.style.Theme_BlueArchiveBrowser_MintGreen_Night else R.style.Theme_BlueArchiveBrowser_MintGreen
            "Lavender" -> if (isNightMode) R.style.Theme_BlueArchiveBrowser_Lavender_Night else R.style.Theme_BlueArchiveBrowser_Lavender
            else -> if (isNightMode) R.style.Theme_BlueArchiveBrowser_SkyBlue_Night else R.style.Theme_BlueArchiveBrowser_SkyBlue
        }
        setTheme(themeResId)
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupTabs(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val tabsRecyclerView = headerView.findViewById<RecyclerView>(R.id.tabs_recycler_view)
        val addTabButton = headerView.findViewById<Button>(R.id.add_tab_button)
        val headerImage = headerView.findViewById<android.widget.ImageView>(R.id.header_image)

        // Load header image from assets
        try {
            assets.open("menu.png").use { inputStream ->
                val drawable = android.graphics.drawable.Drawable.createFromStream(inputStream, null)
                headerImage.setImageDrawable(drawable)
            }
        } catch (e: java.io.IOException) {
            e.printStackTrace() // Or handle the error gracefully
        }

        tabsAdapter = TabAdapter(tabs) { tab ->
            currentTabId = tab.id // Set current tab
            sharedViewModel.urlToLoad.value = tab.url
            binding.drawerLayout.closeDrawers()
        }

        tabsRecyclerView.layoutManager = LinearLayoutManager(this)
        tabsRecyclerView.adapter = tabsAdapter

        if (tabs.isEmpty()) {
            addNewTab()
        }

        addTabButton.setOnClickListener {
            addNewTab()
        }
    }

    private fun addNewTab() {
        val newId = System.currentTimeMillis()
        val newTab = Tab(newId, "New Tab", "file:///android_asset/index.html")
        tabs.add(newTab)
        tabsAdapter.notifyItemInserted(tabs.size - 1)
        
        // Switch to the new tab
        currentTabId = newId
        sharedViewModel.urlToLoad.value = newTab.url
    }

    private fun showSettingsDialog() {
        val themes = arrayOf("System", "Sky Blue", "Sakura Pink", "Mint Green", "Lavender")
        val currentTheme = sharedPreferences.getString("theme", "SkyBlue")
        val currentThemeIndex = themes.indexOfFirst { it.replace(" ", "") == currentTheme }

        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setSingleChoiceItems(themes, currentThemeIndex) { dialog, which ->
                val selectedTheme = themes[which].replace(" ", "")
                sharedPreferences.edit().putString("theme", selectedTheme).apply()
                dialog.dismiss()
                recreate()
            }
            .setNeutralButton("Toggle Dark Mode") { _, _ ->
                val newNightMode = !isNightMode
                sharedPreferences.edit().putBoolean("night_mode", newNightMode).apply()
                recreate()
            }
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage("Made by minhmc2007")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showTabsDialog() {
        val tabTitles = tabs.map { it.title }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Open Tabs")
            .setItems(tabTitles) { dialog, which ->
                val selectedTab = tabs[which]
                currentTabId = selectedTab.id // Set current tab
                sharedViewModel.urlToLoad.value = selectedTab.url
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_tabs -> {
                showTabsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}

