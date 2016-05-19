package chrislo27.galvanize.screen;

import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.Main;
import chrislo27.galvanize.render.WorldRenderer;
import chrislo27.galvanize.world.World;
import ionium.screen.Updateable;

public class WorldScreen extends Updateable<Main> {

	World world;
	WorldRenderer renderer;

	public WorldScreen(Main m) {
		super(m);

		world = new World(64, 64);
		renderer = new WorldRenderer();
	}

	@Override
	public void render(float delta) {
		renderer.render(main, main.camera.combined, main.batch, world);
	}

	@Override
	public void renderUpdate() {
	}

	@Override
	public void tickUpdate() {
		world.tickUpdate();
	}

	@Override
	public void getDebugStrings(Array<String> array) {
		if (world == null) {
			array.add("World is null!");
			return;
		}

		array.add("entities: " + world.entities.size);
	}

	@Override
	public void resize(int width, int height) {
		renderer.resize(width, height);
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		renderer.dispose();
	}

}
