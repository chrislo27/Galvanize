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

	public RenderBlockGeneric(float animationTime, boolean fitInBlockSize, String... textures) {
		super(animationTime, fitInBlockSize, textures);
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
		AtlasRegion region = Blocks.getRegion(textures.get(getCurrentRegion(x, y)));

		drawRegion(batch, region, x, y);
	}

}
