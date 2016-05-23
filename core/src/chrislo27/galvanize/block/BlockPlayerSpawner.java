package chrislo27.galvanize.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

import chrislo27.galvanize.entity.living.EntityPlayer;
import chrislo27.galvanize.render.block.RenderBlockGeneric;
import chrislo27.galvanize.world.World;
import ionium.aabbcollision.PhysicsBody;

public class BlockPlayerSpawner extends Block {

	public BlockPlayerSpawner() {
		this.renderBlock = new RenderBlockGeneric("playerSpawner");
	}

	@Override
	public void tickUpdate(World world, int x, int y) {
		super.tickUpdate(world, x, y);

		if (world.getPlayer() == null) {
			EntityPlayer player = new EntityPlayer(world, x, y);

			world.entities.add(player);

			// force update
			world.getPlayer();
		}
	}

	@Override
	public PhysicsBody getPhysicsBody(PhysicsBody body, int x, int y) {
		return null;
	}

	@Override
	public void getRequiredTextures(ObjectMap<String, Texture> map) {
		map.put("playerSpawner", new Texture("images/blocks/playerSpawner.png"));
	}

}
