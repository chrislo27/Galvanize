package chrislo27.galvanize.render.block;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.registry.Blocks;
import chrislo27.galvanize.world.World;

public class RenderBlockConnectedSimple extends RenderBlockMultiTexture {

	protected static final int TEX_ID_JUNCTION = 0;
	protected static final int TEX_ID_FILLED = 1;
	protected static final int TEX_ID_HORIZONTAL = 2;
	protected static final int TEX_ID_VERTICAL = 3;

	/**
	 * Four textures of junction, filled, horizontal, vertical.
	 * @param animationTime
	 * @param numOfTex
	 * @param fitInBlockSize
	 * @param textures
	 */
	public RenderBlockConnectedSimple(float animationTime, int numOfTex, boolean fitInBlockSize,
			String... textures) {
		super(animationTime, numOfTex, fitInBlockSize, textures);
	}

	protected int getTextureID(World world, int x, int y) {
		Block block = world.getBlock(x, y);

		boolean horizontal = world.getBlock(x - 1, y) == block && world.getBlock(x + 1, y) == block;
		boolean vertical = world.getBlock(x, y - 1) == block && world.getBlock(x, y + 1) == block;

		if (horizontal && vertical) {
			return TEX_ID_FILLED;
		} else if (horizontal) {
			return TEX_ID_HORIZONTAL;
		} else if (vertical) {
			return TEX_ID_VERTICAL;
		}

		return TEX_ID_JUNCTION;
	}

	@Override
	public void render(Batch batch, World world, int x, int y) {
		AtlasRegion region = Blocks
				.getRegion(textures.get(getCurrentRegion(x, y, getTextureID(world, x, y))));

		drawRegion(batch, region, x, y);
	}

}
