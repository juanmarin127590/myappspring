# Guía de Migración para Imágenes Remotas

## 🎯 Objetivo
Configurar tu backend Spring Boot para servir imágenes a través de URLs que Flutter pueda cargar.

## 📝 Pasos de Implementación

### 1. Actualizar la Entidad Producto

```java
@Entity
@Table(name = "productos")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;
    
    // ... otros campos
    
    /**
     * URL completa de la imagen del producto
     * Ejemplo: "http://localhost:8080/api/images/products/abc123.jpg"
     * o Cloud: "https://cloudinary.com/myapp/products/abc123.jpg"
     */
    @Column(length = 500, name = "imagen_url")
    private String imagenUrl;
    
    // Getters y Setters
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
```

### 2. Actualizar el ProductoController

```java
@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Value("${app.server.url:http://localhost:8080}")
    private String serverUrl;
    
    /**
     * Crear producto con imagen
     */
    @PostMapping
    public ResponseEntity<Producto> createProduct(
        @RequestBody Producto producto
    ) {
        // Validar que tenga imagen
        if (producto.getImagenUrl() == null || producto.getImagenUrl().isEmpty()) {
            // Asignar imagen por defecto
            producto.setImagenUrl(serverUrl + "/api/images/products/default-product.jpg");
        }
        
        Producto saved = productoRepository.save(producto);
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Obtener todos los productos
     * Las URLs de imagen ya vienen de la base de datos
     */
    @GetMapping
    public List<Producto> getAllProducts() {
        return productoRepository.findAll();
    }
}
```

### 3. Crear ImageController (usar EJEMPLO_ImageController.java)

Ya proporcionado en el archivo `EJEMPLO_ImageController.java`

### 4. Configurar application.properties

Ya proporcionado en el archivo `CONFIGURACION_application.properties`

### 5. Actualizar la Base de Datos

```sql
-- Actualizar la columna imagen_url para almacenar URLs completas
ALTER TABLE productos 
MODIFY COLUMN imagen_url VARCHAR(500);

-- Ejemplo de actualización para productos existentes
-- Opción A: Si tienes imágenes locales, generales URLs
UPDATE productos 
SET imagen_url = CONCAT('http://localhost:8080/api/images/products/', imagen_url)
WHERE imagen_url NOT LIKE 'http%';

-- Opción B: Asignar imagen por defecto
UPDATE productos 
SET imagen_url = 'http://localhost:8080/api/images/products/default-product.jpg'
WHERE imagen_url IS NULL OR imagen_url = '';
```

## 🚀 Flujo de Trabajo Completo

### Escenario 1: Cargar Productos con Imágenes desde Admin

```java
@PostMapping("/api/productos/with-image")
public ResponseEntity<?> createProductWithImage(
    @RequestParam("imagen") MultipartFile imagen,
    @RequestParam("nombreProducto") String nombre,
    @RequestParam("precio") BigDecimal precio,
    @RequestParam("idCategoria") Long idCategoria
    // ... otros parámetros
) {
    try {
        // 1. Subir imagen primero
        ImageController.ImageUploadResponse uploadResponse = 
            imageController.uploadProductImage(imagen).getBody();
        
        // 2. Crear producto con la URL de la imagen
        Producto producto = new Producto();
        producto.setNombreProducto(nombre);
        producto.setPrecio(precio);
        producto.setImagenUrl(uploadResponse.getUrl());
        
        // Asociar categoría
        Categoria categoria = categoriaRepository.findById(idCategoria)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoria);
        
        // 3. Guardar producto
        Producto saved = productoRepository.save(producto);
        
        return ResponseEntity.ok(saved);
        
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body("Error al crear producto: " + e.getMessage());
    }
}
```

### Escenario 2: Productos de Prueba (Seed Data)

```java
@Component
public class DataSeeder implements CommandLineRunner {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Value("${app.server.url}")
    private String serverUrl;
    
    @Override
    public void run(String... args) {
        // Solo ejecutar si no hay productos
        if (productoRepository.count() == 0) {
            
            // Opción A: URLs locales (debes tener las imágenes en uploads/products/)
            Producto laptop = new Producto();
            laptop.setNombreProducto("Laptop Gaming");
            laptop.setPrecio(new BigDecimal("1299.99"));
            laptop.setImagenUrl(serverUrl + "/api/images/products/laptop-gaming.jpg");
            productoRepository.save(laptop);
            
            // Opción B: URLs externas (para pruebas rápidas)
            Producto mouse = new Producto();
            mouse.setNombreProducto("Mouse Inalámbrico");
            mouse.setPrecio(new BigDecimal("29.99"));
            mouse.setImagenUrl("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46");
            productoRepository.save(mouse);
            
            System.out.println("✅ Datos de prueba creados");
        }
    }
}
```

## 📱 Desde Flutter

Tu app Flutter ahora manejará automáticamente ambos tipos:

```dart
// Ejemplo de respuesta del backend:
{
  "idProducto": 1,
  "nombreProducto": "Laptop Gaming",
  "precio": 1299.99,
  "imagenUrl": "http://localhost:8080/api/images/products/laptop.jpg",
  // o
  "imagenUrl": "https://images.unsplash.com/photo-xyz"
}

// NetworkImageWidget detecta automáticamente el tipo y lo maneja
NetworkImageWidget(
  imageUrl: product.imageUrl,  // ✅ Funciona con ambos
  fit: BoxFit.cover,
)
```

## 🔧 Testing

### Probar subida de imagen con Postman/cURL:

```bash
curl -X POST http://localhost:8080/api/upload/product-image \
  -F "file=@/ruta/a/tu/imagen.jpg"

# Respuesta esperada:
{
  "url": "http://localhost:8080/api/images/products/abc-123-xyz.jpg",
  "filename": "abc-123-xyz.jpg"
}
```

### Probar descarga de imagen:

```bash
# Abrir en navegador
http://localhost:8080/api/images/products/abc-123-xyz.jpg
```

### Probar productos endpoint:

```bash
curl http://localhost:8080/api/productos

# Respuesta esperada:
[
  {
    "idProducto": 1,
    "nombreProducto": "Laptop Gaming",
    "precio": 1299.99,
    "imagenUrl": "http://localhost:8080/api/images/products/laptop.jpg",
    "categoria": {
      "idCategoria": 1,
      "nombreCategoria": "Electrónica"
    }
  }
]
```

## 🛡️ Seguridad (Opcional pero Recomendado)

### Proteger el endpoint de subida con autenticación:

```java
@PostMapping("/upload/product-image")
@PreAuthorize("hasRole('ADMIN')") // Solo admins pueden subir imágenes
public ResponseEntity<?> uploadProductImage(
    @RequestHeader("Authorization") String token,
    @RequestParam("file") MultipartFile file
) {
    // Validar token JWT
    // ... código de subida
}
```

## 📊 Monitoreo de Uso de Disco

```java
@GetMapping("/api/admin/storage-info")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getStorageInfo() {
    try {
        Path uploadPath = Paths.get(uploadDir);
        
        long totalSize = Files.walk(uploadPath)
            .filter(Files::isRegularFile)
            .mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return 0L;
                }
            })
            .sum();
        
        long fileCount = Files.walk(uploadPath)
            .filter(Files::isRegularFile)
            .count();
        
        Map<String, Object> info = new HashMap<>();
        info.put("totalSizeMB", totalSize / (1024.0 * 1024.0));
        info.put("fileCount", fileCount);
        info.put("directory", uploadPath.toString());
        
        return ResponseEntity.ok(info);
        
    } catch (IOException e) {
        return ResponseEntity.internalServerError().build();
    }
}
```

## ✅ Checklist Final

- [ ] ImageController creado y funcionando
- [ ] application.properties configurado
- [ ] Directorio uploads/products creado
- [ ] Entidad Producto actualizada con imagenUrl (VARCHAR 500)
- [ ] Base de datos actualizada
- [ ] Productos de prueba con URLs válidas
- [ ] CORS configurado para permitir Flutter
- [ ] Imágenes por defecto disponibles
- [ ] Flutter actualizado con NetworkImageWidget
- [ ] Tests de endpoints realizados

## 🎉 ¡Listo!

Tu aplicación ahora está lista para manejar imágenes remotas de manera profesional.
