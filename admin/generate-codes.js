const admin = require('firebase-admin');

const SERVICE_ACCOUNT_PATH = './service-account.json';

function generateCode() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let code = '';
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

async function generateCodes(count) {
  if (!admin.apps.length) {
    try {
      const serviceAccount = require(SERVICE_ACCOUNT_PATH);
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
    } catch (err) {
      console.error('ERROR: Could not load service-account.json');
      console.error('Please download your service account key from:');
      console.error('  Firebase Console → Project Settings → Service Accounts → Generate New Private Key');
      console.error('  Save it as: admin/service-account.json');
      process.exit(1);
    }
  }

  const db = admin.firestore();
  const batch = db.batch();
  const codes = [];

  for (let i = 0; i < count; i++) {
    const code = generateCode();
    const docRef = db.collection('activations').doc();
    batch.set(docRef, {
      code: code,
      used: false,
      usedAt: null,
      activatedDeviceId: '',
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });
    codes.push(code);
  }

  await batch.commit();
  console.log(`\n✓ Successfully generated ${count} activation codes\n`);

  const codesPerLine = 10;
  for (let i = 0; i < codes.length; i += codesPerLine) {
    console.log(codes.slice(i, i + codesPerLine).join(', '));
  }
  console.log('\nTotal codes: ' + codes.length);
  console.log('\nThese codes can now be used to activate ClientFlow Pro.');
  console.log('Each code works for one device only.\n');
}

const args = process.argv.slice(2);
const count = parseInt(args[0]) || 10;

if (count > 0 && count <= 1000) {
  generateCodes(count).catch(console.error);
} else {
  console.log('Usage: node generate-codes.js [count]');
  console.log('  count: number of codes to generate (1-1000, default: 10)');
  process.exit(1);
}