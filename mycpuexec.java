
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class mycpuexec {

	public static void main(String[] args) {

		String input = "prog.bin";
		ArrayList<String> data = readFile(input);
		ArrayList<ArrayList<String>> memory = instructionSet(data);	  //Creating instruction set from prog.bin
		ArrayList<ArrayList<String>> myCPU = createCPU();	//Creating arraylist of CPU
		output(memory, myCPU);
	}
	
	public static void output(ArrayList<ArrayList<String>> memory, ArrayList<ArrayList<String>> myCPU) {
		
		for(int i=0; i<100000; i++) {
			ArrayList<String> currentInst = getInstructions(memory, (myCPU.get(0))); //Getting 24 bit instruction which PC shows
			StringBuilder instruction = new StringBuilder(6);
			StringBuilder addMode = new StringBuilder(2);
			StringBuilder operandBuild = new StringBuilder(16);
			
			int operand = Integer.parseInt(currentInst.get(2),2);
			instruction.insert(0, currentInst.get(0));	//Separating instruction, addressing mode and operand
			addMode.insert(0, currentInst.get(1));
			
			if(addMode.toString().equals("00")) {	//Operand is in immediate data
				operandBuild.insert(0, currentInst.get(2)); 
			}else if (addMode.toString().equals("01")) {	//Operand is in given in the register
				operandBuild.insert(0, myCPU.get(operand).get(0));
			}else if (addMode.toString().equals("10")) {	//Operand's memory address is given in the register
				operandBuild.insert(0, memory.get(Integer.parseInt(myCPU.get(operand).get(0),2)).get(0));
			}else if (addMode.toString().equals("11")) {	//Operand is a memory address
				operandBuild.insert(0, memory.get(operand).get(0));
			}
			
			if(operandBuild.length()<16) {	//if it is not 16 bit, fill it
				int bitLength=operandBuild.length();
				for (int j = 0; j < 16-bitLength; j++) {
					operandBuild.insert(0, "0");
				}
			}
			
			if(instruction.toString().equals("000001")) {	//HALT
				
				myCPU.get(0).set(0,(Integer.toBinaryString((Integer.parseInt(myCPU.get(0).get(0), 2)-1))));
				break;//break to get out of the for loop	
				
			}else if(instruction.toString().equals("000010")) {	//LOAD
				
				myCPU.get(1).set(0, operandBuild.toString());	
				
			}else if(instruction.toString().equals("000011")) {	//STORE
				
				if(addMode.toString().equals("01")) {
					//Storing to operand register
					myCPU.get(operand).set(0,myCPU.get(1).get(0));
				}else if (addMode.toString().equals("10")) {
					//Storing register's content to memory
					memory.get(Integer.parseInt(myCPU.get(operand).get(0),2)).set(0, myCPU.get(1).get(0)); 	
				}
			
			}else if(instruction.toString().equals("000100")) {	//ADD
				
				//Summing register A and operand register and put the result into register A
				myCPU.get(1).set(0, Integer.toBinaryString(Integer.parseInt(operandBuild.toString(),2)+Integer.parseInt(myCPU.get(1).get(0),2)));
	
				if(myCPU.get(1).get(0).length()>24) { 	//Check for CF
					myCPU.get(7).set(1, Integer.toBinaryString(1));
					//Getting rid of the carry 
					myCPU.get(1).set(0, myCPU.get(1).get(0).substring((myCPU.get(1).get(0).length()-24), myCPU.get(1).get(0).length())); 
					
				}else {myCPU.get(7).set(1, Integer.toBinaryString(0));}
				
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) { 	//Check for zero flag -ZF-
					myCPU.get(7).set(0, "1"); 	//if the result is 0, zero flag -ZF- is 1
				}else {myCPU.get(7).set(0, "1");} 	//if the result is 0, zero flag -ZF- is 1
				
			}else if(instruction.toString().equals("000101")) {	//SUB
				
				myCPU.get(1).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(1).get(0),2)-Integer.parseInt(operandBuild.toString(),2)));
				//A-(operand register) and result to A
				if(myCPU.get(1).get(0).length()>24) {
					myCPU.get(7).set(1, Integer.toBinaryString(1));
					myCPU.get(1).set(0, myCPU.get(1).get(0).substring((myCPU.get(1).get(0).length()-24), myCPU.get(1).get(0).length()));
				}else {myCPU.get(7).set(1, Integer.toBinaryString(0));}
				
				if((Integer.parseInt(operandBuild.toString(),2)-Integer.parseInt(myCPU.get(1).get(0),2))==0) { 
					//Zero flag -ZF-
					myCPU.get(7).set(0, Integer.toBinaryString(1));
				}else {
					myCPU.get(7).set(0, Integer.toBinaryString(0));
				}
				
			}else if(instruction.toString().equals("000110")) {	//INC

				myCPU.get(operand).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(operand).get(0),2)+1));
						
				if(myCPU.get(operand).get(0).length()>24) {
					myCPU.get(7).set(1, Integer.toBinaryString(1));
					myCPU.get(1).set(0, myCPU.get(operand).get(0).substring((myCPU.get(operand).get(0).length()-24), myCPU.get(operand).get(0).length()));
				}else {myCPU.get(7).set(1, Integer.toBinaryString(0));}
				
				if(Integer.parseInt(myCPU.get(operand).get(0)) == 0) {
					myCPU.get(7).set(0, "1"); 	//if the result is 0, zero flag -ZF- is 1
				}else{myCPU.get(7).set(0, "0");}	 //else zero flag -ZF- is 0
				
			}else if(instruction.toString().equals("000111")) {	//DEC
				
				if(Integer.parseInt(myCPU.get(operand).get(0)) != 1) {
				myCPU.get(operand).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(operand).get(0),2)-1));
				myCPU.get(7).set(0, Integer.toBinaryString(0)); 	//Zero flag -ZF- is 0
				myCPU.get(7).set(1, Integer.toBinaryString(0));
				myCPU.get(7).set(2, Integer.toBinaryString(0)); //updating sign flag -SF- to be 0
				}else if(Integer.parseInt(myCPU.get(operand).get(0)) == 1) {
					myCPU.get(7).set(0, Integer.toBinaryString(1));
					myCPU.get(7).set(1, Integer.toBinaryString(0));
					myCPU.get(7).set(2, Integer.toBinaryString(0)); //updating sign flag -SF- to be 0
					myCPU.get(operand).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(Integer.parseInt(operandBuild.toString(),2)).get(0),2)-1));
				}
				else if(Integer.parseInt(myCPU.get(operand).get(0),2) == 0) {
					myCPU.get(operand).set(0, "0");
					myCPU.get(7).set(0, Integer.toBinaryString(0)); 	//Zero flag -ZF- is 0
					myCPU.get(7).set(1, Integer.toBinaryString(0));
					myCPU.get(7).set(2, Integer.toBinaryString(1)); 	//updating sign flag -SF- to be 1
				}
				if(myCPU.get(operand).get(0).length()>24) {
					myCPU.get(7).set(1, Integer.toBinaryString(1));
					myCPU.get(1).set(0, myCPU.get(operand).get(0).substring((myCPU.get(operand).get(0).length()-24), myCPU.get(operand).get(0).length()));
				}else {myCPU.get(7).set(1, Integer.toBinaryString(0));}		
				
			}else if(instruction.toString().equals("001000")) {	//MUL
				
				//Multiplication of operand register and A
				myCPU.get(1).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(1).get(0),2 )*(Integer.parseInt(operandBuild.toString(),2))));
				
				if(myCPU.get(1).get(0).length()>24) {
					myCPU.get(7).set(1, Integer.toBinaryString(1));
					myCPU.get(1).set(0, myCPU.get(1).get(0).substring((myCPU.get(1).get(0).length()-24), myCPU.get(1).get(0).length()));
				}else {myCPU.get(7).set(1, Integer.toBinaryString(0));}
				//Checking if the result is zero to update zero flag -ZF-
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) { 
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
				
			}else if(instruction.toString().equals("001001")) {	//DIV
				
				//Calculating the remainder and putting on D
				myCPU.get(4).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(1).get(0),2)%(Integer.parseInt(operandBuild.toString(),2))));
				//Calculating the quotient
				myCPU.get(1).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(1).get(0),2)/(Integer.parseInt(operandBuild.toString(),2))));
				
				if(myCPU.get(1).get(0).length()>24) { //Check for carry flag -CF-
					myCPU.get(7).set(1, Integer.toBinaryString(1));
					myCPU.get(1).set(0, myCPU.get(1).get(0).substring((myCPU.get(1).get(0).length()-24), myCPU.get(1).get(0).length()));
				}else {myCPU.get(7).set(1, Integer.toBinaryString(0));}
				
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) { //Check for zero flag -ZF-
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
				
			}else if(instruction.toString().equals("001010")) {	//XOR
				
				myCPU.get(1).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(1).get(0))^ Integer.parseInt(operandBuild.toString(),2)));
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) {
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
				
			}else if(instruction.toString().equals("001011")) {	//AND

				myCPU.get(1).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(1).get(0)) & Integer.parseInt(operandBuild.toString(),2)));
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) {
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
				
			}else if(instruction.toString().equals("001100")) {	//OR
				
				myCPU.get(1).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(1).get(0))| Integer.parseInt(operandBuild.toString(),2)));
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) {
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
				
			}else if(instruction.toString().equals("001101")) {	//NOT
								
				if(addMode.toString().equals("00")) {	//if immediate
					myCPU.get(1).set(0, Integer.toBinaryString(~Integer.parseInt(operandBuild.toString(),2)));
					if(myCPU.get(1).get(0).length()>24)  //Keeping only 24 bits
						myCPU.get(1).set(0, myCPU.get(1).get(0).substring((myCPU.get(1).get(0).length()-24), myCPU.get(1).get(0).length()));
							
				}else if(addMode.toString().equals("01")) {		//if register
					myCPU.get(operand).set(0, Integer.toBinaryString(~Integer.parseInt(operandBuild.toString(),2)));
					if(myCPU.get(operand).get(0).length()>24) { //Keeping only 24 bits
						myCPU.get(operand).set(0, myCPU.get(operand).get(0).substring((myCPU.get(operand).get(0).length()-24), myCPU.get(operand).get(0).length()));
					}
				}else if(addMode.toString().equals("10")) {		//if memory
					memory.get(operand).set(0, Integer.toBinaryString(~Integer.parseInt(memory.get(operand).get(0),2)));
					if(memory.get(operand).get(0).length()>24) { //Keeping only 24 bits
						memory.get(operand).set(0, memory.get(operand).get(0).substring((memory.get(operand).get(0).length()-24), memory.get(operand).get(0).length()));
					}
				}
					
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) {
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
					
			}else if(instruction.toString().equals("001110")) {	//SHL
				
				myCPU.get(operand).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(operand).get(0),2)*2));
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) {
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
				
			}else if(instruction.toString().equals("001111")) {	//SHR

				myCPU.get(operand).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(operand).get(0),2)/2));
				if(Integer.parseInt(myCPU.get(1).get(0),2) == 0) {
					myCPU.get(7).set(0, "1");
				}else{myCPU.get(7).set(0, "0");}
				
			}else if(instruction.toString().equals("010000")) {	//NOP
					
			}else if(instruction.toString().equals("010001")) {	//PUSH
				
				//Writing register data to memory
				memory.get(Integer.parseInt(myCPU.get(6).get(0),2)).set(0, operandBuild.toString()); 
				//Decrementing stack pointer to show a new point
				myCPU.get(6).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(6).get(0),2)-1) );
				
			}else if(instruction.toString().equals("010010")) {	//POP
				
				//Writing memory data to register
				myCPU.get(operand).set(0, memory.get( Integer.parseInt(myCPU.get(6).get(0),2)+1 ).get(0));
				//incrementing stack pointer
				myCPU.get(6).set(0, Integer.toBinaryString(Integer.parseInt(myCPU.get(7).get(0),2)+1) );
			
			}else if(instruction.toString().equals("010011")) {	//CMP
				
				int accValue = Integer.parseInt(myCPU.get(1).get(0),2);
				int comparedValue = Integer.parseInt(operandBuild.toString(),2);
				
				if(accValue>comparedValue) {
					myCPU.get(7).set(0, Integer.toBinaryString(0)); //ZF
					myCPU.get(7).set(1, Integer.toBinaryString(1)); //CF
					myCPU.get(7).set(2, Integer.toBinaryString(0)); //SF
				}else if (accValue == comparedValue) {
					myCPU.get(7).set(0, Integer.toBinaryString(1));
					myCPU.get(7).set(1, Integer.toBinaryString(0));
					myCPU.get(7).set(2, Integer.toBinaryString(0));
				}else if (accValue < comparedValue) {
					myCPU.get(7).set(0, Integer.toBinaryString(0));
					myCPU.get(7).set(1, Integer.toBinaryString(0));
					myCPU.get(7).set(2, Integer.toBinaryString(1));
				}
				
			}else if(instruction.toString().equals("010100")) {	//JMP
				
				myCPU.get(0).set(0, Integer.toBinaryString(operand/3-2));
				
			}else if(instruction.toString().equals("010101")) {	//JZ JZE
				
				if(Integer.parseInt(myCPU.get(7).get(0),2) ==1) //if zero flag -ZF- is 1, branch
					myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
				
			}else if(instruction.toString().equals("010110")) {	// JNZ JNE

				if(Integer.parseInt(myCPU.get(7).get(0),2) ==0) //if zero flag -ZF- is 0, branch
				myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
				
			}else if(instruction.toString().equals("010111")) {	//JC 
				
				if(Integer.parseInt(myCPU.get(7).get(1),2) == 1)//if carry flag -CF- is 1, branch
					myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
				
			}else if(instruction.toString().equals("011000")) {	//JNC
				
				if(Integer.parseInt(myCPU.get(7).get(1),2) == 0) //if carry flag -CF- is 0, branch
				myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
			
			}else if(instruction.toString().equals("011001")) {	//JA
				 
				if(Integer.parseInt(myCPU.get(7).get(1),2) == 0) //if carry flag -CF- is 0, branch
					myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
				
			}else if(instruction.toString().equals("100000")) {	//JAE
				
				if(((Integer.parseInt(myCPU.get(7).get(1),2)) == 0)| (Integer.parseInt(myCPU.get(7).get(0),2)==1))
					myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
				
			}else if(instruction.toString().equals("100001")) {	//JB 
				
				if(Integer.parseInt(myCPU.get(7).get(2),2)== 1)
					myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
					
			}else if(instruction.toString().equals("100010")) {	//JBE
				 
				if(Integer.parseInt(myCPU.get(7).get(2),2)== 1 | Integer.parseInt(myCPU.get(7).get(0),2)==1)
					myCPU.get(0).set(0,(Integer.toBinaryString(operand/3-2)));
				
			}else if(instruction.toString().equals("100011")) {	//READ
				//Reading character, converting it into ASCII then storing in the operandBuilder
				Scanner sc= new Scanner(System.in);    
				System.out.print("Enter a character ");  
				char c = sc.next().charAt(0);
				if (addMode.toString().equals("10")) {
					 memory.get(Integer.parseInt(operandBuild.toString(),2)).set(0, Integer.toBinaryString(c));
					 System.out.println(memory.get(Integer.parseInt(operandBuild.toString(),2)).get(0) +"MEM READ "+ Integer.parseInt(operandBuild.toString(),2));
				 }else if (addMode.toString().equals("01")) {

					 myCPU.get(Integer.parseInt(operandBuild.toString(),2)).set(0,Integer.toBinaryString(c));
					 System.out.println(myCPU.get(Integer.parseInt(operandBuild.toString(),2)).get(0) + "REG READ");
					 System.out.println(myCPU);
				}
				
			}else if(instruction.toString().equals("100100")) {	//PRINT
				int secASCII = Integer.parseInt(myCPU.get(operand).get(0),2);	//get the ASCII value of the input
				char d= (char) secASCII;	//char casting the ascii value
				System.out.println(d);
			}
			//incrementing PC by 1 after the operations
			myCPU.get(0).set(0,(Integer.toBinaryString((Integer.parseInt(myCPU.get(0).get(0), 2)+1)))); 
		}	
	}

	public static ArrayList<String> readFile(String input) {
		//reading the file and putting the lines input into arraylist
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
	
	public static ArrayList<ArrayList<String>> instructionSet(ArrayList<String> data) {
		//creating arraylist of arraylist to parse instructions into 3 which are instruction, addressing mode, operand
		ArrayList<ArrayList<String>> instructions = new ArrayList<ArrayList<String>>();
		String[] temp = new String[3];
		for(int i = 0; i<data.size(); i++) {
			//parsing the instruction
			temp[0] = data.get(i).substring(0, 6);	//instruction
			temp[1] = data.get(i).substring(6, 8);	//addressing mode
		  	temp[2] = data.get(i).substring(8);	  //operand
		  	ArrayList<String> list2 = new ArrayList<String>(Arrays.asList(temp));	//converting string array into arraylist
		     instructions.add(list2);
		}
		for(int i=0; i<100-data.size();i++) {	//to use memory adding more lines
			String test= "0000000000000000";
			ArrayList<String> list2 = new ArrayList<String>(Arrays.asList(test));
		    instructions.add(list2);
		}
		return instructions;
	}	
	
	public static ArrayList<ArrayList<String>> createCPU() {
		//creating the 8 registered CPU
		ArrayList<ArrayList<String>> CPU = new ArrayList<ArrayList<String>>();
		String[] temp1 = new String[3];
		ArrayList<String> data = new ArrayList<>();
		ArrayList<String> data1 = new ArrayList<>();
		ArrayList<String> data2 = new ArrayList<>();
		ArrayList<String> data3 = new ArrayList<>();
		ArrayList<String> data4 = new ArrayList<>();
		ArrayList<String> data5 = new ArrayList<>();
		ArrayList<String> data6 = new ArrayList<>();
		ArrayList<String> data7 = new ArrayList<>();
		ArrayList<String> temp = new ArrayList<>();
				
		temp1[0]="0";	//register that holds flags
		temp1[1]= "0";	
		temp1[2]="0";
		temp.add(temp1[0]);
		temp.add(temp1[1]);
		temp.add(temp1[2]);
		
		data.add("0000000000000000");
		data1.add("0000000000000000");
		data2.add("0000000000000000");
		data3.add("0000000000000000");
		data4.add("0000000000000000");
		data5.add("0000000000000000");
		data6.add("0000000000000000");
		data7.add("0000000000000000");
		CPU.add(data);
		CPU.add(data1);
		CPU.add(data2);
		CPU.add(data3);
		CPU.add(data4);
		CPU.add(data5);
		CPU.add(data7);
		CPU.add(temp);
		return CPU;
	}	

	public static ArrayList<String> getInstructions(ArrayList<ArrayList<String>> memory, ArrayList<String> PC) {
		//selecting the instruction by instruction set and PC 
		return memory.get(Integer.parseInt(PC.get(0), 2));
	}
}
