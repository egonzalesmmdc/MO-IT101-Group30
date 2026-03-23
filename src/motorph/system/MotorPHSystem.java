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
import java.text.DecimalFormat; // Added this to handle commas without rounding

public class MotorPHSystem {
    
    static final int LUNCH_THRESHOLD_MINS = 240; 
    static final int LUNCH_DEDUCTION_MINS = 60;
    static final int SHIFT_END_HOUR = 17;
    static final int GRACE_PERIOD_LIMIT = 10;

    static List<String[]> employeeList = new ArrayList<>();
    static List<String[]> attendanceList = new ArrayList<>();
    
    // Creates a formatter that adds commas and shows unrounded decimals
    static final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00##############");
    static final DecimalFormat hoursFormat = new DecimalFormat("#,##0.################");
    
    /**
     * Main entry point of the system.
     * Initializes data from CSV files and handles the main login portal.
     * @param args
     */
    public static void main(String[] args) {
        loadEmployeeData("Data/MotorPH_Employee Data - Employee Details.csv");
        loadAttendanceData("Data/MotorPH_Employee Data - Attendance Record.csv");

        Scanner inputScanner = new Scanner(System.in);
        System.out.println("------| MOTORPH SYSTEM LOGIN |------");
        System.out.print("Username: ");
        String username = inputScanner.nextLine();
        System.out.print("Password: ");
        String password = inputScanner.nextLine();
        
        // Authenticate user role
        if ((username.equals("employee") || username.equals("payroll_staff")) && password.equals("12345")){
            if (username.equals("employee")){
                handleEmployeeLogin(inputScanner);
            } else {
                handlePayrollStaffLogin(inputScanner);
            }
        } else {
            System.out.println("Error: Incorrect Username and/or Password");
            System.exit(0);
        }
    }
    
    /**
     * Helper method to safely prompt for and validate integer input.
     * Prevents system crashes from non-numeric user entries.
     */
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
    
    /**
     * Handles the workflow for standard employees.
     * Searches for the inputted employee ID and displays their basic details.
     */
    static void handleEmployeeLogin(Scanner inputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        System.out.print("\nEnter Employee Number: ");
        String employeeNumber = inputScanner.nextLine();
        int employeeIndex = findEmployeeIndex(employeeNumber); 
        
        if (employeeIndex != -1) {
            String[] employeeDetails = employeeList.get(employeeIndex);
            System.out.println("");
            System.out.println("------| EMPLOYEE DETAILS |------");
            System.out.println("Employee Number: " + employeeDetails[0]);
            System.out.println("Employee Full Name: " + employeeDetails[2] + " " + employeeDetails[1]);
            System.out.println("Birthday: " + formatBirthdayDate(employeeDetails[3]));
        } else {
            System.out.println("Error: Employee Number Does Not Exist"); 
        }
        System.exit(0);
    }
    
    /**
     * Handles the interactive menu for payroll staff operations.
     * Allows processing of single or batch payrolls.
     */
    static void handlePayrollStaffLogin(Scanner inputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        System.out.println("\n1. Process Payroll \n2. Exit the Program");
        
        int choice = getSafeInt(inputScanner, "Select: ", 1, 2);

        switch (choice) {
            case 1 -> {
                System.out.println("\n1. One Employee \n2. All Employees \n3. Exit the Program");
                int subChoice = getSafeInt(inputScanner, "Select: ", 1, 3);
                switch (subChoice) {
                    case 1 -> {
                        System.out.print("Enter Employee Number: ");
                        String employeeNumber = inputScanner.nextLine();
                        int employeeIndex = findEmployeeIndex(employeeNumber);
                        if (employeeIndex != -1) {
                            calculateAndDisplayPayroll(employeeIndex);
                        } else {
                            System.out.println("Error: Employee Number Does Not Exist"); 
                        }
                }
                    case 2 -> {
                        // Batch processing for all employees
                        for (int i = 0; i < employeeList.size(); i++) {
                            calculateAndDisplayPayroll(i);
                        } 
                        System.out.println("\nPayroll Processing Status: Finished");
                }
                    case 3 -> {
                        System.out.println("Thank You!");
                        System.out.println("------| EXITED PROGRAM |------");
                        System.exit(0);
                }
                }
                // Ends the 'case 1' for the main menu
            }
            case 2 -> {
                System.out.println("Thank You!");
                System.out.println("------| EXITED PROGRAM |------");
                System.exit(0);
            }
        }
        System.exit(0);
    }

    /**
     * Calculates and displays the complete payroll breakdown for a specific employee.
     * Processes both standard 15-day cutoffs and applies statutory deductions.
     */
    static void calculateAndDisplayPayroll(int employeeIndex) {
        String[] currentEmployee = employeeList.get(employeeIndex);
        String employeeNumber = currentEmployee[0];
        String firstName = currentEmployee[2];
        String lastName = currentEmployee[1];
        
        double hourlyRate = Double.parseDouble(currentEmployee[18].replace(",", "")); 
        
        System.out.println("\n======| EMPLOYEE DETAILS |======");
        System.out.println("Employee Number: " + employeeNumber);
        System.out.println("Employee Name: " + firstName + " " + lastName);
        System.out.println("Birthday: " + formatBirthdayDate(currentEmployee[3]));
        System.out.println("================================");
        System.out.println("\n======| Proccessed Payroll |======");
        
        // Loop through valid months in the attendance record
        for (int month = 6; month <= 12; month++) {
            String monthLabel = getMonthNameLabel(month);
            
            // First Cutoff Calculation (Days 1 - 15)
            double workedHoursCutoff1 = getTotalHoursWorked(employeeNumber, month, 1, 15);
            double grossSalaryCutoff1 = workedHoursCutoff1 * hourlyRate;
            
            System.out.println("\n-----------------------------------");
            System.out.println("PAYROLL (" + monthLabel.toUpperCase() + "):");
            printPayrollResults(monthLabel, "1 - 15", workedHoursCutoff1, grossSalaryCutoff1, grossSalaryCutoff1);
            
            // Second Cutoff Calculation (Days 16 - End of Month)
            int endOfMonth = (month == 6 || month == 9 || month == 11) ? 30 : 31; 
            double workedHoursCutoff2 = getTotalHoursWorked(employeeNumber, month, 16, endOfMonth);
            double grossSalaryCutoff2 = workedHoursCutoff2 * hourlyRate;
            double monthlyGrossTotal = grossSalaryCutoff1 + grossSalaryCutoff2;
            
            // Statutory Deductions (Applied on total monthly gross)
            double SSS = 0, philHealth = 0, pagIbig = 0, withholdingTax = 0;
            if (monthlyGrossTotal > 0) {
                SSS = calculateSSS(monthlyGrossTotal);
                philHealth = calculatePhilHealth(monthlyGrossTotal);
                pagIbig = calculatePagIbig(monthlyGrossTotal);
                withholdingTax = calculateWithholdingTax(monthlyGrossTotal, SSS, philHealth, pagIbig);
            }
            
            double totalDeductions = SSS + philHealth + pagIbig + withholdingTax;
            double netSalaryCutoff2 = grossSalaryCutoff2 - totalDeductions;
            
            printPayrollResults(monthLabel, "16 - 30/31", workedHoursCutoff2, grossSalaryCutoff2, netSalaryCutoff2);
            
            // Display deduction breakdown (Using DecimalFormat for commas without rounding)
            if (monthlyGrossTotal > 0) {
                System.out.println("EACH DEDUCTION:");
                System.out.println("- SSS: PHP " + moneyFormat.format(SSS));
                System.out.println("- PhilHealth: PHP " + moneyFormat.format(philHealth));
                System.out.println("- Pag-IBIG: PHP " + moneyFormat.format(pagIbig));
                System.out.println("- Withholding Tax: PHP " + moneyFormat.format(withholdingTax));
                System.out.println("TOTAL DEDUCTIONS: PHP " + moneyFormat.format(totalDeductions));
                System.out.println("Net Salary: PHP " + moneyFormat.format(netSalaryCutoff2));
                System.out.println("|====================================|");
            }
        }
    }

    /**
     * Parses the employee detail CSV and populates the employeeList.
     * Safely handles embedded commas within quotes using regex.
     */
    static void loadEmployeeData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine(); // Skip header row
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
    
    /**
     * Parses the attendance record CSV and populates the attendanceList.
     */
    static void loadAttendanceData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine(); // Skip header row
            String line;
            while ((line = br.readLine()) != null ){
                attendanceList.add(line.split(","));
            }
        } catch (IOException e) {
            System.out.println("Error: Missing Attendance File: " + e.getMessage()); 
        }
    }
    
    /**
     * Locates an employee's index in the data list using their ID number.
     * Returns -1 if no matching record is found.
     */
    static int findEmployeeIndex(String employeeNumber) {
       for (int i = 0; i < employeeList.size(); i++) {
            if (employeeList.get(i)[0].equals(employeeNumber)) return i;
        }
        return -1;
    }
    
    /** Formats MM/DD/YYYY to a readable string (e.g., "June 12, 2000"). */
    static String formatBirthdayDate(String date) { 
        String[] parts = date.split("/");
        return getMonthNameLabel(Integer.parseInt(parts[0])) + " " + parts [1] + ", " + parts[2];
    }
    
    /** Retrieves the string representation of a month number. */
    static String getMonthNameLabel(int monthNumber) {
        String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return months[monthNumber];
    }
    
    /**
     * Scans attendance records to calculate total hours worked for a specific employee
     * during a defined date range within a specific month.
     */
    static double getTotalHoursWorked(String employeeNumber, int targetMonth, int dayStart, int dayEnd) {
        double accumulatedHours = 0;
        for (int i = 0; i < attendanceList.size(); i++) {
            String[] record = attendanceList.get(i);
            
            if (record[0].equals(employeeNumber)) {
                String[] dateParts = record[3].split("/");
                int month = Integer.parseInt(dateParts[0]);
                int day = Integer.parseInt(dateParts[1]);
                if (month == targetMonth && day >= dayStart && day <= dayEnd) { 
                    accumulatedHours += computeDailyWorkHours(record[4], record[5]);
                }
            }
        }
        return accumulatedHours;
    }

    /**
     * Calculates hours worked in a single shift using the java.time API.
     * Enforces company policies: 10-minute grace period, 5 PM strict cutoff, and lunch deductions.
     */
    static double computeDailyWorkHours(String timeInStr, String timeOutStr) {
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("H:mm");
            LocalTime timeIn = LocalTime.parse(timeInStr.trim(), format);
            LocalTime timeOut = LocalTime.parse(timeOutStr.trim(), format);

            LocalTime shiftStart = LocalTime.of(8, 0);
            
            LocalTime graceLimit = LocalTime.of(8, GRACE_PERIOD_LIMIT); 
            
            LocalTime effectiveIn = timeIn;
            if (timeIn.isBefore(shiftStart)) {
                effectiveIn = shiftStart; 
            } else if (timeIn.isAfter(shiftStart) && !timeIn.isAfter(graceLimit)) {
                effectiveIn = shiftStart; 
            }

            LocalTime shiftEnd = LocalTime.of(SHIFT_END_HOUR, 0);
            LocalTime effectiveOut = timeOut.isAfter(shiftEnd) ? shiftEnd : timeOut;

            if (effectiveOut.isBefore(effectiveIn)) return 0.0;

            long minutes = Duration.between(effectiveIn, effectiveOut).toMinutes();
            
            if (minutes > LUNCH_THRESHOLD_MINS) {
                minutes -= LUNCH_DEDUCTION_MINS;
            }

            return Math.max(0, minutes / 60.0); // Raw unrounded division
        } catch (Exception e) {
            return 0.0; // Gracefully handles empty or malformed time entries
        }
    }
   
    /**
     * Computes the SSS contribution based on the official salary bracket table.
     * @param salary
     * @return 
     */
    public static double calculateSSS(double salary) {
        double[][] sssTable = {
            {3250, 135.0}, {3750, 157.5}, {4250, 180.0}, {4750, 202.5},
            {5250, 225.0}, {5750, 247.5}, {6250, 270.0}, {6750, 292.5},
            {7250, 315.0}, {7750, 337.5}, {8250, 360.0}, {8750, 382.5},
            {9250, 405.0}, {9750, 427.5}, {10250, 450.0} 
        };

        for (double[] sssTable1 : sssTable) {
            if (salary < sssTable1[0]) {
                return sssTable1[1];
            }
        }
        return 1125.0; // Maximum contribution cap
    }
    
    /**
     * Computes PhilHealth contribution (3% premium rate equally shared).
     * @param salary
     * @return 
     */
    public static double calculatePhilHealth(double salary) {
        double totalPremium;
        if (salary <= 10000) {
            totalPremium = 300;
        } else if (salary < 60000) {
            totalPremium = salary * 0.03;
        } else {
            totalPremium = 1800;
        }
        return totalPremium / 2; // Employee's 50% share
    }
    
    /**
     * Computes Pag-IBIG contribution based on 1% or 2% brackets (Max PHP 100).
     * @param salary
     * @return 
     */
    public static double calculatePagIbig(double salary) {
        double contribution;
        if (salary <= 1500) {
            contribution = salary * 0.01;
        } else {
            contribution = salary * 0.02;
        }
        return Math.min(contribution, 100);
    }

    /**
     * Computes withholding tax based on Philippine graduated tax brackets.
     * Executed strictly after statutory deductions are removed from gross income.
     * @param monthlyGross
     * @param SSS
     * @param philHealth
     * @param pagIbig
     * @return 
     */
    public static double calculateWithholdingTax(double monthlyGross, double SSS, double philHealth, double pagIbig) {
        double taxableIncome = monthlyGross - (SSS + philHealth + pagIbig);
        
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

    /**
     * Helper method to output standardized, aligned payroll records.
     * Using DecimalFormat to keep commas, but output exact, unrounded values.
     */
    static void printPayrollResults(String month, String range, double hours, double gross, double net) {
        System.out.println("Period            : " + month + " " + range);
        System.out.println("Total Hours Worked: " + hoursFormat.format(hours) + " hours");
        System.out.println("Gross Salary      : PHP " + moneyFormat.format(gross));
        System.out.println("Net Salary        : PHP " + moneyFormat.format(net));
        System.out.println("-----------------------------------");
    }
}
