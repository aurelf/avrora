
package avrora.syntax.objdump;

import java.io.*;		
import java.util.StringTokenizer;
import avrora.util.StringUtil;

/**
 * The <code>ObjDumpPreprocessor</code> class is a utility class that takens the output
 * from the <code>avr-obdjump</code> utility and produces a cleaned up version that is
 * more suitable for parsing into the internal format of Avrora.
 * @author Ben L. Titzer
 * @author Vids Samanta
 */
public class ObjDumpPreprocessor {


    BufferedReader in;
    StringBuffer out;

    /**
     * 
     * @param filename of file with avr-objdump format code
     * @return a ObjDumpPreprocessor object; call getCleanObjDumpCode() on it. 
     */
    public ObjDumpPreprocessor(String inFile){
	try{
	    in = new BufferedReader(new FileReader(inFile));
	    out = new StringBuffer();
	    cleanFile();
	    in.close();

	} catch(Exception e){
	    System.err.println("Error reading file \""+ inFile + "\":\n" +e);
	    System.exit(-1);
	}
    }

    /**
     * @return output StringBuffer of well formated 
     * cleaned up objdump file contents
     */
    public StringBuffer getCleanObjDumpCode(){
	return out;
    }




    private void cleanFile() throws IOException{

	String line = in.readLine();
	
	//clean up first section
	while (line != null){
	    if(line.indexOf("Disassembly of section .text") != -1){
		break;
	    }
	    if (line.indexOf("main.exe") != -1)
		out.append("program \"main.exe\":\n\n");
	    else if (line.indexOf(".text") != -1){
		out.append("  section .text ");
		StringTokenizer st = new StringTokenizer(line);
		st.nextToken(); // 0
		st.nextToken(); //.text
		out.append(" size=0x" + st.nextToken());
		out.append(" vma=0x" + st.nextToken());
		out.append(" lma=0x" + st.nextToken());
		out.append(" offset=0x" + st.nextToken());
		out.append(" ;" + st.nextToken());
		out.append(" \n");
	    }
	    else if(line.indexOf(".data") != -1){
		out.append("  section .data ");
		StringTokenizer st = new StringTokenizer(line);
		st.nextToken(); // 0
		st.nextToken(); //.text
		out.append(" size=0x" + st.nextToken());
		out.append(" vma=0x" + st.nextToken());
		out.append(" lma=0x" + st.nextToken());
		out.append(" offset=0x" + st.nextToken());
		out.append(" ;" + st.nextToken());
		out.append(" \n");
	    }
	    line = in.readLine();
	}
	
	//clean up .text and .data sections
	out.append("start \".text\":\n\n");

	while (line != null){
	    if(line.indexOf("Disassembly of section .data")!=-1)
	       out.append("\nstart \".data\":\n\n");
	    else {
		
		if (isLabel(line)){
		    out.append("\nlabel 0x");
		    StringTokenizer st = new StringTokenizer(line);
		    out.append(st.nextToken());
		    out.append("  " + (st.nextToken()).replaceAll("[<,>]","\"") + "\n");
		} else {
		    
		    String tok;
		    StringTokenizer st = new StringTokenizer(line);
		    if (st.hasMoreTokens()){
			tok = st.nextToken();
			out.append(StringUtil.rightJustify("0x" + tok, 10));
			while (st.hasMoreTokens()) {
			    tok = st.nextToken();
			    
			    if (tok.matches("\\p{XDigit}\\p{XDigit}"))
				out.append(" 0x" + tok);
			else 
			    out.append("  " + tok);
			    
			}
			out.append("\n");
		    }
		}
	    }
	    line = in.readLine();
	}
    }

    /**
     * @param statement
     * @return true if statement is of the form: <hexdig> <\<LABEL\>:>
     */
    private boolean isLabel(String s) {
	if (s.indexOf("<") == -1)
	    return false;
	if (s.indexOf(">:") == -1)
	    return false;
	return true;
    }

    //test main
    public static void main(String a[]){
	ObjDumpPreprocessor p = new ObjDumpPreprocessor(a[0]);
	System.out.println(p.getCleanObjDumpCode());
    }
}
