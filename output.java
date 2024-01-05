import java.util.*;
import java.io.*;  
import java.nio.file.*; 



public class output 
{
    private int currentPos;

    public output()
    {
        currentPos = 1;
    }
    
    public void startNewLine()
    {
        System.out.print(currentPos + ":");
        currentPos++;
    }

    public void printChar(char input)
    {
        System.out.print(input);
    }

    public void endListing()
    {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    
}
