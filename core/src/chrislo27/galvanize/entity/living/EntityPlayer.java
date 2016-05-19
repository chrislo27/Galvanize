package chrislo27.galvanize.entity.living;

import chrislo27.galvanize.render.entity.RenderPlayer;
import chrislo27.galvanize.world.World;
import ionium.util.MathHelper;

public class EntityPlayer extends EntityLiving {

	public EntityPlayer(World world, float x, float y) {
		super(world, x, y, 2, 2);

		this.renderer = new RenderPlayer(this);
		this.maxSpeed.set(10, 10);
		this.accSpeed.set(maxSpeed.x * 5, maxSpeed.y * 5);
		this.jumpHeight = MathHelper.getJumpVelo(Math.abs(world.gravity.y),
				2f + physicsBody.bounds.height);
	}

}
