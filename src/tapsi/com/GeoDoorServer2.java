package tapsi.com;

import tapsi.com.clientserver.ServerThread;
import tapsi.com.logging.GeoDoorExceptions;
import tapsi.com.logging.LogHandler;
import tapsi.com.visualization.InterfaceToVisu;
import tapsi.com.visualization.Visualization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Main class of the application and handles all command line user inputs.
 */
public class GeoDoorServer2 {

    private static InterfaceToVisu inVisu = null;
    private static ServerThread serverThread = null;
    private static boolean quit = false;
    private static Visualization visu = null;

    /**
     * Main Method of the application
     *
     * @param args
     */
    public static void main(String[] args) {

        LogHandler.setDebugMode(true);

//        new Thread() {
//            @Override
//            public void run() {
//                javafx.application.Application.launch(Visualization.class);
//            }
//        }.start();
//        visu = Visualization.waitForStartUpTest();
//        initVisuListener(visu);
//        visu.printSomething();

        // Create instance of the server
        //inVisu = new InterfaceToVisu();

        // Console Prompt
        while (!quit) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            LogHandler.printPrompt();
            try {
                String command = bufferedReader.readLine();
                checkCommand(command);
            } catch (GeoDoorExceptions ex) {
                LogHandler.handleError(ex);
            } catch (IOException e) {
                LogHandler.handleError(e);
            }
        }
    }

    // Todo: add console command: set permission

    /**
     * Checks the command provided in the command line and calls the corresponding method
     *
     * @param command
     * @throws GeoDoorExceptions
     */
    private static void checkCommand(String command) throws GeoDoorExceptions {
        switch (command) {
            case "quit":
                setQuit(true);
                break;
            case "show":
                //showHideVisu(true);
                visu.hideVisualization(false);
                break;
            case "hide":
                //showHideVisu(false);
                visu.hideVisualization(true);
                break;
            case "start": {
                try {
                    startServer();
                } catch (InterruptedException ex) {
                    LogHandler.handleError(ex);
                } catch (IOException ex) {
                    LogHandler.handleError(ex);
                }
                break;
            }
            case "debug on":
                LogHandler.setDebugMode(true);
                break;
            case "debug off":
                LogHandler.setDebugMode(false);
                break;
            case "knx on":
                serverThread.startKNXTimer();
                break;
            case "knx off":
                serverThread.stopKNXHandler();
                break;
            case "set door open":
                serverThread.setDoorStatus(true);
                break;
            case "set door closed":
                serverThread.setDoorStatus(false);
                break;
            case "message queue":
                LogHandler.printLog(new Date() + ": Message queue Size: " + serverThread.getMessageQueueSize());
                break;
            case "connected users":
                LogHandler.printLog(new Date() + ": Connected users:\n");
                serverThread.getUsers();
                break;
            case "help":
                LogHandler.printLog("quit                - disables the server and ends the program");
                LogHandler.printLog("show                - show visualisation");
                LogHandler.printLog("hide                - hide visualisation");
                LogHandler.printLog("start               - starts the server");
                LogHandler.printLog("debug on            - enables error debug messages");
                LogHandler.printLog("debug off           - disable error debug messages");
                LogHandler.printLog("knx on              - enables knx thread (door status)");
                LogHandler.printLog("knx off             - disable knx thread (door status)");
                LogHandler.printLog("set door open       - set door to open state");
                LogHandler.printLog("set door closed     - set door to closed state");
                LogHandler.printLog("message queue       - gets the current message queue size");
                LogHandler.printLog("connected users     - shows a list of the current connected users");
                break;

            // Test commands delete later
            case "knx get":
                serverThread.getKNXItem();
                break;
            default:
                throw new GeoDoorExceptions("Command not found");
        }
    }

    /**
     * Not usable in a raspberry PI3 application
     * Custom listener for a JavaFx visualization
     *
     * @param visu
     */
    static void initVisuListener (Visualization visu) {
        visu.setCustomListener(new Visualization.VisListener() {
            @Override
            public void onStart() {
                LogHandler.printLog("got onStart!");
            }
        });
    }

    /**
     * Command to start the server. Starts the server thread.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    public static void startServer() throws InterruptedException, IOException {
        serverThread = new ServerThread();
        Thread tServerThread = new Thread(serverThread);
        tServerThread.start();
    }

    /**
     * Command to stop the server. Ends the server thread.
     *
     * @throws GeoDoorExceptions
     */
    public static void stopServer() throws GeoDoorExceptions {
        if (serverThread != null)
            serverThread.quit();
        else
            throw new GeoDoorExceptions("Can't stop not started server!");
    }

    /**
     * Command to quit the application and calls stopServer.
     *
     * @param val
     */
    private static void setQuit(boolean val) {
        try {
            //visu.closeVisualization();
            //inVisu.closeVisualisation();
            stopServer();
        } catch (GeoDoorExceptions ex) {
            LogHandler.handleError(ex);
        }
        quit = val;
    }

    /**
     * @Version Windows
     * Not usable in a raspberry PI3 application.
     * Hides the JavaFX visualization.
     *
     * @param val
     */
    private static void showHideVisu(boolean val) {
        inVisu.showVisualization(val);
    }
}
