package com.example.gestiondecompras.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UpdateCheckerTest {

    @Test
    public void normalize_quitaVInicial() {
        assertTrue(UpdateChecker.esNuevaVersion("v1.1"));
        assertTrue(UpdateChecker.esNuevaVersion("V2.0"));
    }

    @Test
    public void versionIgual_noEsNueva() {
        assertFalse(UpdateChecker.esNuevaVersion("v1.0"));
        assertFalse(UpdateChecker.esNuevaVersion("1.0"));
    }

    @Test
    public void versionMenor_noEsNueva() {
        assertFalse(UpdateChecker.esNuevaVersion("0.9"));
        assertFalse(UpdateChecker.esNuevaVersion("1.0.0"));
    }

    @Test
    public void versionMayor_simple() {
        assertTrue(UpdateChecker.esNuevaVersion("1.1"));
        assertTrue(UpdateChecker.esNuevaVersion("2.0"));
        assertTrue(UpdateChecker.esNuevaVersion("10.0"));
    }

    @Test
    public void versionMayor_conTresPartes() {
        assertTrue(UpdateChecker.esNuevaVersion("1.0.1"));
        assertTrue(UpdateChecker.esNuevaVersion("1.0.2"));
    }

    @Test
    public void versionVacia_noEsNueva() {
        assertFalse(UpdateChecker.esNuevaVersion(""));
        assertFalse(UpdateChecker.esNuevaVersion(null));
    }

    @Test
    public void versionConPrefijoRaro_noEsNueva() {
        assertFalse(UpdateChecker.esNuevaVersion("abc"));
    }
}
