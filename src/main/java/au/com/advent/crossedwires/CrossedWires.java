package au.com.advent.crossedwires;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CrossedWires {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossedWires.class);

    private static Set<Point> firstWire;
    private static Set<Point> secondWire;

    private static List<String> firstWirePaths = new ArrayList<>();
    private static List<String> secondWirePaths = new ArrayList<>();

    private static List<Integer> distances = new ArrayList<>();

    private static HashMap<Point,Integer> firstWireIntersectionMapper = new HashMap<>();
    private static HashMap<Point,Integer> secondWireIntersectionMapper = new HashMap<>();

//    final static String firstWireTest1 = "R75,D30,R83,U83,L12,D49,R71,U7,L72";
//    final static String secondWireTest1 = "U62,R66,U55,R34,D71,R55,D58,R83";
//
//    final static String firstWireTest2 = "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51";
//    final static String secondWireTest2  = "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7";

    public static void main (String[] args) {

        // get wires from input txt
        getWires("src/main/resources/wire1.txt", firstWirePaths, "");
        getWires("src/main/resources/wire2.txt", secondWirePaths, "");

        // initialise wires
        firstWire = new HashSet<>();
        secondWire = new HashSet<>();

        // break down input into coordinates (x, y)
        getWireCoordinates(firstWirePaths, firstWire, null, null);
        getWireCoordinates(secondWirePaths, secondWire, null, null);

        // retail all sets that match between first and second wire
        firstWire.retainAll(secondWire);

        // calculate Manhattan distance
        LOGGER.info("Shortest Manhattan distance is: {}", getManhattanDistance(firstWire).get(0));

        // add the retained wires into another variable
        HashSet<Point> intersections = new HashSet<>(firstWire);

        // initialise wires
        firstWire = new HashSet<>();
        secondWire = new HashSet<>();

        // break down input into coordinates (x, y) this time checking intersections and assigning the amount of steps into a mapper
        getWireCoordinates(firstWirePaths, firstWire, intersections, firstWireIntersectionMapper);
        getWireCoordinates(secondWirePaths, secondWire, intersections, secondWireIntersectionMapper);

        // a list to store total combined steps of each intersections
        List<Integer> combinedSteps = new ArrayList<>();

        // loop through intersections
        for (Point intersection : intersections) {
            int firstWireSteps = firstWireIntersectionMapper.get(intersection);
            int secondWireSteps = secondWireIntersectionMapper.get(intersection);
            int combinedWireSteps = firstWireSteps + secondWireSteps;
            LOGGER.debug("Combined Steps of Intersections x ({}) and y ({}) of both wire1 ({}) and wire2 ({}): {}",
                    intersection.x, intersection.y, firstWireSteps, secondWireSteps, combinedWireSteps);
            combinedSteps.add(combinedWireSteps);
        }

        // sort the steps
        List<Integer> sortedCombinedSteps = combinedSteps.stream().sorted().collect(Collectors.toList());

        LOGGER.info("Fewest combines steps of an intersection: {}", sortedCombinedSteps.get(0));
    }

    private static List<Integer> getManhattanDistance(Set<Point> wireSet) {

        int x1 = 0;
        int y1 = 0;
        int x2;
        int y2;
        int distance = 0;

        for (Point intersection : wireSet) {
            x2 = intersection.x;
            y2 = intersection.y;

            // formula of Manhattan distance
            distance = Math.abs(x1-x2) + Math.abs(y1-y2);

            LOGGER.debug("Manhattan distance of x2 {} and y2 {} is: {}", x2, y2, distance);
            distances.add(distance);
        }

        // sort list of distances
        List<Integer> sortedDistances = distances.stream().sorted().collect(Collectors.toList());

        return sortedDistances;
    }

    private static void getWireCoordinates(List<String> wirePaths, Set<Point> wireSet, Set<Point> intersection, HashMap<Point,Integer> intersectionMapper) {

        int x = 0;
        int y = 0;
        int steps = 0;

        for (String wirePath: wirePaths) {

            // Get the direction command i.e 'U', 'D', 'L', 'R'
            char direction = wirePath.charAt(0);
            // Get the pointer or steps
            int pointer = Integer.parseInt(wirePath.replaceAll("[^0-9.]",""));

            switch(direction) {
                case 'D':
                    for (int i = 1; i <= pointer; i++) {
                        x--;
                        if (!Objects.isNull(intersection)) {
                            steps++;
                            if (checkIntersection(x, y, intersection)) {
                                intersectionMapper.put(new Point(x,y), steps);
                            }
                        }
                        wireSet.add(new Point(x, y));

                    }
                    break;
                case 'U':
                    for (int i = 1; i <= pointer; i++) {
                        x++;
                        if (!Objects.isNull(intersection)) {
                            steps++;
                            if (checkIntersection(x, y, intersection)) {
                                intersectionMapper.put(new Point(x,y), steps);
                            }
                        }
                        wireSet.add(new Point(x, y));
                    }
                    break;
                case 'L':
                    for (int i = 1; i <= pointer; i++) {
                        y--;
                        if (!Objects.isNull(intersection)) {
                            steps++;
                            if (checkIntersection(x, y, intersection)) {
                                intersectionMapper.put(new Point(x,y), steps);
                            }
                        }
                        wireSet.add(new Point(x, y));
                    }
                    break;
                case 'R':
                    for (int i = 1; i <= pointer; i++) {
                        y++;
                        if (!Objects.isNull(intersection)) {
                            steps++;
                            if (checkIntersection(x, y, intersection)) {
                                intersectionMapper.put(new Point(x,y), steps);
                            }
                        }
                        wireSet.add(new Point(x, y));
                    }
                    break;
            }
        }
    }

    private static boolean checkIntersection(int x, int y, Set<Point> intersections) {

        return intersections.stream()
                .anyMatch(intersection -> (x == intersection.x && y == intersection.y));
    }

    private static void getWires(String wireDirPath, List<String> wireLists, String testData) {

        final File wireFile = new File(wireDirPath);

        String[] wirePaths;

        try (BufferedReader br = new BufferedReader(new FileReader(wireFile))) {

            if (!testData.isEmpty()) {
                wirePaths = testData.split(",");
            } else {
                wirePaths = br.readLine().split(",");
            }

            initialiseWireLists(wirePaths, wireLists);

        } catch (Exception ex) {
            LOGGER.error("Exception occurred: {0}", ex);
            throw new RuntimeException("Exception occurred");
        }

    }

    private static void initialiseWireLists(String[] wirePaths, List<String> wireLists) {
        Collections.addAll(wireLists, wirePaths);
    }
}
