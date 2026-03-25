/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorph.system;

/**
 * MotorPH Payroll System
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

public class MotorPHSystem {
    
    // --- CSV Column Indexes - Employee Details ---
    static final int CSV_EMP_ID = 0;
    static final int CSV_EMP_LAST_NAME = 1;
    static final int CSV_EMP_FIRST_NAME = 2;
    static final int CSV_EMP_BIRTHDAY = 3;
    static final int CSV_EMP_HOURLY_RATE = 18;

    // --- CSV Column Indexes - Attendance ---
    static final int CSV_ATT_EMP_ID = 0;
    static final int CSV_ATT_DATE = 3;
    static final int CSV_ATT_TIME_IN = 4;
    static final int CSV_ATT_TIME_OUT = 5;

    // --- Payroll Settings ---
    static final int PAYROLL_START_MONTH = 6;
    static final int PAYROLL_END_MONTH = 12;
    static final int FIRST_CUTOFF_START_DAY = 1;
    static final int FIRST_CUTOFF_END_DAY = 15;
    static final int SECOND_CUTOFF_START_DAY = 16;
    
    // --- Time Settings ---
    static final int SHIFT_START_HOUR = 8;
    static final int SHIFT_END_HOUR = 17;
    static final int MINUTES_IN_HOUR = 60;
    static final int LUNCH_THRESHOLD_MINS = 240; 
    static final int LUNCH_DEDUCTION_MINS = 60;
    static final int GRACE_PERIOD_LIMIT = 10;
    
    // --- System Settings ---
    static final String DEFAULT_PASSWORD = "12345";
    static final String EMPLOYEE_DATA_FILE_PATH = "Data/MotorPH_Employee Data - Employee Details.csv";
    static final String ATTENDANCE_DATA_FILE_PATH = "Data/MotorPH_Employee Data - Attendance Record.csv";

    // --- Government Deductions Settings ---
    static final double MAXIMUM_CONTRIBUTION_CAP = 1125.0; // SSS
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
    
    // Formatters
    static final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    static final DecimalFormat hoursFormat = new DecimalFormat("#,##0.##");

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
            
            // Applied DEFAULT_PASSWORD constant
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
    
    static void viewEmployeeProfile(Scanner inputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        
        while (true) {
            System.out.print("\nEnter Employee Number: ");
            String employeeNumber = inputScanner.nextLine();
            int employeeIndex = findEmployeeIndex(employeeNumber); 
            
            if (employeeIndex == -1) {
                System.out.println("Error: Employee Number Does Not Exist"); 
                continue;
            }
            
            String[] employeeDetails = employeeList.get(employeeIndex);
            System.out.println("");
            System.out.println("------| EMPLOYEE DETAILS |------");
            // Replaced hardcoded array indexes with Constants
            System.out.println("Employee Number: " + employeeDetails[CSV_EMP_ID]);
            System.out.println("Employee Full Name: " + employeeDetails[CSV_EMP_FIRST_NAME] + " " + employeeDetails[CSV_EMP_LAST_NAME]);
            System.out.println("Birthday: " + formatBirthdayDate(employeeDetails[CSV_EMP_BIRTHDAY]));
            break; 
        }
        System.exit(0);
    }
    
    static void launchPayrollMenu(Scanner inputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        System.out.println("\n1. Process Payroll \n2. Exit the Program");
        
        int choice = getSafeInt(inputScanner, "Select: ", 1, 2);

        if (choice == 2) {
            System.out.println("Thank You!");
            System.out.println("------| EXITED PROGRAM |------");
            System.exit(0);
        }

        System.out.println("\n1. One Employee \n2. All Employees \n3. Exit the Program");
        int subChoice = getSafeInt(inputScanner, "Select: ", 1, 3);
        
        switch (subChoice) {
            case 1 -> {
                while (true) {
                    System.out.print("Enter Employee Number: ");
                    String employeeNumber = inputScanner.nextLine();
                    int employeeIndex = findEmployeeIndex(employeeNumber);
                    
                    if (employeeIndex != -1) {
                        generatePayrollReport(employeeIndex);
                        break; 
                    } else {
                        System.out.println("Error: Employee Number Does Not Exist"); 
                    }
                }
            }
            case 2 -> {
                for (int i = 0; i < employeeList.size(); i++) {
                    generatePayrollReport(i);
                } 
                System.out.println("\nPayroll Processing Status: Finished");
            }
            case 3 -> {
                System.out.println("Thank You!");
                System.out.println("------| EXITED PROGRAM |------");
                System.exit(0);
            }
        }
        System.exit(0);
    }

    static void generatePayrollReport(int employeeIndex) {
        String[] currentEmployee = employeeList.get(employeeIndex);
        
        // Replaced hardcoded array indexes with Constants
        String employeeNumber = currentEmployee[CSV_EMP_ID];
        String firstName = currentEmployee[CSV_EMP_FIRST_NAME];
        String lastName = currentEmployee[CSV_EMP_LAST_NAME];
        double hourlyRate = Double.parseDouble(currentEmployee[CSV_EMP_HOURLY_RATE].replace(",", "")); 
        
        System.out.println("\n======| EMPLOYEE DETAILS |======");
        System.out.println("Employee Number: " + employeeNumber);
        System.out.println("Employee Name: " + firstName + " " + lastName);
        System.out.println("Birthday: " + formatBirthdayDate(currentEmployee[CSV_EMP_BIRTHDAY]));
        System.out.println("================================");
        System.out.println("\n======| Proccessed Payroll |======");
        
        // Applied Payroll Month Constants
        for (int month = PAYROLL_START_MONTH; month <= PAYROLL_END_MONTH; month++) {
            String monthLabel = getMonthNameLabel(month);
            
            // First Cutoff (Applied cutoff day constants)
            double workedHoursCutoff1 = getTotalHoursWorked(employeeNumber, month, FIRST_CUTOFF_START_DAY, FIRST_CUTOFF_END_DAY);
            double grossSalaryCutoff1 = workedHoursCutoff1 * hourlyRate;
            
            System.out.println("\n-----------------------------------");
            System.out.println("PAYROLL (" + monthLabel.toUpperCase() + "):");
            printPayrollResults(monthLabel, FIRST_CUTOFF_START_DAY + " - " + FIRST_CUTOFF_END_DAY, workedHoursCutoff1, grossSalaryCutoff1, grossSalaryCutoff1);
            
            // Second Cutoff 
            int endOfMonth = (month == 6 || month == 9 || month == 11) ? 30 : 31; 
            double workedHoursCutoff2 = getTotalHoursWorked(employeeNumber, month, SECOND_CUTOFF_START_DAY, endOfMonth);
            double grossSalaryCutoff2 = workedHoursCutoff2 * hourlyRate;
            double monthlyGrossTotal = grossSalaryCutoff1 + grossSalaryCutoff2;
            
            if (monthlyGrossTotal <= 0) {
                printPayrollResults(monthLabel, SECOND_CUTOFF_START_DAY + " to " + endOfMonth, workedHoursCutoff2, grossSalaryCutoff2, grossSalaryCutoff2);
                continue;
            }
            
            double sss = calculateSSS(monthlyGrossTotal);
            double philHealth = calculatePhilHealth(monthlyGrossTotal);
            double pagIbig = calculatePagIbig(monthlyGrossTotal);
            double withholdingTax = calculateWithholdingTax(monthlyGrossTotal, sss, philHealth, pagIbig);
            
            double totalDeductions = sss + philHealth + pagIbig + withholdingTax;
            double netSalaryCutoff2 = grossSalaryCutoff2 - totalDeductions;
            
            printPayrollResults(monthLabel, SECOND_CUTOFF_START_DAY + " to " + endOfMonth, workedHoursCutoff2, grossSalaryCutoff2, netSalaryCutoff2);
            
            System.out.println("EACH DEDUCTION:");
            System.out.println("- SSS: PHP " + moneyFormat.format(sss));
            System.out.println("- PhilHealth: PHP " + moneyFormat.format(philHealth));
            System.out.println("- Pag-IBIG: PHP " + moneyFormat.format(pagIbig));
            System.out.println("- Withholding Tax: PHP " + moneyFormat.format(withholdingTax));
            System.out.println("TOTAL DEDUCTIONS: PHP " + moneyFormat.format(totalDeductions));
            System.out.println("Net Salary: PHP " + moneyFormat.format(netSalaryCutoff2));
            System.out.println("|====================================|");
        }
    }

    static void loadEmployeeData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null ){
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); 
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }
                employeeList.add(data);
            }
        } catch (IOException e) {
            System.out.println("Error: Missing Employee File: " + e.getMessage()); 
        }
    }
    
    static void loadAttendanceData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null ){
                attendanceList.add(line.split(","));
            }
        } catch (IOException e) {
            System.out.println("Error: Missing Attendance File: " + e.getMessage()); 
        }
    }
    
    static int findEmployeeIndex(String employeeNumber) {
       for (int i = 0; i < employeeList.size(); i++) {
            if (employeeList.get(i)[CSV_EMP_ID].equals(employeeNumber)) return i;
        }
        return -1;
    }
    
    static String formatBirthdayDate(String date) { 
        String[] parts = date.split("/");
        return getMonthNameLabel(Integer.parseInt(parts[0])) + " " + parts [1] + ", " + parts[2];
    }
    
    static String getMonthNameLabel(int monthNumber) {
        String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return months[monthNumber];
    }
    
    static double getTotalHoursWorked(String employeeNumber, int targetMonth, int dayStart, int dayEnd) {
        double accumulatedHours = 0;
        for (String[] record : attendanceList) {
            // Applied CSV_ATT Constants
            if (!record[CSV_ATT_EMP_ID].equals(employeeNumber)) continue;
            
            String[] dateParts = record[CSV_ATT_DATE].split("/");
            int month = Integer.parseInt(dateParts[0]);
            int day = Integer.parseInt(dateParts[1]);
            
            if (month != targetMonth || day < dayStart || day > dayEnd) continue;
            
            accumulatedHours += computeDailyWorkHours(record[CSV_ATT_TIME_IN], record[CSV_ATT_TIME_OUT]);
        }
        return accumulatedHours;
    }

    static double computeDailyWorkHours(String timeInStr, String timeOutStr) {
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("H:mm");
            LocalTime timeIn = LocalTime.parse(timeInStr.trim(), format);
            LocalTime timeOut = LocalTime.parse(timeOutStr.trim(), format);

            // Applied Shift & Grace Period Constants
            LocalTime shiftStart = LocalTime.of(SHIFT_START_HOUR, 0);
            LocalTime graceLimit = LocalTime.of(SHIFT_START_HOUR, GRACE_PERIOD_LIMIT); 
            
            LocalTime effectiveIn = timeIn;
            if (timeIn.isBefore(shiftStart) || (!timeIn.isAfter(graceLimit))) {
                effectiveIn = shiftStart; 
            }

            LocalTime shiftEnd = LocalTime.of(SHIFT_END_HOUR, 0);
            LocalTime effectiveOut = timeOut.isAfter(shiftEnd) ? shiftEnd : timeOut;

            if (effectiveOut.isBefore(effectiveIn)) return 0.0;

            long minutes = Duration.between(effectiveIn, effectiveOut).toMinutes();
            
            if (minutes > LUNCH_THRESHOLD_MINS) {
                minutes -= LUNCH_DEDUCTION_MINS;
            }

            // Applied Minutes In Hour Constant
            return Math.max(0, minutes / (double) MINUTES_IN_HOUR); 
        } catch (Exception e) {
            return 0.0; 
        }
    }
   
    public static double calculateSSS(double salary) {
        double[][] sssTable = {
            {3250, 135.0}, {3750, 157.5}, {4250, 180.0}, {4750, 202.5}, {5250, 225.0}, {5750, 247.5},
            {6250, 270.0}, {6750, 292.5}, {7250, 315.0}, {7750, 337.5}, {8250, 360.0}, {8750, 382.5},
            {9250, 405.0}, {9750, 427.5}, {10250, 450.0}, {10750, 472.5}, {11250, 495.0}, {11750, 517.5},
            {12250, 540.0}, {12750, 562.5}, {13250, 585.0}, {13750, 607.5}, {14250, 630.0}, {14750, 652.5},
            {15250, 675.0}, {15750, 697.5}, {16250, 720.0}, {16750, 742.5}, {17250, 765.0}, {17750, 787.5},
            {18250, 810.0}, {18750, 832.5}, {19250, 855.0}, {19750, 877.5}, {20250, 900.0}, {20750, 922.5},
            {21250, 945.0}, {21750, 967.5}, {22250, 990.0}, {22750, 1012.5}, {23250, 1035.0}, {23750, 1057.5},
            {24250, 1080.0}, {24750, 1102.5}
        };

        for (double[] bracket : sssTable) {
            if (salary < bracket[0]) {
                return bracket[1];
            }
        }
        return MAXIMUM_CONTRIBUTION_CAP; 
    }
    
    public static double calculatePhilHealth(double salary) {
        double totalPremium;
        if (salary <= MIN_PHILHEALTH_CONTRIBUTION_SALARY_THRESHOLD) {
            totalPremium = MIN_PHILHEALTH_PREMIUM;
        } else if (salary < MAX_PHILHEALTH_SALARY_THRESHOLD) {
            totalPremium = salary * PHILHEALTH_PREMIUM_RATE;
        } else {
            totalPremium = MAX_PHILHEALTH_PREMIUM;
        }
        return totalPremium / 2; 
    }
    
    public static double calculatePagIbig(double salary) {
        double contribution;
        if (salary <= PAGIBIG_LOWER_SALARY_THRESHOLD) {
            contribution = salary * PAGIBIG_LOWER_RATE;
        } else {
            contribution = salary * PAGIBIG_UPPER_RATE;
        }
        return Math.min(contribution, MAX_PAGIBIG_CONTRIBUTION_CAP);
    }

    public static double calculateWithholdingTax(double monthlyGross, double sss, double philHealth, double pagIbig) {
        double taxableIncome = monthlyGross - (sss + philHealth + pagIbig);
        
        if (taxableIncome <= 20832) {
            return 0;
        } else if (taxableIncome < 33333) {
            return (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome < 66667) {
            return 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome < 166667) {
            return 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome < 666667) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            return 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    }

    static void printPayrollResults(String monthLabel, String dateRange, double totalHours, double grossSalary, double netSalary) {
        System.out.println("Period            : " + monthLabel + " " + dateRange);
        System.out.println("Total Hours Worked: " + hoursFormat.format(totalHours) + " hours");
        System.out.println("Gross Salary      : PHP " + moneyFormat.format(grossSalary));
        System.out.println("Net Salary        : PHP " + moneyFormat.format(netSalary));
        System.out.println("-----------------------------------");
    }
}
