package org.fast_db;


public class Main {

    public static void main(String[] args) {
        String port = Setting.getSettings().get(Setting.DEFAULT_PORT);
        System.out.println("FastDB is Running on port " + port);

        FastDBServer.start(Integer.parseInt(port));
    }
}