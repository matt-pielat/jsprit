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
    df['relative cost'] = df.apply(lambda row: row['eh-dvrp best cost'] / row['jsprit best cost'] - 1, axis=1)
    df = df.sort_values(by=['relative cost'])

    labels = df['id'].values
    relative_costs = df['relative cost'].values.astype(float)

    plt.xticks(rotation=90)
    ax.bar(labels, relative_costs)

    ax.set_ylim(ymin=-0.2, ymax=0.2)

    ax.set_ylabel('Koszt względny')

    ax.text(0.985, 0.96, '(wartości ujemne - przewaga EH-DVRP)', verticalalignment='top', horizontalalignment='right', transform=ax.transAxes, color='gray')

    plt.savefig(output_path, bbox_inches='tight')