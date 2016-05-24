package chrislo27.galvanize.registry;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.block.BlockGreyWall;
import chrislo27.galvanize.block.BlockPlayerSpawner;
import ionium.util.BiObjectMap;
import ionium.util.render.TextureRegionDebleeder;

public class Blocks {

	private static Blocks instance;

	private Blocks() {
	}

	public static Blocks instance() {
		if (instance == null) {
			instance = new Blocks();
			instance.loadResources();
		}
		return instance;
	}

	private BiObjectMap<String, Block> map = new BiObjectMap<>();

	private TextureAtlas atlas;
	private ObjectMap<String, AtlasRegion> regions = new ObjectMap<>();

	private void loadResources() {
		addBlock("greyWall", new BlockGreyWall());
		addBlock("playerSpawner", new BlockPlayerSpawner());
	}

	public void setAtlas(TextureAtlas a) {
		atlas = a;

		Array<AtlasRegion> allRegions = atlas.getRegions();
		regions.clear();

		TextureRegionDebleeder.fixAmountPx = 0.25f;

		for (AtlasRegion ar : allRegions) {
			//TextureRegionDebleeder.fixBleeding(ar);
			regions.put(ar.name + (ar.name.endsWith("_") ? ar.index : ""), ar);
		}
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	public static AtlasRegion getRegion(String id) {
		return instance().regions.get(id);
	}

	public static Block getBlock(String key) {
		if (key == null) return null;

		return instance().map.getValue(key);
	}

	public static String getKey(Block block) {
		if (block == null) return null;

		return instance().map.getKey(block);
	}

	public Array<Block> getAllBlocks() {
		return map.getAllValues();
	}

	public Array<String> getAllKeys() {
		return map.getAllKeys();
	}

	public static void addBlock(String key, Block block) {
		instance().map.put(key, block);
	}

}
