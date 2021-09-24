
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class mycpuassemble {

	public static void main(String[] args) throws FileNotFoundException {
		
		String input = "prog.asm";
		String output = "prog.bin";
		
		ArrayList<String> data = readFile(input);	//read input file line by line				
		ArrayList<ArrayList<String>> newData = cleanData(data);		//separating each line and put them into arraylist
		ArrayList<ArrayList<String>> label = new ArrayList<ArrayList<String>>();
		int labelCount =0;
		
		
		//separating labels in the input 
		for (int i = 0; i < newData.size(); i++) {
			if(newData.get(i).size() == 1) {
				if(newData.get(i).get(0).contains(":")) {	//if ":" exists, it is a label
					String[][] Label = new String[10][2];
						
			        String[] tempString = newData.get(i).get(0).split(":");
			      	Label[labelCount][0] = tempString[0];
					Label[labelCount][1]= (Integer.toString(i+1));
						
					//putting them into tempList and deleting from newData
				    ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(Label[labelCount++]));
					label.add(tempList);
					newData.remove(i);
				}
			}
		}	
		String[] binInstructions = convertToHex(newData, label); 
		writeFile(binInstructions, output);
		System.out.println("DONE");
	}
	
	public static ArrayList<String> readFile(String input) {
		//reading file and creating arraylist of each line
		ArrayList<String> data = new ArrayList<>();
		
		try {
			File file = new File(input);
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				data.add(scanner.nextLine());
			}
			scanner.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.File not found.");
			e.printStackTrace();
		}
		
		return data;
	}
	
	public static void writeFile(String[] binInstructions, String output) {
		//writing the data into the output file in binary format
		try {
            FileWriter writer = new FileWriter(output);
         
            for (int i = 0; i < binInstructions.length-1; i++) {
            	
            	if(binInstructions[i] == null)
            		continue;
            	writer.append(binInstructions[i] + "\n");
			}
            writer.append(binInstructions[binInstructions.length-1]);
            writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String[] convertToHex(ArrayList<ArrayList<String>> newData,ArrayList<ArrayList<String>> label) {
		//declaration for instruction, addressing mode and operand bits
		String[] binInst = new String[newData.size()];
		String[] opMode= new String[newData.size()];
		String[] outputOperand = new String[newData.size()];
		
		//if label exists, convert to integer and multiply by 3 adding a tag in the end
		for(int i =0; i<newData.size(); i++) {
			for(int j=0 ; j<label.size(); j++) {
				if(newData.get(i).contains(label.get(j).get(0))) {
					newData.get(i).set(1, Integer.toString(3*Integer.parseInt(label.get(j).get(1))));
					newData.get(i).add("label");
				}
			}
		}
		
		for (int i = 0; i < newData.size(); i++) {
			//for each line, declaration of instruction, addressing mode and operand bits
			StringBuilder operandCreater = new StringBuilder(16);
			StringBuilder instructionCreater = new StringBuilder(6);
			StringBuilder admCreater  = new StringBuilder(2);
			
			if(newData.get(i).size()==0)
				continue;
			String instruction = newData.get(i).get(0);
			
			boolean haltCheck= instruction.equals("HALT");	
			//if instruction is halt, jump this section
			if(haltCheck == false) { 
				String operand = newData.get(i).get(1); //
				// if "'" is included, add the ascii code of the input and add to operand
				if(operand.contains("'")) {
					admCreater.insert(0, "00");
					String[] temp1 = operand.split("'");
					char[] Ascii = temp1[1].toCharArray();
					
					//to convert ascii into binary 
					for(int ji= 0 ; ji<Ascii.length; ji++) {
						
						String qq = Integer.toBinaryString(Ascii[ji]);
						operandCreater.insert(0,qq);
						if(qq.length()<8) operandCreater.insert(0, "0");
					}
					
					//if it is not 16 bit, fill it
					if(operandCreater.length()<16) {
						int bitLength=operandCreater.length();
						for (int j = 0; j < 16-bitLength; j++) {
			             operandCreater.insert(0, "0");
						}
					}	
				}
				else if(operand.contains("A") ||operand.contains("B") ||operand.contains("C") ||operand.contains("D") ||operand.contains("E") ||operand.contains("F") ||operand.contains("S")){
					//if addressing mode is 10, memory address given in the register 
					if(operand.contains("[") || operand.contains("]")) {	//separating "[" and "]", and converting to binary string
					admCreater.insert(0, "10");
					String[] temp1 = operand.split("\\[");
					temp1= temp1[1].split("\\]");
					if(temp1[0].equals("PC"))  operandCreater.insert(0, Integer.toBinaryString(0));
					if(temp1[0].equals("A"))  operandCreater.insert(0, Integer.toBinaryString(1));
					if(temp1[0].equals("B"))  operandCreater.insert(0, Integer.toBinaryString(2));
					if(temp1[0].equals("C"))  operandCreater.insert(0, Integer.toBinaryString(3));
					if(temp1[0].equals("D"))  operandCreater.insert(0, Integer.toBinaryString(4));   							
					if(temp1[0].equals("E"))   operandCreater.insert(0, Integer.toBinaryString(5));
					if(temp1[0].equals("S"))   operandCreater.insert(0, Integer.toBinaryString(6));
				}else{
					//if addressing mode is 01, memory address given in the register
					admCreater.insert(0, "01");
					//to convert to binary string 
					if(operand.equals("PC"))  operandCreater.insert(0, Integer.toBinaryString(0));
					if(operand.equals("A"))  operandCreater.insert(0, Integer.toBinaryString(1));
					if(operand.equals("B"))  operandCreater.insert(0, Integer.toBinaryString(2));
					if(operand.equals("C"))  operandCreater.insert(0, Integer.toBinaryString(3));
					if(operand.equals("D"))  operandCreater.insert(0, Integer.toBinaryString(4));   							
					if(operand.equals("E"))   operandCreater.insert(0, Integer.toBinaryString(5));
					if(operand.equals("S"))   operandCreater.insert(0, Integer.toBinaryString(6));
					
				}	
			}else if(operand.contains("0") || operand.contains("1") || operand.contains("2") || operand.contains("3") || operand.contains("4") || operand.contains("5") || operand.contains("6") 
					|| operand.contains("6")|| operand.contains("7")|| operand.contains("8")|| operand.contains("9")) {
					//if addressing mode is 11, operand is a memory address
				if(operand.contains("[") || operand.contains("]")) {
					admCreater.insert(0, "11");
					String[] temp1 = operand.split("\\[");
					temp1= temp1[1].split("\\]");
					operand=temp1[0];
						
				}else if(newData.get(i).contains("label")) {	//if label exists, addressing mode is 00 
					admCreater.insert(0, "00");
				}else admCreater.insert(0, "00");
				
				int value = Integer.parseInt(operand);	//convert to binary
				operandCreater.insert(0, Integer.toBinaryString(value));
				}
			}else {
				operandCreater.insert(0, "0000000000000000"); admCreater.insert(0, "00");
			}
			//instructions and instruction codes
			if (instruction.equals("HALT"))  instructionCreater.insert(0, "000001"); 
	        if (instruction.equals("LOAD"))  instructionCreater.insert(0, "000010"); 
	        if (instruction.equals("STORE")) instructionCreater.insert(0, "000011"); 
	        if (instruction.equals("ADD"))   instructionCreater.insert(0, "000100"); 
	        if (instruction.equals("SUB"))   instructionCreater.insert(0, "000101"); 
	        if (instruction.equals("INC"))   instructionCreater.insert(0, "000110"); 
	        if (instruction.equals("DEC"))   instructionCreater.insert(0, "000111"); 
	        if (instruction.equals("MUL"))   instructionCreater.insert(0, "001000"); 
	        if (instruction.equals("DIV"))   instructionCreater.insert(0, "001001"); 
	        if (instruction.equals("XOR"))   instructionCreater.insert(0, "001010"); 
	        if (instruction.equals("AND"))   instructionCreater.insert(0, "001011");
	        if (instruction.equals("OR"))    instructionCreater.insert(0, "001100"); 
	        if (instruction.equals("NOT"))   instructionCreater.insert(0, "001101"); 
	        if (instruction.equals("SHL"))   instructionCreater.insert(0, "001110"); 
	        if (instruction.equals("SHR"))   instructionCreater.insert(0, "001111"); 
	        if (instruction.equals("NOP"))   instructionCreater.insert(0, "010000"); 
	        if (instruction.equals("PUSH"))  instructionCreater.insert(0, "010001"); 
	        if (instruction.equals("POP"))   instructionCreater.insert(0, "010010"); 
	        if (instruction.equals("CMP"))   instructionCreater.insert(0, "010011"); 
	        if (instruction.equals("JMP"))   instructionCreater.insert(0, "010100"); 
	        if (instruction.equals("JZ") || instruction.equals("JE"))   instructionCreater.insert(0, "010101"); 
	        if (instruction.equals("JNZ") || instruction.equals("JNE")) instructionCreater.insert(0, "010110"); 
	        if (instruction.equals("JC"))    instructionCreater.insert(0, "010111"); 
	        if (instruction.equals("JNC"))   instructionCreater.insert(0, "011000"); 
	        if (instruction.equals("JA"))    instructionCreater.insert(0, "011001"); 
	        if (instruction.equals("JAE"))   instructionCreater.insert(0, "100000"); 
	        if (instruction.equals("JB"))    instructionCreater.insert(0, "100001"); 
	        if (instruction.equals("JBE"))   instructionCreater.insert(0, "100010"); 
	        if (instruction.equals("READ"))  instructionCreater.insert(0, "100011"); 
	        if (instruction.equals("PRINT")) instructionCreater.insert(0, "100100"); 
	        
	        binInst[i]= instructionCreater.toString();
			
	        opMode[i]= admCreater.toString();
		
			if(operandCreater.length()<16) {
				int bitLength=operandCreater.length();
				for (int j = 0; j < 16-bitLength; j++) {
	             operandCreater.insert(0, "0");
				}
			}
			outputOperand[i]= operandCreater.toString();	
		
			binInst[i]= binInst[i] + opMode[i] + outputOperand[i];	//adjoining opcode, addressing mode and operation strings
			System.out.println(binInst[i]);
		}		
		return binInst;
	}
	
	public static ArrayList<ArrayList<String>> cleanData(ArrayList<String> data) {
		ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>(); 
		
		for (int i = 0; i < data.size(); i++) {
			String parts[] = data.get(i).split("\\//");
            data.set(i, parts[0]); 	//taking the part without the comment
            
            String parts2[] = data.get(i).split("\\s+");	//separating spaces and put them into an arraylist
            ArrayList<String> list2 = new ArrayList<String>(Arrays.asList(parts2));
            newData.add(list2);
            
            for (int j = 0; j < newData.get(i).size(); j++) {
            	newData.get(i).set(j, newData.get(i).get(j).replaceAll("\\s+", ""));
            	
            	if(newData.get(i).get(j).equals(""))	//to remove empty part in arraylist if it exits
            		newData.get(i).remove(j);
            	if(newData.get(i).get(0).equals(""))
            		newData.get(i).remove(0);
			}     	
		}return newData;
	}


}
