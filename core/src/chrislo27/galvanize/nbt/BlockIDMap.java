package chrislo27.galvanize.nbt;

import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.registry.Blocks;

public class BlockIDMap extends IDMap {

	public BlockIDMap(String tagName) {
		super(tagName);

		this.ticker = 1;

		Array<String> blockIds = Blocks.instance().getAllKeys();

		for (String key : blockIds) {
			this.add(key);
		}
	}

}
