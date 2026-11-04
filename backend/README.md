# Backend local (json-server + json-server-auth)

```bash
npm install -g json-server@0.17.4 json-server-auth
json-server-auth backend/db.json --routes backend/routes.json --port 3000
```

- El emulador accede con `http://10.0.2.2:3000/`.
- `POST /register` y `POST /login` devuelven `accessToken` y `user`.
- `plants` tiene permiso 660: hay que enviar `Authorization: Bearer <token>` para leer y escribir.
