package chrislo27.galvanize.render.block;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.registry.Blocks;
import chrislo27.galvanize.world.World;
import ionium.templates.Main;
import ionium.util.MathHelper;

public abstract class BlockRenderer {

	protected Array<String> textures = new Array<>();
	public float animationTime = 1;
	public boolean fitInBlockSize = true;

	/**
	 * This amount will be multiplied by the position and added to the "time" value of the animation.
	 * Useful if you want a sweeping effect.
	 */
	public Vector2 timeOffset = new Vector2(0, 0);

	public BlockRenderer(float animationTime, boolean fitInBlockSize, String... textures) {
		this.textures.addAll(textures);
		this.animationTime = animationTime;
		this.fitInBlockSize = fitInBlockSize;
	}

	public BlockRenderer(boolean fitInBlockSize, String texture) {
		this(1, fitInBlockSize, texture);
	}

	public BlockRenderer(String texture) {
		this(true, texture);
	}

	public BlockRenderer(float animationTime, String... textures) {
		this(animationTime, true, textures);
	}

	public void drawRegion(Batch batch, AtlasRegion region, int x, int y) {
		batch.draw(region, x, y, fitInBlockSize ? 1 : region.getRegionWidth() / Block.TILE_SIZE,
				fitInBlockSize ? 1 : region.getRegionHeight() / Block.TILE_SIZE);
	}

	public int getCurrentRegion(int x, int y) {
		if (textures.size == 1) return 0;

		float offset = (timeOffset.x * y * 1000) + (timeOffset.y * y * 1000);
		float percent = MathHelper.getSawtoothWave(System.currentTimeMillis() + ((long) offset),
				animationTime);

		return MathUtils.clamp((int) (percent * textures.size), 0, textures.size - 1);
	}

	public Array<String> getAllTextures() {
		return textures;
	}

	public abstract void render(Batch batch, World world, int x, int y);

}
