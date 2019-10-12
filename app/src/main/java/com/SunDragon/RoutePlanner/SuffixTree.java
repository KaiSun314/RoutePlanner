package com.SunDragon.RoutePlanner;

import java.util.ArrayList;

public class SuffixTree {

    public static class Edge {
        public int from;
        public int to;
        public Node child;
    }

    public static class Node {
        public Node parent;
        public int depth;
        public int[] bestFive = new int[5];
        public ArrayList<Edge> edges = new ArrayList<>();
        public int suffix = -1;
    }
}
