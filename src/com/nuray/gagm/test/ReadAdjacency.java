package com.nuray.gagm.test;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by TOSHIBA on 1/2/2017.
 */
public class ReadAdjacency {

    public static int[][] read(String name)
    {
        int[][] adjacency = null;
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(name));

            String line = bufferedReader.readLine();
            int count = 0;
            String[] temp = line.split("\\s+");

            adjacency = new int[temp.length][temp.length];
            adjacency[0][0] = 0;

            for (int i = 1; i < temp.length; i++)
            {
                if (temp[i].equals("."))
                {
                    adjacency[count][i] = Integer.MAX_VALUE;
                } else
                {
                    adjacency[count][i] = Integer.parseInt(temp[i]);
                }

            }
            while (line != null)
            {
                line = bufferedReader.readLine();
                count++;
                if (line == null)
                {
                    break;
                }
                temp = line.split("\\s+");

                for (int i = 0; i < temp.length; i++)
                {
                    if (temp[i].equals("."))
                    {
                        if (i == count)
                        {
                            adjacency[count][i] = 0;
                        } else
                        {
                            adjacency[count][i] = Integer.MAX_VALUE;
                        }
                    } else
                    {
                        adjacency[count][i] = Integer.parseInt(temp[i]);
                    }
                }
            }

            bufferedReader.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return adjacency;
    }
}
