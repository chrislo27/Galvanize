package chrislo27.galvanize.block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

import chrislo27.galvanize.render.block.BlockRenderer;
import ionium.aabbcollision.PhysicsBody;

public abstract class Block {

	public static final int TILE_SIZE = 32;

	protected BlockRenderer renderBlock;
	protected Color mapColor = new Color(1, 1, 1, 1);
	public float friction = 1;

	public Block() {

	}

	public void tickUpdate() {

	}

	public BlockRenderer getRenderBlock() {
		return renderBlock;
	}

	public Color getMapColor(Color foliage) {
		return mapColor;
	}

	public PhysicsBody getPhysicsBody(PhysicsBody body, int x, int y) {
		return body.setBounds(x, y, 1, 1);
	}

	public abstract void getRequiredTextures(ObjectMap<String, Texture> map);

}
