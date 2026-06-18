package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novel_projects")
data class NovelProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var judul: String = "",
    var genres: String = "", // Comma-separated list of up to 3 genres
    var pov: String = "Orang Pertama (Aku)",
    var targetWordCount: Int = 2000, // Word count per chapter (e.g. 2000 or 3000)
    var styleExample: String = "",
    var styleAnalysis: String = "",
    var fandomName: String = "",
    var researchData: String = "", // Fandom canon rules, themes or psychological profiles
    var sinopsis: String = "",
    var tema: String = "",
    var setting: String = "",
    var lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val novelId: Int,
    var nama: String,
    var peran: String, // Protagonis, Antagonis, Pendukung, Tritagonis
    var usiaDeskripsi: String = "",
    var sifatPsikologis: String = "", // Deep psychological traits
    var latarBelakang: String = "",
    var tujuan: String = ""
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val novelId: Int,
    var chapIndex: Int,
    var judul: String,
    var sinopsis: String = "",
    
    // Each chapter is divided into THREE sub-chapters/parts for optimal writing
    var outlineBagianA: String = "",
    var outlineBagianB: String = "",
    var outlineBagianC: String = "",
    
    var isiBagianA: String = "",
    var isiBagianB: String = "",
    var isiBagianC: String = "",
    
    var statusA: String = "Belum Ditulis", // Belum Ditulis, Draf, Selesai
    var statusB: String = "Belum Ditulis",
    var statusC: String = "Belum Ditulis"
)
