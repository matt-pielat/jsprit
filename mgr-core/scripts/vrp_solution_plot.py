import sys
import matplotlib.pyplot as plt
import numpy as np
from xml.dom import minidom
from matplotlib.lines import Line2D
from matplotlib.colors import LinearSegmentedColormap

def get_color(demand):
    return demand/100.0

if __name__ == "__main__":
    input_path = sys.argv[1]
    output_path = sys.argv[2]

    cmap_colors = [(0, 0, 1), (1, 0, 0)]
    custom_cmap = LinearSegmentedColormap.from_list('cmapname', cmap_colors, N=200)

    figure = plt.figure(figsize=(12, 12))

    ax = figure.add_subplot(1, 1, 1)

    routes = []

    with open(input_path) as f:
        line = f.readline() # read depot
        depot_x, depot_y, depot_d = [float(i) for i in line.strip().split()]

        line = f.readline() # read new line

        xs, ys, ds = [depot_x], [depot_y], [0.0]
        while True:
            line = f.readline()
            if not line or line.strip() == '':
                xs.append(depot_x), ys.append(depot_y), ds.append(0.0)
                routes.append((xs, ys, ds))
                xs, ys, ds = [depot_x], [depot_y], [0.0]
                if not line:
                    break
                continue
            x, y, d = [float(i) for i in line.strip().split()]
            xs.append(x), ys.append(y), ds.append(d)
    
    i = 0
    for xs, ys, ds in routes:
        route_size = len(xs)
        colors = [get_color(d) for d in ds]
        ax.scatter(xs, ys, marker='.', c = colors, cmap = custom_cmap) 
    ax.plot(depot_x, depot_y, marker='s', color='black', markersize=15)

    plt.axis('equal')
    # plt.axis('off')

    plt.tick_params(
        axis='both',
        which='both',
        bottom=False, top=False, left=False, right=False,
        labelbottom=False, labeltop=False, labelleft=False, labelright=False)
    
    legend_elements = [
        Line2D([0], [0], marker='s', color='w', label='Baza pojazdów', markerfacecolor='black', markersize=10),
        Line2D([0], [0], marker='.', color='w', label='Niskie zapotrzebowanie', markerfacecolor=(0, 0, 1), markersize=15),
        Line2D([0], [0], marker='.', color='w', label='Średnie zapotrzebowanie', markerfacecolor=(0.5, 0, 0.5), markersize=15),
        Line2D([0], [0], marker='.', color='w', label='Wysokie zapotrzebowanie', markerfacecolor=(1, 0, 0), markersize=15)
    ]
    ax.legend(handles=legend_elements)


    plt.savefig(output_path, bbox_inches='tight')