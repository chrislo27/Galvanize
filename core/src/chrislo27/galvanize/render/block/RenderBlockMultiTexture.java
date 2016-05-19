package chrislo27.galvanize.render.block;

import com.badlogic.gdx.graphics.g2d.Batch;

import chrislo27.galvanize.world.World;

public abstract class RenderBlockMultiTexture extends BlockRenderer {

	protected int numberOfTextures = 1;

	/**
	 * The types of textures you need are interleaved
	 * @param animationTime
	 * @param fitInBlockSize
	 * @param textures
	 */
	public RenderBlockMultiTexture(float animationTime, int numOfTex, boolean fitInBlockSize,
			String... textures) {
		super(animationTime, fitInBlockSize, textures);

		numberOfTextures = numOfTex;
	}

	/**
	 * Gets the region for the certain texture ID (the interleaved textures)
	 * @param x
	 * @param y
	 * @param texId
	 * @return
	 */
	protected int getCurrentRegion(int x, int y, int texId) {
		return (getCurrentRegion(x, y) / numberOfTextures) + texId;
	}

}
