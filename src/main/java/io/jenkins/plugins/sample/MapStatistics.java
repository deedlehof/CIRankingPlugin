//package io.jenkins.plugins.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.Collections;

public class MapStatistics {

  public static MapStatistics INSTANCE = new MapStatistics();

  private MapStatistics() { 
  }

  public static MapStatistics getInstance() {
    return INSTANCE;
  }

  /**
  *
  *
  * @param input
  * @param results	
  * @return	
  */
  /*
  static <K, V extends Comparable<V>> Map<K, V> topScores(Map<K, V> input, int results) {
    return input;
  }
  
  static <K, V extends Comparable<V>> V kthSmallest(Map<K, V> input, int k) {
    @SuppressWarnings("unchecked")
    Entry<K, V>[] mapEntries = (Entry<K, V>[])input.entrySet().toArray();
    return kthSmallest(mapEntries, 0, input.size(), k);
  }  
  
  private static <K, V extends Comparable<V>> V kthSmallest(Entry<K, V>[] mapEntries, int left, int right, int k) {
    // Check that k is within bounds
    if (k > 0 && k <= left - right + 1) {
      // Number of elements in the array
      int numElements = right - left + 1;

      // Divide the array into groups with size 5 and
      // calculate the median 
      int i;
      // There are floor((numElements+4)/5) groups
      @SuppressWarnings("unchecked")
      V[] medians = (V[])new Object[(numElements + 4) / 5];
      for (i = 0; i < numElements / 5; i += 1) {
        medians[i] = findMedian(mapEntries, 1 + i * 5, 5);
      }
      
      if (i * 5 < numElements) {
        medians[i] = findMedian(mapEntries, 1 + i * 5, numElements % 5);
	i += 1;
      } 
      

    }
    return mapEntries[0].getValue();
  }

  */
  public static <K, V extends Comparable<V>> V findMedian(List<Entry<K, V>> mapEntries, int start, int end) {
    if (start > end) {
      int tmp = start;
      start = end;
      end = start;
    } 
    mapEntries.subList(start, end).forEach(System.out::println);

    Collections.sort(mapEntries.subList(start, end), new Comparator<Entry<K, V>>() {
      @Override
      public int compare(Entry<K, V> e1, Entry<K, V> e2) {
        return e1.getValue().compareTo(e2.getValue());
      } 
    });
    System.out.println(start + ((end - start) / 2));
    return mapEntries.get(start + ((end - start) / 2)).getValue();
  }
  
  
  public static void main(String [] args) {
    MapStatistics stats = MapStatistics.INSTANCE;
    Map<String, Double> map = new HashMap<>();
    map.put("first", 1.0);
    map.put("one 'n half", 1.5);
    map.put("two", 2.0);
    map.put("five", 4.5);
    map.put("three", 3.0);
    List<Entry<String, Double>> mapEntries = new ArrayList<>(map.entrySet());
    System.out.println(stats.findMedian(mapEntries, 2, 5));
  }
}
