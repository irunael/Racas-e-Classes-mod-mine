package com.pedro.racasclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedro.racasclasses.entity.IllusionCloneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class IllusionCloneRenderer extends LivingEntityRenderer<IllusionCloneEntity, PlayerModel<IllusionCloneEntity>> {
    private static final ResourceLocation FALLBACK = ResourceLocation.withDefaultNamespace("textures/entity/steve/wide/steve.png");
    private final PlayerModel<IllusionCloneEntity> wideModel;
    private final PlayerModel<IllusionCloneEntity> slimModel;

    public IllusionCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        wideModel = model;
        slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(IllusionCloneEntity clone, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        AbstractClientPlayer owner = getOwner(clone);
        model = owner != null && owner.getSkin().model() == PlayerSkin.Model.SLIM ? slimModel : wideModel;
        super.render(clone, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(IllusionCloneEntity clone) {
        AbstractClientPlayer owner = getOwner(clone);
        return owner == null ? FALLBACK : owner.getSkin().texture();
    }

    private AbstractClientPlayer getOwner(IllusionCloneEntity clone) {
        if (clone.getOwnerId() == null || Minecraft.getInstance().level == null) return null;
        return Minecraft.getInstance().level.getPlayerByUUID(clone.getOwnerId()) instanceof AbstractClientPlayer player
                ? player : null;
    }
}
