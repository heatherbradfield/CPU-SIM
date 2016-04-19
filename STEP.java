// This is the SHELL for the STEP class for the CPU SIM project
// STUDENT NAME: Heather Bradfield
// Version: 03/24/16
// STUDENT STATUS COMMENTS: 
// Part1: The program works great! Implemented the ReadMem(), WriteMem(), and FI() methods. 
//        Tested my program amd compared it to the PEP8-Source Code simulation.  
//        Added if(cpu.IS == 0) cpu.DESCR = "STOP" to deal with previous problems with an ArrayIndexOutOfBoundsExeption.
// Part2: Implemented the DI() method. The SIM can now decode the cpu instruction specifier. Ran the program
//        and compared it to the PEP8-Source Code simulation and everything works! 
// Part3: Everything works well. Implemented the CO(), FO(), and WO() methods. When comparing my simulator to the pep8, 
//        all my operands match except for the ldbtyea, lda, stbytea and sta instructions. This is due to the load/store insructions 
//        executing while the ex() and execution methods have not yet been implemented. So, there is no way to check the load/store (operand) yet.
// Part4: Implemented the EX() method and created the shells for all the individual execution methods. Ran the simulation
//        and compared it to the PEP8-Source code and it matches. Also, the print statements in the individual execution methods 
//        print at the appropriate time when its corresponding instruction is "called." Everything is working great!
// Part5: Coded the following instruction procesing methods: LDr, LDBYTEr, STr, STBYTEr, ADDr, SUBr, ANDr, ORr, and CPr. When invoked 
//        by the EX() method, they correctly perform the given operation and set the appropriate status bits. Checking for 
//        condition when SUBtracting is very complex, so I did not attempt it and set bit for both SUBr and CPr.
//        The simulation produces the complete update of the CPU State (Status bits, Registers,Memory), for those instructions that 
//        are implemented.
// Part6: Implemented  the NOTr, NEGr, ASLr, ASRr, ROLr, and RORr methods. All unary instructions set the appropriate status bits and 
//        update the accumulator appropriately. Branch instructions still need to be implemented to compleetely mimick the Pep8 source code.
// Part7: Changed RORr to use the cpu.C (current Carry) value to set the LEFTMOST bit: if(cpu.C == 1) cpu.A = cpu.A | 0x00008000;
//        Implemented all branch instructions and the simulation produces the complete update of the CPU State (Status bits, Registers,
//        Memory), for the complete program. Did the final verification step and in BOTH the PEP8 app and the SIMULATION program,
//        the CPU STATE Elements are identical. 
//----------------------------------------------------------------------------------------------------
public class STEP{
//GLOBAL VARIABLES
   CPU cpu; // this is the object(instance of a class) that represents the 'STATE' of the CPU
/* // these are the CPU class(Object) public attributes(variables)which represent the 'state' of the CPU
byte N; // the NEGATIVE flag
byte Z; // the ZERO flag
byte V; // the OVERFLOW flag
byte C; // the CARRY flag
int A;  // the ACCUMULATOR Register
int X;  // the INDEX Register
int PC; // the PROGRAM COUNTER
int SP; // the STACK POINTER (NOT USED)
int IS; // the INSTRUCTION SPECIFIER (OPCODE)
String cpu.DESCR; // the INSTRUCTION mnemonic
char MODE; // the ADDRESSING MODE
int OS;    // the INSTRUCTION OPERAND SPECIFIER 
int OP;    // the OPERAND DATA
char [] MEMORY;   // the PROGRAM (Machine Instructions)
*/
    //
    boolean CO=true; boolean FO=true; boolean WO=false; // logical vars that control the stage execution
    // 
    boolean PR=true; // logical var that allows for the printing of the CPU fields,to verify accuracy
    //
    boolean Unary=false; // logical var to differentiate between the one/three byte type instructions
    //
    int EA=0;    // this var holds the instruction's operand memory effective address; 
                 // used as the INDEX to to read/write from/to memory array
    int NEA=0;   // this a temp var used for iNdirect address calculating/processing
    char DATA=0; // this var holds the value that is read from or written to memory
    int OP=0;    // this var is used to SWITCH on the opcode in the DI() and EX() methods
    //
// the 'main'>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
  public static void main(String args[])
{ new SIMULATOR(); } // all it does is to instantiate an object from the SIMULATOR class
// end of the 'main' >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//
// this method executes ONE VON NEUMANN CYCLE using/updating the CPU state passed as a parameter
// it is invoked by the SIMULATOR when the user clicks the 'STEP' button
  public void dostep(CPU cpu) { // the VonNeumann loop
           this.cpu = cpu; // the CPU state object
           FI(); // always executed
           DI(); // always executed
    if(CO) CO(); // MAY be skipped
    if(FO) FO(); // MAY be skipped
           EX(); // always executed
    if(WO) WO(); // MAY be skipped
    if(PR) PR();
// end of one pass of the VonNeumann cycle
    return;} // end of dostep method
//
// START OF Auxiliary methods
//
  void PR() { //prints the key CPU fields
      System.out.printf("\nFetched instruction specifier at: 0x%04x, Opcode: 0x%02x", cpu.PC, cpu.IS);
      System.out.printf("\nFetched operand specifier, value: 0x%04x",cpu.OS);
      System.out.printf("\nInstruction Descriptor: %s , Addressing Mode: %c \n",cpu.DESCR,cpu.MODE); 
      //System.out.printf("0x%04x",cpu.OP);
  } // end PR method
//
  void ReadMem() {
    DATA = cpu.MEMORY[EA];  // uses the instruction/operand's Effective address to index into the MEMORY/PROGRAM array
                            // in order to read one byte from that location and stores the value in the DATA variable 
  } // end ReadMem
  //
  void WriteMem() {
    cpu.MEMORY[EA] = DATA;  // uses the instruction/operand's Effective address to index into the MEMORY/PROGRAM array
                            // in order to store the one byte value in the DATA variable into that location
  } // end WriteMem
//
// START OF Instruction State METHODS
  void FI(){
  // reset flags for the start of a new Instruction cycle
    Unary=false; CO=true; FO=true; WO=false; EA=0; NEA=0; DATA=0; OP=0;  // reset to default values for each new cycle
    this.EA = this.cpu.PC; // prep the EA
    ReadMem(); //fetches ONE byte from memory, an instruction's OP CODE
    this.cpu.IS = this.DATA; // stores it into the cpu.IS
    this.cpu.PC++; //increment PC
    
    if(this.cpu.IS == 0x00 || (this.cpu.IS >= 0x18 && this.cpu.IS <= 0x23)) {  //logical statement to determine if the cpu.IS(opcode) is UNARY )
          Unary = true; cpu.OS = 0;
      if (this.cpu.IS == 0x00) this.cpu.DESCR = "STOP";
    } // if UNARY we are done...otherwise
 
 else { // here we handle the trinary instruction by retrieveing the 2 byte operand specifier
    this.EA = this.cpu.PC; // prep the EA
    ReadMem(); //fetches ONE byte from memory, an instruction's Operand's 1st(HO) byte
    this.cpu.OS = this.DATA << 8;// shifts it 8 bits and stores it into the cpu.OS
    this.cpu.PC++; //increment PC for 2nd(LO) byte
    this.EA = this.cpu.PC; // prep the EA
    ReadMem();//fetches ONE byte from memory, an instruction's Operand's 2nd (LO) byte
    this.cpu.OS += this.DATA; // adds it to cpu.OS, thus storing it.
    this.cpu.PC++; //increment PC
    } //end else
 
  }// end of the FI() method
  //
  void DI(){
  //
    OP = cpu.IS;
    if (Unary)
    {
      cpu.MODE = ' ';
      switch (OP)
      {
        case 0x18: cpu.DESCR = "NOTA"; break;
        case 0x19: cpu.DESCR = "NOTX"; break;
        case 0x1A: cpu.DESCR = "NEGA"; break;
        case 0x1B: cpu.DESCR = "NEGX"; break;
        case 0x1C: cpu.DESCR = "ASLA"; break;
        case 0x1D: cpu.DESCR = "ASLX"; break;
        case 0x1E: cpu.DESCR = "ASRA"; break;
        case 0x1F: cpu.DESCR = "ASRX"; break;
        case 0x20: cpu.DESCR = "ROLA"; break;
        case 0x21: cpu.DESCR = "ROLX"; break;
        case 0x22: cpu.DESCR = "RORA"; break;
        case 0x23: cpu.DESCR = "RORX"; break;
        default:   cpu.DESCR = "STOP"; break;
      }  
      CO = false; FO = false; WO = false;
    } 
    else
    {
      // branch instructions
      if ( OP <= 0x15 )
      {
        switch(OP)
        {
          case 0x04: case 0x05: cpu.DESCR = "BR"; break;
          case 0x06: case 0x07: cpu.DESCR = "BRLE"; break;
          case 0x08: case 0x09: cpu.DESCR = "BRLT"; break;
          case 0x0A: case 0x0B: cpu.DESCR = "BREQ"; break;
          case 0x0C: case 0x0D: cpu.DESCR = "BRNE"; break;
          case 0x0E: case 0x0F: cpu.DESCR = "BRGE"; break;
          case 0x10: case 0x11: cpu.DESCR = "BRGT"; break;
          case 0x12: case 0x13: cpu.DESCR = "BRV"; break;
          case 0x14: case 0x15: cpu.DESCR = "BRC"; break;
          default:   System.err.printf("INVALID"); break;
        } 
        if ( OP % 2 == 0 )
          cpu.MODE = 'i';
        else
          cpu.MODE = 'x';
            
        FO = false;
      } // end of branch instructions
      else{
        switch(OP & 0xF8) //mask for data move mnemonic
        {
          case 0x70: cpu.DESCR = "ADDA"; break;
          case 0x78: cpu.DESCR = "ADDX"; break;
          case 0x80: cpu.DESCR = "SUBA"; break;
          case 0x88: cpu.DESCR = "SUBX"; break;
          case 0x90: cpu.DESCR = "ANDA"; break;
          case 0x98: cpu.DESCR = "ANDX"; break;
          case 0xA0: cpu.DESCR = "ORA"; break;
          case 0xA8: cpu.DESCR = "ORX"; break;
          case 0xB0: cpu.DESCR = "CPA"; break;
          case 0xB8: cpu.DESCR = "CPX"; break;
          case 0xC0: cpu.DESCR = "LDA"; break;
          case 0xC8: cpu.DESCR = "LDX"; break;
          case 0xD0: cpu.DESCR = "LDBYTEA"; break;
          case 0xD8: cpu.DESCR = "LDBYTEX"; break;
          case 0xE0: cpu.DESCR = "STA"; FO = false;  WO = true;  break;
          case 0xE8: cpu.DESCR = "STX"; FO = false;  WO = true; break;
          case 0xF0: cpu.DESCR = "STBYTEA"; FO = false; WO = true; break;
          case 0xF8: cpu.DESCR = "STBYTEX"; FO = false; WO = true; break;
          default:  System.err.printf("INVALID"); break;
        } 
        switch(OP & 0x07) //mask for addressing mode
        {
          case 0x00: cpu.MODE = 'i'; break; //immediate
          case 0x01: cpu.MODE = 'd'; break; //direct
          case 0x02: cpu.MODE = 'n'; break; //indirect
          case 0x05: cpu.MODE = 'x'; break; //indexed
          default:   cpu.MODE = ' '; break;
        }
      }  
    }
  }// end of the DI() method
  //
  void CO(){
    switch (cpu.MODE)
    {
      case 'x': // IF 'indexed' mode
        EA = cpu.OS + cpu.X; // effective address is: the value of the operand specifier plus the contents of the 'X' reg.
        break; 
      case 'n': // IF 'indirect' mode
        // effective address is calculated as follows:
        // 1) use the value of the operand specifier in order to retrieve a two byte address from memory.
        // 2) the two byte address retrieved from memory IS the effective address
        EA = cpu.OS;
        ReadMem();
        NEA = DATA << 8;
        EA++;
        ReadMem();
        NEA += DATA;
        EA = NEA;
        break;
      case 'd': // IF 'direct' mode
        EA = cpu.OS; // effective address is: the value of the operand specifier.
        break;
      case 'i': // IF 'immediate' mode
        // in case of BRanches, the effective address is: the value of the operand specifier.
        // for the other trinary instructions, there is NO effective address needed, instead,
        // the operand value (OP) is the value of the operand specifier
        if ( cpu.IS <= 0x15 && cpu.IS >= 0x04 ) EA = cpu.OS;
        else EA = 0;
        cpu.OP = cpu.OS;
        FO = false;
        break;
      default: break;
    }
  }; // end of the CO() method
  //
  void FO(){
    // if an "LDBYTEA" or "LDBYTEX" instruction:
      // uses the effective address (EA) to fetch ONE byte from memory, an instruction's Operand's Data (1st or ONLY byte)
      // and stores it into the cpu.OP's low order byte
    if (cpu.DESCR.equals("LDBYTEA")||cpu.DESCR.equals("LDBYTEX")) {
      ReadMem();
      cpu.OP = DATA;
    }
    // otherwise, for the rest of trinary instructions:
      // uses the effective address (EA) to fetch TWO bytes from memory, an instruction's Operand's Data (1st/2nd bytes)
      // and stores them into the cpu.OP
    else {
      ReadMem();
      cpu.OP = DATA << 8;
      EA++;
      ReadMem();
      cpu.OP += DATA;
    }
  };// end of the FO() method
  //
  void EX(){
    // the EXECUTE STEP code is structurally similar to the code in the DI() method
             // the difference is that the only ACTION taken here is to INVOKE/CALL the 
       // specific method that will 'execute' the particular instruction!!! nothing else...
       //
     OP = cpu.IS; // local var OP is a modified Instr Spec. and used to bracket the 'switch'
     if(cpu.IS >= 0x70) { OP= (cpu.IS & 0xF8);} // if instr. uses the full address mode, then keep the op code only

       switch (OP)
       {
         case 0x00: System.out.println("STOP INSTRUCTION EXECUTED");break;
         // start of branches bracket
         case 0x04: case 0x05: BR(); break;
         case 0x06: case 0x07: BRLE(); break;
         case 0x08: case 0x09: BRLT(); break;
         case 0x0A: case 0x0B: BREQ(); break;
         case 0x0C: case 0x0D: BRNE(); break;
         case 0x0E: case 0x0F: BRGE(); break;
         case 0x10: case 0x11: BRGT(); break;
         case 0x12: case 0x13: BRV(); break;
         case 0x14: case 0x15: BRC(); break;
         // end of branches bracket
         //
         // start of unary instr; operates on a REG with no operand
         case 0x18: NOTA();break;
         case 0x19: NOTX();break;
         case 0x1A: NEGA();break;
         case 0x1B: NEGX(); break;
         case 0x1C: ASLA(); break;
         case 0x1D: ASLX(); break;
         case 0x1E: ASRA(); break;
         case 0x1F: ASRX(); break;
         case 0x20: ROLA(); break;
         case 0x21: ROLX(); break;
         case 0x22: RORA(); break;
         case 0x23: RORX(); break;
         // end of unary instr
         //
         // the following instructions are specified in range increments of 8
         // with the 3 low order bits having the addressing mode set to 0's
         // for example: the range 0x70/0x77 are all ADDA with the 8 different modes
         case 0x70: ADDA();break;
         case 0x78: ADDX();break;
         case 0x80: SUBA();break;
         case 0x88: SUBX(); break;
         case 0x90: ANDA(); break;
         case 0x98: ANDX(); break;
         case 0xA0: ORA(); break;
         case 0xA8: ORX(); break;
         case 0xB0: CPA(); break;
         case 0xB8: CPX(); break;
         case 0xC0: LDA(); break;
         case 0xC8: LDX(); break;
         case 0xD0: LDBYTEA(); break;
         case 0xD8: LDBYTEX(); break;
         case 0xE0: STA(); break;
         case 0xE8: STX(); break;
         case 0xF0: STBYTEA(); break;
         case 0xF8: STBYTEX(); break;
         default: Unary=false; cpu.DESCR="INVALID";break;
         //
      }// end of the 'switch' structure
  }; // end of the EX() method
  //
  void WO(){
    // if a "STA" or "STX" instruction:
       // uses the effective address (EA) to store two bytes, from the cpu.OP, to memory
    if (cpu.DESCR.equals("STA") || cpu.DESCR.equals("STX")) {
      DATA = (char) ((cpu.OP >> 8)); //DATA = HI byte
      WriteMem();
      EA++;
      DATA = (char)((cpu.OP) & 0x00FF); //DATA = LO byte
      WriteMem();
    }
    // if a "STBYTEA" or "STBYTEX"  instruction:
       // uses the effective address (EA) to store one byte(LO byte) from the cpu.OP, to memory.
    else if (cpu.DESCR.equals("STBYTEA") || cpu.DESCR.equals("STBYTEX")) {
      DATA = (char)(cpu.OP & 0x00FF); //DATA = LO byte
      WriteMem();
    }
  };// end of the WO()
// 
// start of individual instruction execution methods; 
// at this point all we have is the 'shell' for each of the instruction methods
// with a Print statement to show that we actually Switched and Invoked the correct
// instruction method
  // start of of mem/reg instructions(trinary)
  void ADDA(){
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;
    
    int temp = cpu.A;
    cpu.A += cpu.OP;

    if ( (cpu.A & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.A == 0 )
        cpu.Z = 1;
    if ( (temp > 0 && cpu.OP > 0 && cpu.A < 0) || (temp < 0 && cpu.OP < 0 && cpu.A > 0) )
        cpu.V = 1;
    if ( (cpu.A & 0x00010000) == 0x00010000 )
        cpu.C = 1;

    System.out.println("ADDA INSTRUCTION EXECUTED");
  }
  
  void ADDX(){
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;
    
    int temp = cpu.X;
    cpu.X += cpu.OP;

    if ( (cpu.X & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.X == 0 )
        cpu.Z = 1;
    if ( (temp > 0 && cpu.OP > 0 && (cpu.X+cpu.OP) < 0) || (temp < 0 && cpu.OP < 0 && cpu.X > 0) )
        cpu.V = 1;
    if ( (cpu.X & 0x00010000) == 0x00010000 )
        cpu.C = 1;

    System.out.println("ADDX INSTRUCTION EXECUTED");
  }
  
  void SUBA(){
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;
    
    int temp = cpu.A;
    cpu.A -= cpu.OP;

    if ( (cpu.A & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.A == 0 )
        cpu.Z = 1;
    if ( (temp > 0 && (-cpu.OP) > 0 && cpu.A < 0) || (temp < 0 && (-cpu.OP) < 0 && cpu.A > 0) )
        cpu.V = 1;
    //Checking for a ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“CARRYÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ condition when SUBtracting is very complex and we will not attempt it
    //here. I will be willing to explain WHY in class, but for this project we will just ignore it and always set
    //the ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“CÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ bit to ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“0ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢.
    
    System.out.println("SUBA INSTRUCTION EXECUTED");
  }
  
  void SUBX(){
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;
    
    int temp = cpu.X;
    cpu.X -= cpu.OP;

    if ( (cpu.X & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.X == 0 )
        cpu.Z = 1;
    if ( (temp > 0 && (-cpu.OP) > 0 && cpu.X < 0) || (temp < 0 && (-cpu.OP) < 0 && cpu.X > 0) )
        cpu.V = 1;
    //Checking for condition when SUBtracting is very complex and we will not attempt it
    //here. I will be willing to explain WHY in class, but for this project we will just ignore it and always set
    
    System.out.println("SUBX INSTRUCTION EXECUTED");
  }
  
  void ANDA(){
    cpu.N = 0;
    cpu.Z = 0;

    cpu.A = cpu.A & cpu.OP;

    if ( (cpu.A & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    else if ( cpu.A == 0 )
        cpu.Z = 1;

    System.out.println("ANDA INSTRUCTION EXECUTED");
  }
  
  void ANDX(){
    cpu.N = 0;
    cpu.Z = 0;

    cpu.X = cpu.X & cpu.OP;

    if ( (cpu.X & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    else if ( cpu.X == 0 )
        cpu.Z = 1;

    System.out.println("ANDX INSTRUCTION EXECUTED");
  }
  
  void ORA(){
    cpu.N = 0;
    cpu.Z = 0;

    cpu.A = cpu.A | cpu.OP;

    if ((cpu.A & 0x00008000) == 0x00008000)
        cpu.N = 1;
    else if ( cpu.A == 0 )
        cpu.Z = 1;

    System.out.println("ORA INSTRUCTION EXECUTED");
  }
  
  void ORX(){
    cpu.N = 0;
    cpu.Z = 0;

    cpu.X = cpu.X | cpu.OP;

    if ((cpu.X & 0x00008000) == 0x00008000)
        cpu.N = 1;
    else if ( cpu.X == 0 )
        cpu.Z = 1;

    System.out.println("ORX INSTRUCTION EXECUTED");
  }
  
  void CPA(){
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;

    int temp = cpu.A - cpu.OP;

    if ( (temp & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( temp == 0 )
        cpu.Z = 1;
    if ( (cpu.A > 0 && (-cpu.OP) > 0 && temp < 0) || (cpu.A < 0 && (-cpu.OP) < 0 && temp > 0))
        cpu.V = 1;

    System.out.println("CPA INSTRUCTION EXECUTED");
  }
  
  void CPX(){
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;
    
    int temp = cpu.X - cpu.OP;

    if ( (temp & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( temp == 0 )
        cpu.Z = 1;
    if ( (cpu.X > 0 && (-cpu.OP) > 0 && temp < 0) || (cpu.X < 0 && (-cpu.OP) < 0 && temp > 0))
        cpu.V = 1;

    System.out.println("CPX INSTRUCTION EXECUTED");
  }
  
  void LDA(){
    cpu.N = 0;
    cpu.Z = 0;

    cpu.A = cpu.OP;

    if ( cpu.A == 0 )
        cpu.Z = 1;
    else if ( (cpu.A & 0x00008000) == 0x00008000 )
        cpu.N = 1;

    System.out.println("LDA INSTRUCTION EXECUTED");
  }
  
  void LDX(){
    cpu.N = 0;
    cpu.Z = 0;

    cpu.X = cpu.OP;

    if ( (cpu.X & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.X == 0 )
        cpu.Z = 1;

    System.out.println("LDX INSTRUCTION EXECUTED");
  }
  
  void LDBYTEA(){

    cpu.N = 0;
    cpu.Z = 0;

    cpu.A = (cpu.A & 0xFFFFFF00) | (cpu.OP & 0x000000FF);

    if ( (cpu.A & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.A == 0 )
        cpu.Z = 1;       

    System.out.println("LDBYTEA INSTRUCTION EXECUTED");
  }
  
  void LDBYTEX(){

    cpu.N = 0;
    cpu.Z = 0;

    cpu.X = (cpu.X & 0xFFFFFF00) | (cpu.OP & 0x000000FF);

    if ( (cpu.X & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.X == 0 )
        cpu.Z = 1;  

    System.out.println("LDBYTEX INSTRUCTION EXECUTED");
  }
  
  void STA(){
    cpu.OP = cpu.A;
    System.out.println("STA INSTRUCTION EXECUTED");
  }
  
  void STX(){
    cpu.OP = cpu.X;
    System.out.println("STX INSTRUCTION EXECUTED");
  }
  
  void STBYTEA(){
    cpu.OP = cpu.A & 0x000000FF;
    System.out.println("STBYTEA INSTRUCTION EXECUTED");
  }
  
  void STBYTEX(){
    cpu.OP = cpu.X & 0x000000FF;
    System.out.println("STBYTEX INSTRUCTION EXECUTED");
  }
  
  // start of logicals(unary)
  void NOTA()
  {
    cpu.N = 0;
    cpu.Z = 0;

    // use the Ã¢â‚¬Ëœ~Ã¢â‚¬â„¢ java operator and then set the Z/N flags
    cpu.A = ~cpu.A;

    if ((cpu.A & 0x00008000) == 0x00008000)
        cpu.N = 1;
    if ( cpu.A == 0 )
        cpu.Z = 1;
    
    System.out.println("NOTA INSTRUCTION EXECUTED");
  }
  
  void NOTX()
  {
    cpu.N = 0;
    cpu.Z = 0;

    // use the Ã¢â‚¬Ëœ~Ã¢â‚¬â„¢ java operator and then set the Z/N flags
    cpu.X = ~cpu.X;

    if ((cpu.X & 0x00008000) == 0x00008000)
        cpu.N = 1;
    if ( cpu.X == 0 )
        cpu.Z = 1;

    System.out.println("NOTX INSTRUCTION EXECUTED");
  }
  
  void NEGA()
  {
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;

    // for the Ã¢â‚¬ËœVÃ¢â‚¬â„¢ flag, just Ã¢â‚¬ËœthinkÃ¢â‚¬â„¢ about how you would set it
    if (cpu.A < 0) cpu.V = 1;
    // use the Ã¢â‚¬Ëœ-Ã¢â‚¬â„¢ java operator and then set the Z/N flags
    cpu.A = -cpu.A;

    if ((cpu.A & 0x00008000) == 0x00008000)
      cpu.N = 1;
    if ( cpu.A == 0 )
      cpu.Z = 1;
    
    System.out.println("NEGA INSTRUCTION EXECUTED");
  }
  
  void NEGX()
  {
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;

    if (cpu.X < 0) cpu.V = 1;

    cpu.X = -cpu.X;

    if ((cpu.X & 0x00008000) == 0x00008000)
      cpu.N = 1;
    if ( cpu.X == 0 )
      cpu.Z = 1;
    
    System.out.println("NEGX INSTRUCTION EXECUTED");
  }
  
  void ASLA()
  {
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;
    //int temp = cpu.A;

    // copy/save the value of the leftmost Bit to the cpu.C (the Carry).
    if ((cpu.A & 0x00008000) == 0x00008000)
      cpu.C = 1;

    cpu.A = cpu.A << 1;

   if ( ((cpu.A & 0x00008000) == 0x00000000 && cpu.C == 1) || ((cpu.A & 0x00008000) == 0x00008000 && cpu.C == 0))
      cpu.V = 1;
    // and then set the Z/N flags
    if (cpu.A < 0)
      cpu.N = 1;
    if ( cpu.A == 0 )
      cpu.Z = 1;
    
    System.out.println("ASLA INSTRUCTION EXECUTED");
  }
  
  void ASLX()
  {
    cpu.N = 0;
    cpu.Z = 0;
    cpu.V = 0;
    cpu.C = 0;
    int temp = cpu.X;

    // copy/save the value of the leftmost Bit to the cpu.C (the Carry).
    if ((cpu.X & 0x00008000) == 0x00008000)
      cpu.C = 1;

    cpu.X = cpu.X << 1;

    if ( ((cpu.X & 0x00008000) == 0x00000000 && cpu.C == 1) || ((cpu.X & 0x00008000) == 0x00008000 && cpu.C == 0))
      cpu.V = 1;
    // and then set the Z/N flags
    if (cpu.X < 0)
      cpu.N = 1;
    if (cpu.X == 0)
      cpu.Z = 1;
    
    System.out.println("ASLX INSTRUCTION EXECUTED");
  }
  
  void ASRA()
  {
    cpu.N = 0;
    cpu.Z = 0;
    cpu.C = 0;
    
    if ((cpu.A & 0x00000001) == 0x00000001)
      cpu.C = 1;

    // copy/save the value of the leftmost Bit (the Sign) to a temp var(int Sign).
    int sign = cpu.A & 0x00008000;

    cpu.A = cpu.A >> 1;

    cpu.A = (cpu.A & 0x00007FFF) + sign;

    // and then set the Z/N flags
    if ( (cpu.A & 0x00008000) == 0x00008000 )
      cpu.N = 1;
    if ( cpu.A == 0 )
      cpu.Z = 1;
    
    System.out.println("ASRA INSTRUCTION EXECUTED");
  }
  
  void ASRX()
  {
    cpu.N = 0;
    cpu.Z = 0;
    cpu.C = 0;
    
    if ((cpu.X & 0x00000001) == 0x00000001)
      cpu.C = 1;

    // copy/save the value of the leftmost Bit (the Sign) to a temp var(int Sign).
    int sign = cpu.X & 0x00008000;

    cpu.X = cpu.X >> 1;

    cpu.X = (cpu.X & 0x00007FFF) + sign;

    // and then set the Z/N flags
    if ( (cpu.X & 0x00008000) == 0x00008000 )
        cpu.N = 1;
    if ( cpu.X == 0 )
        cpu.Z = 1;
    
    System.out.println("ASRX INSTRUCTION EXECUTED");
  }
  
  void ROLA()
  {

    cpu.N = 0;
    cpu.Z = 0;
//    cpu.C = 1;

    // copy/save the value of the leftmost Bit to a temp var(int LeftC).
    byte LeftC;
    if ((cpu.A & 0x00008000) == 0x00008000) LeftC = 1;
    else LeftC = 0;

    cpu.A = cpu.A << 1;
    // use the cpu.C (current Carry) value to set the RIGHTMOST bit.
    cpu.A += cpu.C;

    cpu.C = LeftC;
    // and then set the Z/N flags
    if ((cpu.A & 0x00008000) == 0x00008000)
      cpu.N = 1;
    if (cpu.A == 0)
      cpu.Z = 1;
    
    System.out.println("ROLA INSTRUCTION EXECUTED");
  }
  
  void ROLX()
  {
    cpu.N = 0;
    cpu.Z = 0;

    // copy/save the value of the leftmost Bit to a temp var(int LeftC).
    int LeftC = cpu.X & 0x00008000;

    cpu.X = cpu.X << 1;
    // use the cpu.C (current Carry) value to set the RIGHTMOST bit.
    cpu.X += cpu.C;

    if ((LeftC & 0x00008000) == 0x00008000)
      cpu.C = 1;
    else
      cpu.C = 0;
    // and then set the Z/N flags
    if ((cpu.X & 0x00008000) == 0x00008000)
      cpu.N = 1;
    if (cpu.X == 0)
      cpu.Z = 1;
    
    System.out.println("ROLX INSTRUCTION EXECUTED");
  }
  
  void RORA()
  {

    cpu.N = 0;
    cpu.Z = 0;

    // copy/save the value of the rightmost Bit to a temp var(int RightC).
    byte RightC;
    if ((cpu.A & 0x00000001) == 0x00000001) RightC = 1;
    else RightC = 0;

    cpu.A = cpu.A >> 1;
    // use the cpu.C (current Carry) value to set the LEFTMOST bit.
    if(cpu.C == 1)
      cpu.A = cpu.A | 0x00008000;

    cpu.C = RightC;
    // and then set the Z/N flags
    if ((cpu.A & 0x00008000) == 0x00008000)
      cpu.N = 1;
    if (cpu.A == 0)
      cpu.Z = 1;
          
    System.out.println("RORA INSTRUCTION EXECUTED");
  }
  
  void RORX()
  {
    cpu.N = 0;
    cpu.Z = 0;

    // copy/save the value of the rightmost Bit to a temp var(int RightC).
    byte RightC;
    if ((cpu.X & 0x00000001) == 0x00000001) RightC = 1;
    else RightC = 0;

    cpu.X = cpu.X >> 1;
    // use the cpu.C (current Carry) value to set the LEFTMOST bit.
    if(cpu.C == 1)
      cpu.X = cpu.X | 0x00008000;

    cpu.C = RightC;
    // and then set the Z/N flags
    if ((cpu.X & 0x00008000) == 0x00008000)
      cpu.N = 1;
    if (cpu.X == 0)
      cpu.Z = 1;

    System.out.println("RORX INSTRUCTION EXECUTED");
  }

  // start of branches 
  void BR(){
    cpu.PC = EA;
    System.out.println("BR INSTRUCTION EXECUTED");
  }
  
  void BRLE(){
    if (cpu.N == 1 || cpu.Z == 1) cpu.PC = EA;
    System.out.println("BRLE INSTRUCTION EXECUTED");
  }
  
  void BRLT(){
    if (cpu.N == 1) cpu.PC = EA;
    System.out.println("BRLT INSTRUCTION EXECUTED");
  }
  
  void BREQ(){
    if (cpu.Z == 1) cpu.PC = EA;
    System.out.println("BREQ INSTRUCTION EXECUTED");
  }
  
  void BRNE(){
    if (cpu.Z == 0) cpu.PC = EA;
    System.out.println("BRNE INSTRUCTION EXECUTED");
  }
  
  void BRGE(){
    if (cpu.N == 0 || cpu.Z == 1) cpu.PC = EA;
    System.out.println("BRGE INSTRUCTION EXECUTED");
  }
  
  void BRGT(){
    if (cpu.N == 0 && cpu.Z == 0) cpu.PC = EA;
    System.out.println("BRGT INSTRUCTION EXECUTED");
  }
  
  void BRV(){
    if (cpu.V == 1) cpu.PC = EA;
    System.out.println("BRV INSTRUCTION EXECUTED");
  }
  
  void BRC(){
    if (cpu.C == 1) cpu.PC = EA;
    System.out.println("BRC INSTRUCTION EXECUTED");
  } 
} // class step