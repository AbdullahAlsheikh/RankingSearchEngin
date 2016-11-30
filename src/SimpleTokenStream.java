import java.io.*;
import java.util.*;

/**
Reading tokens one at a time from an input stream. Returns tokens with minimal
processing: removing all non-alphanumeric characters, and converting to 
lowercase.
*/
public class SimpleTokenStream implements TokenStream {
   private Scanner mReader;

   /**
   Constructs a SimpleTokenStream to read from the specified file.
   */
   public SimpleTokenStream(File fileToOpen) throws FileNotFoundException {
      mReader = new Scanner(new FileReader(fileToOpen));
   }
   
   /**
   Constructs a SimpleTokenStream to read from a String of text.
   */
   public SimpleTokenStream(String text) {
	   try{
		   mReader = new Scanner(text);
	   }catch(Exception e){
		   
			System.out.println(text + "Error (SimpeTokemStream(Text)) - > " + e);
		   e.printStackTrace();
	   }
      
   }

   /**
   Returns true if the stream has tokens remaining.
   */
   @Override
   public boolean hasNextToken() {
      return mReader.hasNext();
   }

   /**
    * nextToken: will return {@value next} term within the file to be indexed
    * @return next 
    */
   @Override
   public String nextToken() {
      if (!hasNextToken())
         return null;
      
      String next = mReader.next();
      System.out.println(next);
      if(next.contains("("))
      {
//    	  System.out.println(next);
    	  next = next.replace("(", "");
      }
      
      if(next.contains(")"))
      {
//    	  System.out.println(next);
    	  next = next.replace(")", "");
      }
      
      if(next.contains("/"))
      {
//    	  System.out.println(next);
    	  next = next.replace("/", "");
      }
      
      if(next.contains("\\"))
      {
//    	  System.out.println(next);
    	  next = next.replace("\\", "");
      }
      
      if(next.contains("\""))
      {
//    	  System.out.println(next);
    	  next = next.replace("\"", "");
      }
      
      if(next.contains(".")){
//    	  System.out.println(next);
    	  next = next.replace(".", "");
    	  
      }
      
      if(next.contains("{")){
//    	  System.out.println(next);
    	  next = next.replace("{", "");
      }
      
      if(next.contains("}")){
//    	  System.out.println(next);
    	  next = next.replace("}", "");
      }
      
      if(next.contains(",")){
//    	  System.out.println(next);
    	  next = next.replace(",", "");
    	  
      }
      
      if(next.contains(":")){
//    	  System.out.println(next);
    	  next = next.replace(":", "");
  
      }
      
      if(next.contains("\\u0027s")){
//    	  System.out.println(next);
    	  next = next.replace("\\u0027s","");  
      }
      
      if(next.contains("\u0027")){
//    	  System.out.println(next);
    	  next = next.replace("\u0027","");  
      }
      
      if(next.contains("’")){
//    	  System.out.println(next);
    	  next = next.replace("’","");  
      }
      
      if(next.contains("‘")){
//    	  System.out.println(next);
    	  next = next.replace("‘","");  
      }
      
      if(next.contains("“")){
    	  next = next.replace("“","");
      }
      
      if(next.contains("”")){
    	  next = next.replace("”","");
      }
      
      if(next.contains("ʻ")){
    	  next = next.replace("ʻ","");
      }
      
      if(next.contains("ʻ")){
    	  next = next.replace("ʻ","");
      }
      
      
      if(next.contains("·")){
    	  next = next.replace("·","");
      }
      
      if(next.contains("|")){
    	  next = next.replace("|","");
      }
      
      if(next.contains("¨")){
    	  next = next.replace("¨","");
      }
      
      if(next.contains("~")){
    	  next = next.replace("~","");
      }
      
      if(next.contains("!")){
    	  next = next.replace("!","");
      }
      
      if(next.contains(";")){
    	  next = next.replace(";","");
      }
      
      if(next.contains("_")){
    	  next = next.replace("_","");
      }
      
      if(next.contains("?")){
    	  next = next.replace("?","");
      }
      
      if(next.contains("…")){
    	  next = next.replace("…","");
      }
      
      if(next.contains("*")){
    	  next = next.replace("*","");
      }
      if(next.contains("**")){
    	  next = next.replace("**","");
      }
      if(next.contains("***")){
    	  next = next.replace("***","");
      }
      
      if(next.contains("+")){
    	  next = next.replace("+","");
      }
      
      if(next.contains("-")){
    	  next = next.replace("-","");
      }
      
      if(next.contains("`")){
    	  next = next.replace("`","");
      }

      
      
      next = next.toLowerCase();
      
      return next.length() > 0 ? next : 
       hasNextToken() ? nextToken() : null;
   }
   /**
    * nextLineToken is method for printing documents, it prints sentences {@value line} the end with '.'
    * @return next
    */
   public String nextLineToken() {
      if (!hasNextToken())
         return null;
      
      String line = mReader.next();
      if(line.contains(".")){
    	  line = line + "\n";
      }
      return line.length() > 0 ? line : 
          hasNextToken() ? nextToken() : null;
   }
}