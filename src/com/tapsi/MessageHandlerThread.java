package com.tapsi;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageHandlerThread implements Runnable {

    BlockingQueue<String> queue;
    private boolean close = true;

    public MessageHandlerThread() {
        queue = new ArrayBlockingQueue<String>(1024);
    }

    // put a message from client Thread in the queue (That's thread safe!)
    public void putMessage (String msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            LogHandler.handleError(e);
        }
    }

    // Check permanently the queue for new messages to handle
    @Override
    public void run() {
        System.err.println("MessagHandlerThread started ...");
        while (close) {
            if(queue.size() > 0) {
                try {
                    System.err.println("Size: " + queue.size() + " Took message: " + queue.take());
                } catch (InterruptedException e) {
                    LogHandler.handleError(e);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LogHandler.handleError(e);
            }
        }
    }

    // Close the thread
    public void quit() {
        close = false;
    }

    // Todo: Add command handling for different commands
}
