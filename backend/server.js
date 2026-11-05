// Arranque del backend local:  node server.js   (o  npm start)
//   PORT             puerto (por defecto 3000)
//   HOST             interfaz (por defecto 127.0.0.1; el emulador llega con 10.0.2.2.
//                    Usa 0.0.0.0 solo si pruebas con un teléfono físico en tu red)
//   JWT_SECRET       secreto para firmar tokens
//   TOKEN_TTL_HOURS  vigencia del token en horas (por defecto 24)
const fs = require('node:fs');
const path = require('node:path');
const { createServer } = require('./src/app');

const dbPath = path.join(__dirname, 'db.json');
const seedPath = path.join(__dirname, 'db.seed.json');

if (process.argv.includes('--reset') || !fs.existsSync(dbPath)) {
  fs.copyFileSync(seedPath, dbPath);
  console.log('Base de datos restaurada desde db.seed.json');
}

const port = Number(process.env.PORT) || 3000;
const host = process.env.HOST || '127.0.0.1';
const secret = process.env.JWT_SECRET || 'secreto-de-desarrollo-solo-para-clase';
const ttlSeconds = (Number(process.env.TOKEN_TTL_HOURS) || 24) * 3600;

createServer({ dbPath, secret, ttlSeconds, log: true }).listen(port, host, () => {
  console.log(`API de plantas en http://${host}:${port}  (emulador Android: http://10.0.2.2:${port}/)`);
});
