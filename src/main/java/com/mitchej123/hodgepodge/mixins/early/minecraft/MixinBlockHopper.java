package com.mitchej123.hodgepodge.mixins.early.minecraft;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.collect.ImmutableList;

@Mixin(BlockHopper.class)
public class MixinBlockHopper extends Block {

    // Inspired by DietHoppers by rwtema - https://github.com/rwtema/DietHopper/
    @Unique
    private static final EnumMap<EnumFacing, List<AxisAlignedBB>> bounds;

    static {
        List<AxisAlignedBB> commonBounds = ImmutableList
                .of(makeAABB(0, 10, 0, 16, 16, 16), makeAABB(4, 4, 4, 12, 10, 12));
        bounds = Stream.of(EnumFacing.values()).filter(t -> t != EnumFacing.UP).collect(
                Collectors.toMap(
                        a -> a,
                        a -> new ArrayList<>(commonBounds),
                        (u, v) -> { throw new IllegalStateException(); },
                        () -> new EnumMap<>(EnumFacing.class)));

        bounds.get(EnumFacing.DOWN).add(makeAABB(6, 0, 6, 10, 4, 10));

        bounds.get(EnumFacing.NORTH).add(makeAABB(6, 4, 0, 10, 8, 4));
        bounds.get(EnumFacing.SOUTH).add(makeAABB(6, 4, 12, 10, 8, 16));

        bounds.get(EnumFacing.WEST).add(makeAABB(12, 4, 6, 16, 8, 10));
        bounds.get(EnumFacing.EAST).add(makeAABB(0, 4, 6, 4, 8, 10));
    }

    private MixinBlockHopper(Material materialIn) {
        super(materialIn);
    }

    @Unique
    private static AxisAlignedBB makeAABB(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return AxisAlignedBB.of(fromX / 16F, fromY / 16F, fromZ / 16F, toX / 16F, toY / 16F, toZ / 16F);
    }

    @Unique
    private static MovingObjectPosition rayTrace(Vec3 pos, Vec3 start, Vec3 end, AxisAlignedBB boundingBox) {
        final Vec3 vec3d = start.add(-pos.x, -pos.y, -pos.z);
        final Vec3 vec3d1 = end.add(-pos.x, -pos.y, -pos.z);

        final MovingObjectPosition raytraceresult = boundingBox.clip(vec3d, vec3d1);
        if (raytraceresult == null) return null;

        final Vec3 res = raytraceresult.offset.add(pos.x, pos.y, pos.z);
        return new MovingObjectPosition(
                (int) res.x,
                (int) res.y,
                (int) res.z,
                raytraceresult.face,
                pos);
    }

    @Override
    public MovingObjectPosition rayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        final Vec3 pos = Vec3.of(x, y, z);
        final EnumFacing facing = EnumFacing
                .values()[(BlockHopper.getFacing(world.getBlockMetadata(x, y, z)))];
        List<AxisAlignedBB> list = bounds.get(facing);
        if (list == null) return super.rayTrace(world, x, y, z, start, end);
        return list.stream().map(bb -> rayTrace(pos, start, end, bb)).anyMatch(Objects::nonNull)
                ? super.rayTrace(world, x, y, z, start, end)
                : null;
    }
}
