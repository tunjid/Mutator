package com.tunjid.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.tunjid.sample.ui.frame.Root
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appDeps = (applicationContext as App).appDeps
        setContent { Root(appDeps) }
        lifecycleScope.launch {
            delay(1000)
            if(!intent.hasExtra("lol")) startActivity(Intent(this@MainActivity, MainActivity::class.java).putExtra("lol", true))
        }
    }
}