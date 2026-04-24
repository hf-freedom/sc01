package com.example.permission.config;

import com.example.permission.store.DataStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        DataStore.init();
    }
}
