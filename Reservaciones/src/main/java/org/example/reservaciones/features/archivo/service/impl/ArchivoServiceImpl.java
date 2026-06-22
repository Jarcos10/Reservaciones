package org.example.reservaciones.features.archivo.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.reservaciones.core.domain.Archivo;
import org.example.reservaciones.core.domain.Reservacion;
import org.example.reservaciones.core.domain.TipoDocumento;
import org.example.reservaciones.core.exceptions.BussinesValidationException;
import org.example.reservaciones.core.exceptions.EntityNotFoundException;
import org.example.reservaciones.features.archivo.repository.ArchivoRepository;
import org.example.reservaciones.features.archivo.service.ArchivoService;
import org.example.reservaciones.features.reservacion.repository.ReservacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchivoServiceImpl implements ArchivoService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String PDF_EXTENSION = ".pdf";

    private final ArchivoRepository archivoRepository;
    private final ReservacionRepository reservacionRepository;

    @Value("${app.uploads.identificaciones-dir:uploads/identificaciones}")
    private String identificacionesDir;

    private Path directorioIdentificaciones;

    @PostConstruct
    public void inicializarDirectorio() throws IOException {
        directorioIdentificaciones = Paths.get(identificacionesDir).toAbsolutePath().normalize();
        Files.createDirectories(directorioIdentificaciones);
    }

    @Override
    @Transactional
    public Archivo guardarIdentificacionDeReservacion(Long idReservacion, MultipartFile datosArchivo) throws IOException {
        validarArchivoPdf(datosArchivo);

        Reservacion reservacion = reservacionRepository.findById(idReservacion)
                .orElseThrow(() -> new EntityNotFoundException("La reservación " + idReservacion + " no existe"));

        boolean yaTieneIdentificacion = archivoRepository.existsByReservacionIdReservacionAndTipoDocumento(
                idReservacion,
                TipoDocumento.IDENTIFICACION
        );

        if (yaTieneIdentificacion) {
            throw new BussinesValidationException("Esta reservación ya tiene una identificación registrada");
        }

        String nombreOriginal = resolverNombreArchivo(datosArchivo);
        String nombreGuardado = generarNombreGuardado(idReservacion);
        Path directorioReservacion = directorioIdentificaciones.resolve("reservacion-" + idReservacion).normalize();
        Files.createDirectories(directorioReservacion);

        Path destino = directorioReservacion.resolve(nombreGuardado).normalize();
        if (!destino.startsWith(directorioIdentificaciones)) {
            throw new BussinesValidationException("La ruta del archivo no es válida");
        }

        try (InputStream inputStream = datosArchivo.getInputStream()) {
            Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        String rutaRelativa = directorioIdentificaciones.relativize(destino).toString().replace("\\", "/");

        Archivo archivo = Archivo.builder()
                .nombreArchivo(nombreOriginal)
                .nombreGuardado(nombreGuardado)
                .tipoArchivo(PDF_CONTENT_TYPE)
                .tipoDocumento(TipoDocumento.IDENTIFICACION)
                .rutaArchivo(rutaRelativa)
                .tamanoBytes(datosArchivo.getSize())
                .reservacion(reservacion)
                .build();

        return archivoRepository.save(archivo);
    }

    @Override
    @Transactional(readOnly = true)
    public Archivo obtenerIdentificacionDeReservacion(Long idReservacion) {
        return archivoRepository.findByReservacionIdReservacionAndTipoDocumento(
                idReservacion,
                TipoDocumento.IDENTIFICACION
        ).orElseThrow(() -> new EntityNotFoundException("La reservación no tiene identificación registrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource cargarIdentificacionDeReservacion(Long idReservacion) {
        Archivo archivo = obtenerIdentificacionDeReservacion(idReservacion);
        Path rutaFisica = resolverRutaFisica(archivo);

        if (!Files.exists(rutaFisica) || !Files.isReadable(rutaFisica)) {
            throw new EntityNotFoundException("El archivo físico de la identificación no existe en el servidor");
        }

        return new PathResource(rutaFisica);
    }

    private void validarArchivoPdf(MultipartFile datosArchivo) throws IOException {
        if (datosArchivo == null || datosArchivo.isEmpty()) {
            throw new BussinesValidationException("Debe seleccionar el archivo PDF de identificación");
        }

        if (datosArchivo.getSize() > MAX_FILE_SIZE) {
            throw new BussinesValidationException("El archivo de identificación no debe superar los 5 MB");
        }

        String nombreArchivo = resolverNombreArchivo(datosArchivo);
        if (!nombreArchivo.toLowerCase().endsWith(PDF_EXTENSION)) {
            throw new BussinesValidationException("La identificación debe ser un archivo PDF");
        }

        String contentType = datosArchivo.getContentType();
        if (contentType != null
                && !PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)
                && !"application/octet-stream".equalsIgnoreCase(contentType)) {
            throw new BussinesValidationException("Solo se permite subir identificación en formato PDF");
        }

        byte[] primerosBytes;
        try (InputStream inputStream = datosArchivo.getInputStream()) {
            primerosBytes = inputStream.readNBytes(4);
        }

        String encabezado = new String(primerosBytes, StandardCharsets.US_ASCII);
        if (!"%PDF".equals(encabezado)) {
            throw new BussinesValidationException("El archivo seleccionado no parece ser un PDF válido");
        }
    }

    private String resolverNombreArchivo(MultipartFile archivo) {
        String nombreOriginal = archivo.getOriginalFilename();
        String nombreArchivo = StringUtils.hasText(nombreOriginal)
                ? StringUtils.cleanPath(nombreOriginal)
                : "identificacion.pdf";

        if (nombreArchivo.contains("..")) {
            throw new BussinesValidationException("El nombre del archivo no es válido");
        }

        return nombreArchivo;
    }

    private String generarNombreGuardado(Long idReservacion) {
        return "identificacion-" + idReservacion + "-" + UUID.randomUUID() + PDF_EXTENSION;
    }

    private Path resolverRutaFisica(Archivo archivo) {
        if (!StringUtils.hasText(archivo.getRutaArchivo())) {
            throw new EntityNotFoundException("La identificación existe en la base de datos, pero no tiene una ruta de archivo registrada");
        }

        Path ruta = Paths.get(archivo.getRutaArchivo());
        if (ruta.isAbsolute()) {
            return ruta.normalize();
        }
        return directorioIdentificaciones.resolve(ruta).normalize();
    }
}
