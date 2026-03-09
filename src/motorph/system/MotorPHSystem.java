/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorph.system;

/**
 *
 * @author Group 30 - Gonzales, De Pano, Villamor
 */
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MotorPHSystem {

    /*
    Global arrays that store all employee information and attendance records loaded from CSV files.
    Each row represents one record while each column represents a specific data field.
    */
    static String [][] employeeData = new String[35][19]; // Stores employee details loaded from the employee CSV file.
    static String [][] attendanceData = new String[5169][6]; // Stores daily attendance records loaded from the attendance CSV file.

    // Counters to track the actual number of records loaded from files.
    static int totalEmployees = 0;
    static int totalAttendanceRecords = 0;
    
    
    // Main method that initializes the system by loading employee and attendance data, then handles the login process to determine whether the user is an employee or payroll staff member.
    public static void main(String[] args) {
        
        // Initialize system by loading data from external CSV files.
        loadEmployeeData("Data/MotorPH_Employee Data - Employee Details.csv");
        loadAttendanceData("Data/MotorPH_Employee Data - Attendance Record.csv");

        /*
        Prompts the user to enter login credentials and verifies whether the user is an employee or payroll staff member before directing them to the appropriate system menu.
        LOGIN PRINT OUTCOME
        */
        Scanner InputScanner = new Scanner(System.in);
        System.out.println("------ MOTORPH SYSTEM LOGIN ------");
        System.out.print("Username: ");
        String username = InputScanner.nextLine();
        System.out.print("Password: ");
        String password = InputScanner.nextLine();
        
        // Check user credentials and direct to the appropriate menu.
        if ((username.equals("employee") || username.equals("payroll_staff")) && password.equals("12345")){
            if (username.equals("employee")){
                handleEmployeeLogin(InputScanner);
            } else {
                handlePayrollStaffLogin(InputScanner);
            }
        } else {
            System.out.println("Error: Incorrect Username and/or Password"); // Error message will appear if there a different input in the username/password.
            System.exit(0);
        }
    }
    
    
    // Handles the employee login workflow by asking for an employee number, searching the employee data array, and displaying basic employee details if a valid record is found.
    static void handleEmployeeLogin(Scanner InputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        System.out.print("\nEnter Employee Number: ");
        String employeeNumber = InputScanner.nextLine();
        int employeeIndex = findEmployeeIndex(employeeNumber); // Search for the employee in the loaded data.
        
        // EMPLOYEE DETAILS PRINT OUTCOME
        if (employeeIndex != -1) {
            System.out.println("");
            System.out.println("------ EMPLOYEE DETAILS ------");
            System.out.println("Employee Number: " + employeeData[employeeIndex][0]);
            System.out.println("Employee Full Name: " + employeeData[employeeIndex][2] + " " + employeeData[employeeIndex][1]);
            System.out.println("Birthday: " + formatBirthdayDate(employeeData[employeeIndex][3]));
        } else {
            System.out.println("Error: Employee Number Does Not Exist"); // Error message will appear if an employee inputs an employee number not present in the system.
        }
        System.exit(0);
    }
    
    
    // Handles payroll staff operations by displaying menu options that allow the user to process payroll for a single employee or for all employees in the system.
     static void handlePayrollStaffLogin(Scanner InputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        System.out.println("\n1. Process Payroll \n2. Exit the Program");
        System.out.print("Select: ");
        String choice = InputScanner.nextLine();

        if (choice.equals("1")) {
            System.out.println("\n1. One Employee \n2. All Employees \n3. Exit the Program");
            System.out.print("Select: ");
            String subChoice = InputScanner.nextLine();
            
            // Process payroll for a SPECIFIC ID.
            if (subChoice.equals("1")) {
                System.out.print("Enter Employee Number: ");
                String employeeNumber = InputScanner.nextLine();
                int employeeIndex = findEmployeeIndex(employeeNumber);
                if (employeeIndex != -1) {
                    calculateAndDisplayPayroll(employeeIndex);
                } else {
                    System.out.println("Error: Employee Number Does Not Exist"); // Error message will appear if a payroll staff inputs an employee number not present in the system.
                }
            } else if (subChoice.equals("2")) { // Loop through EVERY EMPLOYEE IN THE SYSTEM to calculate salaries.
                for (int i = 0; i < totalEmployees; i++) {
                    calculateAndDisplayPayroll(i);
                } 
                System.out.println("\nPayroll Processing Status: Finished");
            } else if (subChoice.equals("3")) {
                System.out.println("Thank You!");
                System.out.println("------ EXITED PROGRAM ------");
                System.exit(0); 
            } else {
            System.out.println("Error: Invalid Choice. Please Select A Valid Option From The List...");} // Error message will appear if an option other than the ones present is submitted.
            
        } else if (choice.equals("2")) {
            System.out.println("Thank You!");
            System.out.println("------ EXITED PROGRAM ------");
            System.exit(0);
        } else {
            System.out.println("Error: Invalid Choice. Please Select A Valid Option From The List..."); // Error message will appear if an option other than the ones present is submitted.
        }
            System.exit(0);
    } 

    
    /*
    Calculates and displays payroll information for a specific employee. 
    This method computes total hours worked for each payroll cutoff, calculates gross salary, applies government deductions, and determines the final net salary.
    */
    static void calculateAndDisplayPayroll(int employeeIndex) {
        String employeeNumber = employeeData[employeeIndex][0];
        String firstName = employeeData[employeeIndex][2];
        String lastName = employeeData[employeeIndex][1];
        double hourlyRate = Double.parseDouble(employeeData[employeeIndex][18].replace(",", "")); // Remove commas from the HOURLY RATE string before converting to a number
        
        // EMPLOYEE DETAILS IN PAYROLL PROCESSING PRINT OUTCOME
        System.out.println("\n------------------------------");
        System.out.println("EMPLOYEE DETAILS: ");
        System.out.println("Employee Number: " + employeeNumber);
        System.out.println("Employee Name: " + firstName + " " + lastName);
        System.out.println("Birthday: " + formatBirthdayDate(employeeData[employeeIndex][3]));
        System.out.println("------------------------------");
        
        
        // Calculate payroll for the WORK MONTHS of the employees (June to December).
        for (int Month = 6; Month <= 12; Month++) {
            String monthLabel = getMonthNameLabel(Month);
            
            // 1st Cutoff: Days 1 - 15
            double workedHoursCutoff1 = getTotalHoursWorked(employeeNumber, Month, 1, 15);
            double grossSalaryCutoff1 = workedHoursCutoff1 * hourlyRate;
            
            // 1ST CUTOFF PRINT OUTCOME
            System.out.println("\nPAYROLL (" + monthLabel.toUpperCase() + "):");
            System.out.println("1st Cutoff Date: " + monthLabel + " " + "15");
            System.out.println("Total Hours Worked: " + workedHoursCutoff1);
            System.out.println("Gross Salary: PHP " + String.format("%.2f", grossSalaryCutoff1));
            System.out.println("Net Salary: PHP " + String.format("%.2f", grossSalaryCutoff1));
            
            // 2nd Cutoff: Days 16 - 31
            double workedHoursCutoff2 = getTotalHoursWorked(employeeNumber, Month, 16, 31);
            double grossSalaryCutoff2 = workedHoursCutoff2 * hourlyRate;
            double monthlyGrossTotal = grossSalaryCutoff1 + grossSalaryCutoff2; // Combine BOTH CUTOFFS to determine MONTHLY GOVERNMENT CONTRIBUTIONS.
            
            // GOVERNMENT DEDUCTIONS TO BE DEDUCTED FROM THE 2ND CUTOFF PAY.
            double sssContribution = calculateSSS(monthlyGrossTotal);
            double philHealthContribution = calculatePhilHealth(monthlyGrossTotal);
            double pagIbigContribution = calculatePagIbig(monthlyGrossTotal);
            
            // If the employee did not work, this ensures deductions are automatically 0.
            if (monthlyGrossTotal == 0) {
            sssContribution = 0;
            philHealthContribution = 0;
            pagIbigContribution = 0;}
            
            // Calculate TAX based on the income remaining after mandatory government contributions.
            double withholdingTax = calculateWithholdingTax (monthlyGrossTotal, sssContribution, philHealthContribution, pagIbigContribution);
            double totalDeductions = sssContribution + philHealthContribution + pagIbigContribution + withholdingTax;
            double netSalaryCutoff2 = grossSalaryCutoff2 - totalDeductions; // FINAL NET SALARY for the 2nd Cutoff.
            
            // 2ND CUTOFF PRINT OUTCOME
            System.out.println("\n2nd Cutoff Date: " + monthLabel + " " + "30");
            System.out.println("Total Hours Worked: " + workedHoursCutoff2);
            System.out.println("Gross Salary: PHP " + String.format("%.2f", grossSalaryCutoff2));
            System.out.println("EACH DEDUCTION:");
            System.out.println("- SSS: PHP " + String.format("%.2f", sssContribution));
            System.out.println("- PhilHealth: PHP " + String.format("%.2f", philHealthContribution));
            System.out.println("- Pag-IBIG: PHP " + String.format("%.2f", pagIbigContribution));
            System.out.println("- Withholding Tax: PHP " + String.format("%.2f", withholdingTax));
            System.out.println("TOTAL DEDUCTIONS: PHP " + String.format("%.2f", totalDeductions));
            System.out.println("Net Salary: PHP " + String.format("%.2f", netSalaryCutoff2));
            System.out.println("------------------------------");
        }
    }
    

    /*
    Reads the employee details CSV file line by line and stores each record into the employeeData array. 
    The first row is skipped because it contains column headers.
    */
    static void loadEmployeeData(String FileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(FileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null && totalEmployees < employeeData.length) {
                employeeData[totalEmployees++] = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex splits by comma but ignores commas inside the double quotes.
            }
        } catch (IOException e) {
            System.out.println("Error: Missing Employee File: " + e.getMessage()); // Error message will appear if the CSV file is not found by the program.
        }
    }
    
    
    // Reads the attendance CSV file and stores each attendance record into the attendanceData array for later payroll calculations.
    static void loadAttendanceData(String FileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(FileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null && totalAttendanceRecords < attendanceData.length) {
                attendanceData[totalAttendanceRecords++] = line.split(",");
            }
        } catch (IOException e) {
            System.out.println("Error: Missing Attendance File: " + e.getMessage()); // Error message will appear if the CSV file is not found by the program.
        }
    }
    
    
    /*
     Searches the employeeData array for a specific employee number and returns the index of the matching record. 
    Returns -1 if the employee does not exist in the system.
    */
    static int findEmployeeIndex(String employeeNumber) {
        for (int i = 0; i < totalEmployees; i++) {
            if (employeeData[i][0].equals(employeeNumber)) return i;
        }
        return -1;
    }
    
    
    // Converts a date formatted as "MM/DD/YYYY" into a more readable format such as "June 12, 2000".
    static String formatBirthdayDate(String date) { 
        String[] Parts = date.split("/");
        return getMonthNameLabel(Integer.parseInt(Parts[0])) + " " + Parts [1] + ", " + Parts[2];
    }
    
    
    // Returns the name of the month for a given number.
    static String getMonthNameLabel(int monthNumber) {
        String[] Months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return Months[monthNumber];
    }
    
    
    // Calculates the total number of hours worked by a specific employee within a given month and day range by scanning the attendance records and summing the daily work hours.
    static double getTotalHoursWorked(String employeeNumber, int targetMonth, int dayStart, int dayEnd) {
        double accumulatedHours = 0;
        for (int i = 0; i < totalAttendanceRecords; i++) {
            if (attendanceData[i][0].equals(employeeNumber)) {
                String[] dateParts = attendanceData[i][3].split("/");
                int Month = Integer.parseInt(dateParts[0]);
                int Day = Integer.parseInt(dateParts[1]);
                if (Month == targetMonth && Day >= dayStart && Day <= dayEnd) { // Checks if the record falls within the specified month and date range.
                    accumulatedHours += computeDailyWorkHours(attendanceData[i][4], attendanceData[i][5]);
                }
            }
        }
        return accumulatedHours;
    }

    
    // Computes the number of hours worked in a day while applying company attendance policies such as the 10-minute grace period, the 5:00 PM work cutoff, and the 1-hour lunch break deduction.
    static double computeDailyWorkHours(String timeIn, String timeOut) {
        String[] InParts = timeIn.split(":");
        int hourIn = Integer.parseInt(InParts[0]);
        int minuteIn = Integer.parseInt(InParts[1]);
        
        // 10 Minute Grace Period Policy (8:00 AM to 8:10 AM = On Time while 8:11 onwards is considered LATE).
        int effectiveMinuteIn;
        if (hourIn < 8) {
            effectiveMinuteIn = 0; 
        } else if (hourIn == 8 && minuteIn <= 10) {
            effectiveMinuteIn = 0;
        } else {
            effectiveMinuteIn = (hourIn - 8) * 60 + minuteIn;
        }

        String[] outParts = timeOut.split(":");
        int hourOut = Integer.parseInt(outParts[0]);
        int minuteOut = Integer.parseInt(outParts[1]);
        
        int effectiveHourOut = hourOut;
        int effectiveMinuteOut = minuteOut;
        
        // Work hours end STRICTLY AT 5:00 PM (17:00) (NO OVERTIME COUNTED).
        if (hourOut >= 17) {
            effectiveHourOut = 17;
            effectiveMinuteOut = 0;
        }

        int startTotalMinutes = (8 * 60) + effectiveMinuteIn;
        int endTotalMinutes = (effectiveHourOut * 60) + effectiveMinuteOut;
        
        double totalMinutes = endTotalMinutes - startTotalMinutes - 60; // Total minutes minus the 60 minutes or 1 hour for the lunch break.
        
        return Math.max(0, totalMinutes / 60.0);
    }
   
    
    // Determines the employee's SSS contribution based on their salary using the official contribution table ranges.
    public static double calculateSSS(double Salary) {
        if (Salary < 3250) return 135.00;
        if (Salary < 3750) return 157.50;
        if (Salary < 4250) return 180.00;
        if (Salary < 4750) return 202.50;
        if (Salary < 5250) return 225.00;
        if (Salary < 5750) return 247.50;
        if (Salary < 6250) return 270.00;
        if (Salary < 6750) return 292.50;
        if (Salary < 7250) return 315.00;
        if (Salary < 7750) return 337.50;
        if (Salary < 8250) return 360.00;
        if (Salary < 8750) return 382.50;
        if (Salary < 9250) return 405.00;
        if (Salary < 9750) return 427.50;
        if (Salary < 10250) return 450.00;
        if (Salary < 10750) return 472.50;
        if (Salary < 11250) return 495.00;
        if (Salary < 11750) return 517.50;
        if (Salary < 12250) return 540.00;
        if (Salary < 12750) return 562.50;
        if (Salary < 13250) return 585.00;
        if (Salary < 13750) return 607.50;
        if (Salary < 14250) return 630.00;
        if (Salary < 14750) return 652.50;
        if (Salary < 15250) return 675.00;
        if (Salary < 15750) return 697.50;
        if (Salary < 16250) return 720.00;
        if (Salary < 16750) return 742.50;
        if (Salary < 17250) return 765.00;
        if (Salary < 17750) return 787.50;
        if (Salary < 18250) return 810.00;
        if (Salary < 18750) return 832.50;
        if (Salary < 19250) return 855.00;
        if (Salary < 19750) return 877.50;
        if (Salary < 20250) return 900.00;
        if (Salary < 20750) return 922.50;
        if (Salary < 21250) return 945.00;
        if (Salary < 21750) return 967.50;
        if (Salary < 22250) return 990.00;
        if (Salary < 22750) return 1012.50;
        if (Salary < 23250) return 1035.00;
        if (Salary < 23750) return 1057.50;
        if (Salary < 24250) return 1080.00;
        if (Salary < 24750) return 1102.50;
        return 1125.00;
    }
    
    
    // Calculates the employee's PhilHealth contribution based on the 3% premium rate where the total premium is equally shared between the employer and the employee.
    public static double calculatePhilHealth(double Salary) {
        double TotalPremium;
        if (Salary <= 10000) {
            TotalPremium = 300;
        } else if (Salary < 60000) {
            TotalPremium = Salary * 0.03;
        } else {
            TotalPremium = 1800;
        }
        return TotalPremium / 2;
    }
    
    
    // Calculates the Pag-IBIG contribution based on the salary rate, applying the 1% or 2% rule and limiting the maximum contribution to PHP 100.
    public static double calculatePagIbig(double Salary) {
        double Contribution;
        if (Salary <= 1500) {
            Contribution = Salary * 0.01;
        } else {
            Contribution = Salary * 0.02;
        }
        return Math.min(Contribution, 100);
    }

    
    // Computes the withholding tax based on Philippine tax brackets after deducting mandatory government contributions from the salary.
    public static double calculateWithholdingTax(double monthlyGross, double sss, double ph, double pi) {
        double taxableIncome = monthlyGross - (sss + ph + pi);
        
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
    
}
        

