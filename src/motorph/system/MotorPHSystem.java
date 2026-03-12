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
import java.util.ArrayList;
import java.util.List;

public class MotorPHSystem {

    static List<String[]> employeeList = new ArrayList<>();
    static List<String[]> attendanceList = new ArrayList<>();
    
    // Main method that initializes the system by loading employee and attendance data, then handles the login process to determine whether the user is an employee or payroll staff member.
    public static void main(String[] args) {
        
        // Initialize system by loading data from external CSV files.
        loadEmployeeData("Data/MotorPH_Employee Data - Employee Details.csv");
        loadAttendanceData("Data/MotorPH_Employee Data - Attendance Record.csv");

        /*
        Prompts the user to enter login credentials and verifies whether the user is an employee or payroll staff member before directing them to the appropriate system menu.
        LOGIN PRINT OUTCOME
        */
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("------| MOTORPH SYSTEM LOGIN |------");
        System.out.print("Username: ");
        String username = inputScanner.nextLine();
        System.out.print("Password: ");
        String password = inputScanner.nextLine();
        
        // Check user credentials and direct to the appropriate menu.
        if ((username.equals("employee") || username.equals("payroll_staff")) && password.equals("12345")){
            if (username.equals("employee")){
                handleEmployeeLogin(inputScanner);
            } else {
                handlePayrollStaffLogin(inputScanner);
            }
        } else {
            System.out.println("Error: Incorrect Username and/or Password"); // Error message will appear if there a different input in the username/password.
            System.exit(0);
        }
    }
    
    
    // Handles the employee login workflow by asking for an employee number, searching the employee data array, and displaying basic employee details if a valid record is found.
    static void handleEmployeeLogin(Scanner inputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        System.out.print("\nEnter Employee Number: ");
        String employeeNumber = inputScanner.nextLine();
        int employeeIndex = findEmployeeIndex(employeeNumber); // Search for the employee in the loaded data.
        
        // EMPLOYEE DETAILS PRINT OUTCOME
        if (employeeIndex != -1) {
            String[] employeeDetails = employeeList.get(employeeIndex);
            System.out.println("");
            System.out.println("------| EMPLOYEE DETAILS |------");
            System.out.println("Employee Number: " + employeeDetails[0]);
            System.out.println("Employee Full Name: " + employeeDetails[2] + " " + employeeDetails[1]);
            System.out.println("Birthday: " + formatBirthdayDate(employeeDetails[3]));
        } else {
            System.out.println("Error: Employee Number Does Not Exist"); // Error message will appear if an employee inputs an employee number not present in the system.
        }
        System.exit(0);
    }
    
    
    // Handles payroll staff operations by displaying menu options that allow the user to process payroll for a single employee or for all employees in the system.
     static void handlePayrollStaffLogin(Scanner inputScanner) {
        System.out.println("");
        System.out.print("Welcome!");
        System.out.println("\n1. Process Payroll \n2. Exit the Program");
        System.out.print("Select: ");
        String choice = inputScanner.nextLine();

        if (choice.equals("1")) {
            System.out.println("\n1. One Employee \n2. All Employees \n3. Exit the Program");
            System.out.print("Select: ");
            String subChoice = inputScanner.nextLine();
            
            // Process payroll for a SPECIFIC ID.
            if (subChoice.equals("1")) {
                System.out.print("Enter Employee Number: ");
                String employeeNumber = inputScanner.nextLine();
                int employeeIndex = findEmployeeIndex(employeeNumber);
                if (employeeIndex != -1) {
                    calculateAndDisplayPayroll(employeeIndex);
                } else {
                    System.out.println("Error: Employee Number Does Not Exist"); // Error message will appear if a payroll staff inputs an employee number not present in the system.
                }
            } else if (subChoice.equals("2")) { // Loop through EVERY EMPLOYEE IN THE SYSTEM to calculate salaries.
                for (int i = 0; i < employeeList.size(); i++) {
                    calculateAndDisplayPayroll(i);
                } 
                System.out.println("\nPayroll Processing Status: Finished");
            } else if (subChoice.equals("3")) {
                System.out.println("Thank You!");
                System.out.println("------| EXITED PROGRAM |------");
                System.exit(0); 
            } else {
            System.out.println("Error: Invalid Choice. Please Select A Valid Option From The List...");} // Error message will appear if an option other than the ones present is submitted.
            
        } else if (choice.equals("2")) {
            System.out.println("Thank You!");
            System.out.println("------| EXITED PROGRAM |------");
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
        String[] currentEmployee = employeeList.get(employeeIndex);
        String employeeNumber = currentEmployee[0];
        String firstName = currentEmployee[2];
        String lastName = currentEmployee[1];
        double hourlyRate = Double.parseDouble(currentEmployee[18].replace(",", "")); // Remove commas from the HOURLY RATE string before converting to a number
        
        // EMPLOYEE DETAILS IN PAYROLL PROCESSING PRINT OUTCOME
        System.out.println("\n------------------------------");
        System.out.println("EMPLOYEE DETAILS: ");
        System.out.println("Employee Number: " + employeeNumber);
        System.out.println("Employee Name: " + firstName + " " + lastName);
        System.out.println("Birthday: " + formatBirthdayDate(currentEmployee[3]));
        System.out.println("------------------------------");
        
        
        // Calculate payroll for the WORK MONTHS of the employees (June to December).
        for (int month = 6; month <= 12; month++) {
            String monthLabel = getMonthNameLabel(month);
            
            // 1st Cutoff: Days 1 - 15
            double workedHoursCutoff1 = getTotalHoursWorked(employeeNumber, month, 1, 15);
            double grossSalaryCutoff1 = workedHoursCutoff1 * hourlyRate;
            
            // 1ST CUTOFF PRINT OUTCOME
            System.out.println("\nPAYROLL (" + monthLabel.toUpperCase() + "):");
            System.out.println("1st Cutoff Date: " + monthLabel + " " + "15");
            System.out.println("Total Hours Worked: " + workedHoursCutoff1);
            System.out.println("Gross Salary: PHP " + grossSalaryCutoff1);
            System.out.println("Net Salary: PHP " + grossSalaryCutoff1);
            
            // 2nd Cutoff: Days 16 - 31
            double workedHoursCutoff2 = getTotalHoursWorked(employeeNumber, month, 16, 31);
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
            System.out.println("Gross Salary: PHP " + grossSalaryCutoff2);
            System.out.println("EACH DEDUCTION:");
            System.out.println("- SSS: PHP " + sssContribution);
            System.out.println("- PhilHealth: PHP " + philHealthContribution);
            System.out.println("- Pag-IBIG: PHP " + pagIbigContribution);
            System.out.println("- Withholding Tax: PHP " + withholdingTax);
            System.out.println("TOTAL DEDUCTIONS: PHP " + totalDeductions);
            System.out.println("Net Salary: PHP " + netSalaryCutoff2);
            System.out.println("------------------------------");
        }
    }
    

    /*
    Reads the employee details CSV file line by line and stores each record into the employeeData array. 
    The first row is skipped because it contains column headers.
    */
    static void loadEmployeeData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null ){
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex splits by comma but ignores commas inside the double quotes.
            for (int i = 0; i < data.length; i++) {
                data[i] = data[i].replace("\"", "").trim();
            }
                employeeList.add(data);//Add the array directly to our list
            }
        } catch (IOException e) {
            System.out.println("Error: Missing Employee File: " + e.getMessage()); // Error message will appear if the CSV file is not found by the program.
        }
    }
    
    
    // Reads the attendance CSV file and stores each attendance record into the attendanceData array for later payroll calculations.
    static void loadAttendanceData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null ){
                attendanceList.add(line.split(","));
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
       for (int i = 0; i < employeeList.size(); i++) {
            if (employeeList.get(i)[0].equals(employeeNumber)) return i;
        }
        return -1;
    }
    
    
    // Converts a date formatted as "MM/DD/YYYY" into a more readable format such as "June 12, 2000".
    static String formatBirthdayDate(String date) { 
        String[] parts = date.split("/");
        return getMonthNameLabel(Integer.parseInt(parts[0])) + " " + parts [1] + ", " + parts[2];
    }
    
    
    // Returns the name of the month for a given number.
    static String getMonthNameLabel(int monthNumber) {
        String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return months[monthNumber];
    }
    
    
    // Calculates the total number of hours worked by a specific employee within a given month and day range by scanning the attendance records and summing the daily work hours.
    static double getTotalHoursWorked(String employeeNumber, int targetMonth, int dayStart, int dayEnd) {
        double accumulatedHours = 0;
        for (int i = 0; i < attendanceList.size(); i++) {
            String[] record = attendanceList.get(i);
            
            if (record[0].equals(employeeNumber)) {
                String[] dateParts = record[3].split("/");
                int month = Integer.parseInt(dateParts[0]);
                int day = Integer.parseInt(dateParts[1]);
                if (month == targetMonth && day >= dayStart && day <= dayEnd) { // Checks if the record falls within the specified month and date range.
                    accumulatedHours += computeDailyWorkHours(record[4], record[5]);
                }
            }
        }
        return accumulatedHours;
    }

    
    // Computes the number of hours worked in a day while applying company attendance policies such as the 10-minute grace period, the 5:00 PM work cutoff, and the 1-hour lunch break deduction.
    static double computeDailyWorkHours(String timeIn, String timeOut) {
        String[] inParts = timeIn.split(":");
        int hourIn = Integer.parseInt(inParts[0]);
        int minuteIn = Integer.parseInt(inParts[1]);
        
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
        
        double totalMinutes = endTotalMinutes - startTotalMinutes; 
        if (totalMinutes > 240){
        totalMinutes -=60; 
        }
        return Math.max(0, totalMinutes / 60.0);
    }
   
    
    // Determines the employee's SSS contribution based on their salary using the official contribution table ranges.
   public static double calculateSSS(double salary) {
        if (salary < 3250) return 135.00;
        if (salary < 3750) return 157.50;
        if (salary < 4250) return 180.00;
        if (salary < 4750) return 202.50;
        if (salary < 5250) return 225.00;
        if (salary < 5750) return 247.50;
        if (salary < 6250) return 270.00;
        if (salary < 6750) return 292.50;
        if (salary < 7250) return 315.00;
        if (salary < 7750) return 337.50;
        if (salary < 8250) return 360.00;
        if (salary < 8750) return 382.50;
        if (salary < 9250) return 405.00;
        if (salary < 9750) return 427.50;
        if (salary < 10250) return 450.00;
        if (salary < 10750) return 472.50;
        if (salary < 11250) return 495.00;
        if (salary < 11750) return 517.50;
        if (salary < 12250) return 540.00;
        if (salary < 12750) return 562.50;
        if (salary < 13250) return 585.00;
        if (salary < 13750) return 607.50;
        if (salary < 14250) return 630.00;
        if (salary < 14750) return 652.50;
        if (salary < 15250) return 675.00;
        if (salary < 15750) return 697.50;
        if (salary < 16250) return 720.00;
        if (salary < 16750) return 742.50;
        if (salary < 17250) return 765.00;
        if (salary < 17750) return 787.50;
        if (salary < 18250) return 810.00;
        if (salary < 18750) return 832.50;
        if (salary < 19250) return 855.00;
        if (salary < 19750) return 877.50;
        if (salary < 20250) return 900.00;
        if (salary < 20750) return 922.50;
        if (salary < 21250) return 945.00;
        if (salary < 21750) return 967.50;
        if (salary < 22250) return 990.00;
        if (salary < 22750) return 1012.50;
        if (salary < 23250) return 1035.00;
        if (salary < 23750) return 1057.50;
        if (salary < 24250) return 1080.00;
        if (salary < 24750) return 1102.50;
        return 1125.00;
    }
    
    
    // Calculates the employee's PhilHealth contribution based on the 3% premium rate where the total premium is equally shared between the employer and the employee.
    public static double calculatePhilHealth(double salary) {
        double totalPremium;
        if (salary <= 10000) {
            totalPremium = 300;
        } else if (salary < 60000) {
            totalPremium = salary * 0.03;
        } else {
            totalPremium = 1800;
        }
        return totalPremium / 2;
    }
    
    
    // Calculates the Pag-IBIG contribution based on the salary rate, applying the 1% or 2% rule and limiting the maximum contribution to PHP 100.
    public static double calculatePagIbig(double salary) {
        double contribution;
        if (salary <= 1500) {
            contribution = salary * 0.01;
        } else {
            contribution = salary * 0.02;
        }
        return Math.min(contribution, 100);
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
        
