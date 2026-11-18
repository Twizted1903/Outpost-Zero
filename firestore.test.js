const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const { serverTimestamp } = require("/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules/firebase/firestore");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserStash(uid) {
  return {
    userId: uid,
    callsign: "WARDEN",
    credits: 350,
    matScrap: 5,
    matGunpowder: 2,
    matCloth: 4,
    matElectronics: 1,
    matHerbs: 3,
    ammoM4: 0,
    ammoSmg: 0,
    ammoShotgun: 0,
    maxPantsLevel: 1,
    unlockedWeaponKeys: ["baton"],
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validMarketListing(sellerId) {
  return {
    sellerId,
    sellerCallsign: "WARDEN",
    buyerId: "",
    category: "WEAPON",
    itemName: "M4 Carbine",
    itemDetail: "5.56mm Crafted Rifle",
    rarityLabel: "UNCOMMON // SERVICE",
    weaponKey: "m4",
    materialKey: "",
    cdId: 0,
    pantsLevel: 0,
    value: 1,
    quantity: 1,
    priceCredits: 420,
    status: "ACTIVE",
    payoutClaimed: false,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read users or market listings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("market_listings").where("status", "==", "ACTIVE").get());
});

test("UserStash: owner can create and update, cross-user and ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validUserStash(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());

  // Update credits and scrap
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      credits: 500,
      matScrap: 10,
      updatedAt: serverTimestamp(),
    })
  );

  // Shadow update with ghost field must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      credits: 9999,
      isAdmin: true,
      updatedAt: serverTimestamp(),
    })
  );
});

test("MarketListing: unfiltered list fails, status==ACTIVE query succeeds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("market_listings").doc("listing_1").set(validMarketListing(ALICE_UID))
  );

  // Seed a SOLD listing owned by Alice (Bob is NOT buyer or seller)
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const sold = validMarketListing(ALICE_UID);
    sold.status = "SOLD";
    sold.buyerId = "someone_else";
    await ctx.firestore().collection("market_listings").doc("listing_sold_other").set(sold);
  });

  // Unfiltered list violates query enforcer when non-active doc exists
  await assertFails(bobDb.collection("market_listings").get());

  // Filtered ACTIVE query succeeds
  await assertSucceeds(
    bobDb.collection("market_listings").where("status", "==", "ACTIVE").get()
  );
});

test("MarketListing: buy, claim payout, and terminal lock work securely", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const listingRefAlice = aliceDb.collection("market_listings").doc("listing_buy_1");
  const listingRefBob = bobDb.collection("market_listings").doc("listing_buy_1");

  await assertSucceeds(listingRefAlice.set(validMarketListing(ALICE_UID)));

  // Seller cannot buy own listing
  await assertFails(
    listingRefAlice.update({
      status: "SOLD",
      buyerId: ALICE_UID,
      updatedAt: serverTimestamp(),
    })
  );

  // Buyer cannot alter priceCredits while buying
  await assertFails(
    listingRefBob.update({
      status: "SOLD",
      buyerId: BOB_UID,
      priceCredits: 1,
      updatedAt: serverTimestamp(),
    })
  );

  // Valid purchase by Bob
  await assertSucceeds(
    listingRefBob.update({
      status: "SOLD",
      buyerId: BOB_UID,
      updatedAt: serverTimestamp(),
    })
  );

  // Bob cannot claim payout on Alice's listing
  await assertFails(
    listingRefBob.update({
      payoutClaimed: true,
      updatedAt: serverTimestamp(),
    })
  );

  // Alice claims payout
  await assertSucceeds(
    listingRefAlice.update({
      payoutClaimed: true,
      updatedAt: serverTimestamp(),
    })
  );

  // Terminal state lock: no further updates allowed after SOLD + payoutClaimed
  await assertFails(
    listingRefAlice.update({
      status: "CANCELLED",
      updatedAt: serverTimestamp(),
    })
  );
});

test("MarketListing: seller cancel locks listing and blocks buyer", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const listingRefAlice = aliceDb.collection("market_listings").doc("listing_cancel_1");
  const listingRefBob = bobDb.collection("market_listings").doc("listing_cancel_1");

  await assertSucceeds(listingRefAlice.set(validMarketListing(ALICE_UID)));

  // Buyer cannot cancel Alice's listing
  await assertFails(
    listingRefBob.update({
      status: "CANCELLED",
      updatedAt: serverTimestamp(),
    })
  );

  // Seller cancels listing
  await assertSucceeds(
    listingRefAlice.update({
      status: "CANCELLED",
      updatedAt: serverTimestamp(),
    })
  );

  // Buyer cannot buy a cancelled listing
  await assertFails(
    listingRefBob.update({
      status: "SOLD",
      buyerId: BOB_UID,
      updatedAt: serverTimestamp(),
    })
  );
});

function validFieldNote(uid) {
  return {
    userId: uid,
    title: "Precinct Armory Check",
    body: "Gunpowder & Primers drop on 2F cruiser lockers.",
    category: "LOOT",
    locationStamp: "Oakridge • District 4 Precinct • 2F",
    pinned: true,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

test("FieldNotes: owner can create/read/update/delete, unauth and cross-user denied", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const aliceNotesCol = aliceDb.collection("users").doc(ALICE_UID).collection("notes");
  const noteId = "note_1";

  // Unauthenticated cannot read or write notes
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());

  // Owner creates valid note
  await assertSucceeds(aliceNotesCol.doc(noteId).set(validFieldNote(ALICE_UID)));

  // Owner lists and updates note
  await assertSucceeds(aliceNotesCol.get());
  await assertSucceeds(
    aliceNotesCol.doc(noteId).update({
      body: "Updated: Also check Apex Mart back room for CDs.",
      pinned: false,
      updatedAt: serverTimestamp(),
    })
  );

  // Cross-user read/write/delete must fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").doc(noteId).delete());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").doc("bob_spoof").set(validFieldNote(BOB_UID)));

  // Empty body or invalid category fails schema check
  await assertFails(
    aliceNotesCol.doc("bad_note").set({
      ...validFieldNote(ALICE_UID),
      body: "",
    })
  );

  // Owner deletes note cleanly
  await assertSucceeds(aliceNotesCol.doc(noteId).delete());
});

function validKillFeedEvent(killerId, victimId = "rival_bot_1") {
  return {
    killerId,
    killerCallsign: "WARDEN",
    victimId,
    victimCallsign: "VIPER-4",
    weaponKey: "m4",
    weaponName: "M4 Carbine",
    zoneName: "OAKRIDGE STREETS",
    feedScope: "PUBLIC",
    createdAt: serverTimestamp(),
  };
}

test("KillFeed: unauthenticated rejected, signed-in creator publishes & both players observe", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Unauthenticated cannot read or publish kill feed events
  await assertFails(unauthDb.collection("kill_feed").where("feedScope", "==", "PUBLIC").get());
  await assertFails(unauthDb.collection("kill_feed").doc("kf_unauth").set(validKillFeedEvent(ALICE_UID)));

  // Identity spoofing or self-kill rejected
  await assertFails(bobDb.collection("kill_feed").doc("kf_spoof").set(validKillFeedEvent(ALICE_UID)));
  await assertFails(aliceDb.collection("kill_feed").doc("kf_self").set(validKillFeedEvent(ALICE_UID, ALICE_UID)));

  // Extra ghost field rejected
  await assertFails(
    aliceDb.collection("kill_feed").doc("kf_ghost").set({
      ...validKillFeedEvent(ALICE_UID),
      xpBonus: 999,
    })
  );

  // Valid PvP kill event succeeds and Bob can observe live PUBLIC feed
  await assertSucceeds(aliceDb.collection("kill_feed").doc("kf_1").set(validKillFeedEvent(ALICE_UID, BOB_UID)));
  await assertSucceeds(bobDb.collection("kill_feed").where("feedScope", "==", "PUBLIC").get());

  // Immutable: cannot update or delete kill feed events
  await assertFails(aliceDb.collection("kill_feed").doc("kf_1").update({ weaponName: "Knife" }));
  await assertFails(aliceDb.collection("kill_feed").doc("kf_1").delete());
});
