import sys
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib.ticker import MaxNLocator

if __name__ == "__main__":
    input_path = sys.argv[1]
    output_path = sys.argv[2]

    figure = plt.figure(figsize=(12, 4))
    ax = figure.add_subplot(1, 1, 1)

    plt.xticks(rotation=90)

    ax.set_axisbelow(True)
    ax.yaxis.grid(color='#dbdbdb', linestyle='--')
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))

    df = pd.read_csv(input_path, delimiter='\t')
    df = df[df['time windows'] == True]
    df = df[['id', 'best known k', 'jsprit best k', 'eh-dvrp best k']]

    labels = df.values[:,0]
    best_ks = df.values[:,1].astype(int)
    jsprit_ks = df.values[:,2].astype(int)
    ehdvrp_ks = df.values[:,3].astype(int)

    best_ss = []
    jsprit_ss = []
    ehdvrp_ss = []
    for i in range(len(labels)):
        best_s = jsprit_s = ehdvrp_s = 80
        if best_ks[i] == jsprit_ks[i]:
            best_s = 30
            jsprit_s = 80
            if best_ks[i] == ehdvrp_ks[i]:
                ehdvrp_s = 160
        elif best_ks[i] == ehdvrp_ks[i]:
            best_s = 30
            ehdvrp_s = 80
        elif jsprit_ks[i] == ehdvrp_ks[i]:
            jsprit_s = 30
            ehdvrp_s = 80
        best_ss.append(best_s)
        jsprit_ss.append(jsprit_s)
        ehdvrp_ss.append(ehdvrp_s)
        pass

    ax.scatter(labels, ehdvrp_ks, s = ehdvrp_ss)
    ax.scatter(labels, jsprit_ks, s = jsprit_ss)
    ax.scatter(labels, best_ks, s = best_ss)

    legend = ax.legend(["EH-DVRP", "jSprit", "Najlepsze znane"], loc='upper left')

    legend.legendHandles[0]._sizes[0] = 80
    legend.legendHandles[1]._sizes[0] = 80
    legend.legendHandles[2]._sizes[0] = 80
    
    ax.set_ylabel('Liczba tras najlepszego rozwiązania')

    plt.savefig(output_path, bbox_inches='tight')