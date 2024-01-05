/*
Name: Scott Lonsdale
Course: COMP3290
Student Number: C3303788
*/


import java.util.*;
import java.io.*;



public class treeNode
{
    private treeNode left, middle, right;
    private String nodeName;
	private String lexeme;
    
    public treeNode()
    {
        left = null;
        right = null;
        middle = null;
        nodeName = null;
		lexeme = null;
    } 
	
	public treeNode(String nodeName)
    {
        left = null;
        right = null;
        middle = null;
        this.nodeName = nodeName;
		lexeme = null;
    } 

    public void setLeft(treeNode left)
	{
		this.left = left;
	}

	public treeNode getLeft()
	{
		return left;
	}

    public void setMiddle(treeNode middle)
	{
		this.middle = middle;
	}

	public treeNode getMiddle()
	{
		return middle;
	}

	public void setRight(treeNode right)
	{
		this.right = right;
	}

	public treeNode getRight()
	{
		return right;
	}

	public void setNodeName(String nodeName)
	{
		this.nodeName = nodeName;
	}

	public String getNodeName()
	{
		return nodeName;
	}

	public void setLexeme(String lexeme)
	{
		this.lexeme = lexeme;
	}

	public String getLexeme()
	{
		return lexeme;
	}



}