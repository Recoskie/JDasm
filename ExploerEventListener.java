import RandomAccessFileV.*;

public interface ExploerEventListener
{
  public void ElementOpen(String Element);
  public void Read(String File, RandomAccessFileV file);
}