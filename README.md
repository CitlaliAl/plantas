# Cuidado de Plantas
App Android nativa para llevar el riego de tus plantas. CRUD completo con autenticación, hecha con Kotlin y Jetpack Compose. Proyecto de la Unidad 3 de Aplicaciones móviles, UP Chiapas.

**Autora:** Citlali  Montserrat Alvarado Pérez· 4°A · **Maestro:** Ali López Zúnun

## Funciones

- Registro, inicio y cierre de sesión (JWT).
- Lista de plantas con estado de riego: "Atrasada N días", "Toca regar hoy", "Riego mañana", "Riego en N días".
- Detalle con próximo riego, último riego, frecuencia, sol y fecha de siembra.
- Crear, editar y eliminar plantas (con confirmación).
- Botón **Regar hoy** que registra el riego del día.
- Si la sesión expira (401), la app la limpia y vuelve al Login con aviso.

## Capturas

| Login | Lista | Detalle |
| --- | --- | --- |
| ![Login](docs/capturas/login.png) | ![Lista](docs/capturas/lista.png) | ![Detalle](docs/capturas/detalle.png) |

| Nueva planta | Editar | Eliminar |
| --- | --- | --- |
| ![Nueva](docs/capturas/nueva.png) | ![Editar](docs/capturas/editar.png) | ![Eliminar](docs/capturas/eliminar.png) |

## Tecnologías

- Kotlin, Jetpack Compose y Material 3
- MVVM + Repository, con DTO y Mapper
- Inyección de dependencias manual (`AppContainer`)
- Retrofit, kotlinx.serialization y un interceptor de OkHttp
- Sesión en SharedPreferences detrás de una abstracción
- Navigation Compose
- Version Catalog de Gradle

## Arquitectura

Paquete base: `com.upchiapas.plantas`

```
auth/            data · domain · ui
plants/          data · domain · ui
core/            network · session · validation
di/              AppContainer
ui/              components · navigation · theme
MainActivity, PlantasApplication
```

Cada funcionalidad (`auth`, `plants`) separa:

- **data**: API (Retrofit), DTO, mappers y repositorio.
- **domain**: modelos y reglas.
- **ui**: pantallas Compose y ViewModels.

Rutas de navegación: `login?expired={expired}`, `register`, `plants`, `plant/{id}`, `plant_form?id={id}`.

## Cómo correr la app

1. Clona el repositorio:
   ```bash
   git clone https://github.com/CitlaliAI/plantas.git
   ```
2. Ábrelo con Android Studio y espera a que Gradle sincronice.
3. Elige un emulador o dispositivo y presiona **Run**.

La app apunta por defecto a la API ya desplegada (`BASE_URL` en `di/AppContainer.kt`), así que no necesitas levantar el backend.

**Usuario de prueba:** `demo@plantas.com` / `plantas123`

> Si la API no responde, puede ser que la instancia de AWS (Learner Lab) esté apagada.

## API

URL base: `https://plantas-citlali.duckdns.org`

| Método | Ruta | Descripción | Sesión |
| --- | --- | --- | --- |
| POST | `/register` | Crear usuario | No |
| POST | `/login` | Iniciar sesión | No |
| POST | `/logout` | Revocar el token actual | Sí |
| GET | `/tokens` | Historial de tokens | Sí |
| GET | `/plants` | Listar plantas | Sí |
| GET | `/plants/{id}` | Detalle | Sí |
| POST | `/plants` | Crear | Sí |
| PUT | `/plants/{id}` | Editar | Sí |
| DELETE | `/plants/{id}` | Eliminar | Sí |

- Node 22 sin dependencias externas, contraseñas con scrypt y JWT HS256.
- Cada JWT lleva un `jti`; `/logout` lo revoca y `/tokens` muestra el historial.
- Desplegada en AWS EC2 (Amazon Linux 2023) con Caddy para HTTPS y un dominio de DuckDNS.

## Pruebas

- Unitarias en la app: `AuthValidatorTest`, `PlantFormValidatorTest`, `PlantTest`.
- Backend: 12 pruebas con `node --test`.
