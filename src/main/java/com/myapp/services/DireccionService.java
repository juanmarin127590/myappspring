package com.myapp.services;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myapp.models.Direccion;
import com.myapp.models.Usuario;
import com.myapp.repositories.DireccionRepository;
import com.myapp.repositories.UsuarioRepository;

@Service
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DireccionService(DireccionRepository direccionRepository, UsuarioRepository usuarioRepository) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Lógica para manejar la dirección principal de un usuario
    private void asegurarUnaPrincipal(Long idUsuario, Direccion nuevaDireccion) {
        if (nuevaDireccion.getPrincipalEnvio() != null && nuevaDireccion.getPrincipalEnvio()) {
            // Desactiva cualquier otra dirección principal para este usuario
            direccionRepository.findByUsuario_IdUsuarioAndPrincipalEnvioTrue(idUsuario)
                .ifPresent(principalExistente -> {
                    if (!principalExistente.getIdDireccion().equals(nuevaDireccion.getIdDireccion())) {
                        principalExistente.setPrincipalEnvio(false);
                        direccionRepository.save(principalExistente);
                    }
                });
        }
    }

    // CREATE
    @Transactional
    public Direccion crearDireccion(Long idUsuario, Direccion direccion) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));
        
        direccion.setUsuario(usuario);

        // 1. Manejar lógica de dirección principal
        asegurarUnaPrincipal(idUsuario, direccion);
        
        // 2. Si no es la primera dirección, y no se marcó como principal, se guarda como no principal
        List<Direccion> direccionesExistentes = direccionRepository.findByUsuario_IdUsuario(idUsuario);
        if (direccionesExistentes.isEmpty()) {
            direccion.setPrincipalEnvio(true); // La primera dirección siempre es principal
        }
        
        return direccionRepository.save(direccion);
    }

    // READ ALL (por Usuario)
    public List<Direccion> obtenerDireccionesPorUsuario(Long idUsuario) {
        return direccionRepository.findByUsuario_IdUsuario(idUsuario);
    }

    // READ BY ID (Verificación de propiedad)
    public Optional<Direccion> obtenerDireccionPorIdYUsuario(Long idDireccion, Long idUsuario) {
        return direccionRepository.findByIdDireccionAndUsuario_IdUsuario(idDireccion, idUsuario);
    }

    // UPDATE
    @Transactional
    public Direccion actualizarDireccion(Long idDireccion, Long idUsuario, Direccion detalles) {
        Direccion direccionExistente = direccionRepository.findByIdDireccionAndUsuario_IdUsuario(idDireccion, idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada o no pertenece al usuario: " + idDireccion));

        // Actualizar campos solo si se proporcionan (no son null)
        if (detalles.getNombreDestinatario() != null) {
            direccionExistente.setNombreDestinatario(detalles.getNombreDestinatario());
        }
        if (detalles.getCallePrincipal() != null) {
            direccionExistente.setCallePrincipal(detalles.getCallePrincipal());
        }
        if (detalles.getNumeroExterior() != null) {
            direccionExistente.setNumeroExterior(detalles.getNumeroExterior());
        }
        if (detalles.getInformacionAdicional() != null) {
            direccionExistente.setInformacionAdicional(detalles.getInformacionAdicional());
        }
        if (detalles.getCiudad() != null) {
            direccionExistente.setCiudad(detalles.getCiudad());
        }
        if (detalles.getEstado() != null) {
            direccionExistente.setEstado(detalles.getEstado());
        }
        if (detalles.getCodigoPostal() != null) {
            direccionExistente.setCodigoPostal(detalles.getCodigoPostal());
        }
        if (detalles.getPais() != null) {
            direccionExistente.setPais(detalles.getPais());
        }

        // Si se intenta cambiar el estado de 'principal'
        if (detalles.getPrincipalEnvio() != null) {
            direccionExistente.setPrincipalEnvio(detalles.getPrincipalEnvio());
            asegurarUnaPrincipal(idUsuario, direccionExistente);
        }
        
        return direccionRepository.save(direccionExistente);
    }

    // DELETE
    @Transactional
    public void eliminarDireccion(Long idDireccion, Long idUsuario) {
        Direccion direccion = direccionRepository.findByIdDireccionAndUsuario_IdUsuario(idDireccion, idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada o no pertenece al usuario: " + idDireccion));

        if (direccion.getPrincipalEnvio()) {
            throw new IllegalStateException("No se puede eliminar la dirección principal. Asigna otra principal primero.");
        }
        
        direccionRepository.delete(direccion);
    }

    // ----------------------------------------------------
    // MÉTODOS DE ACCESO DE ADMINISTRADOR (Sin restricción de Usuario)
    // ----------------------------------------------------

    public List<Direccion> obtenerTodasDirecciones() {
        return direccionRepository.findAll();
    }
    
    public Optional<Direccion> obtenerDireccionPorId(Long idDireccion) {
        return direccionRepository.findById(idDireccion);
    }
    
}
