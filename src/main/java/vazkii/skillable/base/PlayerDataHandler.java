package vazkii.skillable.base;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import vazkii.arl.network.NetworkHandler;
import vazkii.skillable.network.MessageDataSync;

public class PlayerDataHandler {

	private static HashMap<Integer, PlayerData> playerData = new HashMap();

	private static final String DATA_TAG = "SkillableData";

	public static PlayerData get(EntityPlayer player) {
		int key = getKey(player);
		if(!playerData.containsKey(key))
			playerData.put(key, new PlayerData(player));

		PlayerData data = playerData.get(key);
		if(data.playerWR.get() != player) {
			NBTTagCompound cmp = new NBTTagCompound();
			data.saveToNBT(cmp);
			playerData.remove(key);
			data = get(player);
			data.loadFromNBT(cmp);
		}

		return data;
	}

	public static void cleanup() {
		Iterator<Entry<Integer, PlayerData>> it = playerData.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, PlayerData> item = it.next();
			PlayerData d = item.getValue();
			if(d != null && d.playerWR.get() == null)
				it.remove();
		}
	}

	private static int getKey(EntityPlayer player) {
		return player.hashCode() << 1 + (player.getEntityWorld().isRemote ? 1 : 0);
	}

	public static NBTTagCompound getDataCompoundForPlayer(EntityPlayer player) {
		NBTTagCompound forgeData = player.getEntityData();
		if(!forgeData.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
			forgeData.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());

		NBTTagCompound persistentData = forgeData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		if(!persistentData.hasKey(DATA_TAG))
			persistentData.setTag(DATA_TAG, new NBTTagCompound());

		return persistentData.getCompoundTag(DATA_TAG);
	}
	
	public static class EventHandler {

		@SubscribeEvent
		public void onServerTick(ServerTickEvent event) {
			if(event.phase == Phase.END)
				PlayerDataHandler.cleanup();
		}

		@SubscribeEvent
		public void onPlayerTick(LivingUpdateEvent event) {
			if(event.getEntityLiving() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) event.getEntityLiving();

				PlayerDataHandler.get(player).tick();
			}
		}

		@SubscribeEvent
		public void onPlayerLogin(PlayerLoggedInEvent event) {
			if(event.player instanceof EntityPlayerMP) {
				MessageDataSync message = new MessageDataSync(get(event.player));
				NetworkHandler.INSTANCE.sendTo(message, (EntityPlayerMP) event.player);
			}
		}
	}

	public static class PlayerData {

		public WeakReference<EntityPlayer> playerWR;
		private final boolean client;		
		
		public PlayerData(EntityPlayer player) {
			playerWR = new WeakReference(player);
			client = player.getEntityWorld().isRemote;

			load();
		}

		public void tick() {

		}

		public void load() {
			if(!client) {
				EntityPlayer player = playerWR.get();

				if(player != null) {
					NBTTagCompound cmp = getDataCompoundForPlayer(player);
					loadFromNBT(cmp);
				}
			}
		}

		public void save() {
			if(!client) {
				EntityPlayer player = playerWR.get();

				if(player != null) {
					NBTTagCompound cmp = getDataCompoundForPlayer(player);
					saveToNBT(cmp);
				}
			}
		}

		public void loadFromNBT(NBTTagCompound cmp) {

		}

		public void saveToNBT(NBTTagCompound cmp) {

		}

	}

}
