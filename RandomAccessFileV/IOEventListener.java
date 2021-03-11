package RandomAccessFileV;
import java.util.*;

//Basic IO Events.

public interface IOEventListener extends EventListener
{
  public void onSeek( IOEvent evt );
  public void onRead( IOEvent evt );
  public void onWrite( IOEvent evt );
}