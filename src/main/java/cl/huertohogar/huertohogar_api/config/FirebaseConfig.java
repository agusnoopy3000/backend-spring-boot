package cl.huertohogar.huertohogar_api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Configuración de Firebase Admin SDK para autenticación híbrida.
 * 
 * El SDK se puede inicializar de dos formas:
 * 1. Mediante archivo JSON en classpath (firebase-service-account.json)
 * 2. Mediante variable de entorno FIREBASE_CREDENTIALS_JSON con el contenido JSON
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path:firebase-service-account.json}")
    private String credentialsPath;

    @Value("${firebase.credentials.json:#{null}}")
    private String credentialsJson;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initializeFirebase() {
        if (!firebaseEnabled) {
            log.warn("Firebase está deshabilitado. La autenticación híbrida no estará disponible.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseOptions options = createFirebaseOptions();
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK inicializado correctamente");
            } catch (IOException e) {
                log.error("❌ Error al inicializar Firebase Admin SDK: {}", e.getMessage());
                log.warn("La autenticación híbrida Firebase no estará disponible. " +
                        "Asegúrate de configurar las credenciales de Firebase.");
            }
        } else {
            log.info("Firebase Admin SDK ya estaba inicializado");
        }
    }

    private FirebaseOptions createFirebaseOptions() throws IOException {
        InputStream serviceAccount = getCredentialsInputStream();
        
        return FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
    }

    private InputStream getCredentialsInputStream() throws IOException {
        // Opción 1: Credenciales desde variable de entorno (recomendado para producción)
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            log.info("Usando credenciales de Firebase desde variable de entorno");
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        // Opción 2: Archivo en sistema de archivos (ruta absoluta)
        File externalFile = new File(credentialsPath);
        if (externalFile.exists() && externalFile.isFile()) {
            log.info("Usando credenciales de Firebase desde archivo externo: {}", credentialsPath);
            Resource fileResource = new FileSystemResource(externalFile);
            return fileResource.getInputStream();
        }

        // Opción 3: Archivo en classpath
        Resource resource = new ClassPathResource(credentialsPath);
        if (resource.exists()) {
            log.info("Usando credenciales de Firebase desde classpath: {}", credentialsPath);
            return resource.getInputStream();
        }

        throw new IOException("No se encontraron credenciales de Firebase. " +
                "Configure FIREBASE_CREDENTIALS_JSON o proporcione una ruta válida en firebase.credentials.path");
    }
}
