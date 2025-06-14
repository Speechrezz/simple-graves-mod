package speechrezz.simplegraves.entity.custom;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import speechrezz.simplegraves.entity.ModBlockEntities;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class GravestoneBlockEntity extends BlockEntity {
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(50, ItemStack.EMPTY);
    private int xp = 0;
    private GameProfile graveOwner = null;

    public GravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COUNTER_BLOCK_ENTITY, pos, state);
    }

    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public void setItems(DefaultedList<ItemStack> items) {
        this.items = items;
    }

    public GameProfile getGraveOwner() {
        return graveOwner;
    }

    public void setGraveOwner(GameProfile gameProfile) {
        this.graveOwner = gameProfile;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.put("Items", Inventories.writeNbt(new NbtCompound(), this.items, registryLookup));
        nbt.putInt("XP", xp);
        if(graveOwner != null) {
            nbt.putString("GraveOwnerUUID", graveOwner.getId().toString());
            nbt.putString("GraveOwnerName", graveOwner.getName());
        }

        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        Inventories.readNbt(nbt.getCompoundOrEmpty("Items"), this.items, registryLookup);
        xp = nbt.getInt("XP", 0);

        if (nbt.contains("GraveOwnerUUID") && nbt.contains("GraveOwnerName")) {
            UUID id = UUID.fromString(nbt.getString("GraveOwnerUUID", ""));
            String name = nbt.getString("GraveOwnerName", "");
            graveOwner = new GameProfile(id, name);
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
