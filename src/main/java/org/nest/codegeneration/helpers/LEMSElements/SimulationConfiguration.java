package org.nest.codegeneration.helpers.LEMSElements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class stores all configuration of the simulation as handed over it the simulation script.
 *
 * @author perun
 */
public class SimulationConfiguration {
  //indicates whether units and dimensions have to be generated externally
  boolean unitsExternal = true;
  double simSteps;
  List<List<String>> instructions;


  public SimulationConfiguration(Path configPath,boolean ext,double simSteps){
    this.unitsExternal = ext;
    this.simSteps = simSteps;
    this.instructions = new ArrayList<>();
    if(configPath==null){
      System.err.println("Could not read external artifact.");
      return;
    }
    try{
      this.adaptSettings(configPath);
    }catch(IOException exception){
      System.err.println("Problems with the external artifact! Settings are set to standard.");
    }
  }

  /**
   * Reads an external simulation script and extracts all required information.
   * @param path A path to the external script file.
   * @throws IOException thrown if non file is given.
   */
  private void adaptSettings(Path path) throws  IOException{
    try{
    Scanner input = new Scanner(new File(path.toAbsolutePath().toString()));
    String temp;
    List<String>  tempInstructions = new ArrayList<>();
    int commentIndex;
    while(input.hasNextLine()){
      temp = input.nextLine().replaceAll("\\s\\t","");//kill white spaces
      if(!temp.startsWith("#")){//ignore comments lines completely
        if(temp.contains("#")){//comment is present, ignore everything after the # symbol
          commentIndex = temp.indexOf("#");
          if(commentIndex!=-1){
            temp = temp.substring(0,commentIndex);
          }
        }
        if (temp.endsWith(":")) {
          if (tempInstructions.size()>0){
            instructions.add(tempInstructions);//add the preceding block
          }
          tempInstructions = new ArrayList<>();
          tempInstructions.add(temp.replaceAll(":|\\s|\\t",""));//kill white spaces and the : symbol
        }
        else{
          if(!temp.trim().isEmpty()){//not a whitespace line
            tempInstructions.add(temp.replaceAll("\\t",""));
          }
        }
      }
    }
      if(tempInstructions.size()>0){
        instructions.add(tempInstructions);//add the preceding block
      }
    }
    catch(NullPointerException excep){
      System.err.println("No external artifact provided!");
    }

  }

  public boolean isUnitsExternal() {
    return unitsExternal;
  }

  public List getInstructions(){
    return this.instructions;
  }

  public double getSimSteps(){
    return this.simSteps;
  }

}
