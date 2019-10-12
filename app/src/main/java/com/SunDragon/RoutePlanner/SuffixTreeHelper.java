package com.SunDragon.RoutePlanner;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.SunDragon.RoutePlanner.SuffixTree.*;

public class SuffixTreeHelper {
    private static final int NUM_LETTERS = 256;
    public static final int INF = 1000000;

    private static int[] constructSuffixArray(String txt) {
        int n = txt.length();
        int[] idx = new int[n];
        final int[] rnk = new int[2*n];
        int[] tmp = new int[n];
        LinkedList<Integer>[] init = new LinkedList[NUM_LETTERS];
        for (int i=0; i<NUM_LETTERS; i++) {
            init[i] = new LinkedList<>();
        }
        for (int i=0; i<n; i++) {
            rnk[i] = txt.charAt(i);
            init[rnk[i]].add(i);
        }

        for (int i=n; i<2*n; i++) {
            rnk[i] = -INF;
        }

        int cnt = 0;
        for (int i=0; i<NUM_LETTERS; i++) {
            while (init[i].size() != 0) {
                idx[cnt] = init[i].poll();
                cnt++;
            }
        }

        for (int offset=1; offset<n; offset+=offset) {
            for (int l=0, r=1; r<=n; r++) {
                if (r == n || rnk[idx[l]] != rnk[idx[r]]) {
                    final int offsetTemp = offset;
                    Integer[] idxTemp = new Integer[r-l];
                    for (int i=l; i<r; i++) {
                        idxTemp[i-l] = idx[i];
                    }
                    Arrays.sort(idxTemp, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer idx1, Integer idx2) {
                            return rnk[idx1 + offsetTemp] - rnk[idx2 + offsetTemp];
                        }
                    });
                    for (int i=l; i<r; i++) {
                        idx[i] = idxTemp[i-l];
                    }
                    l = r;
                }
            }

            for (int i=0; i<n; i++) {
                tmp[idx[i]] = ((i>0 && rnk[idx[i]] == rnk[idx[i-1]] && rnk[idx[i]+offset] == rnk[idx[i-1]+offset]) ? tmp[idx[i-1]] : i);
            }

            System.arraycopy(tmp, 0, rnk, 0, n);
        }

        return idx;
    }

    private static int[] constructLCPArray(String txt, int[] suffixArray) {
        int n = txt.length();
        int k = 0;
        int[] lcp = new int[n];
        int[] rnk = new int[n];

        lcp[0] = 0;

        for (int i=0; i<n; i++) {
            rnk[suffixArray[i]] = i;
        }

        for (int i=0; i<n; i++) {
            if (rnk[i] == n-1) {
                k = 0;
                continue;
            }
            int j = suffixArray[rnk[i]+1];
            while (i+k < n && j+k < n && txt.charAt(i+k) == txt.charAt(j+k)) k++;
            lcp[rnk[i]+1] = k;
            if (k > 0) {
                k--;
            }
        }
        return lcp;
    }

    private static void dfs(Node node) {
        if (node.suffix != -1) {
            node.bestFive = new int[] { node.suffix, INF, INF, INF, INF };
        } else {
            for (int i=0; i<node.edges.size(); i++) {
                dfs(node.edges.get(i).child);
            }

            ArrayList<Integer> best = new ArrayList<>();
            for (int i=0; i<node.edges.size(); i++) {
                for (int j=0; j<5; j++) {
                    best.add(node.edges.get(i).child.bestFive[j]);
                }
            }
            Collections.sort(best);
            int idx = 0;
            ArrayList<String> searchHistory = new ArrayList<>();
            for (int i=0; idx < 5 && i<best.size(); i++) {
                if (best.get(i) != INF) {
                    if (searchHistory.contains(MapsActivity.mSearchHistoryRegex[MapsActivity.mCharToPos[best.get(i)]])) continue;
                    searchHistory.add(MapsActivity.mSearchHistoryRegex[MapsActivity.mCharToPos[best.get(i)]]);
                }
                node.bestFive[idx] = best.get(i);
                idx++;
            }
        }
    }

    public static Node constructSuffixTree(String txt) {
        txt = prepare(txt);

        if (txt.length() == 0) {
            Node root = new Node();
            root.parent = null;
            root.depth = 0;
            root.bestFive = new int[] { INF, INF, INF, INF, INF };
            return root;
        }

        int n = txt.length();
        int[] suffixArray = constructSuffixArray(txt);
        int[] lcp = constructLCPArray(txt, suffixArray);

        Node root = new Node();
        root.parent = null;
        root.depth = 0;

        Node curNode = root;

        for (int i=0; i<suffixArray.length; i++) {
            while (curNode.depth > lcp[i]) {
                curNode = curNode.parent;
            }

            if (curNode.depth == lcp[i]) {
                Node leaf = new Node();
                leaf.parent = curNode;
                leaf.depth = n-suffixArray[i];
                leaf.suffix = suffixArray[i];

                Edge e = new Edge();
                e.from = suffixArray[i] + curNode.depth;
                e.to = n-1;
                e.child = leaf;

                curNode.edges.add(e);

                curNode = leaf;
            } else {
                Node prev = curNode.edges.get(curNode.edges.size()-1).child;
                curNode.edges.remove(curNode.edges.size()-1);

                Node internal = new Node();
                internal.parent = curNode;
                internal.depth = lcp[i];

                Edge e1 = new Edge();
                e1.from = suffixArray[i-1] + curNode.depth;
                e1.to = suffixArray[i-1] + lcp[i]-1;
                e1.child = internal;

                curNode.edges.add(e1);

                Edge e2 = new Edge();
                e2.from = suffixArray[i-1] + lcp[i];
                e2.to = suffixArray[i-1] + prev.depth-1;
                e2.child = prev;

                prev.parent = internal;

                internal.edges.add(e2);

                Node leaf = new Node();
                leaf.parent = internal;
                leaf.depth = n-suffixArray[i];
                leaf.suffix = suffixArray[i];

                Edge e3 = new Edge();
                e3.from = suffixArray[i] + lcp[i];
                e3.to = n-1;
                e3.child = leaf;

                internal.edges.add(e3);

                curNode = leaf;
            }
        }

        dfs(root);

        return root;
    }

    public static int[] getSearchHistoryResults(String query, int idx, Node node) {
        for (int i=0; i<node.edges.size(); i++) {
            int idxTemp = idx;
            int j = node.edges.get(i).from;
            while (idxTemp < query.length() && j <= node.edges.get(i).to) {
                if (j < MapsActivity.mSearchHistory.length() && toLowercase(query.charAt(idxTemp)) == toLowercase(MapsActivity.mSearchHistory.charAt(j))) {
                    idxTemp++;
                    j++;
                } else {
                    break;
                }
            }

            if (idxTemp == query.length()) {
                return node.edges.get(i).child.bestFive;
            } else if (j > node.edges.get(i).to) {
                return getSearchHistoryResults(query, idxTemp, node.edges.get(i).child);
            }
        }
        return new int[] { INF, INF, INF, INF, INF };
    }

    private static String prepare(String txt) {
        StringBuilder preparedTxt = new StringBuilder();
        for (int i=0; i<txt.length(); i++) {
            preparedTxt.append(toLowercase(txt.charAt(i)));
        }
        return preparedTxt.toString() + "$";
    }

    private static char toLowercase(char c) {
        if (c >= 'A' && c <= 'Z') return (char) (c + 'a' - 'A');
        return c;
    }

}
