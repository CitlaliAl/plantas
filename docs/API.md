# API de Cuidado de Plantas

Documentación del backend local del Proyecto Unidad 3 (App CRUD con autenticación).
Autora: Citlali Montserrat Alvarado Pérez · Grupo 4° A · Desarrollo de aplicaciones móviles

## 1. Resumen y alcance

La app Android consume una API REST por HTTP con Retrofit. La API permite registrar usuarios, iniciar sesión y gestionar el recurso principal del dominio: **Planta**. Cada usuario trabaja solo con sus propias plantas.

| Decisión | Definición |
|---|---|
| Tecnología | Node 20+ con módulos nativos (`http`, `crypto`); sin dependencias externas |
| Persistencia | Archivo `backend/db.json`, creado desde `db.seed.json` al primer arranque |
| Formato | JSON; fechas como texto `yyyy-MM-dd` |
| Autenticación | JWT HS256 en `Authorization: Bearer <token>`; vigencia de 24 h |
| Contraseñas | Se guardan con hash scrypt y sal; nunca salen en una respuesta |
| Fuera de alcance | Recuperar contraseña, renovar token, paginación, búsqueda, imágenes, HTTPS |

La API cumple el contrato mínimo del enunciado: `/register`, `/login` y el CRUD `/{recursos}`. Usa los mismos endpoints y la misma forma de respuesta de registro y login que `json-server-auth` (`accessToken` + `user`).

## 2. Requisitos de la API

| ID | Requisito | Criterio de aceptación |
|---|---|---|
| API-01 | Registrar usuario | Valida nombre, correo y contraseña de 8 o más caracteres; rechaza correos repetidos; devuelve token y usuario |
| API-02 | Iniciar sesión | Con credenciales correctas devuelve token y usuario; con incorrectas responde 401 con un mensaje que no revela si el correo existe |
| API-03 | Proteger el recurso | Toda ruta `/plants` sin token válido, con token vencido o con firma ajena responde 401 |
| API-04 | Listar | Devuelve solo las plantas del usuario autenticado; un usuario nuevo recibe `[]` |
| API-05 | Obtener una | Devuelve la planta; si no existe o es de otro usuario, responde 404 |
| API-06 | Crear | Valida el cuerpo, asigna `id` y responde 201 con la planta creada |
| API-07 | Editar | Reemplaza todos los campos (PUT) con las mismas validaciones que crear |
| API-08 | Eliminar | Borra la planta y responde 200 con `{}` |
| API-09 | Errores de validación | 400 con `message` y `errors` por campo |

## 3. Modelo de datos

### Usuario

| Campo | Tipo | Reglas |
|---|---|---|
| id | Int | Lo asigna el servidor |
| name | Texto | Obligatorio, máximo 60 caracteres |
| email | Texto | Formato de correo; se guarda en minúsculas; único |
| password | Texto | Solo en `/register`; mínimo 8 caracteres; no se devuelve |

### Planta (recurso principal)

| Campo | Tipo | Reglas de validación |
|---|---|---|
| id | Int | Lo asigna el servidor; no se envía al crear |
| name | Texto | Obligatorio, de 2 a 50 caracteres (se recortan espacios) |
| species | Texto | Obligatorio, máximo 80 caracteres |
| wateringIntervalDays | Número entero | De 1 a 60 |
| plantingDate | Fecha `yyyy-MM-dd` | Fecha real; no puede ser futura |
| lastWateredDate | Fecha `yyyy-MM-dd` | Fecha real; no futura; no anterior a `plantingDate` |
| needsSun | Booleano | `true` o `false` |

El dueño de la planta (`userId`) se guarda en el servidor y no viaja en las respuestas. El **próximo riego** no se almacena: la app lo calcula como `lastWateredDate + wateringIntervalDays`.

## 4. Contrato de la API

| Método | Ruta | Auth | Éxito | Errores |
|---|---|---|---|---|
| POST | `/register` | No | 201 `{accessToken, user}` | 400, 409 |
| POST | `/login` | No | 200 `{accessToken, user}` | 400, 401 |
| GET | `/plants` | Sí | 200 `[Planta]` | 401 |
| GET | `/plants/{id}` | Sí | 200 `Planta` | 401, 404 |
| POST | `/plants` | Sí | 201 `Planta` | 400, 401 |
| PUT | `/plants/{id}` | Sí | 200 `Planta` | 400, 401, 404 |
| DELETE | `/plants/{id}` | Sí | 200 `{}` | 401, 404 |

### Ejemplos

`POST /register`
```json
{ "name": "Citlali", "email": "citlali@mail.com", "password": "12345678" }
```
Respuesta 201 (igual para `/login`):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { "id": 2, "name": "Citlali", "email": "citlali@mail.com" }
}
```

`POST /plants` (cabecera `Authorization: Bearer <token>`)
```json
{
  "name": "Helecho",
  "species": "Nephrolepis exaltata",
  "wateringIntervalDays": 3,
  "plantingDate": "2026-05-20",
  "lastWateredDate": "2026-11-01",
  "needsSun": false
}
```
Respuesta 201: el mismo objeto con `"id"`.

Error 400 de validación:
```json
{
  "message": "Datos inválidos",
  "errors": { "wateringIntervalDays": "Los días entre riegos deben ser un entero entre 1 y 60" }
}
```
Los demás errores traen solo `message`, por ejemplo `{ "message": "Sesión no válida o expirada" }`.

### Códigos de estado y respuesta de la app

| Código | Cuándo ocurre | La app debe… |
|---|---|---|
| 400 | Datos inválidos | Mostrar el mensaje y marcar los campos de `errors` |
| 401 en `/login` | Credenciales incorrectas | Mostrar «Correo o contraseña incorrectos» |
| 401 en `/plants` | Token ausente, vencido o inválido | Borrar la sesión y volver a Login |
| 404 | La planta no existe o ya se eliminó | Avisar y refrescar la lista |
| 409 | Correo ya registrado | Avisar en el formulario de registro |
| Sin respuesta | Servidor apagado o sin red | Mostrar error de red con reintentar |

## 5. Autenticación con JWT

1. `POST /register` o `POST /login` devuelve `accessToken`.
2. La app lo guarda detrás de la interfaz `SessionStorage` (SharedPreferences).
3. `AuthInterceptor` de OkHttp agrega `Authorization: Bearer <token>` en cada petición, de modo que ningún endpoint de Retrofit lleva `@Header`.
4. El servidor verifica la firma HMAC-SHA256 y la vigencia (`exp`) en cada petición a `/plants`.

El payload contiene `sub` (id del usuario), `email`, `iat` y `exp`. Está codificado en Base64URL, no cifrado: no lleva contraseñas.

## 6. Datos semilla

`backend/db.seed.json` trae un usuario de demostración (`demo@plantas.com` / `plantas123`) con cinco plantas que cubren los estados de la lista:

| Planta | Especie | Cada | Último riego | Estado al 04/11/2026 |
|---|---|---|---|---|
| Sábila | Aloe vera | 14 días | 25/10/2026 | Riego en 4 días |
| Monstera | Monstera deliciosa | 7 días | 29/10/2026 | Riego en 1 día |
| Helecho | Nephrolepis exaltata | 3 días | 01/11/2026 | Toca hoy |
| Suculenta | Echeveria elegans | 21 días | 10/10/2026 | Atrasada 4 días |
| Menta | Mentha spicata | 2 días | 02/11/2026 | Toca hoy |

Un usuario recién registrado empieza sin plantas, lo que permite probar el estado de lista vacía.

## 7. Cómo ejecutar y probar

```bash
cd backend
npm start          # http://127.0.0.1:3000  ·  emulador: http://10.0.2.2:3000/
npm run start:fresh  # restaura los datos de la semilla
npm test           # 9 pruebas automáticas (node --test)
```

Prueba manual con curl (en PowerShell usa `curl.exe`):

```bash
curl -X POST http://127.0.0.1:3000/login -H "Content-Type: application/json" \
     -d '{"email":"demo@plantas.com","password":"plantas123"}'
curl http://127.0.0.1:3000/plants -H "Authorization: Bearer <accessToken>"
```

Cada petición se imprime en la consola del servidor (`GET /plants -> 200`), útil para la demostración.

## 8. Integración con la app Android

- `AppContainer` usa `http://10.0.2.2:3000/` como URL base: el emulador llega así al servidor de la computadora.
- El tráfico HTTP está permitido solo en depuración (`app/src/debug/AndroidManifest.xml`).
- Los DTO `PlantDto` y `PlantRequestDto` tienen los mismos campos que el JSON de la planta; el mapper convierte las fechas de texto a `LocalDate`.
- Con un teléfono físico, arranca el servidor con `HOST=0.0.0.0` y cambia la URL base por la IP de tu computadora.
