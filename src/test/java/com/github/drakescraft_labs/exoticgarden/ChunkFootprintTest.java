package com.github.drakescraft_labs.exoticgarden;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regresion del watchdog de 10 s del 2026-09-09 (ticket #403): durante ChunkPopulateEvent,
 * isFlat leia columnas del chunk vecino y forzaba ServerChunkCache.syncLoad en el hilo principal.
 */
class ChunkFootprintTest {

    private static final int SCAN = 5;

    @Test
    @DisplayName("el rango seguro deja la huella completa dentro del chunk")
    void rangoSeguroMantieneLaHuellaDentroDelChunk() {
        for (int width = 1; width <= 15; width++) {
            for (int length = 1; length <= 15; length++) {
                int[] range = ChunkFootprint.safeOffsetRange(width, length, SCAN);
                if (range == null) continue;

                for (int offset = range[0]; offset <= range[1]; offset++) {
                    // barrido de planitud: [offset, offset + SCAN - 1] en ambos ejes
                    assertTrue(offset + SCAN - 1 <= 15,
                        "el barrido se sale del chunk con width=" + width + " length=" + length + " offset=" + offset);

                    // pegado de la plantilla: X recorre width desde -length/2, Z recorre length desde -width/2
                    assertTrue(offset - length / 2 >= 0 && offset - length / 2 + width - 1 <= 15,
                        "la plantilla se sale en X con width=" + width + " length=" + length + " offset=" + offset);
                    assertTrue(offset - width / 2 >= 0 && offset - width / 2 + length - 1 <= 15,
                        "la plantilla se sale en Z con width=" + width + " length=" + length + " offset=" + offset);
                }
            }
        }
    }

    @Test
    @DisplayName("una plantilla de 7x7 admite offsets 3..11")
    void plantillaDeSieteAdmiteElRangoCentral() {
        int[] range = ChunkFootprint.safeOffsetRange(7, 7, SCAN);

        assertNotNull(range);
        assertTrue(range[0] == 3 && range[1] == 11, "rango inesperado: " + range[0] + ".." + range[1]);
    }

    @Test
    @DisplayName("una plantilla mas ancha que un chunk no genera candidato")
    void plantillaDemasiadoGrandeNoGeneraCandidato() {
        assertNull(ChunkFootprint.safeOffsetRange(40, 40, SCAN));
        assertNull(ChunkFootprint.safeOffsetRange(0, 7, SCAN));
        assertNull(ChunkFootprint.safeOffsetRange(7, 7, 0));
    }

    @Test
    @DisplayName("isInsideChunk resuelve bien las coordenadas negativas del incidente")
    void isInsideChunkResuelveCoordenadasNegativas() {
        // chunk del thread dump: (-263507, -516577) en 'world'
        int chunkX = -263507;
        int chunkZ = -516577;
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        assertTrue(ChunkFootprint.isInsideChunk(baseX, baseZ, chunkX, chunkZ));
        assertTrue(ChunkFootprint.isInsideChunk(baseX + 15, baseZ + 15, chunkX, chunkZ));
        assertFalse(ChunkFootprint.isInsideChunk(baseX + 16, baseZ, chunkX, chunkZ));
        assertFalse(ChunkFootprint.isInsideChunk(baseX - 1, baseZ, chunkX, chunkZ));
        assertFalse(ChunkFootprint.isInsideChunk(baseX, baseZ + 16, chunkX, chunkZ));
    }

    @Test
    @DisplayName("el candidato del bug original se salia del chunk")
    void elCandidatoOriginalSeSaliaDelChunk() {
        // antes se sorteaba nextInt(16): un offset 12..15 hacia que isFlat leyera el vecino
        int chunkX = -263507;
        int chunkZ = -516577;

        assertFalse(ChunkFootprint.isInsideChunk(chunkX * 16 + 15 + (SCAN - 1), chunkZ * 16, chunkX, chunkZ));

        int[] range = ChunkFootprint.safeOffsetRange(7, 7, SCAN);
        assertNotNull(range);
        assertTrue(range[1] < 12, "el rango seguro no debe admitir offsets que el bug si admitia");
    }

    @Test
    @DisplayName("las 11 plantillas reales del plugin siguen teniendo candidato")
    void todasLasPlantillasRealesTienenCandidato() {
        // Width x Length de src/main/resources/schematics: si una plantilla creciera hasta no
        // caber en un chunk, ese arbol dejaria de generarse en silencio.
        int[][] plantillas = { { 11, 11 }, { 13, 13 }, { 5, 5 }, { 9, 9 }, { 7, 7 },
                               { 5, 5 }, { 9, 9 }, { 8, 8 }, { 11, 11 }, { 5, 5 }, { 5, 5 } };

        for (int[] plantilla : plantillas) {
            int[] range = ChunkFootprint.safeOffsetRange(plantilla[0], plantilla[1], SCAN);
            assertNotNull(range, "sin candidato para " + plantilla[0] + "x" + plantilla[1]);
            assertTrue(range[0] <= range[1]);
        }
    }
}
