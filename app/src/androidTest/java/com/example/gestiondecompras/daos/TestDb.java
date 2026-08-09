package com.example.gestiondecompras.daos;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gestiondecompras.database.AppDatabase;

public final class TestDb {

    private TestDb() {
    }

    public static AppDatabase crear() {
        return Room.inMemoryDatabaseBuilder(
                        InstrumentationRegistry.getInstrumentation().getTargetContext(),
                        AppDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    public static void cerrar(AppDatabase db) {
        if (db != null) db.close();
    }
}