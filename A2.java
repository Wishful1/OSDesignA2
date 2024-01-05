/*
Name: Scott Lonsdale
Course: COMP3290
Student Number: C3303788
*/

import java.util.*;
import java.io.*;  
import java.nio.file.*; 


public class A2
{
    public static void main (String args[]) throws FileNotFoundException
    {
        String inputCode;

        try
        {
        inputCode = new String(Files.readAllBytes(Paths.get(args[0])));
        testScanner scanner = new testScanner(inputCode);
        //scanner.standardOutput();
        ArrayList<token> output = scanner.processInput();
        scanner.processErrors();
        Parser parser = new Parser(output);
        parser.parseInput();
        parser.processErrors();
        parser.finalOutput();
        } catch(Exception e){System.out.println("File input error");};


        
        
        
       
        

    }
}