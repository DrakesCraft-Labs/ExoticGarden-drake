package com.github.drakescraft_labs.exoticgarden;

/**
 * Geometria de la huella que ExoticGarden ocupa dentro de un chunk.
 *
 * <p>Durante {@code ChunkPopulateEvent} solo el chunk que se esta poblando esta garantizado en
 * memoria. Leer o escribir un bloque de un chunk vecino que aun no esta cargado hace que Paper
 * llame a {@code ServerChunkCache.syncLoad} en el hilo principal, que bloquea el tick hasta que
 * la generacion del vecino termina: es lo que disparo el watchdog de 10 s del 2026-09-09.
 *
 * <p>Esta clase es pura aritmetica, sin dependencias de Bukkit, para poder cubrirla con pruebas.
 */
public final class ChunkFootprint {

    /** Lado de un chunk en bloques. */
    public static final int CHUNK_SIZE = 16;

    private ChunkFootprint() {}

    /**
     * @return {@code true} si el bloque pertenece al chunk indicado.
     */
    public static boolean isInsideChunk(int blockX, int blockZ, int chunkX, int chunkZ) {
        return (blockX >> 4) == chunkX && (blockZ >> 4) == chunkZ;
    }

    /**
     * Rango {@code [min, max]} de desplazamientos dentro del chunk en los que puede nacer un arbol
     * sin que el barrido de planitud ni la plantilla toquen un chunk vecino.
     *
     * <p>El pegado recorre {@code blockX} en {@code [locX - length/2, locX - length/2 + width - 1]}
     * y {@code blockZ} en {@code [locZ - width/2, locZ - width/2 + length - 1]}; el barrido de
     * planitud mira {@code [loc, loc + scanSize - 1]} en ambos ejes.
     *
     * @return el rango, o {@code null} si la huella no cabe entera en un chunk.
     */
    public static int[] safeOffsetRange(int width, int length, int scanSize) {
        if (width <= 0 || length <= 0 || scanSize <= 0) {
            return null;
        }

        int min = Math.max(length / 2, width / 2);
        int max = CHUNK_SIZE - 1 - Math.max(Math.max(width - 1 - length / 2, length - 1 - width / 2), scanSize - 1);

        return min > max ? null : new int[] { min, max };
    }
}
