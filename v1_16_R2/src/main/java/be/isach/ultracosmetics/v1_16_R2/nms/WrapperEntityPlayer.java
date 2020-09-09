package be.isach.ultracosmetics.v1_16_R2.nms;

import net.minecraft.server.v1_16_R2.EntityPlayer;

/**
 * @author RadBuilder
 */
public class WrapperEntityPlayer extends WrapperEntityHuman {

    protected EntityPlayer handle;

    public WrapperEntityPlayer(EntityPlayer handle) {
        super(handle);

        this.handle = handle;
    }

    @Override
    public EntityPlayer getHandle() {
        return handle;
    }

}
