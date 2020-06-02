package services;

import l2s.gameserver.Config;
import l2s.gameserver.dao.RefferalDao;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;

public class dummyLoader extends Functions implements ScriptFile
{

	@Override
	public void onLoad()
	{
		if(Config.ALLOW_REFFERAL_SYSTEM)
		{
			RefferalDao.getInstance().loadRefferals();
			RefferalDao.getInstance().startSaveTask();
		}	
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{
		if(Config.ALLOW_REFFERAL_SYSTEM)
			RefferalDao.getInstance().SaveRef();
	}
}