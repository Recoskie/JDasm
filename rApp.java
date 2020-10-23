import java.io.*;

public class rApp
{
  //Convince method to prompt user if they wish to run application as administrator (Super user).
  //The argument is say the disk, or file to open.

  public boolean win( String args )
  {
    try
    {
      File f = new File(System.getProperty("java.home")); f = new File(f, "bin"); f = new File(f, "javaw.exe");

      //The java engine.
      
      String jre = f.getAbsolutePath();

      //The java application location.

      String app = new File(rApp.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();

      //Current DIR.

      String dir = System.getProperty("user.dir");

      //Create run as admin. Originally was going to do this byte by byte. However WScript is better.

      f = File.createTempFile ("JFH", ".js"); PrintWriter script = new PrintWriter(f);

      script.printf("var shell = new ActiveXObject(\"WScript.Shell\"),s = shell.CreateShortcut(\"" + dir.replaceAll("\\\\","\\\\\\\\") + "\\\\" + "J.lnk\");\r\n");
      script.printf("s.TargetPath = \"" + jre.replaceAll("\\\\","\\\\\\\\") + "\";\r\n");
      script.printf("s.Arguments = \"-jar " + app.replaceAll("\\\\","\\\\\\\\") + " " + args + "\";\r\n");
      script.printf("s.Save();");
      script.close();

      Process p = Runtime.getRuntime().exec("cscript " + f.getAbsolutePath()); p.waitFor(); f.delete();

      //Set byte value in link header to run as administrator.

      f = new File("J.lnk"); RandomAccessFile d = new RandomAccessFile( f, "rw" );
      d.seek(0x15); int i = d.read() | 0x20; d.seek(0x15); d.write( i ); d.close();

      //Start the process.

      p = Runtime.getRuntime().exec("cmd /c J.lnk");

      //Test if a new process started as administrator.

      while( p.isAlive() )
      {
        if( !f.exists() ) { return( true ); }
      }

      //User declined run as administrator.
      
      f.delete(); return( false );
    }
    catch( Exception e ) { e.printStackTrace(); }

    //User declined run as administrator, or operation failed.

    return( false );
  }
}