const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_user_123";
const BOB_UID = "bob_user_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user cannot read or write user profiles or projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .get()
  );
});

test("Authenticated user can create and read their own valid UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      publicUid: "CF00000001",
      username: "alice_creator",
      displayName: "Alice Creator",
      email: "alice@example.com",
      planTier: "FREE",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user cannot spoof publicUid format or paid planTier", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  // Invalid publicUid (not CF + 8 digits)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      publicUid: "INVALID123",
      username: "alice_creator",
      planTier: "FREE",
      createdAt: now,
      updatedAt: now,
    })
  );
  // Spoofed PRO planTier while payments are disabled
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      publicUid: "CF00000001",
      username: "alice_creator",
      planTier: "PRO",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice's profile or projects", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      publicUid: "CF00000001",
      username: "alice_creator",
      planTier: "FREE",
      createdAt: now,
      updatedAt: now,
    });
    await adminDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .set({
        userId: ALICE_UID,
        projectId: "proj_1",
        name: "My Documentary",
        aspectRatio: "16:9",
        durationMicros: 15000000,
        frameRateNum: 30,
        frameRateDen: 1,
        trackCount: 2,
        clipCount: 4,
        missingMediaCount: 0,
        schemaVersion: 1,
        createdAt: now,
        updatedAt: now,
      });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );
});

test("Shadow Update Test: rejects ghost fields on project metadata update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .set({
        userId: ALICE_UID,
        projectId: "proj_1",
        name: "Initial Cut",
        aspectRatio: "16:9",
        durationMicros: 5000000,
        frameRateNum: 60,
        frameRateDen: 1,
        trackCount: 1,
        clipCount: 1,
        missingMediaCount: 0,
        schemaVersion: 1,
        createdAt: now,
        updatedAt: now,
      })
  );

  // Valid update succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .update({
        name: "Final Cut",
        durationMicros: 12000000,
        updatedAt: new Date(),
      })
  );

  // Ghost field update fails
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .update({
        name: "Hacked Cut",
        isVerifiedAdmin: true,
        updatedAt: new Date(),
      })
  );
});
