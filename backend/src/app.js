// API REST de Cuidado de Plantas: /register, /login, /logout, /tokens y CRUD de /plants.
// Los datos viven en un archivo JSON (db.json) y cada usuario solo ve sus plantas.
const fs = require('node:fs');
const crypto = require('node:crypto');
const http = require('node:http');
const { hashPassword, verifyPassword, signToken, verifyToken } = require('./auth');
const { validateRegister, validateLogin, validatePlant } = require('./validate');

const MAX_BODY_BYTES = 100_000;

const httpError = (status, message, errors) => Object.assign(new Error(message), { status, errors });

function send(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(payload),
  });
  res.end(payload);
}

async function readJson(req) {
  const chunks = [];
  let size = 0;
  for await (const chunk of req) {
    size += chunk.length;
    if (size > MAX_BODY_BYTES) throw httpError(413, 'El cuerpo de la petición es demasiado grande');
    chunks.push(chunk);
  }
  if (size === 0) return {};
  try {
    return JSON.parse(Buffer.concat(chunks).toString('utf8'));
  } catch {
    throw httpError(400, 'El cuerpo no es un JSON válido');
  }
}

function createServer({ dbPath, secret, ttlSeconds = 24 * 3600, log = false }) {
  const db = JSON.parse(fs.readFileSync(dbPath, 'utf8'));
  db.users ??= [];
  db.plants ??= [];
  db.tokens ??= []; // historial de tokens emitidos (uno por login/registro)

  const save = () => {
    const tmp = `${dbPath}.tmp`;
    fs.writeFileSync(tmp, JSON.stringify(db, null, 2));
    fs.renameSync(tmp, dbPath);
  };
  const nextId = (list) => list.reduce((max, item) => Math.max(max, item.id), 0) + 1;
  const publicUser = ({ id, name, email }) => ({ id, name, email });
  const publicPlant = ({ userId, ...plant }) => plant;
  const clientIp = (req) => (req.headers['x-forwarded-for'] || req.socket.remoteAddress || '').split(',')[0].trim();

  // Cada token lleva un jti y se registra en el historial; así se puede listar y revocar.
  function sessionFor(user, req) {
    const jti = crypto.randomUUID();
    const accessToken = signToken({ sub: String(user.id), email: user.email, jti }, secret, ttlSeconds);
    const { iat, exp } = verifyToken(accessToken, secret);
    db.tokens.push({
      jti,
      userId: user.id,
      issuedAt: new Date(iat * 1000).toISOString(),
      expiresAt: new Date(exp * 1000).toISOString(),
      ip: clientIp(req),
      userAgent: String(req.headers['user-agent'] || '').slice(0, 200),
      revokedAt: null,
    });
    save();
    return { accessToken, user: publicUser(user) };
  }
  const tokenStatus = (t) => (t.revokedAt ? 'revoked' : Date.parse(t.expiresAt) < Date.now() ? 'expired' : 'active');

  function authenticate(req) {
    const [scheme, token] = (req.headers.authorization || '').split(' ');
    const payload = scheme === 'Bearer' && token ? verifyToken(token, secret) : null;
    const user = payload && db.users.find((u) => u.id === Number(payload.sub));
    const record = payload && db.tokens.find((t) => t.jti === payload.jti && t.userId === user?.id);
    if (!user || !record || record.revokedAt) throw httpError(401, 'Sesión no válida o expirada');
    return { user, record };
  }

  async function register(req, res) {
    const { errors, value } = validateRegister(await readJson(req));
    if (!value) throw httpError(400, 'Datos inválidos', errors);
    if (db.users.some((u) => u.email === value.email)) throw httpError(409, 'Ese correo ya está registrado');
    const user = {
      id: nextId(db.users),
      name: value.name,
      email: value.email,
      passwordHash: hashPassword(value.password),
    };
    db.users.push(user);
    save();
    send(res, 201, sessionFor(user, req));
  }

  async function login(req, res) {
    const { errors, value } = validateLogin(await readJson(req));
    if (!value) throw httpError(400, 'Datos inválidos', errors);
    const user = db.users.find((u) => u.email === value.email);
    if (!user || !verifyPassword(value.password, user.passwordHash)) {
      throw httpError(401, 'Correo o contraseña incorrectos');
    }
    send(res, 200, sessionFor(user, req));
  }

  async function plants(req, res, rawId) {
    const { user } = authenticate(req);
    const mine = db.plants.filter((p) => p.userId === user.id);

    if (rawId === undefined) {
      if (req.method === 'GET') return send(res, 200, mine.sort((a, b) => a.id - b.id).map(publicPlant));
      if (req.method === 'POST') {
        const { errors, value } = validatePlant(await readJson(req));
        if (!value) throw httpError(400, 'Datos inválidos', errors);
        const plant = { id: nextId(db.plants), userId: user.id, ...value };
        db.plants.push(plant);
        save();
        return send(res, 201, publicPlant(plant));
      }
      throw httpError(405, 'Método no permitido');
    }

    const id = Number(rawId);
    const plant = Number.isInteger(id) ? mine.find((p) => p.id === id) : undefined;
    if (!plant) throw httpError(404, 'Planta no encontrada');

    if (req.method === 'GET') return send(res, 200, publicPlant(plant));
    if (req.method === 'PUT') {
      const { errors, value } = validatePlant(await readJson(req));
      if (!value) throw httpError(400, 'Datos inválidos', errors);
      Object.assign(plant, value);
      save();
      return send(res, 200, publicPlant(plant));
    }
    if (req.method === 'DELETE') {
      db.plants = db.plants.filter((p) => p !== plant);
      save();
      return send(res, 200, {});
    }
    throw httpError(405, 'Método no permitido');
  }

  async function logout(req, res) {
    const { record } = authenticate(req);
    record.revokedAt = new Date().toISOString();
    save();
    send(res, 200, {});
  }

  async function tokens(req, res) {
    const { user, record } = authenticate(req);
    const history = db.tokens
      .filter((t) => t.userId === user.id)
      .sort((a, b) => b.issuedAt.localeCompare(a.issuedAt))
      .map((t) => ({
        id: t.jti,
        issuedAt: t.issuedAt,
        expiresAt: t.expiresAt,
        revokedAt: t.revokedAt,
        status: tokenStatus(t),
        current: t.jti === record.jti,
        ip: t.ip,
        userAgent: t.userAgent,
      }));
    send(res, 200, history);
  }

  async function route(req, res) {
    const { pathname } = new URL(req.url, 'http://localhost');
    const parts = pathname.split('/').filter(Boolean);
    const [resource, rawId] = parts;

    if ((resource === 'register' || resource === 'login') && parts.length === 1) {
      if (req.method !== 'POST') throw httpError(405, 'Método no permitido');
      return resource === 'register' ? register(req, res) : login(req, res);
    }
    if ((resource === 'logout' || resource === 'tokens') && parts.length === 1) {
      const expected = resource === 'logout' ? 'POST' : 'GET';
      if (req.method !== expected) throw httpError(405, 'Método no permitido');
      return resource === 'logout' ? logout(req, res) : tokens(req, res);
    }
    if (resource === 'plants' && parts.length <= 2) return plants(req, res, rawId);
    throw httpError(404, 'Ruta no encontrada');
  }

  return http.createServer(async (req, res) => {
    const started = Date.now();
    try {
      await route(req, res);
    } catch (error) {
      if (error.status) {
        send(res, error.status, { message: error.message, ...(error.errors && { errors: error.errors }) });
      } else {
        console.error(error);
        send(res, 500, { message: 'Error interno del servidor' });
      }
    }
    if (log) console.log(`${req.method} ${req.url} -> ${res.statusCode} (${Date.now() - started} ms)`);
  });
}

module.exports = { createServer };
