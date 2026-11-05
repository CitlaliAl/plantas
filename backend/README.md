# Backend local · API de Cuidado de Plantas

API REST en Node (sin dependencias: no hace falta `npm install`). Requiere **Node 20 o superior**.

```bash
cd backend
npm start            # o: node server.js
npm run start:fresh  # restaura los datos de db.seed.json y arranca
npm test             # pruebas automáticas de la API
```

- Emulador Android: `http://10.0.2.2:3000/`
- Teléfono físico en la misma red: `HOST=0.0.0.0 npm start` y usa la IP de tu computadora.
- Usuario de demostración: `demo@plantas.com` / `plantas123` (trae 5 plantas). Un usuario recién registrado empieza con la lista vacía.
- `db.json` se crea al arrancar a partir de `db.seed.json` y no se sube a Git.

Variables opcionales: `PORT` (3000), `HOST` (127.0.0.1), `JWT_SECRET`, `TOKEN_TTL_HOURS` (24).

El contrato completo (endpoints, validaciones y códigos de estado) está en [`docs/API.md`](../docs/API.md).
