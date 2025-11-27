package com.example.backendgym;

import com.example.backendgym.config.EnvLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class BackendgymApplicationTests {

    @BeforeAll
    static void setup() {
        // Cargar variables de entorno antes de los tests
        EnvLoader.load();
    }

    @Test
    void contextLoads() {
        // Test de carga de contexto
    }
}
