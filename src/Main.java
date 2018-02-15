import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by Josh Harkness on 1/30/2018.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        //////////////////////////////////////////////////////////enter file name here ///////////////////////////////////////////////////////////
        File schedules = new File("mhky80-89\\mhky80-89.txt");//use the file named
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Scanner reader = new Scanner(schedules); //prepare to read the file
        String file = "";//start of making the file into a string
        Pattern rows = Pattern.compile("<tr(.*?)</tr>"); //pattern matcher used to find each row of the table
        String header = "Date,Opponent,Location,H/A/N,Result,Team Score,Opponent Score,Conference Game"; //header of the csv file to be created
        String date = "", opp = "", loc = "", han = "", conf = "";
        Pattern cellDetails = Pattern.compile("px;\">(.*?)</td>");//used to get stats from each game
        Matcher cellMatcher, yearMatcher;
        String sheetYear = "", cellContents, line;
        Pattern yearPattern = Pattern.compile("<em>(.*?)</em>|<i>(.*?)</i>");
        String[] conferenceTeams = {"Brockport", "Buffalo State", "Cortland", "Fredonia", "Geneseo", "New Paltz",
                "Oneonta", "Plattsburg", "Potsdam", "Morrisville State"};
        int cellNum = 0, oswScore = -1, oppScore = -1;
        String yearChecker = "1979-80";
        PrintWriter writer = null;
        String result = "";
        Pattern firstScore = Pattern.compile("\\w,\\s*(\\d+)-\\d+.*");
        Pattern secondScore = Pattern.compile("\\w,\\s*\\d+-(\\d+).*");
        Pattern homeCellContents = Pattern.compile("<strong>(.*?)</strong>|<b>(.*?)</b>");
        Matcher scoreMatcher, homeCellMatcher;


        while (reader.hasNextLine()) { //makes file a giant one line string
            file = file + reader.nextLine();
        }

        Matcher rowMatcher = rows.matcher(file); //uses the matcher on the giant file string
        //System.out.print(file);

        while (rowMatcher.find()) { //while the pattern exists or while more games are left, write to a csv file
            line = rowMatcher.group(1); //sets the line equal to one row
            cellMatcher = cellDetails.matcher(line);//matches everything in a cell
            while (cellMatcher.find()) { //while the row has cells
                cellContents = cellMatcher.group(1);
                if (cellContents.contains("<u>")) { //if an underline is in the cell, go to the next line
                    break;
                } else if(cellContents.contains("<em>") || cellContents.contains("<i>")){ //only creates new writer if it has entered a new year
                    yearMatcher = yearPattern.matcher(cellContents);//finds only the year in the cell
                    if (yearMatcher.matches()) {
                        sheetYear = yearMatcher.group(1);
                        if(sheetYear == null) {
                            sheetYear = yearMatcher.group(2);
                        }
                        if (!sheetYear.equals(yearChecker)) {
                            writer.close();
                        }
                        ////////////////////////////////new files to write to ////////////////////////////////////////////////////////////////
                        writer = new PrintWriter("mhky80-89\\mhky" + sheetYear + ".csv");
                        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        writer.println(header);
                        break; //exits examination of this row
                    } else {
                        System.out.println("Couldn't find the year.");
                        break;
                    }
                } else { //if not a line with the year in it
                    if (!(cellContents.equals("") || cellContents.matches("\\s+"))) {
                        if(cellContents.contains("<strong>")) {
                            han = "H";
                            loc = "Oswego N.Y.";
                            homeCellMatcher = homeCellContents.matcher(cellContents);
                            if(homeCellMatcher.matches()){
                                cellContents = homeCellMatcher.group(1);
                            } else {
                                System.out.println("Home cell matcher did not work");
                            }
                        } else if(cellContents.contains("<b>")){
                            han = "H";
                            loc = "Oswego N.Y.";
                            homeCellMatcher = homeCellContents.matcher(cellContents);
                            if(homeCellMatcher.matches()){
                                cellContents = homeCellMatcher.group(2);
                            } else {
                                System.out.println("Home cell matcher did not work");
                            }
                        } else if (Integer.parseInt(sheetYear.substring(0,4)) > 1966) {
                            han = "A";
                            loc = "";
                        }
                        String firstChar = String.valueOf(cellContents.charAt(0));
                        if (firstChar.matches("[#&*^$%~]")){
                            String secondChar = String.valueOf(cellContents.charAt(1));
                            if (!secondChar.equals(" ")) {
                                cellContents = cellContents.substring(1);
                            } else {
                                cellContents = "";
                            }
                        }
                    }
                }
                cellNum++;
                if (cellNum == 4) {//resets the cell counter for the next row
                    cellNum = 1;
                }
                if (cellNum == 1) {
                    date = cellContents;
                } else if (cellNum == 2) {
                    opp = cellContents;
                } else if (cellNum == 3) {
                    oswScore = -1;
                    oppScore = -1;
                    if (!(cellContents.equals("") || cellContents.matches("\\s+")
                            || cellContents.equals("*Missing two games")
                            || cellContents.equals("* Missing three game scores (1 win, 2 losses)"))) { //if the third cell is not blank
                        result = String.valueOf(cellContents.charAt(0));
                        if (cellContents.length() > 1) { //if a score was given
                            int firstPoints, secondPoints;
                            scoreMatcher = firstScore.matcher(cellContents);
                            if(scoreMatcher.matches()) {
                                firstPoints = Integer.parseInt(scoreMatcher.group(1));
                                scoreMatcher = secondScore.matcher(cellContents);
                                if(scoreMatcher.matches()){
                                    secondPoints = Integer.parseInt(scoreMatcher.group(1));
                                } else {
                                    secondPoints = -1;
                                }
                            } else {
                                firstPoints = -1;
                                secondPoints = -1;
                            }
                            if (result.equals("W")) { //set oswScore to the higher of the points
                                if (firstPoints > -1 && secondPoints > -1) {
                                    if (firstPoints > secondPoints) {
                                        oswScore = firstPoints;
                                        oppScore = secondPoints;
                                    } else {
                                        oswScore = secondPoints;
                                        oppScore = firstPoints;
                                    }
                                }
                            } else { //set oswScore to the lower of the points
                                if (firstPoints > -1 && secondPoints > -1) {
                                    if (firstPoints >= secondPoints) {
                                        oppScore = firstPoints;
                                        oswScore = secondPoints;
                                    } else {
                                        oppScore = secondPoints;
                                        oswScore = firstPoints;
                                    }
                                }
                            }
                        }
                    } else { //no score or result
                        result = "";
                    }
                    if (!(opp.equals("") || opp.matches("\\s+"))) {// if no score but an opponent exists
                        if (Arrays.asList(conferenceTeams).contains(opp)) {
                            conf = "Y";
                        } else {
                            conf = "N";
                        }
                    } else {
                        conf = "";
                    }
                }
            }
            ////////////////////////////////////////////////writes to the empty files created before hand using PowerShell////////////////////////////////////////
            if (!sheetYear.equals(yearChecker)) {
                yearChecker = String.valueOf(Integer.parseInt(yearChecker.substring(0,4)) + 1) + "-" +
                        String.valueOf(Integer.parseInt(yearChecker.substring(5)) + 1);
            } else {
                if (!writer.equals(null)) {
                    if (oswScore > -1 && oppScore > -1) {
                        writer.println(date + "," + opp + "," + loc + "," + han + "," + result + "," + oswScore + "," +
                                oppScore + "," + conf);
                        //System.out.println(sheetYear);
                        //System.out.println(date + "," + opp + "," + loc + "," + han + "," + result + "," + oswScore + "," + oppScore + "," + conf);
                    } else if (!(opp.equals("") || opp.matches("\\s+"))) {
                       writer.println(date + "," + opp + "," + loc + "," + han + "," + result + "," + "" + "," +
                               "" + "," + conf);
                        //System.out.println(sheetYear);
                        //System.out.println(date + "," + opp + "," + loc + "," + han + "," + result + "," + "" + "," + "" + "," + conf);
                    }
                } else {
                    System.out.println("Failed to write");
                }
            }
        }
        writer.close();
    }
}
