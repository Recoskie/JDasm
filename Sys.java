import java.io.*;

public class Sys
{
  private static final String Sys = System.getProperty("os.name");

  public static final boolean windows = Sys.startsWith("Windows");
  public static final boolean linux = Sys.startsWith("Linux");
  public static final boolean mac = Sys.startsWith("Mac");
  public static final boolean solaris = Sys.startsWith("SunOS");
  public static final boolean iOS = Sys.startsWith("iOS");

  //Convince method to prompt user if they wish to run application as administrator (Super user).
  //Starts the application with command line arguments.

  public boolean promptAdmin( String args )
  {
    //Windows

    if( windows )
    {
      try
      {
        File f = new File(System.getProperty("java.home")); f = new File(f, "bin"); f = new File(f, "javaw.exe");

        //The java engine.
      
        String jre = f.getAbsolutePath();

        //The java application location.

        String app = new File(Sys.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();

        //Current DIR.

        String dir = System.getProperty("user.dir");

        //Create run as admin. Note WScript.shell is not rally necessary. The link can be wrote byte by byte into temp.

        f = File.createTempFile ("JFH", ".js"); PrintWriter script = new PrintWriter(f);

        script.printf("var shell = new ActiveXObject(\"WScript.Shell\"),s = shell.CreateShortcut(\"" + dir.replaceAll("\\\\","\\\\\\\\") + "\\\\" + "J.lnk\");\r\n");
        script.printf("s.TargetPath = \"" + jre.replaceAll("\\\\","\\\\\\\\") + "\";\r\n");
        script.printf("s.Arguments = \"-jar \\\"" + app.replaceAll("\\\\","\\\\\\\\") + "\\\" " + args.replaceAll("\\\\","\\\\\\\\") + "\";\r\n");
        script.printf("s.Save();");
        script.close();

        Process p = Runtime.getRuntime().exec("cscript " + f.getAbsolutePath()); p.waitFor(); f.delete();

        //Set byte value in link header to run as administrator.

        f = new File("J.lnk"); RandomAccessFile d = new RandomAccessFile( f, "rw" );
        d.seek(0x15); int i = d.read() | 0x20; d.seek(0x15); d.write( i ); d.close();

        //Start the process.

        p = Runtime.getRuntime().exec("cmd /c J.lnk");

        //Test if a new process started as administrator.

        while( p.isAlive() ) { if( !f.exists() ) { return( true ); } }

        //User declined run as administrator.
      
        f.delete(); return( false );
      }
      catch( Exception e ) { e.printStackTrace(); }
    }

    //User declined run as administrator, or operation failed.

    return( false );
  }

  //This method tests if the new process started as administrator.

  public boolean start()
  {
    boolean test = false;

    if( windows ) { File f = new File("J.lnk"); test = f.exists(); if(test) { f.delete(); } }

    return( test );
  }
}