package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.cloud.MarketRepository
import com.example.data.db.OutpostDatabase
import com.example.data.db.OutpostRepository
import com.example.ui.GameViewModel
import com.example.ui.OutpostScreen
import com.example.ui.theme.OutpostZeroTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OutpostZeroTheme {
                OutpostZeroRoot()
            }
        }
    }
}

@Composable
private fun OutpostZeroRoot() {
    val app = LocalContext.current.applicationContext as Application
    val gameViewModel: GameViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                val dao = OutpostDatabase.getInstance(app).outpostDao()
                val repo = OutpostRepository(dao)
                val marketRepo = MarketRepository(db)
                val prefs = app.getSharedPreferences("outpost_zero_settings", android.content.Context.MODE_PRIVATE)
                val authUid = runCatching {
                    com.google.firebase.Firebase.let { _ ->
                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    }
                }.getOrNull() ?: ""
                GameViewModel(
                    repo = repo,
                    marketRepo = marketRepo,
                    currentUserId = authUid,
                    prefs = prefs
                )
            }
        }
    )
    OutpostScreen(viewModel = gameViewModel)
}
