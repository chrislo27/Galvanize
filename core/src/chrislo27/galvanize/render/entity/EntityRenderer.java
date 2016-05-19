package chrislo27.galvanize.render.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import chrislo27.galvanize.Main;
import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.entity.Entity;
import chrislo27.galvanize.world.World;
import ionium.registry.GlobalVariables;
import ionium.util.DebugSetting;

public abstract class EntityRenderer<T extends Entity> {

	protected static final float THRESHOLD = 1f / Block.TILE_SIZE;
	protected static final Vector2 tmpVector = new Vector2();
	protected static final float LERP_SPEED = 8f;
	protected static float tickRate = -1;

	public final T entity;

	public Vector2 lerpPosition = new Vector2();

	public EntityRenderer(T entity) {
		this.entity = entity;

		if (tickRate < 0) tickRate = 1f / GlobalVariables.ticks;

		lerpPosition.set(entity.physicsBody.bounds.x, entity.physicsBody.bounds.y);
	}

	public void updateLerpPosition() {
		lerpPosition.set(entity.lastKnownPosition);
		lerpPosition.lerp(tmpVector.set(entity.physicsBody.bounds.x, entity.physicsBody.bounds.y),
				Main.tickDeltaTime / tickRate);

		if (DebugSetting.debug && Gdx.input.isKeyPressed(Keys.P)) {
			Main.logger.debug(entity.lastKnownPosition + " to " + tmpVector + " with "
					+ (Main.tickDeltaTime / tickRate) + " = " + lerpPosition);
		}
	}

	public abstract void render(Batch batch, World world);

}
