package com.alekkos;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Aristofanis Lekkos on 12/1/2024
 */
public class Main {
    public static void main(String[] args) {
        stage1();
        stage1b();
        stage2();
        stage2b();
        stage3();
    }

    public static void stage1() {
        try {
            Pair data = Pair.fromFile("stage1.txt");
            long distance = calculateDistance(data);
            System.out.println("Stage1: Distance " + distance);
        } catch (IOException | URISyntaxException e) {
            System.out.println("Error loading file");
        }
    }

    public static long calculateDistance(Pair data) {
        long distance = 0L;
        for (int i = 0; i < data.left.size(); i++) {
            distance += Math.abs(data.left.get(i) - data.right.get(i));
        }
        return distance;
    }

    public static void stage1b() {
        try {
            Pair data = Pair.fromFile("stage1.txt");
            Long similarity = calculateSimilarity(data);
            System.out.println("Stage1b: Similarity sum " + similarity);
        } catch (Exception e) {
            System.out.println("Error in stage 2");
            e.printStackTrace();
        }
    }

    public static long calculateSimilarity(Pair data) {
        return data.left.stream()
                .map(l -> {
                    long count = data.right.stream().filter(r -> Objects.equals(r, l)).count();
                    return l * count;
                })
                .reduce(0L, Long::sum);
    }

    public static void stage2() {
        try {
            List<Integer[]> data = loadReports("stage2.txt");
            long count = data.stream().map($ -> isValidReport($, 0))
                    .filter(ReportValidationResult::valid)
                    .count();
            System.out.println("Stage2: Valid reports count " + count + "/" + data.size());
        } catch (Exception e) {
            System.out.println("Error in stage2");
            e.printStackTrace();
        }
    }

    public static void stage2b() {
        try {
            List<Integer[]> data = loadReports("stage2b.txt");
            long count = data.stream().map($ -> isValidReport($, 1))
                    .filter(ReportValidationResult::valid)
                    .count();
            System.out.println("Stage2b: Valid reports count " + count + "/" + data.size());
        } catch (Exception e) {
            System.out.println("Error in stage2b");
            e.printStackTrace();
        }
    }

    public static void stage3() {
        try {
            URL resource = Main.class.getClassLoader().getResource("stage3.txt");
            List<String> data = readFile(resource);
            Pattern pattern = Pattern.compile("mul\\(\\d{1,3},\\d{1,3}\\)");
            Integer finalResult = data.stream().map(d -> {
                        Matcher m = pattern.matcher(d);
                        return m.results().map(r -> {
                                    String g = r.group();
                                    String[] s = g.trim().replace("mul(", "")
                                            .replace(")", "")
                                            .split(",");
                                    int a = Integer.parseInt(s[0]);
                                    int b = Integer.parseInt(s[1]);
                                    System.out.println(r.group());
                                    return a * b;
                                })
                                .reduce(0, Integer::sum);
                    })
                    .reduce(0, Integer::sum);
            System.out.println("Final result " + finalResult);

        } catch (Exception e) {
            System.out.println("Error in stage3");
            e.printStackTrace();
        }
    }

    private static Integer[] removeElement(Integer[] data, int position) {
        ArrayList<Integer> tmp = new ArrayList<>(Arrays.asList(data));
        tmp.remove(position);
        return tmp.toArray(new Integer[0]);
    }

    public static ReportValidationResult isValidReport(Integer[] report, int maxErrors) {
        LevelState state = LevelState.NONE;
        int totalDistance = 0;
        int errors = 0;
        for (int i = 0; i < report.length - 1; i++) {
            if (report[i] - report[i + 1] > 0) {
                if (state.equals(LevelState.DECREASING)) {
                    errors++;
                    errors = checkSubReport(report, i) ? errors : errors + 1;
                    continue;
                }
                state = LevelState.INCREASING;
            } else if (report[i] - report[i + 1] < 0) {
                if (state.equals(LevelState.INCREASING)) {
                    errors++;
                    errors = checkSubReport(report, i) ? errors : errors + 1;
                    continue;
                }
                state = LevelState.DECREASING;
            } else {
                // no increase or decrease
                errors++;
                errors = checkSubReport(report, i) ? errors : errors + 1;
                continue;
            }
            int distance = Math.abs(report[i] - report[i + 1]);
            if (distance < 1 || distance > 3) {
                errors++;
                errors = checkSubReport(report, i) ? errors : errors + 1;
            } else {
                totalDistance += distance;
            }
        }
        boolean isOk = errors <= maxErrors;
        return new ReportValidationResult(isOk, totalDistance, errors);
    }

    private static boolean checkSubReport(Integer[] data, int pos) {
        Integer[] d1 = removeElement(data, pos);
        Integer[] d2 = pos == data.length ? data : removeElement(data, pos + 1);
        return isValidReport(d1, 0).valid() && isValidReport(d2, 0).valid();
    }

    enum LevelState {
        INCREASING, DECREASING, NONE
    }

    private static List<coordinates> readCoordinates(URL resource) {
        try {
            List<String> data = readFile(resource);
            return data.stream().map($ -> $.split("   "))
                    .map($ -> new coordinates($[0], $[1]))
                    .toList();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Integer[]> loadReports(String fileName) {
        URL resource = Main.class.getClassLoader().getResource(fileName);
        try {
            return readFile(resource).stream()
                    .map(row -> {
                        String[] sp = row.split(" ");
                        Integer[] d = new Integer[sp.length];
                        for (int i = 0; i < sp.length; i++) {
                            d[i] = Integer.parseInt(sp[i]);
                        }
                        return d;
                    }).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> readFile(URL resource) throws IOException, URISyntaxException {
        System.out.println("Loading " + resource.toURI());
        return Files.lines(Path.of(resource.toURI())).toList();
    }

    record coordinates(String left, String right) {
    }

    record Pair(List<Long> left, List<Long> right) {
        public static Pair fromFile(String file) throws IOException, URISyntaxException {
            URL resource = Main.class.getClassLoader().getResource(file);
            List<coordinates> fileData = readCoordinates(resource);
            List<Long> left = fileData.stream().map(coordinates::left)
                    .map(Long::parseLong).sorted().toList();
            List<Long> right =
                    fileData.stream().map(coordinates::right)
                            .map(Long::parseLong).sorted().toList();
            return new Pair(left, right);
        }
    }

    public record ReportValidationResult(boolean valid, int distance, int errorsFound) {
    }
}