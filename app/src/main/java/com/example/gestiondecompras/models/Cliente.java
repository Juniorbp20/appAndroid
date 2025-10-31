package com.example.gestiondecompras.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clientes")
public class Cliente {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nombre;
    @androidx.room.ColumnInfo(name = "apellido", defaultValue = "")
    public String apellido;
    public String telefono;
    @androidx.room.ColumnInfo(name = "direccion", defaultValue = "")
    public String direccion;
    @androidx.room.ColumnInfo(name = "activo", defaultValue = "1")
    public boolean activo;
    public String email;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        if (nombre == null) return "";
        return apellido != null && !apellido.isEmpty()
                ? nombre + " " + apellido
                : nombre;
    }
}
