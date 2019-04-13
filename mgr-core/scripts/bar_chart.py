import sys
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib.ticker import FuncFormatter

if __name__ == "__main__":
    input_path = sys.argv[1]
    output_path = sys.argv[2]
    ids = set(sys.argv[3:])
    
    figure = plt.figure(figsize=(12, 4))
    ax = figure.add_subplot(1, 1, 1)
    ax.yaxis.set_major_formatter(FuncFormatter(lambda y, _: '{:.0%}'.format(y)))

    ax.set_axisbelow(True)
    ax.yaxis.grid(color='#dbdbdb', linestyle='--')

    df = pd.read_csv(input_path, delimiter='\t')
    df = df[df.id.isin(ids)]
    df = df[['id', 'jsprit cost mean', 'eh-dvrp cost mean']]

    labels = df.values[:,0]
    jsprit_mean = df.values[:,1].astype(float)
    ehdvrp_mean = df.values[:,2].astype(float)
    relative_costs = np.divide(ehdvrp_mean, jsprit_mean) - 1

    plt.xticks(rotation=90)
    ax.bar(labels, relative_costs)

    ax.set_ylim(ymin=-0.2, ymax=0.2)

    ax.set_ylabel('Współczynnik różnicy kosztu')

    plt.savefig(output_path, bbox_inches='tight')
    # plt.show()