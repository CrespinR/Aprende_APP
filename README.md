# AprendeApp 📚🚀

**AprendeApp** es una aplicación móvil desarrollada para la gestión de recursos educativos de Ingeniería de Sistemas (Libros, Videos, Artículos y Tutoriales), correspondiente al **Tercer Desafío Práctico de Desarrollo de Software para Móviles (DSM)** de la **Universidad Don Bosco**.

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **IDE:** Android Studio
- **Arquitectura:** Modelo-Vista-Controlador (MVC)
- **UI:** XML Layouts con Material Design 3
- **Autenticación:** Firebase Authentication (Correo y Contraseña)
- **API Rest:** Retrofit 2 + Gson Converter Factory
- **Carga de Imágenes:** Coil
- **Base de Datos Local:** Room Database (para Favoritos)
- **Concurrencia:** Corrutinas Kotlin (Dispatchers.IO & lifecycleScope)
- **Servidor API Remoto:** MockAPI (`https://6ae0095b8e42963c8d5ec19c.mockapi.io/`)

---

## 🏛️ Arquitectura del Proyecto (MVC)

El proyecto está estructurado formalmente bajo la arquitectura MVC para mantener separación clara de responsabilidades:

```text
com.example.aprendeapp
├── model/
│   ├── Recurso.kt              # Modelo de recursos educativos y cálculos de calificaciones
│   ├── Usuario.kt              # Modelo de usuario autenticado y rol
│   ├── Favorito.kt             # Modelo conceptual de favorito
│   └── Calificacion.kt         # Submodelo de calificaciones compartidas
├── network/
│   ├── RetrofitClient.kt       # Configuración Singleton de Retrofit y OkHttpClient
│   └── RecursoApiService.kt    # Interfaz HTTP para operaciones CRUD en MockAPI
├── database/
│   ├── FavoritoEntity.kt       # Entidad Room para almacenamiento local
│   ├── FavoritoDao.kt          # DAO con consultas SQLite
│   └── AppDatabase.kt          # Clase abstracta RoomDatabase
├── controller/
│   ├── AuthController.kt       # Lógica de registro, login y roles en Firebase Auth
│   ├── RecursoController.kt    # Lógica de consumo de API, filtrado y búsqueda
│   ├── FavoritoController.kt   # Lógica de gestión de favoritos locales con Room
│   └── CalificacionController.kt # Cálculo y envío de calificaciones compartidas
├── adapter/
│   └── RecursoAdapter.kt       # Adaptador RecyclerView para renderizar tarjetas de recursos
├── view/
│   ├── LoginActivity.kt        # Pantalla de inicio de sesión
│   ├── RegistroActivity.kt     # Pantalla de registro con validaciones avanzadas
│   ├── MainActivity.kt         # Catálogo principal con filtros y búsqueda
│   ├── DetalleRecursoActivity.kt # Vista detallada y sistema de calificación
│   ├── FavoritosActivity.kt    # Listado de recursos guardados en Room
│   ├── PerfilActivity.kt       # Información del usuario actual y rol
│   ├── AdminRecursosActivity.kt  # Gestión docente (CRUD de recursos)
│   └── FormularioRecursoActivity.kt # Formulario de creación y edición
└── util/
    ├── PasswordValidator.kt    # Validador de contraseñas seguras
    └── Constants.kt            # Constantes de API, tipos y correos autorizados
```

---

## 🌐 Conexión con MockAPI

- **Base URL:** `https://6ae0095b8e42963c8d5ec19c.mockapi.io/`
- **Endpoint:** `recursos`

### Métodos HTTP Implementados en `RecursoApiService`:
- `GET /recursos` - Obtener todos los recursos.
- `GET /recursos/{id}` - Obtener un recurso específico.
- `POST /recursos` - Crear un nuevo recurso educativo.
- `PUT /recursos/{id}` - Actualizar un recurso existente.
- `DELETE /recursos/{id}` - Eliminar un recurso del catálogo.

### Esquema del Recurso:
```json
{
  "id": "1",
  "titulo": "Estructura de Datos en Kotlin",
  "descripcion": "Guía completa de estructuras de datos avanzadas.",
  "tipo": "Libro",
  "enlace": "https://kotlinlang.org/docs/home.html",
  "imagen": "https://picsum.photos/400/250",
  "fechaCreacion": "2025-03-30",
  "calificaciones": [
    {
      "usuarioId": "firebase_uid_123",
      "puntuacion": 5.0
    }
  ]
}
```

> **Nota sobre el esquema de MockAPI:** Para almacenar correctamente el arreglo de `calificaciones`, no se requiere crear una nueva colección. En MockAPI Console, asegúrate de que el recurso contenga un atributo opcional `calificaciones` con tipo Objeto/Arreglo JSON. Si MockAPI no incluye la clave `calificaciones` en respuestas antiguas, el parser lo interpreta como lista vacía o nula de forma transparente.

---

## 🔐 Firebase Authentication y Roles

La aplicación utiliza exclusivamente **Firebase Authentication** para el control de acceso:

1. **Registro:** Solicita Nombre completo, Correo, Contraseña y Confirmación.
2. **Validación de Contraseña:**
   - Mínimo 12 caracteres.
   - Al menos 1 letra mayúscula.
   - Al menos 1 letra minúscula.
   - Al menos 1 número.
   - Al menos 1 carácter especial (`!@#$%^&*()_+-=[]{}|;':",./<>?`).
3. **Roles (Estudiante / Docente):**
   - Todos los usuarios registrados inician de forma predeterminada con el rol **Estudiante**.
   - Para fines demostrativos y de prueba del rol **Docente**, la aplicación identifica cuentas autorizadas mediante la lista configurable `Constants.DOCENTE_EMAILS` (por ejemplo: `docente@udb.edu.sv`, `docente@aprendeapp.com`, `admin@udb.edu.sv`).
   - El rol autoriza la visibilidad de los botones de creación, edición y eliminación en la interfaz.

---

## 💾 Favoritos con Room Database

Los favoritos se almacenan en la base de datos local SQLite mediante **Room**:
- **Tabla:** `favoritos` (Llave primaria compuesta por `usuarioId` + `recursoId`).
- Cada usuario autenticado ve únicamente sus propios favoritos (`WHERE usuarioId = :uid`).
- **Persistencia local:** Los favoritos permanecen guardados al cerrar y abrir la aplicación.
- **Aviso:** Los favoritos son locales del dispositivo y no se sincronizan entre diferentes dispositivos.

---

## ⭐ Sistema de Calificaciones Compartidas

- Las calificaciones son globales para que todos los usuarios puedan ver el promedio actualizado.
- Se almacenan dentro del arreglo `calificaciones` de cada objeto `Recurso` en MockAPI.
- Cuando un usuario califica, la app actualiza su puntuación previa o agrega una nueva, y realiza una solicitud `PUT /recursos/{id}` a MockAPI.

---

## ⚠️ Limitaciones Técnicas y Riesgos Identificados

1. **Concurrencia en MockAPI:** MockAPI no proporciona actualizaciones transaccionales automáticas. Si dos usuarios califican o modifican un recurso al mismo tiempo, existe riesgo de sobrescritura.
2. **Validación de Permisos:** La restricción del rol Docente en esta app es demostrativa en el cliente Android. En un entorno de producción real, el servidor backend debe validar el token de Firebase Authentication antes de permitir operaciones `POST`, `PUT` o `DELETE`.
3. **Sincronización de Favoritos:** La base de datos Room es local por dispositivo; cerrar sesión no borra la BD, pero la sesión activa aísla las vistas por el `UID` del usuario actual.

---

## 🚀 Pasos Manuales para Compilar y Ejecutar

Dado que las compilaciones automáticas han sido omitidas según los requerimientos:

1. Abre el proyecto **AprendeApp** en Android Studio.
2. Realiza un **Gradle Sync** (`File -> Sync Project with Gradle Files`).
3. Asegúrate de tener conexión a Internet habilitada.
4. Selecciona un emulador Android (API 24 o superior) o un dispositivo físico.
5. Ejecuta la aplicación presionando **Run 'app'** (`Shift + F10`).

---

## 📝 Propuesta de Commits en Español

A continuación se presenta la lista de 15 commits sugeridos para el control de versiones Git:

1. `Agregar configuracion inicial del proyecto y estructura MVC`
2. `Configurar dependencias en Gradle libs versions toml`
3. `Configurar conexion con MockAPI utilizando Retrofit y Gson`
4. `Implementar modelos de datos Recurso Calificacion y Usuario`
5. `Implementar autenticacion Firebase con registro e inicio de sesion`
6. `Agregar validador de contraseñas seguras de 12 caracteres`
7. `Crear base de datos local Room para gestion de favoritos`
8. `Implementar catalogo principal con RecyclerView y tarjetas Material Design`
9. `Agregar busqueda interactiva por ID titulo y tipo`
10. `Implementar filtros por tipo de recurso y ordenamiento alfabético`
11. `Crear pantalla de detalles de recurso y apertura de enlaces seguros`
12. `Implementar sistema de calificaciones compartidas en MockAPI`
13. `Crear panel de administracion docente con operaciones CRUD`
14. `Implementar pantalla de perfil de usuario y cierre de sesion`
15. `Actualizar documentacion README con guia de arquitectura y ejecucion`
