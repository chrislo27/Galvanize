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
		saveLocation = Gdx.files.local("saves/save.nbt");
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
		FileOutputStream outputStream = new FileOutputStream(handle.file().getAbsolutePath());
		GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream);

		nbtStream.write(writeToNBT());
		gzipStream.write(baos.toByteArray());

		nbtStream.close();
		gzipStream.close();
	}

	public void load(FileHandle handle) throws IOException {
		if (handle.isDirectory()) throw new IllegalArgumentException("File cannot be a directory!");

		ByteArrayInputStream byteStream = new ByteArrayInputStream(loadBytes(handle));
		NbtInputStream nbtStream = new NbtInputStream(byteStream);

		readFromNBT((TagCompound) nbtStream.readTag());

		nbtStream.close();
	}

	public static byte[] loadBytes(FileHandle file) throws IOException {
		FileInputStream fis = new FileInputStream(file.file());
		GZIPInputStream gzipstream = new GZIPInputStream(fis);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2048);
		byte[] buffer = new byte[2048];
		int bytesRead;
		while ((bytesRead = gzipstream.read(buffer)) > 0) {
			byteStream.write(buffer, 0, bytesRead);
		}

		gzipstream.close();
		fis.close();

		return byteStream.toByteArray();
	}

	public boolean readFromNBT(TagCompound root) {

		return true;
	}

	public TagCompound writeToNBT() {
		TagCompound root = new TagCompound("Save");

		return root;
	}

}
