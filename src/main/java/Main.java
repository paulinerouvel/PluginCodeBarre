import plugin.Plugin;

public class Main extends Plugin {
    //@Override
    public void run() {
       /* CodeReaderPlugin code = new CodeReaderPlugin();
        code.launch();*/

       System.out.println("couou!");
    }

    //@Override
    public void close() {
        System.out.println("Closing...");
    }
}
