package run.runnable.calculatepiclooklike.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Asher
 */
public class Calculate {

    /**
     * 可选值为：1,2,4,8,16,32
     * 当值为64时会抛出异常，此时需要实现64位转10进制
     * radix 64 greater than Character.MAX_RADIX
     */
    public static int compareLevel = 4;

    public static void main(String[] args) throws IOException {
        final String pic1Path = Objects.requireNonNull(Calculate.class.getClassLoader().getResource("pic1.jpeg")).getPath();
        final String pic2Path = Objects.requireNonNull(Calculate.class.getClassLoader().getResource("pic2.jpeg")).getPath();
        final List<Double> origin = getPicArrayData(pic1Path);
        System.out.println(origin);
        final List<Double> after = getPicArrayData(pic2Path);
        System.out.println(after);
        System.out.println(PearsonDemo.getPearsonBydim(origin, after));
    }


    public static List<Double> getPicArrayData(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));

        //初始化集合
        final List<Double> picFingerprint = new ArrayList<>(compareLevel*compareLevel*compareLevel);
        IntStream.range(0, compareLevel*compareLevel*compareLevel).forEach(i->{
            picFingerprint.add(i, 0.0);
        });
        //遍历像素点
        for(int i = 0; i < image.getWidth(); i++){
            for(int j = 0; j < image.getHeight(); j++){
                Color color = new Color(image.getRGB(i, j));
                //对像素点进行计算
                putIntoFingerprintList(picFingerprint, color.getRed(), color.getGreen(), color.getBlue());
            }
        }

        return picFingerprint;
    }

    /**
     * 放入像素的三原色进行计算，得到List的位置
     * @param picFingerprintList picFingerprintList
     * @param r r
     * @param g g
     * @param b b
     * @return
     */
    public static List<Double> putIntoFingerprintList(List<Double> picFingerprintList, int r, int g, int b){
        //比如r g b是126, 153, 200 且 compareLevel为16进制，得到字符串：79c ,然后转10进制，这个数字就是List的位置
        final Integer index = Integer.valueOf(getBlockLocation(r) + getBlockLocation(g) + getBlockLocation(b), compareLevel);
        final Double origin = picFingerprintList.get(index);
        picFingerprintList.set(index, origin + 1);
        return picFingerprintList;
    }

    /**w
     * 计算 当前原色应该分在哪个区块
     * @param colorPoint colorPoint
     * @return
     */
    public static String getBlockLocation(int colorPoint){
        return IntStream.range(0, compareLevel)
                //以10进制计算分在哪个区块
                .filter(i -> {
                    int areaStart = (256 / compareLevel) * i;
                    int areaEnd = (256 / compareLevel) * (i + 1) - 1;
                    return colorPoint >= areaStart && colorPoint <= areaEnd;
                })
                //如果compareLevel大于10则转为对应的进制的字符串
                .mapToObj(location -> compareLevel > 10 ? Integer.toString(location, compareLevel) : location+"")
                .findFirst()
                .orElseThrow();
    }

    public static void putIntoFingerprintMap(Map<Integer, Integer> picFingerprintMap, int r, int g, int b){
        final Integer picFingerprint = Integer.valueOf(getBlockLocation(r) + getBlockLocation(g) + getBlockLocation(b), compareLevel);
        Integer value = picFingerprintMap.containsKey(Integer.valueOf(picFingerprint)) ? picFingerprintMap.get(Integer.valueOf(picFingerprint)) + 1 : 1;
        picFingerprintMap.put(Integer.valueOf(picFingerprint), value);
    }


    public static List<Double> getPicArrayDataByMap(String path) throws IOException {
        BufferedImage bimg = ImageIO.read(new File(path));

        final Map<Integer, Integer> picFingerprintMap = new HashMap<>();

        for(int i = 0; i < bimg.getWidth(); i++){
            for(int j = 0; j < bimg.getHeight(); j++){
                //输出一列数据比对
                Color color = new Color( bimg.getRGB(i, j));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                putIntoFingerprintMap(picFingerprintMap, r, g, b);
            }
        }

        final List<Integer> keys = picFingerprintMap.keySet().stream().sorted().collect(Collectors.toList());
        final ArrayList<Double> picFingerprintList = new ArrayList<>(keys.size());
        keys.forEach(key->{
            picFingerprintList.add(Double.valueOf(picFingerprintMap.get(key)));
        });
        return picFingerprintList;
    }



}
