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
}
