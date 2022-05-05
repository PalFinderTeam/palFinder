package com.github.palFinderTeam.palfinder.utils.generics

import android.icu.util.Calendar
import android.util.Log
import com.github.palFinderTeam.palfinder.utils.Response
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreRepository<T: FirebaseObject>(val db: FirebaseFirestore, private val column: String, private val timeField: String?, val converter: (DocumentSnapshot)->T?): Repository<T> {
    override suspend fun fetch(uuid: String): T? {
        return try {
            converter(db.collection(column)
                .document(uuid).get().await())
        } catch (e: Exception) {
            Log.d("db obj", "failed safely")
            null
        }
    }

    override fun fetchFlow(uuid: String): Flow<Response<T>> {
        return flow {
            emit(Response.Loading())

            val obj = fetch(uuid)
            if (obj != null) {
                emit(Response.Success(obj))
            } else {
                emit(Response.Failure("Could not find obj."))
            }

        }.catch { error ->
            emit(Response.Failure(error.message.orEmpty()))
        }
    }

    override suspend fun fetch(uuids: List<String>): List<T> {
        // Firebase don't support more than 10 ids in query.
        val chunked = uuids.chunked(10)
        val queries = chunked.map {
            db.collection(column).whereIn(FieldPath.documentId(), it).get()
        }
        val result = Tasks.whenAllSuccess<QuerySnapshot>(queries).await()
        return result.flatMap { it.documents.mapNotNull { converter(it) } }
    }

    override suspend fun edit(uuid: String, field: String, value: Any): String? {
        if (!exists(uuid)) return null
        return try {
            db.collection(column).document(uuid).update(field, value).await()
            uuid
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun edit(uuid: String, obj: T): String? {
        return try {
            db.collection(column).document(uuid).update(obj.toFirestoreData())
                .await()
            uuid
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun create(obj: T): String? {
        return try {
            db.collection(column).document(obj.getUUID())
                .set(obj.toFirestoreData()).await()
            obj.getUUID()
        } catch (e: Exception) {
            null
        }
    }

    override fun fetchAll(currentDate: Calendar?, ): Flow<List<T>> {
        var query: Query = db.collection(column)

        if (currentDate != null && timeField != null) {
            query = query.whereGreaterThan(timeField, currentDate.time)
        }

        return callbackFlow {
            val listenerRegistration = query
                .addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException != null) {
                        cancel(
                            message = "Error fetching profiles",
                            cause = firebaseFirestoreException
                        )
                        return@addSnapshotListener
                    }
                    val map = querySnapshot?.documents
                        ?.mapNotNull { converter(it) }
                    if (map != null) {
                        trySend(map)
                    }
                }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    override suspend fun exists(uuid: String): Boolean {
        return db.collection(column).document(uuid).get().await().exists()
    }
}