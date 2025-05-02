package com.example.learningenglishvocab.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningenglishvocab.data.model.AnsweredTerm
import com.example.learningenglishvocab.data.model.Term
import com.example.learningenglishvocab.data.model.TermStatus
import com.example.learningenglishvocab.data.model.UserRole
import com.example.learningenglishvocab.data.model.VocabSet
import com.example.learningenglishvocab.data.repository.UserRepository
import com.example.learningenglishvocab.data.repository.VocabSetRepository
import kotlinx.coroutines.launch

class VocabSetViewModel(
    private val repository: VocabSetRepository = VocabSetRepository(),
    private val authViewModel: AuthViewModel,
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val currentUserId: String?
        get() = authViewModel.getCurrentUserId()

    var vocabSetId by mutableStateOf<String?>(null)
    var vocabSetName by mutableStateOf("")
    var isPublic by mutableStateOf(false)
    var created_by by mutableStateOf("")
    var terms = mutableStateListOf(Term(), Term())

    var premiumContent by mutableStateOf(false)

    private var _answeredTerms = mutableStateListOf<AnsweredTerm>()
    val answeredTerms: List<AnsweredTerm> get() = _answeredTerms

    private var _unknownTerms = mutableStateListOf<Term>()
    val unknownTerms: List<Term> get() = _unknownTerms

    private var currentRound = 1

    fun startNewRound() {
        currentRound++
        _unknownTerms.clear()
        _unknownTerms.addAll(terms.filter { it.status != TermStatus.KNOWN })
    }

    fun addAnsweredTerm(term: Term, isCorrect: Boolean) {
        val existingAnswer = _answeredTerms.find {
            it.term.id == term.id && it.round == currentRound
        }

        if (existingAnswer != null) {
            if (isCorrect) {
                val termIndex = terms.indexOfFirst { it.id == term.id }
                if (termIndex != -1) {
                    if (existingAnswer.isCorrect) {
                        terms[termIndex] = terms[termIndex].copy(status = TermStatus.KNOWN)
                    }
                }
            }
        } else {
            val updatedStatus = if (isCorrect) TermStatus.KNOWN else TermStatus.LEARNING
            _answeredTerms.add(AnsweredTerm(term.copy(status = updatedStatus), isCorrect, currentRound))

            // Cập nhật trạng thái trong terms drape
            val termIndex = terms.indexOfFirst { it.id == term.id }
            if (termIndex != -1) {
                terms[termIndex] = terms[termIndex].copy(status = updatedStatus)
            }
        }
        _unknownTerms.clear()
        _unknownTerms.addAll(terms.filter { it.status != TermStatus.KNOWN })
    }

    fun clearLearningResults() {
        _answeredTerms.clear()
        _unknownTerms.clear()
        terms = terms.map { it.copy(status = TermStatus.NONE) }.toMutableStateList()
        currentRound = 1
    }

    fun addTermField() {
        terms.add(Term())
    }

    fun removeTerm(index: Int) {
        if (index in terms.indices) {
            terms.removeAt(index)
        }
    }

    fun updateTerm(index: Int, newTerm: String) {
        terms[index] = terms[index].copy(term = newTerm)
    }

    fun updateDefinition(index: Int, newDefinition: String) {
        terms[index] = terms[index].copy(definition = newDefinition)
    }

    fun togglePrivacy() {
        isPublic = !isPublic
    }

    fun clear() {
        vocabSetId = null
        vocabSetName = ""
        isPublic = false
        created_by = ""
        terms = mutableStateListOf(Term())
        clearLearningResults()
    }

    fun loadVocabSetById(id: String) {
        repository.getVocabSetById(id) { vocabSet ->
            vocabSetId = vocabSet.vocabSetId
            vocabSetName = vocabSet.vocabSetName
            isPublic = vocabSet._public
            premiumContent = vocabSet.premiumContent
            created_by = vocabSet.created_by
            terms = vocabSet.terms.toMutableStateList()
            clearLearningResults()
        }
    }

    fun saveToFirebase(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                try {
                    val user = userRepository.getUser(userId)
                    if (user == null) {
                        onFailure(Exception("Không tìm thấy thông tin người dùng"))
                        return@launch
                    }
                    val finalIsPremiumContent = if (user.role == UserRole.ADMIN) premiumContent else false
                    val vocabSet = VocabSet(
                        vocabSetId = vocabSetId ?: "",
                        vocabSetName = vocabSetName,
                        _public = isPublic,
                        created_by = userId,
                        created_at = System.currentTimeMillis(),
                        updated_at = System.currentTimeMillis(),
                        terms = terms,
                        premiumContent = finalIsPremiumContent
                    )
                    repository.addVocabSet(vocabSet, onSuccess, onFailure)
                } catch (e: Exception) {
                    onFailure(e)
                }
            }
        } ?: onFailure(Exception("Người dùng chưa đăng nhập"))
    }

    fun updateVocabSet(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                try {
                    Log.d("VocabSetViewModel", "Current User ID: $userId")
                    val user = userRepository.getUser(userId)
                    Log.d("VocabSetViewModel", "User found: ${user}")
                    if (user == null) {
                        onFailure(Exception("Không tìm thấy thông tin người dùng"))
                        return@launch
                    }
                    Log.d("VocabSetViewModel", "User found: ${user.username}")
                    val vocabSet = VocabSet(
                        vocabSetId = vocabSetId ?: "",
                        vocabSetName = vocabSetName,
                        _public = isPublic,
                        created_by = userId,
                        terms = terms,
                        updated_at = System.currentTimeMillis(),
                        premiumContent = premiumContent
                    )
                    repository.updateVocabSet(vocabSet, onSuccess, onFailure)
                } catch (e: Exception) {
                    onFailure(e)
                }
            }
        } ?: onFailure(Exception("Người dùng chưa đăng nhập"))
    }

    fun deleteVocabSet(
        onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        vocabSetId?.let { id ->
            repository.deleteVocabSet(id, onSuccess, onFailure)
        }
    }
}