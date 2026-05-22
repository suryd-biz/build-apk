package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.TransactionEntity
import com.example.data.database.WalletEntity
import com.example.ui.components.FinanceDonutDistribution
import com.example.ui.components.FinanceLineTrendChart
import com.example.ui.viewmodel.WalletsViewModel
import com.example.utils.ScreenUtils

@Composable
fun WalletsScreen(
    viewModel: WalletsViewModel,
    modifier: Modifier = Modifier
) {
    val wallets by viewModel.wallets.collectAsState()
    val selectedWallet by viewModel.selectedWallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedWallet == null) {
            WalletsLandingScreen(viewModel = viewModel, wallets = wallets)
        } else {
            WalletsDashboardScreen(
                viewModel = viewModel,
                wallet = selectedWallet!!,
                transactions = transactions
            )
        }

        // Modals Overlay
        if (viewModel.isPinModalOpen) {
            WalletPinVerificationModal(viewModel = viewModel)
        }
        if (viewModel.addWalletModalOpen) {
            WalletAddModal(viewModel = viewModel)
        }
        if (viewModel.reorderModalOpen) {
            WalletReorderModal(viewModel = viewModel, wallets = wallets)
        }
        if (viewModel.helpModalOpen) {
            WalletHelpModal(viewModel = viewModel)
        }
    }
}

@Composable
fun WalletsLandingScreen(
    viewModel: WalletsViewModel,
    wallets: List<WalletEntity>
) {
    var expandedWalletPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Icon
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2DD4BF), Color(0xFF0891B2))
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Wallet,
                contentDescription = "Logo Wallets",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SYD Wallets",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = (-1).sp
        )

        Text(
            text = "Pilih dompet untuk melanjutkan",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Controls
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
                    contentDescription = "Urutan Mod",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Urutan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { viewModel.helpModalOpen = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, // Icon surrogate
                    contentDescription = "Bantuan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wallet Dropdown Selector Box
        val selectedWalletLabel = wallets.find { it.id == viewModel.selectedWallet.value?.id }?.nama
            ?: (wallets.firstOrNull()?.nama ?: "Belum ada Dompet")

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { expandedWalletPicker = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Icon Dompet",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedWalletLabel,
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
                expanded = expandedWalletPicker,
                onDismissRequest = { expandedWalletPicker = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                if (wallets.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Memuat data dompet...") },
                        onClick = {}
                    )
                } else {
                    wallets.forEach { w ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(w.nama, fontWeight = FontWeight.Bold)
                                    if (w.hasPin) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Lock, "Locked", modifier = Modifier.size(12.dp))
                                    }
                                }
                            },
                            onClick = {
                                expandedWalletPicker = false
                                viewModel.selectWallet(w)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val chosen = wallets.firstOrNull()
                if (chosen != null) viewModel.selectWallet(chosen)
                else viewModel.addWalletModalOpen = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF14B8A6)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp)
                .testTag("open_selected_wallet_button"),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Buka Dompet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.addWalletModalOpen = true },
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp)
                .testTag("create_wallet_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF14B8A6))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat Dompet Baru", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WalletsDashboardScreen(
    viewModel: WalletsViewModel,
    wallet: WalletEntity,
    transactions: List<TransactionEntity>
) {
    val items = transactions
    val totalIncome = items.filter { it.jenis == "Masuk" }.sumOf { it.nominal }
    val totalExpense = items.filter { it.jenis == "Keluar" }.sumOf { it.nominal }
    val remainingBalance = totalIncome - totalExpense

    val focusManager = LocalFocusManager.current

    // Categorized allocations
    val incomeCategories = items.filter { it.jenis == "Masuk" }.groupBy { it.kategori }
        .mapValues { entry -> entry.value.sumOf { it.nominal } }
    val expenseCategories = items.filter { it.jenis == "Keluar" }.groupBy { it.kategori }
        .mapValues { entry -> entry.value.sumOf { it.nominal } }

    val timelineDates = items.sortedBy { it.tanggal }.groupBy { it.tanggal }.mapValues { entry ->
        val inc = entry.value.filter { it.jenis == "Masuk" }.sumOf { it.nominal }
        val exp = entry.value.filter { it.jenis == "Keluar" }.sumOf { it.nominal }
        inc - exp
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Toolbar Header
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
                IconButton(onClick = { viewModel.refreshWallets() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallet.nama,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Pencatat Keuangan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Sensor and Action states
                IconButton(onClick = { viewModel.isMasked = !viewModel.isMasked }) {
                    Icon(
                        imageVector = if (viewModel.isMasked) Icons.Default.Lock else Icons.Default.Edit,
                        contentDescription = "Sensor Mode"
                    )
                }
                IconButton(onClick = { viewModel.helpModalOpen = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Bantuan Dashboard")
                }
            }
        }

        // Ledger quick transaction card inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Catat Transaksi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Forms Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.formTanggal,
                            onValueChange = { viewModel.formTanggal = it },
                            label = { Text("Tanggal (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                        )

                        var expandedJenis by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = viewModel.formJenis,
                                onValueChange = {},
                                label = { Text("Jenis") },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "DropDown") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedJenis = true },
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            DropdownMenu(
                                expanded = expandedJenis,
                                onDismissRequest = { expandedJenis = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Uang Masuk", fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.formJenis = "Masuk"
                                        viewModel.formKategori = "Gaji Utama"
                                        expandedJenis = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Uang Keluar", fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.formJenis = "Keluar"
                                        viewModel.formKategori = "Kebutuhan Pokok"
                                        expandedJenis = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var expandedKategori by remember { mutableStateOf(false) }
                        val categories = if (viewModel.formJenis == "Masuk") {
                            listOf("Gaji Utama", "Bonus", "Hasil Investasi", "Dikasih", "Penjualan", "Lain-lain")
                        } else {
                            listOf("Kebutuhan Pokok", "Tagihan", "Transportasi", "Kesehatan", "Hiburan", "Sedekah", "Cicilan", "Lain-lain")
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = viewModel.formKategori,
                                onValueChange = {},
                                label = { Text("Kategori") },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "DropDown") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedKategori = true },
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            DropdownMenu(
                                expanded = expandedKategori,
                                onDismissRequest = { expandedKategori = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.formKategori = cat
                                            expandedKategori = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = viewModel.formNominal,
                            onValueChange = { viewModel.formNominal = it },
                            label = { Text("Nominal (Rp)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = viewModel.formKeterangan,
                        onValueChange = { viewModel.formKeterangan = it },
                        label = { Text("Keterangan Tambahan") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveTransaction() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF14B8A6)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (viewModel.formEditId == null) "Simpan" else "Update Data", color = Color.White)
                        }

                        if (viewModel.formEditId != null) {
                            OutlinedButton(
                                onClick = { viewModel.resetForm() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Batal")
                            }
                        }
                    }
                }
            }
        }

        // Summary Statistics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sisa Saldo Dompet",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (viewModel.isMasked) "Rp •••••••" else ScreenUtils.formatToIdr(remainingBalance),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Total Pemasukan", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = if (viewModel.isMasked) "Rp •••••" else ScreenUtils.formatToIdr(totalIncome),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Total Pengeluaran", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = if (viewModel.isMasked) "Rp •••••" else ScreenUtils.formatToIdr(totalExpense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF43F5E)
                            )
                        }
                    }
                }
            }
        }

        // Animated bezier charts row
        item {
            Column {
                Text(
                    "Grafik Pertumbuhan Dompet",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val trendPoints = timelineDates.entries.sortedBy { it.key }.map { it.value }
                val trendLabels = timelineDates.entries.sortedBy { it.key }.map { it.key }

                FinanceLineTrendChart(
                    dataPoints = trendPoints,
                    labels = trendLabels,
                    lineColor = Color(0xFF14B8A6),
                    modifier = Modifier.fillMaxWidth(),
                    isMasked = viewModel.isMasked
                )
            }
        }

        // Two donut distributions side-by-side
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Distribusi Masuk",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FinanceDonutDistribution(
                        categoryMap = incomeCategories,
                        colors = listOf(Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFF59E0B)),
                        isMasked = viewModel.isMasked
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Distribusi Keluar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FinanceDonutDistribution(
                        categoryMap = expenseCategories,
                        colors = listOf(Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFEAB308), Color(0xFFEC4899)),
                        isMasked = viewModel.isMasked
                    )
                }
            }
        }

        // Quick Calculators Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Alat Bantu Hitung", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Balance spliter
                    var divisorVal by remember { mutableStateOf("3") }
                    val currentDivisor = divisorVal.toDoubleOrNull() ?: 3.0
                    val outputPartial = if (viewModel.isMasked) "Rp •••••" else {
                        if (currentDivisor > 0) ScreenUtils.formatToIdr(remainingBalance / currentDivisor) else "Error"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bagi Sisa Saldo menjadi:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = divisorVal,
                            onValueChange = { divisorVal = it },
                            modifier = Modifier.width(72.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                        Text("bagian", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Hasil: $outputPartial / bag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Expression calculator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.calcExpression,
                            onValueChange = { viewModel.calcExpression = it },
                            placeholder = { Text("Contoh: 150000 * 3", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                        Button(
                            onClick = { viewModel.computeExpression() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("=")
                        }
                    }
                    Text(
                        text = "Hasil Kalkulator: ${viewModel.calcResult}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Sorting & Transactions Logs
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Riwayat Transaksi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada riwayat transaksi.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    val pageCount = Math.ceil(items.size.toDouble() / viewModel.itemsPerPage).toInt()
                    val clampedPage = viewModel.currentPage.coerceIn(1, pageCount)
                    viewModel.currentPage = clampedPage

                    val listIndexStart = (clampedPage - 1) * viewModel.itemsPerPage
                    val pageList = items.slice(
                        listIndexStart until minOf(listIndexStart + viewModel.itemsPerPage, items.size)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pageList.forEach { tx ->
                            TransactionRow(tx = tx, isMasked = viewModel.isMasked, viewModel = viewModel)
                        }

                        // Pagination indicators
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
                                    Text("Sebelumnya", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }

                                Button(
                                    onClick = { viewModel.currentPage++ },
                                    enabled = clampedPage < pageCount,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Selanjutnya", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spacer under fab navigations safe zone
        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    isMasked: Boolean,
    viewModel: WalletsViewModel
) {
    val isMasuk = tx.jenis == "Masuk"
    val nominalStr = if (isMasked) "Rp •••••" else ScreenUtils.formatToIdr(tx.nominal)
    val keteranganStr = if (isMasked) "•••••••" else (tx.keterangan.ifBlank { "-" })

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isMasuk) Color(0xFFE6F4EA) else Color(0xFFFCE8E6),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMasuk) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Tx Flow",
                    tint = if (isMasuk) Color(0xFF137333) else Color(0xFFC5221F),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.kategori,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ScreenUtils.formatDateToLocal(tx.tanggal),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = keteranganStr,
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
                    text = "${if (isMasuk) "+" else "-"} $nominalStr",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMasuk) Color(0xFF10B981) else Color(0xFFF43F5E)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Edit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.editTransactionMode(tx) }
                            .padding(4.dp)
                    )
                    Text(
                        "Hapus",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Red,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.deleteTransaction(tx.id) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WalletPinVerificationModal(viewModel: WalletsViewModel) {
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
                        .background(Color(0xFFFCE8E6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, "Lock", tint = Color(0xFFC5221F), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Akses Terkunci", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5221F)),
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
fun WalletAddModal(viewModel: WalletsViewModel) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { viewModel.addWalletModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Buat Dompet Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Dompet") },
                    modifier = Modifier.fillMaxWidth().testTag("wallet_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN Keamanan (Opsional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("wallet_pin_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.addWalletModalOpen = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            viewModel.createWallet(name, pin.ifBlank { null })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_wallet_button")
                    ) {
                        Text("Buat Dompet", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun WalletReorderModal(viewModel: WalletsViewModel, wallets: List<WalletEntity>) {
    val reordered = remember { wallets.toMutableStateList() }

    Dialog(onDismissRequest = { viewModel.reorderModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Atur Urutan Dompet", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)

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
                            viewModel.reorderWallets(reordered.map { it.id })
                            viewModel.reorderModalOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
fun WalletHelpModal(viewModel: WalletsViewModel) {
    Dialog(onDismissRequest = { viewModel.helpModalOpen = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    "Panduan Penggunaan SYD Wallets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            text = "1. Manajemen Dompet\nBuat dompet baru, lakukan pengeditan, atau hapus transaksi secara bebas menggunakan kontrol di landing screen dan dashboard.\n\n" +
                                    "2. Keamanan PIN\nHubungkan PIN unik kustom ke mana-mana untuk melindungi data dompet Anda. PIN diverifikasi secara online ketika pertama kali loading.\n\n" +
                                    "3. Catat Transaksi\nTentukan Nominal, Jenis (Uang Masuk/Uang Keluar) dan deskripsi notes. Hasilnya akan langsung terupdate di dashboard.\n\n" +
                                    "4. Sensor Saldo\nGunakan Ikon Gembok Gantung di Toolbar Atas untuk memaksakan mode sensor, menyembunyikan nominal dan keterangan rahasia.\n\n" +
                                    "5. Kalkulator & Sisa Uang\nGunakan kalkulator ekspresi dan sisa uang division parser di bagian bawah dashboard untuk hitung-hitungan cepat.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.helpModalOpen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", color = Color.White)
                }
            }
        }
    }
}
