package com.tapsi;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageHandlerThread implements Runnable {

    BlockingQueue<String> queue;
    private boolean close = true;
    DBHandler dbHandler = null;
    KNXHandler knxHandler = null;

    private messageListener listener;

    public interface messageListener {
        public void onClientAnswer(String oldThreadID, String threadID, String message);
    }

    public void setCustomListener(messageListener listener) {
        this.listener = listener;
    }

    public MessageHandlerThread(DBHandler dbHandler, KNXHandler knxHandler) {
        this.dbHandler = dbHandler;
        this.knxHandler = knxHandler;
        queue = new ArrayBlockingQueue<String>(1024);
    }

    // put a message from client Thread in the queue (That's thread safe!)
    public void putMessage(String msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            LogHandler.handleError(e);
        }
    }

    // Check permanently the queue for new messages to handle
    @Override
    public void run() {
        System.out.println("MessagHandlerThread started ...");
        while (close) {
            if (queue.size() > 0) {
                try {
                    String message = queue.take();
                    System.out.println(new Date() + ": Took message: " + message);

                    String threadID = null;
                    String command = null;
                    if (message.contains("#")) {
                        threadID = message.substring(0, message.indexOf("#"));
                        message = message.replace(threadID + "#", "");
                        command = message.substring(0, message.indexOf(":"));
                        message = message.replace(command + ":", "");

                        switch (command) {
                            case "register":
                                commandRegister(threadID, message);
                                break;
                            case "output":
                                commandOutput(message);
                                break;
                            case "pong":
                                System.out.println(message);
                                break;
                            default:
                                throw new GeoDoorExceptions("Sent socket command doesn't exist");
                        }
                    } else
                        throw new GeoDoorExceptions("No identifier found!");

                } catch (InterruptedException e) {
                    LogHandler.handleError(e);
                } catch (GeoDoorExceptions geoDoorExceptions) {
                    LogHandler.handleError(geoDoorExceptions);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LogHandler.handleError(e);
            }
        }
    }

    void commandOutput(String message) {
        String messageTemp = message;
        String msg = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(msg + "-", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        if (checkPhoneID)
            if (checkAllowed) {
                switch (msg) {
                    case "Gate1 open":
                        try {
                            knxHandler.setItem("eg_tor","ON");
                        } catch (IOException e) {
                           LogHandler.handleError(e);
                        }
                        break;
                    case "Door1 open":
                        try {
                            knxHandler.setItem("eg_tuer","ON");
                        } catch (IOException e) {
                            LogHandler.handleError(e);
                        }
                        break;
                    default:
                        try {
                            throw new GeoDoorExceptions("Output command doesn't exist");
                        } catch (GeoDoorExceptions geoDoorExceptions) {
                            LogHandler.handleError(geoDoorExceptions);
                        }
                }
            }
    }

    void commandRegister(String threadID, String message) {

        String messageTemp = message;
        String name = messageTemp.substring(0, messageTemp.indexOf("-"));
        messageTemp = messageTemp.replace(name + "-", "");

        String phoneId = messageTemp;

        boolean checkPhoneID = dbHandler.checkClientByPhoneID(phoneId);
        boolean checkAllowed = dbHandler.checkAllowedByPhoneID(phoneId);

        String oldThreadID = dbHandler.selectThreadIDByName(name);

        if (checkPhoneID) {
            if (!checkAllowed) {
                listener.onClientAnswer(oldThreadID, threadID, "answer:not yet allowed");
            }
            else
                listener.onClientAnswer(oldThreadID, threadID,  "answer:allowed");
        } else {
            listener.onClientAnswer(oldThreadID, threadID, "answer:registered ... waiting for permission");
        }
        // Will automatically insert or update
        dbHandler.insertClient(name, phoneId, threadID);
    }

    // Close the thread
    public void quit() {
        close = false;
    }
}
