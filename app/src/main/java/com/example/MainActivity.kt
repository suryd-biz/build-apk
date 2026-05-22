package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.AssetsScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssetsViewModel
import com.example.ui.viewmodel.WalletsViewModel

class MainActivity : ComponentActivity() {

    private val walletsViewModel: WalletsViewModel by viewModels()
    private val assetsViewModel: AssetsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout(
                    walletsViewModel = walletsViewModel,
                    assetsViewModel = assetsViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppLayout(
    walletsViewModel: WalletsViewModel,
    assetsViewModel: AssetsViewModel
) {
    var activeTab by remember { mutableStateOf(1) } // 1: SYD Wallets, 2: SYD Assets
    var isSettingsOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            Column {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 1: Wallets
                        val isWalletsSelected = activeTab == 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeTab = 1 }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("nav_wallets_tab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = "SYD Dompet Tab",
                                tint = if (isWalletsSelected) Color(0xFF14B8A6) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "SYD Dompet",
                                fontSize = 10.sp,
                                fontWeight = if (isWalletsSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isWalletsSelected) Color(0xFF14B8A6) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        // Settings Center Shortcut button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { isSettingsOpen = true }
                                .testTag("nav_settings_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings Cloud API",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Tab 2: Assets
                        val isAssetsSelected = activeTab == 2
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeTab = 2 }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("nav_assets_tab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home, // Vault surrogate icon
                                contentDescription = "SYD Aset Tab",
                                tint = if (isAssetsSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "SYD Aset",
                                fontSize = 10.sp,
                                fontWeight = if (isAssetsSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAssetsSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = activeTab, label = "tabCrossfade") { tab ->
                when (tab) {
                    1 -> WalletsScreen(viewModel = walletsViewModel)
                    2 -> AssetsScreen(viewModel = assetsViewModel)
                }
            }
        }

        if (isSettingsOpen) {
            SettingsCloudApiModal(
                walletsViewModel = walletsViewModel,
                assetsViewModel = assetsViewModel,
                onDismiss = { isSettingsOpen = false }
            )
        }
    }
}

@Composable
fun SettingsCloudApiModal(
    walletsViewModel: WalletsViewModel,
    assetsViewModel: AssetsViewModel,
    onDismiss: () -> Unit
) {
    var walletsUrlStr by remember { mutableStateOf(walletsViewModel.walletsUrl) }
    var assetsUrlStr by remember { mutableStateOf(assetsViewModel.assetUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Konfigurasi Cloud API",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Masukkan URL Google Apps Script bentukan Anda untuk sinkronisasi cloud.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = walletsUrlStr,
                    onValueChange = { walletsUrlStr = it },
                    label = { Text("Wallets Apps Script URL") },
                    modifier = Modifier.fillMaxWidth().testTag("wallets_api_url_input"),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = assetsUrlStr,
                    onValueChange = { assetsUrlStr = it },
                    label = { Text("Assets Apps Script URL") },
                    modifier = Modifier.fillMaxWidth().testTag("assets_api_url_input"),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            walletsViewModel.walletsUrl = walletsUrlStr
                            assetsViewModel.assetUrl = assetsUrlStr
                            walletsViewModel.refreshWallets()
                            assetsViewModel.refreshProfiles()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_api_settings_button")
                    ) {
                        Text("Simpan & Sync", color = Color.White)
                    }
                }
            }
        }
    }
}
