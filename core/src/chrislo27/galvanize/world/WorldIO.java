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
import com.evilco.mc.nbt.tag.TagIntegerArray;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.nbt.BlockIDMap;
import chrislo27.galvanize.registry.Blocks;

public class WorldIO {

	public static byte[] worldToBytes(World world) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		NbtOutputStream nbtStream = new NbtOutputStream(baos);

		TagCompound root = new TagCompound("World");

		root.setTag(new TagInteger("Width", world.worldWidth));
		root.setTag(new TagInteger("Height", world.worldHeight));

		BlockIDMap idMap = new BlockIDMap("BlockIDMap");
		TagCompound idMapTag = idMap.getTag();

		root.setTag(idMapTag);

		int[] ids = new int[world.worldWidth * world.worldHeight];

		for (int x = 0; x < world.worldWidth; x++) {
			for (int y = 0; y < world.worldHeight; y++) {
				Block b = world.getBlock(x, y);

				ids[y * world.worldHeight + x] = b == null ? 0
						: idMap.keyToValue.get(Blocks.getKey(world.getBlock(x, y)));
			}
		}

		root.setTag(new TagIntegerArray("Blocks", ids));

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

		BlockIDMap idMap = new BlockIDMap("BlockIDMap");
		idMap.loadFromTag(root.getCompound("BlockIDMap"));

		int[] ids = root.getIntegerArray("Blocks");

		for (int x = 0; x < world.worldWidth; x++) {
			for (int y = 0; y < world.worldHeight; y++) {
				int id = ids[y * world.worldHeight + x];

				world.setBlock(id <= 0 ? null : Blocks.getBlock(idMap.valueToKey.get(id)), x, y);
			}
		}

		nbtStream.close();
		return world;
	}

}
