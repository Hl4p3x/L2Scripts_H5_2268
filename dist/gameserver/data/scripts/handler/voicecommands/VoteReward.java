package handler.voicecommands;

import java.util.Collection;

import l2s.gameserver.config.xml.holder.VoteRewardConfigHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.votereward.VoteRewardSite;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.ScriptFile;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 14.02.2019
 * Developed for L2-Scripts.com
 **/
public class VoteReward implements IVoicedCommandHandler, ScriptFile
{
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		Collection<VoteRewardSite> sites = VoteRewardConfigHolder.getInstance().getVoteRewardSites();
		if(sites.isEmpty())
			return false;

		boolean received = false;
		for(VoteRewardSite site :sites ) {
			if(site.isEnabled()) {
				if (site.tryGiveRewards(activeChar))
					received = true;
			}
		}

		if (!received)
			activeChar.sendPacket(new CustomMessage("votereward.not_have_votes", activeChar));

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VoteRewardConfigHolder.REWARD_COMMANDS;
	}

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}
}
