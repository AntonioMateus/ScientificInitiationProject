package br.Color3;

public class EuclideanDistance {
    private double[] difference;

    public EuclideanDistance(int imageQuantity){
        difference = new double[imageQuantity];
    }

    public double[] calculateDistance (double[][] matrix, int id){
        for (int i = 0; i < matrix.length; i++) {
            double sum = 0;
            double root;
            for(int j = 0; j < matrix.length; j++)
            {
                sum = sum + (Math.pow(((matrix[id][j]) - (matrix[i][j])),2));

            }
            root = Math.sqrt(sum);
            difference[i] = root;
        }

        return difference;
    }
}

