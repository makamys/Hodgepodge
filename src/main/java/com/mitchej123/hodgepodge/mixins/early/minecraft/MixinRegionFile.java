package com.mitchej123.hodgepodge.mixins.early.minecraft;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.storage.RegionFile;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.mitchej123.hodgepodge.Common;

import cpw.mods.fml.common.FMLLog;

/**
 * Credit to Spigot/ <a href=
 * "https://github.com/GTNewHorizons/Thermos/blob/ab8a329b10f0857cb246e5d791ede8e1f7419c63/patches/net/minecraft/world/chunk/storage/RegionFile.java.patch">Thermos</a>/
 * <a href=
 * "https://github.com/CrucibleMC/Crucible/blob/81fcb90bad9eecf57d09139b49df81d65f59b36d/patches/net/minecraft/world/chunk/storage/RegionFile.java.patch">Crucible</a>
 * for the increased chunks size patch.
 * <p>
 * Allows saving chunks of any size up to INT_MAX instead of 256 Anvil sectors. When the count bitfield overflows 255,
 * 255 is saved, and the real count is stored as the first 4 bytes of the chunk data - this allows for reading and
 * saving much larger chunk sizes.
 */
@Mixin(RegionFile.class)
public abstract class MixinRegionFile {

    @Unique
    private static final int SECTOR_LENGTH = 4096;
    @Unique
    private static final long SECTOR_LLENGTH = SECTOR_LENGTH;
    @Unique
    private static final int SECTOR_MASK = SECTOR_LENGTH - 1;

    @Shadow
    @Final
    private static byte[] BLOCK_BUFFER;

    @Shadow
    private RandomAccessFile randomAccessFile;

    @Shadow
    @Final
    private int[] chunkBlockInfo;

    @Shadow
    @Final
    private int[] chunkSaveTimes;

    @Shadow
    private ArrayList<Boolean> blockEmptyFlags;

    @Shadow
    private int bytesWritten;

    @Shadow
    private long lastModifiedTime;

    @Shadow
    protected abstract int getChunkBlockInfo(int x, int z);

    @Shadow
    public abstract boolean hasChunkData(int x, int z);

    @Shadow
    protected abstract void writeChunkSaveTime(int chunkX, int chunkZ, int newTimestamp) throws IOException;

    @Shadow
    protected abstract void writeChunkBlockInfo(int chunkX, int chunkZ, int newOffset) throws IOException;

    @Shadow
    protected abstract void writeChunkData(int sector, byte[] data, int length) throws IOException;

    @Unique
    final private IOException hodgepodge$magicException = new IOException("cancel the constructor");

    /**
     * Basically an overwrite for the RegionFile constructor, in order to allow chunks bigger than 256 sectors.
     * 
     * @author eigenraven
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;exists()Z", ordinal = 0))
    private void hodgepodge$initRedirect(File fileName, CallbackInfo ci) throws IOException {
        this.bytesWritten = 0;
        try {
            if (fileName.exists()) {
                this.lastModifiedTime = fileName.lastModified();
            }

            randomAccessFile = new RandomAccessFile(fileName, "rw");

            if (randomAccessFile.length() < SECTOR_LLENGTH) {
                randomAccessFile.write(BLOCK_BUFFER);
                randomAccessFile.write(BLOCK_BUFFER);
                this.bytesWritten += 2 * SECTOR_LENGTH;
            }

            // Size padding
            if ((randomAccessFile.length() & SECTOR_MASK) != 0L) {
                final int oversize = (int) (randomAccessFile.length() & SECTOR_MASK);
                final int missing = SECTOR_LENGTH - oversize;
                randomAccessFile.write(BLOCK_BUFFER, 0, missing);
            }

            int numSectors = (int) (randomAccessFile.length() / SECTOR_LLENGTH);
            this.blockEmptyFlags = new ArrayList<>(numSectors);

            for (int i = 0; i < numSectors; ++i) {
                this.blockEmptyFlags.add(true);
            }

            this.blockEmptyFlags.set(0, false);
            this.blockEmptyFlags.set(1, false);
            randomAccessFile.seek(0L);

            // Read the chunk offsets table
            for (int i = 0; i < 1024; ++i) {
                final int offset = randomAccessFile.readInt();
                this.chunkBlockInfo[i] = offset;
                int length = offset & 0xFF;
                // Hodgepodge: max length => read real length from the chunk data
                if (length == 255) {
                    // if sector pointer is valid
                    if ((offset >> 8) <= this.blockEmptyFlags.size()) {
                        // seek to the sector pointer
                        randomAccessFile.seek((offset >> 8) * SECTOR_LLENGTH);
                        length = (randomAccessFile.readInt() + 4) / SECTOR_LENGTH + 1;
                        randomAccessFile.seek(i * 4 + 4); // seek back to where we were
                    }
                }
                // Hodgepodge: use recalculated length
                if (offset != 0 && (offset >> 8) + length <= this.blockEmptyFlags.size()) {
                    for (int l = 0; l < length; ++l) {
                        this.blockEmptyFlags.set((offset >> 8) + l, false);
                    }
                } else if (length > 0) {
                    FMLLog.warning(
                            "Invalid chunk: (%s, %s) Offset: %s Length: %s runs off end file. %s",
                            i % 32,
                            i / 32,
                            offset >> 8,
                            length,
                            fileName);
                }
            }
            // Read the chunk timestamps table
            for (int i = 0; i < 1024; ++i) {
                int timestamp = randomAccessFile.readInt();
                this.chunkSaveTimes[i] = timestamp;
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        throw hodgepodge$magicException;
    }

    /** Allow early exit from the constructor */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/IOException;printStackTrace()V"))
    private void hodgepodge$ignoreMagicException(IOException e) {
        if (e != hodgepodge$magicException) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * @author eigenraven
     * @reason Reuse an existing implementation for simplicity
     */
    @Overwrite(remap = false) // forge method
    public synchronized boolean chunkExists(int x, int z) {
        return this.hasChunkData(x, z);
    }

    @ModifyVariable(
            method = "Lnet/minecraft/world/chunk/storage/RegionFile;getChunkDataInputStream(II)Ljava/io/DataInputStream;",
            at = @At("STORE"),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/chunk/storage/RegionFile;getChunkBlockInfo(II)I"),
                    to = @At(value = "INVOKE", target = "Ljava/util/ArrayList;size()I")),
            index = 5)
    private int hodgepodge$getChunkDataInputStream$extendedSectorCount(int sectorCount, @Local(index = 4) int sector)
            throws IOException {
        if (sectorCount == 255) {
            randomAccessFile.seek(sector * SECTOR_LLENGTH);
            sectorCount = (randomAccessFile.readInt() + 4) / SECTOR_LENGTH + 1;
        }
        return sectorCount;
    }

    @ModifyArg(
            method = "getChunkDataInputStream",
            at = @At(value = "INVOKE", target = "Ljava/io/DataInputStream;<init>(Ljava/io/InputStream;)V"),
            expect = 2)
    private InputStream hodgepodge$getChunkDataInputStream$buffer(InputStream is) {
        // Spigot: use a BufferedInputStream to improve file read performance
        return new BufferedInputStream(is);
    }

    @ModifyArg(
            method = "getChunkDataOutputStream",
            at = @At(value = "INVOKE", target = "Ljava/io/DataOutputStream;<init>(Ljava/io/OutputStream;)V"),
            index = 0)
    private OutputStream hodgepodge$getChunkDataOutputStream$buffer(OutputStream os) {
        // Spigot: use a BufferedOutputStream to greatly improve file write performance
        return new BufferedOutputStream(os);
    }

    /**
     * @author eigenraven
     * @reason Significant logic changes to support oversized chunk allocation
     */
    @Overwrite
    protected synchronized void writeChunkData(int x, int z, byte[] data, int length) {
        try {
            int offset = this.getChunkBlockInfo(x, z);
            int sector = offset >> 8;
            int sectorCount = offset & 255;
            // Spigot start
            if (sectorCount == 255) {
                this.randomAccessFile.seek(sector * SECTOR_LLENGTH);
                sectorCount = (this.randomAccessFile.readInt() + 4) / 4096 + 1;
            }
            // Spigot end
            int sectorsNeeded = (length + 5) / 4096 + 1;

            // Spigot
            if (sectorsNeeded >= 256) {
                // crucible - info: chunk has a limit of 255 sectors
                Common.log.warn(
                        "[Hodgepodge] Oversized Chunk at ({}, {}) - {} bytes used",
                        x,
                        z,
                        sectorsNeeded * SECTOR_LLENGTH);
            }

            if (sector != 0 && sectorCount == sectorsNeeded) { // fits perfectly in the existing allocation
                // crucible - info: this part just overwrite the current old sectors.
                this.writeChunkData(sector, data, length);
            } else { // reallocate
                for (int i = 0; i < sectorCount; ++i) {
                    this.blockEmptyFlags.set(sector + i, true);
                }

                int sectorStart = this.blockEmptyFlags.indexOf(true);
                int sectorLength = 0;

                // crucible - info: search for an area with enough free space
                if (sectorStart != -1) {
                    for (int i = sectorStart; i < this.blockEmptyFlags.size(); ++i) {
                        if (sectorLength != 0) {
                            if (this.blockEmptyFlags.get(i)) {
                                ++sectorLength;
                            } else {
                                sectorLength = 0;
                            }
                        } else if (this.blockEmptyFlags.get(i)) {
                            sectorStart = i;
                            sectorLength = 1;
                        }

                        if (sectorLength >= sectorsNeeded) {
                            break;
                        }
                    }
                }

                if (sectorLength >= sectorsNeeded) {
                    // crucible - info: space found.
                    sector = sectorStart;
                    // Spigot
                    this.writeChunkBlockInfo(x, z, sector << 8 | Math.min(sectorsNeeded, 255));

                    for (int i = 0; i < sectorsNeeded; ++i) {
                        this.blockEmptyFlags.set(sector + i, false);
                    }

                    this.writeChunkData(sector, data, length);
                } else {
                    // crucible - info: space nof found, grow the file.
                    this.randomAccessFile.seek(this.randomAccessFile.length());
                    sector = this.blockEmptyFlags.size();

                    for (int i = 0; i < sectorsNeeded; ++i) {
                        this.randomAccessFile.write(BLOCK_BUFFER);
                        this.blockEmptyFlags.add(false);
                    }

                    this.bytesWritten += 4096 * sectorsNeeded;
                    this.writeChunkData(sector, data, length);
                    this.writeChunkBlockInfo(x, z, sector << 8 | Math.min(sectorsNeeded, 255)); // Spigot
                }
            }

            this.writeChunkSaveTime(x, z, (int) (MinecraftServer.getTimeMillis() / 1000L));
        } catch (IOException ioexception) {
            ioexception.printStackTrace(System.err);
        }
    }

}
