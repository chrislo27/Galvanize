package chrislo27.galvanize.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.Main;
import chrislo27.galvanize.registry.Blocks;
import chrislo27.galvanize.render.WorldRenderer;
import chrislo27.galvanize.world.World;
import ionium.registry.AssetRegistry;
import ionium.screen.Updateable;
import ionium.stage.Actor;
import ionium.stage.Group;
import ionium.stage.Stage;
import ionium.stage.ui.ImageButton;
import ionium.stage.ui.LocalizationStrategy;
import ionium.stage.ui.TextLabel;
import ionium.stage.ui.skin.Palette;
import ionium.stage.ui.skin.Palettes;
import ionium.util.i18n.Localization;

public class LevelEditorScreen extends Updateable<Main> {

	World world;
	WorldRenderer renderer;

	Stage stage;
	Group group;
	ImageButton toggleTaskbar;
	ImageButton playButton;
	ImageButton pauseButton;
	ImageButton stopButton;
	TextLabel infoText;

	boolean isTesting = false;
	boolean paused = false;

	public LevelEditorScreen(Main m) {
		super(m);

		renderer = new WorldRenderer();
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public void setTesting(boolean test) {
		test &= canTest();

		isTesting = test;
		setPaused(false);

		if (test) {
			stopButton.setEnabled(true);
		} else {
			if (world != null) world.clearAllEntities();
			stopButton.setEnabled(false);
			pauseButton.setEnabled(false);
			playButton.setEnabled(true);
		}
	}

	public void setPaused(boolean p) {
		paused = p;

		if (paused) {
			pauseButton.setEnabled(false);
			playButton.setEnabled(true);
		} else {
			pauseButton.setEnabled(true);
			playButton.setEnabled(false);
		}
	}

	public boolean canTest() {
		return world != null && world.doesWorldContain(Blocks.getBlock("playerSpawner"));
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (world != null) {
			renderer.render(main, main.camera.combined, main.batch, world);
		} else {
			main.batch.setProjectionMatrix(main.camera.combined);

			main.batch.begin();

			main.fontBordered.setColor(1, 1, 1, 1);

			main.fontBordered.draw(main.batch, Localization.get("levelEditor.noWorld"),
					Gdx.graphics.getWidth() * 0.125f,
					Gdx.graphics.getHeight() * 0.5f + main.fontBordered.getLineHeight() * 0.5f,
					Gdx.graphics.getWidth() * 0.75f, Align.center, true);

			main.batch.end();
		}

		if (stage != null) stage.render(main.batch);
	}

	@Override
	public void renderUpdate() {
		if (world != null) {
			if (isTesting) {
				world.inputUpdate();
			}
		}
	}

	@Override
	public void tickUpdate() {
		if (world != null && isTesting && !paused) world.tickUpdate();
	}

	private void generateStage() {
		stage = new Stage();
		final Palette p = Palettes.getIoniumDefault(main.font, main.fontBordered);

		group = new Group(stage) {

			private float otherAlpha = 0;
			private final float alphaMin = 0.25f;
			private final float alphaFadeSpeed = 0.25f;

			@Override
			public void render(SpriteBatch batch, float alpha) {
				boolean isMouseOn = stage.isMouseOver(this);

				if (isMouseOn) {
					if (otherAlpha < 1) {
						otherAlpha = Math.min(1,
								otherAlpha + Gdx.graphics.getDeltaTime() / alphaFadeSpeed);
					}
				} else {
					if (otherAlpha > 0) {
						otherAlpha = Math.max(0,
								otherAlpha - Gdx.graphics.getDeltaTime() / alphaFadeSpeed);
					}
				}

				super.render(batch, alpha * MathUtils.lerp(alphaMin, 1, otherAlpha));
			}

		};
		group.align(Align.bottom | Align.center).setScreenOffset(0, 0, 1, 0).setPixelOffsetSize(0,
				128 + 32);

		{
			Actor bg = new Actor(stage) {

				@Override
				public void render(SpriteBatch batch, float alpha) {
					batch.setColor(p.backgroundColor.r * 1.05f, p.backgroundColor.g * 1.05f,
							p.backgroundColor.b * 1.05f, p.backgroundColor.a * alpha);
					Main.fillRect(batch, getX(), getY(), getWidth(), getHeight());
					batch.setColor(1, 1, 1, 1);
				}
			};

			group.addActor(bg).align(Align.bottomLeft).setScreenOffset(0, 0, 1, 0).setPixelOffset(0,
					0, 0, 128);
		}

		infoText = new TextLabel(stage, Palettes.getIoniumDefault(main.font, main.font), null);
		infoText.setI10NStrategy(new LocalizationStrategy() {

			@Override
			public String get(String key, Object... params) {
				if (key == null) return "";

				return super.get(key, params);
			}
		});
		infoText.setTextAlign(Align.left | Align.bottom);
		infoText.getColor().set(0, 0, 0, 1);
		infoText.align(Align.left | Align.bottom).setScreenOffset(0, 0, 1, 1).setPixelOffset(16, 16,
				-8, 0);

		group.addActor(infoText);

		toggleTaskbar = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "arrow_down")) {

			private float percentage = 1;
			private boolean hidden = false;
			private final float moveTime = 0.25f;

			@Override
			public void render(SpriteBatch batch, float alpha) {
				super.render(batch, alpha * 0.25f + 0.75f);

				if (hidden) {
					if (percentage > 0) percentage = Math.max(0,
							percentage - Gdx.graphics.getDeltaTime() / moveTime);
				} else {
					if (percentage < 1) percentage = Math.min(1,
							percentage + Gdx.graphics.getDeltaTime() / moveTime);
				}

				group.setPixelOffset(0, percentage * 128 - 128);
			}

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);

				if (hidden) {
					this.setTextureRegion(
							AssetRegistry.getAtlasRegion("ionium_ui-icons", "arrow_down"));

					hidden = false;
				} else {
					this.setTextureRegion(
							AssetRegistry.getAtlasRegion("ionium_ui-icons", "arrow_up"));

					hidden = true;
				}
			}

		};
		toggleTaskbar.align(Align.bottomRight).setPixelOffset(0, 128, 128, 32);
		toggleTaskbar.getColor().set(0.25f, 0.25f, 0.25f, 1);
		group.addActor(toggleTaskbar);

		playButton = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "play")) {

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);
			}

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe) infoText.setLocalizationKey("levelEditor.infoText.play");

					wasMouseOnMe = true;
				}
			}

		};
		playButton.getColor().set(0, 0.5f, 0.055f, 1);
		playButton.align(Align.right | Align.top).setScreenOffset(0, 0, 0, 0)
				.setPixelOffset(4 + 36 * 2, 36, 32, 32);
		group.addActor(playButton);

		pauseButton = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "pause")) {

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);
			}

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe) infoText.setLocalizationKey("levelEditor.infoText.pause");

					wasMouseOnMe = true;
				}
			}

		};
		pauseButton.getColor().set(0.75f, 0.75f, 0.25f, 1);
		pauseButton.align(Align.right | Align.top).setScreenOffset(0, 0, 0, 0)
				.setPixelOffset(4 + 36, 36, 32, 32);
		group.addActor(pauseButton);

		stopButton = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "stop")) {

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);
			}

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe) infoText.setLocalizationKey("levelEditor.infoText.stop");

					wasMouseOnMe = true;
				}
			}

		};
		stopButton.getColor().set(242 / 255f, 0.0525f, 0.0525f, 1);
		stopButton.align(Align.right | Align.top).setScreenOffset(0, 0, 0, 0).setPixelOffset(4, 36,
				32, 32);
		group.addActor(stopButton);

		stage.addActor(group);

		setTesting(false);
	}

	@Override
	public void getDebugStrings(Array<String> array) {
		if (world == null) {
			array.add("World is null!");
			return;
		}
	}

	@Override
	public void resize(int width, int height) {
		renderer.resize(width, height);

		if (stage != null) stage.onResize(width, height);
	}

	@Override
	public void show() {
		if (stage == null) generateStage();

		stage.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer) {
			stage.addSelfToInputMultiplexer((InputMultiplexer) Gdx.input.getInputProcessor());
		}
	}

	@Override
	public void hide() {
		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer && stage != null) {
			Gdx.app.postRunnable(new Runnable() {

				@Override
				public void run() {
					stage.removeSelfFromInputMultiplexer(
							(InputMultiplexer) Gdx.input.getInputProcessor());
				}

			});
		}
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