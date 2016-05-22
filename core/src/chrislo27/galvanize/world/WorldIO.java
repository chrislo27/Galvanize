package chrislo27.galvanize.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.evilco.mc.nbt.error.TagNotFoundException;
import com.evilco.mc.nbt.error.UnexpectedTagTypeException;
import com.evilco.mc.nbt.stream.NbtInputStream;
import com.evilco.mc.nbt.stream.NbtOutputStream;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagInteger;

public class WorldIO {

	public static byte[] worldToBytes(World world) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		NbtOutputStream nbtStream = new NbtOutputStream(baos);

		TagCompound root = new TagCompound("World");

		root.setTag(new TagInteger("Width", world.worldWidth));
		root.setTag(new TagInteger("Height", world.worldHeight));

		nbtStream.write(root);

		nbtStream.close();
		return baos.toByteArray();
	}

	public static World bytesToWorld(byte[] bytes)
			throws IOException, TagNotFoundException, UnexpectedTagTypeException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		NbtInputStream nbtStream = new NbtInputStream(bais);

		TagCompound root = (TagCompound) nbtStream.readTag();

		int width = root.getInteger("Width");
		int height = root.getInteger("Height");

		World world = new World(width, height);

		nbtStream.close();
		return world;
	}

}
