
import java.util.*;
import java.io.*;  
import java.nio.file.*; 





public class Parser {
    
    
    private treeNode root;
    private ArrayList<token> tokenStream;
    private ArrayList<String> errors;
    private typeTable symbolTable;
    private int currentToken;
    private String currentLine = "";

    
    
    
    
    
    
    public Parser(ArrayList<token> tokenStream)
    {
        this.tokenStream = tokenStream;
        symbolTable = new typeTable();
        errors = new ArrayList<String>();
        currentToken = 0;
    }

    public token getCurrentToken()
    {
        return tokenStream.get(currentToken); 

    }

    public void finalOutput()
    {
        output(root);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();  
    }

    public void processErrors()
    {
        for(String i : errors)
        {
            System.out.println(i);
        }  
    }
    


    public void output(treeNode tr1)
    {   
        String input;
        if(tr1 == null) {return;}


        
        if (tr1.getNodeName() == null && tr1.getLeft() == null && tr1.getMiddle() == null && tr1.getRight() == null) {return;}

        if (tr1.getNodeName() != null) 
        {
            input = tr1.getNodeName();
            if(input.length() < 6)
            {
                while (input.length() != 6) {input += " ";}
            }
            currentLine += (input + " ");

            if (currentLine.length() > 66) 
            {
                System.out.println(currentLine);
                currentLine = "";
            }
        }
        if (tr1.getLexeme() != null)
        {
            input = tr1.getLexeme();
            if(input.length() % 7 != 0)
            {
                while (input.length() % 7 != 0) {input += " ";}
                currentLine += (input);
            }
            else {currentLine += (input + "       ");}

            if (currentLine.length() > 66) 
            {
                System.out.println(currentLine);
                currentLine = "";
            }
        }
        output(tr1.getLeft());
        output(tr1.getMiddle());
        output(tr1.getRight()); 
    }

    //oh boy here we go
    //i wanna go back to when I first wrote that comment
    public void parseInput()
    {
        root = program();
    }

    //CD22 <id> <globals> <funcs> <mainbody>
    public treeNode program()
    {
        if (getCurrentToken().getTokenNum() != 1)
        {         
           errors.add("ERROR - \"CD22\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           currentToken--;
           //error recovery -- insert CD21
        }
        currentToken++;
        if (getCurrentToken().getTokenNum() != 58)
        {
            errors.add("ERROR - identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            currentToken--;
           //error recovery -- insert an ID
        }
        treeNode newNode = new treeNode("NPROG");
        newNode.setLexeme(getCurrentToken().getLexeme());
        symbolTable.addToTable(newNode.getLexeme(), "id");
        currentToken++;
        newNode.setLeft(globals());
        newNode.setMiddle(funcs());
        newNode.setRight(mainbody());
        return newNode;
    }

    // <consts> <types> <arrays>
    public treeNode globals()
    {
        treeNode newNode = new treeNode("NGLOB");
        newNode.setLeft(consts());
        newNode.setMiddle(types());
        newNode.setRight(arrays());
        return newNode;
    }

    // constants <initlist> | E
    public treeNode consts()
    {
        int tokenNum = getCurrentToken().getTokenNum();
        if (tokenNum == 3 || tokenNum == 5 || tokenNum == 6 || tokenNum == 11) //types arrays main func FOLLOW
        {
            return null;
        } 
        if (tokenNum == 2) //constants FIRST
        {
            currentToken++;
            return initlist();
        }
        errors.add("ERROR - invalid constants declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //error recovery - return null for now, can optimise with searching for FOLLOW
        return null;
    }

    // <init> | <init> , <initlist>
    //LEFT RECURSION 
    // <init> <inittail>
    public treeNode initlist()
    {
        treeNode tr1 = init();
        return inittail(tr1);
    }

    // , <initlist> | E
    public treeNode inittail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 3 || tokenNum == 5 || tokenNum == 6 || tokenNum == 11) //types arrays main func FOLLOW
        {
            return tr1;
        }
        else if (tokenNum == 33) //, FIRST
        {
            currentToken++;
            treeNode newNode = new treeNode("NILIST");
            newNode.setLeft(tr1);
            newNode.setRight(initlist());
            return newNode;
        }
        errors.add("ERROR - invalid intialisation list (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- act like the E path is chosen, TODO optimise by going through token until a FOLLOW is found
        return tr1;
    }

    //<id> = <expr>
    public treeNode init()
    {
        if (getCurrentToken().getTokenNum() != 58) //identifier
        {
            errors.add("ERROR - identifer expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery - assume the statement is missing
            return null;
        }
        treeNode newNode = new treeNode("NINIT");
        newNode.setLexeme(getCurrentToken().getLexeme());
        symbolTable.addToTable(newNode.getLexeme(), "id");
        currentToken++;

        if (getCurrentToken().getTokenNum() != 38) //=
        {
           errors.add("ERROR - \"=\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           //recovery - add is value
           currentToken--;
        }
        currentToken++;

        newNode.setLeft(expr());
        return newNode;   
    }

    // types <typelist> | E
    public treeNode types()
    {
        int tokenNum = getCurrentToken().getTokenNum();
        if (tokenNum == 5 || tokenNum == 6 || tokenNum == 11) //arrays main func FOLLOW
        {
            return null;
        } 
        else if (tokenNum == 3) //types FIRST
        {
            currentToken++;
            return typelist();
        }
        errors.add("ERROR - invalid types declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        return null;
    }

    // arrays <arrdecls> | E
    public treeNode arrays()
    {
        int tokenNum = getCurrentToken().getTokenNum();
        if (tokenNum == 6 || tokenNum == 11) //FOLLOW main func 
        {
            return null;
        } 
        else if (tokenNum == 5) //arrays FIRST
        {
            currentToken++;
            return arrdecls();
        }
        errors.add("ERROR - invalid arrays declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        return null;
    }

    // <func> <funcs> | E
    public treeNode funcs()
    {
        int tokenNum = getCurrentToken().getTokenNum();
        if (tokenNum == 11) //func FIRST
        {
            treeNode newNode = new treeNode("NFUNCS");
            newNode.setLeft(func());
            newNode.setRight(funcs());
            return newNode;
        }
        else if (tokenNum == 6) //FOLLOW main
        {
            return null;
        }
        errors.add("error -- invalid function declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        return null;
    }

    // main <slist> begin <stats> end CD21 <id>
    public treeNode mainbody()
    {
        if (getCurrentToken().getTokenNum() != 6) //main
        {
           errors.add("error -- \"main\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           //recovery -- insert a main keyword       
           currentToken--;
        }
        currentToken++;
        treeNode newNode = slist();
        if (getCurrentToken().getTokenNum() != 7) //begin
        {
            errors.add("error -- \"begin\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a begin keyword
            currentToken--;
        }
        currentToken++;
        treeNode newNode1 = stats();
        
        if (getCurrentToken().getTokenNum() != 8) //end
        {
           errors.add("error -- \"end\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           //recovery -- insert a end keyword
           currentToken--;
        } 
        currentToken++;

        if (getCurrentToken().getTokenNum() != 1) //CD22
        {
           errors.add("error -- \"CD22\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           //recovery -- insert a end keyword
           currentToken--;
        }
        currentToken++;

        if (getCurrentToken().getTokenNum() != 58) //identifier
        {
           errors.add("error -- identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           //recovery -- insert a blank id
           currentToken--;
        }
        treeNode parent = new treeNode("NMAIN");
        parent.setLexeme(getCurrentToken().getLexeme());
        symbolTable.addToTable(parent.getLexeme(), "id");

        
        currentToken++;
        parent.setLeft(newNode);
        parent.setRight(newNode1);
        return parent;       
    }

    // <sdecl> | <sdecl> , <slist>
    // LEFT RECURSION
    // <sdecl> <slisttail>

    public treeNode slist()
    {
        treeNode tr1 = sdecl();
        return slisttail(tr1);
    }

    // , <slist> | E
    public treeNode slisttail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();;
        if (tokenNum == 7) //FOLLOW begin
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NSDLST");
            newNode.setLeft(tr1);
            newNode.setRight(slist());
            return newNode;
        }
        errors.add("error -- invalid sdecl statement (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- act like the E path is chosen, TODO optimise by going through token until a FOLLOW is found
        return tr1;
    }

    // <type> | <type><typelist>
    // LEFT RECURSION
    // <type><typetail>

    public treeNode typelist()
    {
        treeNode tr1 = type();
        return typelisttail(tr1);
    }

    // <typelist> | E
    public treeNode typelisttail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 5 || tokenNum == 6 || tokenNum == 11) //FOLLOW arrays main func
        {
            return tr1;
        } 
        else if (tokenNum == 58) //FIRST identifier
        {
            treeNode newNode = new treeNode("NTYPEL");
            newNode.setLeft(tr1);
            newNode.setRight(typelist());
            return newNode;
        }
        errors.add("error -- invalid typelist declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- act like the E path is chosen, TODO optimise by going through token until a FOLLOW is found
        return tr1;
    }

    //<structid> def <fields> end | <typeid> def array [ <expr> ] of <structid> end 
    //LEFT FACTORED
    //<id> def <typetail>
    public treeNode type()
    {
        if (getCurrentToken().getTokenNum() != 58) //identifier
        {
           errors.add("error -- identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           //recovery -- return no node
           return null;
        }
        treeNode newNode = new treeNode();
        newNode.setLexeme(getCurrentToken().getLexeme());
        currentToken++;

        if (getCurrentToken().getTokenNum() != 4) //def
        {
            errors.add("error -- \"def\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a def keyword
            currentToken--;
        }
        currentToken++;
        
        return typetail(newNode);
    }

    //<fields> end | array [ <expr> ] of <structid> end
    public treeNode typetail(treeNode tr1) 
    {
        treeNode newNode;
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 58) //FIRST identifier
        {
            symbolTable.addToTable(tr1.getLexeme(), "structid");
            newNode = fields();
            if (getCurrentToken().getTokenNum() != 8) //end
            {
                errors.add("ERROR - \"end\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert an end keyword
                currentToken--;
            }
            currentToken++;
            tr1.setNodeName("NRTYPE");
            tr1.setLeft(newNode);
            return tr1;
        } 
        else if (tokenNum == 9) //FIRST array
        {
            symbolTable.addToTable(tr1.getLexeme(), "typeid");
            currentToken++;
            if (getCurrentToken().getTokenNum() != 34) // [
            {
                errors.add("ERROR - [ expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a [ symbol
                currentToken--;
            }
            currentToken++;
            
            newNode = expr();

            if (getCurrentToken().getTokenNum() != 35) // ]
            {
                errors.add("ERROR - ] expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a ] symbol
                currentToken--;
            }
            currentToken++;

            if (getCurrentToken().getTokenNum() != 10) // of
            {
                errors.add("ERROR - \"of\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert an of keyword
                currentToken--;
            }
            currentToken++;

            if (getCurrentToken().getTokenNum() != 58) // identifier
            {
                errors.add("ERROR - identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert an end keyword
                currentToken--;
            }
            symbolTable.addToTable(getCurrentToken().getLexeme(), "structid");
            currentToken++;

            if (getCurrentToken().getTokenNum() != 8) // end
            {
                errors.add("ERROR - \"end\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert an end keyword
                currentToken--;
            }
            currentToken++;
            tr1.setNodeName("NATYPE");
            tr1.setLeft(newNode);
            return tr1;
        }
        errors.add("ERROR -- invalid struct/array declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- return nothing
        return null;
    }

    // <sdecl> | <sdecl>,<fields> 
    // LEFT FACTORED
    // <sdecl> <fieldstail>
    public treeNode fields()
    {
        treeNode tr1 = sdecl();
        return fieldstail(tr1);
    }

    // , <fields> | E
    public treeNode fieldstail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 8) //FOLLOW end
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NFLIST");
            newNode.setLeft(tr1);
            newNode.setRight(fields());
            return newNode;
        }
        errors.add("ERROR -- invalid declaraction of variables (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- act like the E path is chosen, TODO optimise by going through token until a FOLLOW is found
        return tr1;
    }
    
    //<id> : <stype> | <id> : <structid>
    // LEFT FACTORED
    //<id> : <sdecltail>
    public treeNode sdecl()
    {
        if (getCurrentToken().getTokenNum() != 58) //identifier 
        {           
            errors.add("error -- identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- return null
            return null;  
        } 
        treeNode newNode = new treeNode();
        newNode.setLexeme(getCurrentToken().getLexeme());
        symbolTable.addToTable(newNode.getLexeme(), "id");
        currentToken++;
        if (getCurrentToken().getTokenNum() != 47) //:
        {
            errors.add("ERROR - expected \":\" (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a : symbol
            currentToken--;
        } 
        currentToken++; 
        return sdecltail(newNode);       
    }

    // <stype> | <structid>
    public treeNode sdecltail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 8) //FIRST <stype>
        {
            tr1.setNodeName("NSDECL");
            tr1.setLeft(stype());
            return tr1;
        } 
        else if (tokenNum == 58) //FIRST <structid>
        {
            tr1.setNodeName("NTDECL");
            tr1.setLexeme(getCurrentToken().getLexeme());
            symbolTable.addToTable(tr1.getLexeme(), "structid");
            currentToken++;
            return tr1;
        }
        errors.add("ERROR -- invalid declaraction of variables (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        return tr1;
    }

    // <arrdecl> | <arrdecl> , <arrdecls>
    // LEFT FACTORED
    // <arrdecl> <arrdecltail>
    public treeNode arrdecls()
    {
        treeNode tr1 = arrdecl();
        return arrdecltail(tr1);
    }

    // , <arrdecls> | E
    public treeNode arrdecltail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 6 || tokenNum == 11) //FOLLLOW func main 
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NALIST");
            newNode.setLeft(tr1);
            newNode.setRight(arrdecls());
            return newNode;
        }
        errors.add("ERROR - invalid array declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- act like the E path is chosen, TODO optimise by going through token until a FOLLOW is found
        return tr1;
    }

    //<id> : <typeid>
    public treeNode arrdecl()
    {
        if (getCurrentToken().getTokenNum() != 58) //identifier TODO reformat this to assume id
        {
            errors.add("ERROR - identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- return null
            return null;
        } 
        treeNode newNode = new treeNode("NARRD");
        newNode.setLexeme(getCurrentToken().getLexeme());
        symbolTable.addToTable(newNode.getLexeme(), "id");
        currentToken++;
        
        if (getCurrentToken().getTokenNum() != 47) //:
        {
            errors.add("ERROR - \":\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a : symbol
            currentToken--;
        } 
        currentToken++; 
        
        if (getCurrentToken().getTokenNum() != 58) //identifier
        {
            errors.add("ERROR - identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a blank id
            currentToken--;
        }
        currentToken++; 

        return newNode;
    }

    //func <id> ( <plist> ) : <rtype> <funcbody>
    public treeNode func()
    {
    
        if (getCurrentToken().getTokenNum() != 11) //func
        {
            errors.add("ERROR - \"func\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a func keyword
            currentToken--;
        }
        currentToken++;

        if (getCurrentToken().getTokenNum() != 58) //identifier
        {
            errors.add("ERROR - identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a blank id
            currentToken--;
        }       
        String newLexeme = getCurrentToken().getLexeme();
        symbolTable.addToTable(newLexeme, "id");
        currentToken++;

        if (getCurrentToken().getTokenNum() != 36) //(
        {
            errors.add("ERROR - \"(\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ( symbol
            currentToken--;
        } 
        currentToken++;

        treeNode newNode = plist();

        if (getCurrentToken().getTokenNum() != 37) //)
        {
            errors.add("ERROR - \")\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ) symbol
            currentToken--;
        }
        currentToken++;

        if (getCurrentToken().getTokenNum() != 47) //:
        {
            errors.add("ERROR - \":\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a : symbol
            currentToken--;
        }
        currentToken++;

        treeNode newNode1 = rtype();
        treeNode newNode2 = funcbody();

        
        treeNode parent = new treeNode("NFUND");
        parent.setLeft(newNode);
        parent.setMiddle(newNode1);
        parent.setRight(newNode2);
        parent.setLexeme(newLexeme);
        return parent;       
    }

    //<stype> | void
    public treeNode rtype()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 12) //FIRST void
        {
            currentToken++;
            return null;
        } 
        else if (tokenNum == 14 || tokenNum == 15 || tokenNum == 16) //FIRST integer float boolean
        {
            return stype();
        } 
        errors.add("ERROR - invalid return type (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume missing void TODO optimise by checking FOLLOW to get a new start point
        currentToken++;
        return null;
    }

    //<params> |  ε 
    public treeNode plist()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 58 || tokenNum == 13) //FIRST identifier const
        {
            return params();
        } 
        else if (tokenNum == 37) //FOLLOW )
        {
            return null;
        } 
        errors.add("ERROR - invalid plist declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return null;
    }
    

    // <param> | <param> , <params>
    // LEFT FACTORED
    // <param> <paramtail>
    public treeNode params()
    {
        treeNode tr1 = param();
        return paramstail(tr1);
    }

    // , <params> | E
    public treeNode paramstail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 37) //FOLLLOW )
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NPLIST");
            newNode.setLeft(tr1);
            newNode.setRight(params());
            return newNode;
        }
        errors.add("ERROR - invalid params declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return tr1;
    }

    //<sdecl> | <arrdecl> | const <arrdecl> 
    //LEFT FACTORING
    //const <arrdecl> | <id> : <paramtail>
    public treeNode param()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 14) //FIRST const
        {
            currentToken++;
            treeNode newNode = new treeNode("NARRC");
            newNode.setLeft(arrdecl());
            return newNode;
        }
        else if (tokenNum == 58) //FIRST identifier
        {
            treeNode newNode = new treeNode();
            newNode.setLexeme(getCurrentToken().getLexeme());
            symbolTable.addToTable(newNode.getLexeme(), "id");
            currentToken++;
            if (getCurrentToken().getTokenNum() != 47) //:
            {
                errors.add("ERROR - \":\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a : symbol
                currentToken--;
            } 
            currentToken++;
            return paramtail(newNode);  
        }
        errors.add("error -- parameter expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- return null        
        return null;
    }

    //<stype> | <typeid>
    public treeNode paramtail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 14 || tokenNum == 15 || tokenNum == 16) //FIRST integer float boolean
        {
            tr1.setNodeName("NSIMP");
            tr1.setLeft(stype());
            return tr1;
        }
        else if (tokenNum == 58) //FIRST identifier
        {
            tr1.setNodeName("NARRP");
            symbolTable.addToTable(getCurrentToken().getLexeme(), "typeid");
            currentToken++;
            return tr1;
        }
        errors.add("ERROR - invalid paramter (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null
        return null;
    }

    //<locals> begin <stats> end 
    public treeNode funcbody()
    {
        treeNode newNode = locals();

        if (getCurrentToken().getTokenNum() != 7) //begin
        {
            errors.add("error -- \"begin\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert an begin keyword
            currentToken--;
        }
        currentToken++;

        treeNode newNode1 = stats();


        if (getCurrentToken().getTokenNum() != 8) //end
        {
            errors.add("error -- \"end\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert an end keyword
            currentToken--;
        }
        currentToken++;

        treeNode parent = new treeNode();
        parent.setLeft(newNode);
        parent.setRight(newNode1);
        return parent;
    }

    // <dlist> | E
    public treeNode locals()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 7) //FOLLOW begin
        {
            return null;
        }
        else if (tokenNum == 58) //FIRST <dlist> identifier
        {
            return dlist();
        }
        errors.add("error - invalid locals declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // <decl> | <decl> , <dlist>
    // LEFT FACTORED
    // <decl> <dlisttail>
    public treeNode dlist()
    {
        treeNode tr1 = decl();
        return dlisttail(tr1);
    }

    // , <dlist> | E
    public treeNode dlisttail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 7) //FOLLLOW begin
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NDLIST");
            newNode.setLeft(tr1);
            newNode.setRight(dlist());
            return newNode;
        }
        errors.add("error - invalid dlist declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return tr1;
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    
    //<sdecl> | <arrdecl>
    //LEFT FACTORING
    //<id> : <decltail>
    public treeNode decl()
    {
        if (getCurrentToken().getTokenNum() != 58) //identifier TODO reformat this
        {
            errors.add("error - idetifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery - return null
            return null;
        }
        treeNode newNode = new treeNode();
        newNode.setLexeme(getCurrentToken().getLexeme());
        currentToken++;

        if (getCurrentToken().getTokenNum() != 47) //:
        {
            errors.add("error - : expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery - add a : symbol
            return null;
        }
        currentToken++;
        
        return decltail(newNode);
    }

    //<typeid> | <stype>
    public treeNode decltail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 58) //FIRST identifier
        {
            tr1.setNodeName("NARRD");
            symbolTable.addToTable(getCurrentToken().getLexeme(), "typeid");
            currentToken++;
            return tr1;
        }
        else if (tokenNum == 14 || tokenNum == 15 || tokenNum == 16) //FIRST integer float boolean
        {
            tr1.setNodeName("NSDECL");
            tr1.setLeft(stype());
            return tr1;
        }
        errors.add("error - invalid declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // int | float | boolean 
    public treeNode stype()
    {
        int tokenNum = getCurrentToken().getTokenNum();
        if (tokenNum == 14 || tokenNum == 15 || tokenNum == 16) //int float boolean
        {
            currentToken++;
            return null;
        }
        errors.add("error - expected type (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // <stat> ; <stats> | <strstat> <stats> | <stat> ; | <strstat>
    // LEFT FACTORING
    // <stat> ; <statstail> | <strstat> <stattail>
    public treeNode stats()
    {
        treeNode newNode = new treeNode();
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if(tokenNum == 17 || tokenNum == 20) //FIRST <strstat> for if
        {
            newNode = strstat();
            return statstail(newNode);
        }
        else if (tokenNum == 18 || tokenNum == 23 || tokenNum == 24 || tokenNum == 25 || tokenNum == 26 || tokenNum == 58) //FIRST <stat> repeat input print printline return <id>
        {
            newNode = stat();

            if (getCurrentToken().getTokenNum() != 48) //;
            {
                errors.add("error - ; expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery - add a ; symbol
                //currentToken--;
            }
            currentToken++;

            return statstail(newNode);
        }
        errors.add("error - statements expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // <stats> | E
    public treeNode statstail(treeNode left)
    {
        treeNode parent = new treeNode();
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if(tokenNum == 17 || tokenNum == 20 || tokenNum == 18 || tokenNum == 23 || tokenNum == 24 || tokenNum == 25 || tokenNum == 26 || tokenNum == 58) //FIRST <stats> for if repeat input print printline return <id>
        {
            parent.setNodeName("NSTATS");
            parent.setLeft(left);
            parent.setRight(stats());
            return parent;
        }
        else if (tokenNum == 8 || tokenNum == 19 || tokenNum == 21) //FOLLOW end until else
        {
            return left;
        }
        errors.add("invalid statement end (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return left;
    }
    
    // <forstat> | <ifstat>
    public treeNode strstat()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 17) //FIRST <forstat> for
        {
            return forstat();
        }
        if (tokenNum == 20) //FIRST <ifstat> if
        {
            return ifstat();
        }
        //error - pretty sure this error will never come up
        return null;
    }

    // <reptstat> | <asgnstat> | <iostat> | <callstat> | <returnstat> 
    // gonna avoid using the left factored version here as it expands the function way too much
    public treeNode stat() 
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 18) //FIRST <reptstat> repeat
        {
            return reptstat();
        }
        else if (tokenNum == 26) //FIRST <returnstat> return
        {
            return returnstat();
        }
        else if (tokenNum == 23 || tokenNum == 24 || tokenNum == 25) //FIRST <iostat> input print printline
        {
            return iostat();
        }
        else if (tokenNum == 58) //FIRST <id>
        {
            //look ahead to find correct path 
            currentToken++;
            tokenNum = tokenStream.get(currentToken).getTokenNum();
            if (tokenNum == 34 || tokenNum == 38 || tokenNum == 54 || tokenNum == 55 || tokenNum == 56 || tokenNum == 57) //FIRST <asgnstat> [ = += -= *= /=
            {
                currentToken--;
                return asgnstat();
            }
            else if (tokenNum == 36) //FIRST <callstat> (
            {
                currentToken--;
                return callstat();
            }  
            currentToken--;         
        }
        errors.add("error - statement expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    //for ( <asgnlist> ; <bool> ) <stats> end
    public treeNode forstat()
    {
        if (getCurrentToken().getTokenNum() != 17) //for //TODO reformat
        {
            errors.add("error -- \"for\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a for keyword
            currentToken--;
        }
        currentToken++;

        if (getCurrentToken().getTokenNum() != 36) //(
        {
            errors.add("error -- ( expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ( symbol)
            currentToken--;
        }
        currentToken++;

        treeNode newNode = asgnlist();

        if (getCurrentToken().getTokenNum() != 48) //;
        {
            errors.add("error -- ; expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ; symbol)
            currentToken--;
        }
        currentToken++;

        treeNode newNode1 = bool();

        if (getCurrentToken().getTokenNum() != 37) //)
        {
            errors.add("error -- ) expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ) symbol)
            currentToken--;
        }
        currentToken++;

        treeNode newNode2 = stats();


        if (getCurrentToken().getTokenNum() != 8) //end
        {
            errors.add("error -- \"end\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a end keyword
            currentToken--;
        }
        currentToken++;
        
        treeNode parent = new treeNode("NFORL");
        parent.setLeft(newNode);
        parent.setMiddle(newNode1);
        parent.setRight(newNode2);
        return parent;

    }
    
    //repeat ( <asgnlist> ) <stats> until <bool> 
    public treeNode reptstat()
    {
        treeNode parent = new treeNode("NREPT");
        if (getCurrentToken().getTokenNum() != 18) //repeat TODO reformat
        {
            errors.add("error -- \"repeat\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a repeat keyword
            currentToken--;
        }
        currentToken++;

        if (getCurrentToken().getTokenNum() != 36) //(
        {
            errors.add("error -- ( expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ( symbol)
            currentToken--;;
        }
        currentToken++;

        treeNode newNode = asgnlist();

        if (getCurrentToken().getTokenNum() != 37) //)
        {
            errors.add("error -- ) expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ) symbol)
            currentToken--;
        } 
        currentToken++;

        treeNode newNode1 = stats();

        if (getCurrentToken().getTokenNum() != 19) //until
        {
            errors.add("error -- \"until\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a until keyword
            currentToken--;
        }
        currentToken++;

        treeNode newNode2 = bool();

        parent.setLeft(newNode);
        parent.setMiddle(newNode1);
        parent.setRight(newNode2);
        return parent;       
    }

    //<alist> | ε 
    public treeNode asgnlist()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 48 || tokenNum == 37) //FOLLLOW ; )
        {
            return null;
        }
        else if (tokenNum == 58) //FIRST <alist> identifier
        {
            return alist();
        }
        errors.add("error - invalid asgnlist declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // <asgnstat> | <asgnstat> , <alist> 
    // LEFT FACTORED
    // <asgnstat> <alisttail>
    public treeNode alist()
    {
        treeNode tr1 = asgnstat();
        return alisttail(tr1);
    }

    // , <alist> | E
    public treeNode alisttail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 48 || tokenNum == 37) //FOLLLOW ; )
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NASGNS");
            newNode.setLeft(tr1);
            newNode.setRight(asgnstat());
            return newNode;
        }
        //throw error -- invalid alist statement
        errors.add("error -- invalid alist declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return tr1;
    }

    //------------------------
    
    //if ( <bool> ) <stats> end | if ( <bool> ) <stats> else <stats> end 
    //LEFT FACTORIZED
    //if ( <bool> ) <stats> <ifstattail>
    public treeNode ifstat()
    {
        treeNode parent = new treeNode();
        if (getCurrentToken().getTokenNum() != 20) //if
        {
            errors.add("error -- \"if\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert an if keyword
            currentToken--;
        }
        currentToken++;

        if (getCurrentToken().getTokenNum() != 36) //(
        {
           errors.add("error -- ( expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
           //recovery -- insert a ( symbol
           currentToken--;
        }
        currentToken++;

        treeNode newNode = bool();

        if (getCurrentToken().getTokenNum() != 37) //)
        {
            errors.add("error -- ) expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ) symbol
            currentToken--;
        } 
        currentToken++;

        treeNode newNode1 = stats();

        parent.setLeft(newNode);
        parent.setMiddle(newNode1);
        return ifstattail(parent);
    }

    // end | else <stats> end
    public treeNode ifstattail(treeNode parent)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 8) //end
        {
            currentToken++;
            parent.setNodeName("NIFTH");
            return parent;
        }
        else if (tokenNum == 21)
        {
            parent.setNodeName("NIFTE");
            currentToken++;
            treeNode newNode = stats();
            if (getCurrentToken().getTokenNum() != 8)
            {
                errors.add("error -- \"end\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert an end keyword
                currentToken--;
            }
            currentToken++;
            parent.setRight(newNode);
            return parent;
        }
        return null;
    }

    //<var> <asgnop> <bool> 
    public treeNode asgnstat()
    {
        treeNode newNode = new treeNode();
        newNode.setLeft(var());
        newNode.setMiddle(asgnop());
        newNode.setRight(bool());
        return newNode;
    }

    // = | += | -= | *= | /=
    public treeNode asgnop()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        treeNode newNode = new treeNode();
        if (tokenNum == 38) // =
        {
            newNode.setNodeName("NASGN");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 54) // +=
        {
            newNode.setNodeName("NPLEQ");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 55) // -=
        {
            newNode.setNodeName("NMNEQ");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 56) // *=
        {
            newNode.setNodeName("NSTEA");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 57) // /=
        {
            newNode.setNodeName("NDVEQ");
            currentToken++;
            return newNode;
        }
        errors.add("error - expected assign operator (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery -- return null
        return null;
    }

    // input <vlist> | print <prlist> | printline <prlist> 
    public treeNode iostat()
    {
        treeNode parent = new treeNode();
        if (getCurrentToken().getTokenNum() == 23) //input
        {
            currentToken++;
            parent.setNodeName("NINPUT");
            parent.setLeft(vlist());
            return parent;
        }
        else if (getCurrentToken().getTokenNum() == 24) //print
        {
            currentToken++;
            parent.setNodeName("NPRINT");
            parent.setLeft(prlist());
            return parent;
        }
        else if (getCurrentToken().getTokenNum() == 25) //printline
        {
            currentToken++;
            parent.setNodeName("NPRLN");
            parent.setLeft(prlist());
            return parent;
        }
        else {return null;}
    }


    //<id> ( <elist> )  |  <id> ( ) 
    //LEFT FACTORING
    //<id> ( <callstattail>
    public treeNode callstat()
    {
        if (getCurrentToken().getTokenNum() != 58) //identifier 
        {
            symbolTable.addToTable(getCurrentToken().getLexeme(), "id");
           return null;
        }
        treeNode newNode = new treeNode("NCALL");
        newNode.setLexeme(getCurrentToken().getLexeme());
        currentToken++;

        if (getCurrentToken().getTokenNum() != 36) //(
        {
            errors.add("error -- expected ( (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ( symbol
            currentToken--;
        }
        currentToken++;
        
        return callstattail(newNode);
    }

    //<elist> ) | )
    public treeNode callstattail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 27 || tokenNum == 58 || tokenNum == 59 || tokenNum == 60 || tokenNum == 31 || tokenNum == 32) //FIRST <elist> not <id> <intlit> <reallit> true false
        {
            tr1.setLeft(elist());
            tokenNum = tokenStream.get(currentToken).getTokenNum();
            if (tokenNum != 37)
            {
                errors.add("error -- expected ) (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a ( symbol
                currentToken--;
            }
            currentToken++;
            return tr1;
        } 
        else if (tokenNum == 37) //FIRST )
        {
            currentToken++;
            return tr1;
        }
        errors.add("error -- invalid call statement (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // return void |  return <expr> 
    // LEFT FACTORING
    // return <returnstattail>
    public treeNode returnstat()
    {
        if (getCurrentToken().getTokenNum() != 26) //FIRST return
        {
            errors.add("error -- \"return\" expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a return keyword
            currentToken--;
        }
        currentToken++;
        return returnstattail();
    }

    // void | <expr>
    public treeNode returnstattail()
    {
        treeNode newNode = new treeNode("NRETN");
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 12) //FIRST void
        {
            return newNode;
        } 
        else if (tokenNum == 58 || tokenNum == 59 || tokenNum == 60 || tokenNum == 31 || tokenNum == 32) //FIRST <expr> <id> <intlit> <reallit> true false 
        {
            newNode.setLeft(expr());
            return newNode;
        }
        errors.add("error -- invalid return statement (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // <var> | <var> , <vlist>
    // LEFT FACTORED
    // <var> <vlisttail>
    public treeNode vlist()
    {
        treeNode tr1 = var();
        return vlisttail(tr1);
    }

    // , <vlist> | E
    public treeNode vlisttail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 48) //FOLLLOW ;
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NVLIST");
            newNode.setLeft(tr1);
            newNode.setRight(vlist());
            return newNode;
        }
        errors.add("error -- invalid vlist declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return tr1;
    }

    // <id> | <id>[<expr>] . <id> | <id>[<expr>] 
    // LEFT FACTORED
    // <id> | <varmiddle>
    public treeNode var()
    {
        treeNode tr1 = new treeNode();
        if (getCurrentToken().getTokenNum() != 58) //identifier TODO reformat
        {
            symbolTable.addToTable(getCurrentToken().getLexeme(), "id");
        }
        tr1.setLexeme(getCurrentToken().getLexeme());
        //add to symbol table
        currentToken++;
        return varmiddle(tr1);
    }

    // E | [<expr>] <varend>
    public treeNode varmiddle(treeNode tr1)
    {
        if (getCurrentToken().getTokenNum() == 34) //[ 
        {
            currentToken++;
        
            treeNode newNode = expr();
        
            if (getCurrentToken().getTokenNum() != 35) //]
            {
                errors.add("error -- ] expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a ] symbol
                currentToken--;
            } 
            currentToken++; 
            
            tr1.setLeft(newNode);
            return varend(tr1);
        }
        else
        {
            tr1.setNodeName("NSIMV"); 
            return tr1;
        }
    }

    //.<id> | E
    public treeNode varend(treeNode tr1)
    {
        if (getCurrentToken().getTokenNum() == 49) // .
        {
            currentToken++;
            if (getCurrentToken().getTokenNum() != 58) // identifier
            {
                errors.add("error -- identifier expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a blank id
                currentToken--;
            }
            
            symbolTable.addToTable(getCurrentToken().getLexeme(), "typeid");
            currentToken++;
            tr1.setNodeName("NARRV"); 
            return tr1;

        }
        else
        {
            tr1.setNodeName("NAELT"); 
            return tr1;
        }
    }

    // <bool> | <bool> , <elist> 
    // LEFT FACTORED
    // <bool> <elisttail>
    public treeNode elist()
    {
        treeNode tr1 = bool();
        return elisttail(tr1);
    }

    // , <elist> | E
    public treeNode elisttail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 37) //FOLLLOW )
        {
            return tr1;
        } 
        else if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NEXPL");
            newNode.setLeft(tr1);
            newNode.setRight(elist());
            return newNode;
        }
        errors.add("error -- invalid elist declaration (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return tr1;
    }

    // <bool> <logop> <rel> | <rel>
    // LEFT FACTORED
    // <rel> <booltail>
    public treeNode bool()
    {
        treeNode tr1 = rel();
        return booltail(tr1); 
    }

    // <logop> <rel> <booltail> | E
    public treeNode booltail(treeNode left)
    {
        treeNode parent = new treeNode();
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 28 | tokenNum == 29 || tokenNum == 30) //FIRST and or xor
        {
            //System.out.println("doubt it");
            parent.setNodeName("NBOOL");
            parent.setMiddle(logop());
            parent.setLeft(left);
            parent.setRight(rel());
            return booltail(parent);
        } 
        else
        {
            return left;
        }
        
    }

    // not <expr> <relop> <expr> | <expr> <relop> <expr> | <expr>
    // LEFT FACTORED
    // not <expr> <relop> <expr> | <expr> <reltail>
    public treeNode rel()
    {
        treeNode newNode;
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 27)
        {
            currentToken++;
            newNode = new treeNode("NNOT");
            newNode.setLeft(expr());
            newNode.setMiddle(relop());
            newNode.setRight(expr());
            return newNode;
        }
        else
        {
            newNode = expr();
            return reltail(newNode);
        }
    }

    // <relop> <expr> | E
    public treeNode reltail(treeNode left)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum(); 
        if(tokenNum == 53|| tokenNum == 52 || tokenNum == 45 || tokenNum == 46 || tokenNum == 50 || tokenNum == 51) //FIRST <relop> == != > < >= <=
        {
            treeNode newNode = new treeNode();
            newNode.setMiddle(relop());
            newNode.setLeft(left);
            newNode.setRight(expr());
            return newNode;
        }
        else if (tokenNum == 33 || tokenNum == 48 || tokenNum == 37 || tokenNum == 28 || tokenNum == 29 || tokenNum == 30) //FOLLOW , ; ) and or xor
        {
            return left;
        }
        errors.add("error -- relation operator expectedd (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return left;
    }

    // and | or | xor
    public treeNode logop()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        treeNode newNode = new treeNode();
        if (tokenNum == 28) // and
        {
            newNode.setNodeName("NAND");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 29) // or
        {
            newNode.setNodeName("NOR");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 30) // xor
        {
            newNode.setNodeName("NXOR");
            currentToken++;
            return newNode;
        }
        errors.add("error -- logic operator expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // == | != | > | < | <= | >=
    public treeNode relop()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        treeNode newNode = new treeNode();
        if (tokenNum == 53) // ==
        {
            newNode.setNodeName("NEQL");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 52) // !=
        {
            newNode.setNodeName("NNEQ");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 46) // >
        {
            newNode.setNodeName("NGRT");
            currentToken++;
            return newNode;
        }
        if (tokenNum == 45) // <
        {
            newNode.setNodeName("NLSS");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 50) // <=
        {
            newNode.setNodeName("NLEQ");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 51) // >=
        {
            newNode.setNodeName("NGEQ");
            currentToken++;
            return newNode;
        }
        errors.add("error -- relation operator expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // <term> | <expr> + <term> | <expr> - <term>
    //LEFT FACTORING
    // <term> <exprtail>
    public treeNode expr()
    {
        treeNode tr1 = term();
        return exprtail(tr1); 
    }

    //+ <term> <exprtail> | - <term> <exprtail> | E
    public treeNode exprtail(treeNode left)
    {
        treeNode parent = new treeNode();
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 39) //FIRST +
        {
            currentToken++;
            parent.setNodeName("NADD");
            parent.setLeft(left);
            parent.setRight(term());
            return exprtail(parent);
        } 
        else if (tokenNum == 40) //FIRST -
        {
            currentToken++;
            parent.setNodeName("NSUB");
            parent.setLeft(left);
            parent.setRight(term());
            return exprtail(parent);
        }
        else if (tokenNum == 35 || tokenNum == 48 || tokenNum == 33 || tokenNum == 37 || tokenNum == 53 || tokenNum == 52 || tokenNum == 45 || tokenNum == 46 || tokenNum == 51 || tokenNum == 52 || tokenNum == 39 || tokenNum == 40 || tokenNum == 28 || tokenNum == 29 || tokenNum == 30 || tokenNum == 3 || tokenNum == 9) //FOLLOW ] ; , ) == != > < <= >=
        {
            return left;
        }
        
        errors.add("error - invalid expression (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return left;
    }

    // <term> * <fact> | <term> / <fact> |  <term> % <fact> | <fact>
    // LEFT FACTORING
    // <fact> <termtail>
    public treeNode term()
    {
        treeNode tr1 = fact();
        return termtail(tr1); 
    }

    // * <fact> <termtail> | / <fact> <termtail> | % <fact> <termtail> | E
    public treeNode termtail(treeNode left)
    {
        treeNode parent = new treeNode();
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 41) //FIRST *
        {
            currentToken++;
            parent.setNodeName("NMUL");
            parent.setLeft(left);
            parent.setRight(fact());
            return termtail(parent);
        } 
        else if (tokenNum == 42) //FIRST /
        {
            currentToken++;
            parent.setNodeName("NDIV");
            parent.setLeft(left);
            parent.setRight(fact());
            return termtail(parent);
        }
        else if (tokenNum == 43) //FIRST %
        {
            currentToken++;
            parent.setNodeName("NMOD");
            parent.setLeft(left);
            parent.setRight(fact());
            return termtail(parent);
        }
        else if (tokenNum == 35 || tokenNum == 48 || tokenNum == 33 || tokenNum == 37 || tokenNum == 53 || tokenNum == 52 || tokenNum == 45 || tokenNum == 46 || tokenNum == 51 || tokenNum == 52 || tokenNum == 39 || tokenNum == 40 || tokenNum == 28 || tokenNum == 29 || tokenNum == 30 || tokenNum == 3 || tokenNum == 9) //FOLLOW ] ; , ) == != > < <= >= + - and or xor types arrays
        {
            return left;
        }
        
        errors.add("error - invalid term (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return left;
    }

    // <fact> ^ <exponent> | <exponent>
    // LEFT FACTORING
    // <exponent> <facttail>
    public treeNode fact()
    {
        treeNode tr1 = exponent();
        return facttail(tr1); 
    }

    // ^ <exponent> | E
    public treeNode facttail(treeNode left)
    {
        treeNode parent = new treeNode();
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 44) //FIRST ^
        {
            currentToken++;
            parent.setNodeName("NPOW");
            parent.setLeft(left);
            parent.setRight(exponent());
            return facttail(parent);
        } 
        else if (tokenNum == 35 || tokenNum == 48 || tokenNum == 33 || tokenNum == 37 || tokenNum == 53 || tokenNum == 52 || tokenNum == 45 || tokenNum == 46 || tokenNum == 51 || tokenNum == 52 || tokenNum == 39 || tokenNum == 40 || tokenNum == 41 || tokenNum == 42 || tokenNum == 43 || tokenNum == 28 || tokenNum == 29 || tokenNum == 30 || tokenNum == 3 || tokenNum == 9) //FOLLOW ] ; , ) == != > < <= >= << + - * / % and or xor types arrays
        {
            return left;
        }
        
        errors.add("error - invalid exponent (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - assume E TODO optimise by checking FOLLOW to get a new start point
        return left;
    }

    // <var> | <intlit> | <reallit> | <fncall> | true | false | ( <bool> )
    // same thing as stat(), wanted to reduce clutter
    public treeNode exponent()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        treeNode newNode = new treeNode();
        if (tokenNum == 31) // true
        {
            newNode.setNodeName("NTRUE");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 32) // false
        {
            newNode.setNodeName("NFALSE");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 59) // <intlit>
        {
            newNode.setNodeName("NILIT");
            newNode.setLexeme(getCurrentToken().getLexeme());
            symbolTable.addToTable(newNode.getLexeme(), "intlit");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 60) // <reallit>
        {
            newNode.setNodeName("NFLIT");
            newNode.setLexeme(getCurrentToken().getLexeme());
            symbolTable.addToTable(newNode.getLexeme(), "reallit");
            currentToken++;
            return newNode;
        }
        else if (tokenNum == 36) // (
        {
            currentToken++;
            newNode = bool();
            if(getCurrentToken().getTokenNum() != 37) // )
            {
                errors.add("error -- ) expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a ) symbol
                currentToken--;
            }
            return newNode;
        }
        else if (tokenNum == 58) // identifier
        {
            //look ahead to find correct path 
            currentToken++;
            tokenNum = tokenStream.get(currentToken).getTokenNum();
            if (tokenNum == 36) 
            {
                currentToken--;
                return fncall();
            }  
            else 
            {
                currentToken--;
                return var();
            }  
        }
        errors.add("error - expected exponent");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;

    }

    //<id> ( <elist> )  |  <id> ( ) 
    //LEFT FACTORING
    //<id> ( <fncalltail>
    public treeNode fncall()
    {
        if (getCurrentToken().getTokenNum() != 58) //identifier 
        {
            symbolTable.addToTable(getCurrentToken().getLexeme(), "id");
        }
        treeNode newNode = new treeNode("NFCALL");
        newNode.setLexeme(getCurrentToken().getLexeme());
        //add to symbol table
        currentToken++;

        if (getCurrentToken().getTokenNum() != 36) //(
        {
            errors.add("error -- ( expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
            //recovery -- insert a ( symbol
            currentToken--;
        }
        currentToken++;
        
        return fncalltail(newNode);
    }

    //<elist> ) | )
    public treeNode fncalltail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 27 || tokenNum == 58 || tokenNum == 59 || tokenNum == 60 || tokenNum == 31 || tokenNum == 32) //FIRST <elist> not <id> <intlit> <reallit> true false
        {
            tr1.setLeft(elist());
            tokenNum = tokenStream.get(currentToken).getTokenNum();
            if (tokenNum != 37)
            {
                errors.add("errorrr -- ) expected (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
                //recovery -- insert a ) symbol
                currentToken--;
            }
            currentToken++;
            return tr1;
        } 
        else if (tokenNum == 37) //FIRST )
        {
            currentToken++;
            return tr1;
        }
        errors.add("error - invalid print list (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;
    }

    // <printitem> | <printitem> , <prlist>
    // LEFT FACTORED
    // <printitem> <printitemtail>
    public treeNode prlist()
    {
        treeNode tr1 = printitem();
        return printitemtail(tr1);
    }

    // , <prlist> | E
    public treeNode printitemtail(treeNode tr1)
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 33) //FIRST ,
        {
            currentToken++;
            treeNode newNode = new treeNode("NPRLST");
            newNode.setLeft(tr1);
            newNode.setRight(prlist());
            return newNode;
        }
        else if (tokenNum == 48) //FOLLOW ;
        {
            return tr1;
        }
        errors.add("error - invalid print statement  (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        return null;  

    }

    //<expr> | <string>
    public treeNode printitem()
    {
        int tokenNum = tokenStream.get(currentToken).getTokenNum();
        if (tokenNum == 58 || tokenNum == 59 || tokenNum == 60 || tokenNum == 31 || tokenNum == 32) //FIRST <expr> <id> <intlit> <reallit> true false
        {
            return expr();
        }
        else if (tokenNum == 61) //string
        {
            treeNode newNode = new treeNode("NSTRG");
            newNode.setLexeme(getCurrentToken().getLexeme());
            symbolTable.addToTable(getCurrentToken().getLexeme(), "string");
            currentToken++;
            return newNode;
        }
        errors.add("error - invalid print item  (Line: " + getCurrentToken().getLine() + " Column: " + getCurrentToken().getColumn() + ")");
        //recovery - return null TODO optimise by checking FOLLOW to get a new start point
        return null;  
    }
}









