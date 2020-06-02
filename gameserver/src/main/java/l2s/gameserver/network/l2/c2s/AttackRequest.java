package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;

public class AttackRequest extends L2GameClientPacket
{
	// cddddc
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _attackId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC(); // 0 for simple click   1 for shift-click
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			//System.out.println("1");
			return;
		}
		activeChar.setActive();

		if(activeChar.isOutOfControl())
		{
			//System.out.println("2");
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.getPlayerAccess().CanAttack)
		{
			//System.out.println("3");
			activeChar.sendActionFailed();
			return;
		}

		GameObject target = activeChar.getVisibleObject(_objectId);
		if(target == null)
		{
			//System.out.println("4");
			activeChar.sendActionFailed();
			return;
		}
		
		if(activeChar.isPendingOlyEnd() || activeChar.getPendingLfcEnd())
		{
			activeChar.sendActionFailed();
			return;		
		}
		
		if(activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != target && !activeChar.getAggressionTarget().isDead())
		{
			//System.out.println("5");
			activeChar.sendActionFailed();
			return;
		}

		if(target.isPlayer() && (activeChar.isInBoat() || target.isInBoat()))
		{
			//System.out.println("6");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTarget() != target)
		{
			//System.out.println("7");
			target.onAction(activeChar, _attackId == 1);
			return;
		}
		
		if(target.isPlayer() && Config.TOW_INITED)
		{
			if(activeChar.isSameRace((Player) target))
			{
				activeChar.sendActionFailed();
				return;
			}		
		}
		
		if(target.getObjectId() != activeChar.getObjectId() && !activeChar.isInStoreMode() && !activeChar.isProcessingRequest())
		{
			//System.out.println("8");
			target.onForcedAttack(activeChar, _attackId == 1);
		}	
		//System.out.println("base");	
	}
}