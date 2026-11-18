package kelea.play.util;

import java.util.Arrays;
import java.util.Scanner;

public class ScannerUtils {

    public static Scanner scanner = new Scanner(System.in);

    public static String catchText(String input){
        System.out.println(input + " : ");
        return scanner.nextLine();
    };

    public static long catchNumber(String input){
        while (true) {
            System.out.println(input + " : ");
            String line = scanner.nextLine().trim();
            try {
                return Long.parseLong(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, please enter a number.");
            }
        }
    }

    public static double catchDecimals(String input){
        while (true) {
            System.out.println(input + " : ");
            String line = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, please enter a number.");
            }
        }
    }

    /** Like catchDecimals, but repeats until the number is between min and max (both included). */
    public static double catchDecimals(String input, double min, double max) {
        while (true) {
            double num = catchDecimals(input);
            if (num >= min && num <= max) {
                return num;
            }
            System.out.println("The number must be between " + min + " and " + max + ".");
        }
    }

    /**
     * Asks for one constant of any enum (generic: works for Genre or any other enum) and repeats until valid.
     * Matches the text shown to the user (toString), ignoring case.
     * "E extends Enum<E>" restricts the type parameter E to enum types, and Class<E> lets us list its constants.
     */
    public static <E extends Enum<E>> E catchEnum(String input, Class<E> type) {
        E[] values = type.getEnumConstants();
        while (true) {
            System.out.println(input + " " + Arrays.toString(values) + " : ");
            String line = scanner.nextLine().trim();
            for (E value : values) {
                if (value.toString().equalsIgnoreCase(line)) {
                    return value;
                }
            }
            System.out.println("Invalid option, choose one from the list.");
        }
    }
}
