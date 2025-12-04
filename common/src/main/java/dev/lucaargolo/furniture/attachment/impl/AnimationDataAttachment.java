package dev.lucaargolo.furniture.attachment.impl;

import com.mojang.math.Axis;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.utils.Animation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Matrix4f;

import java.util.*;

public class AnimationDataAttachment implements DataAttachment<AnimationDataAttachment> {

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationDataAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.collection(ArrayList::new, Animation.STREAM_CODEC)),
            AnimationDataAttachment::get,
            AnimationDataAttachment::new
    );

    private final Map<String, List<Animation>> data;

    public AnimationDataAttachment(Map<String, List<Animation>> data) {
        this.data = new HashMap<>(data);
    }

    private Map<String, List<Animation>> get() {
        return this.data;
    }

    public AnimationDataAttachment add(String group, Animation animation) {
        List<Animation> list = data.computeIfAbsent(group, g -> new ArrayList<>());
        list.add(animation);
        this.data.put(group, list);
        return this;
    }

    public void tick(Object target) {
        Iterator<Map.Entry<String, List<Animation>>> iterator = this.data.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Animation>> entry = iterator.next();
            entry.getValue().removeIf(Animation::tick);
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
        if(this.data.isEmpty()) {
            ModDataAttachments.ANIMATION_DATA.set(target, null);
        }
    }

    public Matrix4f animate(String group, Matrix4f matrix) {
        List<Animation> list = this.data.getOrDefault(group, List.of());
        for(Animation animation : list) {
            Animation.Type type = animation.type();
            matrix = switch (type) {
                case ROTATE_X -> matrix.rotate(Axis.XP.rotationDegrees(animation.value()));
                case ROTATE_Y -> matrix.rotate(Axis.YP.rotationDegrees(animation.value()));
                case ROTATE_Z -> matrix.rotate(Axis.ZP.rotationDegrees(animation.value()));
                case SCALE_X -> matrix.scale(animation.value(), 1f, 1f);
                case SCALE_Y -> matrix.scale(1f, animation.value(), 1f);
                case SCALE_Z -> matrix.scale(1f, 1f, animation.value());
                case POSITION_X -> matrix.translate(animation.value(), 0f, 0f);
                case POSITION_Y -> matrix.translate(0f, animation.value(), 0f);
                case POSITION_Z -> matrix.translate(0f, 0f, animation.value());
            };
        }
        return matrix;
    }
    @Override
    public DataAttachmentType<AnimationDataAttachment> getType() {
        return ModDataAttachments.ANIMATION_DATA;
    }

}
