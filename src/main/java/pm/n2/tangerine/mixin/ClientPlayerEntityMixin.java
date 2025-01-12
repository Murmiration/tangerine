package pm.n2.tangerine.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pm.n2.tangerine.Tangerine;
import pm.n2.tangerine.modules.movement.NoSlowModule;
import pm.n2.tangerine.modules.player.AntiHungerModule;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
	// i tried @Inject() into this and ci.cancel() but it caused some weird rubberbanding
	// lol
	@Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0))
	public void tangerine$sendMovementPackets(ClientPlayNetworkHandler instance, Packet<?> packet) {
		if (Tangerine.MODULE_MANAGER.get(AntiHungerModule.class).enabled) return;
		instance.sendPacket(packet);
	}

	@Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
	public void tangerine$noSlow(CallbackInfoReturnable<Boolean> cir) {
		var noSlowMod = (NoSlowModule) Tangerine.MODULE_MANAGER.get(NoSlowModule.class);
		if (noSlowMod.enabled && noSlowMod.affectSneaking.getBooleanValue()) {
			cir.setReturnValue(((ClientPlayerEntity) (Object) this).shouldLeaveSwimmingPose());
		}
	}

	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	public boolean tangerine$noSlow_items(ClientPlayerEntity instance) {
		if (Tangerine.MODULE_MANAGER.get(NoSlowModule.class).enabled) {
			return false;
		}
		return instance.isUsingItem();
	}
}
