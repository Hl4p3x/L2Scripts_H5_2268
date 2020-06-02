package l2s.gameserver.model.entity.poll;

import java.util.List;

import l2s.gameserver.model.Player;

public class Poll
{
	private String _question;
	private PollAnswer[] _answers;
	private long _endTime;
	private int lastId = 1;
  
	protected Poll(String question)
	{
		_question = question;
		_answers = new PollAnswer[0];
	}
  
	protected Poll(String question, List<PollAnswer> answers, long endTime)
	{
		_question = question;
		_answers = convertAnswers(answers);
		_endTime = endTime;
	}
  
	public String getQuestion()
	{
		return _question;
	}
  
	public PollAnswer[] getAnswers()
	{
		return _answers;
	}
  
	public long getEndTime()
	{
		return _endTime;
	}
  
	public void setQuestion(String question)
	{
		_question = question;
	}
  
	public void addVote(Player player, int answerId)
	{
		PollAnswer newAnswer = getAnswerById(answerId);
		PollAnswer oldAnswer = getAnswerById(player.getHwidGamer().getPollAnswer());
    
		if (oldAnswer != null)
			oldAnswer.decreaseVotes();
		if(newAnswer != null)
		{
			player.getHwidGamer().setPollAnswer(answerId, true);
			newAnswer.increaseVotes();
		}
		player.sendMessage("Thank You!");
	}
  
	public PollAnswer getAnswerById(int answerId)
	{
		for (PollAnswer answer : getAnswers())
		{
			if (answer.getId() == answerId)
				return answer;
		}
		return null;
	}
  
	public String getPollEndDate()
	{
		long pollTime = _endTime < System.currentTimeMillis() - 360000000L ? System.currentTimeMillis() + _endTime : _endTime;
		long timeDifference = pollTime - System.currentTimeMillis();
		timeDifference /= 1000L;  
		if(timeDifference < 0L)
			return "";
    
		int days = (int)Math.floor(timeDifference / 24L / 60L / 60L);
		timeDifference -= days * 24 * 60 * 60;
		int hours = (int)Math.floor(timeDifference / 60L / 60L);
		timeDifference -= hours * 60 * 60;
		int minutes = (int)Math.floor(timeDifference / 60L);
    
		StringBuilder builder = new StringBuilder();
		if(days > 0)
			builder.append(days).append(" day").append(days > 1 ? "s" : "");
		if(hours > 0)
			builder.append(builder.length() == 0 ? "" : ", ").append(hours).append(" hour").append(hours > 1 ? "s" : "");
		if(minutes > 0)
			builder.append(builder.length() == 0 ? "" : ", ").append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
		return builder.toString();
	}
  
	public void setEndTime(long time)
	{
		_endTime = time;
		if(PollEngine.getInstance().isActive())
		{
			_endTime = (System.currentTimeMillis() + time);
			PollEngine.getInstance().startThread();
		}
	}
  
	public void addAnswers(PollAnswer[] answers)
	{
		_answers = answers;
	}
  
	public void addAnswers(List<PollAnswer> answers)
	{
		addAnswers(convertAnswers(answers));
	}
  
	public void addAnswer(String answerTitle)
	{
		int id = getNewAnswerId();
		PollAnswer newAnswer = new PollAnswer(id, answerTitle, 0);
		addNewAnswerToAnwers(newAnswer);
	}
  
	public void deleteAnswer(int id)
	{
		PollAnswer[] leftAnswers = new PollAnswer[_answers.length - 1];
		int count = 0;
		for (int i = 0; i < _answers.length; i++)
		{
			if(_answers[i].getId() != id)
			{
				leftAnswers[count] = _answers[i];
				count++;
			}
		}
		_answers = leftAnswers;
	}
  
	protected int getNewAnswerId()
	{
		return lastId++;
	}
  
	private void addNewAnswerToAnwers(PollAnswer answer)
	{
		PollAnswer[] newAnswers = new PollAnswer[_answers.length + 1];
		for(int i = 0; i < _answers.length; i++)
			newAnswers[i] = _answers[i];
		newAnswers[(newAnswers.length - 1)] = answer;
		_answers = newAnswers;
	}
  
	private static PollAnswer[] convertAnswers(List<PollAnswer> answers)
	{
		PollAnswer[] convertedAnswers = new PollAnswer[answers.size()];
		for(int i = 0; i < answers.size(); i++)
			convertedAnswers[i] = answers.get(i);
		return convertedAnswers;
	}
}
