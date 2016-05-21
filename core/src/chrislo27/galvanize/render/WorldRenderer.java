package chrislo27.galvanize.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import chrislo27.galvanize.Main;
import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.entity.Entity;
import chrislo27.galvanize.entity.living.EntityPlayer;
import chrislo27.galvanize.world.World;

public class WorldRenderer implements Disposable {

	public static int extraMargin = 1;

	public OrthographicCamera camera;
	private Vector3 tempVector = new Vector3();

	private FrameBuffer worldBuffer;

	public WorldRenderer() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 20, 11.25f);

		createBuffers();
	}

	protected void updateCamera(World world) {
		EntityPlayer player = world.getPlayer();

		if (player != null) {
			tempVector.set(player.renderer.lerpPosition.x + player.physicsBody.bounds.width * 0.5f,
					player.renderer.lerpPosition.y + player.physicsBody.bounds.height * 0.5f,
					camera.position.z);

			camera.position.interpolate(tempVector, Gdx.graphics.getDeltaTime() * 16,
					Interpolation.pow2Out);
		}

		camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth * 0.5f,
				world.worldWidth - camera.viewportWidth * 0.5f);
		camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight * 0.5f,
				world.worldHeight - camera.viewportHeight * 0.5f);
		camera.update();
	}

	public void render(Main main, Matrix4 oldProjectionMatrix, Batch batch, World world) {
		updateCamera(world);

		batch.setProjectionMatrix(camera.combined);

		int minX = (int) MathUtils.clamp(
				camera.position.x - camera.viewportWidth * 0.5f / camera.zoom - extraMargin, 0,
				world.worldWidth);
		int minY = (int) MathUtils.clamp(
				camera.position.y - camera.viewportHeight * 0.5f / camera.zoom - extraMargin, 0,
				world.worldHeight);
		int maxX = (int) MathUtils.clamp(
				camera.position.x + camera.viewportWidth * 0.5f / camera.zoom + extraMargin, 0,
				world.worldWidth);
		int maxY = (int) MathUtils.clamp(
				camera.position.y + camera.viewportHeight * 0.5f / camera.zoom + extraMargin, 0,
				world.worldHeight);

		renderWorldToBuffer(batch, world, minX, minY, maxX, maxY);

		batch.setProjectionMatrix(oldProjectionMatrix);
		batch.begin();

		// draw world
		batch.draw(worldBuffer.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight(), 0, 0, worldBuffer.getColorBufferTexture().getWidth(),
				worldBuffer.getColorBufferTexture().getHeight(), false, true);

		batch.end();
	}

	public void renderWorldToBuffer(Batch batch, World world, int minX, int minY, int maxX,
			int maxY) {
		worldBuffer.begin();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();

		Block b;
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				b = world.getBlock(x, y);

				if (b == null) continue;
				if (b.getRenderBlock() == null) continue;

				b.getRenderBlock().render(batch, world, x, y);
			}
		}

		batch.flush();

		Entity e;
		for (int i = 0; i < world.entities.size; i++) {
			e = world.entities.get(i);

			if (e.renderer != null) {
				e.renderer.updateLerpPosition();
				e.renderer.render(batch, world);
			}
		}

		batch.end();

		worldBuffer.end();
	}

	public void resize(int width, int height) {
		disposeBuffers();
		createBuffers();
	}

	private void createBuffers() {
		worldBuffer = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight(), false);

		worldBuffer.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}

	private void disposeBuffers() {
		worldBuffer.dispose();
	}

	@Override
	public void dispose() {
		disposeBuffers();
	}

}
