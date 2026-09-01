package com.github.drakescraft_labs.exoticgarden;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regresion del cultivo que se "congelaba" tras varias cosechas: growStructure
 * escribe la esencia en los dos bloques del cultivo, asi que resolver la base de
 * hojas como si fuera la cabeza convertia el suelo del jugador en brote y dejaba
 * la cabeza real huerfana e irrompible.
 */
class PlantResolutionTest {

    private static final String ESENCIA = "COAL_ESSENCE";
    private static final String BROTE = "COAL_PLANT";

    @Test
    @DisplayName("Un bloque con esencia y esencia encima es la base, no la cabeza")
    void baseSeDetectaPorLaEsenciaDeArriba() {
        assertTrue(ExoticGarden.esBaseDeCultivo(ESENCIA, ESENCIA));
    }

    @Test
    @DisplayName("La cabeza real no tiene esencia encima")
    void cabezaNoSeConfundeConLaBase() {
        assertFalse(ExoticGarden.esBaseDeCultivo(ESENCIA, null));
        assertFalse(ExoticGarden.esBaseDeCultivo(ESENCIA, "AIR"));
    }

    @Test
    @DisplayName("Desde la cabeza, la base vale tanto con esencia como con brote")
    void baseDebajoDeLaCabeza() {
        assertTrue(ExoticGarden.esBaseDeCultivo(ESENCIA, BROTE, ESENCIA));
        assertTrue(ExoticGarden.esBaseDeCultivo(ESENCIA, BROTE, BROTE));
    }

    @Test
    @DisplayName("El suelo bajo una cabeza huerfana nunca se toma como base")
    void sueloNoEsBase() {
        assertFalse(ExoticGarden.esBaseDeCultivo(ESENCIA, BROTE, null));
        assertFalse(ExoticGarden.esBaseDeCultivo(ESENCIA, BROTE, "GRASS_BLOCK"));
    }

    @Test
    @DisplayName("Solo ORE_PLANT y DOUBLE_PLANT ocupan dos bloques")
    void tiposDeDosBloques() {
        assertTrue(ExoticGarden.esDeDosBloques(new Berry(ESENCIA, PlantType.ORE_PLANT, "x")));
        assertTrue(ExoticGarden.esDeDosBloques(new Berry("X", PlantType.DOUBLE_PLANT, "x")));
        assertFalse(ExoticGarden.esDeDosBloques(new Berry("X", PlantType.BUSH, "x")));
        assertFalse(ExoticGarden.esDeDosBloques(new Berry("X", PlantType.FRUIT, "x")));
    }
}
