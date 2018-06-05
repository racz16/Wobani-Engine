package wobani.core;

import java.util.*;

public class Grid {

    private static int size = 100;

    private Grid() {
    }

    public static int getSize() {
	return size;
    }

    public static void setSize(int size) {
	if (size <= 0) {
	    throw new IllegalArgumentException("Size must be positive");
	}
	Grid.size = size;
	Arrays.sort(null, String::compareToIgnoreCase);
    }
}
