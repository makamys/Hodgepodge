package com.mitchej123.hodgepodge.mixins.hooks;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagList;

import thaumcraft.common.entities.golems.Marker;

public class ThaumcraftMixinMethods {

    public static ArrayList<Marker> overwriteMarkersDimID(NBTTagList nbtTagList, ArrayList<Marker> markers) {
        for (int i = 0; i < nbtTagList.size(); i++) {
            markers.get(i).dim = nbtTagList.getCompound(i).getInt("dim");
        }
        return markers;
    }

}
