package chrislo27.galvanize.render.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.entity.living.EntityPlayer;
import chrislo27.galvanize.world.World;
import ionium.registry.AssetRegistry;
import ionium.templates.Main;

public class RenderPlayer extends EntityRenderer<EntityPlayer> {

	private static final String atlasKey = "entity_player";
	private static final int ROUNDED_RECT_PADDING = 8;
	private static final float TOP_SPACE = (15 - ROUNDED_RECT_PADDING) * (1f / 64f);
	private static final float MIDDLE_SPACE = (18 - ROUNDED_RECT_PADDING) * (1f / 64f);
	private static final float BOTTOM_SPACE = (24 - ROUNDED_RECT_PADDING) * (1f / 64f);
	private static final float MAX_Y_SPACING = (5) * (1f / Block.TILE_SIZE);
	private static final float STRETCH_MULTIPLIER = 1.5f;

	private boolean facingLeft = false;

	private float piecesX = 0;
	private float piecesY = 0;

	public RenderPlayer(EntityPlayer entity) {
		super(entity);
	}

	@Override
	public void render(Batch batch, World world) {
		if (entity.physicsBody.velocity.x != 0) {
			facingLeft = entity.physicsBody.velocity.x < 0;
		}

		// update
		{
			piecesX = MathUtils
					.clamp(piecesX + ((-Math.signum(entity.physicsBody.velocity.x) - piecesX)
							* Gdx.graphics.getDeltaTime() * 8), -1f, 1f);

			piecesY = MathUtils
					.clamp(piecesY + ((-Math.signum(entity.physicsBody.velocity.y) - piecesY)
							* Gdx.graphics.getDeltaTime() * 2), -1f, 0f);
		}

		AtlasRegion emptyHead = AssetRegistry.getAtlasRegion(atlasKey, "headWithoutGears");
		AtlasRegion gears = AssetRegistry.getAtlasRegion(atlasKey, "gears");
		AtlasRegion top = AssetRegistry.getAtlasRegion(atlasKey, "top");
		AtlasRegion middle = AssetRegistry.getAtlasRegion(atlasKey, "middle");
		AtlasRegion bottom = AssetRegistry.getAtlasRegion(atlasKey, "bottom");

		batch.draw(emptyHead, lerpPosition.x, lerpPosition.y, entity.physicsBody.bounds.width,
				entity.physicsBody.bounds.height);

		if (facingLeft && !gears.isFlipX()) gears.flip(true, false);
		batch.draw(gears, lerpPosition.x, lerpPosition.y, entity.physicsBody.bounds.width,
				entity.physicsBody.bounds.height);
		if (facingLeft && gears.isFlipX()) gears.flip(true, false);

		batch.draw(top, lerpPosition.x + (TOP_SPACE * piecesX),
				lerpPosition.y + (MAX_Y_SPACING * piecesY * 1 * STRETCH_MULTIPLIER),
				entity.physicsBody.bounds.width, entity.physicsBody.bounds.height);
		batch.draw(middle, lerpPosition.x + (MIDDLE_SPACE * piecesX),
				lerpPosition.y + (MAX_Y_SPACING * piecesY * 2 * STRETCH_MULTIPLIER),
				entity.physicsBody.bounds.width, entity.physicsBody.bounds.height);
		batch.draw(bottom, lerpPosition.x + (BOTTOM_SPACE * piecesX),
				lerpPosition.y + (MAX_Y_SPACING * piecesY * 3 * STRETCH_MULTIPLIER),
				entity.physicsBody.bounds.width, entity.physicsBody.bounds.height);
	}

}
