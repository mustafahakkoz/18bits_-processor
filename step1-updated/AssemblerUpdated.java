/*
150117509 - Mustafa Abdullah Hakkoz
150117052 - Berk Yıldız
150118508 - Çağrı Hodoğlugil
150115841 - Erman Kundakçıoğlu
*/
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class AssemblerUpdated {

    public static void main(String[] args) {

        //Try to read input file and save to arraylist
        List<String> inputList = new ArrayList<>();
        try {
            Scanner sc = new Scanner(AssemblerUpdated.class.getResourceAsStream("input.txt"));
            while (sc.hasNextLine())
                inputList.add(sc.nextLine());
        }
        catch (NullPointerException e) {
            System.err.println("Error - File is not found.");
        }
        catch (NoSuchElementException e) {
            System.err.println("Error - There's no lines in the file.");
        }

        //write hexadecimal results to output file
        try {
            FileWriter fileWriter = new FileWriter("output.txt");
            //get lines from inputList and clean them
            for (int i = 0; i < inputList.size(); i++) {
                String[] cleanStr = inputList.get(i).toUpperCase().split("[\\s,]+");
                //convert the string to binary then to decimal
                String binString = strToBinary(cleanStr);
                int decString = Integer.parseInt(binString, 2);
                //Now convert decimal to hexadecimal
                String hexString = Integer.toString(decString, 16).toUpperCase();
                //extend hexString to 5 digit
                int len=hexString.length();
                if(len<5)
                    for (int j = 0; j < 5-len; j++)
                        hexString = "0"+hexString;
                System.out.printf("%d. line of input: \"%s\", binary form: \"%s\", hex form (output): \"%s\"\n", i+1, inputList.get(i),binString,hexString);
                //Write the result in a txt file
                fileWriter.write(hexString+"\n");
            }
            fileWriter.close();
        }
        catch (IOException e) {
            System.err.println("Error - File can't be created.");
        }
        catch (NumberFormatException e) {
            System.err.println("Error - Invalid arguments in instructions.");
        }

    }

    //convert instructions to binary code
    public static String strToBinary(String[] str){
        String binaryStr;

        switch (str[0]) {
            case "ADD":
            case "AND":
            case "OR":
            case "XOR":
                binaryStr = arithmeticOps(str);
                break;
            case "ADDI":
            case "ANDI":
            case "ORI":
            case "XORI":
                binaryStr = arithmeticOps_Imm(str);
                break;
            case "JUMP":
                binaryStr = jump(str);
                break;
            case "LD":
            case "ST":
                binaryStr = load_store(str);
                break;
            case "BEQ":
            case "BLT":
            case "BGT":
            case "BLE":
            case "BGE":
               binaryStr = branchOps(str);
                break;
            default:
                throw new IllegalArgumentException("Error - Invalid Instruction");
        }
        return binaryStr;
    }



    //////////////////
    //ARITHMETIC OPS//
    //////////////////
    public static String arithmeticOps(String[] str){
        String opCode = "";
        String result;

        switch (str[0]){
            case "ADD":
                opCode = "000";
                break;
            case "AND":
                opCode = "001";
                break;
            case "OR":
                opCode = "010";
                break;
            case "XOR":
                opCode = "011";
                break;
            default:
        }

        //construct binary result
        String dest = produceRegister(str[1]);
        String src1 = produceRegister(str[2]);
        String src2 = produceRegister(str[3]);
        result = opCode + dest + src1 + "0" + src2 + "00";
        return result;
    }

    //make valid register codes
    public static String produceRegister(String str){
        String validRegister = removeLetter_R(str);   //remove letter R
        validRegister = decToBin(validRegister);    //convert dec to bin
        validRegister = extend(validRegister,4);  //extend to 4 bits
        return validRegister;
    }

    //remove letter "R" to convert register name to a relevant number
    public static String removeLetter_R(String str) {
        String number = str.replaceAll("R", "");
        return number;
    }

    //converts decimal to binary
    public static String decToBin(String str) {
        int integer = Integer.parseInt(str);
        String binary = Integer.toBinaryString(integer);
        return binary;
    }

    //extends given binary string to a limit
    public static String extend(String str,int limit) {
        int len = str.length();
        if (len < limit)
            for (int i = 0; i < limit-len; i++)
                str = "0"+str;
        else if (limit < len) {          //if len > limit (overflow),
            str = str.substring(len-limit,len);
        }
        return str;
    }




    ////////////////////////////
    //IMMEDIATE ARITHMETIC OPS//
    ////////////////////////////
    public static String arithmeticOps_Imm(String[] str){
        String opCode = "";
        String result;

        switch (str[0]){
            case "ADDI":
                opCode = "000";
                break;
            case "ANDI":
                opCode = "001";
                break;
            case "ORI":
                opCode = "010";
                break;
            case "XORI":
                opCode = "011";
                break;
            default:
        }

        //construct binary result
        String dest = produceRegister(str[1]);
        String src1 = produceRegister(str[2]);
        String imm = produceImmediate(str[3]);
        result = opCode + dest + src1 + "1" + imm;
        return result;
    }

    //make valid immediate codes
    public static String produceImmediate(String str){
        String validImmediate, validImmediate2;
        String validImmediate_bin="", extended;

        if (str.contains("#") || str.contains("H")){
            if (str.contains("#") && !str.contains("H")){           //i.e. ADDI, R01, #12
                validImmediate = removeSymbol_Hash(str);            //remove symbol "#" then convert dec to bin
                validImmediate_bin = decToBin(validImmediate);
            }
            if(str.contains("H") && !str.contains("#")){             //i.e. ADDI, R01, 12H
                validImmediate = removeLetter_H(str);                //remove letter "H" then convert hex to bin
                validImmediate_bin = hexToBin(validImmediate);
            }
            if (str.contains("#") && str.contains("H")){            //i.e. ADDI, R01, #12H
                validImmediate = removeSymbol_Hash(str);            //remove symbols "#" and "H" then convert hex to bin
                validImmediate2 = removeLetter_H(validImmediate);
                validImmediate_bin = decToBin(validImmediate2);
            }
        }
        else{
            validImmediate_bin = decToBin(str);             //if string includes neither "H" nor "#"
        }
        extended=extend(validImmediate_bin,6);
        return extended;      //extend to 6 bits
    }

    //remove symbol "#" to convert register name to a relevant number
    public static String removeSymbol_Hash(String str) {
        String number = str.replaceAll("#", "");
        return number;
    }

    //remove letter "H" to convert register name to a relevant number
    public static String removeLetter_H(String str) {
        String number = str.replaceAll("H", "");
        return number;
    }

    //converts hexadecimal to binary
    public static String hexToBin(String str) {
        int integer = Integer.parseInt(str, 16);
        String binary = Integer.toBinaryString(integer);
        return binary;
    }


        //////////////////
    //JUMP OPERATION//
    //////////////////
    public static String jump(String[] str) {
        String opCode = "100";
        String result;

        String address = produceAddress(str[1],15);

        result = opCode + address;
        return result;
    }

    //make valid immediate codes
    public static String produceAddress(String str, int limit){
        String validAddress,validAddress2;
        String validAddress_bin="";

        if (str.contains("#") || str.contains("H")){
            if (str.contains("#") && !str.contains("H")){           //i.e. LD #12
                validAddress = removeSymbol_Hash(str);            //remove symbol "#" then convert dec to bin
                validAddress_bin = decToBin(validAddress);
            }
            if(str.contains("H") && !str.contains("#")){             //i.e. LD 12H
                validAddress = removeLetter_H(str);                //remove letter "H" then convert hex to bin
                validAddress_bin = hexToBin(validAddress);
            }
            if (str.contains("#") && str.contains("H")){            //i.e. LD #12H
                validAddress = removeSymbol_Hash(str);            //remove symbols "#" and "H" then convert hex to bin
                validAddress2 = removeLetter_H(validAddress);
                validAddress_bin = decToBin(validAddress2);
            }
        }

        else
            validAddress_bin = decToBin(str);             //if string doesn't include letter "H"

        return extend(validAddress_bin,limit);         //extend to 15 bits;
    }




    ////////////////////
    //LOAD - STORE OPS//
    ////////////////////
    public static String load_store(String[] str) {
        String opCode = "";
        String result;

        switch (str[0]) {
            case "LD":
                opCode = "101";
                break;
            case "ST":
                opCode = "110";
                break;
            default:
        }

        //construct binary result
        String dest_or_src = produceRegister(str[1]);
        String address = produceAddress(str[2],11);

        result = opCode + dest_or_src + address;
        return result;
    }




    ///////////////
    //BRANCH OPS//
    //////////////
    public static String branchOps(String[] exp) {

        String opCode="";
        String result;
        String n="0";
        String z="0";
        String p="0";

        switch (exp[0]) {
            case "BEQ":
                opCode = "111";
                n="0";
                z="1";
                p="0";
                break;
            case "BLT":
                opCode = "111";
                n="1";
                z="0";
                p="0";
                break;
            case "BGT":
                opCode = "111";
                n="0";
                z="0";
                p="1";
                break;
            case "BLE":
                opCode = "111";
                n="1";
                z="1";
                p="0";
                break;
            case "BGE":
                opCode = "111";
                n="0";
                z="1";
                p="1";
                break;
            default:
        }

        String op1 = produceRegister(exp[1]);
        String op2 = produceRegister(exp[2]);
        String address = produceAddress(exp[3],4);

        result = opCode + n + z + p + op1 + op2 + address;
        return result;
    }
}

