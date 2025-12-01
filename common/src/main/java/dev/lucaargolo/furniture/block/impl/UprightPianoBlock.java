package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.behaviour.Behaviour;
import dev.lucaargolo.furniture.block.behaviour.PianoBehaviour;
import dev.lucaargolo.furniture.sound.ModSounds;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class UprightPianoBlock extends FurnitureBlock {

    public UprightPianoBlock(Block base, VoxelShape[] shapes) {
        super(base, shapes, createBehaviours());
    }

    private static Behaviour<?>[] createBehaviours() {
        Behaviour<?>[] behaviours = new Behaviour[88];
        Vec3 whiteKey = new Vec3(12.75/16.0, 4.0/16.0, -6.5/16.0);
        Vec3 blackKey = new Vec3(12.5/16.0, 4.25/16.0, -5.5/16.0);
        for (int index = 0; index < 88; index++) {
            if (isWhiteKey(index)) {
                behaviours[index] = new PianoBehaviour(whiteKey.add(-(whiteKeyOffset(index) * 0.5)/16.0, 0.0, 0.0), ModSounds.PIANO, 21+index);
            } else {
                behaviours[index] = new PianoBehaviour(blackKey.add(-(blackKeyOffset(index) * 0.5)/16.0 + 0.25/16.0, 0.0, 0.0), ModSounds.PIANO, 21+index);
            }
        }
        return behaviours;
    }

    private static boolean isWhiteKey(int index) {
        int note = (index + 9) % 12;
        return switch (note) {
            case 1, 3, 6, 8, 10 -> false;
            default -> true;
        };
    }

    private static double whiteKeyOffset(int index) {
        int offset = 0;
        for (int k = 0; k < index; k++) {
            if (isWhiteKey(k)) offset++;
        }
        return offset;
    }

    private static double blackKeyOffset(int index) {
        double whiteKeyOffset = whiteKeyOffset(index-1);
        return whiteKeyOffset + 0.5;
    }

}
