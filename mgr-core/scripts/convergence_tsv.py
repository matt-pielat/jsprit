import sys
import numpy as np
import pandas as pd
import xml.etree.ElementTree as ET
from scipy import interpolate

def read_data_from_file(file_path):
    costs = []
    times = []
    xml = ET.parse(file_path)
    for ic in xml.getroot().find("intermediateCosts").iter("ic"):
        costs.append(float(ic.get("cost")))
        times.append(int(ic.get("timeInMs")))
    return costs, times

if __name__ == "__main__":
    output_path = sys.argv[1]
    input_paths = sys.argv[2:]

    all_times = np.concatenate((
        np.arange(0,            1e3 * 10,       200,    dtype=int), 
        np.arange(1e3 * 10,     1e3 * 60,       500,    dtype=int),
        np.arange(1e3 * 60,     1e3 * 180,      1000,   dtype=int),
        np.arange(1e3 * 180,    1e3 * 601,      2000,   dtype=int),
        ))

    all_costs = np.empty([all_times.size, len(input_paths)])

    for i in range(len(input_paths)):
        input_path = input_paths[i]

        costs, times = read_data_from_file(input_path)
        times.append(0)
        costs.append(max(costs))
        times.append(1e3 * 600)
        costs.append(min(costs))

        f = interpolate.interp1d(times, costs)
        all_costs[:, i] = f(all_times)

    data = {
        "time in ms": all_times.tolist(),
        "mean": np.mean(all_costs, axis=1).tolist(),
        "standard deviation": np.std(all_costs, axis=1).tolist(),
        "min": np.min(all_costs, axis=1).tolist(),
        "max": np.max(all_costs, axis=1).tolist()
    }

    df = pd.DataFrame(data)
    df.to_csv(path_or_buf=output_path, sep="\t", index=False, decimal=",")