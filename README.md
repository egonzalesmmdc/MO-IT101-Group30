# MOTORPH SYSTEM - GROUP 30

## Introduction
> Founded in 2020, MotorPH aims to be the top choice for Filipinos seeking competitive and affordable motorcycles. As the company expands its private transportation services, it requires a robust automated system to handle employee data and compensation. This repository contains the Phase 1 implementation of the MotorPH System. The primary goal is to transition from manual processing to an automated environment that ensures accuracy in time tracking and salary disbursement.

## Program Details
> The MotorPH System is a console-based Java application that automates the core requirements of Phase 1: employee data management and weekly salary calculation.

### + Core Functionalities
- Employee Information Display: Loads data from CSV files and presents employee numbers, full names, and birthdays in the prescribed format.
- Time Tracking: Scans attendance records to calculate total hours worked. The system applies an 8:00 AM shift logic with a 10-minute grace period and a strict 5:00 PM cutoff.
- Salary Automation:
- - Gross Wage: Multiplies calculated work hours by the employee's hourly rate.
- - Deductions: Applies statutory Philippine deductions (SSS, PhilHealth, Pag-IBIG) and withholding tax.
- - Net Salary: Computes the final take-home pay after all generic and mandatory deductions.
 
## System Walkthrough & Screenshots
> The following link contains high-resolution screenshots of the system's execution flow. Each image captures a specific milestone in the user journey—from the initial login to the final net salary generation.
#### [DOCUMENTATION OF MOTORPH SYSTEM - PHASE 1](https://drive.google.com/drive/folders/10RRjnKmBP6iueV_8aIb2IKCEDHv-ZxtT?usp=sharing)

### + Key Interface Highlights
- Authentication Layer: Displays the login prompt for employee and payroll_staff roles, including error handling for incorrect credentials.
- Employee Information Display: Shows the "Employee Details" output, verifying that the system correctly parses names and birthdays from the CSV file.
- Payroll Processing (Single/Bulk):
- - Screenshots of the console output for a single employee lookup.
- - Screenshots showing the automated loop for all employees, featuring the "Payroll Processing Status: Finished" confirmation.
- Deduction Breakdown: Visual evidence of the itemized SSS, PhilHealth, Pag-IBIG, and Withholding Tax calculations.
 
## Team Details: GROUP 30
> The development of this system was divided among the following members:

### Eiu-Jin Gonzales
- KEY CONTRIBUTIONS: Built the login authentication system, employee detail displays, and hardcoded government deduction tables.

### Karl De Pano
- KEY CONTRIBUTIONS: Implemented 10-minute grace period logic, gross salary formulas, and final net salary calculations.

### Ken Adrian Villamor
- KEY CONTRIBUTIONS: Developed work hour computation, payroll processing menus (Solo/All employees), and CSV file reading modules.

## Project Plan
> You can access the live tracking of this development via the link below. This document reflects real-time updates as we complete specific MotorPH requirements and system milestones.
#### [PROJECT PLAN LINK](https://docs.google.com/spreadsheets/d/1Cc4IPmUY1yKCRLq9AjWWywvPiit5ydBFOYIVEF-JsVg/edit?usp=sharing)

