package tests.unit.sim;

/**
 * Minimal dense binary classifier for gate cloning (NEXT #185 S1).
 *
 * <p>Zero dependencies, Java 8, fully deterministic: fixed-seed weight
 * init, fixed data order, full-batch Adam - the same inputs always train
 * the same net, so the suite can pin the result instead of a distribution.
 * Sigmoid output, binary cross-entropy loss, tanh hidden layers. Small on
 * purpose: the S1 dataset is thousands of rows, and the suite budget
 * (CONVENTIONS §12) rules out anything bigger.</p>
 */
public final class SimMLP {

    private static final double BETA1 = 0.9;
    private static final double BETA2 = 0.999;
    private static final double EPS = 1e-8;
    private final int[] sizes;
    private final double[][][] weights;
    private final double[][] biases;
    private final double[][][] mW;
    private final double[][] mB;
    private final double[][][] vW;
    private final double[][] vB;
    private int adamStep;

    public SimMLP(long seed, int... sizes) {
        if (sizes == null || sizes.length < 2) {
            throw new IllegalArgumentException("need at least input + output layers");
        }
        this.sizes = sizes.clone();
        this.weights = new double[sizes.length - 1][][];
        this.biases = new double[sizes.length - 1][];
        this.mW = new double[sizes.length - 1][][];
        this.mB = new double[sizes.length - 1][];
        this.vW = new double[sizes.length - 1][][];
        this.vB = new double[sizes.length - 1][];

        java.util.Random rng = new java.util.Random(seed);
        for (int l = 0; l < sizes.length - 1; l++) {
            int rows = sizes[l + 1];
            int cols = sizes[l];
            double limit = Math.sqrt(6.0 / (rows + cols));
            weights[l] = new double[rows][cols];
            biases[l] = new double[rows];
            mW[l] = new double[rows][cols];
            mB[l] = new double[rows];
            vW[l] = new double[rows][cols];
            vB[l] = new double[rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    weights[l][i][j] = (rng.nextDouble() * 2 - 1) * limit;
                }
            }
        }
        this.adamStep = 0;
    }

    private static double[] activate(double[] z, boolean output) {
        double[] a = new double[z.length];
        if (output) {
            for (int i = 0; i < z.length; i++) a[i] = 1.0 / (1.0 + Math.exp(-z[i]));
        }
        else {
            for (int i = 0; i < z.length; i++) a[i] = Math.tanh(z[i]);
        }
        return a;
    }

    /**
     * Output probability for one feature row.
     */
    public double predictProb(double[] x) {
        double[] a = x.clone();
        for (int l = 0; l < sizes.length - 1; l++) {
            double[] z = new double[sizes[l + 1]];
            for (int i = 0; i < sizes[l + 1]; i++) {
                double sum = biases[l][i];
                for (int j = 0; j < sizes[l]; j++) sum += weights[l][i][j] * a[j];
                z[i] = sum;
            }
            a = activate(z, l == sizes.length - 2);
        }
        return a[0];
    }

    /**
     * Mean binary cross-entropy over the rows.
     */
    public double meanLoss(double[][] xs, double[] ys) {
        double total = 0;
        for (int n = 0; n < xs.length; n++) {
            double p = Math.min(1 - 1e-12, Math.max(1e-12, predictProb(xs[n])));
            total += -(ys[n] * Math.log(p) + (1 - ys[n]) * Math.log(1 - p));
        }
        return total / xs.length;
    }

    /**
     * Full-batch Adam for a fixed epoch count. Returns the final mean
     * loss. Deterministic: no shuffling, no sampling, fixed update order.
     */
    public double train(double[][] xs, double[] ys, int epochs, double lr) {
        double[][][] gW = new double[weights.length][][];
        double[][] gB = new double[biases.length][];
        for (int l = 0; l < weights.length; l++) {
            gW[l] = new double[weights[l].length][weights[l][0].length];
            gB[l] = new double[biases[l].length];
        }

        double loss = meanLoss(xs, ys);
        for (int e = 0; e < epochs; e++) {
            for (int l = 0; l < gW.length; l++) {
                for (double[] row : gW[l]) java.util.Arrays.fill(row, 0);
                java.util.Arrays.fill(gB[l], 0);
            }
            for (int n = 0; n < xs.length; n++) {
                backprop(xs[n], ys[n], gW, gB);
            }
            double inv = 1.0 / xs.length;
            adamStep++;
            double fix1 = 1 - Math.pow(BETA1, adamStep);
            double fix2 = 1 - Math.pow(BETA2, adamStep);
            for (int l = 0; l < weights.length; l++) {
                for (int i = 0; i < weights[l].length; i++) {
                    for (int j = 0; j < weights[l][i].length; j++) {
                        double g = gW[l][i][j] * inv;
                        mW[l][i][j] = BETA1 * mW[l][i][j] + (1 - BETA1) * g;
                        vW[l][i][j] = BETA2 * vW[l][i][j] + (1 - BETA2) * g * g;
                        weights[l][i][j] -= lr * (mW[l][i][j] / fix1)
                            / (Math.sqrt(vW[l][i][j] / fix2) + EPS);
                    }
                    double gb = gB[l][i] * inv;
                    mB[l][i] = BETA1 * mB[l][i] + (1 - BETA1) * gb;
                    vB[l][i] = BETA2 * vB[l][i] + (1 - BETA2) * gb * gb;
                    biases[l][i] -= lr * (mB[l][i] / fix1)
                        / (Math.sqrt(vB[l][i] / fix2) + EPS);
                }
            }
            loss = meanLoss(xs, ys);
        }
        return loss;
    }

    private void backprop(double[] x, double y, double[][][] gW, double[][] gB) {
        int layers = sizes.length;
        double[][] a = new double[layers][];
        double[][] z = new double[layers - 1][];
        a[0] = x.clone();
        for (int l = 0; l < layers - 1; l++) {
            z[l] = new double[sizes[l + 1]];
            for (int i = 0; i < sizes[l + 1]; i++) {
                double sum = biases[l][i];
                for (int j = 0; j < sizes[l]; j++) sum += weights[l][i][j] * a[l][j];
                z[l][i] = sum;
            }
            a[l + 1] = activate(z[l], l == layers - 2);
        }

        double p = a[layers - 1][0];
        double[] delta = new double[]{p - y};
        for (int l = layers - 2; l >= 0; l--) {
            for (int i = 0; i < sizes[l + 1]; i++) {
                for (int j = 0; j < sizes[l]; j++) {
                    gW[l][i][j] += delta[i] * a[l][j];
                }
                gB[l][i] += delta[i];
            }
            if (l > 0) {
                double[] prev = new double[sizes[l]];
                for (int j = 0; j < sizes[l]; j++) {
                    double sum = 0;
                    for (int i = 0; i < sizes[l + 1]; i++) sum += weights[l][i][j] * delta[i];
                    prev[j] = sum * (1 - a[l][j] * a[l][j]);
                }
                delta = prev;
            }
        }
    }
}
