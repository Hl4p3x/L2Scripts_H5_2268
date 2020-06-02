package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestVoteNew extends L2GameClientPacket
{
	private int _targetObjectId;

	@Override
	protected void readImpl()
	{
		_targetObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!activeChar.getPlayerAccess().CanEvaluate)
			return;

		GameObject target = activeChar.getTarget();
		if(target == null || !target.isPlayer() || target.getObjectId() != _targetObjectId)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}

		if(target.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}

		Player targetPlayer = (Player)target;

		if(Config.ALT_DISALLOW_RECOMMEND_ON_SINGLE_DEVICE == 1 && activeChar.getIP().equalsIgnoreCase(targetPlayer.getIP()))
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете рекомендовать персонажа с таким же IP как у вас." : "You can not recommend a character with the same IP like you.");
			return;
		}

		if(Config.ALT_DISALLOW_RECOMMEND_ON_SINGLE_DEVICE == 2)
		{
			String activeCharHWID = activeChar.getNetConnection().getHWID();
			if (targetPlayer.getNetConnection() == null)
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете рекомендовать персонажа который переподключается к игре." : "You can not recommend a character who is reconnected.");
				return;
			}
			String targetCharHWID = targetPlayer.getNetConnection().getHWID();
			if(activeCharHWID != null && !activeCharHWID.isEmpty() && targetCharHWID != null && !targetCharHWID.isEmpty() && activeCharHWID.equals(targetCharHWID))
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете рекомендовать персонажа с одного и того же компьютерра." : "You can not recommend a character with the same computer.");
				return;
			}
		}

		if(activeChar.getRecomLeft() <= 0)
		{
			activeChar.sendPacket(Msg.NO_MORE_RECOMMENDATIONS_TO_HAVE);
			return;
		}

		if(targetPlayer.getRecomHave() >= 255)
		{
			activeChar.sendPacket(Msg.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION);
			return;
		}

		activeChar.giveRecom(targetPlayer);
		SystemMessage sm = new SystemMessage(SystemMessage.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addString(target.getName());
		sm.addNumber(activeChar.getRecomLeft());
		activeChar.sendPacket(sm);

		sm = new SystemMessage(SystemMessage.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addString(activeChar.getName());
		targetPlayer.sendPacket(sm);
	}
}