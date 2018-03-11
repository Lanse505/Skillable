package codersafterdark.reskillable.network;

import codersafterdark.reskillable.api.ReskillableRegistries;
import codersafterdark.reskillable.base.PlayerData;
import codersafterdark.reskillable.base.PlayerDataHandler;
import codersafterdark.reskillable.base.PlayerSkillInfo;
import codersafterdark.reskillable.api.skill.Skill;
import codersafterdark.reskillable.api.unlockable.Unlockable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;

import java.util.Objects;

public class MessageUnlockUnlockable implements IMessage, IMessageHandler<MessageUnlockUnlockable, IMessage> {
    
    public String skill, unlockable;
    
    public MessageUnlockUnlockable() {
    }
    
    public MessageUnlockUnlockable(String skill, String unlockable) {
        this.skill = skill;
        this.unlockable = unlockable;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        skill = ByteBufUtils.readUTF8String(buf);
        unlockable = ByteBufUtils.readUTF8String(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, skill);
        ByteBufUtils.writeUTF8String(buf, unlockable);
    
    }
    
    @Override
    public IMessage onMessage(MessageUnlockUnlockable message, MessageContext ctx) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> handleMessage(message, ctx));
        return null;
    }
    
    public IMessage handleMessage(MessageUnlockUnlockable message, MessageContext context) {
        EntityPlayer player = context.getServerHandler().player;
        Skill skill = ReskillableRegistries.SKILLS.getValue(new ResourceLocation(message.skill));
        Unlockable unlockable = Objects.requireNonNull(ReskillableRegistries.UNLOCKABLES.getValue(new ResourceLocation(message.unlockable)));
        PlayerData data = PlayerDataHandler.get(player);
        PlayerSkillInfo info = data.getSkillInfo(skill);

        if(!info.isUnlocked(unlockable) && info.getSkillPoints() >= unlockable.getCost() && data.matchStats(unlockable.getRequirements())) {
            info.unlock(unlockable);
            data.saveAndSync();
        }
        
        return null;
    }
}