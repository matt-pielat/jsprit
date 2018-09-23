package pl.pielat.util;

public class ArrayUtils
{
    public static void reverseArray(Object[] arr, int length)
    {
        for (int i = 0; i < length / 2; i++)
        {
            Object temp = arr[i];
            arr[i] = arr[length - i - 1];
            arr[length - i - 1] = temp;
        }
    }

    public static void reverseArray(double[] arr, int length)
    {
        for (int i = 0; i < length / 2; i++)
        {
            double temp = arr[i];
            arr[i] = arr[length - i - 1];
            arr[length - i - 1] = temp;
        }
    }

    public static void reverseArray(Object[] arr, int beginIdx, int endIdx)
    {
        int length = endIdx - beginIdx;
        for (int i = 0; i < length / 2; i++)
        {
            Object temp = arr[beginIdx + i];
            arr[beginIdx + i] = arr[endIdx - i - 1];
            arr[endIdx - i - 1] = temp;
        }
    }

    public static void reverseArray(double[] arr, int beginIdx, int endIdx)
    {
        int length = endIdx - beginIdx;
        for (int i = 0; i < length / 2; i++)
        {
            double temp = arr[beginIdx + i];
            arr[beginIdx + i] = arr[endIdx - i - 1];
            arr[endIdx - i - 1] = temp;
        }
    }
}
