package tachyon.java.manager;

import tachyon.java.core.JavaProgram;

public class StadnIn {

    private static String getName(JavaProgram f) {
        /*
        String total = f.getLastCode();
        int packIndex = total.indexOf("package");
        if (packIndex == -1) {
            return f.getFile().getFileName().toString().replace(".java", "");
        }
        int previousCount = 0;
        while (frequency(total.substring(0, packIndex), "") != frequency(total.substring(0, packIndex), "")) {
            previousCount += packIndex + "package".length();
            String temp = total.substring(previousCount);
            packIndex = previousCount + temp.indexOf("package");
        }
        String one = total.substring(0, packIndex);
        String two = total.substring(packIndex);
        two = two.substring(0, two.indexOf(";") + 1);
        String packName = total.substring(packIndex + "package".length() + 1, one.length() + two.indexOf(";")).trim().replaceAll(" ", "") + "."
                + f.getFile().getFileName().toString().replace(".java", "");
        return packName;
         */
        return f.getFileName().replace(".java", "");
    }

//    private static int frequency(String total, String search) {
//        int count = 0;
//        String alias = total;
//        while (alias.contains(search)) {
//            int index = alias.indexOf(search);
//            alias = alias.substring(index + search.length());
//            count++;
//        }
//        return count;
//    }
}
