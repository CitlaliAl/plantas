const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { createServer } = require('../src/app');
const { signToken } = require('../src/auth');

const SECRET = 'secreto-de-prueba';
const DEMO = { email: 'demo@plantas.com', password: 'plantas123' };
const validPlant = {
  name: 'Ficus',
  species: 'Ficus benjamina',
  wateringIntervalDays: 5,
  plantingDate: '2026-01-15',
  lastWateredDate: '2026-02-01',
  needsSun: true,
};

let server;
let base;
let tmpDir;

test.before(async () => {
  tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'plantas-api-'));
  const dbPath = path.join(tmpDir, 'db.json');
  fs.copyFileSync(path.join(__dirname, '..', 'db.seed.json'), dbPath);
  server = createServer({ dbPath, secret: SECRET });
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  base = `http://127.0.0.1:${server.address().port}`;
});

test.after(() => {
  server.closeAllConnections();
  server.close();
  fs.rmSync(tmpDir, { recursive: true, force: true });
});

async function api(method, route, { token, body, raw } = {}) {
  const res = await fetch(base + route, {
    method,
    headers: {
      ...(body !== undefined || raw !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: raw ?? (body !== undefined ? JSON.stringify(body) : undefined),
  });
  return { status: res.status, body: await res.json() };
}

async function newUserToken(email) {
  const { body } = await api('POST', '/register', {
    body: { name: 'Prueba', email, password: 'password123' },
  });
  return body.accessToken;
}

test('register: valida nombre, correo y contraseña', async () => {
  const empty = await api('POST', '/register', { body: {} });
  assert.equal(empty.status, 400);
  assert.deepEqual(Object.keys(empty.body.errors).sort(), ['email', 'name', 'password']);

  const short = await api('POST', '/register', { body: { name: 'Ana', email: 'ana@mail.com', password: '1234567' } });
  assert.equal(short.status, 400);
  assert.ok(short.body.errors.password);

  const badEmail = await api('POST', '/register', { body: { name: 'Ana', email: 'ana-mail', password: '12345678' } });
  assert.equal(badEmail.status, 400);
  assert.ok(badEmail.body.errors.email);
});

test('register: crea la cuenta, devuelve token y usuario sin contraseña, y rechaza duplicados', async () => {
  const created = await api('POST', '/register', {
    body: { name: 'Citlali', email: 'Citlali@Correo.com ', password: 'password123' },
  });
  assert.equal(created.status, 201);
  assert.equal(created.body.user.email, 'citlali@correo.com');
  assert.equal(created.body.user.name, 'Citlali');
  assert.equal(created.body.user.password, undefined);
  assert.equal(created.body.user.passwordHash, undefined);

  const parts = created.body.accessToken.split('.');
  assert.equal(parts.length, 3);
  const payload = JSON.parse(Buffer.from(parts[1], 'base64url').toString());
  assert.equal(payload.sub, String(created.body.user.id));
  assert.ok(payload.exp > payload.iat);

  const duplicate = await api('POST', '/register', {
    body: { name: 'Otra', email: 'citlali@correo.com', password: 'password123' },
  });
  assert.equal(duplicate.status, 409);
});

test('login: éxito, contraseña incorrecta, correo inexistente y campos vacíos', async () => {
  const ok = await api('POST', '/login', { body: DEMO });
  assert.equal(ok.status, 200);
  assert.ok(ok.body.accessToken);
  assert.equal(ok.body.user.email, DEMO.email);

  const wrongPassword = await api('POST', '/login', { body: { ...DEMO, password: 'incorrecta' } });
  assert.equal(wrongPassword.status, 401);

  const unknown = await api('POST', '/login', { body: { email: 'nadie@mail.com', password: 'password123' } });
  assert.equal(unknown.status, 401);
  assert.equal(unknown.body.message, wrongPassword.body.message);

  const empty = await api('POST', '/login', { body: {} });
  assert.equal(empty.status, 400);
});

test('plants: exige un token válido', async () => {
  assert.equal((await api('GET', '/plants')).status, 401);
  assert.equal((await api('GET', '/plants', { token: 'basura' })).status, 401);

  const forged = signToken({ sub: '1' }, 'otro-secreto', 3600);
  assert.equal((await api('GET', '/plants', { token: forged })).status, 401);

  const expired = signToken({ sub: '1' }, SECRET, -10);
  assert.equal((await api('GET', '/plants', { token: expired })).status, 401);
});

test('plants: el usuario demo ve sus 5 plantas y uno nuevo empieza con la lista vacía', async () => {
  const demo = (await api('POST', '/login', { body: DEMO })).body.accessToken;
  const list = await api('GET', '/plants', { token: demo });
  assert.equal(list.status, 200);
  assert.equal(list.body.length, 5);
  assert.equal(list.body[0].userId, undefined);
  assert.deepEqual(Object.keys(list.body[0]).sort(), [
    'id', 'lastWateredDate', 'name', 'needsSun', 'plantingDate', 'species', 'wateringIntervalDays',
  ]);

  const fresh = await newUserToken('vacio@mail.com');
  const empty = await api('GET', '/plants', { token: fresh });
  assert.equal(empty.status, 200);
  assert.deepEqual(empty.body, []);
});

test('plants: flujo completo crear, leer, editar y eliminar', async () => {
  const token = await newUserToken('crud@mail.com');

  const created = await api('POST', '/plants', { token, body: { ...validPlant, name: '  Ficus  ' } });
  assert.equal(created.status, 201);
  assert.equal(created.body.name, 'Ficus');
  const id = created.body.id;

  const one = await api('GET', `/plants/${id}`, { token });
  assert.equal(one.status, 200);
  assert.equal(one.body.species, 'Ficus benjamina');

  const updated = await api('PUT', `/plants/${id}`, {
    token,
    body: { ...validPlant, name: 'Ficus grande', wateringIntervalDays: 10 },
  });
  assert.equal(updated.status, 200);
  assert.equal(updated.body.name, 'Ficus grande');
  assert.equal(updated.body.wateringIntervalDays, 10);
  assert.equal((await api('GET', '/plants', { token })).body.length, 1);

  const removed = await api('DELETE', `/plants/${id}`, { token });
  assert.equal(removed.status, 200);
  assert.equal((await api('GET', `/plants/${id}`, { token })).status, 404);
  assert.deepEqual((await api('GET', '/plants', { token })).body, []);
});

test('plants: validaciones devuelven 400 con el campo que falló', async () => {
  const token = await newUserToken('valida@mail.com');
  const cases = [
    ['name', { name: 'A' }],
    ['species', { species: '   ' }],
    ['wateringIntervalDays', { wateringIntervalDays: 0 }],
    ['wateringIntervalDays', { wateringIntervalDays: 61 }],
    ['wateringIntervalDays', { wateringIntervalDays: 2.5 }],
    ['wateringIntervalDays', { wateringIntervalDays: '5' }],
    ['plantingDate', { plantingDate: '2999-01-01' }],
    ['plantingDate', { plantingDate: '15/01/2026' }],
    ['plantingDate', { plantingDate: '2026-02-30' }],
    ['lastWateredDate', { lastWateredDate: '2025-12-31' }],
    ['lastWateredDate', { lastWateredDate: '2999-01-01' }],
    ['needsSun', { needsSun: 'si' }],
  ];
  for (const [field, patch] of cases) {
    const res = await api('POST', '/plants', { token, body: { ...validPlant, ...patch } });
    assert.equal(res.status, 400, `${field} ${JSON.stringify(patch)}`);
    assert.ok(res.body.errors[field], `debe reportar ${field} para ${JSON.stringify(patch)}`);
  }
  assert.deepEqual((await api('GET', '/plants', { token })).body, [], 'ninguna petición inválida debe guardar datos');
});

test('plants: un usuario no puede ver, editar ni borrar plantas de otro', async () => {
  const token = await newUserToken('intruso@mail.com');
  assert.equal((await api('GET', '/plants/1', { token })).status, 404);
  assert.equal((await api('PUT', '/plants/1', { token, body: validPlant })).status, 404);
  assert.equal((await api('DELETE', '/plants/1', { token })).status, 404);

  const demo = (await api('POST', '/login', { body: DEMO })).body.accessToken;
  assert.equal((await api('GET', '/plants/1', { token: demo })).status, 200);
});

test('errores generales: ruta inexistente, método no permitido y JSON roto', async () => {
  assert.equal((await api('GET', '/nada')).status, 404);
  assert.equal((await api('GET', '/login')).status, 405);

  const broken = await api('POST', '/login', { raw: '{no es json' });
  assert.equal(broken.status, 400);
});
