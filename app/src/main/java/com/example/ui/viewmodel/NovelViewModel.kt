package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ChapterEntity
import com.example.data.database.CharacterEntity
import com.example.data.database.NovelProject
import com.example.data.repository.NovelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NovelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NovelRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repository = NovelRepository(db.novelDao(), db.characterDao(), db.chapterDao())
    }

    // List of all novels in Database
    val allProjects: StateFlow<List<NovelProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently active novel project ID
    private val _activeProjectId = MutableStateFlow<Int?>(null)
    val activeProjectId: StateFlow<Int?> = _activeProjectId.asStateFlow()

    // Active project metadata
    val activeProject: StateFlow<NovelProject?> = _activeProjectId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getProjectFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current characters active list
    val activeCharacters: StateFlow<List<CharacterEntity>> = _activeProjectId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getCharactersFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current chapters active list
    val activeChapters: StateFlow<List<ChapterEntity>> = _activeProjectId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getChaptersFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----------------- UI Loading & Prompt Responses -----------------
    val isAnalyzingStyle = MutableStateFlow(false)
    val styleAnalysisResult = MutableStateFlow("")

    val isPerformingResearch = MutableStateFlow(false)
    val fandomResearchResult = MutableStateFlow("")

    val isGeneratingOutline = MutableStateFlow(false)
    val outlineGenerationResult = MutableStateFlow("")

    val isGeneratingContent = MutableStateFlow(false)
    
    // Spell Checker States
    val isCheckingSpelling = MutableStateFlow(false)
    val spellCheckResult = MutableStateFlow("")

    // KBBI Dictionary States
    val isDictionaryLoading = MutableStateFlow(false)
    val dictionaryResult = MutableStateFlow("")

    // ----------------- Project Selection -----------------
    fun selectProject(projectId: Int?) {
        _activeProjectId.value = projectId
        // Reset process outputs
        styleAnalysisResult.value = ""
        fandomResearchResult.value = ""
        outlineGenerationResult.value = ""
        spellCheckResult.value = ""
        dictionaryResult.value = ""
    }

    fun createNewProject(onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val emptyProject = NovelProject(
                judul = "Novel Tanpa Judul",
                genres = "Romansa",
                pov = "Orang Pertama (Aku)"
            )
            val newId = repository.saveProject(emptyProject)
            selectProject(newId.toInt())
            onCreated(newId.toInt())
        }
    }

    fun updateProjectDetails(
        judul: String,
        genres: String,
        pov: String,
        targetWords: Int,
        sinopsis: String,
        tema: String,
        setting: String,
        fandom: String
    ) {
        val current = activeProject.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                judul = judul,
                genres = genres,
                pov = pov,
                targetWordCount = targetWords,
                sinopsis = sinopsis,
                tema = tema,
                setting = setting,
                fandomName = fandom
            )
            repository.saveProject(updated)
        }
    }

    fun deleteCurrentProject(onDeleted: () -> Unit) {
        val current = activeProject.value ?: return
        viewModelScope.launch {
            repository.deleteProject(current)
            selectProject(null)
            onDeleted()
        }
    }

    // ----------------- Characters management -----------------
    fun addOrUpdateCharacter(character: CharacterEntity) {
        val currentNovelId = _activeProjectId.value ?: return
        viewModelScope.launch {
            val item = character.copy(novelId = currentNovelId)
            repository.saveCharacter(item)
        }
    }

    fun removeCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            repository.deleteCharacter(character)
        }
    }

    // ----------------- Chapters management -----------------
    fun addOrUpdateChapter(chapter: ChapterEntity) {
        val currentNovelId = _activeProjectId.value ?: return
        viewModelScope.launch {
            val item = chapter.copy(novelId = currentNovelId)
            repository.saveChapter(item)
        }
    }

    fun removeChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
        }
    }

    // ----------------- Core Gemini Features -----------------

    fun analyzeStyle(sampleText: String) {
        val current = activeProject.value ?: return
        if (sampleText.isBlank()) return
        viewModelScope.launch {
            isAnalyzingStyle.value = true
            styleAnalysisResult.value = ""
            try {
                val result = repository.analyzeWritingStyle(sampleText)
                styleAnalysisResult.value = result
                // Automatically save to the active project attributes
                val updated = current.copy(
                    styleExample = sampleText,
                    styleAnalysis = result
                )
                repository.saveProject(updated)
            } catch (e: Exception) {
                styleAnalysisResult.value = "Error: ${e.localizedMessage}"
            } finally {
                isAnalyzingStyle.value = false
            }
        }
    }

    fun performFandomResearch(topic: String) {
        val current = activeProject.value ?: return
        if (topic.isBlank()) return
        viewModelScope.launch {
            isPerformingResearch.value = true
            fandomResearchResult.value = ""
            try {
                val result = repository.performAutoDeepResearch(
                    genres = current.genres,
                    fandomName = current.fandomName,
                    primaryTopic = topic
                )
                fandomResearchResult.value = result
                
                // Automatically append research content to project metadata
                val updatedData = if (current.researchData.isBlank()) {
                    result
                } else {
                    "${current.researchData}\n\n=== RISET BARU ($topic) ===\n$result"
                }
                val updated = current.copy(researchData = updatedData)
                repository.saveProject(updated)
            } catch (e: Exception) {
                fandomResearchResult.value = "Error: ${e.localizedMessage}"
            } finally {
                isPerformingResearch.value = false
            }
        }
    }

    fun generateOutline(chapterCount: Int, onSuccess: () -> Unit) {
        val current = activeProject.value ?: return
        val characters = activeCharacters.value
        viewModelScope.launch {
            isGeneratingOutline.value = true
            outlineGenerationResult.value = ""
            try {
                val rawOutline = repository.generateChaptersOutline(current, characters, chapterCount)
                outlineGenerationResult.value = rawOutline
                
                // Parse and insert chapters into the Database
                val parsedChapters = parseOutlineText(rawOutline, current.id)
                if (parsedChapters.isNotEmpty()) {
                    // Delete old chapters for novel first to rebuild cleanly
                    val oldChapters = repository.getChapters(current.id)
                    oldChapters.forEach { repository.deleteChapter(it) }
                    
                    // Insert new chapters
                    parsedChapters.forEach { repository.saveChapter(it) }
                    onSuccess()
                } else {
                    outlineGenerationResult.value = "$rawOutline\n\n(Gagal mem-parsing teks ke database. Coba generate ulang)."
                }
            } catch (e: Exception) {
                outlineGenerationResult.value = "Error: ${e.localizedMessage}"
            } finally {
                isGeneratingOutline.value = false
            }
        }
    }

    fun generateChapterPart(chapter: ChapterEntity, part: Char, customInstruction: String, onFinished: () -> Unit) {
        val current = activeProject.value ?: return
        val characters = activeCharacters.value
        viewModelScope.launch {
            isGeneratingContent.value = true
            try {
                val text = repository.generateChapterPartContent(
                    novel = current,
                    chapter = chapter,
                    part = part,
                    customInst = customInstruction,
                    characters = characters
                )
                
                // Update specific part content and change status to Selesai
                val updatedChap = when (part) {
                    'A' -> chapter.copy(isiBagianA = text, statusA = "Selesai")
                    'B' -> chapter.copy(isiBagianB = text, statusB = "Selesai")
                    else -> chapter.copy(isiBagianC = text, statusC = "Selesai")
                }
                repository.saveChapter(updatedChap)
                onFinished()
            } catch (e: Exception) {
                // handle error
            } finally {
                isGeneratingContent.value = false
            }
        }
    }

    fun checkSpellingInText(naskah: String) {
        if (naskah.isBlank()) return
        viewModelScope.launch {
            isCheckingSpelling.value = true
            spellCheckResult.value = ""
            try {
                val result = repository.periksaEjaanIndonesian(naskah)
                spellCheckResult.value = result
            } catch (e: Exception) {
                spellCheckResult.value = "Error: ${e.localizedMessage}"
            } finally {
                isCheckingSpelling.value = false
            }
        }
    }

    fun kamusSearch(vocab: String) {
        if (vocab.isBlank()) return
        viewModelScope.launch {
            isDictionaryLoading.value = true
            dictionaryResult.value = ""
            try {
                val result = repository.kamusLookUp(vocab)
                dictionaryResult.value = result
            } catch (e: Exception) {
                dictionaryResult.value = "Error: ${e.localizedMessage}"
            } finally {
                isDictionaryLoading.value = false
            }
        }
    }

    // ----------------- Parsing Utility -----------------
    private fun parseOutlineText(text: String, novelId: Int): List<ChapterEntity> {
        val list = mutableListOf<ChapterEntity>()
        val blocks = text.split("CHAP_START")
        var indexCount = 1
        for (block in blocks) {
            if (block.isBlank() || !block.contains("CHAP_END")) continue
            
            var judul = "Bab $indexCount"
            var sinopsis = ""
            var outlineA = ""
            var outlineB = ""
            var outlineC = ""
            
            val lines = block.split("\n")
            var currentSection = ""
            
            for (line in lines) {
                val trimLine = line.trim()
                if (trimLine.startsWith("INDEX:")) {
                    // Skip
                } else if (trimLine.startsWith("JUDUL:")) {
                    judul = trimLine.substringAfter("JUDUL:").trim()
                } else if (trimLine.startsWith("SINOPSIS:")) {
                    sinopsis = trimLine.substringAfter("SINOPSIS:").trim()
                } else if (trimLine.startsWith("BAGIAN_A:")) {
                    currentSection = "A"
                    outlineA = trimLine.substringAfter("BAGIAN_A:").trim()
                } else if (trimLine.startsWith("BAGIAN_B:")) {
                    currentSection = "B"
                    outlineB = trimLine.substringAfter("BAGIAN_B:").trim()
                } else if (trimLine.startsWith("BAGIAN_C:")) {
                    currentSection = "C"
                    outlineC = trimLine.substringAfter("BAGIAN_C:").trim()
                } else if (trimLine.contains("CHAP_END")) {
                    break
                } else {
                    if (currentSection == "A") {
                        if (outlineA.isEmpty()) outlineA = trimLine else outlineA += "\n" + trimLine
                    } else if (currentSection == "B") {
                        if (outlineB.isEmpty()) outlineB = trimLine else outlineB += "\n" + trimLine
                    } else if (currentSection == "C") {
                        if (outlineC.isEmpty()) outlineC = trimLine else outlineC += "\n" + trimLine
                    }
                }
            }
            
            // Clean outlines of potential markers
            judul = judul.removePrefix("[").removeSuffix("]")
            sinopsis = sinopsis.removePrefix("[").removeSuffix("]")
            outlineA = outlineA.removePrefix("[").removeSuffix("]")
            outlineB = outlineB.removePrefix("[").removeSuffix("]")
            outlineC = outlineC.removePrefix("[").removeSuffix("]")

            list.add(
                ChapterEntity(
                    novelId = novelId,
                    chapIndex = indexCount++,
                    judul = judul,
                    sinopsis = sinopsis,
                    outlineBagianA = outlineA.trim(),
                    outlineBagianB = outlineB.trim(),
                    outlineBagianC = outlineC.trim()
                )
            )
        }
        return list
    }
}
