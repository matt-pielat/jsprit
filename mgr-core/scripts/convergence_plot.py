import sys
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib.ticker import FormatStrFormatter

if __name__ == "__main__":
    output_path = sys.argv[1]
    input_args = sys.argv[2:]

    figure = plt.figure(figsize=(5, 3))
    ax = figure.add_subplot(1, 1, 1)
    ax.xaxis.set_major_formatter(FormatStrFormatter('%ds'))

    max_end_mean = -np.inf
    min_end_mean = np.inf

    labels = []

    for i in range(0, len(input_args), 2):
        labels.append(input_args[i])
        input_path = input_args[i+1]

        df = pd.read_csv(input_path, delimiter='\t', decimal=',')

        times = df.values[:,0].astype(int) / 1000
        mean = df.values[:,1].astype(float)
        stddev = df.values[:,2].astype(float)
        minimum = df.values[:,3].astype(float)
        maximum = df.values[:,4].astype(float)

        max_end_mean = max(max_end_mean, mean[-1])
        min_end_mean = min(min_end_mean, mean[-1])

        ax.plot(times, mean)

    ax.set_ylim(ymin=0.97 * min_end_mean, ymax=1.10 * max_end_mean)
    ax.legend(labels)
    ax.set_xlabel('Czas działania')
    ax.set_ylabel('Średni koszt rozwiązania')

    plt.savefig(output_path, bbox_inches='tight')
    # plt.show()