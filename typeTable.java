import java.util.*;
import java.io.*;  
import java.nio.file.*; 




public class typeTable 
{
    private HashMap<String, String> typeTable;

    public typeTable()
    {
        typeTable = new HashMap<String, String>();
    }
   
    public void addToTable(String input, String type) 
    {
        boolean inList = false;
        for (String i : typeTable.keySet()) 
        {
            if (i == input)
            {
                //dunno just yet, including an incrementor for each occurence might be helpful for A3 later on
                inList = true;
            }
        }

        if (inList == false)
        {
            typeTable.put(input, type);
        }
    }
}
