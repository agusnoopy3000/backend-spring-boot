package cl.huertohogar.huertohogar_api.config;

import cl.huertohogar.huertohogar_api.model.Product;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.ProductRepository;
import cl.huertohogar.huertohogar_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User();
            admin.setNombre("Admin");
            admin.setApellido("Huertohogar");
            admin.setEmail("admin@huertohogar.cl");
            admin.setPassword(passwordEncoder.encode("min123"));
            admin.setDireccion("Direccion Admin");
            admin.setTelefono("123456789");
            admin.setRol("admin");
            userRepository.save(admin);

            // Create demo client
            User client = new User();
            client.setNombre("Cliente");
            client.setApellido("Demo");
            client.setEmail("cliente@demo.com");
            client.setPassword(passwordEncoder.encode("password"));
            client.setDireccion("Direccion Cliente");
            client.setTelefono("987654321");
            client.setRol("cliente");
            userRepository.save(client);
        }

        if (productRepository.count() == 0) {
            // Create demo products
            Product product1 = new Product();
            product1.setCodigo("PROD001");
            product1.setNombre("Tomate Orgánico");
            product1.setDescripcion("Tomates frescos cultivados orgánicamente");
            product1.setPrecio(1500.0);
            product1.setStock(100);
            product1.setImagen("https://s3.amazonaws.com/huertohogar/tomate.jpg");
            product1.setCategoria("Verdura");
            productRepository.save(product1);
            // ...puedes agregar más productos demo aquí...
        }
    }
}
