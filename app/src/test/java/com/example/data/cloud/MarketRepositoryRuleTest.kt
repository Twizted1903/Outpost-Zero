package com.example.data.cloud

import com.example.base.FirestoreEmulatorTestBase
import com.example.game.model.BaseStashState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MarketRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserStash_authenticatedOwner_createsAndObservesStash() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = MarketRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserStash(aliceUid, "WARDEN", BaseStashState(credits = 500, scrapMetal = 6))
        }
        assertTrue(result.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserStash(aliceUid).first { it != null }
        }
        assertNotNull(observed)
        assertEquals(aliceUid, observed!!.userId)
        assertTrue(observed.credits >= 0L)
    }

    @Test
    fun crossUserStashAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = MarketRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserStash(aliceUid, "WARDEN", BaseStashState(credits = 500))
        }

        signInTestUser(BOB_EMAIL)
        var deniedCode: FirebaseFirestoreException.Code? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
        } catch (e: Exception) {
            deniedCode = e.unwrapFirestoreException()?.code
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, deniedCode)
    }

    @Test
    fun createAndPurchaseMarketListing_atomicTransaction_transfersItemAndAllowsPayout() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = MarketRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserStash(aliceUid, "ALICE", BaseStashState(credits = 400))
        }

        val itemTitle = "M4 Carbine ${UUID.randomUUID().toString().take(6)}"
        val listingResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createListing(
                sellerCallsign = "ALICE",
                category = "WEAPON",
                itemName = itemTitle,
                itemDetail = "5.56mm Precision Carbine",
                rarityLabel = "RARE // MIL-SPEC",
                weaponKey = "m4",
                priceCredits = 120
            )
        }
        assertTrue(listingResult.isSuccess)
        val listingId = listingResult.getOrThrow()

        // Bob observes ACTIVE listings and buys Alice's listing
        val bobUid = signInTestUser(BOB_EMAIL)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserStash(bobUid, "BOB", BaseStashState(credits = 600))
            // Guarantee Bob has enough credits even if emulator persisted earlier test run
            repo.saveUserStash(bobUid, "BOB", BaseStashState(credits = 600))
        }

        val activeList = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeActiveListings().first { list -> list.any { it.id == listingId } }
        }
        assertTrue(activeList.any { it.id == listingId && it.status == "ACTIVE" })

        val purchaseResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.purchaseListing(listingId)
        }
        assertTrue(purchaseResult.isSuccess)
        assertEquals(480, purchaseResult.getOrThrow().updatedBuyerCredits)

        // Alice observes her sold listing and claims payout
        signInTestUser(ALICE_EMAIL)
        val mySold = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeMyListings(aliceUid).first { list ->
                list.any { it.id == listingId && it.status == "SOLD" }
            }
        }
        assertTrue(mySold.any { it.id == listingId })

        val payoutResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.claimSalePayout(listingId)
        }
        assertTrue(payoutResult.isSuccess)
        assertEquals(120, payoutResult.getOrThrow())
    }

    @Test
    fun cancelListing_sellerCancelsActiveListing_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = MarketRepository(firestore)
        val listingId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createListing(
                sellerCallsign = "ALICE",
                category = "MATERIAL",
                itemName = "Scrap Metal",
                itemDetail = "x5 Crafting Material",
                rarityLabel = "COMMON // MATERIAL",
                materialKey = "mat_scrap",
                quantity = 5,
                priceCredits = 75
            ).getOrThrow()
        }

        val cancelRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.cancelListing(listingId)
        }
        assertTrue(cancelRes.isSuccess)
    }

    @Test
    fun observeActiveListings_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = MarketRepository(firestore)

        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeActiveListings().first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val firestoreException = checkNotNull(caught?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $caught"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    @Test
    fun publishAndObserveKillFeed_authenticatedPlayersShareLiveFeed() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = MarketRepository(firestore)

        val victimTag = "VIPER-${UUID.randomUUID().toString().take(4)}"
        val pubResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.publishKillFeedEvent(
                killerCallsign = "WARDEN",
                victimId = "rival_viper",
                victimCallsign = victimTag,
                weaponKey = "m4",
                weaponName = "M4 Carbine",
                zoneName = "High School"
            )
        }
        assertTrue(pubResult.isSuccess)
        val eventId = pubResult.getOrThrow()

        signInTestUser(BOB_EMAIL)
        val feed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeLiveKillFeed(maxItems = 12).first { list -> list.any { it.id == eventId } }
        }
        val matched = feed.first { it.id == eventId }
        assertEquals(aliceUid, matched.killerId)
        assertEquals("WARDEN", matched.killerCallsign)
        assertEquals(victimTag, matched.victimCallsign)
        assertEquals("M4 Carbine", matched.weaponName)
    }

    @Test
    fun observeLiveKillFeed_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = MarketRepository(firestore)

        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeLiveKillFeed().first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val firestoreException = checkNotNull(caught?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $caught"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_oz@test.com"
        const val BOB_EMAIL = "bob_oz@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
