public class token 
{
    private int tokNo;
    private String lexeme;
    private int line;
    private int col; 

    public token() //contructor
	{
	    tokNo = 0;
	    lexeme = null;
	    line = 0;
        col = 0;
	}
    
    public void setTokenNum(int tokNo)
	{
		this.tokNo = tokNo;
	}

	public int getTokenNum()
	{
		return tokNo;
	}

    public void setLexeme(String lexeme)
	{
		this.lexeme = lexeme;
	}

	public String getLexeme()
	{
		return lexeme;
	}

	public void setLine(int line)
	{
		this.line = line;
	}

	public int getLine()
	{
		return line;
	}

    public void setColumn(int column)
	{
		this.col = column;
	}

	public int getColumn()
	{
		return col;
	}
}
