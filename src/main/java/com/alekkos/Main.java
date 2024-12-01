package com.alekkos;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Created by Aristofanis Lekkos on 12/1/2024
 */
public class Main {
    public static void main(String[] args) {
        stage1();
        stage2();
    }

    public static void stage1() {
        try {
            Pair data = Pair.fromFile("stage1.txt");
            long distance = 0L;
            for (int i = 0; i < data.left.size(); i++) {
                distance += Math.abs(data.left.get(i) - data.right.get(i));
            }
            System.out.println("Distance " + distance);
        } catch (IOException | URISyntaxException e) {
            System.out.println("Error loading file");
        }
    }

    public static void stage2() {
        try {
            Pair data = Pair.fromFile("stage1.txt");
            Long similarity = data.left.stream()
                    .map(l -> {
                        long count = data.right.stream().filter(r -> Objects.equals(r, l)).count();
                        return l * count;
                    })
                    .reduce(0L, Long::sum);
            System.out.println("Similarity sum " + similarity);
        } catch (Exception e) {
            System.out.println("Error in stage 2");
            e.printStackTrace();
        }
    }

    private static List<coordinates> readFile(URL resource) throws IOException, URISyntaxException {
        System.out.println("Loading " + resource.toURI());
        return Files.lines(Path.of(resource.toURI()))
                .map($ -> $.split("   "))
                .map($ -> new coordinates($[0], $[1]))
                .toList();
    }

    record coordinates(String left, String right) {
    }

    record Pair(List<Long> left, List<Long> right) {
        public static Pair fromFile(String file) throws IOException, URISyntaxException {
            URL resource = Main.class.getClassLoader().getResource(file);
            List<coordinates> fileData = readFile(resource);
            List<Long> left = fileData.stream().map(coordinates::left)
                    .map(Long::parseLong).sorted().toList();
            List<Long> right =
                    fileData.stream().map(coordinates::right)
                            .map(Long::parseLong).sorted().toList();
            return new Pair(left, right);
        }
    }
}