package chrislo27.galvanize.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

import chrislo27.galvanize.render.block.RenderBlockConnectedSimple;

public class BlockGreyWall extends Block {

	public BlockGreyWall() {
		this.renderBlock = new RenderBlockConnectedSimple(1, 4, true, "greywall_junction",
				"greywall_filled", "greywall_horizontal", "greywall_vertical");
	}

	@Override
	public void getRequiredTextures(ObjectMap<String, Texture> map) {
		map.put("greywall_junction", new Texture("images/blocks/greywall/junction.png"));
		map.put("greywall_filled", new Texture("images/blocks/greywall/filled.png"));
		map.put("greywall_horizontal", new Texture("images/blocks/greywall/horizontal.png"));
		map.put("greywall_vertical", new Texture("images/blocks/greywall/vertical.png"));
	}

}
