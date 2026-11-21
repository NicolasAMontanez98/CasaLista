# CasaLista

Tienda virtual de casas prefabricadas para Android. Conecta a vendedores (fabricantes y constructoras) con compradores, y tiene un administrador que revisa los productos y gestiona los usuarios.

Proyecto de la asignatura **2310-6B Desarrollo de aplicaciones móviles nativas**, Fundación Universitaria Compensar.

## Qué hace la aplicación

| Rol | Qué puede hacer |
|---|---|
| **Comprador** | Registrarse, ver el catálogo y el detalle con fotos, buscar y filtrar, usar el carrito, pagar con ePayco (modo de pruebas), indicar la dirección con el GPS y ver su historial de pedidos. |
| **Vendedor** | Crear, editar, pausar y eliminar productos con fotos de cámara o galería, y gestionar los pedidos recibidos (aceptar, enviar, entregar, cancelar y ver la entrega en el mapa). |
| **Administrador** | Panel con indicadores, CRUD de usuarios (crear, editar, suspender, eliminar), cola de revisión de productos y reporte de ventas con exportación a CSV. |

Para los tres roles: inicio de sesión con correo y contraseña, huella digital, recuperación de contraseña, perfil editable y tema claro u oscuro.

## Tecnologías

- Kotlin, vistas XML con ViewBinding, Navigation Component y Material 3.
- Firebase: Authentication, Cloud Firestore y Cloud Storage.
- Corrutinas para las operaciones en segundo plano.
- Glide para cargar imágenes, BiometricPrompt para la huella y Fused Location con Geocoder para la ubicación.
- ePayco (checkout web dentro de un WebView) para los pagos en modo de pruebas.
- `minSdk 30`, `targetSdk 37`.

## Estructura del código

```
app/src/main/java/com/example/casalista/
├── data/    Modelos, repositorios de Firebase, carrito e integración con ePayco
├── ui/
│   ├── auth/      Inicio de sesión, registro y recuperar contraseña
│   ├── buyer/     Inicio, catálogo, detalle, carrito, pago y mis pedidos
│   ├── seller/    Panel de productos, formulario y pedidos
│   ├── admin/     Panel, usuarios, revisión de productos y ventas
│   └── common/    Perfil, adaptadores de listas, navegación y gráfico
└── util/    Validaciones, formato, huella, ubicación, imágenes y tema
firestore.rules    Reglas de seguridad de Firestore por rol
storage.rules      Reglas de seguridad de Storage
```

## Cómo ejecutarla

El proyecto compila sin configuración extra, pero para que la app se conecte a datos hace falta un proyecto de Firebase propio.

1. Abre la carpeta en **Android Studio** (con el JDK que trae el IDE) y espera la sincronización de Gradle.
2. En la [consola de Firebase](https://console.firebase.google.com) crea un proyecto y registra una app Android con el paquete `com.example.casalista`.
3. Descarga `google-services.json` y colócalo en `app/`. Está en `.gitignore`: no se sube al repositorio.
4. Activa **Authentication** (correo y contraseña), **Firestore** y **Storage**.
5. Publica las reglas: copia `firestore.rules` en *Firestore → Reglas* y `storage.rules` en *Storage → Reglas*.
6. Crea el primer administrador: registra un usuario desde la app y, en Firestore, cambia el campo `role` de su documento en `users` a `ADMIN`.
7. Al entrar como administrador, si no hay productos, se cargan 12 casas de ejemplo.

### Pagos con ePayco (opcional)

Crea una cuenta gratuita de ePayco y agrega tu llave **pública** en `local.properties` (archivo ignorado por Git):

```
epayco.publicKey=TU_LLAVE_PUBLICA
epayco.testAmount=50000
```

`epayco.testAmount` es el tope que se cobra por transacción en pruebas; ajústalo al rango permitido por tu cuenta. Sin la llave, la app avisa al pagar. Tarjeta de prueba aprobada de ePayco: `4575 6231 8229 0326`, vencimiento 12/2027, CVV 123.

## Pruebas

```
./gradlew testDebugUnitTest
```

Hay pruebas unitarias de la lógica de datos, el carrito, las validaciones, el CSV, el filtro de usuarios y la lectura de las respuestas de ePayco.

## Limitaciones conocidas

- El pago funciona solo en modo de pruebas y no hay servidor que reciba la confirmación de ePayco; la app valida el pago al volver del checkout.
- Todos los vendedores ven y editan todos los productos y pedidos.
- Al eliminar un usuario se borra su perfil, pero su cuenta de Authentication permanece, porque no se puede borrar desde el cliente.
- El botón de exportar a PDF del reporte aún no está implementado; el CSV sí.
