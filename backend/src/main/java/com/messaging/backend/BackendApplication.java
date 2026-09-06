package com.messaging.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

	public static void main(String[] args) {
		loadDotEnvIfPresent();
		SpringApplication.run(BackendApplication.class, args);
	}

	private static void loadDotEnvIfPresent() {
		Path[] searchPaths = new Path[] {
				Paths.get(".env"),
				Paths.get("../.env"),
				Paths.get("../../.env")
		};

		for (Path path : searchPaths) {
			if (Files.exists(path)) {
				try {
					List<String> lines = Files.readAllLines(path);
					for (String line : lines) {
						line = line.trim();
						if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
							int idx = line.indexOf('=');
							String key = line.substring(0, idx).trim();
							String val = line.substring(idx + 1).trim();
							if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
								if (val.length() >= 2) {
									val = val.substring(1, val.length() - 1);
								}
							}
							if (System.getProperty(key) == null && System.getenv(key) == null) {
								System.setProperty(key, val);
							}
						}
					}
					break;
				} catch (IOException ignored) {
				}
			}
		}
	}

}
