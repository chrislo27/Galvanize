package chrislo27.galvanize.savefile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.evilco.mc.nbt.stream.NbtInputStream;
import com.evilco.mc.nbt.stream.NbtOutputStream;
import com.evilco.mc.nbt.tag.TagCompound;

import ionium.util.IOUtils;

public class SaveFile {

	private static SaveFile instance;

	private SaveFile() {
	}

	public static SaveFile instance() {
		if (instance == null) {
			instance = new SaveFile();
			instance.loadResources();
		}
		return instance;
	}

	public FileHandle saveLocation;

	private void loadResources() {
		saveLocation = Gdx.files.local("saves/save.gdat");
	}

	public void reset() {

	}

	/**
	 * File must exist to make a backup, otherwise is ignored.
	 * @param handle
	 */
	public void makeBackup(FileHandle handle) {
		if (!handle.exists()) return;

		if (handle.isDirectory()) throw new IllegalArgumentException("File cannot be a directory!");

		handle.copyTo(Gdx.files.absolute(handle.file().getAbsolutePath() + ".bak"));
	}

	public void makeDirectoriesForSave(FileHandle handle) {
		handle.parent().mkdirs();

		// remove new filehandle instance
		System.gc();
	}

	public void save(FileHandle handle) throws IOException {
		makeDirectoriesForSave(handle);

		if (handle.isDirectory()) throw new IllegalArgumentException("File cannot be a directory!");

		makeBackup(handle);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		NbtOutputStream nbtStream = new NbtOutputStream(baos);

		try {
			nbtStream.write(writeToNBT());
			IOUtils.saveGzip(handle, baos.toByteArray());
		} catch (IOException e) {
			nbtStream.close();

			throw e;
		}

		nbtStream.close();
	}

	public void load(FileHandle handle) throws IOException {
		if (handle.isDirectory()) throw new IllegalArgumentException("File cannot be a directory!");

		if (!handle.exists()) return;

		ByteArrayInputStream byteStream = new ByteArrayInputStream(IOUtils.loadGzip(handle));
		NbtInputStream nbtStream = new NbtInputStream(byteStream);

		try {
			readFromNBT((TagCompound) nbtStream.readTag());
		} catch (IOException e) {
			nbtStream.close();

			throw e;
		}

		nbtStream.close();
	}

	public boolean readFromNBT(TagCompound root) {

		return true;
	}

	public TagCompound writeToNBT() {
		TagCompound root = new TagCompound("Save");

		return root;
	}

}
