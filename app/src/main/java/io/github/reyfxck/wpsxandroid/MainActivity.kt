package io.github.reyfxck.wpsxandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.reyfxck.wpsxandroid.ui.WpsxAppNext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WpsxAppNext()
        }
    }
}
