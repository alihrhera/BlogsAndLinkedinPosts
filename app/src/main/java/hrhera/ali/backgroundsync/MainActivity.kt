package hrhera.ali.backgroundsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import hrhera.ali.backgroundsync.ui.screen.UploadScreen
import hrhera.ali.backgroundsync.ui.theme.BackGroundSyncTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackGroundSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UploadScreen(innerPadding)
                }
            }
        }

    }
}
