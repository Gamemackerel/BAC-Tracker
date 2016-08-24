package com.example.abe.bac_dr1;

import java.util.*;
public class BAC {
   
   
   private double gStomachEthanol;         //Pool 1: alcohol in stomach that has been ingested but not digested.
   private double gBloodEthanol;           //Pool 2: alcohol in bloodstream. This value can be used to determine BAC 
   private double gMetabolizedEthanol;     //Pool 3: metabolized alcohol, non-essential info.
   
   
   
   
   
   
   //metabolization constants
   private double weight; // in kg
//   private double height;
//   private double age;
   private boolean male;
   private double bodyWater; // in 100s of ml
   private int build;  // between -2 and +2. 0 is average, negatives are fatter, positives are more athletic

   private String dataPoints;



   
   public BAC(double kg, boolean male, int build) {
      this.weight = kg;
      this.male = male;
      this.build = build;
      if(male) 
         bodyWater = weight * (.58 + .04*build/* +- .08 depending on build. higher ratio of muscle mass to fat means larger body water*/) * 10;  
      else
         bodyWater = weight * (.48 + .03*build/* +- .06*/) * 10; 
   }

   public void takeShot(double volume/*ml*/, double percentEthanol /* (by volume) */) {
      double mlIngest = volume * (percentEthanol / 100);
      double gIngest =  (0.7892 * mlIngest);
      gStomachEthanol += gIngest;
   }
   
   public void absorbUpdate() {
      dataPoints += getBAC() + " ";
      double absorbRate = gStomachEthanol * (.25 / 6);
      // what (fractional?) mass of alcohol is absorbed into the blood from the stomach per minute?
      // currently my dummy value estimates that 25% of the alcohol in stomach is absorbed per minute
       //this is converted to every 10 seconds so (25 / 6)% is absorbed per 10 seconds
      
      gStomachEthanol -= absorbRate;
      gBloodEthanol += absorbRate;
   }
   
   public void metabolizeUpdate() {
      double metabRate = (((.00025 /*+- .000083333*/) / 6) * bodyWater);
      // my alcohol elimination rate is based on -.015g/100ml-bodywater per hour
      // converted to subtracting total grams of ethanol per hour

       //READ BELOW!
       //if I want to update every minute, the rate will be .00025 +- .000083333
       // so if I want to update every 10 seconds (6 times as many updates per minute)
       // my metabRate per 10 seconds should be 1/6th of the minutely rate

       //currently I have it updating every 10 seconds which is why I am dividing the minutely rate by 6
      
      gBloodEthanol -= Math.min(metabRate, gBloodEthanol);
      gMetabolizedEthanol += Math.min(metabRate, gBloodEthanol);      
   }
   
   public double getBAC() {
      double BAC = gBloodEthanol / bodyWater;
      //BAC is percentage by mass of ethanol in g per 100 ml blood 
      // so 80g ethanol / 100ml bodyWater is legal limit 
      System.out.println("    total ethanol in stomach (g):             "+ gStomachEthanol);
      System.out.println("    total ethanol in blood (g):               "+ gBloodEthanol);
      System.out.println("    BAC ethanol(g) / body water (100ml):      "+ BAC);
      
      return BAC;
   }

   //returns a string sequential list of BAC values every 10 seconds since start of object, as well as projected BAC values
   //returns in this format: "x x x x x x | z z z z z z" where each value is a BAC estimation 10 seconds later than the last one.
   // x is recorded bac values, z is predicted future bac values
   public String getGraphData(){
      double gStomachEthanol = this.gStomachEthanol;
      double gBloodEthanol = this.gBloodEthanol;
      double BACSim = this.getBAC();
      String result = dataPoints + "# ";
      while(BACSim > 0) {
         result += BACSim + " ";

         double absorbRate = gStomachEthanol * (.25 / 6);
         gStomachEthanol -= absorbRate;
         gBloodEthanol += absorbRate;

         double metabRate = (((.00025 /*+- .000083333*/) / 6) * bodyWater);
         gBloodEthanol -= Math.min(metabRate, gBloodEthanol);

         BACSim = gBloodEthanol / bodyWater;
      }
      return result + BACSim;
   }
   
   
   
}