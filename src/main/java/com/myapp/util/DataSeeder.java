package com.myapp.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.myapp.models.Rol;
import com.myapp.models.Usuario;
import com.myapp.repositories.RolRepository;
import com.myapp.repositories.UsuarioRepository;

// La anotación @Component la registra como un Bean de Spring
@Component
// La interfaz CommandLineRunner garantiza que el método run se ejecute al inicio
public class DataSeeder implements CommandLineRunner {

    //Inyección de dependencias de repositorios si es necesario
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyección vía constructor (Recomendado por buenas prácticas sobre @Autowired en campos)
    public DataSeeder(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Lógica para sembrar datos iniciales en la base de datos
        // Por ejemplo, crear roles predeterminados y un usuario administrador

        //Solo se ejecuta si no hay roles en la base de datos (para evitar duplicados en reinicios)
        if (rolRepository.count() == 0) {
            System.out.println("Iniciando Data Seeding: Creando Roles y Usuario Inicial...");
        }

        //1. Crear roles predeterminados si no existen
        Rol adminRol = createRoleIfNotFound("ADMINISTRADOR");
        Rol clienteRol = createRoleIfNotFound("CLIENTE");

        //2. Crear un usuario administrador inicial si no existe
        createAdminUser(adminRol);

        System.out.println("Data Seeding completado.");
    }

    private Rol createRoleIfNotFound(String nombreRol) {
        Optional<Rol> rolOpt = rolRepository.findByNombreRol(nombreRol);
        if (rolOpt.isEmpty()) {
            Rol nuevoRol = new Rol(nombreRol);
            return rolRepository.save(nuevoRol);
        }

        return rolOpt.get();
    }

    private void createAdminUser(Rol adminRol) {
        if (usuarioRepository.findByEmail("admin@connectshop.com") == null) {
           Usuario admin = new Usuario();
            admin.setNombre("Super");
            admin.setApellidos("Admin");
            admin.setEmail("admin@connectshop.com");
            admin.setPassword(passwordEncoder.encode("adminpass123")); // ¡IMPORTANTE! Hasheando la contraseña antes de guardarla
            admin.setTelefono("1234567890");

            // Asignar el rol de administrador
            Set<Rol> adminRoles = new HashSet<>(Collections.singleton(adminRol));
            admin.setRoles(adminRoles);

            usuarioRepository.save(admin);
            System.out.println("Usuario Administrador 'admin@connectshop.com' creado.");
            
        }
    }
}
