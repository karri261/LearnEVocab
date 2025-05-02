package com.example.learningenglishvocab.data.repository

import com.example.learningenglishvocab.data.model.Definition
import com.example.learningenglishvocab.data.model.DictionaryResponse
import com.example.learningenglishvocab.data.model.Meaning
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DictionaryRepository {
    // Lấy danh sách gợi ý từ Datamuse API
    suspend fun getSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val url = "https://api.datamuse.com/words?sp=$query*"
        val (_, _, result) = url.httpGet().responseJson()
        when (result) {
            is Result.Success -> {
                try {
                    val jsonArray = result.get().array()
                    (0 until jsonArray.length()).map {
                        jsonArray.getJSONObject(it).getString("word")
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
            is Result.Failure -> emptyList()
        }
    }

    // Lấy nghĩa chi tiết từ Free Dictionary API
    suspend fun searchWord(word: String): DictionaryResponse? = withContext(Dispatchers.IO) {
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
        val (_, _, result) = url.httpGet().responseJson()

        when (result) {
            is Result.Success -> {
                try {
                    val jsonArray = result.get().array()
                    val json = jsonArray.getJSONObject(0) // Lấy entry đầu tiên
                    val meaningsJson = json.getJSONArray("meanings")
                    val meanings = mutableListOf<Meaning>()
                    for (j in 0 until meaningsJson.length()) {
                        val meaningJson = meaningsJson.getJSONObject(j)
                        val definitionsJson = meaningJson.getJSONArray("definitions")
                        val definitions = mutableListOf<Definition>()
                        for (k in 0 until definitionsJson.length()) {
                            definitions.add(
                                Definition(
                                    definition = definitionsJson.getJSONObject(k).getString("definition")
                                )
                            )
                        }
                        meanings.add(
                            Meaning(
                                partOfSpeech = meaningJson.getString("partOfSpeech"),
                                definitions = definitions
                            )
                        )
                    }
                    DictionaryResponse(
                        word = json.getString("word"),
                        meanings = meanings
                    )
                } catch (e: Exception) {
                    null
                }
            }
            is Result.Failure -> null
        }
    }
}