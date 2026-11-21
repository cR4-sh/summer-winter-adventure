package dev.swa.space.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.moltea.unbound.mc.data.DataIn;
import me.moltea.unbound.mc.data.DataOut;
import me.moltea.unbound.mc.data.EntitySaves;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Entity.class, priority = 900, remap = false)
abstract class ConvoyEntitySaveMixin {
    // Epic Fight 21.17.3.1 replaces the nested call in each wrapper. Supply Unbound's
    // context around Epic Fight's handler so both mods can serialize their own data.
    @WrapOperation(method = "saveWithoutId", order = 2000,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void swa$saveEntityData(Entity entity, CompoundTag tag, Operation<Void> original) {
        EntitySaves.save(entity, DataOut.of(tag, entity.level().registryAccess()), () -> original.call(entity, tag));
    }

    @WrapOperation(method = "load", order = 2000,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void swa$readEntityData(Entity entity, CompoundTag tag, Operation<Void> original) {
        EntitySaves.load(entity, DataIn.of(tag, entity.level().registryAccess()), () -> original.call(entity, tag));
    }
}
