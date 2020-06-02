package l2s.gameserver.utils;

import java.util.LinkedList;

public class MaxElementsList<E>
  extends LinkedList<E>
{
  private final int MAX;
  
  public MaxElementsList(int maxElements)
  {
    MAX = maxElements;
  }
  
  public boolean add(E e)
  {
    if (size() + 1 > MAX) {
      removeFirst();
    }
    super.addLast(e);
    return true;
  }
}
