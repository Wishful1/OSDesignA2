/*
Name: Scott Lonsdale
Course: COMP3290
Student Number: C3303788
*/


import java.util.*;

import javax.xml.stream.events.StartDocument;

import java.io.*;
import java.lang.annotation.Repeatable;  


public class testScanner //main object for scanner
{  
    private String input; //the text input of the code
    private String buffer; //buffer used in the scanner
    private ArrayList<String> lexStrings; //lexstrings picked up by the scanner
    private String[] symbols; //these arrays contain the dictionary or allowed terms in SM21
    private String[] keywords; 
    private String[] tokens;
    private ArrayList<String> errors; //errors picked up by the scanner
    private int currentError; //the currentError used by output functions
    private int position; //current overall position inside the file  
    private int column; //current column
    private int line; //current row
    private int startc;
    private int startl;
    private output listingOutput;

    public testScanner(String rawCode)
    {
        input = rawCode;
        currentError = 0;
        symbols = new String[]{",", "[", "]", "(", ")", "=", "+", "-", "*", "/", "%", "^", "<", ">", ":", ";", ".", "<=", ">=", "!=", "==", "+=", "-=", "*=", "/=", ";", "."};
        keywords = new String[]{"cd22", "constants", "types", "def", "arrays", "main", "begin", "end", "array", "of", "func", "void", "const", "int", "float", "bool", "for", "repeat", "until", "if", "else", "elif", "input", "print", "printline", "return", "not", "and", "or", "xor", "true", "false"};
        tokens = new String[]{
			"TTEOF ",
			"TCD22 ",	"TCONS ",	"TTYPS ",	"TTDEF ",	"TARRS ",	"TMAIN ",
			"TBEGN ",	"TTEND ",	"TARAY ",	"TTTOF ",	"TFUNC ",	"TVOID ",
			"TCNST ",	"TINTG ",	"TFLOT ",	"TBOOL ",	"TTFOR ",	"TREPT ",
			"TUNTL ",	"TIFTH ",	"TELSE ",	"TELIF ",   "TINPT ",	"TPRNT ",  "TPRLN ",
			"TRETN ",	"TNOTT ",	"TTAND ",	"TTTOR ",	"TTXOR ",	"TTRUE ",
			"TFALS ",	"TCOMA ",	"TLBRK ",	"TRBRK ",	"TLPAR ",	"TRPAR ",
			"TEQUL ",	"TPLUS ",	"TMINS ",	"TSTAR ",	"TDIVD ",	"TPERC ",
			"TCART ",	"TLESS ",	"TGRTR ",	"TCOLN ",	"TSEMI ",   "TDOTT ",  "TLEQL ",  "TGEQL ",
			"TNEQL ",	"TEQEQ ",	"TPLEQ ",	"TMNEQ ",	"TSTEQ ",	"TDVEQ ",
			"TIDEN ",	"TILIT ",	"TFLIT ",	"TSTRG ",	"TUNDF "};
        lexStrings = new ArrayList<String>();
        errors = new ArrayList<String>();
        listingOutput = new output();
        position = 0;
        column = 1;
        line = 1;
        listingOutput.startNewLine();
    }
    
    public String returnCurrentState(char currentChar) //returns the current char type the position is currently on
    {
        if (Character.isAlphabetic(currentChar)) {return "LETTER";}
        else if (Character.isDigit(currentChar)) {return "DIGIT";}
        else if (Arrays.stream(symbols).anyMatch(String.valueOf(currentChar)::equals)) {return "SYMBOL";}
        else if (currentChar == ('"')) {return "STRING";}
        else if (currentChar == ('\n')) {return "NEWLINE";}
        else if (currentChar == ('\r')) {return "CARRETURN";}
        else if (Character.isWhitespace(currentChar)) {return "WHITESPACE";}
        return "ERROR";
    }
    
    public token getToken() //the core function, generates a token from the text input
    {  
        token newToken = new token();
        while (!eof())
        {
            String currentState = returnCurrentState(input.charAt(position));
            switch(currentState) //from the response of returnCurrentState, goes through a switch case according to the string return
            {
                case "WHITESPACE": //ws, nl and carrreturn are all treated as empty space
                    listingOutput.printChar(input.charAt(position));
                    position++;
                    column++;
                    break;
                
                case "NEWLINE": 
                    listingOutput.printChar(input.charAt(position));
                    position++;
                    column = 1;
                    line++;
                    if (!eof()) {listingOutput.startNewLine();}
                    break;

                case "CARRETURN":
                    listingOutput.printChar(input.charAt(position));
                    position++;
                    break;
                
                case "LETTER": //for the letter case, goes through the file until there are no more letters, and crossreferences with the keywords array, outptting as a constant or keyword depending
                    buffer = "";
                    startc = column;
                    startl = line;
                    do
                    {    
                       buffer += String.valueOf(input.charAt(position));
                       listingOutput.printChar(input.charAt(position));
                       position++;
                       column++;
                    } while (!eof() && returnCurrentState(input.charAt(position)).matches("LETTER|DIGIT"));
                    
                    String testWord = buffer.toLowerCase();
                    for (int i = 0; i < keywords.length; i++) 
                    {
                        if(testWord.equals(keywords[i]))
                        {
                            newToken.setTokenNum(i+1); //keyword
                            newToken.setColumn(startc);
                            newToken.setLine(startl);
                            return newToken;
                        }
                    }

                    newToken.setTokenNum(58); //identifier
                    newToken.setLexeme(buffer);
                    newToken.setColumn(startc);
                    newToken.setLine(startl);
                    return(newToken);

                case "DIGIT": //digit follows the same process as string, except if a dot is found the final number is passed as a float, if no dot is found it's passed as a int
                    boolean tflit = false;
                    buffer = "";
                    startc = column;
                    startl = line;
                    do
                    { 
                        if(returnCurrentState(input.charAt(position)).matches("SYMBOL"))
                        {
                            if (position != input.length()-1 && String.valueOf(input.charAt(position)).equals(".") && tflit == false && returnCurrentState(input.charAt(position+1)).matches("DIGIT")) {tflit = true;}
                            else 
                            {break;}
                        }
                        listingOutput.printChar(input.charAt(position));
                        buffer += String.valueOf(input.charAt(position));
                        position++;
                        column++;
                    } while (!eof() && returnCurrentState(input.charAt(position)).matches("DIGIT|SYMBOL"));
                    if(tflit == true)
                    {
                        newToken.setTokenNum(60);
                        newToken.setLexeme(buffer);
                        newToken.setColumn(startc);
                        newToken.setLine(startl);
                        return newToken;
                    }
                    else
                    {
                        newToken.setTokenNum(59);
                        newToken.setLexeme(buffer);
                        newToken.setColumn(startc);
                        newToken.setLine(startl);
                        return newToken;
                    }

                case "SYMBOL": //the most complex case, firstly checks for double character symbols, then checks for comments, then defaulting to checking for single character symbols
                    buffer = "";
                    startc = column;
                    startl = line;
                    buffer += String.valueOf(input.charAt(position));
                    if(position != input.length()-1 && returnCurrentState(input.charAt(position+1)).matches("SYMBOL")) //passes two characters into the buffer and checks against the symbols array
                        {
                            buffer += String.valueOf(input.charAt(position+1));
                            for (int i = 0; i < symbols.length; i++) 
                            {
                                if(buffer.equals(symbols[i]))
                                {
                                    newToken.setTokenNum(i+33);
                                    newToken.setColumn(startc);
                                    newToken.setLine(startl);
                                    listingOutput.printChar(input.charAt(position));
                                    listingOutput.printChar(input.charAt(position+1));
                                    position += 2;
                                    column += 2;
                                    return newToken;
                                }
                            }
                        }

                    if(position != input.length()-2 && returnCurrentState(input.charAt(position+2)).matches("SYMBOL")) //passes 3 characters into the buffer and checks for comments starts
                        {
                            
                            buffer += String.valueOf(input.charAt(position+2));
                            if (buffer.equals("/--")) //single line comments end when a newline is found
                            {
                                listingOutput.printChar(input.charAt(position));
                                listingOutput.printChar(input.charAt(position+1));
                                listingOutput.printChar(input.charAt(position+2));
                                position += 3;
                                column += 3;
                                while (!eof() && !returnCurrentState(input.charAt(position)).matches("NEWLINE"))
                                {
                                    listingOutput.printChar(input.charAt(position));
                                    position++;
                                    column++;
                                }
                                break;
                            }
                            else if (buffer.equals("/**")) //mutli line comments only end when a closing comment symbol is found
                            {
                                buffer = "   ";
                                listingOutput.printChar(input.charAt(position));
                                listingOutput.printChar(input.charAt(position+1));
                                listingOutput.printChar(input.charAt(position+2));
                                position += 3;
                                column += 3;
                                while (!eof() && !buffer.substring(buffer.length()-3).equals("**/"))
                                {
                                    buffer += String.valueOf(input.charAt(position));
                                    listingOutput.printChar(input.charAt(position));
                                    position++;
                                    column++;
                                }
                                
                                break;
                            }
                        }
      
                    for (int i = 0; i < symbols.length; i++) //checks the single character symbols
                        {
                            if(String.valueOf(input.charAt(position)).equals(symbols[i]))
                            {
                                newToken.setTokenNum(i+33);
                                newToken.setColumn(startc);
                                newToken.setLine(startl);
                                listingOutput.printChar(input.charAt(position));
                                position++;
                                column++;
                                return newToken;
                            }
                        }
                case "ERROR":
                    buffer = "";
                    startc = column;
                    startl = line;
                    if(position != input.length()-1 && String.valueOf(input.charAt(position)).equals("!")) //unique case for !=
                        {
                            buffer += String.valueOf(input.charAt(position));
                            buffer += String.valueOf(input.charAt(position+1));
                            if(buffer.equals("!="))
                            {
                                newToken.setTokenNum(52);
                                newToken.setColumn(startc);
                                newToken.setLine(startl);
                                listingOutput.printChar(input.charAt(position));
                                listingOutput.printChar(input.charAt(position+1));
                                position += 2;
                                column += 2;
                                return newToken;
                            }
                            buffer = "";
                        }
                    do
                    {  
                        buffer += String.valueOf(input.charAt(position));
                        listingOutput.printChar(input.charAt(position));
                        position++;
                        column++;
                    } while (!eof() && returnCurrentState(input.charAt(position)).matches("ERROR"));
                    errors.add("lexical error " + buffer + " (Line: " + line + " Column: " + (column-1) + ")");
                    newToken.setTokenNum(62);
                    newToken.setLexeme(buffer);
                    newToken.setColumn(startc);
                    newToken.setLine(startl);
                    return newToken;

                case "STRING":
                    startc = column;
                    startl = line;
                    buffer = "";
                    buffer += '"';
                    listingOutput.printChar(input.charAt(position));
                    position++;
                    column++;
                    while (!eof() && !returnCurrentState(input.charAt(position)).matches("NEWLINE"))
                    {
                        buffer += String.valueOf(input.charAt(position));
                        if(!eof() && (input.charAt(position)) == ('"'))
                        {
                            newToken.setTokenNum(61);
                            newToken.setLexeme(buffer);
                            newToken.setColumn(startc);
                            newToken.setLine(startl);
                            listingOutput.printChar(input.charAt(position));
                            position++;
                            column++;
                            return newToken;
                        }
                        listingOutput.printChar(input.charAt(position));
                        position++;
                        column++;
                    }  
                    errors.add("lexical error " + buffer + " (Line: " + line + " Column: " + column + ")");
                    newToken.setTokenNum(62); 
                    newToken.setLexeme(buffer);
                    newToken.setColumn(startc);
                    newToken.setLine(startl);
                    return newToken;               
            }
        }
        return newToken;
    }
    
    
    public String printToken(token token)
    {
        String output = "";
        int tokenNo = token.getTokenNum();
        output += tokens[tokenNo];
        if (token.getLexeme() != null)
        {
            output += token.getLexeme();
            output += " ";
        }
        if(tokenNo == 62)
        {
            output += errors.get(currentError);
            currentError++;
            output += " ";
            return output;
        }
        return output;
    }
    
    public boolean eof()
    {
        //System.out.println(position);
        if(position == input.length()) {return true;}
        else {return false;}
    }

    public ArrayList<token> processInput()
    {
        //output printOutput = new output();
        //printOutput.startNewLine();
        ArrayList<token> tokenSet = new ArrayList<token>();
        token currentToken;
        do
        {
            currentToken = getToken();
            tokenSet.add(currentToken);
        } while (currentToken.getTokenNum() != 0);

        listingOutput.endListing();

        return tokenSet;
    }

    public void processErrors()
    {
        for(String i : errors)
        {
            System.out.println(i);
        }  
    }

    public void standardOutput()
    {
        String currentLine = "";
        int currentOutputError = 0;
        token currentToken;
        do
        {
            currentToken = getToken();
            String token = printToken(currentToken);
            if(currentToken.getTokenNum() == 62)
            {
                if (currentLine.length() != 0) {System.out.println(currentLine);}
                System.out.println("TUNDF");
                System.out.println("lexical error " + errors.get(currentOutputError));
                currentLine = "";
                currentOutputError++;
            }
            else
            {
                currentLine += token;
                if (currentLine.length() > 60) 
                {
                    System.out.println(currentLine);
                    currentLine = "";
                }
            }  
        } while (currentToken.getTokenNum() != 0);
        
        if (currentLine.length() != 0)
        {
            System.out.println(currentLine);
        }
    
    }
    







	

}

