package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Guard;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;

public class TalkingGuard extends Guard implements Runnable
{ 
	private boolean _crazyState;
	private long _lastAggroSay;
	private long _lastNormalSay;
	private static final int _crazyChance = 1;
	private static final int _sayNormalChance = 1;
	private static final long _sayNormalPeriod = 5 * 6000;
	private static final long _sayAggroPeriod = 5 * 6000;

    // Фразы, которые может произнести гвард, когда начинает атаковать пк
    private static final String[] _sayAggroText = { 
		"{name}, will not go away, now I'm going to kill you a little bit!",
		"{name}, I'll slaughter, mom I swear!",
		"La-la-la, I'm crazy. Now all I will kill you {name}!",
		"How I killed as cut as I ruined the people! Will you, {name}, one more on the list!",
		"{name}, now I'm going to kill you!",
		"I fear, trembling in the night, I dodgy lock the basement of justice, I am fortune's favorite, I Black Guard!",
		"Wow, my future victim. This is me talking to you, {name}! Do not pretend that you're not in the business!",
		"Hurrah! For the motherland, for all my brethren! Prepare to die, {name}!",
		"{name}, your money or your life?",
		"{name}, just die, do not complicate my life!",
		"{name}, how do you prefer to die? quick and easy or slow and painful?",
		"{name}, pvp or pissed?",
		"{name}, I will kill you dearly.",
		"{name}, I'll tear as Tuzik water bottle!",
		"Prepare to die, {name}!",
		"{name}, you fight like a girl!",
		"{name}, pray before death! Although ... not have time!" };
    
    // Фразы, которые может произнести гвард, адресуя их проходящим мимо игрокам мужского пола
    private static final String[] _sayNormalTextM = { 
		"{name}, Who is it there?",
		"{name}, hello!",
		"{name}, hello!",
		"{name}, hi prativny.",
		"{name}, let arms for a moment, I want to make a screen.",
		"{name}, good hunting.",
		"{name}, in what force where you, brother?",
		"{name}, more kills on you.",
		"{name}, you give me nightmares of dreams.",
		"{name}, I know you - you have long wanted for the murder of innocent monsters.",
		"{name}, pvp or pissed?",
		"{name}, you have a purse fell out.",
		"{name}, I will not go with you on a date, do not even ask.",
		"All Smack in this chat." };
		
    // Фразы, которые может произнести гвард, адресуя их проходящим мимо игрокам женского пола
    private static final String[] _sayNormalTextF = { 
		"{name}, hello beautiful lady.",
		"{name}, wow, what are you ... uh ... eyes.",
		"{name}, do not want to walk around with this macho?",
		"{name}, hello!",
		"{name}, you are really ugly!",
		"{name}, let me touch you ... uh ... well, in general give certain things to touch.",
		"{name}, not a woman thing - to kill the enemy.",
		"{name}, you have the upper hand broke, do not light ... eyes.",
		"{name}, oh what buns you have...",
		"{name}, oh what legs you have...",
		"{name}, but yes you babe.",
		"{name}, wow, what a woman, I would be so.",
		"{name}, but what are you doing tonight?",
		"{name}, you agree that in terms of banal erudition, not every individual is able to locally-selected tendency to ignore the potential of emotions and parity allotsirovat ambivalent quanta logistics extractable given anthropomorphic heuristic genesis?",
		"{name}, offers his hand and heart. purse and after the wedding." };
    
    public TalkingGuard(NpcInstance actor){ 
		super(actor);
		setMaxPursueRange(600);
		_crazyState = false;
		_lastAggroSay = 0;
		_lastNormalSay = 0;}

    @Override
    protected void onEvtSpawn(){ 
		_lastAggroSay = 0;
		_lastNormalSay = 0;
		_crazyState = Rnd.chance(_crazyChance) ? true : false;
		super.onEvtSpawn();} 

	@Override
	public boolean checkAggression(Creature target){ 
        if(_crazyState){ 
            NpcInstance actor = getActor();
			Player player = target.getPlayer();
            if(actor == null || actor.isDead() || player == null)
                return false;
            if(player.isGM())
                return false;
            if(Rnd.chance(_sayNormalChance)){
                if (target.isPlayer() && target.getKarma() <= 0 && (_lastNormalSay + _sayNormalPeriod < System.currentTimeMillis()) && actor.isInRange(target, 250)){ 
                    Functions.npcSay(actor, target.getPlayer().getSex() == 0 ? _sayNormalTextM[Rnd.get(_sayNormalTextM.length)].replace("{name}", target.getName()) : _sayNormalTextF[Rnd.get(_sayNormalTextF.length)].replace("{name}", target.getName()));
                    _lastNormalSay = System.currentTimeMillis();}}
            if(target.getKarma() <= 0)
                return false;
            if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
                return false;
            if(_globalAggro < 0L)
                return false;
			AggroList.AggroInfo ai = actor.getAggroList().get(target);
			if(ai != null && ai.hate > 0){
				if(!target.isInRangeZ(actor.getSpawnedLoc(), getMaxHateRange()))
					return false;}
			else if(!target.isInRangeZ(actor.getSpawnedLoc(), 600))
				return false;
            if(target.isPlayable() && !canSeeInSilentMove((Playable) target))
                return false;
            if(!GeoEngine.canSeeTarget(actor, target))
                return false;
            if(target.isPlayer() && ((Player) target).isInvisible(actor))
                return false;
			if((target.isSummon() || target.isPet()) && target.getPlayer() != null)
				actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
			actor.getAggroList().addDamageHate(target, 0, 2);
            startRunningTask(2000);
            if(_lastAggroSay + _sayAggroPeriod < System.currentTimeMillis()){
                Functions.npcSay(actor, _sayAggroText[Rnd.get(_sayAggroText.length)].replace("{name}", target.getPlayer().getName()));
                _lastAggroSay = System.currentTimeMillis();}

            setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			return true;}else{super.checkAggression(target);}
		return false;}
}