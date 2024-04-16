package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
     private Comparator<T> arrayComparator;
     public MaxArrayDeque(Comparator<T> c) {
          arrayComparator = c;
     }

     public T max() {
          if (this.isEmpty()) return null;

          T maxReturn = this.get(0);
          for(T item : this) {
               if (arrayComparator.compare(item, maxReturn) > 0) {
                    maxReturn = item;
               }
          }
          return maxReturn;
     }

     public T max (Comparator<T> c) {
          if (this.isEmpty()) return null;

          T maxReturn = this.get(0);
          for(T item : this) {
               if (c.compare(item, maxReturn) > 0) {
                    maxReturn = item;
               }
          }
          return maxReturn;
     }
}
