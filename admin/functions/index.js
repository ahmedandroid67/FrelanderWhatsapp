const admin = require("firebase-admin");
const { onCall, HttpsError } = require("firebase-functions/v2/https");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const ACTIVATIONS_COLLECTION = "activations";

exports.activateProCode = onCall(
  {
    region: "us-central1",
    cors: true,
  },
  async (request) => {
    const rawCode = `${request.data?.code ?? ""}`;
    const rawDeviceId = `${request.data?.deviceId ?? ""}`;
    const code = rawCode.trim().replace(/[-\s]/g, "").toUpperCase();
    const deviceId = rawDeviceId.trim().toUpperCase();

    if (!/^[A-Z2-9]{6}$/.test(code)) {
      throw new HttpsError("invalid-argument", "Activation code format is invalid");
    }

    if (!/^CF-[A-Z0-9]{12}$/.test(deviceId)) {
      throw new HttpsError("invalid-argument", "Device ID format is invalid");
    }

    const collection = db.collection(ACTIVATIONS_COLLECTION);
    let doc = null;

    const directDoc = await collection.doc(code).get();
    if (directDoc.exists) {
      doc = directDoc;
    } else {
      const snapshot = await collection
        .where("code", "==", code)
        .limit(1)
        .get();

      if (!snapshot.empty) {
        doc = snapshot.docs[0];
      }
    }

    if (!doc) {
      throw new HttpsError("not-found", "Activation code not found");
    }

    const data = doc.data();
    const activatedDeviceId = `${data.activatedDeviceId || data.deviceId || ""}`.trim().toUpperCase();
    const isUsed = data.used === true;

    if (isUsed && activatedDeviceId && activatedDeviceId !== deviceId) {
      throw new HttpsError("already-exists", "Code already used by another device");
    }

    await doc.ref.set(
      {
        used: true,
        usedAt: admin.firestore.FieldValue.serverTimestamp(),
        activatedDeviceId: deviceId,
        deviceId: deviceId,
      },
      { merge: true }
    );

    return {
      success: true,
      code,
      deviceId,
    };
  }
);
