package chrislo27.galvanize.render.block;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.registry.Blocks;
import chrislo27.galvanize.world.World;
import ionium.util.MathHelper;

public class RenderBlockGeneric extends BlockRenderer {

	private Array<String> textures = new Array<>();
	public float animationTime = 1;
	public boolean fitInBlockSize = true;

	/**
	 * This amount will be multiplied by the position and added to the "time" value of the animation.
	 * Useful if you want a sweeping effect.
	 */
	public Vector2 timeOffset = new Vector2(0, 0);

	public RenderBlockGeneric(float animationTime, boolean fitInBlockSize, String... textures) {
		this.textures.addAll(textures);
		this.animationTime = animationTime;
		this.fitInBlockSize = fitInBlockSize;
	}

	public RenderBlockGeneric(boolean fitInBlockSize, String texture) {
		this(1, fitInBlockSize, texture);
	}

	public RenderBlockGeneric(String texture) {
		this(true, texture);
	}

	public RenderBlockGeneric(float animationTime, String... textures) {
		this(animationTime, true, textures);
	}

	@Override
	public void render(Batch batch, World world, int x, int y) {
		AtlasRegion region = Blocks.getRegion(getCurrentRegion(x, y));

		drawRegion(batch, region, x, y);
	}

	protected void drawRegion(Batch batch, AtlasRegion region, int x, int y) {
		batch.draw(region, x, y, fitInBlockSize ? 1 : region.getRegionWidth() / Block.TILE_SIZE,
				fitInBlockSize ? 1 : region.getRegionHeight() / Block.TILE_SIZE);
	}

	protected String getCurrentRegion(int x, int y) {
		if (textures.size == 1) return textures.first();

		float percent = MathHelper.getSawtoothWave(System.currentTimeMillis(), animationTime);

		return textures.get(MathUtils.clamp((int) (percent * textures.size), 0, textures.size - 1));
	}

}
