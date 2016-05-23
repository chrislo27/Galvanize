package chrislo27.galvanize.screen;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.Keybinds;
import chrislo27.galvanize.Main;
import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.registry.Blocks;
import chrislo27.galvanize.render.WorldRenderer;
import chrislo27.galvanize.world.World;
import chrislo27.galvanize.world.WorldIO;
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
import ionium.util.IOUtils;
import ionium.util.i18n.Localization;
import ionium.util.input.AnyKeyPressed;

public class LevelEditorScreen extends Updateable<Main> {

	World world;
	WorldRenderer renderer;

	final EditorInputProcessor editorInputProcessor = new EditorInputProcessor();
	final float cameraMoveSpeed = 8;

	Stage stage;
	Group group;
	ImageButton toggleTaskbar;
	ImageButton playButton;
	ImageButton pauseButton;
	ImageButton stopButton;
	TextLabel infoText;
	TextLabel saveLocationText;
	ImageButton cameraChange;
	ImageButton newLevel;
	ImageButton openLevel;
	ImageButton saveLevel;
	ImageButton currentBlockIcon;
	TextLabel blockInfoText;

	boolean isTesting = false;
	boolean paused = false;
	boolean isUsingPlayerCam = true;
	Vector3 editorCam = new Vector3();

	File lastSaveLocation = null;

	int selectedBlock = 0;

	public LevelEditorScreen(Main m) {
		super(m);

		renderer = new WorldRenderer();
	}

	public void setWorld(World world) {
		this.world = world;
		lastSaveLocation = null;
		setTesting(false);
		saveLevel.setEnabled(world != null);
	}

	public void setTesting(boolean test) {
		test &= canTest();

		isTesting = test;
		setPaused(false);

		if (isTesting) {
			stopButton.setEnabled(true);
			cameraChange.setEnabled(true);
		} else {
			if (world != null) world.clearAllEntities();
			stopButton.setEnabled(false);
			pauseButton.setEnabled(false);
			playButton.setEnabled(true);
			cameraChange.setEnabled(false);
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

			main.batch.setProjectionMatrix(renderer.camera.combined);

			main.batch.begin();

			final float thickness = 2f / Block.TILE_SIZE;

			main.batch.setColor(0, 1, 0, 1);
			Main.drawRect(main.batch, -thickness, -thickness, world.worldWidth + thickness * 2,
					world.worldHeight + thickness * 2, thickness);

			main.batch.end();

			main.batch.setProjectionMatrix(main.camera.combined);
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
		if (world != null && !paused) {
			if (isTesting) {
				world.inputUpdate();
			}
		}

		if (world != null && !isTesting) {
			if (AnyKeyPressed.isAKeyPressed(Keybinds.UP)) {
				renderer.camera.position.y += Gdx.graphics.getDeltaTime() * cameraMoveSpeed;
			}
			if (AnyKeyPressed.isAKeyPressed(Keybinds.DOWN)) {
				renderer.camera.position.y -= Gdx.graphics.getDeltaTime() * cameraMoveSpeed;
			}
			if (AnyKeyPressed.isAKeyPressed(Keybinds.LEFT)) {
				renderer.camera.position.x -= Gdx.graphics.getDeltaTime() * cameraMoveSpeed;
			}
			if (AnyKeyPressed.isAKeyPressed(Keybinds.RIGHT)) {
				renderer.camera.position.x += Gdx.graphics.getDeltaTime() * cameraMoveSpeed;
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

				if (isMouseOn || world == null) {
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

		blockInfoText = new TextLabel(stage, Palettes.getIoniumDefault(main.font, main.font), null);
		blockInfoText.setI10NStrategy(new LocalizationStrategy() {

			@Override
			public String get(String key, Object... params) {
				if (key == null) return "";

				return key;
			}
		});
		blockInfoText.setTextAlign(Align.left | Align.top);
		blockInfoText.getColor().set(0, 0, 0, 1);
		blockInfoText.align(Align.left | Align.top).setScreenOffset(0, 0, 1, 0)
				.setPixelOffset(4 + 36 * 3 + 4 + 64, 28, -8, 64);

		group.addActor(blockInfoText);

		saveLocationText = new TextLabel(stage, infoText.getPalette(), null);
		saveLocationText.setI10NStrategy(new LocalizationStrategy() {

			@Override
			public String get(String key, Object... params) {
				if (key == null) return "";

				return key;
			}
		});
		saveLocationText.setTextAlign(Align.left | Align.bottom);
		saveLocationText.getColor().set(0, 0, 0, 1);
		saveLocationText.align(Align.left | Align.bottom).setScreenOffset(0, 0, 1, 1)
				.setPixelOffset(16, 40, -8, 0);

		group.addActor(saveLocationText);

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

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe) infoText.setLocalizationKey("levelEditor.infoText.hideMenu");

					wasMouseOnMe = true;
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

		cameraChange = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "moviecamera")) {

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);

				isUsingPlayerCam = !isUsingPlayerCam;
			}

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe)
						infoText.setLocalizationKey("levelEditor.infoText.cameraChange");

					wasMouseOnMe = true;
				}
			}

		};
		cameraChange.getColor().set(0.25f, 0.25f, 0.25f, 1);
		cameraChange.align(Align.right | Align.top).setScreenOffset(0, 0, 0, 0).setPixelOffset(4,
				36 + 4 + 32, 32, 32);
		group.addActor(cameraChange);

		newLevel = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "newFile")) {

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);

				setWorld(new World(64, 64));
			}

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe) infoText.setLocalizationKey("levelEditor.infoText.newFile");

					wasMouseOnMe = true;
				}
			}

		};
		newLevel.getColor().set(0.25f, 0.25f, 0.25f, 1);
		newLevel.align(Align.left | Align.top).setScreenOffset(0, 0, 0, 0).setPixelOffset(4, 36, 32,
				32);
		group.addActor(newLevel);

		openLevel = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "openFile")) {

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);

				Thread t = new Thread() {

					@Override
					public void run() {
						JFileChooser fileChooser = new JFileChooser();
						if (lastSaveLocation != null) {
							fileChooser.setCurrentDirectory(lastSaveLocation);
						} else {
							fileChooser.setCurrentDirectory(
									new File(System.getProperty("user.home"), "Desktop"));
						}
						fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						fileChooser.setDialogTitle("Open a level file (usually .nbt)");
						FileNameExtensionFilter ffef = new FileNameExtensionFilter(
								"Named Binary Tag level files (.nbt)", "nbt");
						fileChooser.addChoosableFileFilter(ffef);
						fileChooser.setFileFilter(ffef);

						int result = fileChooser.showOpenDialog(null);

						if (result == JFileChooser.APPROVE_OPTION) {
							final File selectedFile = fileChooser.getSelectedFile();

							lastSaveLocation = selectedFile;

							World w = null;

							try {
								w = WorldIO.bytesToWorld(
										IOUtils.loadGzip(new FileHandle(selectedFile)));

								saveLocationText.setLocalizationKey(Localization
										.get("levelEditor.loadedFrom", lastSaveLocation.getPath()));
							} catch (Exception e) {
								e.printStackTrace();

								saveLocationText.setLocalizationKey(Localization.get(
										"levelEditor.failedToLoad", lastSaveLocation.getPath()));
							}

							if (w != null) setWorld(w);

						}

						System.gc();
					}

				};

				t.setDaemon(true);
				t.setName("Open Level Dialog");
				t.start();
			}

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe) infoText.setLocalizationKey("levelEditor.infoText.openFile");

					wasMouseOnMe = true;
				}
			}

		};
		openLevel.getColor().set(0.25f, 0.25f, 0.25f, 1);
		openLevel.align(Align.left | Align.top).setScreenOffset(0, 0, 0, 0).setPixelOffset(4 + 36,
				36, 32, 32);
		group.addActor(openLevel);

		saveLevel = new ImageButton(stage, p,
				AssetRegistry.getAtlasRegion("ionium_ui-icons", "saveFile")) {

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);

				if (world == null) return;

				Thread t = new Thread() {

					@Override
					public void run() {
						JFileChooser fileChooser = new JFileChooser();
						if (lastSaveLocation != null) {
							fileChooser.setCurrentDirectory(lastSaveLocation);
						} else {
							fileChooser.setCurrentDirectory(
									new File(System.getProperty("user.home"), "Desktop"));
						}
						fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						fileChooser.setSelectedFile(new File("a-custom-level.nbt"));
						fileChooser.setDialogTitle("Select a directory to save in...");
						int result = fileChooser.showSaveDialog(null);
						if (result == JFileChooser.APPROVE_OPTION) {
							final File selectedFile = fileChooser.getSelectedFile();

							lastSaveLocation = selectedFile;

							try {
								IOUtils.saveGzip(new FileHandle(selectedFile),
										WorldIO.worldToBytes(world));

								saveLocationText.setLocalizationKey(Localization
										.get("levelEditor.savedTo", lastSaveLocation.getPath()));

								sleep(5000);

								saveLocationText.setLocalizationKey(null);
							} catch (Exception e) {
								e.printStackTrace();

								saveLocationText.setLocalizationKey(Localization
										.get("levelEditor.failedToSave", selectedFile.getPath()));
							}
						}

						System.gc();
					}

				};

				t.setDaemon(true);
				t.setName("Save Level Dialog");
				t.start();
			}

			private boolean wasMouseOnMe = false;

			@Override
			public void onMouseMove(float x, float y) {
				super.onMouseMove(x, y);

				if (x < 0 || y < 0 || x >= 1 || y > 1) {
					if (wasMouseOnMe) infoText.setLocalizationKey(null);

					wasMouseOnMe = false;
				} else {
					if (!wasMouseOnMe) infoText.setLocalizationKey("levelEditor.infoText.saveFile");

					wasMouseOnMe = true;
				}
			}

		};
		saveLevel.getColor().set(0.25f, 0.25f, 0.25f, 1);
		saveLevel.align(Align.left | Align.top).setScreenOffset(0, 0, 0, 0)
				.setPixelOffset(4 + 36 * 2, 36, 32, 32);
		group.addActor(saveLevel).setEnabled(false);

		currentBlockIcon = new ImageButton(stage, p, null) {

			@Override
			public void render(SpriteBatch batch, float alpha) {
				Block b = Blocks.instance().getAllBlocks().get(selectedBlock);

				int currentRegionId = b.getRenderBlock().getCurrentRegion(0, 0);
				this.setTextureRegion(
						Blocks.getRegion(b.getRenderBlock().getAllTextures().get(currentRegionId)));

				super.render(batch, alpha);
			}

			@Override
			public void onClickAction(float x, float y) {
				super.onClickAction(x, y);

				editorInputProcessor.changeBlock(1);
			}

		};
		currentBlockIcon.align(Align.left | Align.top).setScreenOffset(0, 0, 0, 0)
				.setPixelOffset(4 + 36 * 3, 36, 64, 64);
		group.addActor(currentBlockIcon);

		stage.addActor(group);

		setTesting(false);
		// force update block info text
		editorInputProcessor.changeBlock(0);
	}

	@Override
	public void getDebugStrings(Array<String> array) {
		if (world == null) {
			array.add("World is null!");
			return;
		}

		array.add("zoom: " + renderer.camera.zoom);
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
			InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();

			stage.addSelfToInputMultiplexer(plex);
			plex.addProcessor(editorInputProcessor);
		}
	}

	@Override
	public void hide() {
		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer && stage != null) {
			Gdx.app.postRunnable(new Runnable() {

				@Override
				public void run() {
					InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();

					stage.removeSelfFromInputMultiplexer(plex);
					plex.removeProcessor(editorInputProcessor);
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

	private class EditorInputProcessor extends InputAdapter {

		private final Vector3 tmp = new Vector3();
		private int button = -1;
		private final Vector2 dragCoord = new Vector2();
		private final Vector2 lastCam = new Vector2();

		private void setBlock(Block block, int screenX, int screenY) {
			if (world == null) return;

			tmp.set(screenX, screenY, 0);
			renderer.camera.unproject(tmp);

			int bx = (int) tmp.x;
			int by = (int) tmp.y;

			world.setBlock(block, bx, by);
		}

		private void setBlockFromButton(int screenX, int screenY, int button) {
			Block block = null;
			boolean canSet = true;

			if (button == Buttons.LEFT) {
				block = Blocks.instance().getAllBlocks().get(selectedBlock);
			} else if (button == Buttons.RIGHT) {
				block = null;
			} else {
				canSet = false;
			}

			if (canSet) setBlock(block, screenX, screenY);
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			if (pointer == 0) this.button = button;

			setBlockFromButton(screenX, screenY, this.button);

			if (button == Buttons.MIDDLE) {
				dragCoord.set(screenX, screenY);
				lastCam.set(renderer.camera.position.x, renderer.camera.position.y);
			}

			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			if (pointer == 0) {
				if (this.button == button) {
					this.button = -1;
				}
			}

			return true;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			if (Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
				float deltaX = dragCoord.x - screenX;
				float deltaY = dragCoord.y - screenY;

				tmp.set(deltaX / Block.TILE_SIZE * renderer.camera.zoom,
						deltaY / Block.TILE_SIZE * renderer.camera.zoom, 0);

				renderer.camera.position.set(lastCam.x + tmp.x, lastCam.y - tmp.y, 0);
			} else {
				setBlockFromButton(screenX, screenY, button);
			}

			return true;
		}

		public void changeBlock(int amount) {
			selectedBlock += amount;

			if (selectedBlock < 0) selectedBlock = Blocks.instance().getAllBlocks().size - 1;
			if (selectedBlock >= Blocks.instance().getAllBlocks().size) selectedBlock = 0;

			String key = Blocks.instance().getAllKeys().get(selectedBlock);

			blockInfoText.setLocalizationKey(Localization.get("block." + key + ".name") + " - "
					+ Localization.get("levelEditor.infoText.changeBlockShortcut") + "\n[GRAY]"
					+ Localization.get("block." + key + ".editorDesc"));
		}

		@Override
		public boolean scrolled(int amount) {
			boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
					|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
			boolean control = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)
					|| Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);

			if (shift) {
				final float moveAmt = 2;

				if (control) {
					// vertical
					renderer.camera.position.y += amount * moveAmt * -1;
				} else {
					// horizontal
					renderer.camera.position.x += amount * moveAmt;
				}
			} else if (control && !shift) {
				changeBlock(amount);
			} else if (!control && !shift) {

				renderer.camera.zoom += amount * 0.25f;

				final float smallerViewport = renderer.camera.viewportWidth <= renderer.camera.viewportHeight
						? renderer.camera.viewportWidth : renderer.camera.viewportHeight;
				final float gutter = world.worldWidth <= world.worldHeight ? world.worldWidth * 0.5f
						: world.worldHeight * 0.5f;

				float maxZoom = world.worldWidth >= world.worldHeight
						? (world.worldWidth + gutter) / smallerViewport
						: (world.worldHeight + gutter) / smallerViewport;

				renderer.camera.zoom = MathUtils.clamp(renderer.camera.zoom, 0.01f, maxZoom);

				renderer.camera.update();
			}

			return true;
		}

	}

}
