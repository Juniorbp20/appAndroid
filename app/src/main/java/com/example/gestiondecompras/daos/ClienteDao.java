package com.example.gestiondecompras.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gestiondecompras.models.Cliente;

import java.util.List;

@Dao
public interface ClienteDao {
    @Insert
    long insert(Cliente cliente);

    @Update
    int update(Cliente cliente);

    @Delete
    int delete(Cliente cliente);

    @Query("SELECT * FROM clientes")
    List<Cliente> getAllClientes();
}
