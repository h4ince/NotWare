package me.notme.notware.utils.elytra;

import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;

public class RenderPath {
    public static String[] status;
    public static ArrayList<Color> rectangles = new ArrayList();
    public static ArrayList<BlockPos> path = new ArrayList();
    public static Color color;

    public static void setPath(ArrayList<BlockPos> path, Color color) {
        RenderPath.path = path;
        RenderPath.color = color;
    }

    public static void clearPath() {
        path.clear();
    }
}
