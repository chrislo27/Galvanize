package chrislo27.galvanize.nbt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.evilco.mc.nbt.tag.ITag;
import com.evilco.mc.nbt.tag.TagCompound;
import com.evilco.mc.nbt.tag.TagInteger;

public class IDMap {

	public HashMap<String, Integer> keyToValue = new HashMap<>();
	public HashMap<Integer, String> valueToKey = new HashMap<>();

	protected int ticker = 0;

	private TagCompound tag = null;

	public String compoundTagName = "";

	public IDMap(String tagName) {
		compoundTagName = tagName;
	}

	public void generateTag() {
		tag = null;
		tag = new TagCompound(compoundTagName);

		Iterator<Entry<String, Integer>> it = keyToValue.entrySet().iterator();
		Entry<String, Integer> entry;

		while (it.hasNext()) {
			entry = it.next();

			TagInteger intTag = new TagInteger(entry.getKey(), entry.getValue());

			tag.setTag(intTag);
		}
	}

	public TagCompound getTag() {
		if (tag == null) generateTag();

		return tag;
	}

	public void loadFromTag(TagCompound other) {
		if (other == null) return;

		tag = null;
		compoundTagName = other.getName();
		keyToValue.clear();
		valueToKey.clear();

		Map<String, ITag> allTags = other.getTags();

		for (ITag entryTag : allTags.values()) {
			if (entryTag instanceof TagInteger) {
				TagInteger intTag = (TagInteger) entryTag;

				keyToValue.put(intTag.getName(), intTag.getValue());
				valueToKey.put(intTag.getValue(), intTag.getName());
			}
		}
	}

	public void add(String key) {
		keyToValue.put(key, ticker);
		valueToKey.put(ticker, key);

		ticker++;
	}

}
