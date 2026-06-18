package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.ChapterEntity
import com.example.data.database.CharacterEntity
import com.example.data.database.NovelProject
import com.example.ui.theme.*
import com.example.ui.viewmodel.NovelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        NovelWriterApp()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovelWriterApp() {
    val context = LocalContext.current
    val viewModel: NovelViewModel = viewModel()
    
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val activeCharacters by viewModel.activeCharacters.collectAsStateWithLifecycle()
    val activeChapters by viewModel.activeChapters.collectAsStateWithLifecycle()
    
    var currentStage by remember { mutableStateOf(0) }
    var showProjectDialog by remember { mutableStateOf(false) }

    val listGenres = listOf(
        "Romansa", "Fantasi", "Fiksi Ilmiah", "Triler", "Horor", "Misteri",
        "Slice of Life", "Sejarah", "Petualangan", "Drama", "Fanfiction", "Psikologi", "Aksi", "Komedi"
    )

    if (activeProject == null) {
        // App intro and projects selection
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✦ AI NOVEL WRITER ✦",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = WarmGoldPrimary,
                    letterSpacing = 4.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Asisten Penulisan Novel Komprehensif\nBahasa Indonesia Berbasis Gemini AI",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = SepiaSecondary,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ParchmentCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selamat Datang, Pujangga",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = InkTextOnBackground,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    Text(
                        text = "Mulai rancang novel dengan 3 genre kombinasi sekaligus, sinkronisasi riset lore fandom fanfiction atau analisis ilmiah psikologi, aturan POV ketat, dan pemeriksaan ejaan EYD V.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = InkTextOnSurface,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.createNewProject { newId ->
                                Toast.makeText(context, "Proyek novel baru berhasil dibuat", Toast.LENGTH_SHORT).show()
                                currentStage = 0
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ParchmentDarkBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BUAT PROYEK NOVEL BARU",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (projects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Daftar Novel Tersimpan",
                            color = SepiaSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            projects.forEach { proj ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectProject(proj.id) },
                                    colors = CardDefaults.cardColors(containerColor = ParchmentDarkBackground),
                                    border = BorderStroke(1.dp, ParchmentBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = WarmGoldPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            Text(proj.judul, fontWeight = FontWeight.Bold, color = InkTextOnBackground, fontSize = 14.sp)
                                            Text(proj.genres, fontSize = 11.sp, color = SepiaSecondary)
                                        }
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = SepiaSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val proj = activeProject!!
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ParchmentCardSurface)
                    .border(BorderStroke(0.5.dp, ParchmentBorder))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectProject(null) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = SepiaSecondary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = proj.judul.ifBlank { "Novel Tanpa Judul" },
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            color = WarmGoldPrimary
                        )
                        Text(
                            text = "Genre: ${proj.genres.ifBlank { "Belum diset" }} · ${proj.pov} · ${proj.targetWordCount} Kata",
                            fontSize = 11.sp,
                            color = SepiaSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "● Tersimpan",
                        color = SageGreenTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    Button(
                        onClick = { showProjectDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonBackground, contentColor = CrimsonText),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Reset novel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Roadmap Stages
            val stagesList = listOf(
                Pair("Konsep", Icons.Default.Build),
                Pair("Gaya", Icons.Default.Create),
                Pair("Riset", Icons.Default.Search),
                Pair("Tokoh", Icons.Default.Face),
                Pair("Alur Bab", Icons.Default.List),
                Pair("Studio", Icons.Default.Edit),
                Pair("Kompilasi", Icons.Default.Star)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ParchmentDarkBackground)
                    .border(BorderStroke(0.5.dp, ParchmentBorder)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(stagesList) { idx, item ->
                    val active = currentStage == idx
                    Surface(
                        modifier = Modifier.clickable { currentStage = idx },
                        color = if (active) WarmGoldPrimary else ParchmentCardSurface,
                        shape = RoundedCornerShape(20.dp),
                        border = if (!active) BorderStroke(1.dp, ParchmentBorder) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.second,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (active) ParchmentDarkBackground else WarmGoldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${idx + 1}. ${item.first}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) ParchmentDarkBackground else InkTextOnBackground
                            )
                        }
                    }
                }
            }

            // Stages Workspaces
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (currentStage) {
                    0 -> StageKonsepPanel(proj, listGenres, viewModel)
                    1 -> StageStylesPanel(proj, viewModel)
                    2 -> StageResearchPanel(proj, viewModel)
                    3 -> StageCharactersPanel(activeCharacters, viewModel)
                    4 -> StageOutlinePanel(proj, activeCharacters, activeChapters, viewModel)
                    5 -> StageWritingStudioPanel(proj, activeCharacters, activeChapters, viewModel)
                    6 -> StageCompileExportPanel(proj, activeChapters, viewModel)
                }
            }
        }
    }

    if (showProjectDialog) {
        AlertDialog(
            onDismissRequest = { showProjectDialog = false },
            containerColor = ParchmentCardSurface,
            title = { Text("Hapus Proyek Ini?", fontFamily = FontFamily.Serif, color = CrimsonText) },
            text = { Text("Seluruh draf bab utama, riset fandom, analisis gaya penulisan, dan profil tokoh fiksi novel ini akan dibersihkan permanen dari database.", color = InkTextOnSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCurrentProject {
                            Toast.makeText(context, "Proyek novel dihapus", Toast.LENGTH_SHORT).show()
                        }
                        showProjectDialog = false
                    }
                ) {
                    Text("HAPUS SEKARANG", color = CrimsonText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProjectDialog = false }) {
                    Text("BATAL", color = SepiaSecondary)
                }
            }
        )
    }
}

// Stage 1: Konsep & Detail
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StageKonsepPanel(project: NovelProject, allGenres: List<String>, viewModel: NovelViewModel) {
    val context = LocalContext.current
    var judul by remember { mutableStateOf(project.judul) }
    var pov by remember { mutableStateOf(project.pov) }
    var targetWords by remember { mutableStateOf(project.targetWordCount) }
    var sinopsis by remember { mutableStateOf(project.sinopsis) }
    var tema by remember { mutableStateOf(project.tema) }
    var setting by remember { mutableStateOf(project.setting) }
    var fandomName by remember { mutableStateOf(project.fandomName) }

    val selectedGenres = remember(project.genres) {
        if (project.genres.isBlank()) mutableStateListOf<String>()
        else mutableStateListOf(*project.genres.split(",").map { it.trim() }.toTypedArray())
    }

    val povOptions = listOf(
        "Orang Pertama ('Aku', 'Saya')",
        "Orang Kedua ('Kamu', 'Kau')",
        "Orang Ketiga Terbatas ('Dia' - terbatas)",
        "Orang Ketiga Serbatahu ('Dia' - serbatahu)"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tahap 1: Fondasi Novel & Persilangan Genre",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WarmGoldPrimary
            )
            Text(
                text = "Atur identitas, plot makro, dan pilih hingga maksimal 3 genre sosiologis sekaligus untuk dikolaborasikan.",
                fontSize = 12.sp,
                color = SepiaSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = judul,
                onValueChange = { judul = it; viewModel.updateProjectDetails(it, project.genres, pov, targetWords, sinopsis, tema, setting, fandomName) },
                label = { Text("Judul Novel/Buku") },
                textStyle = TextStyle(color = InkTextOnBackground, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ParchmentCardSurface,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pilih Genre Utama (Maksimal 3 Genre Sekaligus!)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = WarmGoldPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allGenres.forEach { genre ->
                            val isChecked = selectedGenres.contains(genre)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    if (isChecked) {
                                        selectedGenres.remove(genre)
                                    } else {
                                        if (selectedGenres.size >= 3) {
                                            Toast.makeText(context, "Maksimal pilih 3 genre utama", Toast.LENGTH_SHORT).show()
                                        } else {
                                            selectedGenres.add(genre)
                                        }
                                    }
                                    val genresFlat = selectedGenres.joinToString(", ")
                                    viewModel.updateProjectDetails(judul, genresFlat, pov, targetWords, sinopsis, tema, setting, fandomName)
                                },
                                label = { Text(genre, fontSize = 11.sp, color = if (isChecked) ParchmentDarkBackground else InkTextOnBackground) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarmGoldPrimary,
                                    selectedLabelColor = ParchmentDarkBackground,
                                    containerColor = ParchmentDarkBackground,
                                    labelColor = InkTextOnBackground
                                ),
                                border = if (!isChecked) BorderStroke(1.dp, ParchmentBorder) else null
                            )
                        }
                    }
                    
                    if (selectedGenres.isNotEmpty()) {
                        Text(
                            text = "Disilangkan (${selectedGenres.size}/3): " + selectedGenres.joinToString(", "),
                            fontSize = 11.sp,
                            color = SepiaSecondary,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }

        if (selectedGenres.contains("Fanfiction")) {
            item {
                OutlinedTextField(
                    value = fandomName,
                    onValueChange = { fandomName = it; viewModel.updateProjectDetails(judul, project.genres, pov, targetWords, sinopsis, tema, setting, it) },
                    label = { Text("Nama Fandom Asal (Contoh: Naruto, Harry Potter, Genshin Impact)") },
                    textStyle = TextStyle(color = InkTextOnBackground),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
            }
        }

        item {
            Column {
                Text(
                    text = "Sudut Pandang Utama (Point of View - POV)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = WarmGoldPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                povOptions.forEach { option ->
                    val isSelected = pov == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                pov = option
                                viewModel.updateProjectDetails(judul, project.genres, option, targetWords, sinopsis, tema, setting, fandomName)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                pov = option
                                viewModel.updateProjectDetails(judul, project.genres, option, targetWords, sinopsis, tema, setting, fandomName)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = WarmGoldPrimary, unselectedColor = SepiaSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(option, color = InkTextOnBackground, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Target Word-Count per Bab Utama",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = WarmGoldPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Persyaratan rujukan fiksi Indonesia (2000-3000 kata). Setiap draf bab dibagi menjadi tiga sub-bab terpisah berkisar 700-1000 kata untuk penulisan AI terlengkap.",
                    fontSize = 11.sp,
                    color = SepiaSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1500, 2000, 3000).forEach { size ->
                        val isSelected = targetWords == size
                        Surface(
                            modifier = Modifier
                                .weight(1.0f)
                                .clickable {
                                    targetWords = size
                                    viewModel.updateProjectDetails(judul, project.genres, pov, size, sinopsis, tema, setting, fandomName)
                                },
                            color = if (isSelected) WarmGoldPrimary else ParchmentCardSurface,
                            shape = RoundedCornerShape(4.dp),
                            border = if (!isSelected) BorderStroke(1.dp, ParchmentBorder) else null
                        ) {
                            Text(
                                text = "$size Kata",
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) ParchmentDarkBackground else InkTextOnBackground
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = tema,
                onValueChange = { tema = it; viewModel.updateProjectDetails(judul, project.genres, pov, targetWords, sinopsis, it, setting, fandomName) },
                label = { Text("Tema Utama & Premis") },
                textStyle = TextStyle(color = InkTextOnBackground),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        item {
            OutlinedTextField(
                value = setting,
                onValueChange = { setting = it; viewModel.updateProjectDetails(judul, project.genres, pov, targetWords, sinopsis, tema, it, fandomName) },
                label = { Text("Setting Makro (Tempat, Waktu, Atmosfer)") },
                textStyle = TextStyle(color = InkTextOnBackground),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        item {
            OutlinedTextField(
                value = sinopsis,
                onValueChange = { sinopsis = it; viewModel.updateProjectDetails(judul, project.genres, pov, targetWords, it, tema, setting, fandomName) },
                label = { Text("Sinopsis Buku Lengkap") },
                textStyle = TextStyle(color = InkTextOnBackground),
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 10,
                colors = textFieldColors()
            )
        }
    }
}

// Stage 2: Profil Gaya Bahasa
@Composable
fun StageStylesPanel(project: NovelProject, viewModel: NovelViewModel) {
    var exampleText by remember { mutableStateOf(project.styleExample) }
    val isAnalyzing by viewModel.isAnalyzingStyle.collectAsStateWithLifecycle()
    val analysisResult by viewModel.styleAnalysisResult.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tahap 2: Contoh Contoh Tulisan & Analisis Gaya",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WarmGoldPrimary
            )
            Text(
                text = "Tempel draf tulisan lawas orisinal Anda. AI akan merangking struktur kalimat, kiasan sastra, dan frekuensi kosa kata Anda sebagai modal dasar penulisan draf baru.",
                fontSize = 12.sp,
                color = SepiaSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = exampleText,
                onValueChange = { exampleText = it },
                label = { Text("Draf Referensi Gaya Anda (Copy-paste di sini)") },
                textStyle = TextStyle(color = InkTextOnBackground, lineHeight = 20.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
                colors = textFieldColors()
            )
        }

        item {
            Button(
                onClick = { viewModel.analyzeStyle(exampleText) },
                enabled = !isAnalyzing && exampleText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(color = ParchmentDarkBackground, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Menganalisis Kosa Kata & Struktur...")
                } else {
                    Icon(imageVector = Icons.Default.Create, contentDescription = null, tint = ParchmentDarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EKSTRAK & ANALISIS TIPE GAYA", fontWeight = FontWeight.Bold)
                }
            }
        }

        val displayResult = if (analysisResult.isNotEmpty()) {
            analysisResult
        } else {
            project.styleAnalysis
        }

        if (displayResult.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = WarmGoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hasil Ekstraksi Stilistika Sastra Indonesia", fontWeight = FontWeight.Bold, color = InkTextOnBackground, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = displayResult,
                            color = InkTextOnSurface,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// Stage 3: Auto Deep Research
@Composable
fun StageResearchPanel(project: NovelProject, viewModel: NovelViewModel) {
    var researchTopic by remember { mutableStateOf("") }
    val isResearching by viewModel.isPerformingResearch.collectAsStateWithLifecycle()
    val researchResult by viewModel.fandomResearchResult.collectAsStateWithLifecycle()

    val parsedGenres = remember(project.genres) {
        project.genres.split(",").map { it.trim().lowercase() }
    }
    val isFanf = parsedGenres.any { it.contains("fanfiction") || it.contains("fanfic") }
    val isPsych = parsedGenres.any { it.contains("psikologi") || it.contains("psychology") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tahap 3: Riset Mendalam Otomatis (Auto-Deep Research)",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WarmGoldPrimary
            )
            
            val description = when {
                isFanf -> "Mode Fandom aktif: \"${project.fandomName.ifBlank { "(Belum ada fandom)" }}\". AI akan mencari aturan log, canon backstory tokoh, penuturan kata khas karakter, dan jargon lore, menjaga cerita bebas OOC secara mutlak."
                isPsych -> "Mode Psikologi aktif. Sediakan topik seperti sindrom drajat tinggi, trauma, skizofrenia, depresi klinis, untuk mengambil pola progress medis yang realistis."
                else -> "Ketikkan tema riset umum untuk memperoleh fakta latar sejarah, data teknis, maupun istilah profesional."
            }
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = SepiaSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = researchTopic,
                onValueChange = { researchTopic = it },
                label = { 
                    Text(
                        if (isFanf) "Ketik nama tokoh fandom / teori canon lore"
                        else if (isPsych) "Ketik gangguan perilaku / anomali psikologi"
                        else "Ketik topik latar sejarah atau sains-fiksi"
                    )
                },
                textStyle = TextStyle(color = InkTextOnBackground),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        item {
            Button(
                onClick = { viewModel.performFandomResearch(researchTopic) },
                enabled = !isResearching && researchTopic.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isResearching) {
                    CircularProgressIndicator(color = ParchmentDarkBackground, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Menjalankan Deep Research AI...")
                } else {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ParchmentDarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JALANKAN AUTO-DEEP RESEARCH", fontWeight = FontWeight.Bold)
                }
            }
        }

        val displayResearch = if (researchResult.isNotEmpty()) {
            researchResult
        } else {
            project.researchData
        }

        if (displayResearch.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = WarmGoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hasil Kumpulan Fakta Riset Tersimpan", fontWeight = FontWeight.Bold, color = InkTextOnBackground, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = displayResearch,
                            color = InkTextOnSurface,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// Stage 4: Tokoh & Karakter
@Composable
fun StageCharactersPanel(charactersList: List<CharacterEntity>, viewModel: NovelViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var currentEditingChar by remember { mutableStateOf<CharacterEntity?>(null) }
    
    var nama by remember { mutableStateOf("") }
    var peran by remember { mutableStateOf("Protagonis") }
    var usiaText by remember { mutableStateOf("") }
    var sifat by remember { mutableStateOf("") }
    var latar by remember { mutableStateOf("") }
    var tujuan by remember { mutableStateOf("") }

    val rolesList = listOf("Protagonis", "Antagonis", "Pendukung", "Tritagonis")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tahap 4: Teater Karakter & Sifat Kejiwaan",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WarmGoldPrimary
            )
            Text(
                text = "Tentukan tokoh novel, faksi kelompok, kepribadian sosiologis, trauma masa lalu, beserta ego utama pendorong plot.",
                fontSize = 12.sp,
                color = SepiaSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        if (!isEditing) {
            item {
                Button(
                    onClick = {
                        nama = ""
                        peran = "Protagonis"
                        usiaText = ""
                        sifat = ""
                        latar = ""
                        tujuan = ""
                        currentEditingChar = null
                        isEditing = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ParchmentDarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TAMBAH PROFIL TOKOH", fontWeight = FontWeight.Bold)
                }
            }

            if (charactersList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada tokoh terdaftar. Mari rancang protagonis utama novel Anda!", color = SepiaSecondary, textAlign = TextAlign.Center, fontSize = 13.sp)
                    }
                }
            } else {
                items(charactersList) { char ->
                    val borderPeran = when (char.peran) {
                        "Protagonis" -> WarmGoldPrimary
                        "Antagonis" -> Color(0xFFC53030)
                        "Pendukung" -> Color(0xFF4A5568)
                        else -> Color(0xFF9F7AEA)
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ParchmentCardSurface,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, borderPeran.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = borderPeran.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = char.peran.uppercase(),
                                            color = borderPeran,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(char.nama, fontWeight = FontWeight.Bold, color = InkTextOnBackground, fontSize = 15.sp)
                                }
                                
                                Row {
                                    IconButton(onClick = {
                                        currentEditingChar = char
                                        nama = char.nama
                                        peran = char.peran
                                        usiaText = char.usiaDeskripsi
                                        sifat = char.sifatPsikologis
                                        latar = char.latarBelakang
                                        tujuan = char.tujuan
                                        isEditing = true
                                    }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = SepiaSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { viewModel.removeCharacter(char) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = CrimsonText, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            
                            if (char.usiaDeskripsi.isNotBlank()) {
                                Text("Profil Singkat / Fisik: ${char.usiaDeskripsi}", fontSize = 11.sp, color = SepiaSecondary, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (char.sifatPsikologis.isNotBlank()) {
                                Text("Ciri Psikologis: ${char.sifatPsikologis}", fontSize = 12.sp, color = InkTextOnSurface, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (char.tujuan.isNotBlank()) {
                                Text("Tujuan / Motif: \"${char.tujuan}\"", fontSize = 11.sp, color = WarmGoldPrimary, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (currentEditingChar == null) "Registrasi Tokoh Baru" else "Edit Profil Tokoh",
                            fontWeight = FontWeight.Bold,
                            color = WarmGoldPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = nama,
                            onValueChange = { nama = it },
                            label = { Text("Nama Tokoh") },
                            textStyle = TextStyle(color = InkTextOnBackground),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = textFieldColors()
                        )

                        Text("Peran dalam Novel", fontSize = 11.sp, color = SepiaSecondary, modifier = Modifier.padding(bottom = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rolesList.forEach { roleOpt ->
                                val active = peran == roleOpt
                                Surface(
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .clickable { peran = roleOpt },
                                    color = if (active) WarmGoldPrimary else ParchmentDarkBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    border = if (!active) BorderStroke(1.dp, ParchmentBorder) else null
                                ) {
                                    Text(
                                        text = roleOpt,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        color = if (active) ParchmentDarkBackground else InkTextOnBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = usiaText,
                            onValueChange = { usiaText = it },
                            label = { Text("Usia & Karakterisik Deskriptif") },
                            textStyle = TextStyle(color = InkTextOnBackground),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = textFieldColors()
                        )

                        OutlinedTextField(
                            value = sifat,
                            onValueChange = { sifat = it },
                            label = { Text("Latar Psikologis & Trauma") },
                            textStyle = TextStyle(color = InkTextOnBackground),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = textFieldColors()
                        )

                        OutlinedTextField(
                            value = latar,
                            onValueChange = { latar = it },
                            label = { Text("Latar Belakang / Sejarah Karakter") },
                            textStyle = TextStyle(color = InkTextOnBackground),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = textFieldColors()
                        )

                        OutlinedTextField(
                            value = tujuan,
                            onValueChange = { tujuan = it },
                            label = { Text("Motivasi Nyata / Apa Yang Ditakuti") },
                            textStyle = TextStyle(color = InkTextOnBackground),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = textFieldColors()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isEditing = false },
                                colors = ButtonDefaults.buttonColors(containerColor = ParchmentDarkBackground, contentColor = InkTextOnBackground),
                                modifier = Modifier.weight(1.0f),
                                border = BorderStroke(1.dp, ParchmentBorder)
                            ) {
                                Text("Batal")
                            }

                            Button(
                                onClick = {
                                    val item = currentEditingChar?.copy(
                                        nama = nama,
                                        peran = peran,
                                        usiaDeskripsi = usiaText,
                                        sifatPsikologis = sifat,
                                        latarBelakang = latar,
                                        tujuan = tujuan
                                    ) ?: CharacterEntity(
                                        novelId = 0,
                                        nama = nama,
                                        peran = peran,
                                        usiaDeskripsi = usiaText,
                                        sifatPsikologis = sifat,
                                        latarBelakang = latar,
                                        tujuan = tujuan
                                    )
                                    viewModel.addOrUpdateCharacter(item)
                                    isEditing = false
                                },
                                enabled = nama.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Text("Simpan")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Stage 5: Alur Bab (Outline)
@Composable
fun StageOutlinePanel(
    project: NovelProject,
    characters: List<CharacterEntity>,
    chapters: List<ChapterEntity>,
    viewModel: NovelViewModel
) {
    val context = LocalContext.current
    var inputChapterCount by remember { mutableStateOf("5") }
    val isGenerating by viewModel.isGeneratingOutline.collectAsStateWithLifecycle()
    val rawResult by viewModel.outlineGenerationResult.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tahap 5: Arsitektur Alur & Struktur Tiga Bagian",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WarmGoldPrimary
            )
            Text(
                text = "Pecah draf cerita novel menjadi rentetan bab utama. Pada novel modern, setiap bab dibagi rata menjadi Bagian A, B, dan C agar optimalisasi kata 2000-3000 terpenuhi dengan detail alur yang matang.",
                fontSize = 12.sp,
                color = SepiaSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        if (chapters.isEmpty()) {
            item {
                Surface(
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ParchmentBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Rancang Kerangka Alur Bab Utama",
                            fontWeight = FontWeight.Bold,
                            color = InkTextOnBackground,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = inputChapterCount,
                            onValueChange = { inputChapterCount = it },
                            label = { Text("Jumlah Bab Yang Ingin Dibuat (contoh: 5)") },
                            textStyle = TextStyle(color = InkTextOnBackground),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = textFieldColors()
                        )

                        Button(
                            onClick = {
                                val count = inputChapterCount.toIntOrNull() ?: 5
                                viewModel.generateOutline(count) {
                                    Toast.makeText(context, "Outline alur bab berhasil disusun", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isGenerating && inputChapterCount.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = ParchmentDarkBackground, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Sedang Merangkai Kerangka Bab...")
                            } else {
                                Icon(imageVector = Icons.Default.List, contentDescription = null, tint = ParchmentDarkBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("BUAT RANCANGAN ALUR TIGA BABAK", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Bab: ${chapters.size} Bab Buku",
                        fontWeight = FontWeight.Bold,
                        color = InkTextOnBackground,
                        fontSize = 14.sp
                    )
                    
                    Button(
                        onClick = {
                            viewModel.generateOutline(chapters.size) {
                                Toast.makeText(context, "Draf bab diatur ulang", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonBackground, contentColor = CrimsonText),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Regenerate Alur", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(chapters) { chap ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    var expanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                                Icon(imageVector = Icons.Default.List, contentDescription = null, tint = WarmGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("BAB ${chap.chapIndex}: ${chap.judul}", fontWeight = FontWeight.Bold, color = InkTextOnBackground, fontSize = 14.sp)
                                    Text(chap.sinopsis, fontSize = 11.sp, color = SepiaSecondary, maxLines = 1)
                                }
                            }
                            
                            Text(
                                text = if (expanded) "▲" else "▼",
                                color = SepiaSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ParchmentBorder)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Sinopsis Keseluruhan Bab: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WarmGoldPrimary)
                            Text(chap.sinopsis, color = InkTextOnSurface, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

                            Text("Bagian A Outline (Awal Bab):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SepiaSecondary)
                            Text(chap.outlineBagianA.ifBlank { "Belum disusun" }, color = InkTextOnSurface, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

                            Text("Bagian B Outline (Tengah Bab):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SepiaSecondary)
                            Text(chap.outlineBagianB.ifBlank { "Belum disusun" }, color = InkTextOnSurface, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

                            Text("Bagian C Outline (Akhir Bab):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SepiaSecondary)
                            Text(chap.outlineBagianC.ifBlank { "Belum disusun" }, color = InkTextOnSurface, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
            }
        }
        
        if (isGenerating && rawResult.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Log Output Perancang Alur:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(rawResult, fontSize = 11.sp, color = SepiaSecondary)
                    }
                }
            }
        }
    }
}

// Stage 6: Studio Penulisan Interaktif
@Composable
fun StageWritingStudioPanel(
    project: NovelProject,
    characters: List<CharacterEntity>,
    chapters: List<ChapterEntity>,
    viewModel: NovelViewModel
) {
    val context = LocalContext.current
    if (chapters.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Harap susun outline alur bab terlebih dahulu di Tahap 5.", color = SepiaSecondary, textAlign = TextAlign.Center)
        }
        return
    }

    var selectedChapterIdx by remember { mutableStateOf(0) }
    var selectedPart by remember { mutableStateOf('A') }
    var customInstruction by remember { mutableStateOf("") }
    
    val selectedChapter = chapters.getOrNull(selectedChapterIdx) ?: chapters[0]
    
    val currentEditorText = remember(selectedChapter, selectedPart) {
        mutableStateOf(
            when (selectedPart) {
                'A' -> selectedChapter.isiBagianA
                'B' -> selectedChapter.isiBagianB
                else -> selectedChapter.isiBagianC
            }
        )
    }

    val isWriting by viewModel.isGeneratingContent.collectAsStateWithLifecycle()

    var wordLookupQuery by remember { mutableStateOf("") }
    var showKamusSheet by remember { mutableStateOf(false) }
    var showSpellBox by remember { mutableStateOf(false) }

    val dictionaryResult by viewModel.dictionaryResult.collectAsStateWithLifecycle()
    val isDictLoading by viewModel.isDictionaryLoading.collectAsStateWithLifecycle()
    
    val isSpellChecking by viewModel.isCheckingSpelling.collectAsStateWithLifecycle()
    val spellResult by viewModel.spellCheckResult.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ParchmentCardSurface,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pilih Navigasi Bab & Draf Studio Aktif", fontWeight = FontWeight.Bold, color = WarmGoldPrimary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1.0f)
                                .clickable { selectedChapterIdx = (selectedChapterIdx + 1) % chapters.size }
                                .background(ParchmentDarkBackground)
                                .border(BorderStroke(1.dp, ParchmentBorder))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BAB ${selectedChapter.chapIndex}: ${selectedChapter.judul.take(15)}...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkTextOnBackground)
                            Text("▼", fontSize = 10.sp, color = WarmGoldPrimary)
                        }

                        listOf('A', 'B', 'C').forEach { partChar ->
                            val active = selectedPart == partChar
                            val isDone = when (partChar) {
                                'A' -> selectedChapter.isiBagianA.isNotBlank()
                                'B' -> selectedChapter.isiBagianB.isNotBlank()
                                else -> selectedChapter.isiBagianC.isNotBlank()
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(0.7f)
                                    .clickable { selectedPart = partChar },
                                color = if (active) WarmGoldPrimary else if (isDone) OliveBackground else ParchmentDarkBackground,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (active) WarmGoldPrimary else ParchmentBorder)
                            ) {
                                Text(
                                    text = "BAG. $partChar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = if (active) ParchmentDarkBackground else if (isDone) OliveText else InkTextOnBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            val partOutline = when (selectedPart) {
                'A' -> selectedChapter.outlineBagianA
                'B' -> selectedChapter.outlineBagianB
                else -> selectedChapter.outlineBagianC
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ParchmentDarkBackground,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DESKRIPSI OUTLINE BAGIAN $selectedPart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = WarmGoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = partOutline.ifBlank { "Belum ada rancangan adegan bagian ini." },
                        fontSize = 12.sp,
                        color = InkTextOnSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = customInstruction,
                onValueChange = { customInstruction = it },
                label = { Text("Instruksi Skenario (Opsional)") },
                textStyle = TextStyle(color = InkTextOnBackground),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.generateChapterPart(selectedChapter, selectedPart, customInstruction) {
                        currentEditorText.value = when (selectedPart) {
                            'A' -> selectedChapter.isiBagianA
                            'B' -> selectedChapter.isiBagianB
                            else -> selectedChapter.isiBagianC
                        }
                    }
                },
                enabled = !isWriting,
                colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isWriting) {
                    CircularProgressIndicator(color = ParchmentDarkBackground, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pena AI Sedang Menulis Draf Prosa...")
                } else {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = ParchmentDarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TULIS DRAF PROSA BAGIAN $selectedPart DENGAN AI", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = "NASKAH NOVEL INDONESIA - BAB ${selectedChapter.chapIndex} BAGIAN $selectedPart",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = SepiaSecondary,
                letterSpacing = 1.sp
            )
            
            OutlinedTextField(
                value = currentEditorText.value,
                onValueChange = {
                    currentEditorText.value = it
                    val updated = when (selectedPart) {
                        'A' -> selectedChapter.copy(isiBagianA = it, statusA = if (it.isNotBlank()) "Selesai" else "Belum Ditulis")
                        'B' -> selectedChapter.copy(isiBagianB = it, statusB = if (it.isNotBlank()) "Selesai" else "Belum Ditulis")
                        else -> selectedChapter.copy(isiBagianC = it, statusC = if (it.isNotBlank()) "Selesai" else "Belum Ditulis")
                    }
                    viewModel.addOrUpdateChapter(updated)
                },
                textStyle = TextStyle(
                    color = InkTextOnBackground,
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    lineHeight = 26.sp
                ),
                maxLines = 1000,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp),
                colors = textFieldColors()
            )
            
            val wordsCount = currentEditorText.value.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            Text(
                text = "Statistik: $wordsCount kata · ${currentEditorText.value.length} karakter",
                fontSize = 11.sp,
                color = SepiaSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showKamusSheet = true },
                    modifier = Modifier.weight(1.0f),
                    colors = ButtonDefaults.buttonColors(containerColor = ParchmentCardSurface, contentColor = WarmGoldPrimary),
                    border = BorderStroke(1.dp, ParchmentBorder),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = WarmGoldPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kamus KBBI", fontSize = 11.sp)
                }

                Button(
                    onClick = { 
                        viewModel.checkSpellingInText(currentEditorText.value)
                        showSpellBox = true 
                    },
                    modifier = Modifier.weight(1.0f),
                    colors = ButtonDefaults.buttonColors(containerColor = ParchmentCardSurface, contentColor = WarmGoldPrimary),
                    border = BorderStroke(1.dp, ParchmentBorder),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = WarmGoldPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ejaan EYD V", fontSize = 11.sp)
                }
            }
        }

        if (showSpellBox) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pemeriksa Ejaan Bahasa Indonesia (EYD V)", fontWeight = FontWeight.Bold, color = WarmGoldPrimary, fontSize = 13.sp)
                            IconButton(onClick = { showSpellBox = false }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = CrimsonText)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        if (isSpellChecking) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = WarmGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mengecek naskah...", fontSize = 12.sp, color = SepiaSecondary)
                            }
                        } else {
                            if (spellResult.isNotEmpty()) {
                                Text(spellResult, fontSize = 12.sp, color = InkTextOnSurface, lineHeight = 18.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val containsFixedText = spellResult.contains("### 2. TEKS YANG TELAH DIPERBAIKI") || spellResult.contains("TEKS YANG TELAH DIPERBAIKI")
                                if (containsFixedText) {
                                    Button(
                                        onClick = {
                                            val parts = spellResult.split("### 2. TEKS YANG TELAH DIPERBAIKI", "TEKS YANG TELAH DIPERBAIKI")
                                            val refinedProse = if (parts.size > 1) parts[1].trim() else ""
                                            if (refinedProse.isNotBlank()) {
                                                currentEditorText.value = refinedProse
                                                
                                                val updated = when (selectedPart) {
                                                    'A' -> selectedChapter.copy(isiBagianA = refinedProse)
                                                    'B' -> selectedChapter.copy(isiBagianB = refinedProse)
                                                    else -> selectedChapter.copy(isiBagianC = refinedProse)
                                                }
                                                viewModel.addOrUpdateChapter(updated)
                                                Toast.makeText(context, "Ejaan dan diksi EYD V berhasil diterapkan", Toast.LENGTH_SHORT).show()
                                                showSpellBox = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("TERAPKAN PERBAIKAN EYD V KE NASKAH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Text("Naskah kosong atau analisis gagal. Masukkan tulisan terlebih dahulu.", fontSize = 12.sp, color = CrimsonText)
                            }
                        }
                    }
                }
            }
        }

        if (showKamusSheet) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ParchmentCardSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pencarian Kamus Bahasa KBBI + Sinonim", fontWeight = FontWeight.Bold, color = WarmGoldPrimary, fontSize = 13.sp)
                            IconButton(onClick = { showKamusSheet = false }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = SepiaSecondary)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = wordLookupQuery,
                                onValueChange = { wordLookupQuery = it },
                                label = { Text("Ketik kata (misal: aksara, gulita, fana)") },
                                textStyle = TextStyle(color = InkTextOnBackground),
                                modifier = Modifier.weight(1.0f),
                                colors = textFieldColors()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.kamusSearch(wordLookupQuery) },
                                enabled = !isDictLoading && wordLookupQuery.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Cari")
                            }
                        }

                        if (isDictLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = WarmGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mencari makna kata...", fontSize = 12.sp, color = SepiaSecondary)
                            }
                        } else {
                            if (dictionaryResult.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .verticalScroll(rememberScrollState()),
                                    color = ParchmentDarkBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, ParchmentBorder)
                                ) {
                                    Text(
                                        text = dictionaryResult,
                                        fontSize = 12.sp,
                                        color = InkTextOnSurface,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Stage 7: Kompilasi & Ekspor
@Composable
fun StageCompileExportPanel(project: NovelProject, chaptersList: List<ChapterEntity>, viewModel: NovelViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var compiledNovelText by remember { mutableStateOf("") }

    val totalWordsAllBook = remember(chaptersList) {
        chaptersList.fold(0) { acc, chap ->
            val A = chap.isiBagianA.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val B = chap.isiBagianB.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val C = chap.isiBagianC.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            acc + A + B + C
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tahap 7: Kompilasi Buku & Statistik Kelulusan Target",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = WarmGoldPrimary
            )
            Text(
                text = "Satukan ketiga sub-bab menjadi novel utuh. Setiap bab dievaluasi kecukupan word count sesuai kriteria minimum targets (2000-3000 kata).",
                fontSize = 12.sp,
                color = SepiaSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ParchmentCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, WarmGoldPrimary.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarmGoldPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("TOTAL KATA KARYA SASTRA", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SepiaSecondary, letterSpacing = 2.sp)
                        Text("$totalWordsAllBook KATA", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = WarmGoldPrimary)
                        Text("Terdistribusi di ${chaptersList.size} Bab Buku utama", fontSize = 11.sp, color = InkTextOnSurface)
                    }
                }
            }
        }

        item {
            Text("Evaluasi Target Panjang Kata per Bab:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WarmGoldPrimary)
        }

        items(chaptersList) { chap ->
            val A = chap.isiBagianA.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val B = chap.isiBagianB.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val C = chap.isiBagianC.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            
            val totalInChapter = A + B + C
            val targetMet = totalInChapter >= project.targetWordCount

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ParchmentCardSurface,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (targetMet) SageGreenTertiary.copy(alpha = 0.5f) else ParchmentBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BAB ${chap.chapIndex}: ${chap.judul}", fontWeight = FontWeight.Bold, color = InkTextOnBackground, fontSize = 13.sp)
                        Text("Bag. A ($A) · Bag. B ($B) · Bag. C ($C)", fontSize = 11.sp, color = SepiaSecondary)
                        
                        LinearProgressIndicator(
                            progress = { (totalInChapter.toFloat() / project.targetWordCount.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.width(160.dp).padding(top = 8.dp),
                            color = if (targetMet) SageGreenTertiary else WarmGoldPrimary,
                            trackColor = ParchmentDarkBackground
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("$totalInChapter / ${project.targetWordCount} Kata", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (targetMet) SageGreenTertiary else InkTextOnBackground)
                        if (targetMet) {
                            Surface(
                                color = OliveBackground,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "TARGET TERPENUHI! ★",
                                    color = OliveText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text("Draf belum cukup", fontSize = 10.sp, color = CrimsonText, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val sb = StringBuilder()
                    sb.append("=========================================\n")
                    sb.append("         ${project.judul.uppercase()}\n")
                    sb.append("=========================================\n\n")
                    sb.append("Genres: ${project.genres}\n")
                    sb.append("POV: ${project.pov}\n\n")
                    sb.append("--- SINOPSIS ---\n${project.sinopsis}\n\n")
                    
                    chaptersList.forEach { c ->
                        sb.append("\n\nBAB ${c.chapIndex}: ${c.judul.uppercase()}\n")
                        sb.append("-----------------------------------------\n\n")
                        sb.append(c.isiBagianA).append("\n\n")
                        sb.append(c.isiBagianB).append("\n\n")
                        sb.append(c.isiBagianC).append("\n\n")
                    }
                    
                    compiledNovelText = sb.toString()
                    Toast.makeText(context, "Kompilasi novel selesai!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarmGoldPrimary, contentColor = ParchmentDarkBackground),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = ParchmentDarkBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("KOMPILASI SELURUH BAB SEKARANG", fontWeight = FontWeight.Bold)
            }
        }

        if (compiledNovelText.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(compiledNovelText))
                            Toast.makeText(context, "Naskah disalin ke clipboard", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ParchmentCardSurface, contentColor = WarmGoldPrimary),
                        border = BorderStroke(1.dp, ParchmentBorder),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.0f).height(44.dp)
                    ) {
                        Text("Salin Naskah")
                    }
                    
                    Button(
                        onClick = {
                            compiledNovelText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ParchmentCardSurface, contentColor = CrimsonText),
                        border = BorderStroke(1.dp, ParchmentBorder),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.0f).height(44.dp)
                    ) {
                        Text("Hapus Preview")
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = compiledNovelText,
                    onValueChange = { compiledNovelText = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = InkTextOnSurface,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    colors = textFieldColors()
                )
            }
        }
    }
}

// styling elements
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WarmGoldPrimary,
    unfocusedBorderColor = ParchmentBorder,
    focusedLabelColor = WarmGoldPrimary,
    unfocusedLabelColor = SepiaSecondary,
    cursorColor = WarmGoldPrimary
)

annotation class ExperimentalExperimentalWithClass
