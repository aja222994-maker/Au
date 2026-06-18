package com.example.data.repository

import com.example.data.database.ChapterDao
import com.example.data.database.CharacterDao
import com.example.data.database.NovelDao
import com.example.data.database.NovelProject
import com.example.data.database.CharacterEntity
import com.example.data.database.ChapterEntity
import com.example.data.network.GeminiClient
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class NovelRepository(
    private val novelDao: NovelDao,
    private val characterDao: CharacterDao,
    private val chapterDao: ChapterDao
) {
    // ----------------- Database Queries -----------------
    val allProjects: Flow<List<NovelProject>> = novelDao.getAllProjectsFlow()
    
    fun getProjectFlow(id: Int): Flow<NovelProject?> = novelDao.getProjectByIdFlow(id)
    
    suspend fun getProject(id: Int): NovelProject? = novelDao.getProjectById(id)
    
    suspend fun saveProject(project: NovelProject): Long {
        project.lastUpdated = System.currentTimeMillis()
        return if (project.id == 0) {
            novelDao.insertProject(project)
        } else {
            novelDao.updateProject(project)
            project.id.toLong()
        }
    }
    
    suspend fun deleteProject(project: NovelProject) {
        val novelId = project.id
        chapterDao.deleteChaptersForNovel(novelId)
        characterDao.deleteCharactersForNovel(novelId)
        novelDao.deleteProject(project)
    }

    // Characters
    fun getCharactersFlow(novelId: Int): Flow<List<CharacterEntity>> = 
        characterDao.getCharactersForNovelFlow(novelId)

    suspend fun getCharacters(novelId: Int): List<CharacterEntity> = 
        characterDao.getCharactersForNovel(novelId)

    suspend fun saveCharacter(character: CharacterEntity) {
        if (character.id == 0) {
            characterDao.insertCharacter(character)
        } else {
            characterDao.updateCharacter(character)
        }
    }

    suspend fun deleteCharacter(character: CharacterEntity) {
        characterDao.deleteCharacter(character)
    }

    // Chapters
    fun getChaptersFlow(novelId: Int): Flow<List<ChapterEntity>> = 
        chapterDao.getChaptersForNovelFlow(novelId)

    suspend fun getChapters(novelId: Int): List<ChapterEntity> = 
        chapterDao.getChaptersForNovel(novelId)

    suspend fun saveChapter(chapter: ChapterEntity) {
        if (chapter.id == 0) {
            chapterDao.insertChapter(chapter)
        } else {
            chapterDao.updateChapter(chapter)
        }
    }

    suspend fun deleteChapter(chapter: ChapterEntity) {
        chapterDao.deleteChapter(chapter)
    }

    // ----------------- Gemini REST AI Functions -----------------

    /**
     * Extracts writing grammar patterns, word usage, dialogue style, and formatting cues
     * from copy-pasted/uploaded samples of literature.
     */
    suspend fun analyzeWritingStyle(sampleText: String): String {
        val prompt = """
            Analisislah teks contoh berikut dan buat rangkuman profil gaya penulisan sastra Indonesia secara rinci dalam bentuk poin-poin yang jelas. Rangkuman ini akan digunakan oleh penulis AI untuk meniru gaya tersebut.
            
            Aspek yang harus dianalisis meliputi:
            1. DIKSI DAN PILIHAN KATA (Misal: puitis, banyak metafora alam, kasual modern, sarkastis, formal, sastra klasik, dll)
            2. STRUKTUR KALIMAT (Misal: kalimat panjang deskriptif mengalir, kalimat pendek taktis penuh ketegangan, gabungan ritmis, dll)
            3. DIALOG DAN SUBTEKS (Cara tokoh berbicara, penempatan kutipan, penggunaan dialek, tersirat vs tersurat)
            4. TONE DAN EMOSI (Melankolis, tegang, komedi kering, magis, psikologis mendalam)
            
            Tulis analisismu dalam Bahasa Indonesian yang formal, padat, dan instruktif.
            
            CONTOH TEKS:
            $sampleText
        """.trimIndent()

        val systemInst = "Kamu adalah kritikus sastra, editor senior, dan pakar linguistik Bahasa Indonesia dengan keahlian meniru gaya penulisan novel."
        val result = GeminiClient.generateContent(prompt, systemInst)
        return if (result == "ERROR_API_KEY") {
            "API Key Gemini belum diatur di Secrets Panel AI Studio. Hubungkan kunci Anda untuk mengaktifkan analisis gaya bahasa otomatis."
        } else {
            result
        }
    }

    /**
     * Conducts in-depth research to align with a fandom (for Fanfiction) or explore 
     * psychological disorders and defense mechanisms (for Psychology genre) to ensure high realism.
     */
    suspend fun performAutoDeepResearch(genres: String, fandomName: String, primaryTopic: String): String {
        val parsedGenres = genres.split(",").map { it.trim().lowercase() }
        val isFanfiction = parsedGenres.any { it.contains("fanfiction") || it.contains("fanfic") }
        val isPsychology = parsedGenres.any { it.contains("psikologi") || it.contains("psychology") }

        val researchTypePrompt = when {
            isFanfiction -> """
                Lakukan RISET MENDALAM OTOMATIS (Deep Research Fandom) mengenai Fandom: "$fandomName".
                Fokus riset utama: "$primaryTopic"
                
                Rangkumlah aspek-aspek berikut agar cerita novel kita selaras dengan kanon asli (lore):
                1. ATURAN LORE (Aturan dunia, sejarah penting, sihir/teknologi, detail latar faksi)
                2. SIFAT KANONIKAL TOKOH (Bagaimana tokoh-tokoh utama bertindak, berbicara, kelemahan, julukan khas)
                3. HUBUNGAN ANTARTOKOH (Afiliasi, konflik lama, atau dinamika emosional kuncinya)
                4. SEKTOR MAKRUM (Tepian terminologi, jargon khas, istilah orisinal fandom yang wajib digunakan dengan benar)
                5. TROPE YANG HARUS DIHINDARI (Kecacatan Out of Character - OOC, atau alur klise fandom yang dibenci pembaca)
            """
            isPsychology -> """
                Lakukan RISET PSIKOLOGI (Deep Psychology Research) mengenai Topik atau Kondisi Mental: "$primaryTopic".
                
                Tuliskan analisis ilmiah fiksi yang realistis mengenai:
                1. PROGRESSI GEJALA (Bagaimana kondisi kejiwaan ini bermanifestasi secara bertahap dalam perilaku sehari-hari, pikiran internal, dan fisik)
                2. MEKANISME PERTAHANAN EGO (Bagaimana seseorang dengan kondisi ini menolak kenyataan, menyangkal, atau merasionalisasi keputusannya)
                3. DETAIL SENSORIS & INTERNAL (Bagaimana dunia terasa di kepala penderita: suara, obsesi, kepanikan, dialog batin)
                4. REAKSI SOSIAL & STIGMA (Bagaimana lingkungan terdekat merespons penderitaan mental ini secara realistis)
                5. TIPS NARATIVE (Cara menceritakannya secara berhati-hati, penuh empati, tanpa menjadikannya klise dangkal)
            """
            else -> """
                Lakukan riset latar belakang dunia nyata untuk genre yang dipilih (${genres}).
                Topik Riset: "$primaryTopic"
                
                Berikan fakta mendalam tentang:
                1. DETAIL HISTORIS, ILMIAH ATAU GEOGRAFIS (Akurasi latar dunia nyata atau logika fiksi)
                2. KOSAKATA KHAS & TERMINOLOGI (Jargon, sebutan, perkakas, istilah khusus bidang ini)
                3. INTEGRITAS SEJARAH & REVISI LOGIS (Akurasi kronologi, isu kultural yang relevan)
                4. INSPIRASI KONFLIK SOSIAL (Bagaimana gesekan sosial/latar ini memicu rintangan tokoh)
            """
        }.trimIndent()

        val prompt = """
            $researchTypePrompt
            
            Pastikan riset ini akurat, mendalam, dan disajikan secara sistematis dalam Bahasa Indonesia dengan format yang mudah dibaca oleh penulis fiksi.
        """.trimIndent()

        val systemInst = "Kamu adalah asisten peneliti, sejarawan, akademisi psikologi, dan analis lore fandom yang sangat teliti dan detail."
        val result = GeminiClient.generateContent(prompt, systemInst)
        return if (result == "ERROR_API_KEY") {
            "API Key Gemini belum diatur di Secrets Panel AI Studio. Hubungkan kunci Anda untuk melakukan riset otomatis mendalam."
        } else {
            result
        }
    }

    /**
     * Breaks a novel outline down into chapters, dividing EACH chapter into Part A, B, and C
     * to meet the minimum length requirements of 2000-3000 words in total.
     */
    suspend fun generateChaptersOutline(novel: NovelProject, characters: List<CharacterEntity>, count: Int): String {
        val charSpecs = characters.joinToString("\n") { 
            "- ${it.nama}: Peran ${it.peran}, Kepribadian: ${it.sifatPsikologis}, Motivasi: ${it.tujuan}" 
        }

        val prompt = """
            Buatkan struktur outline bab yang rinci untuk novel berikut:
            JUDUL: ${novel.judul}
            GENRE UTAMA: ${novel.genres}
            SUDUT PANDANG (POV): ${novel.pov}
            SINOPSIS: ${novel.sinopsis}
            TEMA: ${novel.tema}
            SETTING: ${novel.setting}
            TARGET KATA: ${novel.targetWordCount} per bab (setiap bab harus dibagi rata menjadi Bagian A, B, dan C)
            
            DAFTAR KARAKTER:
            $charSpecs
            
            KONTRIBUTOR RISET / GAYA UTAMA:
            ${if (novel.fandomName.isNotEmpty()) "Fandom: " + novel.fandomName else ""}
            ${if (novel.researchData.isNotEmpty()) "Data Riset: \n" + novel.researchData.take(500) else ""}
            
            TUGAS:
            Buatkan rancangan alur draf sebanyak $count bab secara berurutan.
            Setiap bab harus dipotong menjadi TIGA bagian orisinal (Bagian A, Bagian B, Bagian C) agar pacing optimal dan menjaga detail narasi.
            
            Kamu WAJIB mengembalikan draf outline ini dalam format teks literal terstruktur berikut persis agar bisa diparse program:
            
            CHAP_START
            INDEX: 1
            JUDUL: [Judul Bab 1]
            SINOPSIS: [Sinopsis keseluruhan bab 1 - maksimal 3 kalimat]
            BAGIAN_A: [Rancangan jalannya cerita/adegan untuk Bagian A - awal bab, pembuka setting, pemicu konflik mini]
            BAGIAN_B: [Rancangan jalannya cerita/adegan untuk Bagian B - bagian tengah, konfrontasi/percakapan kunci, pengembangan rahasia]
            BAGIAN_C: [Rancangan jalannya cerita/adegan untuk Bagian C - bagian akhir bab, klimaks kecil bab, gantung (cliffhanger) menuju bab berikutnya]
            CHAP_END
            
            Ulangi blok 'CHAP_START' hingga 'CHAP_END' di atas persis sebanyak $count kali, satu blok untuk setiap bab (Bab 1, Bab 2, dst). Jangan tambahkan teks lain di dalam struktur selain instruksi ini agar parsing tidak rusak. Tulis seluruh rancangan dalam Bahasa Indonesia yang baku dan estetik.
        """.trimIndent()

        val systemInst = "Kamu adalah arsitek plot novel, ahli penulisan kreatif Struktur Tiga Babak (Three-Act Structure), dan ahli tata bahasa fiksi."
        return GeminiClient.generateContent(prompt, systemInst)
    }

    /**
     * Generates a fully immersive, narrative-rich chapter part (A, B, or C) with 700-1000 words,
     * maintaining consistency with selected genres, POV, characters, fandom rules, and analyzed writing style.
     */
    suspend fun generateChapterPartContent(
        novel: NovelProject,
        chapter: ChapterEntity,
        part: Char, // 'A', 'B', or 'C'
        customInst: String,
        characters: List<CharacterEntity>
    ): String {
        val charSpecs = characters.joinToString("\n") { 
            "- ${it.nama}: Peran ${it.peran}, Kepribadian: ${it.sifatPsikologis}, Motivasi: ${it.tujuan}" 
        }

        val outlineTeks = when (part) {
            'A' -> chapter.outlineBagianA
            'B' -> chapter.outlineBagianB
            else -> chapter.outlineBagianC
        }

        val partDescription = when (part) {
            'A' -> "Bagian Pertama (Awal Bab - pembuka suasana, penjalaran emosi, intro adegan)"
            'B' -> "Bagian Kedua (Tengah Bab - dialog krusial, akselerasi masalah, dinamika interpersonal)"
            else -> "Bagian Ketiga (Akhir Bab - konklusi bab mini, peningkatan tensi dramatis, hook tak terduga)"
        }

        // Gather all context
        val contextPrompt = """
            PROYEK NOVEL SASTRA INDONESIA
            JUDUL: ${novel.judul}
            GENRE UTAMA: ${novel.genres}
            SUDUT PANDANG (POV): ${novel.pov}
            SINOPSIS NOVEL: ${novel.sinopsis}
            TEMA UTAMA: ${novel.tema}
            LOKASI/SETTING: ${novel.setting}
            
            REKAMAN GAYA BAHASA (Kritik Penulisan):
            ${if (novel.styleAnalysis.isNotEmpty()) novel.styleAnalysis else "Gaya penulisan sastra Indonesia modern bebas, puitis, naratif, mengutamakan 'Show, Don't Tell'."}
            
            REKAMAN RISET DUNIA (Lore & Aturan Fandom/Psikologi):
            ${if (novel.researchData.isNotEmpty()) novel.researchData else "Gunakan logika psikologis tokoh yang realistis, hindari hal-hal klise."}
            
            DAFTAR TOKOH AKTIF:
            $charSpecs
            
            DETAIL BAB INI:
            BAB KE: ${chapter.chapIndex}
            JUDUL BAB: ${chapter.judul}
            SINOPSIS BAB: ${chapter.sinopsis}
            
            BAGIAN YANG DITULIS SEKARANG:
            $partDescription
            FOKUS ALUR BAGIAN INI: $outlineTeks
            
            INSTRUKSI TAMBAHAN PENULIS:
            ${if (customInst.isNotEmpty()) customInst else "Tulis secara organik, perkuat dinamika emosi antartokoh."}
            
            PANDUAN MENULIS (MUTLAK):
            1. Targetkan penulisan yang kaya, detail, dan mendalam berkisar antara **700 hingga 1000 kata** untuk sub-bab ini.
            2. Gunakan prinsip "Show, Don't Tell" (Tunjukkan melalui tindakan, deskripsi sensoris, detak jantung, dan hembusan angin, jangan sekadar menceritakan).
            3. Patuhi dengan sangat ketat SUDUT PANDANG (POV) yaitu: "${novel.pov}".
            4. Karakter-karakter wajib bertindak sesuai dengan draf kepribadian dan tidak boleh bertindak aneh (OOC).
            5. Tuliskan teks sastra mengalir langsung dari adegan pertama, tanpa kata pengantar ("Berikut adalah bab Anda", dsb), langsung ke naskah novel secara murni.
        """.trimIndent()

        val systemInst = "Kamu adalah novelis Indonesia pemenang penghargaan, maestro sastra yang mampu menghidupkan emosi terdalam lewat jalinan diksi yang indah, kaya metafora, dan mengalir natural."
        return GeminiClient.generateContent(contextPrompt, systemInst)
    }

    /**
     * Looks up an Indonesian word, providing parts of speech, KBBI meanings, synonyms, and illustrative sentences.
     */
    suspend fun kamusLookUp(word: String): String {
        val prompt = """
            Bertindaklah sebagai Kamus Besar Bahasa Indonesia (KBBI) digital terintegrasi yang akurat.
            Berikan entri kamus lengkap untuk kata kunci berikut: "$word"
            
            Format keluaran harus mencakup:
            1. KATEGORI KATA (Kelas kata: Nomina, Verba, Adjektiva, Adverbia, dll)
            2. DEFINISI RESMI (Sesuai kaidah KBBI terlengkap)
            3. SINONIM (Persamaan kata Indonesia: minimal 5 kata sinonim yang estetik dan setara)
            4. CONTOH PENGGUNAAN (2 kalimat sastra yang indah menggunakan kata tersebut)
            
            Sajikan dengan tata rias teks yang rapi dan elegan dalam Bahasa Indonesia.
        """.trimIndent()

        val systemInst = "Kamu adalah kamus bahasa Indonesia interaktif, ahli leksikon bahasa Indonesia, dan penyusun kamus KBBI."
        return GeminiClient.generateContent(prompt, systemInst)
    }

    /**
     * Corrects spelling errors, non-standard Indonesian words, affix errors, and suggests changes.
     */
    suspend fun periksaEjaanIndonesian(text: String): String {
        val prompt = """
            Bertindaklah sebagai Pemeriksa Ejaan (Spell Checker) dan Editor Bahasa Indonesia yang akurat dan teliti sesuai Pedoman Umum Ejaan Bahasa Indonesia (PUEBI) / Ejaan yang Disempurnakan (EYD) Edisi V.
            
            Periksa teks naskah novel di bawah ini untuk mencari:
            1. Kesalahan Ketik / Tipografi (Typo)
            2. Kata Tidak Baku (Contoh: 'fikir' -> 'pikir', 'kwalitas' -> 'kualitas', 'nafas' -> 'napas', 'hirup' -> 'isap', 'perduli' -> 'peduli')
            3. Kesalahan imbuhan atau afiksasi (Contoh: 'mempengaruhi' -> 'memengaruhi', 'merubah' -> 'mengubah', 'mengkonsumsi' -> 'mengonsumsi')
            4. Ketidaktepatan penulisan di- Kata Depan vs di- Imbuhan (Contoh: 'di rumah' vs 'dimakan')
            
            Kembalikan laporan pemeriksaan dengan format sebagai berikut:
            
            ### 1. DAFTAR KOREKSI EJAAN
            - Tulislah daftar kata yang salah beserta koreksi yang benar dengan format:
              * [Salah] -> *Koreksi* : (Alasan singkat)
            
            ### 2. TEKS YANG TELAH DIPERBAIKI (DROP-IN READY)
            Sajikan seluruh teks asli di bawah ini yang telah diperbaiki ejaannya secara total tanpa mengubah substansi cerita maupun diksi sastra aslinya.
            
            TEKS NASKAH:
            $text
        """.trimIndent()

        val systemInst = "Kamu adalah editor naskah utama di penerbit buku mayor nasional, ahli PUEBI/EYD V, dan ahli tata bahasa Indonesia."
        return GeminiClient.generateContent(prompt, systemInst)
    }
}
