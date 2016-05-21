package chrislo27.galvanize.screen;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.Main;
import chrislo27.galvanize.savefile.SaveFile;
import ionium.registry.AssetRegistry;
import ionium.registry.ScreenRegistry;
import ionium.screen.Updateable;
import ionium.stage.Actor;
import ionium.stage.Group;
import ionium.stage.Stage;
import ionium.stage.ui.ImageButton;
import ionium.stage.ui.TextButton;
import ionium.stage.ui.TextLabel;
import ionium.stage.ui.skin.Palette;
import ionium.stage.ui.skin.Palettes;
import ionium.transition.GearZoom;

public class MainMenuScreen extends Updateable<Main> {

	private Stage stage;

	private Group mainMenuGroup;
	private TextButton newGameButton;
	private TextButton continueGameButton;

	private Group confirmNewGameGroup;
	private ImageButton confirmYes;
	private ImageButton confirmNo;
	private TextLabel confirmLabel;

	private Group loadFailedGroup;
	private TextLabel loadFailedLabel;
	private TextButton returnToMainMenu;

	public MainMenuScreen(Main m) {
		super(m);
	}

	private void generateStage() {
		stage = new Stage();
		Palette palette = Palettes.getIoniumDefault(main.font, main.fontBordered);

		// new game group

		mainMenuGroup = new Group(stage);

		{
			newGameButton = new TextButton(stage, palette, "mainMenu.newGame") {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					Gdx.app.postRunnable(new Runnable() {

						@Override
						public void run() {
							stage.setAllVisible(false);
							confirmNewGameGroup.setVisible(true);
						}

					});
				}

			};

			newGameButton.align(Align.center | Align.bottom).setPixelOffsetSize(256, 48)
					.setScreenOffset(0, 0.25f);

			mainMenuGroup.addActor(newGameButton);

			continueGameButton = new TextButton(stage, palette, "mainMenu.continueGame") {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					if (SaveFile.instance().saveLocation.exists()) {
						try {
							SaveFile.instance().load(SaveFile.instance().saveLocation);
						} catch (IOException e) {
							e.printStackTrace();
							setEnabled(false);
						}
					} else {
						setEnabled(false);
					}
				}

				@Override
				public Actor setEnabled(boolean enabled) {
					super.setEnabled(enabled && SaveFile.instance().saveLocation.exists());

					return this;
				}
			};

			continueGameButton.align(Align.center | Align.bottom).setEnabled(false)
					.setPixelOffsetSize(256, 48).setScreenOffset(0, 0.25f);

			continueGameButton.setPixelOffset(0, 64);

			mainMenuGroup.addActor(continueGameButton);
		}

		stage.addActor(mainMenuGroup);

		// new game confirmation

		confirmNewGameGroup = new Group(stage);

		{
			Group g = confirmNewGameGroup;

			confirmYes = new ImageButton(stage, palette,
					new TextureRegion(AssetRegistry.getTexture("ui_yes"))) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					SaveFile sf = SaveFile.instance();

					sf.makeBackup(sf.saveLocation);
					sf.saveLocation.delete();
					sf.reset();
					try {
						sf.save(sf.saveLocation);

						transitionToLevelSelect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			};

			confirmYes.getColor().set(0, 0.5f, 0.055f, 1);

			g.addActor(confirmYes).align(Align.center).setPixelOffsetSize(64, 64)
					.setScreenOffset(0, -0.1f).setPixelOffset(-96, 0);

			confirmNo = new ImageButton(stage, palette,
					new TextureRegion(AssetRegistry.getTexture("ui_no"))) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					Gdx.app.postRunnable(new Runnable() {

						@Override
						public void run() {
							stage.setAllVisible(false);
							mainMenuGroup.setVisible(true);
						}

					});
				}

			};

			confirmNo.getColor().set(242 / 255f, 0.0525f, 0.0525f, 1);

			g.addActor(confirmNo).align(Align.center).setPixelOffsetSize(64, 64)
					.setScreenOffset(0, -0.1f).setPixelOffset(96, 0);

			confirmLabel = new TextLabel(stage, palette, "mainMenu.overwriteOldGame");

			confirmLabel.getColor().set(1, 1, 1, 1);
			confirmLabel.setTextAlign(Align.center).setTextWrap(true).align(Align.center)
					.setScreenOffset(0, 0.1f, 0.75f, 1);

			g.addActor(confirmLabel);
		}

		stage.addActor(confirmNewGameGroup).setVisible(false);

		// load failed

		loadFailedGroup = new Group(stage);

		{
			loadFailedLabel = new TextLabel(stage, palette, "mainMenu.loadFailed");

			loadFailedLabel.getColor().set(1, 1, 1, 1);
			loadFailedLabel.setTextAlign(Align.center).setTextWrap(true).align(Align.center)
					.setScreenOffset(0, 0.1f, 0.75f, 1);

			loadFailedGroup.addActor(loadFailedLabel);

			returnToMainMenu = new TextButton(stage, palette, "mainMenu.backToMainMenu") {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					stage.setAllVisible(false);
					mainMenuGroup.setVisible(true);
				}
			};

			returnToMainMenu.align(Align.center).setPixelOffsetSize(256, 48).setScreenOffset(0,
					-0.1f);

			loadFailedGroup.addActor(returnToMainMenu);
		}

		stage.addActor(loadFailedGroup).setVisible(false);
	}

	private void transitionToLevelSelect() {
		try {
			SaveFile.instance().load(SaveFile.instance().saveLocation);
			main.transition(new GearZoom(0.5f), null, ScreenRegistry.get("levelSelect"));
		} catch (IOException e) {
			e.printStackTrace();

			stage.setAllVisible(false);
			loadFailedGroup.setVisible(true);
		}

	}

	@Override
	public void render(float delta) {
		stage.render(main.batch);
	}

	@Override
	public void renderUpdate() {
	}

	@Override
	public void tickUpdate() {
	}

	@Override
	public void getDebugStrings(Array<String> array) {
	}

	@Override
	public void resize(int width, int height) {
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
		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer) {
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
	}

}
