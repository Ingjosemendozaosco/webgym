package com.example.backendgym.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    public static void load() {
        try {
            Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                System.setProperty(key, value);
                System.out.println("Cargada variable de entorno: " + key);
            });
        } catch (Exception e) {
            System.err.println("Error al cargar variables de entorno: " + e.getMessage());
        }
    }
}
