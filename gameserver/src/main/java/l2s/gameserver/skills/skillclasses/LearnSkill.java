package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class LearnSkill
  extends Skill
{
  private final int[] _learnSkillId;
  private final int[] _learnSkillLvl;
  
  public LearnSkill(StatsSet set)
  {
    super(set);
    
    String[] ar = set.getString("learnSkillId", "0").split(",");
    int[] ar2 = new int[ar.length];
    for (int i = 0; i < ar.length; i++) {
      ar2[i] = Integer.parseInt(ar[i]);
    }
    _learnSkillId = ar2;
    
    ar = set.getString("learnSkillLvl", "1").split(",");
    ar2 = new int[_learnSkillId.length];
    for (int i = 0; i < _learnSkillId.length; i++) {
      ar2[i] = 1;
    }
    for (int i = 0; i < ar.length; i++) {
      ar2[i] = Integer.parseInt(ar[i]);
    }
    _learnSkillLvl = ar2;
  }
  
  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!(activeChar instanceof Player)) {
      return;
    }
    Player player = (Player)activeChar;
    for (int i = 0; i < _learnSkillId.length; i++) {
      if ((player.getSkillLevel(Integer.valueOf(_learnSkillId[i])) < _learnSkillLvl[i]) && (_learnSkillId[i] != 0))
      {
        Skill newSkill = SkillHolder.getInstance().getSkill(_learnSkillId[i], _learnSkillLvl[i]);
        if (newSkill != null) {
          player.addSkill(newSkill, true);
        }
      }
    }
  }
}
