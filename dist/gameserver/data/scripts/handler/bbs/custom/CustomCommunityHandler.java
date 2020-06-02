package handler.bbs.custom;

import handler.bbs.ScriptsCommunityHandler;

/**
 * @author Bonux
**/
public abstract class CustomCommunityHandler extends ScriptsCommunityHandler
{
	@Override
	public void onLoad()
	{
		if(BBSConfig.CUSTOM_COMMUNITY_ENABLED)
			super.onLoad();
	}
}
