package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NovelDao {
    @Query("SELECT * FROM novel_projects ORDER BY lastUpdated DESC")
    fun getAllProjectsFlow(): Flow<List<NovelProject>>

    @Query("SELECT * FROM novel_projects WHERE id = :id")
    fun getProjectByIdFlow(id: Int): Flow<NovelProject?>

    @Query("SELECT * FROM novel_projects WHERE id = :id")
    suspend fun getProjectById(id: Int): NovelProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: NovelProject): Long

    @Update
    suspend fun updateProject(project: NovelProject)

    @Delete
    suspend fun deleteProject(project: NovelProject)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE novelId = :novelId ORDER BY id ASC")
    fun getCharactersForNovelFlow(novelId: Int): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE novelId = :novelId ORDER BY id ASC")
    suspend fun getCharactersForNovel(novelId: Int): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE novelId = :novelId")
    suspend fun deleteCharactersForNovel(novelId: Int)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY chapIndex ASC")
    fun getChaptersForNovelFlow(novelId: Int): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE novelId = :novelId ORDER BY chapIndex ASC")
    suspend fun getChaptersForNovel(novelId: Int): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE novelId = :novelId")
    suspend fun deleteChaptersForNovel(novelId: Int)
}
