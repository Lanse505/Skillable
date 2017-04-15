package vazkii.skillable.base;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import vazkii.skillable.skill.Skill;
import vazkii.skillable.skill.Skills;

public class PlayerData {

	private static final String TAG_SKILLS_CMP = "SkillLevels";
	
	public WeakReference<EntityPlayer> playerWR;
	private final boolean client;
	
	private HashMap<Skill, Integer> skillLevels = new HashMap();

	public PlayerData(EntityPlayer player) {
		playerWR = new WeakReference(player);
		client = player.getEntityWorld().isRemote;
		
		for(Skill s : Skills.ALL_SKILLS.values())
			skillLevels.put(s, 1);

		load();
	}

	public void tick() {

	}

	public void load() {
		if(!client) {
			EntityPlayer player = playerWR.get();

			if(player != null) {
				NBTTagCompound cmp = PlayerDataHandler.getDataCompoundForPlayer(player);
				loadFromNBT(cmp);
			}
		}
	}

	public void save() {
		if(!client) {
			EntityPlayer player = playerWR.get();

			if(player != null) {
				NBTTagCompound cmp = PlayerDataHandler.getDataCompoundForPlayer(player);
				saveToNBT(cmp);
			}
		}
	}

	public void loadFromNBT(NBTTagCompound cmp) {
		NBTTagCompound skillsCmp = cmp.getCompoundTag(TAG_SKILLS_CMP);
		for(Skill s : skillLevels.keySet()) {
			String key = s.getKey();
			if(skillsCmp.hasKey(key))
				skillLevels.put(s, skillsCmp.getInteger(key));
		}
	}

	public void saveToNBT(NBTTagCompound cmp) {
		NBTTagCompound skillsCmp = new NBTTagCompound();
		for(Skill s : skillLevels.keySet()) {
			String key = s.getKey();
			cmp.setInteger(key, skillLevels.get(s));
		}
		
		cmp.setTag(TAG_SKILLS_CMP, skillsCmp);
	}

}