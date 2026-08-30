package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CitationSource
import com.example.data.model.MasteryStatus
import com.example.data.model.SubjectCategory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return ""
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return try {
            adapter.fromJson(data) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromCitationList(list: List<CitationSource>?): String {
        if (list == null) return ""
        val type = Types.newParameterizedType(List::class.java, CitationSource::class.java)
        val adapter = moshi.adapter<List<CitationSource>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toCitationList(data: String?): List<CitationSource> {
        if (data.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, CitationSource::class.java)
        val adapter = moshi.adapter<List<CitationSource>>(type)
        return try {
            adapter.fromJson(data) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromSubjectCategory(category: SubjectCategory?): String {
        return category?.name ?: SubjectCategory.BIOLOGY.name
    }

    @TypeConverter
    fun toSubjectCategory(name: String?): SubjectCategory {
        return try {
            SubjectCategory.valueOf(name ?: SubjectCategory.BIOLOGY.name)
        } catch (e: Exception) {
            SubjectCategory.BIOLOGY
        }
    }

    @TypeConverter
    fun fromMasteryStatus(status: MasteryStatus?): String {
        return status?.name ?: MasteryStatus.LOCKED.name
    }

    @TypeConverter
    fun toMasteryStatus(name: String?): MasteryStatus {
        return try {
            MasteryStatus.valueOf(name ?: MasteryStatus.LOCKED.name)
        } catch (e: Exception) {
            MasteryStatus.LOCKED
        }
    }
}
