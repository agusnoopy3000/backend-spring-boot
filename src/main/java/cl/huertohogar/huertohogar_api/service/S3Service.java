package cl.huertohogar.huertohogar_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket-name:huerto-hogar-documentos}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    private S3Client s3Client;

    // Tipos de archivo permitidos
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "image/jpeg",
        "image/png",
        "image/gif",
        "text/plain",
        "text/csv"
    );

    // Tamaño máximo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostConstruct
    public void init() {
        if (accessKeyId != null && !accessKeyId.isEmpty() 
            && secretAccessKey != null && !secretAccessKey.isEmpty()) {
            
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
            log.info("S3Client inicializado para bucket: {} en región: {}", bucketName, region);
        } else {
            log.warn("AWS credentials no configuradas. S3Service operará en modo simulado.");
        }
    }

    /**
     * Sube un archivo a S3
     * @param file Archivo a subir
     * @return Array con [s3Key, urlPublica]
     */
    public String[] uploadFile(MultipartFile file) throws IOException {
        // Validaciones
        validateFile(file);

        // Generar s3Key único
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        LocalDate now = LocalDate.now();
        String s3Key = String.format("documentos/%d/%02d/%s-%s",
            now.getYear(),
            now.getMonthValue(),
            UUID.randomUUID().toString(),
            sanitizeFilename(originalFilename)
        );

        // Si no hay cliente S3 configurado, modo simulado
        if (s3Client == null) {
            log.warn("S3Client no configurado. Retornando URL simulada.");
            String simulatedUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, s3Key);
            return new String[] { s3Key, simulatedUrl };
        }

        // Subir a S3
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        
        // Generar URL pública
        String urlPublica = String.format("https://%s.s3.%s.amazonaws.com/%s", 
            bucketName, region, s3Key);

        log.info("Archivo subido exitosamente: {}", s3Key);
        return new String[] { s3Key, urlPublica };
    }

    /**
     * Elimina un archivo de S3
     * @param s3Key Key del archivo en S3
     */
    public void deleteFile(String s3Key) {
        if (s3Client == null) {
            log.warn("S3Client no configurado. Simulando eliminación de: {}", s3Key);
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Archivo eliminado de S3: {}", s3Key);
        } catch (S3Exception e) {
            log.error("Error eliminando archivo de S3: {}", e.getMessage());
            throw new RuntimeException("Error eliminando archivo de S3: " + e.getMessage());
        }
    }

    /**
     * Valida el archivo antes de subirlo
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o no fue proporcionado");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("El archivo excede el tamaño máximo permitido (10MB). Tamaño: %.2f MB", 
                    file.getSize() / (1024.0 * 1024.0))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                "Tipo de archivo no permitido: " + contentType + 
                ". Tipos permitidos: PDF, DOC, DOCX, XLS, XLSX, JPG, PNG, GIF, TXT, CSV"
            );
        }
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Limpia el nombre del archivo para uso seguro en S3
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "archivo";
        }
        // Remover caracteres especiales, mantener solo alfanuméricos, guiones y puntos
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
