package org.example.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        String str = "";
        while(str.isEmpty()) {
            try {
                str = reader.readLine();
            } catch (Exception e) {
                System.out.println("An error occurred while trying to enter text. Try again.");
            }
        }
        return str;
    }

    public static int readInt() {
        String str;
        int res;
        while(true){
            try{
                str = readString();
                res = Integer.parseInt(str);
                break;
            } catch (NumberFormatException ex) {
                writeMessage("An error occurred while trying to enter a number. Try again.");
            }
        }
        return res;
    }
}
