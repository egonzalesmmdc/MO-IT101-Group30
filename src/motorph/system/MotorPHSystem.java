/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorph.system;

/**
 * MotorPH Payroll System - Final Verified Version
 * @author Group 30 - Gonzales, De Pano, Villamor
 */


import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.text.DecimalFormat;
import java.math.RoundingMode;


public class MotorPHSystem {

    // --- CSV Mapping Constants ---
    static final int CSV_EMP_ID = 0;
    static final int CSV_EMP_LAST_NAME = 1;
    static final int CSV_EMP_FIRST_NAME = 2;
    static final int CSV_EMP_BIRTHDAY = 3;
    static final int CSV_EMP_HOURLY_RATE = 18;

    static final int CSV_ATT_EMP_ID = 0;
    static final int CSV_ATT_DATE = 3;
    static final int CSV_ATT_TIME_IN = 4;
    static final int CSV_ATT_TIME_OUT = 5;

    // --- Work Policy Constants ---
    static final int SHIFT_START_HOUR = 8;
    static final int MINUTES_IN_HOUR = 60;
    static final int LUNCH_THRESHOLD_MINS = 240;
    static final int LUNCH_DEDUCTION_MINS = 60;
    static final int GRACE_PERIOD_LIMIT = 10;

    static final String DEFAULT_PASSWORD = "12345";
    static final String EMPLOYEE_DATA_FILE_PATH = "Data/MotorPH_Employee Data - Employee Details.csv";
    static final String ATTENDANCE_DATA_FILE_PATH = "Data/MotorPH_Employee Data - Attendance Record.csv";

    // --- Government Deductions Settings (Magic Number Fixes) ---
    // These constants replace hardcoded values in calculation methods for better maintenance.
    static final double SSS_MAXIMUM_CONTRIBUTION_CAP = 1125.0; 
    static final double MIN_PHILHEALTH_CONTRIBUTION_SALARY_THRESHOLD = 10000.0;
    static final double MAX_PHILHEALTH_SALARY_THRESHOLD = 60000.0;
    static final double MIN_PHILHEALTH_PREMIUM = 300.0;
    static final double MAX_PHILHEALTH_PREMIUM = 1800.0;
    static final double PHILHEALTH_PREMIUM_RATE = 0.03;
    static final double PAGIBIG_LOWER_SALARY_THRESHOLD = 1500.0;
    static final double PAGIBIG_LOWER_RATE = 0.01;
    static final double PAGIBIG_UPPER_RATE = 0.02;
    static final double MAX_PAGIBIG_CONTRIBUTION_CAP = 100.0;

    static List<String[]> employeeList = new ArrayList<>();
    static List<String[]> attendanceList = new ArrayList<>();

    static final DecimalFormat moneyFormat = new DecimalFormat("#,##0.############");
    static final DecimalFormat hoursFormat = new DecimalFormat("#,##0.############");

    static {
        moneyFormat.setRoundingMode(RoundingMode.DOWN);
        hoursFormat.setRoundingMode(RoundingMode.DOWN);
    }

    public static void main(String[] args) {
        loadEmployeeData(EMPLOYEE_DATA_FILE_PATH);
        loadAttendanceData(ATTENDANCE_DATA_FILE_PATH);

        Scanner inputScanner = new Scanner(System.in);
        System.out.println("------| MOTORPH SYSTEM LOGIN |------");

        boolean isLoggedIn = false;
        while (!isLoggedIn) {
            System.out.print("Username: ");
            String username = inputScanner.nextLine();
            System.out.print("Password: ");
            String password = inputScanner.nextLine();

            if ((username.equals("employee") || username.equals("payroll_staff")) && password.equals(DEFAULT_PASSWORD)) {
                isLoggedIn = true;
                if (username.equals("employee")){
                    viewEmployeeProfile(inputScanner);
                } else {
                    launchPayrollMenu(inputScanner);
                }
            } else {
                System.out.println("Error: Incorrect Username and/or Password");
            }
        }
    }

    static void viewEmployeeProfile(Scanner inputScanner) {
        System.out.println("\nWelcome!");
        while (true) {
            System.out.print("Enter Employee Number: ");
            String employeeNumber = inputScanner.nextLine();
            int index = findEmployeeIndex(employeeNumber);
            if (index == -1) {
                System.out.println("Error: Employee Number Does Not Exist");
                continue;
            }
            printEmployeeDetails(employeeList.get(index));
            break;
        }
        System.exit(0);
    }

    static void printEmployeeDetails(String[] details) {
        System.out.println("\n------| EMPLOYEE DETAILS |------");
        System.out.println("Employee Number: " + details[CSV_EMP_ID]);
        System.out.println("Employee Full Name: " + details[CSV_EMP_FIRST_NAME] + " " + details[CSV_EMP_LAST_NAME]);
        System.out.println("Birthday: " + formatBirthdayDate(details[CSV_EMP_BIRTHDAY]));
        System.out.println("-----------------------------------");
        System.out.println("\n===================================");
    }

    static void launchPayrollMenu(Scanner inputScanner) {
        System.out.println("\nWelcome!");
        System.out.println("1. Process Payroll \n2. Exit");
        int choice = getSafeInt(inputScanner, "Select: ", 1, 2);

        if (choice == 2) System.exit(0);

        System.out.println("\n1. One Employee \n2. All Employees \n3. Exit");
        int subChoice = getSafeInt(inputScanner, "Select: ", 1, 3);

        if (subChoice == 1) {
            while (true) {
                System.out.print("Enter Employee Number: ");
                String employeeNumber = inputScanner.nextLine();
                int index = findEmployeeIndex(employeeNumber);
                if (index != -1) {
                    generatePayrollReport(index);
                    break;
                } else {
                    System.out.println("Error: Employee Number Not Found. Please try again.");
                }
            }
        } else if (subChoice == 2) {
            for (int i = 0; i < employeeList.size(); i++) generatePayrollReport(i);
            System.out.println("\nPayroll Processing Status: Finished");
        }
        System.exit(0);
    }

    static String formatCutoffPeriod(String monthLabel, int startDay, int endDay) {
        // Uses EN DASH (–) instead of hyphen (-) per strict requirements.
        return monthLabel + " " + startDay + "–" + endDay;
    }

    static void generatePayrollReport(int employeeIndex) {
        String[] employeeDetails = employeeList.get(employeeIndex);
        printEmployeeDetails(employeeDetails);
        System.out.println("\n======| Processed Payroll |======");
        double hourlyRate = Double.parseDouble(employeeDetails[CSV_EMP_HOURLY_RATE].replace(",", ""));

        for (int month = 6; month <= 12; month++) {
            processMonthlyPayroll(employeeDetails[CSV_EMP_ID], month, hourlyRate);
        }
    }

    static void processMonthlyPayroll(String employeeID, int month, double rate) {
        String monthLabel = getMonthNameLabel(month);

        // 1st Cutoff Processing
        double firstCutoffHours = getTotalHoursWorked(employeeID, month, 1, 15);
        double firstCutoffGross = firstCutoffHours * rate;
        printPayrollResults(formatCutoffPeriod(monthLabel, 1, 15), firstCutoffHours, firstCutoffGross, firstCutoffGross);

        // 2nd Cutoff Processing
        int lastDay = (month == 6 || month == 9 || month == 11) ? 30 : 31;
        double hours2 = getTotalHoursWorked(employeeID, month, 16, lastDay);
        double gross2 = hours2 * rate;
        double monthlyGross = firstCutoffGross + gross2;

        if (monthlyGross > 0) {
            double sss = calculateSSS(monthlyGross);
            double philHealth = calculatePhilHealth(monthlyGross);
            double pagIbig = calculatePagIbig(monthlyGross);
            double withholdingTax = calculateWithholdingTax(monthlyGross, sss, philHealth, pagIbig);
            double netSalary = gross2 - (sss + philHealth + pagIbig + withholdingTax);

            printPayrollResults(formatCutoffPeriod(monthLabel, 16, lastDay), hours2, gross2, netSalary);
            printDeductionBreakdown(sss, philHealth, pagIbig, withholdingTax, netSalary);
        } else {
            printPayrollResults(formatCutoffPeriod(monthLabel, 16, lastDay), hours2, gross2, gross2);
        }
    }

    static void loadEmployeeData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null ){
                String[] employeeRecord = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (employeeRecord.length > CSV_EMP_HOURLY_RATE) {
                    for (int i = 0; i < employeeRecord.length; i++) employeeRecord[i] = employeeRecord[i].replace("\"", "").trim();
                    employeeList.add(employeeRecord);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading employees: " + e.getMessage());
        }
    }

    static void loadAttendanceData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null ){
                String[] employeeRecord = line.split(",");
                if (employeeRecord.length > CSV_ATT_TIME_OUT) attendanceList.add(employeeRecord);
            }
        } catch (IOException e) {
            System.out.println("Error loading attendance: " + e.getMessage());
        }
    }

    static double computeDailyWorkHours(String timeInStr, String timeOutStr) {
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("H:mm");
            LocalTime in = LocalTime.parse(timeInStr.trim(), format);
            LocalTime out = LocalTime.parse(timeOutStr.trim(), format);
            LocalTime start = LocalTime.of(SHIFT_START_HOUR, 0);
            LocalTime graceLimit = LocalTime.of(SHIFT_START_HOUR, GRACE_PERIOD_LIMIT);

            LocalTime effectiveIn = (in.isBefore(start) || !in.isAfter(graceLimit)) ? start : in;
            LocalTime effectiveOut = out.isAfter(LocalTime.of(17, 0)) ? LocalTime.of(17, 0) : out;

            if (effectiveOut.isBefore(effectiveIn)) return 0.0;
            long totalMinutes = Duration.between(effectiveIn, effectiveOut).toMinutes();

            if (totalMinutes > LUNCH_THRESHOLD_MINS) totalMinutes -= LUNCH_DEDUCTION_MINS;
            return Math.max(0, totalMinutes / (double) MINUTES_IN_HOUR);
        } catch (Exception e) {
            System.out.println("Warning: Invalid time format encountered. Skipping record.");
            return 0.0;
        }
    }

    static void printDeductionBreakdown(double sss, double philHealth, double pagIbig, double tax, double net) {
        System.out.println("EACH DEDUCTION:");
        System.out.println("- SSS: PHP " + moneyFormat.format(sss));
        System.out.println("- PhilHealth: PHP " + moneyFormat.format(philHealth));
        System.out.println("- Pag-IBIG: PHP " + moneyFormat.format(pagIbig));
        System.out.println("- Withholding Tax: PHP " + moneyFormat.format(tax));
        System.out.println("TOTAL DEDUCTIONS : PHP " + moneyFormat.format(sss + philHealth + pagIbig + tax));
        System.out.println("|====================================|");
    }

    static void printPayrollResults(String period, double hours, double gross, double net) {
        System.out.println("Period            : " + period);
        System.out.println("Total Hours Worked: " + hoursFormat.format(hours) + " hours");
        System.out.println("Gross Salary      : PHP " + moneyFormat.format(gross));
        System.out.println("Net Salary        : PHP " + moneyFormat.format(net));
        System.out.println("-----------------------------------");
    }

    static int getSafeInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println("Error: Invalid Choice. Please Select A Valid Option From The List..." + min + "-" + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
    }

    static int findEmployeeIndex(String employeeId) {
        for (int i = 0; i < employeeList.size(); i++) if (employeeList.get(i)[CSV_EMP_ID].equals(employeeId)) return i;
        return -1;
    }

    static String formatBirthdayDate(String dateStr) {
        String[] parts = dateStr.split("/");
        return getMonthNameLabel(Integer.parseInt(parts[0])) + " " + parts[1] + ", " + parts[2];
    }

    static String getMonthNameLabel(int monthNum) {
        String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return months[monthNum];
    }

    static double getTotalHoursWorked(String id, int month, int startDay, int endDay) {
        double totalHours = 0;
        for (String[] record : attendanceList) {
            if (!record[CSV_ATT_EMP_ID].equals(id)) continue;
            String[] dateParts = record[CSV_ATT_DATE].split("/");
            int recordMonth = Integer.parseInt(dateParts[0]);
            int recDay = Integer.parseInt(dateParts[1]);

            if (recordMonth == month && recDay >= startDay && recDay <= endDay) {
                totalHours += computeDailyWorkHours(record[CSV_ATT_TIME_IN], record[CSV_ATT_TIME_OUT]);
            }
        }
        return totalHours;
    }

    public static double calculateSSS(double monthlyGross) {
        double[][] sssTable = { {3250, 135.0}, {3750, 157.5}, {4250, 180.0},
            {4750, 202.5}, {5250, 225.0}, {5750, 247.5}, {6250, 270.0},
            {6750,292.5}, {7250, 315.0}, {7750, 337.5}, {8250, 360.0},
            {8750, 382.5}, {9250, 405.0}, {9750, 427.5}, {10250, 450.0},
            {10750, 472.5}, {11250,495.0}, {11750, 517.5}, {12250, 540.0},
            {12750, 562.5}, {13250, 585.0}, {13750, 607.5}, {14250, 630.0},
            {14750, 652.5}, {15250, 675.0}, {15750, 697.5}, {16250, 720.0},
            {16750, 742.5}, {17250, 765.0}, {17750, 787.5}, {18250, 810.0},
            {18750, 832.5}, {19250, 855.0}, {19750, 877.5}, {20250, 900.0},
            {20750, 922.5}, {21250, 945.0}, {21750, 967.5}, {22250, 990.0},
            {22750, 1012.5}, {23250, 1035.0}, {23750, 1057.5}, {24250, 1080.0},
            {24750, 1102.5} };
        for (double[] bracket : sssTable) if (monthlyGross < bracket[0]) return bracket[1];
        return SSS_MAXIMUM_CONTRIBUTION_CAP;
    }

    public static double calculatePhilHealth(double monthlyGross) {
        if (monthlyGross <= MIN_PHILHEALTH_CONTRIBUTION_SALARY_THRESHOLD) return MIN_PHILHEALTH_PREMIUM / 2;
        if (monthlyGross < MAX_PHILHEALTH_SALARY_THRESHOLD) return (monthlyGross * PHILHEALTH_PREMIUM_RATE) / 2;
        return MAX_PHILHEALTH_PREMIUM / 2;
    }

    public static double calculatePagIbig(double monthlyGross) {
        double rate = (monthlyGross <= PAGIBIG_LOWER_SALARY_THRESHOLD) ? PAGIBIG_LOWER_RATE : PAGIBIG_UPPER_RATE;
        return Math.min(monthlyGross * rate, MAX_PAGIBIG_CONTRIBUTION_CAP);
    }

    public static double calculateWithholdingTax(double monthlyGross, double sss, double philHealth, double pagIbig) {
        double taxableIncome = monthlyGross - (sss + philHealth + pagIbig);
        if (taxableIncome <= 20832) return 0;
        if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome < 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        if (taxableIncome < 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666667) * 0.35;
    }
}
