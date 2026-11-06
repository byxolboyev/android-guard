package uz.androidguard.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GuardApp()
        }
    }
}

@Composable
fun GuardApp() {
    val context = androidx.compose.ui.platform.LocalContext.current

    var status by remember {
        mutableStateOf("Qurilma xavfsizligi tekshirilmagan")
    }

    var selectedFile by remember {
        mutableStateOf("Hech qanday fayl tanlanmagan")
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedFile = uri.lastPathSegment ?: "Tanlangan fayl"

            try {
                val digest = MessageDigest.getInstance("SHA-256")
                val input = context.contentResolver.openInputStream(uri)

                val hash = input?.use { stream ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = stream.read(buffer)
                        if (count == -1) break
                        digest.update(buffer, 0, count)
                    }
                    digest.digest().joinToString("") {
                        "%02x".format(it)
                    }
                }

                status = if (hash != null) {
                    "SHA-256 hisoblandi: $hash"
                } else {
                    "Faylni o‘qib bo‘lmadi"
                }

            } catch (e: Exception) {
                status = "Xatolik: ${e.message}"
            }
        }
    }

    val packageManager = context.packageManager

    val apps = remember {
        packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(
                Intent.CATEGORY_LAUNCHER
            ),
            0
        ).map {
            it.loadLabel(packageManager).toString()
        }.distinct().sorted()
    }

    MaterialTheme {
        Scaffold { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text(
                        text = "ANDROID GUARD",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text("Android xavfsizlik markazi")
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Xavfsizlik holati")
                            Text(status)
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            picker.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fayl yoki APK tanlash")
                    }
                }

                item {
                    Text("Tanlangan fayl: $selectedFile")
                }

                item {
                    HorizontalDivider()

                    Text(
                        text = "O‘rnatilgan ilovalar",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text("Topilgan ilovalar: ${apps.size}")
                }

                items(apps) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = app,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}