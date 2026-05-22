package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.AssetProfileEntity
import com.example.data.database.AssetSnapshotEntity
import com.example.ui.components.FinanceLineTrendChart
import com.example.ui.viewmodel.AssetsViewModel
import com.example.utils.ScreenUtils

@Composable
fun AssetsScreen(
    viewModel: AssetsViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.assetProfiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val snapshots by viewModel.snapshots.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedProfile == null) {
            AssetsLandingScreen(viewModel = viewModel, profiles = profiles)
        } else {
            AssetsDashboardScreen(
                viewModel = viewModel,
                profile = selectedProfile!!,
                snapshots = snapshots
            )
        }

        // Modals Overlay
        if (viewModel.isPinModalOpen) {
            AssetPinVerificationModal(viewModel = viewModel)
        }
        if (viewModel.addProfileModalOpen) {
            AssetProfileAddModal(viewModel = viewModel)
        }
        if (viewModel.reorderModalOpen) {
            AssetProfileReorderModal(viewModel = viewModel, profiles = profiles)
        }
        if (viewModel.snapshotInputModalOpen) {
            SnapshotInputModal(viewModel = viewModel)
        }
        if (viewModel.detailModalOpen && viewModel.activeDetailSnapshot != null) {
            SnapshotDetailModal(viewModel = viewModel, snap = viewModel.activeDetailSnapshot!!)
        }
    }
}

@Composable
fun AssetsLandingScreen(
    viewModel: AssetsViewModel,
    profiles: List<AssetProfileEntity>
) {
    var expandedPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home, // Vault surrogate icon
                contentDescription = "Logo Assets",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SYD Assets",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6366F1),
            letterSpacing = (-1).sp
        )

        Text(
            text = "Pilih profil aset untuk memantau",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.reorderModalOpen = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Urutan Mod Aset",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Urutan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedProfileLabel = profiles.find { it.id == viewModel.selectedProfile.value?.id }?.nama
            ?: (profiles.firstOrNull()?.nama ?: "Belum ada Profil")

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { expandedPicker = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Icon Profil",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedProfileLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Arrow Dropdown",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expandedPicker,
                onDismissRequest = { expandedPicker = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                if (profiles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Memuat profil...") },
                        onClick = {}
                    )
                } else {
                    profiles.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.nama, fontWeight = FontWeight.Bold)
                                    if (p.hasPin) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Lock, "Locked", modifier = Modifier.size(12.dp))
                                    }
                                }
                            },
                            onClick = {
                                expandedPicker = false
                                viewModel.selectProfile(p)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val chosen = profiles.firstOrNull()
                if (chosen != null) viewModel.selectProfile(chosen)
                else viewModel.addProfileModalOpen = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366F1)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp)
                .testTag("open_selected_profile_button"),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Buka Aset", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.addProfileModalOpen = true },
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp)
                .testTag("create_profile_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF6366F1))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat Profil Baru", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssetsDashboardScreen(
    viewModel: AssetsViewModel,
    profile: AssetProfileEntity,
    snapshots: List<AssetSnapshotEntity>
) {
    val latestSnap = snapshots.firstOrNull()
    val priorSnap = if (snapshots.size > 1) snapshots[1] else latestSnap

    val totalBalanceStr = if (viewModel.isHidden) "Rp •••••••" else ScreenUtils.formatToIdr(latestSnap?.total ?: 0.0)

    val relativeChangeVal = (latestSnap?.total ?: 0.0) - (priorSnap?.total ?: 0.0)
    val relativeChangeLabel = if (viewModel.isHidden) "Rp •••••" else ScreenUtils.formatToIdr(Math.abs(relativeChangeVal))

    val accountItems = listOf(
        AccountMap("BNI", latestSnap?.bni ?: 0.0, priorSnap?.bni ?: 0.0, Color(0xFFEF4444)),
        AccountMap("SeaBank", latestSnap?.seabank ?: 0.0, priorSnap?.seabank ?: 0.0, Color(0xFFF97316)),
        AccountMap("Bibit", latestSnap?.bibit ?: 0.0, priorSnap?.bibit ?: 0.0, Color(0xFF10B981)),
        AccountMap("Tunai", latestSnap?.tunai ?: 0.0, priorSnap?.tunai ?: 0.0, Color(0xFF34D399)),
        AccountMap("Flip", latestSnap?.flip ?: 0.0, priorSnap?.flip ?: 0.0, Color(0xFFEC4899)),
        AccountMap("OVO", latestSnap?.ovo ?: 0.0, priorSnap?.ovo ?: 0.0, Color(0xFF8B5CF6)),
        AccountMap("ShopeePay", latestSnap?.shopeepay ?: 0.0, priorSnap?.shopeepay ?: 0.0, Color(0xFFF59E0B)),
        AccountMap("DANA", latestSnap?.dana ?: 0.0, priorSnap?.dana ?: 0.0, Color(0xFF3B82F6)),
        AccountMap("Jago", latestSnap?.jago ?: 0.0, priorSnap?.jago ?: 0.0, Color(0xFF14B8A6)),
        AccountMap("Lainnya", latestSnap?.lain ?: 0.0, priorSnap?.lain ?: 0.0, Color(0xFF94A3B8))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App topbar block
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.refreshProfiles() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6366F1))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.nama,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Pencatat Rekening & Aset", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = { viewModel.openInsertSnapshotMode() }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Snap", tint = Color(0xFF6366F1))
                }
            }
        }

        // Mega Hero cumulative Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "TOTAL GABUNGAN ASET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        )
                        IconButton(
                            onClick = { viewModel.isHidden = !viewModel.isHidden },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.isHidden) Icons.Default.Lock else Icons.Default.Edit,
                                contentDescription = "Toggle Balance"
                            )
                        }
                    }

                    Text(
                        text = totalBalanceStr,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )

                    // Relative delta indicator flag
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (relativeChangeVal > 0) Color(0xFFD1FAE5) else if (relativeChangeVal < 0) Color(0xFFFEE2E2) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (relativeChangeVal > 0) "Naik $relativeChangeLabel"
                            else if (relativeChangeVal < 0) "Turun $relativeChangeLabel"
                            else "Stabil",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (relativeChangeVal > 0) Color(0xFF065F46) else if (relativeChangeVal < 0) Color(0xFF991B1B) else Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // Core 10 accounts grid mapping custom heights
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                accountItems.forEach { acc ->
                    val accDiff = acc.currentVal - acc.priorVal
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 140.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Mini color circle for account index
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(acc.circleColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = acc.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = if (viewModel.isHidden) "Rp ••••" else ScreenUtils.formatToIdr(acc.currentVal),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )

                            // Net variance indicator flag
                            Text(
                                text = if (accDiff > 0) " +${if (viewModel.isHidden) "•••" else ScreenUtils.formatToIdr(accDiff)}"
                                else if (accDiff < 0) " -${if (viewModel.isHidden) "•••" else ScreenUtils.formatToIdr(Math.abs(accDiff))}"
                                else " Tetap",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (accDiff > 0) Color(0xFF10B981) else if (accDiff < 0) Color(0xFFF43F5E) else Color.Gray,
                                modifier = Modifier
                                    .background(
                                        color = if (accDiff > 0) Color(0xFFE6F4EA) else if (accDiff < 0) Color(0xFFFCE8E6) else Color(0xFFF1F3F4),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Asset Growth curve chart
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Grafik Pertumbuhan Aset",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Timeline Selector filter row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        listOf("hari" to "Hari", "minggu" to "Mg", "bulan" to "Bln", "tahun" to "Thn").forEach { opt ->
                            val activeVal = viewModel.timelineFilter == opt.first
                            Text(
                                text = opt.second,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeVal) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (activeVal) Color(0xFF6366F1) else Color.Transparent)
                                    .clickable { viewModel.timelineFilter = opt.first }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val sortedSnaps = snapshots.sortedBy { it.rawTanggal }
                val chartPoints = sortedSnaps.map { it.total }
                val chartLabels = sortedSnaps.map { ScreenUtils.formatDateToLocal(it.rawTanggal) }

                FinanceLineTrendChart(
                    dataPoints = chartPoints,
                    labels = chartLabels,
                    lineColor = Color(0xFF6366F1),
                    modifier = Modifier.fillMaxWidth(),
                    isMasked = viewModel.isHidden
                )
            }
        }

        // Ledger History snapshots list
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Riwayat Gabungan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (snapshots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada catatan saldo harian.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    val pageCount = Math.ceil(snapshots.size.toDouble() / viewModel.itemsPerPage).toInt()
                    val clampedPage = viewModel.currentPage.coerceIn(1, pageCount)
                    viewModel.currentPage = clampedPage

                    val indexStart = (clampedPage - 1) * viewModel.itemsPerPage
                    val activePageList = snapshots.slice(
                        indexStart until minOf(indexStart + viewModel.itemsPerPage, snapshots.size)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activePageList.forEach { snap ->
                            SnapshotHistoryRow(snap = snap, isHidden = viewModel.isHidden, viewModel = viewModel)
                        }

                        // Pagination controls
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Hal. ${clampedPage} dari $pageCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.currentPage-- },
                                    enabled = clampedPage > 1,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Prev", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }

                                Button(
                                    onClick = { viewModel.currentPage++ },
                                    enabled = clampedPage < pageCount,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Next", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom navigation spacer
        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
fun SnapshotHistoryRow(
    snap: AssetSnapshotEntity,
    isHidden: Boolean,
    viewModel: AssetsViewModel
) {
    val displayTotal = if (isHidden) "Rp •••••" else ScreenUtils.formatToIdr(snap.total)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ScreenUtils.formatDateToLocal(snap.rawTanggal),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = snap.keterangan,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayTotal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6366F1)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            viewModel.activeDetailSnapshot = snap
                            viewModel.detailModalOpen = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.List, "Details", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = { viewModel.openEditSnapshotMode(snap) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = { viewModel.deleteSnapshot(snap.rawTanggal) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

data class AccountMap(
    val name: String,
    val currentVal: Double,
    val priorVal: Double,
    val circleColor: Color
)

@Composable
fun AssetPinVerificationModal(viewModel: AssetsViewModel) {
    Dialog(onDismissRequest = { viewModel.isPinModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFAE8FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, "Lock", tint = Color(0xFFA855F7), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Aset Terkunci", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Masukkan PIN untuk melanjutkan",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = viewModel.pinInputText,
                    onValueChange = { viewModel.pinInputText = it },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("****") },
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.isPinModalOpen = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = { viewModel.verifyPinAndUnlock(viewModel.pinInputText) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("Buka", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AssetProfileAddModal(viewModel: AssetsViewModel) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { viewModel.addProfileModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Buat Profil Aset Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Profil/Aset") },
                    modifier = Modifier.fillMaxWidth().testTag("asset_profile_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN Keamanan (Opsional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("asset_profile_pin_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.addProfileModalOpen = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            viewModel.createProfile(name, pin.ifBlank { null })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_profile_button")
                    ) {
                        Text("Buat Profil", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AssetProfileReorderModal(viewModel: AssetsViewModel, profiles: List<AssetProfileEntity>) {
    val reordered = remember { profiles.toMutableStateList() }

    Dialog(onDismissRequest = { viewModel.reorderModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Atur Urutan Profil", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reordered.size) { index ->
                        val item = reordered[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.nama, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    enabled = index > 0,
                                    onClick = {
                                        val temp = reordered[index]
                                        reordered[index] = reordered[index - 1]
                                        reordered[index - 1] = temp
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
                                }

                                IconButton(
                                    enabled = index < reordered.size - 1,
                                    onClick = {
                                        val temp = reordered[index]
                                        reordered[index] = reordered[index + 1]
                                        reordered[index + 1] = temp
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.reorderModalOpen = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            viewModel.reorderProfiles(reordered.map { it.id })
                            viewModel.reorderModalOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Simpan Urutan", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SnapshotInputModal(viewModel: AssetsViewModel) {
    val focusManager = LocalFocusManager.current
    Dialog(onDismissRequest = { viewModel.snapshotInputModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .heightIn(max = 500.dp)
            ) {
                Text(
                    text = if (viewModel.formAction == "insert") "Catat Saldo Baru" else "Edit Catatan Saldo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = viewModel.formTanggal,
                            onValueChange = { viewModel.formTanggal = it },
                            label = { Text("Tanggal Update (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formBni,
                            onValueChange = { viewModel.formBni = it },
                            label = { Text("BNI") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formSeabank,
                            onValueChange = { viewModel.formSeabank = it },
                            label = { Text("SEABANK") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formBibit,
                            onValueChange = { viewModel.formBibit = it },
                            label = { Text("BIBIT") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formTunai,
                            onValueChange = { viewModel.formTunai = it },
                            label = { Text("TUNAI") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formFlip,
                            onValueChange = { viewModel.formFlip = it },
                            label = { Text("FLIP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formOvo,
                            onValueChange = { viewModel.formOvo = it },
                            label = { Text("OVO") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formShopeepay,
                            onValueChange = { viewModel.formShopeepay = it },
                            label = { Text("SHOPEEPAY") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formDana,
                            onValueChange = { viewModel.formDana = it },
                            label = { Text("DANA") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formJago,
                            onValueChange = { viewModel.formJago = it },
                            label = { Text("JAGO") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formLain,
                            onValueChange = { viewModel.formLain = it },
                            label = { Text("LAINNYA") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = viewModel.formKeterangan,
                            onValueChange = { viewModel.formKeterangan = it },
                            label = { Text("Keterangan Catatan") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.snapshotInputModalOpen = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = { viewModel.saveSnapshot() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Simpan Data", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SnapshotDetailModal(viewModel: AssetsViewModel, snap: AssetSnapshotEntity) {
    val itemsMap = listOf(
        "BNI" to snap.bni,
        "SEABANK" to snap.seabank,
        "BIBIT" to snap.bibit,
        "TUNAI" to snap.tunai,
        "FLIP" to snap.flip,
        "OVO" to snap.ovo,
        "SHOPEEPAY" to snap.shopeepay,
        "DANA" to snap.dana,
        "JAGO" to snap.jago,
        "LAINNYA" to snap.lain
    )

    Dialog(onDismissRequest = { viewModel.detailModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 450.dp)
            ) {
                Text(
                    text = "Detail: ${ScreenUtils.formatDateToLocal(snap.rawTanggal)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6366F1)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itemsMap) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(pair.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                if (viewModel.isHidden) "Rp •••••" else ScreenUtils.formatToIdr(pair.second),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(8.dp))

                Text("TOTAL GABUNGAN", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(
                    text = if (viewModel.isHidden) "Rp •••••••" else ScreenUtils.formatToIdr(snap.total),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6366F1)
                )
                Text(
                    text = "Descr: ${snap.keterangan}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Button(
                    onClick = { viewModel.detailModalOpen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", color = Color.White)
                }
            }
        }
    }
}
