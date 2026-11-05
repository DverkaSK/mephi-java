import java.util.Scanner;

public final class Calculator {

    private static final String RESET_COMMAND = "c";
    private static final String STOP_COMMAND = "s";

    private Calculator() {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printHelp();

        double result = 0;
        boolean hasFirstOperand = false;

        while (true) {
            if (!hasFirstOperand) {
                System.out.print("Введите число: ");
                String input = readInput(scanner);
                if (input == null || isStopCommand(input)) {
                    break;
                }
                if (isResetCommand(input)) {
                    System.out.println("Результат сброшен: 0");
                    continue;
                }
                Double firstOperand = parseNumber(input);
                if (firstOperand == null) {
                    System.out.println("Ошибка: " + input + " не число");
                    continue;
                }
                result = firstOperand;
                hasFirstOperand = true;
            }

            System.out.print("Введите операцию (+, -, *, /): ");
            String operationInput = readInput(scanner);
            if (operationInput == null || isStopCommand(operationInput)) {
                break;
            }
            if (isResetCommand(operationInput)) {
                result = 0;
                hasFirstOperand = false;
                System.out.println("Результат сброшен: 0");
                continue;
            }
            if (operationInput.length() != 1 || !isSupportedOperation(operationInput.charAt(0))) {
                System.out.println("Ошибка: операция " + operationInput + " не поддерживается");
                continue;
            }
            char operation = operationInput.charAt(0);

            System.out.print("Введите второе число: ");
            String secondInput = readInput(scanner);
            if (secondInput == null || isStopCommand(secondInput)) {
                break;
            }
            if (isResetCommand(secondInput)) {
                result = 0;
                hasFirstOperand = false;
                System.out.println("Результат сброшен: 0");
                continue;
            }
            Double secondOperand = parseNumber(secondInput);
            if (secondOperand == null) {
                System.out.println("Ошибка: " + secondInput + " не число");
                continue;
            }
            if (operation == '/' && secondOperand == 0) {
                System.out.println("Ошибка: деление на ноль. Результат: " + formatNumber(result));
                continue;
            }

            result = calculate(result, operation, secondOperand);
            System.out.println("Результат: " + formatNumber(result));
        }

        System.out.println("Калькулятор выключен");
    }

    private static void printHelp() {
        System.out.println("Калькулятор");
        System.out.println("Операции: +, -, *, /");
        System.out.println("C - сброс, S - выход");
        System.out.println();
    }

    private static String readInput(Scanner scanner) {
        if (!scanner.hasNext()) {
            return null;
        }
        return scanner.next();
    }

    private static boolean isResetCommand(String input) {
        return input.equalsIgnoreCase(RESET_COMMAND);
    }

    private static boolean isStopCommand(String input) {
        return input.equalsIgnoreCase(STOP_COMMAND);
    }

    private static Double parseNumber(String input) {
        try {
            double number = Double.parseDouble(input.replace(',', '.'));
            return Double.isFinite(number) ? number : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isSupportedOperation(char operation) {
        switch (operation) {
            case '+':
            case '-':
            case '*':
            case '/':
                return true;
            default:
                return false;
        }
    }

    private static double calculate(double firstOperand, char operation, double secondOperand) {
        switch (operation) {
            case '+':
                return add(firstOperand, secondOperand);
            case '-':
                return subtract(firstOperand, secondOperand);
            case '*':
                return multiply(firstOperand, secondOperand);
            case '/':
                return divide(firstOperand, secondOperand);
            default:
                throw new IllegalArgumentException("Неподдерживаемая операция: " + operation);
        }
    }

    private static double add(double firstOperand, double secondOperand) {
        return firstOperand + secondOperand;
    }

    private static double subtract(double firstOperand, double secondOperand) {
        return firstOperand - secondOperand;
    }

    private static double multiply(double firstOperand, double secondOperand) {
        return firstOperand * secondOperand;
    }

    private static double divide(double firstOperand, double secondOperand) {
        return firstOperand / secondOperand;
    }

    private static String formatNumber(double number) {
        if (number % 1 == 0) {
            return String.valueOf((long) number);
        }
        return String.valueOf(number);
    }
}
