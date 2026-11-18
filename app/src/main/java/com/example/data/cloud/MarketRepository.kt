package com.example.data.cloud

import android.content.Context
import com.example.R
import com.example.game.model.BaseStashState
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MarketRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Google Sign-In required before accessing live market.")
    }

    fun observeUserStash(userId: String): Flow<UserCloudStash?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(UserCloudStash::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    suspend fun ensureUserStash(userId: String, initialCallsign: String, seedStash: BaseStashState): Result<UserCloudStash> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(UserCloudStash::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                Result.success(existing ?: UserCloudStash(userId = userId))
            } else {
                val safeCallsign = initialCallsign.trim().ifBlank { "SURVIVOR" }.take(24)
                val weapons = seedStash.unlockedWeaponKeys.ifEmpty { setOf("baton") }.take(10)
                val payload = mapOf(
                    "userId" to userId,
                    "callsign" to safeCallsign,
                    "credits" to seedStash.credits.coerceIn(0, 100_000_000).toLong(),
                    "matScrap" to seedStash.scrapMetal.coerceIn(0, 100_000).toLong(),
                    "matGunpowder" to seedStash.gunpowder.coerceIn(0, 100_000).toLong(),
                    "matCloth" to seedStash.tacticalCloth.coerceIn(0, 100_000).toLong(),
                    "matElectronics" to seedStash.electronicParts.coerceIn(0, 100_000).toLong(),
                    "matHerbs" to seedStash.medicalHerbs.coerceIn(0, 100_000).toLong(),
                    "ammoM4" to seedStash.ammoM4.coerceIn(0, 10_000).toLong(),
                    "ammoSmg" to seedStash.ammoSmg.coerceIn(0, 10_000).toLong(),
                    "ammoShotgun" to seedStash.ammoShotgun.coerceIn(0, 10_000).toLong(),
                    "maxPantsLevel" to seedStash.maxPantsLevel.coerceIn(1, 4).toLong(),
                    "unlockedWeaponKeys" to weapons,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                val created = docRef.get().await().toObject(
                    UserCloudStash::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: UserCloudStash(userId = userId, callsign = safeCallsign)
                Result.success(created)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveUserStash(userId: String, callsign: String, stash: BaseStashState): Result<Unit> {
        val uid = requireUserId()
        require(uid == userId) { "Cannot update another operator's stash." }
        val docRef = db.collection("users").document(userId)
        val safeCallsign = callsign.trim().ifBlank { "SURVIVOR" }.take(24)
        val weapons = stash.unlockedWeaponKeys.ifEmpty { setOf("baton") }.take(10)
        val updates = mapOf(
            "callsign" to safeCallsign,
            "credits" to stash.credits.coerceIn(0, 100_000_000).toLong(),
            "matScrap" to stash.scrapMetal.coerceIn(0, 100_000).toLong(),
            "matGunpowder" to stash.gunpowder.coerceIn(0, 100_000).toLong(),
            "matCloth" to stash.tacticalCloth.coerceIn(0, 100_000).toLong(),
            "matElectronics" to stash.electronicParts.coerceIn(0, 100_000).toLong(),
            "matHerbs" to stash.medicalHerbs.coerceIn(0, 100_000).toLong(),
            "ammoM4" to stash.ammoM4.coerceIn(0, 10_000).toLong(),
            "ammoSmg" to stash.ammoSmg.coerceIn(0, 10_000).toLong(),
            "ammoShotgun" to stash.ammoShotgun.coerceIn(0, 10_000).toLong(),
            "maxPantsLevel" to stash.maxPantsLevel.coerceIn(1, 4).toLong(),
            "unlockedWeaponKeys" to weapons,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeActiveListings(): Flow<List<MarketListing>> {
        val query = db.collection("market_listings")
            .whereEqualTo("status", "ACTIVE")
        return query.snapshots()
            .map { snapshot ->
                snapshot.toObjects(MarketListing::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, "market_listings")
                }
                throw error
            }
    }

    fun observeMyListings(userId: String): Flow<List<MarketListing>> {
        val query = db.collection("market_listings")
            .whereEqualTo("sellerId", userId)
        return query.snapshots()
            .map { snapshot ->
                snapshot.toObjects(MarketListing::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, "market_listings")
                }
                throw error
            }
    }

    suspend fun createListing(
        sellerCallsign: String,
        category: String,
        itemName: String,
        itemDetail: String,
        rarityLabel: String,
        weaponKey: String = "",
        materialKey: String = "",
        cdId: Int = 0,
        pantsLevel: Int = 0,
        value: Int = 1,
        quantity: Int = 1,
        priceCredits: Int
    ): Result<String> {
        val uid = requireUserId()
        val listingId = UUID.randomUUID().toString()
        val docRef = db.collection("market_listings").document(listingId)
        val payload = mapOf(
            "sellerId" to uid,
            "sellerCallsign" to sellerCallsign.trim().ifBlank { "SURVIVOR" }.take(24),
            "buyerId" to "",
            "category" to category,
            "itemName" to itemName.trim().ifBlank { "Gear Item" }.take(60),
            "itemDetail" to itemDetail.trim().take(80),
            "rarityLabel" to rarityLabel.trim().ifBlank { "COMMON" }.take(40),
            "weaponKey" to weaponKey.take(24),
            "materialKey" to materialKey.take(24),
            "cdId" to cdId.coerceIn(0, 150).toLong(),
            "pantsLevel" to pantsLevel.coerceIn(0, 4).toLong(),
            "value" to value.coerceIn(0, 100_000).toLong(),
            "quantity" to quantity.coerceIn(1, 500).toLong(),
            "priceCredits" to priceCredits.coerceIn(1, 1_000_000).toLong(),
            "status" to "ACTIVE",
            "payoutClaimed" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(listingId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun purchaseListing(listingId: String): Result<MarketPurchaseOutcome> {
        val buyerUid = requireUserId()
        val listingRef = db.collection("market_listings").document(listingId)
        val buyerRef = db.collection("users").document(buyerUid)

        return try {
            val outcome = db.runTransaction { tx ->
                val listingSnap = tx.get(listingRef)
                val buyerSnap = tx.get(buyerRef)

                val listing = listingSnap.toObject(
                    MarketListing::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: throw IllegalStateException("Listing no longer exists.")

                if (listing.status != "ACTIVE") {
                    throw IllegalStateException("Listing is no longer active.")
                }
                if (listing.sellerId == buyerUid) {
                    throw IllegalStateException("Cannot buy your own listing.")
                }
                if (!buyerSnap.exists()) {
                    throw IllegalStateException("Buyer stash profile not initialized.")
                }

                val currentCredits = buyerSnap.getLong("credits") ?: 0L
                val price = listing.priceCredits
                if (currentCredits < price) {
                    throw IllegalStateException("Insufficient credits: need $price CRED.")
                }
                val newCredits = (currentCredits - price).coerceAtLeast(0L)

                val buyerUpdates = mutableMapOf<String, Any>(
                    "credits" to newCredits,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                when (listing.category) {
                    "WEAPON" -> {
                        val rawKeys = (buyerSnap.get("unlockedWeaponKeys") as? List<*>)
                            ?.filterIsInstance<String>()
                            ?: listOf("baton")
                        if (listing.weaponKey.isNotBlank() && listing.weaponKey !in rawKeys) {
                            buyerUpdates["unlockedWeaponKeys"] = (rawKeys + listing.weaponKey).take(10)
                        }
                    }
                    "PANTS" -> {
                        val curMax = (buyerSnap.getLong("maxPantsLevel") ?: 1L).toInt()
                        val boughtLvl = listing.pantsLevel.toInt().coerceIn(1, 4)
                        if (boughtLvl > curMax) {
                            buyerUpdates["maxPantsLevel"] = boughtLvl.toLong()
                        }
                    }
                    "MATERIAL" -> {
                        val field = listing.materialFieldKey()
                        if (field != null) {
                            val existingQty = buyerSnap.getLong(field) ?: 0L
                            buyerUpdates[field] = (existingQty + listing.quantity).coerceAtMost(100_000L)
                        }
                    }
                }

                tx.update(buyerRef, buyerUpdates)
                tx.update(
                    listingRef,
                    mapOf(
                        "status" to "SOLD",
                        "buyerId" to buyerUid,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                MarketPurchaseOutcome(
                    listing = listing.copy(status = "SOLD", buyerId = buyerUid),
                    updatedBuyerCredits = newCredits.toInt()
                )
            }.await()
            Result.success(outcome)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, listingRef.path)
            Result.failure(e)
        }
    }

    suspend fun cancelListing(listingId: String): Result<Unit> {
        requireUserId()
        val listingRef = db.collection("market_listings").document(listingId)
        return try {
            listingRef.update(
                mapOf(
                    "status" to "CANCELLED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, listingRef.path)
            Result.failure(e)
        }
    }

    suspend fun claimSalePayout(listingId: String): Result<Int> {
        val sellerUid = requireUserId()
        val listingRef = db.collection("market_listings").document(listingId)
        val sellerRef = db.collection("users").document(sellerUid)

        return try {
            val payout = db.runTransaction { tx ->
                val listingSnap = tx.get(listingRef)
                val sellerSnap = tx.get(sellerRef)

                val listing = listingSnap.toObject(
                    MarketListing::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: throw IllegalStateException("Listing not found.")

                if (listing.sellerId != sellerUid) {
                    throw IllegalStateException("Only the seller can claim this payout.")
                }
                if (listing.status != "SOLD" || listing.payoutClaimed) {
                    throw IllegalStateException("Payout is not available to claim.")
                }
                if (!sellerSnap.exists()) {
                    throw IllegalStateException("Seller stash not initialized.")
                }

                val currentCredits = sellerSnap.getLong("credits") ?: 0L
                val newCredits = (currentCredits + listing.priceCredits).coerceAtMost(100_000_000L)

                tx.update(
                    sellerRef,
                    mapOf(
                        "credits" to newCredits,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.update(
                    listingRef,
                    mapOf(
                        "payoutClaimed" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                listing.priceCredits.toInt()
            }.await()
            Result.success(payout)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, listingRef.path)
            Result.failure(e)
        }
    }

    fun observeLiveKillFeed(maxItems: Int = 12): Flow<List<KillFeedEvent>> {
        val query = db.collection("kill_feed")
            .whereEqualTo("feedScope", "PUBLIC")
        return query.snapshots()
            .map { snapshot ->
                snapshot.toObjects(KillFeedEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    .take(maxItems.coerceIn(1, 30))
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, "kill_feed")
                }
                throw error
            }
    }

    suspend fun publishKillFeedEvent(
        killerCallsign: String,
        victimId: String,
        victimCallsign: String,
        weaponKey: String,
        weaponName: String,
        zoneName: String
    ): Result<String> {
        val uid = requireUserId()
        val safeVictimId = victimId.trim().ifBlank { "rival_player" }.take(128)
        if (safeVictimId == uid) {
            return Result.failure(IllegalArgumentException("Self-elimination cannot be published to PvP feed."))
        }
        val eventId = "kf_${UUID.randomUUID()}"
        val docRef = db.collection("kill_feed").document(eventId)
        val payload = mapOf(
            "killerId" to uid,
            "killerCallsign" to killerCallsign.trim().ifBlank { "SURVIVOR" }.take(24),
            "victimId" to safeVictimId,
            "victimCallsign" to victimCallsign.trim().ifBlank { "RIVAL" }.take(24),
            "weaponKey" to weaponKey.trim().ifBlank { "knife" }.take(40),
            "weaponName" to weaponName.trim().ifBlank { "Combat Knife" }.take(48),
            "zoneName" to zoneName.trim().take(64),
            "feedScope" to "PUBLIC",
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(eventId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
